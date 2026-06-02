package test;

import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
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

	@NonNull
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

	@NonNull
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
	static Map<String, Supplier<? extends Exception>> getRandomCheckedExceptions(@NonNull Random random) {
		Map<String, Supplier<? extends Exception>> returnObj = new HashMap<>();
		{
			long seed = random.nextLong();
			returnObj.put("Exception", () -> new Exception("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("InterruptedException", () -> new InterruptedException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IOException", () -> new IOException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Throwable>> getRandomUncheckeds(@NonNull Random random) {
		Map<String, Supplier<? extends Throwable>> returnObj = new HashMap<>(getRandomRuntimeExceptions(random));
		returnObj.putAll(getRandomErrors(random));
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Throwable>> getRandomThrowables(@NonNull Random random) {
		Map<String, Supplier<? extends Throwable>> returnObj = new HashMap<>(getRandomCheckedExceptions(random));
		returnObj.putAll(getRandomRuntimeExceptions(random));
		returnObj.putAll(getRandomErrors(random));
		return returnObj;
	}

	interface LockRunTest {

		@DisplayName("lock-run: with null lock")
		@Test
		default void testLockRun_NullLock() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndRun(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("lock-run: with null runnable")
		@Test
		default void testLockRun_NullRunnable() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndRun(lock, null));
				assertEquals("runnable must not be null", exception.getMessage());
				assertEquals(Collections.emptyList(), lock.getActualEvents());
			}
		}

		@DisplayName("lock-run: successful lock")
		@Test
		default void testLockRun_Success() {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lock-run: exception thrown inside runnable")
		@TestFactory
		default Iterable<DynamicTest> testLockRun_ThrowableInsideRunnable() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockRun_ThrowableInsideRunnable(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockRun_ThrowableInsideRunnable(@NonNull Supplier<? extends Throwable> supplier) {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lock-run: with Throwable thrown at lock()")
		@TestFactory
		default Iterable<DynamicTest> testLockRun_ThrowableAtLockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockRun_ThrowableAtLockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockRun_ThrowableAtLockMethod(@NonNull Supplier<? extends Throwable> supplier) {
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
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lock-run: with Throwable thrown at unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockRun_ThrowableAtUnlockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockRun_ThrowableAtUnlockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockRun_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLock(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockAndRun(lock, () -> lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				})));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lock-run: with Throwable thrown in runnable AND Throwable thrown in unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockRun_ThrowableInRunnableAndThrowableInUnlockMethod() {
			Random random = new Random(getSeed(0).hashCode());
			Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
			return throwableMap.entrySet().stream().map(entry -> {
				Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
				List<String> keys = new ArrayList<>(innerRandomMap.keySet());
				Collections.shuffle(keys, random);
				Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
				return DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockRun_ThrowableInRunnableAndThrowableInUnlockMethod(mainThrowable, entry.getValue()));
			}).collect(Collectors.toList());
		}

		default void testLockRun_ThrowableInRunnableAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
				AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
				lock.setOnLock(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(
								Throwable.class,
								() -> performLockAndRun(lock, () -> {
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
								}));
				assertSame(mainThrowableRef.get(), actualThrowable);
				assertEquals(1, actualThrowable.getSuppressed().length);
				assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		<T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T;
	}

	interface LockGetTest {

		@DisplayName("lock-get: with null lock")
		@Test
		default void testLockGet_NullLock() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("lock-get: with null supplier")
		@Test
		default void testLockGet_NullSupplier() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockAndGet(lock, null));
				assertEquals("supplier must not be null", exception.getMessage());
			}
		}

		@DisplayName("lock-get: successful lock")
		@TestFactory
		default Iterable<DynamicTest> testLockGet_Success() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockGet_Success(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockGet_Success(@NonNull Supplier<Object> objectSupplier) {
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


		@DisplayName("lock-get: exception thrown inside supplier")
		@TestFactory
		default Iterable<DynamicTest> testLockGet_ThrowableInsideSupplier() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockGet_ThrowableInsideSupplier(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockGet_ThrowableInsideSupplier(@NonNull Supplier<? extends Throwable> supplier) {
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
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockAndGet(lock, () -> {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lock-get: with Throwable thrown at lock()")
		@TestFactory
		default Iterable<DynamicTest> testLockGet_ThrowableAtLockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockGet_ThrowableAtLockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockGet_ThrowableAtLockMethod(@NonNull Supplier<? extends Throwable> supplier) {
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
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockAndGet(lock, Assertions::fail));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lock-get: with Throwable thrown at unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockGet_ThrowableAtUnlockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockGet_ThrowableAtUnlockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockGet_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLock(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(
								Throwable.class,
								() -> performLockAndGet(
												lock,
												() -> {
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
												}));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lock-get: with Throwable thrown in supplier AND Throwable thrown in unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockGet_ThrowableInSupplierAndThrowableInUnlockMethod() {
			Random random = new Random(getSeed(0).hashCode());
			Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
			return throwableMap.entrySet().stream().map(entry -> {
				Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
				List<String> keys = new ArrayList<>(innerRandomMap.keySet());
				Collections.shuffle(keys, random);
				Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
				return DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockGet_ThrowableInSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue()));
			}).collect(Collectors.toList());
		}

		default void testLockGet_ThrowableInSupplierAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
				AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
				lock.setOnLock(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(
								Throwable.class,
								() -> performLockAndGet(lock, () -> {
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
								}));
				assertSame(mainThrowableRef.get(), actualThrowable);
				assertEquals(1, actualThrowable.getSuppressed().length);
				assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		<Return, T extends Throwable> Return performLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T;
	}

	interface LockInterruptiblyRunTest {

		@DisplayName("lockInterruptibly-run: with null lock")
		@Test
		default void testLockInterruptiblyRun_NullLock() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("lockInterruptibly-run: with null runnable")
		@Test
		default void testLockInterruptiblyRun_NullRunnable() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndRun(lock, null));
				assertEquals("runnable must not be null", exception.getMessage());
				assertEquals(
								Collections.emptyList(),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-run: successful lock")
		@Test
		default void testLockInterruptiblyRun_Success() throws InterruptedException {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-run: lock with interrupt")
		@Test
		default void testLockInterruptiblyRun_LockWithInterrupt() {
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
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, Thread.currentThread(), StubbedLock.Event.LOCK_INTERRUPTIBLY)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-run: exception thrown inside runnable")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyRun_ThrowableInsideRunnable() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyRun_ThrowableInsideRunnable(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockInterruptiblyRun_ThrowableInsideRunnable(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger executionCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(lock, () -> {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-run: with Throwable thrown at lock()")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyRun_ThrowableAtLockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyRun_ThrowableAtLockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockInterruptiblyRun_ThrowableAtLockMethod(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(lock, Assertions::fail));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-run: with Throwable thrown at unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyRun_ThrowableAtUnlockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyRun_ThrowableAtUnlockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockInterruptiblyRun_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndRun(lock, () -> lock.setOnUnlock(() -> {
					unlockCount.getAndIncrement();
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				})));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-run: with Throwable thrown in runnable AND Throwable thrown in unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyRun_ThrowableInRunnableAndThrowableInUnlockMethod() {
			Random random = new Random(getSeed(0).hashCode());
			Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
			return throwableMap.entrySet().stream().map(entry -> {
				Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
				List<String> keys = new ArrayList<>(innerRandomMap.keySet());
				Collections.shuffle(keys, random);
				Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
				return DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyRun_ThrowableInRunnableAndThrowableInUnlockMethod(mainThrowable, entry.getValue()));
			}).collect(Collectors.toList());
		}

		default void testLockInterruptiblyRun_ThrowableInRunnableAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
				AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(
								Throwable.class,
								() -> performLockInterruptiblyAndRun(lock, () -> {
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
								}));
				assertSame(mainThrowableRef.get(), actualThrowable);
				assertEquals(1, actualThrowable.getSuppressed().length);
				assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		<T extends Throwable> void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException;
	}

	interface LockInterruptiblyGetTest {

		@DisplayName("lockInterruptibly-get: with null lock")
		@Test
		default void testLockInterruptiblyGet_NullLock() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndGet(null, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("lockInterruptibly-get: with null supplier")
		@Test
		default void testLockInterruptiblyGet_NullSupplier() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performLockInterruptiblyAndGet(lock, null));
				assertEquals("supplier must not be null", exception.getMessage());
				assertEquals(Collections.emptyList(), lock.getActualEvents());
			}
		}

		@DisplayName("lockInterruptibly-get: successful lock")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyGet_Success() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyGet_Success(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockInterruptiblyGet_Success(@NonNull Supplier<Object> objectSupplier) throws InterruptedException {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-get: lock with interrupt")
		@Test
		default void testLockInterruptiblyGet_LockWithInterrupt() {
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
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, Thread.currentThread(), StubbedLock.Event.LOCK_INTERRUPTIBLY)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-get: exception thrown inside supplier")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyGet_ThrowableInsideSupplier() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyGet_ThrowableInsideSupplier(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockInterruptiblyGet_ThrowableInsideSupplier(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger executionCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndGet(lock, () -> {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-get: with Throwable thrown at lock()")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyGet_ThrowableAtLockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyGet_ThrowableAtLockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockInterruptiblyGet_ThrowableAtLockMethod(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
					try {
						throw supplier.get();
					} catch (Throwable throwable) {
						throwableRef.set(throwable);
						throw throwable;
					}
				});
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndGet(lock, Assertions::fail));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-get: with Throwable thrown at unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyGet_ThrowableAtUnlockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyGet_ThrowableAtUnlockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testLockInterruptiblyGet_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performLockInterruptiblyAndGet(lock, () -> {
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
				}));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("lockInterruptibly-get: with Throwable thrown in supplier AND Throwable thrown in unlock()")
		@TestFactory
		default Iterable<DynamicTest> testLockInterruptiblyGet_ThrowableInSupplierAndThrowableInUnlockMethod() {
			Random random = new Random(getSeed(0).hashCode());
			Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
			return throwableMap.entrySet().stream().map(entry -> {
				Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
				List<String> keys = new ArrayList<>(innerRandomMap.keySet());
				Collections.shuffle(keys, random);
				Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
				return DynamicTest.dynamicTest(entry.getKey(), () -> this.testLockInterruptiblyGet_ThrowableInSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue()));
			}).collect(Collectors.toList());
		}

		default void testLockInterruptiblyGet_ThrowableInSupplierAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicInteger unlockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				AtomicReference<Object> mainThrowableRef = new AtomicReference<>();
				AtomicReference<Object> unlockThrowableRef = new AtomicReference<>();
				lock.setOnLockInterruptibly(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
				});
				Throwable actualThrowable = assertThrows(
								Throwable.class,
								() -> performLockInterruptiblyAndGet(lock, () -> {
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
								}));
				assertSame(mainThrowableRef.get(), actualThrowable);
				assertEquals(1, actualThrowable.getSuppressed().length);
				assertSame(unlockThrowableRef.get(), actualThrowable.getSuppressed()[0]);
				assertEquals(1, lockCount.get());
				assertEquals(1, unlockCount.get());
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		<Return, T extends Throwable> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T, InterruptedException;
	}

	interface TryLockInstantRunTest {

		@DisplayName("tryLock-instant-run: with null lock")
		@Test
		default void testTryLockInstantRun_NullLock() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRun(null, Assertions::fail, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("tryLock-instant-run: with null onLockSuccess runnable")
		@Test
		default void testTryLockInstantRun_NullOnLockSuccessRunnable() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRun(lock, null, Assertions::fail));
				assertEquals("onLockSuccess must not be null", exception.getMessage());
				assertEquals(Collections.emptyList(), lock.getActualEvents());
			}
		}

		@DisplayName("tryLock-instant-run: with null onLockFail runnable")
		@Test
		default void testTryLockInstantRun_NullOnLockFailRunnable() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndRun(lock, Assertions::fail, null));
				assertEquals("onLockFail must not be null", exception.getMessage());
				assertEquals(Collections.emptyList(), lock.getActualEvents());
			}
		}

		@DisplayName("tryLock-instant-run: successful lock")
		@Test
		default void testTryLockInstantRun_Success() {
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
				assertEquals(
								Arrays.asList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-run: failed lock")
		@Test
		default void testTryLockInstantRun_Failed() {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				AtomicBoolean failedRunnableReached = new AtomicBoolean(false);
				Thread currentThread = Thread.currentThread();
				lock.setOnTryLockInstant(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
					return false;
				});
				performTryLockAndRun(lock, Assertions::fail, () -> failedRunnableReached.set(true));
				assertTrue(failedRunnableReached.get());
				assertEquals(1, lockCount.get());
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-run: exception thrown inside onSuccessLock runnable")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantRun_ThrowableInsideOnSuccessLockRunnable() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantRun_ThrowableInsideOnSuccessLockRunnable(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantRun_ThrowableInsideOnSuccessLockRunnable(@NonNull Supplier<? extends Throwable> supplier) {
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
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndRun(lock, () -> {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-run: with Throwable thrown at tryLock()")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantRun_ThrowableAtTryLockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantRun_ThrowableAtTryLockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantRun_ThrowableAtTryLockMethod(@NonNull Supplier<? extends Throwable> supplier) {
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
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndRun(lock, Assertions::fail, Assertions::fail));
				assertSame(throwableRef.get(), actualThrowable);
				assertEquals(1, lockCount.get());
				assertEquals(
								Collections.singletonList(
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-run: with Throwable thrown at unlock()")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantRun_ThrowableAtUnlockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantRun_ThrowableAtUnlockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantRun_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier) {
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
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndRun(lock, () -> lock.setOnUnlock(() -> {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-run: with Throwable thrown in onLockSuccess runnable AND Throwable thrown in unlock()")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantRun_ThrowableInOnLockSuccessRunnableAndThrowableInUnlockMethod() {
			Random random = new Random(getSeed(0).hashCode());
			Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
			return throwableMap.entrySet().stream().map(entry -> {
				Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
				List<String> keys = new ArrayList<>(innerRandomMap.keySet());
				Collections.shuffle(keys, random);
				Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
				return DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantRun_ThrowableInOnLockSuccessRunnableAndThrowableInUnlockMethod(mainThrowable, entry.getValue()));
			}).collect(Collectors.toList());
		}

		default void testTryLockInstantRun_ThrowableInOnLockSuccessRunnableAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier) {
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
								() -> performTryLockAndRun(lock, () -> {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-run: exception thrown inside onLockFail runnable")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantRun_ThrowableInsideOnFailLockRunnable() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantRun_ThrowableInsideOnFailLockRunnable(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantRun_ThrowableInsideOnFailLockRunnable(@NonNull Supplier<? extends Throwable> supplier) {
			try (StubbedLock lock = new StubbedLock()) {
				AtomicInteger lockCount = new AtomicInteger();
				Thread currentThread = Thread.currentThread();
				lock.setOnTryLockInstant(() -> {
					lockCount.incrementAndGet();
					assertSame(currentThread, Thread.currentThread());
					return false;
				});
				AtomicReference<Object> throwableRef = new AtomicReference<>();
				Throwable actualThrowable = assertThrows(Throwable.class, () -> performTryLockAndRun(lock, Assertions::fail, () -> {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		<T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws T1, T2;
	}

	interface TryLockInstantGetTest {

		@DisplayName("tryLock-instant-get: with null lock")
		@Test
		default void testTryLockInstantGet_NullLock() {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(null, Assertions::fail, Assertions::fail));
			assertEquals("lock must not be null", exception.getMessage());
		}

		@DisplayName("tryLock-instant-get: with null onLockSuccess supplier")
		@Test
		default void testTryLockInstantGet_NullOnLockSuccessSupplier() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(lock, null, Assertions::fail));
				assertEquals("onLockSuccess must not be null", exception.getMessage());
				assertEquals(Collections.emptyList(), lock.getActualEvents());
			}
		}

		@DisplayName("tryLock-instant-get: with null onLockFail supplier")
		@Test
		default void testTryLockInstantGet_NullOnLockFailSupplier() {
			try (StubbedLock lock = new StubbedLock()) {
				NullPointerException exception = assertThrows(NullPointerException.class, () -> performTryLockAndGet(lock, Assertions::fail, null));
				assertEquals("onLockFail must not be null", exception.getMessage());
				assertEquals(Collections.emptyList(), lock.getActualEvents());
			}
		}

		@DisplayName("tryLock-instant-get: successful lock")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantGet_Success() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantGet_Success(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantGet_Success(@NonNull Supplier<Object> objectSupplier) {
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
				Object expectedObject = objectSupplier.get();
				Object actualObject = performTryLockAndGet(lock, () -> {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-get: failed lock")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantGet_Failed() {
			return getRandomObjects(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantGet_Failed(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantGet_Failed(@NonNull Supplier<Object> objectSupplier) {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-get: exception thrown inside onSuccessLock supplier")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantGet_ThrowableInsideOnSuccessLockSupplier(@NonNull Supplier<? extends Throwable> supplier) {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-get: with Throwable thrown at tryLock()")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantGet_ThrowableAtTryLockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantGet_ThrowableAtTryLockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantGet_ThrowableAtTryLockMethod(@NonNull Supplier<? extends Throwable> supplier) {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-get: with Throwable thrown at unlock()")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantGet_ThrowableAtUnlockMethod() {
			return getRandomUncheckeds(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantGet_ThrowableAtUnlockMethod(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantGet_ThrowableAtUnlockMethod(@NonNull Supplier<? extends Throwable> supplier) {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-get: with Throwable thrown in onLockSuccess supplier AND Throwable thrown in unlock()")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod() {
			Random random = new Random(getSeed(0).hashCode());
			Map<String, Supplier<? extends Throwable>> throwableMap = getRandomUncheckeds(random);
			return throwableMap.entrySet().stream().map(entry -> {
				Map<String, Supplier<? extends Throwable>> innerRandomMap = getRandomThrowables(random);
				List<String> keys = new ArrayList<>(innerRandomMap.keySet());
				Collections.shuffle(keys, random);
				Supplier<? extends Throwable> mainThrowable = innerRandomMap.get(keys.iterator().next());
				return DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(mainThrowable, entry.getValue()));
			}).collect(Collectors.toList());
		}

		default void testTryLockInstantGet_ThrowableInOnLockSuccessSupplierAndThrowableInUnlockMethod(@NonNull Supplier<? extends Throwable> mainExceptionSupplier, @NonNull Supplier<? extends Throwable> supplier) {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK),
												new StubbedLock.CallEvents(1, currentThread, StubbedLock.Event.UNLOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		@DisplayName("tryLock-instant-get: exception thrown inside onLockFail supplier")
		@TestFactory
		default Iterable<DynamicTest> testTryLockInstantGet_ThrowableInsideOnFailLockSupplier() {
			return getRandomThrowables(new Random(getSeed(0).hashCode())).entrySet().stream().map(entry -> DynamicTest.dynamicTest(entry.getKey(), () -> this.testTryLockInstantGet_ThrowableInsideOnFailLockSupplier(entry.getValue()))).collect(Collectors.toList());
		}

		default void testTryLockInstantGet_ThrowableInsideOnFailLockSupplier(@NonNull Supplier<? extends Throwable> supplier) {
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
												new StubbedLock.CallEvents(0, currentThread, StubbedLock.Event.TRY_LOCK)
								),
								lock.getActualEvents()
				);
			}
		}

		<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws T1, T2;
	}

	interface TryLockTimeoutTest {

	}
}
