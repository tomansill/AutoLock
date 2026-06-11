package test;

import com.ansill.autolock.ThrowableRunnable;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

interface TryLockTimeoutRunTest {

	@DisplayName("tryLock-timeout-duration-run: with null lock")
	@Test
	default void testTryLockTimeoutDurationRun_NullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDuration(null, Duration.ZERO, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("tryLock-timeout-duration-run: with null duration")
	@Test
	default void testTryLockTimeoutDurationRun_NullDuration() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDuration(lock, null, Assertions::fail, Assertions::fail));
			assertEquals("timeout must not be null", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-duration-run: with negative duration")
	@Test
	default void testTryLockTimeoutDurationRun_NegativeDuration() {
		try (StubbedLock lock = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndRunDuration(lock, Duration.ofSeconds(-1), Assertions::fail, Assertions::fail));
			assertEquals("timeout must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-duration-run: with null onLockSuccess supplier")
	@Test
	default void testTryLockTimeoutDurationRun_NullOnLockSuccessSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDuration(lock, Duration.ZERO, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("tryLock-timeout-duration-run: with null onLockFail supplier")
	@Test
	default void testTryLockTimeoutDurationRun_NullOnLockFailSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDuration(lock, Duration.ZERO, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-run: with null lock")
	@Test
	default void testTryLockTimeoutLongUnitRun_NullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnit(null, 0, TimeUnit.MILLISECONDS, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("tryLock-timeout-long/unit-run: with null TimeUnit")
	@Test
	default void testTryLockTimeoutLongUnitRun_NullTimeUnit() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnit(lock, 0, null, Assertions::fail, Assertions::fail));
			assertEquals("unit must not be null", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-run: with negative time")
	@Test
	default void testTryLockTimeoutLongUnitRun_NegativeTime() {
		try (StubbedLock lock = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndRunLongAndTimeUnit(lock, -1, TimeUnit.MILLISECONDS, Assertions::fail, Assertions::fail));
			assertEquals("time must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-run: with null onLockSuccess supplier")
	@Test
	default void testTryLockTimeoutLongUnitRun_NullOnLockSuccessSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnit(lock, 0, TimeUnit.MILLISECONDS, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("tryLock-timeout-long/unit-run: with null onLockFail supplier")
	@Test
	default void testTryLockTimeoutLongUnitRun_NullOnLockFailSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnit(lock, 0, TimeUnit.MILLISECONDS, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	default TimeoutLessPerform convertFromDuration(@NonNull Duration duration) {
		return new TimeoutLessPerform() {
			@Override
			public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
				performTryLockAndRunDuration(lock, duration, onLockSuccess, onLockFail);
			}
		};
	}

	default TimeoutLessPerform convertFromTimeUnit(@NonNull Duration duration) {
		return new TimeoutLessPerform() {
			@Override
			public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
				performTryLockAndRunLongAndTimeUnit(lock, duration.toMillis(), TimeUnit.MILLISECONDS, onLockSuccess, onLockFail);
			}
		};
	}

	@DisplayName("tryLock-timeout-run: successful lock")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutRun_Success() {
		Random random = new Random(getSeed(0).hashCode());
		return IntStream.range(0, AutoLockTest.MAX_REPETITIONS).boxed().flatMap(i -> {
			Duration testDuration = TestUtility.generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutRun_Success(
											convertFromDuration(testDuration),
											testDuration
							)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutRun_Success(convertFromTimeUnit(testDuration), testDuration))
			);
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutRun_Success(@NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) throws InterruptedException {
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
			perform.performTryLockAndRun(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});

			}, Assertions::fail);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-timeout-duration-run: failed lock")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationRun_Failed() {
		Random random = new Random(getSeed(0).hashCode());
		return IntStream.range(0, AutoLockTest.MAX_REPETITIONS).boxed().flatMap(i -> {
			Duration testDuration = TestUtility.generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationRun_Failed(
											convertFromDuration(testDuration),
											testDuration
							)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationRun_Failed(convertFromTimeUnit(testDuration), testDuration))
			);
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationRun_Failed(@NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) throws InterruptedException {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			AtomicBoolean failedRunnableReached = new AtomicBoolean(false);
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toMillis(), time);
				assertEquals(TimeUnit.MILLISECONDS, unit);
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return false;
			});
			perform.performTryLockAndRun(lock, Assertions::fail, () -> failedRunnableReached.set(true));
			assertTrue(failedRunnableReached.get());
			assertEquals(1, lockCount.get());
			assertEquals(
							Collections.singletonList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-timeout-duration-run: exception thrown inside onSuccessLock runnable")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockRunnable() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockRunnable(entry.getValue(), convertFromDuration(testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockRunnable(entry.getValue(), convertFromTimeUnit(testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockRunnable(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
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
			Throwable actualThrowable = assertThrows(Throwable.class, () -> perform.performTryLockAndRun(lock, () -> {
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-timeout-duration-run: with Throwable thrown at tryLock()")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationRun_ThrowableAtTryLockMethod() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(entry.getValue(), convertFromDuration(testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(entry.getValue(), convertFromTimeUnit(testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
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
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndRunDuration(lock, testDuration, Assertions::fail, Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(
							Collections.singletonList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-timeout-duration-run: with Throwable thrown at unlock()")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationRun_ThrowableAtUnlockMethod() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(entry.getValue(), convertFromDuration(testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(entry.getValue(), convertFromTimeUnit(testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
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
			Throwable actualThrowable = assertThrows(Throwable.class, () -> perform.performTryLockAndRun(lock, () -> lock.setOnUnlock(() -> {
				unlockCount.getAndIncrement();
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			}), Assertions::fail));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(1, unlockCount.get());
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-timeout-duration-run: with Throwable thrown in onLockSuccess runnable AND Throwable thrown in unlock()")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationRun_ThrowableInOnLockSuccessRunnableAndThrowableInUnlockMethod() {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableInOnLockSuccessRunnableAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), convertFromDuration(testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableInOnLockSuccessRunnableAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), convertFromTimeUnit(testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationRun_ThrowableInOnLockSuccessRunnableAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
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
							() -> perform.performTryLockAndRun(lock, () -> {
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	@DisplayName("tryLock-timeout-duration-run: exception thrown inside onLockFail runnable")
	@TestFactory
	default Iterable<DynamicTest> testTryLockTimeoutDurationRun_ThrowableInsideOnFailLockRunnable() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableInsideOnFailLockRunnable(entry.getValue(), convertFromDuration(testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testTryLockTimeoutDurationRun_ThrowableInsideOnFailLockRunnable(entry.getValue(), convertFromTimeUnit(testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testTryLockTimeoutDurationRun_ThrowableInsideOnFailLockRunnable(@NonNull Supplier<? extends Throwable> supplier, @NonNull TimeoutLessPerform perform, @NonNull Duration testDuration) {
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
			Throwable actualThrowable = assertThrows(Throwable.class, () -> perform.performTryLockAndRun(lock, Assertions::fail, () -> {
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toMillis(), TimeUnit.MILLISECONDS)
							),
							lock.getActualEvents()
			);
		}
	}

	<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunLongAndTimeUnit(@Nullable Lock lock, long time, @Nullable TimeUnit unit, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2;

	<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunDuration(@Nullable Lock lock, @Nullable Duration duration, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2;

	/* Attempt to reduce boilerplate by reusing test code across Duration and Long+TimeUnit */
	@FunctionalInterface
	interface TimeoutLessPerform {
		<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2;
	}
}
