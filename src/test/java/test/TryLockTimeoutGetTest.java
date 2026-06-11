package test;

import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

interface TryLockTimeoutGetTest {

	static TimeoutLessPerform convertFromDurationGet(final TryLockTimeoutGetTest tryLockTimeoutGetTest, @NonNull Duration duration) {
		return new TimeoutLessPerform() {
			@Override
			public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
				return tryLockTimeoutGetTest.performTryLockAndGetDuration(lock, duration, onLockSuccess, onLockFail);
			}
		};
	}

	static TimeoutLessPerform convertFromTimeUnitGet(final TryLockTimeoutGetTest tryLockTimeoutGetTest, @NonNull Duration duration) {
		return new TimeoutLessPerform() {
			@Override
			public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
				return tryLockTimeoutGetTest.performTryLockAndGetLongAndTimeUnit(lock, duration.toMillis(), TimeUnit.MILLISECONDS, onLockSuccess, onLockFail);
			}
		};
	}

	@DisplayName("tryLock-timeout-duration-get: with null lock")
	@Test
	default void testTryLockTimeoutDurationGet_NullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDuration(null, Duration.ZERO, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("tryLock-timeout-duration-get: with null duration")
	@Test
	default void testTryLockTimeoutDurationGet_NullDuration() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDuration(lock, null, Assertions::fail, Assertions::fail));
			assertEquals("timeout must not be null", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-duration-get: with negative duration")
	@Test
	default void testTryLockTimeoutDurationGet_NegativeDuration() {
		try (StubbedLock lock = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndGetDuration(lock, Duration.ofSeconds(-1), Assertions::fail, Assertions::fail));
			assertEquals("timeout must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-duration-get: with null onLockSuccess supplier")
	@Test
	default void testTryLockTimeoutDurationGet_NullOnLockSuccessSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDuration(lock, Duration.ZERO, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("tryLock-timeout-duration-get: with null onLockFail supplier")
	@Test
	default void testTryLockTimeoutDurationGet_NullOnLockFailSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDuration(lock, Duration.ZERO, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-get: with null lock")
	@Test
	default void testTryLockTimeoutLongUnitGet_NullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnit(null, 0, TimeUnit.MILLISECONDS, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("tryLock-timeout-long/unit-get: with null TimeUnit")
	@Test
	default void testTryLockTimeoutLongUnitGet_NullTimeUnit() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnit(lock, 0, null, Assertions::fail, Assertions::fail));
			assertEquals("unit must not be null", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-get: with negative time")
	@Test
	default void testTryLockTimeoutLongUnitGet_NegativeTime() {
		try (StubbedLock lock = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndGetLongAndTimeUnit(lock, -1, TimeUnit.MILLISECONDS, Assertions::fail, Assertions::fail));
			assertEquals("time must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-get: with null onLockSuccess supplier")
	@Test
	default void testTryLockTimeoutLongUnitGet_NullOnLockSuccessSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnit(lock, 0, TimeUnit.MILLISECONDS, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-get: with null onLockFail supplier")
	@Test
	default void testTryLockTimeoutLongUnitGet_NullOnLockFailSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnit(lock, 0, TimeUnit.MILLISECONDS, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("tryLock-instant-get: successful lock")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationGet_Success() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationGet_Success(entry.getValue(), convertFromDurationGet(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationGet_Success(entry.getValue(), convertFromTimeUnitGet(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationGet_Success(@NonNull Supplier<Object> objectSupplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) throws InterruptedException {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Object expectedObject = objectSupplier.get();
			Object actualObject = perform.performTryLockAndGet(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				return expectedObject;
			}, Assertions::fail);
			assertSame(expectedObject, actualObject);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-instant-get: failed lock")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationGet_Failed() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationGet_Failed(entry.getValue(), convertFromDurationGet(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationGet_Failed(entry.getValue(), convertFromTimeUnitGet(this, testDuration), testDuration)));

		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationGet_Failed(@NonNull Supplier<Object> objectSupplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) throws InterruptedException {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicBoolean failedSupplierReached = new AtomicBoolean(false);
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return false;
			});
			Object expectedObject = objectSupplier.get();
			Object actualObject = perform.performTryLockAndGet(lock, Assertions::fail, () -> {
				failedSupplierReached.set(true);
				return expectedObject;
			});
			assertSame(expectedObject, actualObject);
			assertTrue(failedSupplierReached.get());
			assertEquals(1, lockCount.get());
			assertEquals(
							Collections.singletonList(
											new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-instant-get: exception thrown inside onSuccessLock supplier")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), convertFromDurationGet(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), convertFromTimeUnitGet(this, testDuration), testDuration)));

		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Throwable actualThrowable = assertThrows(Throwable.class, () -> perform.performTryLockAndGet(lock, () -> {
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
											new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-instant-get: with Throwable thrown at tryLock()")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationGet_ThrowableAtTryLockMethod() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).values().stream().flatMap(supplier -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(supplier, convertFromDurationGet(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(supplier, convertFromTimeUnitGet(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> perform.performTryLockAndGet(lock, Assertions::fail, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(
							Collections.singletonList(
											new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-instant-get: with Throwable thrown at unlock()")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationGet_ThrowableAtUnlockMethod() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(entry.getValue(), convertFromDurationGet(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(entry.getValue(), convertFromTimeUnitGet(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Throwable actualThrowable = assertThrows(Throwable.class, () -> perform.performTryLockAndGet(lock, () -> {
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
											new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-instant-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock()")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod() {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().flatMap(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), convertFromDurationGet(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), convertFromTimeUnitGet(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
			AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Throwable actualThrowable = assertThrows(
							Throwable.class,
							() -> perform.performTryLockAndGet(lock, () -> {
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
											new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-instant-get: exception thrown inside onLockFail supplier")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier(entry.getValue(), convertFromDurationGet(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier(entry.getValue(), convertFromTimeUnitGet(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return false;
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Throwable actualThrowable = assertThrows(Throwable.class, () -> perform.performTryLockAndGet(lock, Assertions::fail, () -> {
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
											new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS)
							),
							lock.getActualEvents()
			);
		}
	}

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetLongAndTimeUnit(@Nullable Lock lock, long time, @Nullable TimeUnit unit, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2;

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetDuration(@Nullable Lock lock, @Nullable Duration duration, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2;


	/* Attempt to reduce boilerplate by reusing test code across Duration and Long+TimeUnit */
	@FunctionalInterface
	interface TimeoutLessPerform {
		<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2;
	}
}
