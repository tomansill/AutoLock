package com.ansill.lock.autolock;

/**
 * Runnable that throws a throwable
 *
 * @param <T> throwable
 */
@FunctionalInterface
public interface ThrowableRunnable<T extends Throwable>{

  /**
   * Runs the runnable
   *
   * @throws T throws exception
   */
  void run() throws T;

}
