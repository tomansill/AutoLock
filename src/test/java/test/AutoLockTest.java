package test;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("AutoLock Test")
abstract class AutoLockTest {

	interface LockRunTest {

		@DisplayName("test successful lock (run)")
		@Test
		default void testSuccessfulLockRunnable() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			performLockAndRun(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
			});
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		void performLockAndRun(@NonNull Lock lock, @NonNull Runnable runnable);
	}

	interface LockGetTest {

		@DisplayName("test successful lock (get)")
		@Test
		default void testSuccessfulLockGet() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			Object expected = null; // TODO
			Object actual = performLockAndGet(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				return expected;
			});
			assertEquals(expected, actual);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		<Return> Return performLockAndGet(@NonNull Lock lock, @NonNull Supplier<Return> supplier);

	}

	interface LockInterruptiblyTest {

		@DisplayName("test successful lockInterruptibly")
		@Test
		default void testSuccessfulLockInterruptibly() throws InterruptedException {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLockInterruptibly(() -> {
				lockCount.incrementAndGet();
				assertEquals(currentThread, Thread.currentThread());
			});
			performLockInterruptiblyAndRun(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertEquals(currentThread, Thread.currentThread());
				});
			});
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		void performLockInterruptiblyAndRun(@NonNull Lock lock, @NonNull Runnable runnable) throws InterruptedException;
	}

	interface TryLockInstantTest {

	}

	interface TryLockTimeoutTest {

	}
}
