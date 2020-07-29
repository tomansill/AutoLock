package com.ansill.lock.autolock;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/** LockedAutoLock implementation */
final class Locked implements LockedAutoLock{

  /** Lock object */
  @Nonnull
  private final Lock lock;

  /** Lock state */
  @Nonnull
  private final AtomicBoolean lockState;

  /**
   * Creates locked resource
   *
   * @param lock      lock
   * @param lockState lock state
   */
  Locked(@Nonnull Lock lock, @Nonnull AtomicBoolean lockState){
    this.lock = lock;
    this.lockState = lockState;
  }

  @Override
  public void unlock(){
    if(this.lockState.compareAndSet(true, false)) this.lock.unlock();
  }
}
