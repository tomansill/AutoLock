package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.LockedAutoLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static com.ansill.lock.autolock.test.AutoLockTest.beforeLock;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Instance Methods")
class AutoLockInstanceMethodsTest implements AutoLockTest {

	@DisplayName("Attempt to successfully double-unlock")
	@Test
	void testDoubleUnlock() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Create AutoLock
		AutoLock al = AutoLock.create(rl);

		// Do before
		Supplier<Runnable> duringThread = beforeLock(rl);

		// Set up after-lock runnable reference
		Runnable afterLock;

		// Lock it
		try (LockedAutoLock lock = al.doLock()) {

			// Do during-thread test and get post lock runnable
			afterLock = duringThread.get();

			// Unlock here
			lock.unlock();

		} // Will unlock here

		// Check if it's unlocked
		assertFalse(al.isLocked());

		// Do after-lock test
		afterLock.run();

	}

	@DisplayName("Attempt to create AutoLock with null Lock")
	@Test
	void testCreateWithNull() {
		//noinspection DataFlowIssue
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> AutoLock.create(null));
		assertEquals("'lock' is null", exception.getMessage());
	}

	@DisplayName("Attempt to successfully run doLock() method")
	@Test
	void testDoLock() {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Create AutoLock
		AutoLock al = AutoLock.create(rl);

		// Do before
		Supplier<Runnable> duringThread = beforeLock(rl);

		// Set up after-lock runnable reference
		Runnable afterLock;

		// Lock it
		try (LockedAutoLock ignored = al.doLock()) {

			// Do during-thread test and get post lock runnable
			afterLock = duringThread.get();
		}

		// Check if it's unlocked
		assertFalse(al.isLocked());

		// Do after-lock test
		afterLock.run();

	}

	@DisplayName("Attempt to successfully run doLockInterruptibly() method")
	@Test
	void testDoLockInterruptibly() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Create AutoLock
		AutoLock al = AutoLock.create(rl);

		// Do before
		Supplier<Runnable> duringThread = beforeLock(rl);

		// Set up after-lock runnable reference
		Runnable afterLock;

		// Lock it
		try (LockedAutoLock ignored = al.doLockInterruptibly()) {

			// Check if its locked
			assertTrue(al.isLocked());

			// Do during-thread test and get post lock runnable
			afterLock = duringThread.get();
		}

		// Check if it's unlocked
		assertFalse(al.isLocked());

		// Do after-lock test
		afterLock.run();
	}

	@DisplayName("Attempt to run doLockInterruptibly() method and call interrupt")
	@Test
	void testDoLockInterruptiblyInterrupt() throws InterruptedException {

		// Create lock
		ReentrantLock rl = new ReentrantLock();

		// Create AutoLock
		AutoLock al = AutoLock.create(rl);

		// Lock it
		rl.lock();

		// Flag for successful interrupt
		AtomicBoolean success = new AtomicBoolean(false);

		// Create thread
		Thread thread = new Thread(() -> {

			try (LockedAutoLock ignored = al.doLockInterruptibly()) {
				fail("Lock obtained");
			} catch (InterruptedException e) {
				success.set(true);
			}

		});

		// Start thread
		thread.start();

		// Wait a tiny bit
		Thread.sleep(EXECUTION_TIME.toMillis());

		// Interrupt
		thread.interrupt();

		// Wait for thread to join
		thread.join();

		// Ensure that lock has been interrupted
		assertTrue(success.get());

	}
}
