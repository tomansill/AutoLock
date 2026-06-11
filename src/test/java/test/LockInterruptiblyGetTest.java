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
