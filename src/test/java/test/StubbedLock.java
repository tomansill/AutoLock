package test;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.fail;

public class StubbedLock implements Lock, AutoCloseable {


	@Override
	public void close() {
		confirmNoMoreExpectedCalls();
	}

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
		if (onTryLockRunnable.get() != null) {
			fail("tryLock() waiting to be called");
		}
		if (onTryLockTimeoutRunnable.get() != null) {
			fail("tryLock(long,TimeUnit) waiting to be called");
		}
	}

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
	@NonNull
	private final AtomicReference<Supplier<Boolean>> onTryLockRunnable = new AtomicReference<>();
	@NonNull
	private final AtomicReference<TryLockWithTimeoutFunction> onTryLockTimeoutRunnable = new AtomicReference<>();

	private final List<TimestampedEvent> actualEvents = new CopyOnWriteArrayList<>();

	@NonNull List<TimestampedEvent> getActualEvents() {
		return Collections.unmodifiableList(actualEvents);
	}

	@Override
	public void lock() {
		actualEvents.add(new TimestampedEvent(callIndex.getAndIncrement(), Thread.currentThread(), Event.LOCK));
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

	public void setOnTryLockInstant(@NonNull Supplier<Boolean> onTryLock) {
		this.onTryLockRunnable.set(onTryLock);
	}

	@Override
	public void unlock() {
		actualEvents.add(new TimestampedEvent(callIndex.getAndIncrement(), Thread.currentThread(), Event.UNLOCK));
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
		actualEvents.add(new TimestampedEvent(callIndex.getAndIncrement(), Thread.currentThread(), Event.LOCK_INTERRUPTIBLY));
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
		actualEvents.add(new TimestampedEvent(callIndex.getAndIncrement(), Thread.currentThread(), Event.TRY_LOCK));
		Supplier<Boolean> function = onTryLockRunnable.getAndSet(null);
		if (function == null) fail("unstubbed");
		return function.get();
	}

	@Override
	public boolean tryLock(long time, @NonNull TimeUnit unit) throws InterruptedException {
		actualEvents.add(new TimestampedEvent(callIndex.getAndIncrement(), Thread.currentThread(), Event.TRY_LOCK_TIMEOUT, Duration.ofNanos(unit.toNanos(time))));
		TryLockWithTimeoutFunction function = onTryLockTimeoutRunnable.getAndSet(null);
		if (function == null) fail("unstubbed");
		return function.apply(time, unit);
	}

	@FunctionalInterface
	interface ERunnable {
		void run() throws Throwable;
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

	@FunctionalInterface
	interface TryLockWithTimeoutFunction {
		boolean apply(long time, TimeUnit timeUnit) throws InterruptedException;
	}

	public static class TimestampedEvent {
		private final int callIndex;
		@NonNull
		private final Thread thread;
		@NonNull
		private final Event event;
		@Nullable
		private final Duration timeout;

		TimestampedEvent(int callIndex, @NonNull Thread thread, @NonNull Event event) {
			this.callIndex = callIndex;
			this.thread = thread;
			this.event = event;
			this.timeout = null;
		}

		private TimestampedEvent(int callIndex, @NonNull Thread thread, @NonNull Event event, @NonNull Duration timeout) {
			this.callIndex = callIndex;
			this.thread = thread;
			this.event = event;
			this.timeout = timeout;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			TimestampedEvent that = (TimestampedEvent) o;
			return callIndex == that.callIndex && Objects.equals(thread, that.thread) && event == that.event && Objects.equals(timeout, that.timeout);
		}

		@Override
		public int hashCode() {
			return Objects.hash(callIndex, thread, event, timeout);
		}

		@Override
		public String toString() {
			return "TimestampedEvent{" +
							"callIndex=" + callIndex +
							", thread=" + thread +
							", event=" + event +
							", timeout=" + timeout +
							'}';
		}
	}
}
