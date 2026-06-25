package test;

import com.ansill.autolock.ThrowableRunnable;
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, Thread.currentThread(), StubbedLock.Event.LOCK_INTERRUPTIBLY)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
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
											new StubbedLock.CallEvent(lock, 0, currentThread, StubbedLock.Event.LOCK_INTERRUPTIBLY),
											new StubbedLock.CallEvent(lock, 1, currentThread, StubbedLock.Event.UNLOCK)
							),
							lock.getActualEvents()
			);
		}
	}

	<T extends Throwable> void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException;
}
