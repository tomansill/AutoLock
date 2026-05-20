package com.ansill.lock.autolock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * AutoLock class that creates LockedAutoLock object AutoCloseable resource that can be used in Try-with-resources scope
 */
public interface AutoLock {

	/**
	 * Locks and creates a LockedAutoLock reference
	 *
	 * @param lock lock to lock on
	 * @return LockedAutoLock locked auto lock
	 */
	@Nonnull
	static LockedAutoLock lock(@Nonnull Lock lock) {
		Objects.requireNonNull(lock, "lock must not be null");
		lock.lock();
		return new LockedAutoLockImplementation(lock);
	}

	/**
	 * Locks and creates a LockedAutoLock reference
	 *
	 * @param lock lock to lock on
	 * @return LockedAutoLock locked auto lock
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	@Nonnull
	static LockedAutoLock lockInterruptibly(@Nonnull Lock lock) throws InterruptedException {
		Objects.requireNonNull(lock, "lock must not be null");
		lock.lockInterruptibly();
		return new LockedAutoLockImplementation(lock);
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

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(runnable, "runnable must not be null");

		// Lock it
		try (LockedAutoLock ignored = lock(lock)) {

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

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(supplier, "supplier must not be null");

		// Lock it
		try (LockedAutoLock ignored = lock(lock)) {

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

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(runnable, "runnable must not be null");

		// Lock it
		try (LockedAutoLock ignored = lockInterruptibly(lock)) {

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
	) throws T, InterruptedException {

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(supplier, "supplier must not be null");

		// Lock it
		try (LockedAutoLock ignored = lockInterruptibly(lock)) {

			// Get it
			return supplier.get();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
	 *
	 * @param <T1>          type of exception to be thrown inside onLockSuccess runnable
	 * @param <T2>          type of exception to be thrown inside onLockFail runnable
	 * @param lock          lock to lock on
	 * @param onLockSuccess runnable to run while locked
	 * @param onLockFail    runnable to run when lock did not succeed
	 * @throws T1 thrown if onLockSuccess runnable has thrown an exception
	 * @throws T2 thrown if onLockFail runnable has thrown an exception
	 */
	static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T1> onLockSuccess, @Nonnull ThrowableRunnable<T2> onLockFail) throws T1, T2 {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock)) {
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
	 * @param <R>           desired type of the return value
	 * @param <T1>          type of exception to be thrown inside onLockSuccess supplier
	 * @param <T2>          type of exception to be thrown inside onLockFail supplier
	 * @param lock          lock to lock on
	 * @param onLockSuccess supplier to run while locked
	 * @param onLockFail    supplier to run when lock did not succeed
	 * @return result of supplier
	 * @throws T1 thrown if onLockSuccess supplier has thrown an exception
	 * @throws T2 thrown if onLockFail supplier has thrown an exception
	 */
	static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R, T1> onLockSuccess, @Nonnull ThrowableSupplier<R, T2> onLockFail) throws T1, T2 {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
	 *
	 * @param <T1>          type of exception to be thrown inside onLockSuccess runnable
	 * @param <T2>          type of exception to be thrown inside onLockFail runnable
	 * @param lock          lock to lock on
	 * @param timeout       duration of the lock attempt
	 * @param onLockSuccess runnable to run while locked
	 * @param onLockFail    runnable to run if lock was not acquired
	 * @throws T1                   thrown if onLockSuccess runnable has thrown an exception
	 * @throws T2                   thrown if onLockFail runnable has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@Nonnull Lock lock,
					@Nonnull Duration timeout,
					@Nonnull ThrowableRunnable<T1> onLockSuccess,
					@Nonnull ThrowableRunnable<T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(timeout, "timeout must not be null");
		if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock)) {
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
	 * @param <R>           desired type of the return value
	 * @param <T1>          type of exception to be thrown inside onLockSuccess supplier
	 * @param <T2>          type of exception to be thrown inside onLockFail supplier
	 * @param lock          lock to lock on
	 * @param timeout       duration of the lock attempt
	 * @param onLockSuccess supplier to run while locked
	 * @param onLockFail    supplier to run when lock fails to be acquired
	 * @return result of either supplier
	 * @throws T1                   thrown if onLockSuccess supplier has thrown an exception
	 * @throws T2                   thrown if onLockFail supplier has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@Nonnull Lock lock,
					@Nonnull Duration timeout,
					@Nonnull ThrowableSupplier<R, T1> onLockSuccess,
					@Nonnull ThrowableSupplier<R, T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(timeout, "timeout must not be null");
		if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

	/**
	 * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
	 *
	 * @param <T1>          type of exception to be thrown inside onLockSuccess runnable
	 * @param <T2>          type of exception to be thrown inside onLockFail runnable
	 * @param lock          lock to lock on
	 * @param time          timeout duration
	 * @param unit          timeout timeunit
	 * @param onLockSuccess runnable to run while locked
	 * @param onLockFail    runnable to run if lock was not acquired
	 * @throws T1                   thrown if onLockSuccess runnable has thrown an exception
	 * @throws T2                   thrown if onLockFail runnable has thrown an exception
	 * @throws InterruptedException thrown when the locking process was interrupted
	 */
	static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@Nonnull Lock lock,
					@Nonnegative long time,
					@Nonnull TimeUnit unit,
					@Nonnull ThrowableRunnable<T1> onLockSuccess,
					@Nonnull ThrowableRunnable<T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		//noinspection ConstantValue
		if (time < 0) throw new IllegalArgumentException("time must be non-negative");
		Objects.requireNonNull(unit, "unit must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock)) {
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
	 * @param <R>           desired type of the return value
	 * @param <T1>          type of exception to be thrown inside onLockSuccess supplier
	 * @param <T2>          type of exception to be thrown inside onLockFail supplier
	 * @param lock          lock to lock on
	 * @param time          timeout duration
	 * @param unit          timeout timeunit
	 * @param onLockSuccess supplier to run while locked
	 * @param onLockFail    supplier to run if lock was not acquired
	 * @return result of supplier
	 * @throws T1                   thrown if onLockSuccess supplier has thrown an exception
	 * @throws T2                   thrown if onLockFail supplier has thrown an exception
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@Nonnull Lock lock,
					@Nonnegative long time,
					@Nonnull TimeUnit unit,
					@Nonnull ThrowableSupplier<R, T1> onLockSuccess,
					@Nonnull ThrowableSupplier<R, T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		//noinspection ConstantValue
		if (time < 0) throw new IllegalArgumentException("time must be non-negative");
		Objects.requireNonNull(unit, "unit must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLockImplementation(lock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

}
