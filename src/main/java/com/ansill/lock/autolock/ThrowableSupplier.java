package com.ansill.lock.autolock;

import javax.annotation.Nonnull;

/**
 * Supplier that throws a throwable
 *
 * @param <R> value that this supplier supplies
 * @param <T> throwable
 */
@FunctionalInterface
public interface ThrowableSupplier<R, T extends Throwable>{

  /**
   * Supplies the value
   *
   * @return value r
   * @throws T thrown if there are any exceptions thrown while running the supplier
   */
  @Nonnull
  R get() throws T;

}
