package test;

import com.ansill.autolock.ThrowableRunnable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

interface MultiLockInterruptiblyRunTest {

	@DisplayName("multi-lockInterruptibly-run: with 1st null lock")
	@Test
	default void testMultiLockInterruptiblyRun_NullLock1() {
		try (StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(new Lock[]{null, lock2, lock3, lock4}, Assertions::fail));
			assertEquals("lock1 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with 2nd null lock")
	@Test
	default void testMultiLockInterruptiblyRun_NullLock2() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, null, lock3, lock4}, Assertions::fail));
			assertEquals("lock2 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with 3rd null lock")
	@Test
	default void testMultiLockInterruptiblyRun_NullLock3() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, null, lock4}, Assertions::fail));
			assertEquals("lock3 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with 4th null lock")
	@Test
	default void testMultiLockInterruptiblyRun_NullLock4() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, null}, Assertions::fail));
			assertEquals("lock4 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-lockInterruptibly-run: successful lock with 4 locks (max)")
	@Test
	default void testMultiLockInterruptiblyRun_Success4() throws InterruptedException {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicInteger unlockCount3 = new AtomicInteger();
			lock3.setOnLockInterruptibly(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount4 = new AtomicInteger();
			AtomicInteger unlockCount4 = new AtomicInteger();
			lock4.setOnLockInterruptibly(() -> {
				lockCount4.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
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
				executionCount.incrementAndGet();
			});
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
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
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: successful lock with 2 locks (min)")
	@Test
	default void testMultiLockInterruptiblyRun_Success2() throws InterruptedException {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			performLockInterruptiblyAndRun(new Lock[]{lock1, lock2}, () -> {
				lock1.setOnUnlock(() -> {
					unlockCount1.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				lock2.setOnUnlock(() -> {
					unlockCount2.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				executionCount.incrementAndGet();
			});
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
			assertEquals(1, lockCount2.get());
			assertEquals(1, unlockCount2.get());
			assertEquals(1, executionCount.get());
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: exception thrown inside supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableInsideSupplier() {
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableInsideSupplier(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiLockInterruptiblyRun_ThrowableInsideSupplier(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicInteger unlockCount3 = new AtomicInteger();
			lock3.setOnLockInterruptibly(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount4 = new AtomicInteger();
			AtomicInteger unlockCount4 = new AtomicInteger();
			lock4.setOnLockInterruptibly(() -> {
				lockCount4.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
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
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			}));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
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
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at lock() on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtLockMethod4th() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtLockMethod4th(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiLockInterruptiblyRun_ThrowableAtLockMethod4th(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicInteger unlockCount3 = new AtomicInteger();
			lock3.setOnLockInterruptibly(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount4 = new AtomicInteger();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock4.setOnLockInterruptibly(() -> {
				lockCount4.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
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
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
			assertEquals(1, lockCount2.get());
			assertEquals(1, unlockCount2.get());
			assertEquals(1, lockCount3.get());
			assertEquals(1, unlockCount3.get());
			assertEquals(1, lockCount4.get());
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.addAll(lock3.getActualEvents());
			finalEvents.addAll(lock4.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at lock() on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtLockMethod3rd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtLockMethod3rd(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiLockInterruptiblyRun_ThrowableAtLockMethod3rd(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock3.setOnLockInterruptibly(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				lock1.setOnUnlock(() -> {
					unlockCount1.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				lock2.setOnUnlock(() -> {
					unlockCount2.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
			assertEquals(1, lockCount2.get());
			assertEquals(1, unlockCount2.get());
			assertEquals(1, lockCount3.get());
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.addAll(lock3.getActualEvents());
			finalEvents.addAll(lock4.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at lock() on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtLockMethod2nd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtLockMethod2nd(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiLockInterruptiblyRun_ThrowableAtLockMethod2nd(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				lock1.setOnUnlock(() -> {
					unlockCount1.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
			assertEquals(1, lockCount2.get());
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.addAll(lock3.getActualEvents());
			finalEvents.addAll(lock4.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at lock() on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtLockMethod1st() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtLockMethod1st(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiLockInterruptiblyRun_ThrowableAtLockMethod1st(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount1.get());
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.addAll(lock3.getActualEvents());
			finalEvents.addAll(lock4.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Collections.singletonList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at unlock() on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod4th() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod(entry.getValue(), 3))).collect(Collectors.toList());
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at unlock() on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod3rd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod(entry.getValue(), 2))).collect(Collectors.toList());
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at unlock() on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod2nd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod(entry.getValue(), 1))).collect(Collectors.toList());
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown at unlock() on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod1st() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod(entry.getValue(), 0))).collect(Collectors.toList());
	}

	default void testMultiLockInterruptiblyRun_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier, int position) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			AtomicInteger failLocation = new AtomicInteger(-1);
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicInteger unlockCount3 = new AtomicInteger();
			lock3.setOnLockInterruptibly(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount4 = new AtomicInteger();
			AtomicInteger unlockCount4 = new AtomicInteger();
			lock4.setOnLockInterruptibly(() -> {
				lockCount4.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
				executionCount.incrementAndGet();
				lock1.setOnUnlock(() -> {
					unlockCount1.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 0) return;
					failLocation.set(0);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				lock2.setOnUnlock(() -> {
					unlockCount2.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 1) return;
					failLocation.set(1);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				lock3.setOnUnlock(() -> {
					unlockCount3.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 2) return;
					failLocation.set(2);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				lock4.setOnUnlock(() -> {
					unlockCount4.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 3) return;
					failLocation.set(3);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
			}));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(position, failLocation.get());
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
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
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown in supplier AND Throwable thrown in unlock() on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod4th() {
		return testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod(3);
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown in supplier AND Throwable thrown in unlock() on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod3rd() {
		return testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod(2);
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown in supplier AND Throwable thrown in unlock() on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod2nd() {
		return testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod(1);
	}

	@DisplayName("multi-lockInterruptibly-run: with Throwable thrown in supplier AND Throwable thrown in unlock() on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod1st() {
		return testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod(3);
	}

	default Iterable<DynamicTest> testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod(int position) {
		Random random = new Random(getSeed(1).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().map(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			return DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), position));
		}).collect(Collectors.toList());
	}

	default void testMultiLockInterruptiblyRun_ThrowableInSupplierAndThrowableInUnlockMethod(
					@NonNull Supplier<? extends Throwable> mainExceptionSupplier,
					@NonNull Supplier<? extends Throwable> supplier,
					int position
	) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
			AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			AtomicInteger failLocation = new AtomicInteger(-1);
			lock1.setOnLockInterruptibly(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnLockInterruptibly(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicInteger unlockCount3 = new AtomicInteger();
			lock3.setOnLockInterruptibly(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicInteger lockCount4 = new AtomicInteger();
			AtomicInteger unlockCount4 = new AtomicInteger();
			lock4.setOnLockInterruptibly(() -> {
				lockCount4.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
				executionCount.incrementAndGet();
				lock1.setOnUnlock(() -> {
					unlockCount1.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 0) return;
					failLocation.set(0);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						unlockThrowableRef.set(throwable);
						throw throwable;
					}
				});
				lock2.setOnUnlock(() -> {
					unlockCount2.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 1) return;
					failLocation.set(1);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						unlockThrowableRef.set(throwable);
						throw throwable;
					}
				});
				lock3.setOnUnlock(() -> {
					unlockCount3.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 2) return;
					failLocation.set(2);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						unlockThrowableRef.set(throwable);
						throw throwable;
					}
				});
				lock4.setOnUnlock(() -> {
					unlockCount4.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
					if (position != 3) return;
					failLocation.set(3);
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						unlockThrowableRef.set(throwable);
						throw throwable;
					}
				});
				try {
					throw mainExceptionSupplier.get();
				} catch (Throwable throwable) {
					mainThrowableRef.set(throwable);
					throw throwable;
				}
			}));
			assertSame(mainThrowableRef.get(), actualThrowable);
			assertEquals(1, actualThrowable.getSuppressed().length);
			assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
			assertEquals(position, failLocation.get());
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
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
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	<T extends Throwable> void performLockInterruptiblyAndRun(@NonNull Lock[] locks, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException;
}
