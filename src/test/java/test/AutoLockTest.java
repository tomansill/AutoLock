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
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.generateAlphanumericString;
import static test.TestUtility.getSeed;

@DisplayName("AutoLock Test")
abstract class AutoLockTest {

	@NonNull
	static Map<String, Supplier<Object>> getRandomObjects(@NonNull Random random1) {
		Map<String, Supplier<Object>> returnObj = new HashMap<>();
		{
			long seed = random1.nextLong();
			returnObj.put("string", () -> generateAlphanumericString(new Random(seed), 3, 32));
		}
		{
			long seed = random1.nextLong();
			returnObj.put("list of random strings", () -> {
				Random random = new Random(seed);
				return IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toSet());
			});
		}
		{
			long seed = random1.nextLong();
			returnObj.put("map of random strings", () -> {
				Random random = new Random(seed);
				return IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toMap(i -> i, i -> IntStream.range(0, random.nextInt(15) + 2).mapToObj(j -> generateAlphanumericString(random, 3, 20)).collect(Collectors.toSet())));
			});
		}
		{
			long seed = random1.nextLong();
			returnObj.put("big decimal", () -> new BigDecimal(new Random(seed).nextDouble()));
		}
		{
			long seed = random1.nextLong();
			returnObj.put("int", () -> new Random(seed).nextInt());
		}
		{
			returnObj.put("null", () -> null);
		}
		return returnObj;
	}

	static Map<String, Supplier<? extends RuntimeException>> getRandomRuntimeExceptions(@NonNull Random random) {
		Map<String, Supplier<? extends RuntimeException>> returnObj = new HashMap<>();
		{
			long seed = random.nextLong();
			returnObj.put("RuntimeException", () -> new RuntimeException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("SecurityException", () -> new SecurityException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("CancellationException", () -> new CancellationException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IllegalStateException", () -> new IllegalStateException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IllegalArgumentException", () -> new IllegalArgumentException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("UnsupportedOperationException", () -> new UnsupportedOperationException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IllegalMonitorStateException", () -> new IllegalMonitorStateException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		return returnObj;
	}

	static Map<String, Supplier<? extends Error>> getRandomErrors(@NonNull Random random) {
		Map<String, Supplier<? extends Error>> returnObj = new HashMap<>();
		{
			long seed = random.nextLong();
			returnObj.put("AssertionError", () -> new AssertionError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("Error", () -> new Error("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("OutOfMemoryError", () -> new OutOfMemoryError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("StackOverflowError", () -> new StackOverflowError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("LinkageError", () -> new LinkageError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("ExceptionInInitializerError", () -> new ExceptionInInitializerError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Throwable>> getRandomThrowables(@NonNull Random random) {
		Map<String, Supplier<? extends Throwable>> returnObj = new HashMap<>(getRandomCheckedExceptions(random));
		returnObj.putAll(getRandomRuntimeExceptions(random));
		returnObj.putAll(getRandomErrors(random));
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Throwable>> getRandomCheckedExceptions(@NonNull Random random) {
		Map<String, Supplier<? extends Throwable>> returnObj = new HashMap<>();
		{
			long seed = random.nextLong();
			returnObj.put("Exception", () -> new Exception("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("InterruptedException", () -> new InterruptedException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		return returnObj;
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
			try (StubbedLock stubbedLock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndRun(stubbedLock, null));
				assertEquals("runnable must not be null", exception.getMessage());
			}
		}

		@DisplayName("test successful lock (run)")
		@Test
		default void testSuccessfulLockRunnable() {
			try (StubbedLock lock = new StubbedLock()) {
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
		}

		@DisplayName("test lock with exception inside runnable (run)")
		@TestFactory
		default Iterable<DynamicTest> testLockRunnableWithException() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockRunnableWithException(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockRunnableWithException(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
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
						throw supplier.get();
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
		}

		@DisplayName("test lock with error at lock (run)")
		@TestFactory
		default Iterable<DynamicTest> testLockRunnableWithErrorAtLock() {
			return getRandomErrors(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockRunnableWithErrorAtLock(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockRunnableWithErrorAtLock(@NonNull Supplier<? extends Error> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLock(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockAndRun(lock, Assertions::fail));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
			}
		}

		@DisplayName("test lock with RuntimeException at lock (run)")
		@TestFactory
		default Iterable<DynamicTest> testLockRunnableWithREAtLock() {
			return getRandomRuntimeExceptions(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockRunnableWithREAtLock(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockRunnableWithREAtLock(@NonNull Supplier<? extends RuntimeException> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLock(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockAndRun(lock, Assertions::fail));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
			}
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
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(lock, null));
				assertEquals("supplier must not be null", exception.getMessage());
			}
		}

		@DisplayName("test successful lock (get)")
		@TestFactory
		default Iterable<DynamicTest> testSuccessfulLockGet() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testSuccessfulLockGet(entry.getValue()))).collect(Collectors.toList());
		}

		default void testSuccessfulLockGet(@NonNull Supplier<Object> objectSupplier) {
			try (StubbedLock lock = new StubbedLock()) {
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
			try (StubbedLock lock = new StubbedLock()) {
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
		}

		@DisplayName("test lockInterruptibly with interrupt (run)")
		@Test
		default void testLockInterruptiblyAndRunWithInterrupt() {
			try (StubbedLock lock = new StubbedLock()) {
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
			try (StubbedLock lock = new StubbedLock()) {
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
		}

		default void testSuccessfulLockInterruptiblyAndGet(@NonNull Supplier<Object> objectSupplier) throws InterruptedException {
			try (StubbedLock lock = new StubbedLock()) {
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
		}

		<Return> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable Supplier<Return> supplier) throws InterruptedException;
	}

	interface TryLockInstantTest {

		@DisplayName("test successful instant try-lock (run)")
		@Test
		default void testSuccessfulTryLockInstantRun() {
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
		}

		@DisplayName("test failed instant try-lock (run)")
		@Test
		default void testFailedTryLockInstantRun() {
			try (StubbedLock lock = new StubbedLock()) {
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
		}

		void performTryLockAndRun(@Nullable Lock lock, @Nullable Runnable onLockSuccess, @Nullable Runnable onLockFail);

		@DisplayName("test successful instant try-lock (get)")
		@TestFactory
		default Iterable<DynamicTest> testSuccessfulTryLockInstantGet() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testSuccessfulTryLockInstantGet(entry.getValue()))).collect(Collectors.toList());
		}

		default void testSuccessfulTryLockInstantGet(@NonNull Supplier<Object> objectSupplier) {
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
		}

		@DisplayName("test failed instant try-lock (get)")
		@TestFactory
		default Iterable<DynamicTest> testFailedTryLockInstantGet() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testFailedTryLockInstantGet(entry.getValue()))).collect(Collectors.toList());
		}

		default void testFailedTryLockInstantGet(@NonNull Supplier<Object> objectSupplier) {
			try (StubbedLock lock = new StubbedLock()) {
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
		}

		<Return> Return performTryLockAndGet(@Nullable Lock lock, @Nullable Supplier<Return> onLockSuccess, @Nullable Supplier<Return> onLockFail);
	}

	interface TryLockTimeoutTest {

	}
}
