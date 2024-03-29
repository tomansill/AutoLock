package com.ansill.lock.autolock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/** AutoLock Implementation */
final class AutoLockImplementation implements AutoLock{

  /** Lock */
  @Nonnull
  private final Lock lock;

  /** Lock state */
  @Nonnull
  private final AtomicBoolean lockState = new AtomicBoolean(false);

  /**
   * Creates AutoLock
   *
   * @param lock lock
   */
  AutoLockImplementation(@Nonnull Lock lock){
    this.lock = lock;
  }

  /**
   * Locks and create a LockAutoLock reference
   *
   * @return LockedAutoLock
   */
  @Nonnull
  @Override
  public LockedAutoLock doLock(){
    this.lock.lock();
    this.lockState.set(true);
    return new LockedAutoLockImplementation(this.lock, this.lockState);
  }

  /**
   * Locks and create a LockAutoLock reference
   *
   * @return LockedAutoLock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  @Nonnull
  @Override
  public LockedAutoLock doLockInterruptibly() throws InterruptedException{
    this.lock.lockInterruptibly();
    this.lockState.set(true);
    return new LockedAutoLockImplementation(this.lock, this.lockState);
  }

  /**
   * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
   *
   * @return LockedAutoLock
   * @throws TimeoutException thrown if lock has failed to lock
   */
  @Nonnull
  @Override
  public LockedAutoLock doTryLock() throws TimeoutException{
    if(!this.lock.tryLock()) throw new TimeoutException("Timed out");
    this.lockState.set(true);
    return new LockedAutoLockImplementation(this.lock, this.lockState);
  }

  /**
   * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
   *
   * @param time amount of time for lock to be attempted before timing out
   * @param unit unit for parameter 'time'
   * @return LockedAutoLock
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  @Nonnull
  @Override
  public LockedAutoLock doTryLock(@Nonnegative long time, @Nonnull TimeUnit unit)
  throws TimeoutException, InterruptedException{
    if(!this.lock.tryLock(time, unit)) throw new TimeoutException("Timed out");
    this.lockState.set(true);
    return new LockedAutoLockImplementation(this.lock, this.lockState);
  }

  /**
   * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
   *
   * @param timeout duration of the lock attempt
   * @return LockedAutoLock
   * @throws TimeoutException     thrown if lock has failed to lock
   * @throws InterruptedException thrown if the thread was interrupted
   */
  @Override
  @Nonnull
  public LockedAutoLock doTryLock(@Nonnull Duration timeout) throws TimeoutException, InterruptedException{
    //noinspection ConstantConditions
    if(timeout == null) throw new IllegalArgumentException("'timeout' is null");
    boolean something = this.lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS);
    if(!something) throw new TimeoutException("Timed out");
    this.lockState.set(true);
    return new LockedAutoLockImplementation(this.lock, this.lockState);
  }

  /**
   * Get lock's state
   *
   * @return true if lock is currently locked, false it is not currently locked
   */
  @Override
  public boolean isLocked(){
    return this.lockState.get();
  }
}
