package test;

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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.TRY_LOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	<Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws T1, T2;
}
