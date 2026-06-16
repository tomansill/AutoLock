package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableFunction;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

interface MultiTryLockTimeoutGetTest {

	static TimeoutLessPerformWithoutContext convertFromDurationGetWithoutContext(final MultiTryLockTimeoutGetTest tryLockTimeoutGetTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithoutContext() {
			@Override
			public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @NonNull Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
				return tryLockTimeoutGetTest.performTryLockAndGetDurationWithoutContext(locks, timeSourceStubber, duration, onLockSuccess, onLockFail);
			}
		};
	}

	static TimeoutLessPerformWithoutContext convertFromTimeUnitGetWithoutContext(final MultiTryLockTimeoutGetTest tryLockTimeoutGetTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithoutContext() {
			@Override
			public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @NonNull Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2 {
				return tryLockTimeoutGetTest.performTryLockAndGetLongAndTimeUnitWithoutContext(locks, timeSourceStubber, duration.toNanos(), TimeUnit.NANOSECONDS, onLockSuccess, onLockFail);
			}
		};
	}

	static TimeoutLessPerformWithContext convertFromDurationGetWithContext(final MultiTryLockTimeoutGetTest tryLockTimeoutGetTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithContext() {
			@Override
			public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @NonNull Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws InterruptedException, T1, T2 {
				return tryLockTimeoutGetTest.performTryLockAndGetDurationWithContext(locks, timeSourceStubber, duration, onLockSuccess, onLockFail);
			}
		};
	}

	static TimeoutLessPerformWithContext convertFromTimeUnitGetWithContext(final MultiTryLockTimeoutGetTest tryLockTimeoutGetTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithContext() {
			@Override
			public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @NonNull Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws InterruptedException, T1, T2 {
				return tryLockTimeoutGetTest.performTryLockAndGetLongAndTimeUnitWithContext(locks, timeSourceStubber, duration.toNanos(), TimeUnit.NANOSECONDS, onLockSuccess, onLockFail);
			}
		};
	}
