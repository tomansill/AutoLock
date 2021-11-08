package com.ansill.lock.autolock;

import com.ansill.validation.Validation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/** AutoLock class that creates LockedAutoLock object AutoCloseable resource that can be used in Try-with-resources scope */
public interface AutoLock{

  /**
   * Creates AutoLock from lock
   *
   * @param lock lock
   * @return AutoLock auto lock
   */
  @Nonnull
  static AutoLock create(@Nonnull Lock lock){
    return new AutoLockImplementation(Validation.assertNonnull(lock, "lock"));
  }

  /**
   * Locks and creates a LockedAutoLock reference
   *
   * @param lock lock to lock on
   * @return LockedAutoLock locked auto lock
   */
  @Nonnull
  static LockedAutoLock doLock(@Nonnull Lock lock){
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
  static LockedAutoLock doLockInterruptibly(@Nonnull Lock lock) throws InterruptedException{
    return create(lock).doLockInterruptibly();
  }

  /**
   * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
   *
   * @param lock lock to lock on
   * @return LockedAutoLock locked auto lock
   * @throws TimeoutException thrown if lock has failed to lock
   */
  @Nonnull
  static LockedAutoLock doTryLock(@Nonnull Lock lock) throws TimeoutException{
    return create(lock).doTryLock();
  }

  /**
   * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
   *
   * @param lock lock to lock on
   * @param time amount of time for lock to be attempted before timing out
   * @param unit unit for parameter 'time'
   * @return LockedAutoLock locked auto lock
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  @Nonnull
  static LockedAutoLock doTryLock(@Nonnull Lock lock, long time, @Nonnull TimeUnit unit)
  throws TimeoutException, InterruptedException{
    return create(lock).doTryLock(time, unit);
  }

  /**
   * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
   *
   * @param lock    lock to lock on
   * @param timeout duration of the lock attempt
   * @return LockedAutoLock locked auto lock
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  @Nonnull
  static LockedAutoLock doTryLock(@Nonnull Lock lock, @Nonnull Duration timeout)
  throws TimeoutException, InterruptedException{
    return create(lock).doTryLock(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Locks the provided lock, runs the runnable during the lock, unlocks afterwards
   *
   * @param <T>      type of exception
   * @param lock     lock to lock on
   * @param runnable runnable to run while locked
   * @throws T thrown if runnable has thrown an exception
   */
  static <T extends Throwable> void lockAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T> runnable) throws T{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doLock(lock)){

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
  static <R, T extends Throwable> R lockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R,T> supplier) throws T{

    // Ensure runnable is not null
    Validation.assertNonnull(supplier, "supplier");

    // Lock it
    try(LockedAutoLock ignored = doLock(lock)){

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
  ) throws T, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doLockInterruptibly(lock)){

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
    @Nonnull ThrowableSupplier<R,T> supplier
  )
  throws T, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(supplier, "supplier");

