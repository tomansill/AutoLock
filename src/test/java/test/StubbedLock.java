package test;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opentest4j.AssertionFailedError;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.fail;

public class StubbedLock implements Lock, AutoCloseable {


	@Override
	public void close() {
		confirmNoMoreExpectedCalls();
	}

	@NonNull
	private final AtomicReference<ESupplier<Boolean>> onTryLockInstantRunnable = new AtomicReference<>();

	@NonNull
	private final AtomicReference<ERunnable> onUnlockRunnable = new AtomicReference<>();

	@NonNull
	private final AtomicInteger callIndex = new AtomicInteger(0);

	void setOnLock(@NonNull ERunnable onLock) {
		this.onLockRunnable.set(onLock);
	}
	@NonNull
	private final AtomicReference<ERunnable> onLockRunnable = new AtomicReference<>();

	@SuppressWarnings("unchecked")
	static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
		throw (T) t;
	}
	@NonNull
	private final AtomicReference<ERunnable> onLockInterruptiblyRunnable = new AtomicReference<>();

	public void confirmNoMoreExpectedCalls() {
		if (onLockRunnable.get() != null) {
			fail("lock() waiting to be called");
		}
		if (onUnlockRunnable.get() != null) {
			fail("unlock() waiting to be called");
		}
		if (onLockInterruptiblyRunnable.get() != null) {
			fail("lockInterruptibly() waiting to be called");
		}
		if (onTryLockInstantRunnable.get() != null) {
			fail("tryLock() waiting to be called");
		}
		if (onTryLockTimeoutRunnable.get() != null) {
			fail("tryLock(long,TimeUnit) waiting to be called");
		}
	}
	@NonNull
	private final AtomicReference<TryLockWithTimeoutFunction> onTryLockTimeoutRunnable = new AtomicReference<>();

	private final List<CallEvent> actualEvents = new CopyOnWriteArrayList<>();

	@NonNull List<CallEvent> getActualEvents() {
		return Collections.unmodifiableList(actualEvents);
	}

	@Override
	public void lock() {
		actualEvents.add(new CallEvent(this, callIndex.getAndIncrement(), Thread.currentThread(), Event.LOCK));
		ERunnable runnable = onLockRunnable.getAndSet(null);
		if (runnable == null) fail("unstubbed");
		try {
			runnable.run();
		} catch (Throwable e) {
			sneakyThrow(e);
		}
	}

	void setOnLockInterruptibly(@NonNull ERunnable onLock) {
		this.onLockInterruptiblyRunnable.set(onLock);
	}

	void setOnUnlock(@NonNull ERunnable onUnlock) {
		this.onUnlockRunnable.set(onUnlock);
	}

	public void setOnTryLockInstant(@NonNull ESupplier<Boolean> onTryLock) {
		this.onTryLockInstantRunnable.set(onTryLock);
	}


	public void setOnTryLockTimeout(@NonNull TryLockWithTimeoutFunction onTryLock) {
		this.onTryLockTimeoutRunnable.set(onTryLock);
	}

	@Override
	public void unlock() {
		actualEvents.add(new CallEvent(this, callIndex.getAndIncrement(), Thread.currentThread(), Event.UNLOCK));
		ERunnable runnable = onUnlockRunnable.getAndSet(null);
		if (runnable == null) fail("unstubbed");
		try {
			runnable.run();
		} catch (Throwable e) {
			sneakyThrow(e);
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		actualEvents.add(new CallEvent(this, callIndex.getAndIncrement(), Thread.currentThread(), Event.LOCK_INTERRUPTIBLY));
		ERunnable runnable = onLockInterruptiblyRunnable.getAndSet(null);
		if (runnable == null) fail("unstubbed");
		try {
			runnable.run();
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			sneakyThrow(e);
		}
	}

	@Override
	public boolean tryLock() {
		actualEvents.add(new CallEvent(this, callIndex.getAndIncrement(), Thread.currentThread(), Event.TRY_LOCK));
		ESupplier<Boolean> function = onTryLockInstantRunnable.getAndSet(null);
		if (function == null) fail("unstubbed");
		try {
			return function.get();
		} catch (Throwable e) {
			sneakyThrow(e);
			throw new AssertionFailedError("unreachable error");
		}
	}

	@Override
	public boolean tryLock(long time, @NonNull TimeUnit unit) throws InterruptedException {
		actualEvents.add(new CallEvent(this, callIndex.getAndIncrement(), Thread.currentThread(), Event.TRY_LOCK_TIMEOUT, time, unit));
		TryLockWithTimeoutFunction function = onTryLockTimeoutRunnable.getAndSet(null);
		if (function == null) fail("unstubbed");
		try {
			return function.apply(time, unit);
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			sneakyThrow(e);
			throw new AssertionFailedError("unreachable error");
		}
	}

	@FunctionalInterface
	interface ERunnable {
		void run() throws Throwable;
	}

	@FunctionalInterface
	public interface ESupplier<Return> {
		Return get() throws Throwable;
	}

	@FunctionalInterface
	public interface TryLockWithTimeoutFunction {
		boolean apply(long time, TimeUnit timeUnit) throws Throwable;
	}

	@NonNull
	@Override
	public Condition newCondition() {
		fail("newCondition was called - this is not supported");
		throw new UnsupportedOperationException("newCondition not supported in StubbedLock");
	}

	public enum Event {
		LOCK,
		LOCK_INTERRUPTIBLY,
		TRY_LOCK,
		TRY_LOCK_TIMEOUT,
		UNLOCK
	}

	public static class CallEvent {
		final Instant timestamp;
		private final int callIndex;
		@NonNull
		private final Thread thread;
		@NonNull
		private final Event event;
		@Nullable
		private final Long time;
		@Nullable
		private final TimeUnit unit;
		@NonNull
		private final StubbedLock lock;

		CallEvent(@NonNull StubbedLock lock, int callIndex, @NonNull Thread thread, @NonNull Event event) {
			this.lock = lock;
			this.timestamp = Instant.now();
			this.callIndex = callIndex;
			this.thread = thread;
			this.event = event;
			this.time = null;
			this.unit = null;
		}

		CallEvent(@NonNull StubbedLock lock, int callIndex, @NonNull Thread thread, @NonNull Event event, long time, @NonNull TimeUnit timeUnit) {
			this.lock = lock;
			this.timestamp = Instant.now();
			this.callIndex = callIndex;
			this.thread = thread;
			this.event = event;
			this.time = time;
			this.unit = timeUnit;
		}

		@Override
		public String toString() {
			return "CallEvents{" +
							"callIndex=" + callIndex +
							", thread=" + thread +
							", event=" + event +
							", time=" + time +
							", unit=" + unit +
							", timestamp=" + timestamp +
							", lock=" + lock +
							'}';
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			CallEvent that = (CallEvent) o;
			return callIndex == that.callIndex && Objects.equals(thread, that.thread) && event == that.event && Objects.equals(time, that.time) && unit == that.unit && lock == that.lock;
		}

		@Override
		public int hashCode() {
			return Objects.hash(callIndex, thread, event, time, unit, lock);
		}
	}
}
