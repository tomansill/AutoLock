package test;

import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

@DisplayName("AutoLock Test")
abstract class AutoLockTest {

	@NonNull
	static Map<String, Supplier<Object>> getRandomObjects(@NonNull Random random1) {
		Map<String, Supplier<Object>> returnObj = new HashMap<>();
		{
			Random random = new Random(random1.nextLong());
			returnObj.put("string", () -> generateAlphanumericString(random, 3, 32));
		}
		{
			Random random = new Random(random1.nextLong());
			returnObj.put("list of random strings", () -> IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toSet()));
		}
		{
			Random random = new Random(random1.nextLong());
			returnObj.put("map of random strings", () -> IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toMap(i -> i, i -> IntStream.range(0, random.nextInt(15) + 2).mapToObj(j -> generateAlphanumericString(random, 3, 20)).collect(Collectors.toSet()))));
		}
		{
			Random random = new Random(random1.nextLong());
			returnObj.put("big decimal", () -> new BigDecimal(random.nextDouble()));
		}
		{
			Random random = new Random(random1.nextLong());
			returnObj.put("int", random::nextInt);
		}
		return returnObj;
	}

	@Deprecated
	@NonNull
	static Object getRandomObject(@NonNull Random random) {
		switch (random.nextInt(5)) {
			case 0:
				return TestUtility.generateAlphanumericString(random, 3, 32);
			case 1:
				return IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toSet());
			case 2:
				return IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toMap(i -> i, i -> IntStream.range(0, random.nextInt(15) + 2).mapToObj(j -> generateAlphanumericString(random, 3, 20)).collect(Collectors.toSet())));
			case 3:
				return new BigDecimal(random.nextDouble());
			default:
				return random.nextInt();
		}
	}

	static void throwRandomThrowable(@NonNull Random random) throws Throwable {
		switch (random.nextInt(7)) {
			case 0:
				throw new RuntimeException("fake exception" + generateAlphanumericString(random, 3, 32));
			case 1:
				throw new Exception("fake exception" + generateAlphanumericString(random, 3, 32));
			case 2:
				throw new AssertionError("fake exception" + generateAlphanumericString(random, 3, 32));
			case 3:
				throw new SecurityException("fake exception" + generateAlphanumericString(random, 3, 32));
			case 4:
				throw new InterruptedException("fake exception" + generateAlphanumericString(random, 3, 32));
			case 5:
				throw new Throwable("fake exception" + generateAlphanumericString(random, 3, 32));
			default:
				throw new Error("fake exception" + generateAlphanumericString(random, 3, 32));
		}
	}

	interface LockRunTest {

		@DisplayName("test with null lock on lock run")
		@Test
		default void testNullLockOnLockRun() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndRun(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("test with null runnable on lock run")
		@Test
		default void testNullRunnableOnLockRun() {
			StubbedLock stubbedLock = new StubbedLock();
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndRun(stubbedLock, null));
			assertEquals("runnable must not be null", exception.getMessage());
		}

		@DisplayName("test successful lock (run)")
		@Test
		default void testSuccessfulLockRunnable() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			performLockAndRun(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
			});
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		@DisplayName("test lock with exception (run)")
		@TestFactory
		default Iterable<DynamicTest> testLockRunnableWithException() {
			return generateTests(5, this::testLockRunnableWithException);
		}

		default void testLockRunnableWithException(@NonNull Random random) {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			AtomicReference<Object> throwableRef = new AtomicReference<>();
			Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockAndRun(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				try {
					throwRandomThrowable(random);
				} catch (Throwable throwable) {
					throwableRef.set(throwable);
					throw throwable;
				}
			}));
			assertSame(throwableRef.get(), actualThrowable);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		<T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T;
	}

	interface LockGetTest {

		@DisplayName("test with null lock on lock get")
		@Test
		default void testNullLockOnLockGet() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("test with null supplier on lock get")
		@Test
		default void testNullRunnableOnLockGet() {
			StubbedLock stubbedLock = new StubbedLock();
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(stubbedLock, null));
			assertEquals("supplier must not be null", exception.getMessage());
		}

		@DisplayName("test successful lock (get)")
		@TestFactory
		default Iterable<DynamicTest> testSuccessfulLockGet() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testSuccessfulLockGet(entry.getValue()))).collect(Collectors.toList());
		}

		default void testSuccessfulLockGet(@NonNull Supplier<Object> objectSupplier) {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			Object expected = objectSupplier.get();
			Object actual = performLockAndGet(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				return expected;
			});
			assertSame(expected, actual);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		@DisplayName("test successful lock (get) with null return value")
		@Test
		default void testSuccessfulLockGetNullReturn() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLock(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
			});
			Object actual = performLockAndGet(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				return null;
			});
			assertNull(actual);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		<Return, T extends Throwable> Return performLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T;
	}

	interface LockInterruptiblyRunTest {

		@DisplayName("test with null lock on lock interruptibly run")
		@Test
		default void testNullLockOnLockInterruptiblyRun() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("test with null runnable on lock interruptibly run")
		@Test
		default void testNullRunnableOnLockInterruptiblyRun() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(new StubbedLock(), null));
			assertEquals("runnable must not be null", exception.getMessage());
		}

		@DisplayName("test successful lockInterruptibly (run)")
		@Test
		default void testSuccessfulLockInterruptiblyAndRun() throws InterruptedException {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLockInterruptibly(() -> {
				lockCount.incrementAndGet();
				assertEquals(currentThread, Thread.currentThread());
			});
			performLockInterruptiblyAndRun(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertEquals(currentThread, Thread.currentThread());
				});
			});
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		@DisplayName("test lockInterruptibly with interrupt (run)")
		@Test
		default void testLockInterruptiblyAndRunWithInterrupt() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicReference<InterruptedException> thrownInterruptedException = new AtomicReference<>();
			lock.setOnLockInterruptibly(() -> {
				lockCount.incrementAndGet();
				InterruptedException ie = new InterruptedException();
				thrownInterruptedException.set(ie);
				throw ie;
			});
			InterruptedException ie = assertThrows(InterruptedException.class, () -> performLockInterruptiblyAndRun(lock, Assertions::fail));
			assertEquals(1, lockCount.get());
			assertSame(thrownInterruptedException.get(), ie);
		}

		void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable Runnable runnable) throws InterruptedException;
	}

	interface LockInterruptiblyGetTest {

		@DisplayName("test with null lock on lock interruptibly get")
		@Test
		default void testNullLockOnLockInterruptiblyGet() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndGet(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("test with null lock on lock interruptibly get")
		@Test
		default void testNullSupplierOnLockInterruptiblyGet() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndGet(new StubbedLock(), null));
			assertEquals("supplier must not be null", exception.getMessage());
		}

		@DisplayName("test successful lockInterruptibly (get)")
		@TestFactory
		default Iterable<DynamicTest> testSuccessfulLockInterruptiblyAndGet() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testSuccessfulLockInterruptiblyAndGet(entry.getValue()))).collect(Collectors.toList());
		}

		@DisplayName("test lockInterruptibly with interrupt (get)")
		@Test
		default void testLockInterruptiblyAndGetWithInterrupt() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicReference<InterruptedException> thrownInterruptedException = new AtomicReference<>();
			lock.setOnLockInterruptibly(() -> {
				lockCount.incrementAndGet();
				InterruptedException ie = new InterruptedException();
				thrownInterruptedException.set(ie);
				throw ie;
			});
			InterruptedException ie = assertThrows(InterruptedException.class, () -> performLockInterruptiblyAndGet(lock, Assertions::fail));
			assertEquals(1, lockCount.get());
			assertSame(thrownInterruptedException.get(), ie);
		}

		default void testSuccessfulLockInterruptiblyAndGet(@NonNull Supplier<Object> objectSupplier) throws InterruptedException {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnLockInterruptibly(() -> {
				lockCount.incrementAndGet();
				assertEquals(currentThread, Thread.currentThread());
			});
			Object expected = objectSupplier.get();
			Object actual = performLockInterruptiblyAndGet(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertEquals(currentThread, Thread.currentThread());
				});
				return expected;
			});
			assertSame(expected, actual);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		<Return> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable Supplier<Return> supplier) throws InterruptedException;
	}

	interface TryLockInstantTest {

		@DisplayName("test successful instant try-lock (run)")
		@Test
		default void testSuccessfulTryLockInstantRun() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			performTryLockAndRun(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});

			}, Assertions::fail);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		@DisplayName("test failed instant try-lock (run)")
		@Test
		default void testFailedTryLockInstantRun() {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicBoolean failedRunnableReached = new AtomicBoolean(false);
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return false;
			});
			performTryLockAndRun(lock, Assertions::fail, () -> {
				failedRunnableReached.set(true);
			});
			assertTrue(failedRunnableReached.get());
			assertEquals(1, lockCount.get());
		}

		void performTryLockAndRun(@Nullable Lock lock, @Nullable Runnable onLockSuccess, @Nullable Runnable onLockFail);

		@DisplayName("test successful instant try-lock (get)")
		@TestFactory
		default Iterable<DynamicTest> testSuccessfulTryLockInstantGet() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testSuccessfulTryLockInstantGet(entry.getValue()))).collect(Collectors.toList());
		}

		default void testSuccessfulTryLockInstantGet(@NonNull Supplier<Object> objectSupplier) {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicInteger executionCount = new AtomicInteger();
			AtomicInteger unlockCount = new AtomicInteger();
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return true;
			});
			Object expected = objectSupplier.get();
			Object actual = performTryLockAndGet(lock, () -> {
				executionCount.incrementAndGet();
				lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					assertSame(currentThread, Thread.currentThread());
				});
				return expected;
			}, Assertions::fail);
			assertEquals(expected, actual);
			assertEquals(1, lockCount.get());
			assertEquals(1, executionCount.get());
			assertEquals(1, unlockCount.get());
		}

		@DisplayName("test failed instant try-lock (get)")
		@TestFactory
		default Iterable<DynamicTest> testFailedTryLockInstantGet() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testFailedTryLockInstantGet(entry.getValue()))).collect(Collectors.toList());
		}

		default void testFailedTryLockInstantGet(@NonNull Supplier<Object> objectSupplier) {
			StubbedLock lock = new StubbedLock();
			AtomicInteger lockCount = new AtomicInteger();
			AtomicBoolean failedSupplierReached = new AtomicBoolean(false);
			Thread currentThread = Thread.currentThread();
			lock.setOnTryLockInstant(() -> {
				lockCount.incrementAndGet();
				assertSame(currentThread, Thread.currentThread());
				return false;
			});
			Object expected = objectSupplier.get();
			Object actual = performTryLockAndGet(lock, Assertions::fail, () -> {
				failedSupplierReached.set(true);
				return expected;
			});
			assertSame(expected, actual);
			assertTrue(failedSupplierReached.get());
			assertEquals(1, lockCount.get());
		}

		<Return> Return performTryLockAndGet(@Nullable Lock lock, @Nullable Supplier<Return> onLockSuccess, @Nullable Supplier<Return> onLockFail);
	}

	interface TryLockTimeoutTest {

	}
}
