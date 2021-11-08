package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.ThrowableRunnable;
import com.ansill.lock.autolock.ThrowableSupplier;
import org.junit.jupiter.api.DisplayName;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

@DisplayName("Lambda methods (without Exception)")
class AutoLockLambdaWithoutExceptionTest extends AutoLockLambdasTest{

  @Override
  <T extends Throwable> void lockAndRun(Lock lock, ThrowableRunnable<T> runnable) throws T{
    AutoLock.lockAndRun(lock, runnable);
  }

  @Override
  <R, T extends Throwable> R lockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R,T> supplierWithException)
  throws T{
    return AutoLock.lockAndGet(lock, supplierWithException);
  }

  @Override
  <T extends Throwable> void lockInterruptiblyAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T> runnable)
  throws T, InterruptedException{
    AutoLock.lockInterruptiblyAndRun(lock, runnable);
  }

  @Override
  <R, T extends Throwable> R lockInterruptiblyAndGet(
    @Nonnull Lock lock,
    @Nonnull ThrowableSupplier<R,T> supplierWithException
  )
  throws T, InterruptedException{
    return AutoLock.lockInterruptiblyAndGet(lock, supplierWithException);
  }

  @Override
  <T extends Throwable> void tryLockAndRun(Lock lock, ThrowableRunnable<T> runnable) throws T, TimeoutException{
    AutoLock.tryLockAndRun(lock, runnable);
  }

  @Override
  <R, T extends Throwable> R tryLockAndGet(Lock lock, ThrowableSupplier<R,T> supplier) throws T, TimeoutException{
    return AutoLock.tryLockAndGet(lock, supplier);
  }

  @Override
  <T extends Throwable> void tryLockAndRun(Lock lock, long time, TimeUnit unit, ThrowableRunnable<T> runnable)
  throws T, InterruptedException, TimeoutException{
    AutoLock.tryLockAndRun(lock, time, unit, runnable);
  }

  @Override
  <R, T extends Throwable> R tryLockAndGet(Lock lock, long time, TimeUnit unit, ThrowableSupplier<R,T> supplier)
  throws T, InterruptedException, TimeoutException{
    return AutoLock.tryLockAndGet(lock, time, unit, supplier);
  }

  @Override
  <T extends Throwable> void tryLockAndRun(Lock lock, Duration timeout, ThrowableRunnable<T> runnable)
  throws T, InterruptedException, TimeoutException{
    AutoLock.tryLockAndRun(lock, timeout, runnable);
  }

  @Override
  <R, T extends Throwable> R tryLockAndGet(Lock lock, Duration timeout, ThrowableSupplier<R,T> supplier)
  throws T, InterruptedException, TimeoutException{
    return AutoLock.tryLockAndGet(lock, timeout, supplier);
  }
}
