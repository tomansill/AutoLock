package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.ThrowableRunnable;
import com.ansill.lock.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lambda methods")
abstract class AutoLockLambdasTest implements AutoLockTest {

	abstract <T extends Throwable> void lockAndRun(Lock lock, ThrowableRunnable<T> runnable) throws T;

	abstract <R, T extends Throwable> R lockAndGet(Lock lock, ThrowableSupplier<R, T> throwableSupplier) throws T;

	abstract <T extends Throwable> void lockInterruptiblyAndRun(Lock lock, ThrowableRunnable<T> runnable)
					throws T, InterruptedException;

	abstract <R, T extends Throwable> R lockInterruptiblyAndGet(Lock lock, ThrowableSupplier<R, T> throwableSupplier)
					throws T, InterruptedException;

	abstract <T extends Throwable> void tryLockAndRun(Lock lock, ThrowableRunnable<T> onSuccess, ThrowableRunnable<T> onFail) throws T;

	abstract <R, T extends Throwable> R tryLockAndGet(Lock lock, ThrowableSupplier<R, T> onSuccess, ThrowableSupplier<R, T> onFail)
					throws T;

	abstract <T extends Throwable> void tryLockAndRun(Lock lock, long time, TimeUnit unit, ThrowableRunnable<T> onSuccess, ThrowableRunnable<T> onFail)
					throws T, InterruptedException;

	abstract <R, T extends Throwable> R tryLockAndGet(
					Lock lock,
					long time,
					TimeUnit unit,
					ThrowableSupplier<R, T> onSuccess,
					ThrowableSupplier<R, T> onFail)
					throws T, InterruptedException;

	abstract <T extends Throwable> void tryLockAndRun(Lock lock, Duration timeout, ThrowableRunnable<T> onSuccess, ThrowableRunnable<T> onFail)
					throws T, InterruptedException;

	abstract <R, T extends Throwable> R tryLockAndGet(Lock lock, Duration timeout, ThrowableSupplier<R, T> onSuccess, ThrowableSupplier<R, T> onFail)
					throws T, InterruptedException;

	@DisplayName("Attempt to successfully run lockAndRun(Lock,Runnable) method")
	@Test
	void testLockAndRun() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Lock it
		assertDoesNotThrow(() -> lockAndRun(rl, () -> afterLock.set(duringThread.get())));

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to pass in null Lock in lockAndRun(Lock,Runnable) method")
	@Test
	void testLockAndRunNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockAndRun(null, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null Runnable in lockAndRun(Lock,Runnable) method")
	@Test
	void testLockAndRunNullRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockAndRun(new ReentrantLock(), null));
		assertEquals("runnable must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to successfully run lockAndGet(Lock,Supplier) method")
	@Test
	void testLockAndGet() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Desired item
		int item = RNG.nextInt();

		// Lock it
		int value = assertDoesNotThrow(() -> lockAndGet(rl, () -> {
			afterLock.set(duringThread.get());
			return item;
		}));

		// Do after-lock test
		afterLock.get().run();

