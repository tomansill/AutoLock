package com.ansill.lock.autolock;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Utility class for working with {@link Lock} instances using try-with-resources semantics.
 *
 * <p>This class provides a set of convenience methods that wrap a {@link Lock} into an
 * AutoCloseable-style resource, ensuring proper unlocking even in the presence of exceptions.</p>
 *
 * <p>It also provides higher-level helper methods for executing code under lock protection
 * using either Runnable or Supplier-based APIs.</p>
 */
public final class AutoLock {

	/**
	 * Prevent instantiation.
	 */
	private AutoLock() {
		throw new UnsupportedOperationException("AutoLock is a utility class and cannot be instantiated");
	}

	/**
	 * Acquires the given lock and returns an {@link LockedAutoLock} that will release the lock
	 * when closed.
	 *
	 * <p>This method blocks until the lock is acquired.</p>
	 *
	 * @param lock the lock to acquire
	 * @return a scoped lock that will release the lock on close
	 * @throws NullPointerException if {@code lock} is null
	 */
	@NonNull
	public static LockedAutoLock lock(@NonNull Lock lock) {
		Objects.requireNonNull(lock, "lock must not be null");
		lock.lock();
		return new LockedAutoLock(lock);
	}

	/**
	 * Acquires the given lock interruptibly and returns an {@link LockedAutoLock}
	 * that will release the lock when closed.
	 *
	 * @param lock the lock to acquire
	 * @return a scoped lock that will release the lock on close
	 * @throws NullPointerException if {@code lock} is null
	 * @throws InterruptedException if the current thread is interrupted while waiting
	 */
	@NonNull
	public static LockedAutoLock lockInterruptibly(@NonNull Lock lock) throws InterruptedException {
		Objects.requireNonNull(lock, "lock must not be null");
		lock.lockInterruptibly();
		return new LockedAutoLock(lock);
	}

