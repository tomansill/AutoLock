package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.LockedAutoLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Static Methods")
@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
class AutoLockStaticMethodsTest implements AutoLockTest {

	@Test
	void shouldNotAllowInstantiationViaReflection() throws Exception {
		Constructor<AutoLock> constructor = AutoLock.class.getDeclaredConstructor();

		constructor.setAccessible(true);

		Exception exception = assertThrows(Exception.class, constructor::newInstance);

		assertTrue(exception.getCause() instanceof UnsupportedOperationException || exception instanceof UnsupportedOperationException
		);
	}

	@DisplayName("Attempt to successfully run lock(Lock) method")
	@Test
	void testLock() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		Runnable afterLock;

		// Lock it
		try (LockedAutoLock ignored = AutoLock.lock(rl)) {

			// Do during-thread test and get post lock runnable
			afterLock = duringThread.get();
		}

		// Do after-lock test
		afterLock.run();
	}

	@DisplayName("Attempt to successfully run lockInterruptibly(Lock) method")
	@Test
	void testLockInterruptibly() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Do before
		Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

		// Set up after-lock runnable reference
		Runnable afterLock;

		// Lock it
		try (LockedAutoLock ignored = AutoLock.lockInterruptibly(rl)) {

			// Do during-thread test and get post lock runnable
			afterLock = duringThread.get();
		}

		// Do after-lock test
		afterLock.run();
	}

	@DisplayName("Attempt to run lockInterruptibly(Lock) method and call interrupt")
	@Test
	void testLockInterruptiblyInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			try (LockedAutoLock ignored = AutoLock.lockInterruptibly(rl)) {
				fail("Lock obtained");
			} catch (InterruptedException e) {
				success.set(true);
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
}
