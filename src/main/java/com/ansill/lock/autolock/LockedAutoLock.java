package com.ansill.lock.autolock;

/** Locked Resource that will unlock when close() was called. Ideal for use in Try-with-resources scope. */
public interface LockedAutoLock extends AutoCloseable{

  /** Unlocks the locked lock */
  void unlock();

  @Override
  default void close(){
    this.unlock();
  }
}