	/**
	 * Acquires the given lock, executes the provided action, and releases the lock.
	 *
	 * <p>The lock is held for the duration of the runnable execution.</p>
	 *
	 * @param lock     the lock to acquire
	 * @param runnable the action to execute under lock protection
	 * @param <T>      the type of exception thrown by the runnable
	 * @throws T                    if the runnable throws an exception
	 * @throws NullPointerException if {@code lock} or {@code runnable} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T extends Throwable> void lockAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T> runnable) throws T {

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
	 * Acquires the given lock, executes the supplier, releases the lock, and returns the result.
	 *
	 * @param lock     the lock to acquire
	 * @param supplier the computation to execute under lock protection
	 * @param <R>      the return type
	 * @param <T>      the exception type thrown by the supplier
	 * @return the result of the supplier
	 * @throws T                    if the supplier throws an exception
	 * @throws NullPointerException if {@code lock} or {@code supplier} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T extends Throwable> R lockAndGet(@NonNull Lock lock, @NonNull ThrowableSupplier<R, T> supplier) throws T {

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
	 * Acquires the given lock interruptibly, executes the provided action, and releases the lock.
	 *
	 * @param lock     the lock to acquire
	 * @param runnable the action to execute under lock protection
	 * @param <T>      the exception type thrown by the runnable
	 * @throws T                    if the runnable throws an exception
	 * @throws InterruptedException if interrupted while waiting for the lock
	 * @throws NullPointerException if {@code lock} or {@code runnable} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T extends Throwable> void lockInterruptiblyAndRun(
					@NonNull Lock lock,
					@NonNull ThrowableRunnable<T> runnable
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
	 * Acquires the given lock interruptibly, executes the supplier, releases the lock,
	 * and returns the result.
	 *
	 * @param lock     the lock to acquire
	 * @param supplier the computation to execute under lock protection
	 * @param <R>      the return type
	 * @param <T>      the exception type thrown by the supplier
	 * @return the result of the supplier
	 * @throws T                    if the supplier throws an exception
	 * @throws InterruptedException if interrupted while waiting for the lock
	 * @throws NullPointerException if {@code lock} or {@code supplier} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T extends Throwable> R lockInterruptiblyAndGet(
					@NonNull Lock lock,
					@NonNull ThrowableSupplier<R, T> supplier
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
	 * Attempts to acquire the lock. If successful, executes {@code onLockSuccess}
	 * while holding the lock. Otherwise, executes {@code onLockFail}.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param onLockSuccess action executed if the lock is acquired
	 * @param onLockFail    action executed if the lock is not acquired
	 * @param <T1>          exception type from success action
	 * @param <T2>          exception type from failure action
	 * @throws T1                   if the success action throws an exception
	 * @throws T2                   if the failure action throws an exception
	 * @throws NullPointerException if any argument is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2 {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock)) {
				onLockSuccess.run();
			}
		} else {
			onLockFail.run();
		}
	}

	/**
	 * Attempts to acquire the lock. If successful, executes {@code onLockSuccess}
	 * and returns its result. Otherwise, executes {@code onLockFail} and returns its result.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param onLockSuccess computation executed if lock is acquired
	 * @param onLockFail    computation executed if lock is not acquired
	 * @param <R>           return type
	 * @param <T1>          exception type from success computation
	 * @param <T2>          exception type from failure computation
	 * @return result of either computation
	 * @throws T1                   if success computation throws an exception
	 * @throws T2                   if failure computation throws an exception
	 * @throws NullPointerException if any argument is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(@NonNull Lock lock, @NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2 {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess}. Otherwise, executes {@code onLockFail}.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param timeout       maximum time to wait for the lock
	 * @param onLockSuccess action executed if lock is acquired
	 * @param onLockFail    action executed if lock is not acquired
	 * @param <T1>          exception type from success action
	 * @param <T2>          exception type from failure action
	 * @throws T1                       if success action throws an exception
	 * @throws T2                       if failure action throws an exception
	 * @throws InterruptedException     if interrupted while waiting
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if timeout is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@NonNull Lock lock,
					@NonNull Duration timeout,
					@NonNull ThrowableRunnable<T1> onLockSuccess,
					@NonNull ThrowableRunnable<T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(timeout, "timeout must not be null");
		if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock)) {
				onLockSuccess.run();
			}
		} else {
			onLockFail.run();
		}
	}


	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess} and returns its result. Otherwise, executes
	 * {@code onLockFail} and returns its result.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param timeout       maximum time to wait for the lock
	 * @param onLockSuccess computation executed if lock is acquired
	 * @param onLockFail    computation executed if lock is not acquired
	 * @param <R>           return type
	 * @param <T1>          exception type from success computation
	 * @param <T2>          exception type from failure computation
	 * @return result of either computation
	 * @throws T1                       if success computation throws an exception
	 * @throws T2                       if failure computation throws an exception
	 * @throws InterruptedException     if interrupted while waiting
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if timeout is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@NonNull Lock lock,
					@NonNull Duration timeout,
					@NonNull ThrowableSupplier<R, T1> onLockSuccess,
					@NonNull ThrowableSupplier<R, T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(timeout, "timeout must not be null");
		if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess}. Otherwise, executes {@code onLockFail}.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param time          maximum time to wait for the lock
	 * @param unit          time unit of the timeout
	 * @param onLockSuccess action executed if lock is acquired
	 * @param onLockFail    action executed if lock is not acquired
	 * @param <T1>          exception type from success action
	 * @param <T2>          exception type from failure action
	 * @throws T1                       if success action throws an exception
	 * @throws T2                       if failure action throws an exception
	 * @throws InterruptedException     if interrupted while waiting for the lock
	 * @throws NullPointerException     if {@code lock}, {@code unit}, {@code onLockSuccess}, or {@code onLockFail} is null
	 * @throws IllegalArgumentException if {@code time} is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@NonNull Lock lock,
					long time,
					@NonNull TimeUnit unit,
					@NonNull ThrowableRunnable<T1> onLockSuccess,
					@NonNull ThrowableRunnable<T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		if (time < 0) throw new IllegalArgumentException("time must be non-negative");
		Objects.requireNonNull(unit, "unit must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock)) {
				onLockSuccess.run();
			}
		} else {
			onLockFail.run();
		}
	}

	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess} and returns its result. Otherwise, executes
	 * {@code onLockFail} and returns its result.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param time          maximum time to wait for the lock
	 * @param unit          time unit of the timeout
	 * @param onLockSuccess computation executed if lock is acquired
	 * @param onLockFail    computation executed if lock is not acquired
	 * @param <R>           return type
	 * @param <T1>          exception type from success computation
	 * @param <T2>          exception type from failure computation
	 * @return result of either computation
	 * @throws T1                       if success computation throws an exception
	 * @throws T2                       if failure computation throws an exception
	 * @throws InterruptedException     if interrupted while waiting for the lock
	 * @throws NullPointerException     if {@code lock}, {@code unit}, {@code onLockSuccess}, or {@code onLockFail} is null
	 * @throws IllegalArgumentException if {@code time} is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@NonNull Lock lock,
					long time,
					@NonNull TimeUnit unit,
					@NonNull ThrowableSupplier<R, T1> onLockSuccess,
					@NonNull ThrowableSupplier<R, T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		if (time < 0) throw new IllegalArgumentException("time must be non-negative");
		Objects.requireNonNull(unit, "unit must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

}
