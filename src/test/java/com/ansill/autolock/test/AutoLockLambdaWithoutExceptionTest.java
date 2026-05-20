package com.ansill.autolock.test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@DisplayName("Lambda methods (without Exception)")
class AutoLockLambdaWithoutExceptionTest extends AutoLockLambdasTest {

	@Override
	<T extends Throwable> void lockAndRun(Lock lock, ThrowableRunnable<T> runnable) throws T {
		AutoLock.lockAndRun(lock, runnable);
	}

	@Override
	<R, T extends Throwable> R lockAndGet(@NonNull Lock lock, @NonNull ThrowableSupplier<R, T> supplierWithException)
					throws T {
		return AutoLock.lockAndGet(lock, supplierWithException);
	}

	@Override
	<T extends Throwable> void lockInterruptiblyAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T> runnable)
					throws T, InterruptedException {
		AutoLock.lockInterruptiblyAndRun(lock, runnable);
	}

	@Override
	<R, T extends Throwable> R lockInterruptiblyAndGet(
					@NonNull Lock lock,
					@NonNull ThrowableSupplier<R, T> supplierWithException
	)
					throws T, InterruptedException {
		return AutoLock.lockInterruptiblyAndGet(lock, supplierWithException);
	}

	@Override
	<T extends Throwable> void tryLockAndRun(Lock lock, ThrowableRunnable<T> onSuccess, ThrowableRunnable<T> onFail) throws T {
		AutoLock.tryLockAndRun(lock, onSuccess, onFail);
	}

	@Override
	<R, T extends Throwable> R tryLockAndGet(Lock lock, ThrowableSupplier<R, T> onSuccess, ThrowableSupplier<R, T> onFail) throws T {
		return AutoLock.tryLockAndGet(lock, onSuccess, onFail);
	}

	@Override
	<T extends Throwable> void tryLockAndRun(Lock lock, long time, TimeUnit unit, ThrowableRunnable<T> onSuccess, ThrowableRunnable<T> onFail)
					throws T, InterruptedException {
		AutoLock.tryLockAndRun(lock, time, unit, onSuccess, onFail);
	}

	@Override
	<R, T extends Throwable> R tryLockAndGet(Lock lock, long time, TimeUnit unit, ThrowableSupplier<R, T> onSuccess, ThrowableSupplier<R, T> onFail)
					throws T, InterruptedException {
		return AutoLock.tryLockAndGet(lock, time, unit, onSuccess, onFail);
	}

	@Override
	<T extends Throwable> void tryLockAndRun(Lock lock, Duration timeout, ThrowableRunnable<T> onSuccess, ThrowableRunnable<T> onFail)
					throws T, InterruptedException {
		AutoLock.tryLockAndRun(lock, timeout, onSuccess, onFail);
	}

	@Override
	<R, T extends Throwable> R tryLockAndGet(Lock lock, Duration timeout, ThrowableSupplier<R, T> onSuccess, ThrowableSupplier<R, T> onFail)
					throws T, InterruptedException {
		return AutoLock.tryLockAndGet(lock, timeout, onSuccess, onFail);
	}
}