		// Check value
		assertEquals(item, value);
	}

	@DisplayName("Attempt to pass in null Lock lockAndGet(Lock,Supplier) method")
	@Test
	void testLockAndGetNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockAndGet(null, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null Runnable in lockAndGet(Lock,Supplier) method")
	@Test
	void testLockAndGetNullRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockAndGet(new ReentrantLock(), null));
		assertEquals("supplier must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to successfully run lockInterruptiblyAndRun(Lock,Runnable) method")
	@Test
	void testLockInterruptiblyAndRun() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Lock it
		assertDoesNotThrow(() -> lockInterruptiblyAndRun(rl, () -> afterLock.set(duringThread.get())));

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to pass in null Lock in lockInterruptiblyAndRun(Lock,Runnable) method")
	@Test
	void testLockInterruptiblyAndRunNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockInterruptiblyAndRun(null, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null Runnable in lockInterruptiblyAndRun(Lock,Runnable) method")
	@Test
	void testLockInterruptiblyAndRunNullRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockInterruptiblyAndRun(new ReentrantLock(), null));
		assertEquals("runnable must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to successfully run lockInterruptiblyAndGet(Lock,Supplier) method")
	@Test
	void testLockInterruptiblyAndGet() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Desired item
		int item = RNG.nextInt();

		// Lock it
		int value = assertDoesNotThrow(() -> lockInterruptiblyAndGet(rl, () -> {
			afterLock.set(duringThread.get());
			return item;
		}));

		// Do after-lock test
		afterLock.get().run();

		// Check value
		assertEquals(item, value);
	}

	@DisplayName("Attempt to pass in null Lock lockInterruptiblyAndGet(Lock,Supplier) method")
	@Test
	void testLockInterruptiblyAndGetNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockInterruptiblyAndGet(null, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null Runnable in lockInterruptiblyAndGet(Lock,Supplier) method")
	@Test
	void testLockInterruptiblyAndGetNullRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> lockInterruptiblyAndGet(new ReentrantLock(), null));
		assertEquals("supplier must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to run lockInterruptiblyAndRun(Lock,Runnable) method and call interrupt")
	@Test
	void testLockInterruptiblyAndRunInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			try {
				lockInterruptiblyAndRun(rl, () -> fail("Lock obtained"));
			} catch (InterruptedException e) {
				success.set(true);
			} catch (Exception e) {
				fail(e);
			}

		});

		// Start thread
		thread.start();

		// Wait a tiny bit
		Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

		// Interrupt
		thread.interrupt();

		// Wait for thread to join
		thread.join();

		// Ensure that lock has been interrupted
		assertTrue(success.get());

	}

	@DisplayName("Attempt to run lockInterruptiblyAndGet(Lock,Supplier) method and call interrupt")
	@Test
	void testLockInterruptiblyAndGetInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			try {
				lockInterruptiblyAndGet(rl, () -> {
					fail("Lock obtained");
					return 10;
				});
			} catch (InterruptedException e) {
				success.set(true);
			} catch (Exception e) {
				fail(e);
			}

		});

		// Start thread
		thread.start();

		// Wait a tiny bit
		Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

		// Interrupt
		thread.interrupt();

		// Wait for thread to join
		thread.join();

		// Ensure that lock has been interrupted
		assertTrue(success.get());

	}

	@DisplayName("Attempt to run tryLockAndRun(Lock,long,TimeUnit,Runnable) method and call interrupt")
	@Test
	void testTryLockAndRunTimeUnitInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			// Ensure doTryLock doesn't take too long
			assertTimeout(Duration.ofSeconds(1), () -> {

				// Do it
				try {
					tryLockAndRun(rl, 1, TimeUnit.MINUTES, () -> fail("Lock obtained"), () -> fail("Timed out"));
				} catch (InterruptedException e) {
					success.set(true);
				}
			});
		});

		// Start thread
		thread.start();

		// Wait a tiny bit
		Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

		// Interrupt
		thread.interrupt();

		// Wait for thread to join
		thread.join();

		// Ensure that lock has been interrupted
		assertTrue(success.get());

	}

	@DisplayName("Attempt to run tryLockAndGet(Lock,long,TimeUnit,Supplier) method and call interrupt")
	@Test
	void testTryLockAndGetTimeUnitInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			// Ensure doTryLock doesn't take too long
			assertTimeout(Duration.ofSeconds(1), () -> {

				// Do it
				try {
					tryLockAndGet(rl, 1, TimeUnit.MINUTES, () -> {
						fail("Lock obtained");
						return 1;
					}, () -> fail("Timed out"));
				} catch (InterruptedException e) {
					success.set(true);
				}
			});
		});

		// Start thread
		thread.start();

		// Wait a tiny bit
		Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

		// Interrupt
		thread.interrupt();

		// Wait for thread to join
		thread.join();

		// Ensure that lock has been interrupted
		assertTrue(success.get());

	}

	@DisplayName("Attempt to run tryLockAndRun(Lock,Duration,Runnable) method and call interrupt")
	@Test
	void testTryLockAndRunDurationInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			// Ensure doTryLock doesn't take too long
			assertTimeout(Duration.ofSeconds(1), () -> {

				// Do it
				try {
					tryLockAndRun(rl, Duration.ofMinutes(1), () -> fail("Lock obtained"), () -> fail("Timed out"));
				} catch (InterruptedException e) {
					success.set(true);
				}
			});
		});

		// Start thread
		thread.start();

		// Wait a tiny bit
		Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

		// Interrupt
		thread.interrupt();

		// Wait for thread to join
		thread.join();

		// Ensure that lock has been interrupted
		assertTrue(success.get());

	}

	@DisplayName("Attempt to run tryLockAndGet(Lock,Duration,Supplier) method and call interrupt")
	@Test
	void testTryLockAndGetDurationInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			// Ensure doTryLock doesn't take too long
			assertTimeout(Duration.ofSeconds(1), () -> {

				// Do it
				try {
					tryLockAndGet(rl, Duration.ofMinutes(1), () -> {
						fail("Lock obtained");
						return 1;
					}, () -> fail("Timed out"));
				} catch (InterruptedException e) {
					success.set(true);
				}
			});
		});

		// Start thread
		thread.start();

		// Wait a tiny bit
		Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

		// Interrupt
		thread.interrupt();

		// Wait for thread to join
		thread.join();

		// Ensure that lock has been interrupted
		assertTrue(success.get());

	}

	@DisplayName("Attempt to successfully run tryLockAndRun(Lock,Runnable,Runnable) method")
	@Test
	void testTryLockAndRun() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Lock it
		assertDoesNotThrow(() -> tryLockAndRun(rl, () -> {

			// Do during-thread test and get post lock runnable
			afterLock.set(duringThread.get());

		}, () -> fail("Timed out")));

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to pass in null Lock in tryLockAndRun(Lock,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(null, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 1st Runnable in tryLockAndRun(Lock,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunNullSuccessRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), null, Assertions::fail));
		assertEquals("onLockSuccess must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 2nd Runnable in tryLockAndRun(Lock,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunNullFailRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), Assertions::fail, null));
		assertEquals("onLockFail must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to successfully run tryLockAndGet(Lock,Supplier,Supplier) method")
	@Test
	void testTryLockAndGet() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Expected value
		int expected = RNG.nextInt();

		// Lock it and ensure that tryLock is instantaneous
		int value = assertDoesNotThrow(() -> tryLockAndGet(rl, () -> {

			// Do during-thread test and get post lock runnable
			afterLock.set(duringThread.get());

			// Return expected value
			return expected;
		}, () -> fail("Timed out")));

		// Check value
		assertEquals(expected, value);

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to pass in null Lock in tryLockAndGet(Lock,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(null, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 1st Supplier in tryLockAndGet(Lock,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetNullSuccessRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), null, Assertions::fail));
		assertEquals("onLockSuccess must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 2nd Supplier in tryLockAndGet(Lock,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetNullFailRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), Assertions::fail, null));
		assertEquals("onLockFail must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to successfully run tryLockAndRun(Lock,long,TimeUnit,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunTimeUnit() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Lock it and ensure that tryLock is instantaneous
		assertTimeout(Duration.ofSeconds(1), () -> tryLockAndRun(rl, 1, TimeUnit.MINUTES, () -> {

			// Do during-thread test and get post lock runnable
			afterLock.set(duringThread.get());

		}, () -> fail("Timed out")));

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to successfully run tryLockAndGet(Lock,long,TimeUnit,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetTimeUnit() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Expected value
		int expected = RNG.nextInt();

		// Lock it and ensure that tryLock is instantaneous
		assertTimeout(Duration.ofSeconds(1), () -> {
			int value = tryLockAndGet(rl, 1, TimeUnit.MINUTES, () -> {

				// Do during-thread test and get post lock runnable
				afterLock.set(duringThread.get());

				// Return expected value
				return expected;
			}, () -> fail("Timed out"));

			// Check value
			assertEquals(expected, value);
		});

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to successfully run tryLockAndRun(Lock,Duration,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunDuration() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Lock it and ensure that tryLock is instantaneous
		assertTimeout(Duration.ofSeconds(1), () -> tryLockAndRun(rl, Duration.ofMinutes(1), () -> {

			// Do during-thread test and get post lock runnable
			afterLock.set(duringThread.get());
		}, () -> fail("Timed out")));

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to pass in null Lock in tryLockAndRun(Lock,Duration,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunDurationNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(null, Duration.ofMinutes(1), Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null Duration in tryLockAndRun(Lock,Duration,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunDurationNullDuration() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), null, Assertions::fail, Assertions::fail));
		assertEquals("timeout must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in negative Duration in tryLockAndRun(Lock,Duration,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunDurationNegativeDuration() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> tryLockAndRun(new ReentrantLock(), Duration.ofMinutes(-1), Assertions::fail, Assertions::fail));
		assertEquals("timeout must be non-negative", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 1st Runnable in tryLockAndRun(Lock,Duration,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunDurationNullSuccessRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), Duration.ofMinutes(1), null, Assertions::fail));
		assertEquals("onLockSuccess must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 2nd Runnable in tryLockAndRun(Lock,Duration,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunDurationNullFailRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), Duration.ofMinutes(1), Assertions::fail, null));
		assertEquals("onLockFail must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to successfully run tryLockAndGet(Lock,Duration,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetDuration() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Expected value
		int expected = RNG.nextInt();

		// Set up after-lock runnable reference
		AtomicReference<Runnable> afterLock = new AtomicReference<>();

		// Lock it and ensure that tryLock is instantaneous
		assertTimeout(Duration.ofSeconds(1), () -> {
			int value = tryLockAndGet(rl, Duration.ofMinutes(1), () -> {

				// Do during-thread test and get post lock runnable
				afterLock.set(duringThread.get());

				// Return expected value
				return expected;
			}, () -> fail("Timed out"));

			// Check value
			assertEquals(expected, value);
		});

		// Do after-lock test
		afterLock.get().run();
	}

	@DisplayName("Attempt to pass in null Lock in tryLockAndGet(Lock,Duration,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetDurationNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(null, Duration.ofMinutes(1), Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null Duration in tryLockAndGet(Lock,Duration,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetDurationNullDuration() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), null, Assertions::fail, Assertions::fail));
		assertEquals("timeout must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in negative Duration in tryLockAndGet(Lock,Duration,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetDurationNegativeDuration() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> tryLockAndGet(new ReentrantLock(), Duration.ofMinutes(-1), Assertions::fail, Assertions::fail));
		assertEquals("timeout must be non-negative", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 1st Runnable in tryLockAndGet(Lock,Duration,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetDurationNullSuccessRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), Duration.ofMinutes(1), null, Assertions::fail));
		assertEquals("onLockSuccess must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 2nd Runnable in tryLockAndGet(Lock,Duration,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetDurationNullFailRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), Duration.ofMinutes(1), Assertions::fail, null));
		assertEquals("onLockFail must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to run tryLockAndRun(Lock,Runnable,Runnable) method and force it to time out")
	@Test
	void testTryLockAndRunTimeout() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Go-ahead CDL
		CountDownLatch goAhead = new CountDownLatch(1);

		// Unlock CDL
		CountDownLatch cdl = new CountDownLatch(1);

		// Create thread
		Thread thread = new Thread(() -> {
			try {
				rl.lock();
				goAhead.countDown();
				cdl.await();
			} catch (InterruptedException e) {
				fail();
			} finally {
				rl.unlock();
			}
		});

		try {

			// Start thread to lock
			thread.start();

			// Wait for thread to lock
			goAhead.await();

			// Set up variable to toggle on timeout
			AtomicBoolean timedOut = new AtomicBoolean(false);

			// Attempt to tryLock
			assertTimeoutPreemptively(MAX_WAIT_TIME, () -> tryLockAndRun(rl, () -> fail("Lock obtained"), () -> timedOut.set(true)));

			// Check
			assertTrue(timedOut.get(), "onFail Runnable never ran");

		} finally {
			cdl.countDown();
		}

		// Wait for thread to finish
		thread.join();

	}

	@DisplayName("Attempt to pass in null Lock in tryLockAndRun(Lock,long,TimeUnit,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunLongTimeUnitNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(null, 1, TimeUnit.MINUTES, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null TimeUnit in tryLockAndRun(Lock,long,TimeUnit,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunLongTimeUnitNullDuration() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), 1, null, Assertions::fail, Assertions::fail));
		assertEquals("unit must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in negative time in tryLockAndRun(Lock,long,TimeUnit,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunLongTimeUnitNegativeDuration() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> tryLockAndRun(new ReentrantLock(), -1, TimeUnit.MINUTES, Assertions::fail, Assertions::fail));
		assertEquals("time must be non-negative", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 1st Runnable in tryLockAndRun(Lock,long,TimeUnit,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunLongTimeUnitNullSuccessRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), 1, TimeUnit.MINUTES, null, Assertions::fail));
		assertEquals("onLockSuccess must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 2nd Runnable in tryLockAndRun(Lock,long,TimeUnit,Runnable,Runnable) method")
	@Test
	void testTryLockAndRunLongTimeUnitNullFailRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndRun(new ReentrantLock(), 1, TimeUnit.MINUTES, Assertions::fail, null));
		assertEquals("onLockFail must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to run tryLockAndGet(Lock,Supplier,Supplier) method and force it to time out")
	@Test
	void testTryLockAndGetTimeout() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Go-ahead CDL
		CountDownLatch goAhead = new CountDownLatch(1);

		// Unlock CDL
		CountDownLatch cdl = new CountDownLatch(1);

		// Create thread
		Thread thread = new Thread(() -> {
			try {
				rl.lock();
				goAhead.countDown();
				cdl.await();
			} catch (InterruptedException e) {
				fail();
			} finally {
				rl.unlock();
			}
		});

		try {

			// Start thread to lock
			thread.start();

			// Wait for thread to lock
			goAhead.await();

			// Set up variable to toggle on timeout
			AtomicBoolean timedOut = new AtomicBoolean(false);

			// Attempt to tryLock
			assertTimeoutPreemptively(MAX_WAIT_TIME, () -> tryLockAndGet(rl, () -> {
				fail("Lock obtained");
				return 1;
			}, () -> {
				timedOut.set(true);
				return 1;
			}));

			// Check
			assertTrue(timedOut.get(), "onFail Supplier never ran");

		} finally {
			cdl.countDown();
		}

		// Wait for thread to finish
		thread.join();

	}

	@DisplayName("Attempt to pass in null Lock in tryLockAndGet(Lock,long,TimeUnit,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetLongTimeUnitNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(null, 1, TimeUnit.MINUTES, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null TimeUnit in tryLockAndGet(Lock,long,TimeUnit,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetLongTimeUnitNullDuration() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), 1, null, Assertions::fail, Assertions::fail));
		assertEquals("unit must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in negative time in tryLockAndGet(Lock,long,TimeUnit,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetLongTimeUnitNegativeDuration() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> tryLockAndGet(new ReentrantLock(), -1, TimeUnit.MINUTES, Assertions::fail, Assertions::fail));
		assertEquals("time must be non-negative", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 1st Runnable in tryLockAndGet(Lock,long,TimeUnit,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetLongTimeUnitNullSuccessRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), 1, TimeUnit.MINUTES, null, Assertions::fail));
		assertEquals("onLockSuccess must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to pass in null 2nd Runnable in tryLockAndGet(Lock,long,TimeUnit,Supplier,Supplier) method")
	@Test
	void testTryLockAndGetLongTimeUnitNullFailRunnable() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> tryLockAndGet(new ReentrantLock(), 1, TimeUnit.MINUTES, Assertions::fail, null));
		assertEquals("onLockFail must not be null", exception.getMessage());
	}

	@DisplayName("Attempt to run tryLockAndRun(Lock,long,TimeUnit,Runnable) method and force it to time out")
	@Test
	void testTryLockAndRunTimeUnitTimeout() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Go-ahead CDL
		CountDownLatch goAhead = new CountDownLatch(1);

		// Unlock CDL
		CountDownLatch cdl = new CountDownLatch(1);

		// Create thread
		Thread thread = new Thread(() -> {
			try {
				rl.lock();
				goAhead.countDown();
				cdl.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			} finally {
				rl.unlock();
			}
		});

		try {

			// Start thread to lock
			thread.start();

			// Wait for thread to lock
			goAhead.await();

			// Set up variable to toggle on timeout
			AtomicBoolean timedOut = new AtomicBoolean(false);

			// Attempt to tryLock
			assertTimeoutPreemptively(MAX_WAIT_TIME, () -> tryLockAndRun(rl,
							AutoLockTest.EXECUTION_TIME.toMillis(),
							TimeUnit.MILLISECONDS,
							() -> fail("Lock obtained"), () -> timedOut.set(true)));

			// Check
			assertTrue(timedOut.get(), "onFail Runnable never ran");

		} finally {
			cdl.countDown();
		}

		// Wait for thread to finish
		thread.join();

	}

	@DisplayName("Attempt to run tryLockAndGet(Lock,long,TimeUnit,Supplier,Supplier) method and force it to time out")
	@Test
	void testTryLockAndGetTimeUnitTimeout() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Go-ahead CDL
		CountDownLatch goAhead = new CountDownLatch(1);

		// Unlock CDL
		CountDownLatch cdl = new CountDownLatch(1);

		// Create thread
		Thread thread = new Thread(() -> {
			try {
				rl.lock();
				goAhead.countDown();
				cdl.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			} finally {
				rl.unlock();
			}
		});

		try {

			// Start thread to lock
			thread.start();

			// Wait for thread to lock
			goAhead.await();

			// Set up variable to toggle on timeout
			AtomicBoolean timedOut = new AtomicBoolean(false);

			// Attempt to tryLock
			assertTimeoutPreemptively(MAX_WAIT_TIME, () -> tryLockAndGet(rl, AutoLockTest.EXECUTION_TIME.toMillis(), TimeUnit.MILLISECONDS, () -> {
				fail("Lock obtained");
				return 1;
			}, () -> {
				timedOut.set(true);
				return 1;
			}));

			// Check
			assertTrue(timedOut.get(), "onFail Supplier never ran");

		} finally {
			cdl.countDown();
		}

		// Wait for thread to finish
		thread.join();

	}

	@DisplayName("Attempt to run tryLockAndRun(Lock,Duration,Runnable,Runnable) method and force it to time out")
	@Test
	void testTryLockAndRunDurationTimeout() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Go-ahead CDL
		CountDownLatch goAhead = new CountDownLatch(1);

		// Unlock CDL
		CountDownLatch cdl = new CountDownLatch(1);

		// Create thread
		Thread thread = new Thread(() -> {
			try {
				rl.lock();
				goAhead.countDown();
				cdl.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			} finally {
				rl.unlock();
			}
		});

		try {

			// Start thread to lock
			thread.start();

			// Wait for thread to lock
			goAhead.await();

			// Set up variable to toggle on timeout
			AtomicBoolean timedOut = new AtomicBoolean(false);

			// Attempt to tryLock
			assertTimeoutPreemptively(MAX_WAIT_TIME, () -> tryLockAndRun(rl,
							AutoLockTest.EXECUTION_TIME,
							() -> fail("Lock obtained"), () -> timedOut.set(true)));

			// Check
			assertTrue(timedOut.get(), "onFail Runnable never ran");

		} finally {
			cdl.countDown();
		}

		// Wait for thread to finish
		thread.join();

	}

	@DisplayName("Attempt to run tryLockAndGet(Lock,Duration,Supplier) method and force it to time out")
	@Test
	void testTryLockAndGetDurationTimeout() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Go-ahead CDL
		CountDownLatch goAhead = new CountDownLatch(1);

		// Unlock CDL
		CountDownLatch cdl = new CountDownLatch(1);

		// Create thread
		Thread thread = new Thread(() -> {
			try {
				rl.lock();
				goAhead.countDown();
				cdl.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				fail();
			} finally {
				rl.unlock();
			}
		});

		try {

			// Start thread to lock
			thread.start();

			// Wait for thread to lock
			goAhead.await();

			// Set up variable to toggle on timeout
			AtomicBoolean timedOut = new AtomicBoolean(false);

			// Attempt to tryLock
			assertTimeoutPreemptively(MAX_WAIT_TIME, () -> tryLockAndGet(rl, AutoLockTest.EXECUTION_TIME, () -> {
				fail("Lock obtained");
				return 1;
			}, () -> {
				timedOut.set(true);
				return 1;
			}));

			// Check
			assertTrue(timedOut.get(), "onFail Supplier never ran");

		} finally {
			cdl.countDown();
		}

		// Wait for thread to finish
		thread.join();

	}

	@DisplayName("Lambda methods (with Exception)")
	static class LambdaWithExceptionTest extends AutoLockLambdasTest {

		@Override
		<T extends Throwable> void lockAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T> runnable) throws T {
			AutoLock.lockAndRun(lock, runnable);
		}

		@Override
		<R, T extends Throwable> R lockAndGet(@NonNull Lock lock, @NonNull ThrowableSupplier<R, T> throwableSupplier)
						throws T {
			return AutoLock.lockAndGet(lock, throwableSupplier);
		}

		@Override
		<T extends Throwable> void lockInterruptiblyAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T> runnable)
						throws T, InterruptedException {
			AutoLock.lockInterruptiblyAndRun(lock, runnable);
		}

		@Override
		<R, T extends Throwable> R lockInterruptiblyAndGet(
						@NonNull Lock lock,
						@NonNull ThrowableSupplier<R, T> throwableSupplier
		)
						throws T, InterruptedException {
			return AutoLock.lockInterruptiblyAndGet(lock, throwableSupplier);
		}

		@Override
		<T extends Throwable> void tryLockAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T> onSuccess, ThrowableRunnable<T> onFail) throws T {
			AutoLock.tryLockAndRun(lock, onSuccess, onFail);
		}

		@Override
		<R, T extends Throwable> R tryLockAndGet(@NonNull Lock lock, @NonNull ThrowableSupplier<R, T> onSuccess, ThrowableSupplier<R, T> onFail)
						throws T {
			return AutoLock.tryLockAndGet(lock, onSuccess, onFail);
		}

		@Override
		<T extends Throwable> void tryLockAndRun(
						@NonNull Lock lock,
						long time,
						@NonNull TimeUnit unit,
						@NonNull ThrowableRunnable<T> onSuccess,
						ThrowableRunnable<T> onFail)
						throws T, InterruptedException {
			AutoLock.tryLockAndRun(lock, time, unit, onSuccess, onFail);
		}

		@Override
		<R, T extends Throwable> R tryLockAndGet(
						@NonNull Lock lock,
						long time,
						@NonNull TimeUnit unit,
						@NonNull ThrowableSupplier<R, T> onSuccess,
						ThrowableSupplier<R, T> onFail)
						throws T, InterruptedException {
			return AutoLock.tryLockAndGet(lock, time, unit, onSuccess, onFail);
		}

		@Override
		<T extends Throwable> void tryLockAndRun(
						@NonNull Lock lock,
						@NonNull Duration timeout,
						@NonNull ThrowableRunnable<T> onSuccess,
						@NonNull ThrowableRunnable<T> onFail)
						throws T, InterruptedException {
			AutoLock.tryLockAndRun(lock, timeout, onSuccess, onFail);
		}

		@Override
		<R, T extends Throwable> R tryLockAndGet(
						@NonNull Lock lock,
						@NonNull Duration timeout,
						@NonNull ThrowableSupplier<R, T> onSuccess,
						ThrowableSupplier<R, T> onFail)
						throws T, InterruptedException {
			return AutoLock.tryLockAndGet(lock, timeout, onSuccess, onFail);
		}

		@DisplayName("Attempt to run lockAndRun(Lock,Runnable) method and have the inside function to throw exception")
		@Test
		void testLockAndRunWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.lockAndRun(rl, () -> {
								throw exception;
							})
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run lockAndGet(Lock,Supplier) method and have the inside function to throw exception")
		@Test
		void testLockAndGetWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(FileNotFoundException.class, () -> AutoLock.lockAndGet(rl, () -> {
				throw exception;
			}));

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run lockInterruptiblyAndRun(Lock,Runnable) method and have the inside function to throw exception")
		@Test
		void testLockInterruptiblyAndRunWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.lockInterruptiblyAndRun(rl, () -> {
								throw exception;
							})
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run lockInterruptiblyAndGet(Lock,Supplier) method and have the inside function to throw exception")
		@Test
		void testLockInterruptiblyAndGetWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.lockInterruptiblyAndGet(rl, () -> {
								throw exception;
							})
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run tryLockAndRun(Lock,Runnable) method and have the inside function to throw exception")
		@Test
		void testTryLockAndRunWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.tryLockAndRun(
											rl,
											() -> {
												throw exception;
											},
											Assertions::fail
							)
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run tryLockAndGet(Lock,Supplier) method and have the inside function to throw exception")
		@Test
		void testTryLockAndGetWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.tryLockAndGet(
											rl,
											() -> {
												throw exception;
											},
											Assertions::fail
							)
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run tryLockAndRun(Lock,long,TimeUnit,Runnable) method and have the inside function to throw exception")
		@Test
		void testTryLockAndRunTimeUnitWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.tryLockAndRun(rl, 1, TimeUnit.MINUTES,
											() -> {
												throw exception;
											},
											Assertions::fail
							)
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run tryLockAndGet(Lock,long,TimeUnit,Supplier) method and have the inside function to throw exception")
		@Test
		void testTryLockAndGetTimeUnitWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.tryLockAndGet(rl, 1, TimeUnit.MINUTES,
											() -> {
												throw exception;
											},
											Assertions::fail
							)
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run tryLockAndRun(Lock,Duration,Runnable) method and have the inside function to throw exception")
		@Test
		void testTryLockAndRunDurationWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.tryLockAndRun(rl, Duration.ofMinutes(1),
											() -> {
												throw exception;
											},
											Assertions::fail
							)
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}

		@DisplayName("Attempt to run tryLockAndGet(Lock,Duration,Supplier) method and have the inside function to throw exception")
		@Test
		void testTryLockAndGetDurationWithException() {

			// Create lock
			ReentrantLock rl = new ReentrantLock();

			// Exception
			FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

			// Do it
			FileNotFoundException thrown = assertThrows(
							FileNotFoundException.class,
							() -> AutoLock.tryLockAndGet(rl, Duration.ofMinutes(1),
											() -> {
												throw exception;
											},
											Assertions::fail
							)
			);

			// Get cause and compare
			assertEquals(exception, thrown);
		}
	}
}
