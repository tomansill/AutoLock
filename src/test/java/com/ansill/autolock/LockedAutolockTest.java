package com.ansill.autolock;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class LockedAutolockTest {

	@DisplayName("test double close on LockedAutoLock")
	@Test
	void testDoubleClose(){

		// Create mocked lock
		AtomicInteger timesUnlocked = new AtomicInteger(0);
		Lock mockedLock = new Lock(){

			@Override
			public void lock() {

			}

			@Override
			public void lockInterruptibly() {
				fail();
			}

			@Override
			public boolean tryLock() {
				fail();
				return false;
			}

			@Override
			public boolean tryLock(long time, @NonNull TimeUnit unit) {
				fail();
				return false;
			}

			@Override
			public void unlock() {
				timesUnlocked.incrementAndGet();
			}

			@NonNull
			@Override
			public Condition newCondition() {
				fail();
				return null;
			}
		};

		// Assert times unlocked
		assertEquals(0, timesUnlocked.get());

		// Create
		LockedAutoLock captured;
		try(LockedAutoLock handle = new LockedAutoLock(mockedLock)){
			captured = handle;
			assertEquals(0, timesUnlocked.get());
		}
		assertEquals(1, timesUnlocked.get());
		captured.close();
		assertEquals(1, timesUnlocked.get());
		mockedLock.unlock();
		assertEquals(2, timesUnlocked.get());
	}
}
