package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableFunction;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

interface MultiTryLockInstantGetTest {


	@DisplayName("multi-tryLock-get: with 1st null lock")
	@Test
	default void testMultiTryLockGet_NullLock1() {
		try (StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(new Lock[]{null, lock2, lock3, lock4}, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock1 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-get: with 2nd null lock")
	@Test
	default void testMultiTryLockGet_NullLock2() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(new Lock[]{lock1, null, lock3, lock4}, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock2 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-get: with 3rd null lock")
	@Test
	default void testMultiTryLockGet_NullLock3() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(new Lock[]{lock1, lock2, null, lock4}, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock3 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-get: with 4th null lock")
	@Test
	default void testMultiTryLockGet_NullLock4() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(new Lock[]{lock1, lock2, lock3, null}, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock4 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-instant-get: with null onLockSuccess supplier")
	@Test
	default void testMultiTryLockInstantGet_NullOnLockSuccessSupplier() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, null, ctx -> Assertions.fail()));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-instant-get: with null onLockFail supplier")
	@Test
	default void testMultiTryLockInstantGet_NullOnLockFailSupplier() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-instant-get: successful lock with 4 locks (max)")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_Success4() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_Success4(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_Success4(@NonNull Supplier<Object> objectSupplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnTryLockInstant(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnTryLockInstant(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicInteger lockCount3 = new AtomicInteger();
			AtomicInteger unlockCount3 = new AtomicInteger();
			lock3.setOnTryLockInstant(() -> {
				lockCount3.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicInteger lockCount4 = new AtomicInteger();
			AtomicInteger unlockCount4 = new AtomicInteger();
			lock4.setOnTryLockInstant(() -> {
				lockCount4.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Object expected = objectSupplier.get();
			Object actual = performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
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
			}, ctx -> Assertions.fail());
			assertSame(expected, actual);
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
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-tryLock-instant-get: successful lock with 2 locks (min)")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_Success2() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_Success2(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_Success2(@NonNull Supplier<Object> objectSupplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicInteger lockCount1 = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount1 = new AtomicInteger();
			lock1.setOnTryLockInstant(() -> {
				lockCount1.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicInteger lockCount2 = new AtomicInteger();
			AtomicInteger unlockCount2 = new AtomicInteger();
			lock2.setOnTryLockInstant(() -> {
				lockCount2.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Object expected = objectSupplier.get();
			Object actual = performTryLockAndGet(new Lock[]{lock1, lock2}, () -> {
				executionCount.incrementAndGet();
				lock1.setOnUnlock(() -> {
					unlockCount1.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				lock2.setOnUnlock(() -> {
					unlockCount2.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				return expected;
			}, ctx -> Assertions.fail());
			assertSame(expected, actual);
			assertEquals(1, lockCount1.get());
			assertEquals(1, unlockCount1.get());
			assertEquals(1, lockCount2.get());
			assertEquals(1, unlockCount2.get());
			assertEquals(1, executionCount.get());
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-tryLock-instant-get: failed lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_Failed() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_Failed(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_Failed(@NonNull Supplier<Object> objectSupplier) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicBoolean failedSupplierReached = new AtomicBoolean(false);
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return false;
			});
			Object expectedObject = objectSupplier.get();
			Object actualObject = performTryLockAndGet(lock, Assertions::fail, () -> {
				failedSupplierReached.set(true);
				return expectedObject;
			});
			assertSame(expectedObject, actualObject);
			assertTrue(failedSupplierReached.get());
			assertEquals(1, lockCount.get());
			assertEquals(
							Collections.singletonList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("multi-tryLock-instant-get: exception thrown inside onSuccessLock supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier() {
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndGet(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			}, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at tryLock()")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtTryLockMethod() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtTryLockMethod(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableAtTryLockMethod(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndGet(lock, Assertions::fail, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(
							Collections.singletonList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at unlock()")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtUnlockMethod() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtUnlockMethod(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndGet(lock, () -> {
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				return null;
			}, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(1, unlockCount.get());
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock()")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod() {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().map(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			return DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue()));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
			AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Throwable actualThrowable = assertThrows(
							Throwable.class,
							() -> performTryLockAndGet(lock, () -> {
								lock.setOnUnlock(() -> {
									unlockCount.getAndIncrement();
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
							}, Assertions::fail));
			assertSame(mainThrowableRef.get(), actualThrowable);
			assertEquals(1, actualThrowable.getSuppressed().length);
			assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
			assertEquals(1, lockCount.get());
			assertEquals(1, unlockCount.get());
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("multi-tryLock-instant-get: exception thrown inside onLockFail supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInsideOnFailLockSupplier() {
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInsideOnFailLockSupplier(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableInsideOnFailLockSupplier(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return false;
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndGet(lock, Assertions::fail, () -> {
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			}));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(
							Collections.singletonList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws T1, T2;
}
