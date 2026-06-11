package test;

import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.getRandomObjects;
import static test.TestUtility.getSeed;

interface MultiLockGetTest {

	@DisplayName("multi-lock-get: with null locks")
	@Test
	default void testMultiLockGet_NullLocks() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(null, Assertions::fail));
		assertEquals("locks must not be null", exception.getMessage());
	}

	@DisplayName("multi-lock-get: with 1st null lock")
	@Test
	default void testMultiLockGet_NullLock1() {
		try (StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(new Lock[]{null, lock2, lock3, lock4}, Assertions::fail));
			assertEquals("lock1 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lock-get: with 2nd null lock")
	@Test
	default void testMultiLockGet_NullLock2() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(new Lock[]{lock1, null, lock3, lock4}, Assertions::fail));
			assertEquals("lock2 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lock-get: with 3rd null lock")
	@Test
	default void testMultiLockGet_NullLock3() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(new Lock[]{lock1, lock2, null, lock4}, Assertions::fail));
			assertEquals("lock3 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lock-get: with 4th null lock")
	@Test
	default void testMultiLockGet_NullLock4() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(new Lock[]{lock1, lock2, lock3, null}, Assertions::fail));
			assertEquals("lock4 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lock-get: successful lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockGet_Success() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockGet_Success(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiLockGet_Success(@NonNull Supplier<Object> objectSupplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnLock(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLock(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicInteger unlockCount3 = new AtomicInteger();
			lock3.setOnLock(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount4 = new AtomicInteger();
			AtomicInteger unlockCount4 = new AtomicInteger();
			lock4.setOnLock(() -> {
				lockCount4.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			Object expected = objectSupplier.get();
			Object actual = performLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
				executionCount.incrementAndGet();
				lock1.setOnUnlock(() -> {
					unlockCount1.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				lock2.setOnUnlock(() -> {
					unlockCount2.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				lock3.setOnUnlock(() -> {
					unlockCount3.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				lock4.setOnUnlock(() -> {
					unlockCount4.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				return expected;
			});
			assertSame(expected, actual);
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount2.get());
			assertEquals(1, lockCount2.get());
			assertEquals(1, unlockCount2.get());
			assertEquals(1, lockCount3.get());
			assertEquals(1, unlockCount3.get());
			assertEquals(1, lockCount4.get());
			assertEquals(1, unlockCount4.get());
			assertEquals(1, executionCount.get());
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.addAll(lock3.getActualEvents());
			finalEvents.addAll(lock4.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.LOCK),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	<Return, T extends Throwable> Return performLockAndGet(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T> supplier) throws T;
}
