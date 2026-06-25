package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableConsumer;
import com.ansill.autolock.ThrowableRunnable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

interface MultiTryLockTimeoutRunTest {

	static TimeoutLessPerformWithoutContext convertFromDurationGetWithoutContext(final MultiTryLockTimeoutRunTest tryLockTimeoutRunTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithoutContext() {
			@Override
			public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
				tryLockTimeoutRunTest.performTryLockAndRunDurationWithoutContext(locks, timeSourceStubber, duration, onLockSuccess, onLockFail);
			}
		};
	}

	static TimeoutLessPerformWithoutContext convertFromTimeUnitGetWithoutContext(final MultiTryLockTimeoutRunTest tryLockTimeoutRunTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithoutContext() {
			@Override
			public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
				tryLockTimeoutRunTest.performTryLockAndRunLongAndTimeUnitWithoutContext(locks, timeSourceStubber, duration.toNanos(), TimeUnit.NANOSECONDS, onLockSuccess, onLockFail);
			}
		};
	}

	static TimeoutLessPerformWithContext convertFromDurationGetWithContext(final MultiTryLockTimeoutRunTest tryLockTimeoutRunTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithContext() {
			@Override
			public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableConsumer<AutoLock.MultipleLocks.TryLockFailContext, T2> onLockFail) throws InterruptedException, T1, T2 {
				tryLockTimeoutRunTest.performTryLockAndGetDurationWithContext(locks, timeSourceStubber, duration, onLockSuccess, onLockFail);
			}
		};
	}

	static TimeoutLessPerformWithContext convertFromTimeUnitGetWithContext(final MultiTryLockTimeoutRunTest tryLockTimeoutRunTest, @NonNull Duration duration) {
		return new TimeoutLessPerformWithContext() {
			@Override
			public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableConsumer<AutoLock.MultipleLocks.TryLockFailContext, T2> onLockFail) throws InterruptedException, T1, T2 {
				tryLockTimeoutRunTest.performTryLockAndGetLongAndTimeUnitWithContext(locks, timeSourceStubber, duration.toNanos(), TimeUnit.NANOSECONDS, onLockSuccess, onLockFail);
			}
		};
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with 1st null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock1() {
		try (StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{null, lock2, lock3, lock4}, null, Duration.ZERO, Assertions::fail, Assertions::fail));
			assertEquals("lock1 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with 2nd null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock2() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{lock1, null, lock3, lock4}, null, Duration.ZERO, Assertions::fail, Assertions::fail));
			assertEquals("lock2 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with 3rd null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock3() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{lock1, lock2, null, lock4}, null, Duration.ZERO, Assertions::fail, Assertions::fail));
			assertEquals("lock3 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with 4th null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock4() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{lock1, lock2, lock3, null}, null, Duration.ZERO, Assertions::fail, Assertions::fail));
			assertEquals("lock4 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with 1st null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock1Ctx() {
		try (StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{null, lock2, lock3, lock4}, null, Duration.ZERO, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock1 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with 2nd null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock2Ctx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{lock1, null, lock3, lock4}, null, Duration.ZERO, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock2 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with 3rd null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock3Ctx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{lock1, lock2, null, lock4}, null, Duration.ZERO, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock3 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with 4th null lock")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullLock4Ctx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{lock1, lock2, lock3, null}, null, Duration.ZERO, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock4 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with 1st null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock1() {
		try (StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{null, lock2, lock3, lock4}, null, 0, MINUTES, Assertions::fail, Assertions::fail));
			assertEquals("lock1 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with 2nd null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock2() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{lock1, null, lock3, lock4}, null, 0, MINUTES, Assertions::fail, Assertions::fail));
			assertEquals("lock2 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with 3rd null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock3() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{lock1, lock2, null, lock4}, null, 0, MINUTES, Assertions::fail, Assertions::fail));
			assertEquals("lock3 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with 4th null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock4() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{lock1, lock2, lock3, null}, null, 0, MINUTES, Assertions::fail, Assertions::fail));
			assertEquals("lock4 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with 1st null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock1Ctx() {
		try (StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{null, lock2, lock3, lock4}, null, 0, MINUTES, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock1 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with 2nd null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock2Ctx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{lock1, null, lock3, lock4}, null, 0, MINUTES, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock2 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with 3rd null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock3Ctx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{lock1, lock2, null, lock4}, null, 0, MINUTES, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock3 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with 4th null lock")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullLock4Ctx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{lock1, lock2, lock3, null}, null, 0, MINUTES, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("lock4 must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with null duration")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullDuration() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, null, Assertions::fail, Assertions::fail));
			assertEquals("timeout must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with null duration")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullDurationCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, null, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("timeout must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with null unit")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullUnit() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, 0, null, Assertions::fail, Assertions::fail));
			assertEquals("unit must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with null unit")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullUnitCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, 0, null, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("unit must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with negative duration")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NegativeDuration() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, Duration.ofSeconds(-1), Assertions::fail, Assertions::fail));
			assertEquals("timeout must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with negative duration")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NegativeDurationCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, Duration.ofSeconds(-1), Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("timeout must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with negative time")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NegativeTime() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, -1, MINUTES, Assertions::fail, Assertions::fail));
			assertEquals("time must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with negative time")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NegativeTimeCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, -1, MINUTES, Assertions::fail, ctx -> Assertions.fail()));
			assertEquals("time must be non-negative", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with null onLockSuccess supplier")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullOnLockSuccessSupplier() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, Duration.ZERO, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with null onLockSuccess supplier")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullOnLockSuccessSupplierCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, Duration.ZERO, null, ctx -> Assertions.fail()));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with null onLockSuccess supplier")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullOnLockSuccessSupplier() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, 0, MINUTES, null, Assertions::fail));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with null onLockSuccess supplier")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullOnLockSuccessSupplierCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, 0, MINUTES, null, ctx -> Assertions.fail()));
			assertEquals("onLockSuccess must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-run: with null onLockFail supplier")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullOnLockFailSupplier() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunDurationWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, Duration.ZERO, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-duration-get-ctx: with null onLockFail supplier")
	@Test
	default void testMultiTryLockTimeoutDurationRun_NullOnLockFailSupplierCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetDurationWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, Duration.ZERO, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-run: with null onLockFail supplier")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullOnLockFailSupplier() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRunLongAndTimeUnitWithoutContext(new Lock[]{lock1, lock2, lock3, lock4}, null, 0, MINUTES, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-timeunit-get-ctx: with null onLockFail supplier")
	@Test
	default void testMultiTryLockTimeoutTimeUnitRun_NullOnLockFailSupplierCtx() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGetLongAndTimeUnitWithContext(new Lock[]{lock1, lock2, lock3, lock4}, null, 0, MINUTES, Assertions::fail, null));
			assertEquals("onLockFail must not be null", exception.getMessage());
		}
	}

	@DisplayName("multi-tryLock-timeout-run: successful lock with 4 locks (max)")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Success4() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ofMinutes(1), Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Success4(entry.getValue(), convertFromDurationGetWithoutContext(this, testDuration), null, testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Success4(entry.getValue(), convertFromTimeUnitGetWithoutContext(this, testDuration), null, testDuration)));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: successful lock with 4 locks (max)")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Success4Ctx() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ofMinutes(1), Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Success4(entry.getValue(), null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Success4(entry.getValue(), null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationRun_Success4(@NonNull Supplier<Object> objectSupplier, MultiTryLockTimeoutRunTest.@Nullable TimeoutLessPerformWithoutContext noCtx, MultiTryLockTimeoutRunTest.TimeoutLessPerformWithContext withCtx, @NonNull Duration testDuration) throws Throwable {
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
			ThrowableRunnable<?> onLockSuccess = () -> {
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
			};
			if (noCtx != null) {
				noCtx.performTryLockAndRun(locks, () -> timeSource, onLockSuccess, Assertions::fail);
			} else {
				withCtx.performTryLockAndRun(locks, () -> timeSource, onLockSuccess, ctx -> fail());
			}
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
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Success2Ctx() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ofMinutes(1), Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Success2(entry.getValue(), null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Success2(entry.getValue(), null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration)));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationRun_Success2(@NonNull Supplier<Object> objectSupplier, MultiTryLockTimeoutRunTest.@Nullable TimeoutLessPerformWithoutContext noCtx, MultiTryLockTimeoutRunTest.TimeoutLessPerformWithContext withCtx, @NonNull Duration testDuration) throws Throwable {
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

			Lock[] locks = new Lock[]{lock1, lock2};
			ThrowableRunnable<?> onLockSuccess = () -> {
				assertSame(currentThread, Thread.currentThread());
				lock2.setOnUnlock(() -> {
					assertSame(currentThread, Thread.currentThread());
					lock1.setOnUnlock(() -> assertSame(currentThread, Thread.currentThread()));
				});
			};
			if (noCtx != null) {
				noCtx.performTryLockAndRun(locks, () -> timeSource, onLockSuccess, Assertions::fail);
			} else {
				withCtx.performTryLockAndRun(locks, () -> timeSource, onLockSuccess, ctx -> fail());
			}
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

	@DisplayName("multi-tryLock-timeout-run: failed lock on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed1st() {
		return testMultiTryLockTimeoutDurationRun_Failed(0, false);
	}

	@DisplayName("multi-tryLock-timeout-run: failed lock on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed2nd() {
		return testMultiTryLockTimeoutDurationRun_Failed(1, false);
	}

	@DisplayName("multi-tryLock-timeout-run: failed lock on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed3rd() {
		return testMultiTryLockTimeoutDurationRun_Failed(2, false);
	}

	@DisplayName("multi-tryLock-timeout-run: failed lock on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed4th() {
		return testMultiTryLockTimeoutDurationRun_Failed(3, false);
	}

	@DisplayName("multi-tryLock-timeout-run: failed lock on 2nd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed2ndBudget() {
		return testMultiTryLockTimeoutDurationRun_Failed(1, true);
	}

	@DisplayName("multi-tryLock-timeout-run: failed lock on 3rd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed3rdBudget() {
		return testMultiTryLockTimeoutDurationRun_Failed(2, true);
	}

	@DisplayName("multi-tryLock-timeout-run: failed lock on 4th lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed4thBudget() {
		return testMultiTryLockTimeoutDurationRun_Failed(3, true);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed(int lockFailPosition, boolean budgetRelated) {
		Random random = new Random(getSeed(1).hashCode());
		return getRandomObjects(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Failed(budgetRelated, lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null, testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Failed(budgetRelated, lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null, testDuration)));

		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed1stCtx() {
		return testMultiTryLockTimeoutDurationRun_FailedCtx(0, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed2ndCtx() {
		return testMultiTryLockTimeoutDurationRun_FailedCtx(1, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed3rdCtx() {
		return testMultiTryLockTimeoutDurationRun_FailedCtx(2, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed4thCtx() {
		return testMultiTryLockTimeoutDurationRun_FailedCtx(3, false);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 2nd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed2ndBudgetCtx() {
		return testMultiTryLockTimeoutDurationRun_FailedCtx(1, true);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 3rd lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed3rdBudgetCtx() {
		return testMultiTryLockTimeoutDurationRun_FailedCtx(2, true);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: failed lock on 4th lock due to no budget")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_Failed4thBudgetCtx() {
		return testMultiTryLockTimeoutDurationRun_FailedCtx(3, true);
	}


	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_FailedCtx(int lockFailPosition, boolean budgetRelated) {
		Random random = new Random(getSeed(1).hashCode());
		Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
		return Arrays.asList(
						DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Failed(budgetRelated, lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
						DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_Failed(budgetRelated, lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration))
		);
	}

	default void testMultiTryLockTimeoutDurationRun_Failed(
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
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			AtomicReference<Instant> timestampOfOnLockFail = new AtomicReference<>();
			Runnable original = () -> {
				failedSupplierReached.set(true);
				timestampOfOnLockFail.set(Instant.now());
			};
			AtomicReference<AutoLock.MultipleLocks.TryLockFailContext> contextRef = new AtomicReference<>();
			if (null != noCtx)
				noCtx.performTryLockAndRun(locks, () -> timeSource, Assertions::fail, original::run);
			else {
				withContext.performTryLockAndRun(locks, () -> timeSource, Assertions::fail, ctx -> {
					contextRef.set(ctx);
					original.run();
				});
			}
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

	@DisplayName("multi-tryLock-timeout-run: exception thrown inside onSuccessLock supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockSupplier() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), convertFromDurationGetWithoutContext(this, testDuration), null, testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), convertFromTimeUnitGetWithoutContext(this, testDuration), null, testDuration)));

		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: exception thrown inside onSuccessLock supplier")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockSupplierCtx() {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), null, convertFromDurationGetWithContext(this, testDuration), testDuration)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockSupplier(entry.getValue(), null, convertFromTimeUnitGetWithContext(this, testDuration), testDuration)));

		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationRun_ThrowableInsideOnSuccessLockSupplier(
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
			ThrowableRunnable<?> onLockSuccess = () -> {
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
					noCtx.performTryLockAndRun(locks, () -> timeSource, onLockSuccess, Assertions::fail);
				} else {
					withContext.performTryLockAndRun(locks, () -> timeSource, onLockSuccess, ctx -> Assertions.fail());
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

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at tryLock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod1st() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(0);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at tryLock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod2nd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(1);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at tryLock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod3rd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(2);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at tryLock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod4th() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).values().stream().flatMap(supplier -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(supplier, lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(supplier, lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null)));
		}).collect(Collectors.toList());
	}


	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod1stCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethodCtx(0);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod2ndCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethodCtx(1);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod3rdCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethodCtx(2);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at tryLock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod4thCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethodCtx(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethodCtx(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).values().stream().flatMap(supplier -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(supplier, lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration))),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(supplier, lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration))));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationRun_ThrowableAtTryLockMethod(
					@NonNull Supplier<? extends Throwable> supplier,
					int lockFailPosition,
					MultiTryLockTimeoutRunTest.@Nullable TimeoutLessPerformWithoutContext noCtx,
					MultiTryLockTimeoutRunTest.TimeoutLessPerformWithContext withCtx
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
				if (noCtx != null) noCtx.performTryLockAndRun(locks, null, Assertions::fail, Assertions::fail);
				else withCtx.performTryLockAndRun(locks, null, Assertions::fail, ctx -> Assertions.fail());
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

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod1st() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(0);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod2nd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(1);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod3rd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(2);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown at unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod4th() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null)));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod1stCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethodCtx(0);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod2ndCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethodCtx(1);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod3rdCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethodCtx(2);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown at unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod4thCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethodCtx(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethodCtx(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomUncheckeds(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration))),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(entry.getValue(), lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration))));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationRun_ThrowableAtUnlockMethod(
					@NonNull Supplier<? extends Throwable> supplier,
					int lockFailPosition,
					MultiTryLockTimeoutRunTest.@Nullable TimeoutLessPerformWithoutContext noCtx,
					MultiTryLockTimeoutRunTest.TimeoutLessPerformWithContext withCtx
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
			ThrowableRunnable<?> onLockSuccess = () -> {
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
			};
			Throwable actualThrowable = assertThrows(Throwable.class, () -> {
				if (noCtx == null) withCtx.performTryLockAndRun(locks, null, onLockSuccess, ctx -> Assertions.fail());
				else noCtx.performTryLockAndRun(locks, null, onLockSuccess, Assertions::fail);
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

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod1st() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(0);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod2nd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(1);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod3rd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(2);
	}

	@DisplayName("multi-tryLock-timeout-run: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod4th() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().flatMap(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null)));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod1stCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(0);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod2ndCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(1);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod3rdCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(2);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock() at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod4thCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethodCtx(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
		return throwableMap.entrySet().stream().flatMap(entry -> {
			Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
			List<String> keys = new ArrayList<>(innerRandomMap.keySet());
			Collections.shuffle(keys, random);
			Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration))),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue(), lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration))));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationRun_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(
					@NonNull Supplier<? extends Throwable> mainExceptionSupplier,
					@NonNull Supplier<? extends Throwable> supplier,
					int lockFailPosition,
					MultiTryLockTimeoutRunTest.@Nullable TimeoutLessPerformWithoutContext noCtx,
					MultiTryLockTimeoutRunTest.TimeoutLessPerformWithContext withCtx
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
			ThrowableRunnable<?> onLockSuccess = () -> {
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
								if (noCtx == null) withCtx.performTryLockAndRun(locks, null, onLockSuccess, ctx -> Assertions.fail());
								else noCtx.performTryLockAndRun(locks, null, onLockSuccess, Assertions::fail);
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

	@DisplayName("multi-tryLock-timeout-run: exception thrown inside onLockFail supplier at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier1st() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(0);
	}

	@DisplayName("multi-tryLock-timeout-run: exception thrown inside onLockFail supplier at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier2nd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(1);
	}

	@DisplayName("multi-tryLock-timeout-run: exception thrown inside onLockFail supplier at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier3rd() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(2);
	}

	@DisplayName("multi-tryLock-timeout-run: exception thrown inside onLockFail supplier at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier4th() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(entry.getValue(), lockFailPosition, convertFromDurationGetWithoutContext(this, testDuration), null)),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(entry.getValue(), lockFailPosition, convertFromTimeUnitGetWithoutContext(this, testDuration), null)));
		}).collect(Collectors.toList());
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: exception thrown inside onLockFail supplier at 1st lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier1stCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplierCtx(0);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: exception thrown inside onLockFail supplier at 2nd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier2ndCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplierCtx(1);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: exception thrown inside onLockFail supplier at 3rd lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier3rdCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplierCtx(2);
	}

	@DisplayName("multi-tryLock-timeout-get-ctx: exception thrown inside onLockFail supplier at 4th lock")
	@TestFactory
	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier4thCtx() {
		return testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplierCtx(3);
	}

	default Iterable<DynamicTest> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplierCtx(int lockFailPosition) {
		Random random = new Random(getSeed(0).hashCode());
		return getRandomThrowables(random).entrySet().stream().flatMap(entry -> {
			Duration testDuration = generateDuration(random, Duration.ZERO, Duration.ofMinutes(60));
			return Stream.of(
							DynamicTest.dynamicTest("duration " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(entry.getValue(), lockFailPosition, null, convertFromDurationGetWithContext(this, testDuration))),
							DynamicTest.dynamicTest("time/unit " + testDuration, () -> testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(entry.getValue(), lockFailPosition, null, convertFromTimeUnitGetWithContext(this, testDuration))));
		}).collect(Collectors.toList());
	}

	default void testMultiTryLockTimeoutDurationRun_ThrowableInsideOnFailLockSupplier(
					@NonNull Supplier<? extends Throwable> supplier,
					int lockFailPosition,
					MultiTryLockTimeoutRunTest.@Nullable TimeoutLessPerformWithoutContext noCtx,
					TimeoutLessPerformWithContext withCtx
	) {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock(); StubbedLock lock3 = new StubbedLock(); StubbedLock lock4 = new StubbedLock()) {
			Thread currentThread = Thread.currentThread();
			AtomicLong lock1Time = new AtomicLong();
			AtomicLong lock2Time = new AtomicLong();
			AtomicLong lock3Time = new AtomicLong();
			AtomicLong lock4Time = new AtomicLong();
			lock1.setOnTryLockTimeout((time, unit) -> {
				// Skip assertions on time/unit
				lock1Time.set(time);
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
				lock2.setOnTryLockTimeout((time, unit) -> {
					// Skip assertions on time/unit
					lock2Time.set(time);
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
				lock3.setOnTryLockTimeout((time, unit) -> {
					// Skip assertions on time/unit
					lock3Time.set(time);
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
				lock4.setOnTryLockTimeout((time, unit) -> {
					// Skip assertions on time/unit
					lock4Time.set(time);
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
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			AtomicReference<AutoLock.MultipleLocks.TryLockFailContext> contextRef = new AtomicReference<>();
			AtomicReference<Instant> timestampOfOnLockFail = new AtomicReference<>();
			Lock[] locks = new Lock[]{lock1, lock2, lock3, lock4};
			ThrowableRunnable<?> original = () -> {
				timestampOfOnLockFail.set(Instant.now());
				try {
					throw supplier.get();
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			};
			Throwable actualThrowable = assertThrows(Throwable.class, () -> {
				if (noCtx == null) {
					withCtx.performTryLockAndRun(locks, null, Assertions::fail, ctx -> {
						contextRef.set(ctx);
						original.run();
					});
				} else {
					noCtx.performTryLockAndRun(locks, null, Assertions::fail, original::run);
				}
			});
			assertSame(throwableRef.get(), actualThrowable);
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

			// Assert that onLockFail is run AFTER all unlocks, not between
			Instant instantOfOnLockFail = timestampOfOnLockFail.get();
			assertNotNull(instantOfOnLockFail);
			assertTrue(instantOfOnLockFail.isAfter(actualList.get(actualList.size() - 1).timestamp));
		}
	}

	<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunLongAndTimeUnitWithoutContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, long time, @Nullable TimeUnit unit, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2;

	<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunDurationWithoutContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable Duration duration, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2;

	<T1 extends Throwable, T2 extends Throwable> void performTryLockAndGetLongAndTimeUnitWithContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, long time, @Nullable TimeUnit unit, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableConsumer<AutoLock.MultipleLocks.TryLockFailContext, T2> onLockFail) throws InterruptedException, T1, T2;

	<T1 extends Throwable, T2 extends Throwable> void performTryLockAndGetDurationWithContext(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable Duration duration, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableConsumer<AutoLock.MultipleLocks.TryLockFailContext, T2> onLockFail) throws InterruptedException, T1, T2;

	/* Attempt to reduce boilerplate by reusing test code across Duration and Long+TimeUnit */
	@FunctionalInterface
	interface TimeoutLessPerformWithoutContext {
		<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2;
	}

	/* Attempt to reduce boilerplate by reusing test code across Duration and Long+TimeUnit */
	@FunctionalInterface
	interface TimeoutLessPerformWithContext {
		<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock[] locks, @Nullable Supplier<LongSupplier> timeSourceStubber, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableConsumer<AutoLock.MultipleLocks.TryLockFailContext, T2> onLockFail) throws InterruptedException, T1, T2;
	}

}
