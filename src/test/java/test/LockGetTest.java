package test;

import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static test.TestUtility.*;

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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	<Return, T extends Throwable> Return performLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T;
}
