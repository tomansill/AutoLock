package com.ansill.lock.autolock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * AutoLock class that creates LockedAutoLock object AutoCloseable resource that can be used in Try-with-resources scope
 */
public interface AutoLock {

	/**
	 * Creates AutoLock from lock
	 *
	 * @param lock lock
	 * @return AutoLock auto lock
	 */
	@Nonnull
	static AutoLock create(@Nonnull Lock lock) {
		//noinspection ConstantConditions
		if (lock == null) throw new IllegalArgumentException("'lock' is null");
		return new AutoLockImplementation(lock);
	}

	/**
	 * Locks and creates a LockedAutoLock reference
	 *
	 * @param lock lock to lock on
	 * @return LockedAutoLock locked auto lock
	 */
	@Nonnull
	static LockedAutoLock doLock(@Nonnull Lock lock) {
		return create(lock).doLock();
	}

	/**
	 * Locks and creates a LockedAutoLock reference
	 *
	 * @param lock lock to lock on
	 * @return LockedAutoLock locked auto lock
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	@Nonnull
	static LockedAutoLock doLockInterruptibly(@Nonnull Lock lock) throws InterruptedException {
		return create(lock).doLockInterruptibly();
	}

	/**
	 * Locks the provided lock, runs the runnable during the lock, unlocks afterwards
	 *
	 * @param <T>      type of exception
	 * @param lock     lock to lock on
	 * @param runnable runnable to run while locked
	 * @throws T thrown if runnable has thrown an exception
	 */
	static <T extends Throwable> void lockAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T> runnable) throws T {

		// Ensure runnable is not null
		//noinspection ConstantConditions
		if (runnable == null) throw new IllegalArgumentException("'runnable' is null");

		// Lock it
		try (LockedAutoLock ignored = doLock(lock)) {

			// Run it
			runnable.run();
		}
	}

	/**
	 * Locks the provided lock, runs the supplier during the lock, unlocks afterwards and, assuming if supplier does
	 * not return an exception, value returned by the supplier will be returned
	 *
	 * @param <R>      desired type of the return value
	 * @param <T>      type of exception
	 * @param lock     lock to lock on
	 * @param supplier supplier to run while locked
	 * @return result of supplier
	 * @throws T thrown if runnable has thrown an exception
	 */
	static <R, T extends Throwable> R lockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R, T> supplier) throws T {

		// Ensure supplier is not null
		//noinspection ConstantConditions
		if (supplier == null) throw new IllegalArgumentException("'supplier' is null");

		// Lock it
		try (LockedAutoLock ignored = doLock(lock)) {

			// Get it
			return supplier.get();
		}
	}

	/**
	 * Locks the provided lock, runs the runnable during the lock, unlocks afterwards
	 *
	 * @param <T>      type of exception
	 * @param lock     lock to lock on
	 * @param runnable runnable to run while locked
	 * @throws T                    thrown if runnable has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <T extends Throwable> void lockInterruptiblyAndRun(
					@Nonnull Lock lock,
					@Nonnull ThrowableRunnable<T> runnable
	) throws T, InterruptedException {

		// Ensure runnable is not null
		//noinspection ConstantConditions
		if (runnable == null) throw new IllegalArgumentException("'runnable' is null");

		// Lock it
		try (LockedAutoLock ignored = doLockInterruptibly(lock)) {

			// Run it
			runnable.run();
		}
	}

	/**
	 * Locks the provided lock, runs the supplier during the lock, unlocks afterwards and, assuming if supplier does
	 * not return an exception, value returned by the supplier will be returned
	 *
	 * @param <R>      desired type of the return value
	 * @param <T>      type of exception
	 * @param lock     lock to lock on
	 * @param supplier supplier to run while locked
	 * @return result of supplier
	 * @throws T                    thrown if runnable has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <R, T extends Throwable> R lockInterruptiblyAndGet(
					@Nonnull Lock lock,
					@Nonnull ThrowableSupplier<R, T> supplier
	)
					throws T, InterruptedException {

		// Ensure supplier is not null
		//noinspection ConstantConditions
		if (supplier == null) throw new IllegalArgumentException("'supplier' is null");

		// Lock it
		try (LockedAutoLock ignored = doLockInterruptibly(lock)) {

			// Get it
			return supplier.get();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
	 *
	 * @param <T1>           type of exception
	 * @param lock          lock to lock on
	 * @param onLockSuccess runnable to run while locked
	 * @throws T1 thrown if runnable has thrown an exception
	 */
	static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T1> onLockSuccess, @Nonnull ThrowableRunnable<T2> onLockFail) throws T1, T2 {

		// Ensure runnables are not null
		//noinspection ConstantConditions
		if (lock == null) throw new IllegalArgumentException("'lock' is null");
		//noinspection ConstantConditions
		if (onLockSuccess == null) throw new IllegalArgumentException("'onLockSuccess' is null");
		//noinspection ConstantConditions
		if (onLockFail == null) throw new IllegalArgumentException("'onLockFail' is null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock, new AtomicBoolean(true))) {
				onLockSuccess.run();
			}
		} else {
			onLockFail.run();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
	 * Assuming that supplier did not throw an exception, value returned by the supplier will be returned.
	 *
	 * @param <R>                   desired type of the return value
	 * @param <T1>                   type of exception
	 * @param lock                  lock to lock on
	 * @param onLockSuccessSupplier supplier to run while locked
	 * @return result of supplier
	 * @throws T1 thrown if runnable has thrown an exception
	 */
	static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R, T1> onLockSuccessSupplier, @Nonnull ThrowableSupplier<R, T2> onLockFailSupplier) throws T1,T2 {

		// Ensure supplier is not null
		//noinspection ConstantConditions
		if (lock == null) throw new IllegalArgumentException("'lock' is null");
		//noinspection ConstantConditions
		if (onLockSuccessSupplier == null) throw new IllegalArgumentException("'onLockSuccessSupplier' is null");
		//noinspection ConstantConditions
		if (onLockFailSupplier == null) throw new IllegalArgumentException("'onLockFailSupplier' is null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock, new AtomicBoolean(true))) {
				return onLockSuccessSupplier.get();
			}
		} else {
			return onLockFailSupplier.get();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the onSuccessfulLock will be run. After running the onSuccessfulLock, lock is unlocked.
	 *
	 * @param <T1>              type of exception
	 * @param lock             lock to lock on
	 * @param timeout          duration of the lock attempt
	 * @param onSuccessfulLock onSuccessfulLock to run while locked
	 * @throws T1                    thrown if onSuccessfulLock has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@Nonnull Lock lock,
					@Nonnull Duration timeout,
					@Nonnull ThrowableRunnable<T1> onSuccessfulLock,
					@Nonnull ThrowableRunnable<T2> onFailedLock
	) throws  T1, T2, InterruptedException {

		// Ensure onSuccessfulLock is not null
		//noinspection ConstantConditions
		if (lock == null) throw new IllegalArgumentException("'lock' is null");
		//noinspection ConstantConditions
		if (timeout == null) throw new IllegalArgumentException("'timeout' is null");
		//noinspection ConstantConditions
		if (onSuccessfulLock == null) throw new IllegalArgumentException("'onSuccessfulLock' is null");
		//noinspection ConstantConditions
		if (onFailedLock == null) throw new IllegalArgumentException("'onFailedLock' is null");

		// Lock it
		if (lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock, new AtomicBoolean(true))) {
				onSuccessfulLock.run();
			}
		} else {
			onFailedLock.run();
		}
	}


	/**
	 * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
	 * Assuming that supplier did not throw an exception, value returned by the supplier will be returned.
	 *
	 * @param <R>                   desired type of the return value
	 * @param <T1>                   type of exception
	 * @param lock                  lock to lock on
	 * @param timeout               duration of the lock attempt
	 * @param onLockSuccessSupplier supplier to run while locked
	 * @return result of supplier
	 * @throws T1                    thrown if runnable has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@Nonnull Lock lock,
					@Nonnull Duration timeout,
					@Nonnull ThrowableSupplier<R, T1> onLockSuccessSupplier,
					@Nonnull ThrowableSupplier<R, T2> onLockFailSupplier
	) throws  T1, T2, InterruptedException {

		// Ensure supplier is not null
		//noinspection ConstantConditions
		if (lock == null) throw new IllegalArgumentException("'lock' is null");
		//noinspection ConstantConditions
		if (timeout == null) throw new IllegalArgumentException("'timeout' is null");
		//noinspection ConstantConditions
		if (onLockSuccessSupplier == null) throw new IllegalArgumentException("'onLockSuccessSupplier' is null");
		//noinspection ConstantConditions
		if (onLockFailSupplier == null) throw new IllegalArgumentException("'onLockFailSupplier' is null");

		// Lock it
		if (lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock, new AtomicBoolean(true))) {
				return onLockSuccessSupplier.get();
			}
		} else {
			return onLockFailSupplier.get();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
	 *
	 * @param <T1>                      type of exception
	 * @param lock                     lock to lock on
	 * @param time                     timeout duration
	 * @param unit                     timeout timeunit
	 * @param onSuccessfulLockRunnable runnable to run while locked
	 * @throws T1                    thrown if runnable has thrown an exception
	 * @throws InterruptedException thrown when the locking process was interrupted
	 */
	static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@Nonnull Lock lock,
					@Nonnegative long time,
					@Nonnull TimeUnit unit,
					@Nonnull ThrowableRunnable<T1> onSuccessfulLockRunnable,
					@Nonnull ThrowableRunnable<T2> onFailedLockRunnable
	) throws  T1, T2, InterruptedException {

		// Ensure onSuccessfulLock is not null
		//noinspection ConstantConditions
		if (lock == null) throw new IllegalArgumentException("'lock' is null");
		//noinspection ConstantConditions
		if (unit == null) throw new IllegalArgumentException("'unit' is null");
		//noinspection ConstantConditions
		if (onSuccessfulLockRunnable == null) throw new IllegalArgumentException("'onSuccessfulLock' is null");
		//noinspection ConstantConditions
		if (onFailedLockRunnable == null) throw new IllegalArgumentException("'onFailedLock' is null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock, new AtomicBoolean(true))) {
				onSuccessfulLockRunnable.run();
			}
		} else {
			onFailedLockRunnable.run();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
	 * Assuming that supplier did not throw an exception, value returned by the supplier will be returned.
	 *
	 * @param <R>                      desired type of the return value
	 * @param <T1>                      type of exception
	 * @param lock                     lock to lock on
	 * @param time                     timeout duration
	 * @param unit                     timeout timeunit
	 * @param onSuccessfulLockSupplier supplier to run while locked
	 * @return result of supplier
	 * @throws T1                    thrown if runnable has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@Nonnull Lock lock,
					@Nonnegative long time,
					@Nonnull TimeUnit unit,
					@Nonnull ThrowableSupplier<R, T1> onSuccessfulLockSupplier,
					@Nonnull ThrowableSupplier<R, T2> onFailedLockSupplier
	) throws  T1, T2, InterruptedException {

		// Ensure supplier is not null
		//noinspection ConstantConditions
		if (lock == null) throw new IllegalArgumentException("'lock' is null");
		//noinspection ConstantConditions
		if (unit == null) throw new IllegalArgumentException("'unit' is null");
		//noinspection ConstantConditions
		if (onSuccessfulLockSupplier == null) throw new IllegalArgumentException("'onSuccessfulLockSupplier' is null");
		//noinspection ConstantConditions
		if (onFailedLockSupplier == null) throw new IllegalArgumentException("'onFailedLockSupplier' is null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock, new AtomicBoolean(true))) {
				return onSuccessfulLockSupplier.get();
			}
		} else {
			return onFailedLockSupplier.get();
		}
	}

	/**
	 * Locks this AutoLock and creates AutoCloseable LockedAutoLock resource
	 *
	 * @return LockedAutoLock resource
	 */
	@Nonnull
	LockedAutoLock doLock();

	/**
	 * Locks this AutoLock and creates AutoCloseable LockedAutoLock resource
	 *
	 * @return LockedAutoLock resource
	 * @throws InterruptedException thrown when the locking process was interrupted
	 */
	@Nonnull
	LockedAutoLock doLockInterruptibly() throws InterruptedException;

	/**
	 * Returns lock state of this lock, true if locked, false if unlocked
	 *
	 * @return true if locked, false if unlocked
	 */
	boolean isLocked();

}
