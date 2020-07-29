package com.ansill.lock.autolock;

import com.ansill.utility.function.RunnableWithException;
import com.ansill.utility.function.SupplierWithException;
import com.ansill.validation.Validation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/** AutoLock class that creates LockedAutoLock object AutoCloseable resource that can be used in Try-with-resources scope */
public interface AutoLock{

  /**
   * Creates AutoLock from lock
   *
   * @param lock lock
   * @return AutoLock
   */
  @Nonnull
  static AutoLock create(@Nonnull Lock lock){
    return new ALock(Validation.assertNonnull(lock, "lock"));
  }

  /**
   * Locks and creates a LockedAutoLock reference
   *
   * @param lock lock to lock on
   * @return LockedAutoLock
   */
  @Nonnull
  static LockedAutoLock doLock(@Nonnull Lock lock){
    return create(lock).doLock();
  }

  /**
   * Locks and creates a LockedAutoLock reference
   *
   * @param lock lock to lock on
   * @return LockedAutoLock
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
   * @return LockedAutoLock
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
   * @return LockedAutoLock
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
   * @return LockedAutoLock
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  @Nonnull
  static LockedAutoLock doTryLock(@Nonnull Lock lock, @Nonnull Duration timeout)
  throws TimeoutException, InterruptedException{
    return create(lock).doTryLock(timeout.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Locks the provided lock, performs the runnable during the lock, unlocks afterwards
   *
   * @param lock     lock to lock on
   * @param runnable runnable to run while locked
   */
  static void lockAndPerform(@Nonnull Lock lock, @Nonnull Runnable runnable){

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doLock(lock)){

      // Perform it
      runnable.run();
    }
  }

  /**
   * Locks the provided lock, performs the supplier during the lock, unlocks afterwards and, assuming if supplier does
   * not return an exception, value returned by the supplier will be returned
   *
   * @param lock     lock to lock on
   * @param supplier supplier to run while locked
   * @param <T>      desired type of the return value
   * @return result of supplier
   */
  static <T> T lockAndGet(@Nonnull Lock lock, @Nonnull Supplier<T> supplier){

    // Ensure runnable is not null
    Validation.assertNonnull(supplier, "supplier");

    // Lock it
    try(LockedAutoLock ignored = doLock(lock)){

      // Get it
      return supplier.get();
    }
  }

  /**
   * Locks the provided lock, performs the runnable during the lock, unlocks afterwards
   *
   * @param lock     lock to lock on
   * @param runnable runnable to run while locked
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static void lockInterruptiblyAndPerform(@Nonnull Lock lock, @Nonnull Runnable runnable) throws InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doLockInterruptibly(lock)){

      // Perform it
      runnable.run();
    }
  }

  /**
   * Locks the provided lock, performs the supplier during the lock, unlocks afterwards and, assuming if supplier does
   * not return an exception, value returned by the supplier will be returned
   *
   * @param lock     lock to lock on
   * @param supplier supplier to run while locked
   * @param <T>      desired type of the return value
   * @return result of supplier
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static <T> T lockInterruptiblyAndGet(@Nonnull Lock lock, @Nonnull Supplier<T> supplier)
  throws InterruptedException{

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
   * @param lock     lock to lock on
   * @param runnable runnable to run while locked
   * @throws TimeoutException thrown if lock has failed to lock
   */
  static void tryLockAndPerform(@Nonnull Lock lock, @Nonnull Runnable runnable) throws TimeoutException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock)){

      // Perform it
      runnable.run();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
   * Assuming that supplier did not thrown an exception, value returned by the supplier will be returned.
   *
   * @param lock     lock to lock on
   * @param supplier supplier to run while locked
   * @param <T>      desired type of the return value
   * @return result of supplier
   * @throws TimeoutException thrown if lock has failed to lock
   */
  static <T> T tryLockAndGet(@Nonnull Lock lock, @Nonnull Supplier<T> supplier) throws TimeoutException{

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
   * @param lock     lock to lock on
   * @param timeout  duration of the lock attempt
   * @param runnable runnable to run while locked
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static void tryLockAndPerform(
    @Nonnull Lock lock,
    @Nonnull Duration timeout,
    @Nonnull Runnable runnable
  ) throws TimeoutException, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock, timeout)){

      // Perform it
      runnable.run();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
   * Assuming that supplier did not thrown an exception, value returned by the supplier will be returned.
   *
   * @param lock     lock to lock on
   * @param timeout  duration of the lock attempt
   * @param supplier supplier to run while locked
   * @param <T>      desired type of the return value
   * @return result of supplier
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static <T> T tryLockAndGet(
    @Nonnull Lock lock,
    @Nonnull Duration timeout,
    @Nonnull Supplier<T> supplier
  ) throws TimeoutException, InterruptedException{

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
   * @param lock     lock to lock on
   * @param time     timeout duration
   * @param unit     timeout timeunit
   * @param runnable runnable to run while locked
   * @throws TimeoutException     thrown if the lock cannot be obtained
   * @throws InterruptedException thrown when the locking process was interrupted
   */
  static void tryLockAndPerform(
    @Nonnull Lock lock,
    @Nonnegative long time,
    @Nonnull TimeUnit unit,
    @Nonnull Runnable runnable
  ) throws TimeoutException, InterruptedException{

    // Ensure runnable is not null
    Validation.assertNonnull(runnable, "runnable");

    // Lock it
    try(LockedAutoLock ignored = doTryLock(lock, time, unit)){

      // Perform it
      runnable.run();
    }
  }

  /**
   * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
   * Assuming that supplier did not thrown an exception, value returned by the supplier will be returned.
   *
   * @param lock     lock to lock on
   * @param time     timeout duration
   * @param unit     timeout timeunit
   * @param supplier supplier to run while locked
   * @param <T>      desired type of the return value
   * @return result of supplier
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  static <T> T tryLockAndGet(
    @Nonnull Lock lock,
    @Nonnegative long time,
    @Nonnull TimeUnit unit,
    @Nonnull Supplier<T> supplier
  ) throws TimeoutException, InterruptedException{

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
   * @return LockedAutoLock
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

  /** A variant of Autolock's lockAndPerform and lockAndGet functions to include runnables and suppliers with exception */
  final class Ex{

    /* Bad things will happen if you try to instantiate this utility class */
    private Ex(){
      throw new AssertionError(this.getClass().getSimpleName() + " is an utility class. It cannot be instantiated.");
    }

    /**
     * Locks the provided lock, performs the runnable during the lock, unlocks afterwards
     *
     * @param lock     lock to lock on
     * @param runnable runnable to run while locked
     * @throws ExecutionException thrown if invocation of runnable function has thrown an exception
     */
    public static void lockAndPerform(@Nonnull Lock lock, @Nonnull RunnableWithException runnable)
    throws ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(runnable, "runnable");

      // Lock it
      try(LockedAutoLock ignored = doLock(lock)){

        // Perform it
        try{
          runnable.run();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Locks the provided lock, performs the supplier during the lock, unlocks afterwards and, assuming if supplier does
     * not return an exception, value returned by the supplier will be returned
     *
     * @param lock     lock to lock on
     * @param supplier supplier to run while locked
     * @param <T>      desired type of the return value
     * @return result of supplier
     * @throws ExecutionException thrown if invocation of supplier function has thrown an exception
     */
    public static <T> T lockAndGet(@Nonnull Lock lock, @Nonnull SupplierWithException<T> supplier)
    throws ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(supplier, "supplier");

      // Lock it
      try(LockedAutoLock ignored = doLock(lock)){

        // Get it
        try{
          return supplier.get();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Locks the provided lock, performs the runnable during the lock, unlocks afterwards
     *
     * @param lock     lock to lock on
     * @param runnable runnable to run while locked
     * @throws InterruptedException thrown if the thread was interrupted
     * @throws ExecutionException   thrown if invocation of runnable function has thrown an exception
     */
    public static void lockInterruptiblyAndPerform(@Nonnull Lock lock, @Nonnull RunnableWithException runnable)
    throws InterruptedException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(runnable, "runnable");

      // Lock it
      try(LockedAutoLock ignored = doLockInterruptibly(lock)){

        // Perform it
        try{
          runnable.run();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Locks the provided lock, performs the supplier during the lock, unlocks afterwards and, assuming if supplier does
     * not return an exception, value returned by the supplier will be returned
     *
     * @param lock     lock to lock on
     * @param supplier supplier to run while locked
     * @param <T>      desired type of the return value
     * @return result of supplier
     * @throws InterruptedException thrown if the thread was interrupted
     * @throws ExecutionException   thrown if invocation of supplier function has thrown an exception
     */
    public static <T> T lockInterruptiblyAndGet(@Nonnull Lock lock, @Nonnull SupplierWithException<T> supplier)
    throws InterruptedException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(supplier, "supplier");

      // Lock it
      try(LockedAutoLock ignored = doLockInterruptibly(lock)){

        // Get it
        try{
          return supplier.get();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
     *
     * @param lock     lock to lock on
     * @param runnable runnable to run while locked
     * @throws TimeoutException   thrown if lock has failed to lock
     * @throws ExecutionException thrown if invocation of runnable function has thrown an exception
     */
    public static void tryLockAndPerform(@Nonnull Lock lock, @Nonnull RunnableWithException runnable)
    throws TimeoutException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(runnable, "runnable");

      // Lock it
      try(LockedAutoLock ignored = doTryLock(lock)){

        // Perform it
        try{
          runnable.run();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
     * Assuming that supplier did not thrown an exception, value returned by the supplier will be returned.
     *
     * @param lock     lock to lock on
     * @param supplier supplier to run while locked
     * @param <T>      desired type of the return value
     * @return result of supplier
     * @throws TimeoutException   thrown if lock has failed to lock
     * @throws ExecutionException thrown if invocation of supplier function has thrown an exception
     */
    public static <T> T tryLockAndGet(@Nonnull Lock lock, @Nonnull SupplierWithException<T> supplier)
    throws TimeoutException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(supplier, "supplier");

      // Lock it
      try(LockedAutoLock ignored = doTryLock(lock)){

        // Get it
        try{
          return supplier.get();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
     *
     * @param lock     lock to lock on
     * @param timeout  duration of the lock attempt
     * @param runnable runnable to run while locked
     * @throws TimeoutException     thrown if lock has failed to lock
     * @throws InterruptedException thrown if the thread was interrupted
     * @throws ExecutionException   thrown if invocation of runnable function has thrown an exception
     */
    public static void tryLockAndPerform(
      @Nonnull Lock lock,
      @Nonnull Duration timeout,
      @Nonnull RunnableWithException runnable
    ) throws TimeoutException, InterruptedException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(runnable, "runnable");

      // Lock it
      try(LockedAutoLock ignored = doTryLock(lock, timeout)){

        // Perform it
        try{
          runnable.run();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
     * Assuming that supplier did not thrown an exception, value returned by the supplier will be returned.
     *
     * @param lock     lock to lock on
     * @param timeout  duration of the lock attempt
     * @param supplier supplier to run while locked
     * @param <T>      desired type of the return value
     * @return result of supplier
     * @throws TimeoutException     thrown if lock has failed to lock
     * @throws InterruptedException thrown if the thread was interrupted
     * @throws ExecutionException   thrown if invocation of supplier function has thrown an exception
     */
    public static <T> T tryLockAndGet(
      @Nonnull Lock lock,
      @Nonnull Duration timeout,
      @Nonnull SupplierWithException<T> supplier
    ) throws TimeoutException, InterruptedException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(supplier, "supplier");

      // Lock it
      try(LockedAutoLock ignored = doTryLock(lock, timeout)){

        // Get it
        try{
          return supplier.get();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Attempts to lock. If lock is obtained, then the runnable will be run. After running the runnable, lock is unlocked.
     *
     * @param lock     lock to lock on
     * @param time     timeout duration
     * @param unit     timeout timeunit
     * @param runnable runnable to run while locked
     * @throws TimeoutException     thrown if the lock cannot be obtained
     * @throws InterruptedException thrown when the locking process was interrupted
     * @throws ExecutionException   thrown if invocation of runnable function has thrown an exception
     */
    public static void tryLockAndPerform(
      @Nonnull Lock lock,
      @Nonnegative long time,
      @Nonnull TimeUnit unit,
      @Nonnull RunnableWithException runnable
    ) throws TimeoutException, InterruptedException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(runnable, "runnable");

      // Lock it
      try(LockedAutoLock ignored = doTryLock(lock, time, unit)){

        // Perform it
        try{
          runnable.run();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }

    /**
     * Attempts to lock. If lock is obtained, then the supplier will be run. After running the supplier, lock is unlocked.
     * Assuming that supplier did not thrown an exception, value returned by the supplier will be returned.
     *
     * @param lock     lock to lock on
     * @param time     timeout duration
     * @param unit     timeout timeunit
     * @param supplier supplier to run while locked
     * @param <T>      desired type of the return value
     * @return result of supplier
     * @throws TimeoutException     thrown if lock has failed to lock
     * @throws InterruptedException thrown if the thread was interrupted
     * @throws ExecutionException   thrown if invocation of supplier function has thrown an exception
     */
    public static <T> T tryLockAndGet(
      @Nonnull Lock lock,
      @Nonnegative long time,
      @Nonnull TimeUnit unit,
      @Nonnull SupplierWithException<T> supplier
    ) throws TimeoutException, InterruptedException, ExecutionException{

      // Ensure runnable is not null
      Validation.assertNonnull(supplier, "supplier");

      // Lock it
      try(LockedAutoLock ignored = doTryLock(lock, time, unit)){

        // Get it
        try{
          return supplier.get();
        }catch(Exception exception){
          throw new ExecutionException(exception);
        }
      }
    }
  }
}