    // Lock it
    try(LockedAutoLock ignored = doLockInterruptibly(lock)){

      // Get it
      return supplier.get();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
   *
   * @param <T>      type of exception
   * @param lock     lock to lock on
   * @param runnable runnable to run while locked
   * @throws T                thrown if runnable has thrown an exception
   * @throws TimeoutException thrown if lock has failed to lock
   */
  static <T extends Throwable> void tryLockAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T> runnable)
  throws T, TimeoutException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock)){

      // Run it
      runnable.run();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
   * Assuming that supplier did not throw an exception, value returned by the supplier will be returned.
   *
   * @param <R>      desired type of the return value
   * @param <T>      type of exception
   * @param lock     lock to lock on
   * @param supplier supplier to run while locked
   * @return result of supplier
   * @throws T                thrown if runnable has thrown an exception
   * @throws TimeoutException thrown if lock has failed to lock
   */
  static <R, T extends Throwable> R tryLockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R,T> supplier)
  throws T, TimeoutException{

    // Ensure runnable is not null
    Validation.assertNonnull(supplier, "supplier");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock)){

      // Get it
      return supplier.get();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
   *
   * @param <T>      type of exception
   * @param lock     lock to lock on
   * @param timeout  duration of the lock attempt
   * @param runnable runnable to run while locked
   * @throws T                    thrown if runnable has thrown an exception
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static <T extends Throwable> void tryLockAndRun(
    @Nonnull Lock lock,
    @Nonnull Duration timeout,
    @Nonnull ThrowableRunnable<T> runnable
  ) throws T, TimeoutException, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock, timeout)){

      // Run it
      runnable.run();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
   * Assuming that supplier did not throw an exception, value returned by the supplier will be returned.
   *
   * @param <R>      desired type of the return value
   * @param <T>      type of exception
   * @param lock     lock to lock on
   * @param timeout  duration of the lock attempt
   * @param supplier supplier to run while locked
   * @return result of supplier
   * @throws T                    thrown if runnable has thrown an exception
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static <R, T extends Throwable> R tryLockAndGet(
    @Nonnull Lock lock,
    @Nonnull Duration timeout,
    @Nonnull ThrowableSupplier<R,T> supplier
  ) throws T, TimeoutException, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(supplier, "supplier");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock, timeout)){

      // Get it
      return supplier.get();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
   *
   * @param <T>      type of exception
   * @param lock     lock to lock on
   * @param time     timeout duration
   * @param unit     timeout timeunit
   * @param runnable runnable to run while locked
   * @throws T                    thrown if runnable has thrown an exception
   * @throws TimeoutException     thrown if the lock cannot be obtained
   * @throws InterruptedException thrown when the locking process was interrupted
   */
  static <T extends Throwable> void tryLockAndRun(
    @Nonnull Lock lock,
    @Nonnegative long time,
    @Nonnull TimeUnit unit,
    @Nonnull ThrowableRunnable<T> runnable
  ) throws T, TimeoutException, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock, time, unit)){

      // Run it
      runnable.run();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
   * Assuming that supplier did not throw an exception, value returned by the supplier will be returned.
   *
   * @param <R>      desired type of the return value
   * @param <T>      type of exception
   * @param lock     lock to lock on
   * @param time     timeout duration
   * @param unit     timeout timeunit
   * @param supplier supplier to run while locked
   * @return result of supplier
   * @throws T                    thrown if runnable has thrown an exception
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static <R, T extends Throwable> R tryLockAndGet(
    @Nonnull Lock lock,
    @Nonnegative long time,
    @Nonnull TimeUnit unit,
    @Nonnull ThrowableSupplier<R,T> supplier
  ) throws T, TimeoutException, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(supplier, "supplier");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock, time, unit)){

      // Get it
      return supplier.get();
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
   * Attempts to lock this AutoLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
   * thrown if the lock cannot be obtained
   *
   * @return LockedAutoLock resource
   * @throws TimeoutException thrown if the lock cannot be obtained
   */
  @Nonnull
  LockedAutoLock doTryLock() throws TimeoutException;

  /**
   * Attempts to lock this AutoLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
   * thrown if the lock cannot be obtained
   *
   * @param time timeout duration
   * @param unit timeout timeunit
   * @return LockedAutoLock resource
   * @throws TimeoutException     thrown if the lock cannot be obtained
   * @throws InterruptedException thrown when the locking process was interrupted
   */
  @Nonnull
  LockedAutoLock doTryLock(@Nonnegative long time, @Nonnull TimeUnit unit)
  throws TimeoutException, InterruptedException;

  /**
   * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
   *
   * @param timeout duration of the lock attempt
   * @return LockedAutoLock locked auto lock
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  @Nonnull
  LockedAutoLock doTryLock(@Nonnull Duration timeout) throws TimeoutException, InterruptedException;

  /**
   * Returns lock state of this lock, true if locked, false if unlocked
   *
   * @return true if locked, false if unlocked
   */
  boolean isLocked();

}
