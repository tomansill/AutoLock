package test;

import com.ansill.autolock.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
public class FluentTest implements
				LockRunTest,
				LockGetTest,
				LockInterruptiblyRunTest,
				LockInterruptiblyGetTest,
				TryLockInstantRunTest,
				TryLockInstantGetTest,
				TryLockTimeoutRunTest,
				TryLockTimeoutGetTest,
				MultiLockRunTest,
				MultiLockGetTest,
				MultiLockInterruptiblyRunTest,
				MultiLockInterruptiblyGetTest,
				MultiTryLockInstantRunTest,
				MultiTryLockInstantGetTest,
				MultiTryLockTimeoutGetTest {

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		AutoLock.with(lock).run(runnable);
	}

	@Override
	public <T extends Throwable> void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException {
		AutoLock.with(lock).interruptibly().run(runnable);
	}

	@Override
	public <Return, T extends Throwable> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T, InterruptedException {
		return AutoLock.with(lock).interruptibly().get(supplier);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws T1, T2 {
		AutoLock.with(lock).tryAcquire().run(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws T1, T2 {
		return AutoLock.with(lock).tryAcquire().get(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T extends Throwable> Return performLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T {
		return AutoLock.with(lock).get(supplier);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunLongAndTimeUnit(@Nullable Lock lock, long time, @Nullable TimeUnit unit, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
		AutoLock.with(lock).tryAcquire(time, unit).run(onLockSuccess, onLockFail);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunDuration(@Nullable Lock lock, @Nullable Duration duration, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
		AutoLock.with(lock).tryAcquire(duration).run(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetLongAndTimeUnit(@Nullable Lock lock, long time, @Nullable TimeUnit unit, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
		return AutoLock.with(lock).tryAcquire(time, unit).get(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetDuration(@Nullable Lock lock, @Nullable Duration duration, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
		return AutoLock.with(lock).tryAcquire(duration).get(onLockSuccess, onLockFail);
	}

	@DisplayName("no-op on incomplete with()")
	@Test
	void testNoOpOnIncompleteWith() {
		try (StubbedLock lock = new StubbedLock()) {
			AutoLock.with(lock);
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("no-op on incomplete interruptibly()")
	@Test
	void testNoOpOnIncompleteInterruptibly() {
		try (StubbedLock lock = new StubbedLock()) {
			AutoLock.with(lock).interruptibly();
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("no-op on incomplete tryAcquire()")
	@Test
	void testNoOpOnIncompleteTryAcquire() {
		try (StubbedLock lock = new StubbedLock()) {
			AutoLock.with(lock).tryAcquire();
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("no-op on incomplete tryAcquire(long,TimeUnit)")
	@Test
	void testNoOpOnIncompleteTryAcquireLongTimeUnit() {
		try (StubbedLock lock = new StubbedLock()) {
			AutoLock.with(lock).tryAcquire(1, TimeUnit.NANOSECONDS);
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("no-op on incomplete tryAcquire(Duration)")
	@Test
	void testNoOpOnIncompleteTryAcquireDuration() {
		try (StubbedLock lock = new StubbedLock()) {
			AutoLock.with(lock).tryAcquire(Duration.ofMinutes(1));
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-run")
	@Test
	void testReusableWithRun() {
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock withLock = AutoLock.with(lock);
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			withLock.run(() -> assertEquals(0, execution.getAndIncrement()));
			lock.setOnLock(() -> {
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			withLock.run(() -> assertEquals(1, execution.getAndIncrement()));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-get")
	@Test
	void testReusableWithGet() {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		List<Supplier<Object>> objs = new ArrayList<>(TestUtility.getRandomObjects(random).values());
		Collections.shuffle(objs, random);
		final Object testObj1 = objs.get(0).get();
		final Object testObj2 = objs.get(1).get();
		assertNotEquals(testObj1, testObj2);
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock withLock = AutoLock.with(lock);
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			assertEquals(testObj1, withLock.get(() -> {
				assertEquals(0, execution.getAndIncrement());
				return testObj1;
			}));
			lock.setOnLock(() -> {
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			assertEquals(testObj2, withLock.get(() -> {
				assertEquals(1, execution.getAndIncrement());
				return testObj2;
			}));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-interruptibly-run")
	@Test
	void testReusableWithInterruptiblyRun() throws InterruptedException {
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.Interruptibly withLock = AutoLock.with(lock).interruptibly();
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnLockInterruptibly(() -> {
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			withLock.run(() -> assertEquals(0, execution.getAndIncrement()));
			lock.setOnLockInterruptibly(() -> {
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			withLock.run(() -> assertEquals(1, execution.getAndIncrement()));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-interruptibly-get")
	@Test
	void testReusableWithInterruptiblyGet() throws InterruptedException {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		List<Supplier<Object>> objs = new ArrayList<>(TestUtility.getRandomObjects(random).values());
		Collections.shuffle(objs, random);
		final Object testObj1 = objs.get(0).get();
		final Object testObj2 = objs.get(1).get();
		assertNotEquals(testObj1, testObj2);
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.Interruptibly withLock = AutoLock.with(lock).interruptibly();
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnLockInterruptibly(() -> {
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			assertEquals(testObj1, withLock.get(() -> {
				assertEquals(0, execution.getAndIncrement());
				return testObj1;
			}));
			lock.setOnLockInterruptibly(() -> {
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			assertEquals(testObj2, withLock.get(() -> {
				assertEquals(1, execution.getAndIncrement());
				return testObj2;
			}));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-tryAcquire-run")
	@Test
	void testReusableWithTryAcquireRun() {
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.TryInstant withLock = AutoLock.with(lock).tryAcquire();
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			withLock.run(() -> assertEquals(0, execution.getAndIncrement()), Assertions::fail);
			lock.setOnTryLockInstant(() -> {
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			withLock.run(() -> assertEquals(1, execution.getAndIncrement()), Assertions::fail);
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-tryAcquire-get")
	@Test
	void testReusableWithTryAcquireGet() {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		List<Supplier<Object>> objs = new ArrayList<>(TestUtility.getRandomObjects(random).values());
		Collections.shuffle(objs, random);
		final Object testObj1 = objs.get(0).get();
		final Object testObj2 = objs.get(1).get();
		assertNotEquals(testObj1, testObj2);
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.TryInstant withLock = AutoLock.with(lock).tryAcquire();
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			assertEquals(testObj1, withLock.get(() -> {
				assertEquals(0, execution.getAndIncrement());
				return testObj1;
			}, Assertions::fail));
			lock.setOnTryLockInstant(() -> {
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			assertEquals(testObj2, withLock.get(() -> {
				assertEquals(1, execution.getAndIncrement());
				return testObj2;
			}, Assertions::fail));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-tryAcquireDuration-run")
	@Test
	void testReusableWithTryAcquireDurationRun() throws InterruptedException {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		Duration testDuration = TestUtility.generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.TryWithTimeout withLock = AutoLock.with(lock).tryAcquire(testDuration);
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			withLock.run(() -> assertEquals(0, execution.getAndIncrement()), Assertions::fail);
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			withLock.run(() -> assertEquals(1, execution.getAndIncrement()), Assertions::fail);
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-tryAcquireDuration-get")
	@Test
	void testReusableWithTryAcquireDurationGet() throws InterruptedException {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		Duration testDuration = TestUtility.generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
		List<Supplier<Object>> objs = new ArrayList<>(TestUtility.getRandomObjects(random).values());
		Collections.shuffle(objs, random);
		final Object testObj1 = objs.get(0).get();
		final Object testObj2 = objs.get(1).get();
		assertNotEquals(testObj1, testObj2);
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.TryWithTimeout withLock = AutoLock.with(lock).tryAcquire(testDuration);
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			assertEquals(testObj1, withLock.get(() -> {
				assertEquals(0, execution.getAndIncrement());
				return testObj1;
			}, Assertions::fail));
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			assertEquals(testObj2, withLock.get(() -> {
				assertEquals(1, execution.getAndIncrement());
				return testObj2;
			}, Assertions::fail));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-tryAcquireLongTimeUnit-run")
	@Test
	void testReusableWithTryAcquireLongTimeUnitRun() throws InterruptedException {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		Duration testDuration = TestUtility.generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.TryWithTimeout withLock = AutoLock.with(lock).tryAcquire(testDuration.toNanos(), TimeUnit.NANOSECONDS);
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			withLock.run(() -> assertEquals(0, execution.getAndIncrement()), Assertions::fail);
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			withLock.run(() -> assertEquals(1, execution.getAndIncrement()), Assertions::fail);
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable with-tryAcquireLongTimeUnit-get")
	@Test
	void testReusableWithTryAcquireLongTimeUnitGet() throws InterruptedException {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		Duration testDuration = TestUtility.generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
		List<Supplier<Object>> objs = new ArrayList<>(TestUtility.getRandomObjects(random).values());
		Collections.shuffle(objs, random);
		final Object testObj1 = objs.get(0).get();
		final Object testObj2 = objs.get(1).get();
		assertNotEquals(testObj1, testObj2);
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock.TryWithTimeout withLock = AutoLock.with(lock).tryAcquire(testDuration.toNanos(), TimeUnit.NANOSECONDS);
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			assertEquals(testObj1, withLock.get(() -> {
				assertEquals(0, execution.getAndIncrement());
				return testObj1;
			}, Assertions::fail));
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			assertEquals(testObj2, withLock.get(() -> {
				assertEquals(1, execution.getAndIncrement());
				return testObj2;
			}, Assertions::fail));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@DisplayName("reusable mixed")
	@Test
	void testReusableMixed() throws InterruptedException {
		Random random = new Random(TestUtility.getSeed(0).hashCode());
		Duration testDuration1 = TestUtility.generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
		List<Supplier<Object>> objs = new ArrayList<>(TestUtility.getRandomObjects(random).values());
		Collections.shuffle(objs, random);
		final Object testObj1 = objs.get(0).get();
		final Object testObj2 = objs.get(1).get();
		assertNotEquals(testObj1, testObj2);
		try (StubbedLock lock = new StubbedLock()) {
			final AutoLock.WithLock withLock = AutoLock.with(lock);
			AtomicInteger locked = new AtomicInteger(0);
			AtomicInteger execution = new AtomicInteger(0);
			AtomicInteger unlocked = new AtomicInteger(0);
			final Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration1.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
				assertEquals(0, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(0, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
				return true;
			});
			assertEquals(testObj1, withLock.tryAcquire(testDuration1.toNanos(), TimeUnit.NANOSECONDS).get(() -> {
				assertEquals(0, execution.getAndIncrement());
				return testObj1;
			}, Assertions::fail));
			lock.setOnLockInterruptibly(() -> {
				assertEquals(1, locked.getAndIncrement());
				assertSame(currentThread, Thread.currentThread());
				lock.setOnUnlock(() -> {
					assertEquals(1, unlocked.getAndIncrement());
					assertSame(currentThread, Thread.currentThread());
				});
			});
			assertEquals(testObj2, withLock.interruptibly().get(() -> {
				assertEquals(1, execution.getAndIncrement());
				return testObj2;
			}));
			assertEquals(2, locked.get());
			assertEquals(2, unlocked.get());
			assertEquals(2, execution.get());
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration1.toNanos(), TimeUnit.NANOSECONDS),
							new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock, 2, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock, 3, currentThread, StubbedLock.Event.UNLOCK)
			), lock.getActualEvents());
		}
	}

	@Override
	public <Return, T extends Throwable> Return performLockAndGet(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T> supplier) throws T {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		return AutoLock.with(lock1, lock2, rest).get(supplier);
	}

	@Override
	public <Return, T extends Throwable> Return performLockInterruptiblyAndGet(@NonNull Lock[] locks, @Nullable ThrowableSupplier<Return, T> supplier) throws T, InterruptedException {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		return AutoLock.with(lock1, lock2, rest).interruptibly().get(supplier);
	}

	@Override
	public <T extends Throwable> void performLockAndRun(@NonNull Lock[] locks, @Nullable ThrowableRunnable<T> runnable) throws T {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.with(lock1, lock2, rest).run(runnable);
	}

	@Override
	public <T extends Throwable> void performLockInterruptiblyAndRun(@NonNull Lock[] locks, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.with(lock1, lock2, rest).interruptibly().run(runnable);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetWithContext(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		return AutoLock.with(lock1, lock2, rest).tryAcquire().get(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetWithoutContext(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		return AutoLock.with(lock1, lock2, rest).tryAcquire().get(onLockSuccess, onLockFail);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunWithContext(@Nullable Lock[] locks, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableConsumer<AutoLock.MultipleLocks.TryLockFailContext, T2> onLockFail) throws T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.with(lock1, lock2, rest).tryAcquire().run(onLockSuccess, onLockFail);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunWithoutContext(@Nullable Lock[] locks, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.with(lock1, lock2, rest).tryAcquire().run(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetLongAndTimeUnitWithoutContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, long time, @Nullable TimeUnit unit, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.MultipleLocks.TryWithTimeout ctx = AutoLock.with(lock1, lock2, rest).tryAcquire(time, unit);
		if (timeSourceStubber != null) Bypass.injectTimeSupplier(ctx, timeSourceStubber.get());
		return ctx.get(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetDurationWithoutContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable Duration duration, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.MultipleLocks.TryWithTimeout ctx = AutoLock.with(lock1, lock2, rest).tryAcquire(duration);
		if (timeSourceStubber != null) Bypass.injectTimeSupplier(ctx, timeSourceStubber.get());
		return ctx.get(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetLongAndTimeUnitWithContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, long time, @Nullable TimeUnit unit, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws InterruptedException, T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.MultipleLocks.TryWithTimeout ctx = AutoLock.with(lock1, lock2, rest).tryAcquire(time, unit);
		if (timeSourceStubber != null) Bypass.injectTimeSupplier(ctx, timeSourceStubber.get());
		return ctx.get(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetDurationWithContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable Duration duration, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws InterruptedException, T1, T2 {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		AutoLock.MultipleLocks.TryWithTimeout ctx = AutoLock.with(lock1, lock2, rest).tryAcquire(duration);
		if (timeSourceStubber != null) Bypass.injectTimeSupplier(ctx, timeSourceStubber.get());
		return ctx.get(onLockSuccess, onLockFail);
	}
}