/*
	@DisplayName("multi-tryLock-timeout-duration-get: with null lock")
	@Test
	default void testMultiTryLockTimeoutDurationGet_NullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithoutContext(null, Duration.ZERO, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("multi-tryLock-timeout-duration-get: with null duration")
	@Test
	default void testMultiTryLockTimeoutDurationGet_NullDuration() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithoutContext(lock, null, Assertions::fail, Assertions::fail));
			assertEquals("timeout must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get: with negative duration")
	@Test
	default void testMultiTryLockTimeoutDurationGet_NegativeDuration() {
		try (StubbedLock lock = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndGetDurationWithoutContext(lock, Duration.ofSeconds(-1), Assertions::fail, Assertions::fail));
			assertEquals("timeout must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get: with null onLockSuccess supplier")
	@Test
	default void testMultiTryLockTimeoutDurationGet_NullOnLockSuccessSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithoutContext(lock, Duration.ZERO, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get: with null onLockFail supplier")
	@Test
	default void testMultiTryLockTimeoutDurationGet_NullOnLockFailSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithoutContext(lock, Duration.ZERO, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("multi-tryLock-timeout-long/unit-get: with null lock")
	@Test
	default void testMultiTryLockTimeoutLongUnitGet_NullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithoutContext(null, 0, TimeUnit.NANOSECONDS, Assertions::fail, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("multi-tryLock-timeout-long/unit-get: with null TimeUnit")
	@Test
	default void testMultiTryLockTimeoutLongUnitGet_NullTimeUnit() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithoutContext(lock, 0, null, Assertions::fail, Assertions::fail));
			assertEquals("unit must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-long/unit-get: with negative time")
	@Test
	default void testMultiTryLockTimeoutLongUnitGet_NegativeTime() {
		try (StubbedLock lock = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndGetLongAndTimeUnitWithoutContext(lock, -1, TimeUnit.NANOSECONDS, Assertions::fail, Assertions::fail));
			assertEquals("time must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-long/unit-get: with null onLockSuccess supplier")
	@Test
	default void testMultiTryLockTimeoutLongUnitGet_NullOnLockSuccessSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithoutContext(lock, 0, TimeUnit.NANOSECONDS, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}

	@DisplayName("multi-tryLock-timeout-long/unit-get: with null onLockFail supplier")
	@Test
	default void testMultiTryLockTimeoutLongUnitGet_NullOnLockFailSupplier() {
		try (StubbedLock lock = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithoutContext(lock, 0, TimeUnit.NANOSECONDS, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
			assertEquals(Collections.emptyList(), lock.getActualEvents());
		}
	}*/

	@DisplayName("multi-tryLock-timeout-get: successful lock with 4 locks (max)")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Success4() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ofMinutes(1), Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Success4(entry.getValue(), convertFromDurationGetWithoutContext(this, testDuration), null, testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Success4(entry.getValue(), convertFromTimeUnitGetWithoutContext(this, testDuration), null, testDuration)));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: successful lock with 4 locks (max)")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Success4Ctx() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ofMinutes(1), Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Success4(entry.getValue(), null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Success4(entry.getValue(), null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_Success4(@NonNull Supplier<Object> objectSupplier, MultiTryLockTimeoutGetTest.@Nullable TimeoutLessPerformWithoutContext noCtx, MultiTryLockTimeoutGetTest.TimeoutLessPerformWithContext withCtx, @NonNull Duration testDuration) throws Throwable {
		Random random = new Random(getSeed(3).hashCode());
		try (StubbedTimeSource timeSource = new StubbedTimeSource(); StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			Duration budget = testDuration.dividedBy(4);
			Duration lock1Duration = generateDuration(random, Duration.ZERO, budget);
			Duration lock2Duration = generateDuration(random, Duration.ZERO, budget);
			Duration lock3Duration = generateDuration(random, Duration.ZERO, budget);
			{
				long workingTime = random.nextInt(50000) + 1000;
				timeSource.insertTime(workingTime); // Root
				timeSource.insertTime(workingTime); // Assume instant
				workingTime += lock1Duration.toNanos();
				timeSource.insertTime(workingTime);
				workingTime += lock2Duration.toNanos();
				timeSource.insertTime(workingTime);
				workingTime += lock3Duration.toNanos();
				timeSource.insertTime(workingTime);
			}
			lock1.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.minus(lock1Duration).toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock3.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.minus(lock1Duration).minus(lock2Duration).toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock4.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.minus(lock1Duration).minus(lock2Duration).minus(lock3Duration).toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Object expectedObject = objectSupplier.get();
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			ThrowableSupplier<Object, ?> onLockSuccess = () -> {
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
				return expectedObject;
			};
			Object actualObject = noCtx != null ? noCtx.performTryLockAndGet(locks, () -> timeSource, onLockSuccess, Assertions::fail) : withCtx.performTryLockAndGet(locks, () -> timeSource, onLockSuccess, ctx -> Assertions.fail());
			assertSame(expectedObject, actualObject);
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.addAll(lock3.getActualEvents());
			finalEvents.addAll(lock4.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), NANOSECONDS),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.minus(lock1Duration).toNanos(), NANOSECONDS),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.minus(lock1Duration).minus(lock2Duration).toNanos(), NANOSECONDS),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.minus(lock1Duration).minus(lock2Duration).minus(lock3Duration).toNanos(), NANOSECONDS),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}


	@DisplayName("multi-tryLock-timeout-get-ctx: successful lock with 2 locks (min)")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Success2Ctx() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ofMinutes(1), Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Success2(entry.getValue(), null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Success2(entry.getValue(), null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_Success2(@NonNull Supplier<Object> objectSupplier, MultiTryLockTimeoutGetTest.@Nullable TimeoutLessPerformWithoutContext noCtx, MultiTryLockTimeoutGetTest.TimeoutLessPerformWithContext withCtx, @NonNull Duration testDuration) throws Throwable {
		Random random = new Random(getSeed(3).hashCode());
		try (StubbedTimeSource timeSource = new StubbedTimeSource(); StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			Duration budget = testDuration.dividedBy(4);
			Duration lock1Duration = generateDuration(random, Duration.ZERO, budget);
			{
				long workingTime = random.nextInt(50000) + 1000;
				timeSource.insertTime(workingTime); // Root
				timeSource.insertTime(workingTime); // Assume instant
				workingTime += lock1Duration.toNanos();
				timeSource.insertTime(workingTime);
			}
			lock1.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.minus(lock1Duration).toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});

			Object expectedObject = objectSupplier.get();
			Lock[] locks = new Lock[]{lock1, lock2};
			ThrowableSupplier<Object, ?> onLockSuccess = () -> {
				assertSame(currentThread, Thread.currentThread());
				lock2.setOnUnlock(() -> {
					assertSame(currentThread, Thread.currentThread());
					lock1.setOnUnlock(() -> assertSame(currentThread, Thread.currentThread()));
				});
				return expectedObject;
			};
			Object actualObject = noCtx != null ? noCtx.performTryLockAndGet(locks, () -> timeSource, onLockSuccess, Assertions::fail) : withCtx.performTryLockAndGet(locks, () -> timeSource, onLockSuccess, ctx -> Assertions.fail());
			assertSame(expectedObject, actualObject);
			List<StubbedLock.CallEvent> finalEvents = new ArrayList<>(lock1.getActualEvents());
			finalEvents.addAll(lock2.getActualEvents());
			finalEvents.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), NANOSECONDS),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.minus(lock1Duration).toNanos(), NANOSECONDS),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), finalEvents);
		}
	}

	@DisplayName("multi-tryLock-timeout-get: failed lock on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed1st() {
		return testMultiTryLockTimeoutDurationGet_Failed(0, false);
	}

	@DisplayName("multi-tryLock-timeout-get: failed lock on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed2nd() {
		return testMultiTryLockTimeoutDurationGet_Failed(1, false);
	}

	@DisplayName("multi-tryLock-timeout-get: failed lock on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed3rd() {
		return testMultiTryLockTimeoutDurationGet_Failed(2, false);
	}

	@DisplayName("multi-tryLock-timeout-get: failed lock on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed4th() {
		return testMultiTryLockTimeoutDurationGet_Failed(3, false);
	}

	@DisplayName("multi-tryLock-timeout-get: failed lock on 2nd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed2ndBudget() {
		return testMultiTryLockTimeoutDurationGet_Failed(1, true);
	}

	@DisplayName("multi-tryLock-timeout-get: failed lock on 3rd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed3rdBudget() {
		return testMultiTryLockTimeoutDurationGet_Failed(2, true);
	}

	@DisplayName("multi-tryLock-timeout-get: failed lock on 4th lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed4thBudget() {
		return testMultiTryLockTimeoutDurationGet_Failed(3, true);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed(int lockFailPosition, boolean budgetRelated) {
		Random random = new Random(getSeed(1).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Failed(entry.getValue(), budgetRelated, lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null, testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Failed(entry.getValue(), budgetRelated, lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null, testDuration)));

		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed1stCtx() {
		return testMultiTryLockTimeoutDurationGet_FailedCtx(0, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed2ndCtx() {
		return testMultiTryLockTimeoutDurationGet_FailedCtx(1, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed3rdCtx() {
		return testMultiTryLockTimeoutDurationGet_FailedCtx(2, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed4thCtx() {
		return testMultiTryLockTimeoutDurationGet_FailedCtx(3, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 2nd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed2ndBudgetCtx() {
		return testMultiTryLockTimeoutDurationGet_FailedCtx(1, true);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 3rd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed3rdBudgetCtx() {
		return testMultiTryLockTimeoutDurationGet_FailedCtx(2, true);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 4th lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_Failed4thBudgetCtx() {
		return testMultiTryLockTimeoutDurationGet_FailedCtx(3, true);
	}


	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_FailedCtx(int lockFailPosition, boolean budgetRelated) {
		Random random = new Random(getSeed(1).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Failed(entry.getValue(), budgetRelated, lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_Failed(entry.getValue(), budgetRelated, lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration)));

		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_Failed(
					@NonNull Supplier<Object> objectSupplier,
					boolean failDueToBudget,
					int lockFailPosition,
					@Nullable TimeoutLessPerformWithoutContext noCtx,
					TimeoutLessPerformWithContext withContext,
					@NonNull Duration testDuration
	) throws InterruptedException {
		Random random = new Random(getSeed(1).hashCode());
		try (
						StubbedTimeSource timeSource = new StubbedTimeSource();
						StubbedLock lock1 = new StubbedLock();
						StubbedLock lock2 = new StubbedLock();
						StubbedLock lock3 = new StubbedLock();
						StubbedLock lock4 = new StubbedLock()
		) {
			AtomicBoolean failedSupplierReached = new AtomicBoolean(false);
			Thread currentThread = Thread.currentThread();
			Duration budget = testDuration.dividedBy(4);
			Duration lock1Duration = lockFailPosition == 1 && failDueToBudget ? generateDuration(random, testDuration, testDuration.plus(testDuration)) : generateDuration(random, Duration.ZERO, budget);
			Duration lock2Duration = lockFailPosition == 2 && failDueToBudget ? generateDuration(random, testDuration, testDuration.plus(testDuration)) : generateDuration(random, Duration.ZERO, budget);
			Duration lock3Duration = lockFailPosition == 3 && failDueToBudget ? generateDuration(random, testDuration, testDuration.plus(testDuration)) : generateDuration(random, Duration.ZERO, budget);
			boolean lock2Call = false;
			boolean lock3Call = false;
			boolean lock4Call = false;
			{
				long workingTime = random.nextInt(50000) + 1000;
				timeSource.insertTime(workingTime); // Root
				timeSource.insertTime(workingTime); // Assume instant
				if (lockFailPosition > 0) {
					workingTime += lock1Duration.toNanos();
					timeSource.insertTime(workingTime);
					if (lockFailPosition > 1) {
						workingTime += lock2Duration.toNanos();
						timeSource.insertTime(workingTime);
						if (lockFailPosition > 2) {
							workingTime += lock3Duration.toNanos();
							timeSource.insertTime(workingTime);
						}
					}
				}
			}
			if (lockFailPosition == 0) {
				lock1.setOnTryLockTimeout((time, unit) -> {
					assertEquals(testDuration.toNanos(), time);
					assertEquals(NANOSECONDS, unit);
					assertSame(currentThread, Thread.currentThread());
					return false;
				});
			} else {
				lock1.setOnTryLockTimeout((time, unit) -> {
					assertEquals(testDuration.toNanos(), time);
					assertEquals(NANOSECONDS, unit);
					assertSame(currentThread, Thread.currentThread());
					lock1.setOnUnlock(() -> assertSame(currentThread, Thread.currentThread()));
					return true;
				});
				Duration expectedLock2Timeout = testDuration.minus(lock1Duration);
				if (lockFailPosition == 1) {
					if (!failDueToBudget) {
						lock2Call = true;
						lock2.setOnTryLockTimeout((time, unit) -> {
							assertEquals(expectedLock2Timeout.toNanos(), time);
							assertEquals(NANOSECONDS, unit);
							assertSame(currentThread, Thread.currentThread());
							return false;
						});
					}
				} else {
					lock2Call = true;
					lock2.setOnTryLockTimeout((time, unit) -> {
						assertEquals(expectedLock2Timeout.toNanos(), time);
						assertEquals(NANOSECONDS, unit);
						assertSame(currentThread, Thread.currentThread());
						lock2.setOnUnlock(() -> assertSame(currentThread, Thread.currentThread()));
						return true;
					});
					Duration expectedLock3Timeout = expectedLock2Timeout.minus(lock2Duration);
					if (lockFailPosition == 2) {
						if (!failDueToBudget) {
							lock3Call = true;
							lock3.setOnTryLockTimeout((time, unit) -> {
								assertEquals(expectedLock3Timeout.toNanos(), time);
								assertEquals(NANOSECONDS, unit);
								assertSame(currentThread, Thread.currentThread());
								return false;
							});
						}
					} else {
						lock3Call = true;
						lock3.setOnTryLockTimeout((time, unit) -> {
							assertEquals(expectedLock3Timeout.toNanos(), time);
							assertEquals(NANOSECONDS, unit);
							assertSame(currentThread, Thread.currentThread());
							lock3.setOnUnlock(() -> assertSame(currentThread, Thread.currentThread()));
							return true;
						});
						Duration expectedLock4Timeout = expectedLock3Timeout.minus(lock3Duration);
						if (!failDueToBudget) {
							lock4Call = true;
							lock4.setOnTryLockTimeout((time, unit) -> {
								assertEquals(expectedLock4Timeout.toNanos(), time);
								assertEquals(NANOSECONDS, unit);
								assertSame(currentThread, Thread.currentThread());
								return false;
							});
						}
					}
				}
			}
			Object expectedObject = objectSupplier.get();
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			AtomicReference<Instant> timestampOfOnLockFail = new AtomicReference<>();
			Supplier<Object> original = () -> {
				failedSupplierReached.set(true);
				timestampOfOnLockFail.set(Instant.now());
				return expectedObject;
			};
			AtomicReference<AutoLock.MultipleLocks.TryLockFailContext> contextRef = new AtomicReference<>();
			Object actualObject = noCtx != null ?
							noCtx.performTryLockAndGet(locks, () -> timeSource, Assertions::fail, original::get) :
							withContext.performTryLockAndGet(locks, () -> timeSource, Assertions::fail, ctx -> {
								contextRef.set(ctx);
								return original.get();
							});
			assertSame(expectedObject, actualObject);
			assertTrue(failedSupplierReached.get());
			AutoLock.MultipleLocks.TryLockFailContext context = contextRef.get();
			if (noCtx == null) {
				assertNotNull(context);
				assertEquals(lockFailPosition, context.getSequenceIndex());
				if (lockFailPosition == 0) assertEquals(lock1, context.getFailedLock());
				else if (lockFailPosition == 1) assertEquals(lock2, context.getFailedLock());
				else if (lockFailPosition == 2) assertEquals(lock3, context.getFailedLock());
				else if (lockFailPosition == 3) assertEquals(lock4, context.getFailedLock());
				else fail("what? " + lockFailPosition);
			} else {
				assertNull(context);
			}
			List<StubbedLock.CallEvent> expectedList = new ArrayList<>();
			expectedList.add(new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), NANOSECONDS));
			if (lockFailPosition >= 1) {
				if (lock2Call)
					expectedList.add(new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.minus(lock1Duration).toNanos(), NANOSECONDS));
				if (lockFailPosition >= 2) {
					if (lock3Call)
						expectedList.add(new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.minus(lock1Duration).minus(lock2Duration).toNanos(), NANOSECONDS));
					if (lockFailPosition >= 3) {
						if (lock4Call)
							expectedList.add(new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.minus(lock1Duration).minus(lock2Duration).minus(lock3Duration).toNanos(), NANOSECONDS));
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

	@DisplayName("multi-tryLock-timeout-get: exception thrown inside onSuccessLock supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), convertFromDurationGetWithoutContext(this, testDuration), null, testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), convertFromTimeUnitGetWithoutContext(this, testDuration), null, testDuration)));

		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: exception thrown inside onSuccessLock supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplierCtx() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration)));

		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_ThrowableInsideOnSuccessLockSupplier(
					@NonNull Supplier<? extends Throwable> supplier,
					@Nullable TimeoutLessPerformWithoutContext noCtx,
					TimeoutLessPerformWithContext withContext,
					@NonNull Duration testDuration
	) {
		try (
						StubbedTimeSource timeSource = new StubbedTimeSource();
						StubbedLock lock1 = new StubbedLock();
						StubbedLock lock2 = new StubbedLock();
						StubbedLock lock3 = new StubbedLock();
						StubbedLock lock4 = new StubbedLock()
		) {
			long epoch = 1L;
			timeSource.insertTime(epoch); // Assume instanteous lock
			timeSource.insertTime(epoch); // Assume instanteous lock
			timeSource.insertTime(epoch); // Assume instanteous lock
			timeSource.insertTime(epoch); // Assume instanteous lock
			timeSource.insertTime(epoch); // Assume instanteous lock
			AtomicInteger executionCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock1.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock3.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock4.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(NANOSECONDS, unit);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			ThrowableSupplier<Object, ?> onLockSuccess = () -> {
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
			};
			Throwable actualThrowable = assertThrows(Throwable.class, () -> {
				if (noCtx != null) {
					noCtx.performTryLockAndGet(locks, () -> timeSource, onLockSuccess, Assertions::fail);
				} else {
					withContext.performTryLockAndGet(locks, () -> timeSource, onLockSuccess, ctx -> Assertions.fail());
				}
			});
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, executionCount.get());
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(
							Arrays.asList(
											new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), NANOSECONDS),
											new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), NANOSECONDS),
											new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), NANOSECONDS),
											new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), NANOSECONDS),
											new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
											new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
											new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
											new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							actualList
			);
		}
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at tryLock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod1st() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(0);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at tryLock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod2nd() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(1);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at tryLock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod3rd() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(2);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at tryLock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod4th() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).values().stream().flatMap(supplier -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(supplier, lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(supplier, lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null)));
		}).collect(Collectors.toList());
	}


	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod1stCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethodCtx(0);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod2ndCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethodCtx(1);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod3rdCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethodCtx(2);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod4thCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethodCtx(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethodCtx(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).values().stream().flatMap(supplier -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(supplier, lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration))),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(supplier, lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration))));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_ThrowableAtTryLockMethod(
					@NonNull Supplier<? extends Throwable> supplier,
					int lockFailPosition,
					MultiTryLockTimeoutGetTest.@Nullable TimeoutLessPerformWithoutContext noCtx,
					MultiTryLockTimeoutGetTest.TimeoutLessPerformWithContext withCtx
	) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			AtomicLong lock1Time = new AtomicLong();
			AtomicLong lock2Time = new AtomicLong();
			AtomicLong lock3Time = new AtomicLong();
			AtomicLong lock4Time = new AtomicLong();
			lock1.setOnTryLockTimeout((time, unit) -> {
				// Skipping assertions for time/unit
				lock1Time.set(time);
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
				lock2.setOnTryLockTimeout((time, unit) -> {
					// Skipping assertions for time/unit
					lock2Time.set(time);
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
				lock3.setOnTryLockTimeout((time, unit) -> {
					// Skipping assertions for time/unit
					lock3Time.set(time);
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
				lock4.setOnTryLockTimeout((time, unit) -> {
					// Skipping assertions for time/unit
					lock4Time.set(time);
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
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			Throwable actualThrowable = assertThrows(Throwable.class, () -> {
				if (noCtx != null) noCtx.performTryLockAndGet(locks, null, Assertions::fail, Assertions::fail);
				else withCtx.performTryLockAndGet(locks, null, Assertions::fail, ctx -> Assertions.fail());
			});
			assertSame(throwableRef.get(), actualThrowable);
			List<StubbedLock.CallEvent> expectedList = new ArrayList<>();
			expectedList.add(new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock1Time.get(), NANOSECONDS));
			if (lockFailPosition >= 1) {
				expectedList.add(new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock2Time.get(), NANOSECONDS));
				if (lockFailPosition >= 2) {
					expectedList.add(new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock3Time.get(), NANOSECONDS));
					if (lockFailPosition == 3) {
						expectedList.add(new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock4Time.get(), NANOSECONDS));
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

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod1st() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(0);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod2nd() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(1);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod3rd() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(2);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown at unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod4th() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null)));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod1stCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethodCtx(0);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod2ndCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethodCtx(1);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod3rdCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethodCtx(2);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod4thCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethodCtx(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethodCtx(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration))),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration))));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_ThrowableAtUnlockMethod(
					@NonNull Supplier<? extends Throwable> supplier,
					int lockFailPosition,
					MultiTryLockTimeoutGetTest.@Nullable TimeoutLessPerformWithoutContext noCtx,
					MultiTryLockTimeoutGetTest.TimeoutLessPerformWithContext withCtx
	) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			AtomicLong lock1Time = new AtomicLong();
			AtomicLong lock2Time = new AtomicLong();
			AtomicLong lock3Time = new AtomicLong();
			AtomicLong lock4Time = new AtomicLong();
			lock1.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock1Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock2Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock3.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock3Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock4.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock4Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			ThrowableSupplier<Object, ?> onLockSuccess = () -> {
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
			};
			Throwable actualThrowable = assertThrows(Throwable.class, () -> {
				if (noCtx == null) withCtx.performTryLockAndGet(locks, null, onLockSuccess, ctx -> Assertions.fail());
				else noCtx.performTryLockAndGet(locks, null, onLockSuccess, Assertions::fail);
			});
			assertSame(throwableRef.get(), actualThrowable);
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock1Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock2Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock3Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock4Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), actualList);
		}
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod1st() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(0);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod2nd() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(1);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod3rd() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(2);
	}

	@DisplayName("multi-tryLock-timeout-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod4th() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().flatMap(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null)));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod1stCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(0);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod2ndCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(1);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod3rdCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(2);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod4thCtx() {
		return testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().flatMap(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration))),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration))));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(
					@NonNull Supplier<? extends Throwable> mainExceptionSupplier,
					@NonNull Supplier<? extends Throwable> supplier,
					int lockFailPosition,
					MultiTryLockTimeoutGetTest.@Nullable TimeoutLessPerformWithoutContext noCtx,
					MultiTryLockTimeoutGetTest.TimeoutLessPerformWithContext withCtx
	) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
			AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
			AtomicLong lock1Time = new AtomicLong();
			AtomicLong lock2Time = new AtomicLong();
			AtomicLong lock3Time = new AtomicLong();
			AtomicLong lock4Time = new AtomicLong();
			lock1.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock1Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock2.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock2Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock3.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock3Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			lock4.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock4Time.set(time);
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			ThrowableSupplier<Object, ?> onLockSuccess = () -> {
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
			};
			Throwable actualThrowable = assertThrows(
							Throwable.class,
							() -> {
								if (noCtx == null) withCtx.performTryLockAndGet(locks, null, onLockSuccess, ctx -> Assertions.fail());
								else noCtx.performTryLockAndGet(locks, null, onLockSuccess, Assertions::fail);
							});
			assertSame(mainThrowableRef.get(), actualThrowable);
			assertEquals(1, actualThrowable.getSuppressed().length);
			assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
			List<StubbedLock.CallEvent> actualList = new ArrayList<>(lock1.getActualEvents());
			actualList.addAll(lock2.getActualEvents());
			actualList.addAll(lock3.getActualEvents());
			actualList.addAll(lock4.getActualEvents());
			actualList.sort(Comparator.comparing(one -> one.timestamp));
			assertEquals(Arrays.asList(
							new StubbedLock.CallEvent(lock1, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock1Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock2, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock2Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock3, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock3Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock4, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, lock4Time.get(), NANOSECONDS),
							new StubbedLock.CallEvent(lock4, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock3, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock2, 1, currentThread, StubbedLock.Event.UNLOCK),
							new StubbedLock.CallEvent(lock1, 1, currentThread, StubbedLock.Event.UNLOCK)
			), actualList);
		}
	}
	/*

	@DisplayName("multi-tryLock-timeout-get: exception thrown inside onLockFail supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier(entry.getValue(), convertFromDurationGetWithoutContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier(entry.getValue(), convertFromTimeUnitGetWithoutContext(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationGet_ThrowableInsideOnFailLockSupplier(@NonNull Supplier<? extends Throwable> supplier, @NonNull MultiTryLockTimeoutGetTest.TimeoutLessPerformWithoutContext perform, @NonNull Duration testDuration) {
		try (StubbedLock lock = new StubbedLock()) {
			AtomicInteger lockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockTimeout((time, unit) -> {
				assertEquals(testDuration.toNanos(), time);
				assertEquals(TimeUnit.NANOSECONDS, unit);
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK_TIMEOUT, testDuration.toNanos(), TimeUnit.NANOSECONDS)
							),
							lock.getActualEvents()
			);
		}
	}*/

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetLongAndTimeUnitWithoutContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, long time, @Nullable TimeUnit unit, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2;

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetDurationWithoutContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable Duration duration, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2;

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetLongAndTimeUnitWithContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, long time, @Nullable TimeUnit unit, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws InterruptedException, T1, T2;

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGetDurationWithContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable Duration duration, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws InterruptedException, T1, T2;

	/* Attempt to reduce boilerplate by reusing test code across Duration and Long+TimeUnit */
	@FunctionalInterface
	interface TimeoutLessPerformWithoutContext {
		<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws InterruptedException, T1, T2;
	}

	/* Attempt to reduce boilerplate by reusing test code across Duration and Long+TimeUnit */
	@FunctionalInterface
	interface TimeoutLessPerformWithContext {
		<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableFunction<AutoLock.MultipleLocks.TryLockFailContext, Return, T2> onLockFail) throws InterruptedException, T1, T2;
	}

}
