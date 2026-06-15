package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableFunction;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.time.Instant;
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

	@DisplayName("multi-tryLock-instant-get: failed lock on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_Failed1st() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_Failed(entry.getValue(), 0))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: failed lock on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_Failed2nd() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_Failed(entry.getValue(), 1))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: failed lock on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_Failed3rd() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_Failed(entry.getValue(), 2))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: failed lock on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_Failed4th() {
		return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_Failed(entry.getValue(), 3))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_Failed(@NonNull Supplier<Object> objectSupplier, int lockFailPosition) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			AtomicBoolean failedSupplierReached = new AtomicBoolean(false);
			Thread currentThread = Thread.currentThread();
			lock1.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				if (lockFailPosition != 0) {
					lock1.setOnUnlock(() -> {
						assertSame(currentThread, Thread.currentThread());
					});
					return true;
				} else {
					return false;
				}
			});
			if (lockFailPosition >= 1) {
				lock2.setOnTryLockInstant(() -> {
					assertSame(currentThread, Thread.currentThread());
					if (lockFailPosition != 1) {
						lock2.setOnUnlock(() -> {
							assertSame(currentThread, Thread.currentThread());
						});
						return true;
					} else {
						return false;
					}
				});
			}
			if (lockFailPosition >= 2) {
				lock3.setOnTryLockInstant(() -> {
					assertSame(currentThread, Thread.currentThread());
					if (lockFailPosition != 2) {
						lock3.setOnUnlock(() -> {
							assertSame(currentThread, Thread.currentThread());
						});
						return true;
					} else {
						return false;
					}
				});
			}
			if (lockFailPosition >= 3) {
				lock4.setOnTryLockInstant(() -> {
					assertSame(currentThread, Thread.currentThread());
					if (lockFailPosition != 3) {
						lock4.setOnUnlock(() -> {
							assertSame(currentThread, Thread.currentThread());
						});
						return true;
					} else {
						return false;
					}
				});
			}
			Object expectedObject = objectSupplier.get();
			AtomicReference<AutoLock.MultipleLocks.TryLockFailContext> contextRef = new AtomicReference<>();
			AtomicReference<Instant> timestampOfOnLockFail = new AtomicReference<>();
			Object actualObject = performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, Assertions::fail, (ctx) -> {
				timestampOfOnLockFail.set(Instant.now());
				failedSupplierReached.set(true);
				contextRef.set(ctx);
				return expectedObject;
			});
			assertSame(expectedObject, actualObject);
			assertTrue(failedSupplierReached.get());
			AutoLock.MultipleLocks.TryLockFailContext context = contextRef.get();
			assertNotNull(context);
			assertEquals(lockFailPosition, context.getSequenceIndex());
			if (lockFailPosition == 0) assertEquals(lock1, context.getFailedLock());
			else if (lockFailPosition == 1) assertEquals(lock2, context.getFailedLock());
			else if (lockFailPosition == 2) assertEquals(lock3, context.getFailedLock());
			else if (lockFailPosition == 3) assertEquals(lock4, context.getFailedLock());
			else fail("what? " + lockFailPosition);
			List<StubbedLock.CallEvent> expectedList = new ArrayList<>();
			expectedList.add(new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK));
			if (lockFailPosition >= 1) {
				expectedList.add(new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK));
				if (lockFailPosition >= 2) {
					expectedList.add(new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK));
					if (lockFailPosition == 3) {
						expectedList.add(new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK));
						expectedList.add(new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK));
					}
					expectedList.add(new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK));
				}
				expectedList.add(new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK));
			}
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(expectedList, actualList);

			// Assert that onLockFail is run AFTER all unlocks, not between
			Instant instantOfOnLockFail = timestampOfOnLockFail.get();
			assertNotNull(instantOfOnLockFail);
			assertTrue(instantOfOnLockFail.isAfter(actualList.get(actualList.size() - 1).timestamp));
		}
	}

	@DisplayName("multi-tryLock-instant-get: exception thrown inside onSuccessLock supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier() {
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue()))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier(@NonNull Supplier<? extends Throwable> supplier) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			AtomicInteger executionCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock1.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock3.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock4.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
				executionCount.incrementAndGet();
				lock4.setOnUnlock(() -> {
					assertSame(currentThread, Thread.currentThread());
					lock3.setOnUnlock(() -> {
						assertSame(currentThread, Thread.currentThread());
						lock2.setOnUnlock(() -> {
							assertSame(currentThread, Thread.currentThread());
							lock1.setOnUnlock(() -> assertSame(currentThread, Thread.currentThread()));
						});
					});
				});
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			}, ctx -> Assertions.fail()));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, executionCount.get());
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
											new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
											new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
											new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							actualList
			);
		}
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at tryLock() on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtTryLockMethod1st() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtTryLockMethod(entry.getValue(), 0))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at tryLock() on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtTryLockMethod2nd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtTryLockMethod(entry.getValue(), 1))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at tryLock() on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtTryLockMethod3rd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtTryLockMethod(entry.getValue(), 2))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at tryLock() on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtTryLockMethod4th() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtTryLockMethod(entry.getValue(), 3))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableAtTryLockMethod(@NonNull Supplier<? extends Throwable> supplier, int lockFailPosition) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock1.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				if (lockFailPosition != 0) {
					lock1.setOnUnlock(() -> {
						assertSame(currentThread, Thread.currentThread());
					});
					return true;
				} else {
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				}
			});
			if (lockFailPosition >= 1) {
				lock2.setOnTryLockInstant(() -> {
					assertSame(currentThread, Thread.currentThread());
					if (lockFailPosition != 1) {
						lock2.setOnUnlock(() -> {
							assertSame(currentThread, Thread.currentThread());
						});
						return true;
					} else {
						try {
							throw supplier.get();
						} catch (Throwable throwable) {
							throwableRef.set(throwable);
							throw throwable;
						}
					}
				});
			}
			if (lockFailPosition >= 2) {
				lock3.setOnTryLockInstant(() -> {
					assertSame(currentThread, Thread.currentThread());
					if (lockFailPosition != 2) {
						lock3.setOnUnlock(() -> {
							assertSame(currentThread, Thread.currentThread());
						});
						return true;
					} else {
						try {
							throw supplier.get();
						} catch (Throwable throwable) {
							throwableRef.set(throwable);
							throw throwable;
						}
					}
				});
			}
			if (lockFailPosition >= 3) {
				lock4.setOnTryLockInstant(() -> {
					assertSame(currentThread, Thread.currentThread());
					if (lockFailPosition != 3) {
						lock4.setOnUnlock(() -> {
							assertSame(currentThread, Thread.currentThread());
						});
						return true;
					} else {
						try {
							throw supplier.get();
						} catch (Throwable throwable) {
							throwableRef.set(throwable);
							throw throwable;
						}
					}
				});
			}
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, Assertions::fail, ctx -> Assertions.fail()));
			assertSame(throwableRef.get(), actualThrowable);
			List<StubbedLock.CallEvent> expectedList = new ArrayList<>();
			expectedList.add(new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK));
			if (lockFailPosition >= 1) {
				expectedList.add(new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK));
				if (lockFailPosition >= 2) {
					expectedList.add(new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK));
					if (lockFailPosition == 3) {
						expectedList.add(new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK));
						expectedList.add(new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK));
					}
					expectedList.add(new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK));
				}
				expectedList.add(new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK));
			}
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(expectedList, actualList);
		}
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at unlock() on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtUnlockMethod1st() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtUnlockMethod(entry.getValue(), 0))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at unlock() on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtUnlockMethod2nd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtUnlockMethod(entry.getValue(), 1))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at unlock() on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtUnlockMethod3rd() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtUnlockMethod(entry.getValue(), 2))).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown at unlock() on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableAtUnlockMethod4th() {
		return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableAtUnlockMethod(entry.getValue(), 3))).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier, int lockFailPosition) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock1.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock3.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock4.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
				lock1.setOnUnlock(() -> {
					if (lockFailPosition == 0) {
						try {
							throw supplier.get();
						} catch (Throwable throwable) {
							throwableRef.set(throwable);
							throw throwable;
						}
					}
				});
				lock2.setOnUnlock(() -> {
					if (lockFailPosition == 1) {
						try {
							throw supplier.get();
						} catch (Throwable throwable) {
							throwableRef.set(throwable);
							throw throwable;
						}
					}
				});
				lock3.setOnUnlock(() -> {
					if (lockFailPosition == 2) {
						try {
							throw supplier.get();
						} catch (Throwable throwable) {
							throwableRef.set(throwable);
							throw throwable;
						}
					}
				});
				lock4.setOnUnlock(() -> {
					if (lockFailPosition == 3) {
						try {
							throw supplier.get();
						} catch (Throwable throwable) {
							throwableRef.set(throwable);
							throw throwable;
						}
					}
				});
				return null;
			}, ctx -> Assertions.fail()));
			assertSame(throwableRef.get(), actualThrowable);
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), actualList);
		}
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod1st() {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().map(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			return DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), 0));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod2nd() {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().map(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			return DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), 1));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod3rd() {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().map(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			return DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), 2));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-instant-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod4th() {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().map(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			return DynamicTest.dynamicTest(entry.getKey(), () -> this.testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), 3));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier, int lockFailPosition) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
			AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
			lock1.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock3.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock4.setOnTryLockInstant(() -> {
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Throwable actualThrowable = assertThrows(
							Throwable.class,
							() -> performTryLockAndGet(new Lock[]{lock1, lock2, lock3, lock4}, () -> {
								lock1.setOnUnlock(() -> {
									if (lockFailPosition == 0) {
										try {
											throw supplier.get();
										} catch (Throwable throwable) {
											unlockThrowableRef.set(throwable);
											throw throwable;
										}
									}
								});
								lock2.setOnUnlock(() -> {
									if (lockFailPosition == 1) {
										try {
											throw supplier.get();
										} catch (Throwable throwable) {
											unlockThrowableRef.set(throwable);
											throw throwable;
										}
									}
								});
								lock3.setOnUnlock(() -> {
									if (lockFailPosition == 2) {
										try {
											throw supplier.get();
										} catch (Throwable throwable) {
											unlockThrowableRef.set(throwable);
											throw throwable;
										}
									}
								});
								lock4.setOnUnlock(() -> {
									if (lockFailPosition == 3) {
										try {
											throw supplier.get();
										} catch (Throwable throwable) {
											unlockThrowableRef.set(throwable);
											throw throwable;
										}
									}
								});
								try {
									throw mainExceptionSupplier.get();
								} catch (Throwable throwable) {
									mainThrowableRef.set(throwable);
									throw throwable;
								}
							}, ctx -> Assertions.fail()));
			assertSame(mainThrowableRef.get(), actualThrowable);
			assertEquals(1, actualThrowable.getSuppressed().length);
			assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), actualList);
		}
	}

		/*
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
	}*/

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws T1, T2;
}
