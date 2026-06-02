package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.locks.Lock;

@SuppressWarnings("DataFlowIssue")
public class ImmediateTest implements AutoLockTest.LockRunTest, AutoLockTest.LockGetTest, AutoLockTest.LockInterruptiblyRunTest, AutoLockTest.LockInterruptiblyGetTest, AutoLockTest.TryLockInstantRunTest, AutoLockTest.TryLockInstantGetTest {

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		AutoLock.lockAndRun(lock, runnable);
	}

	@Override
	public <T extends Throwable> void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException {
		AutoLock.lockInterruptiblyAndRun(lock, runnable);
	}

	@Override
	public <Return, T extends Throwable> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T, InterruptedException {
		return AutoLock.lockInterruptiblyAndGet(lock, supplier);
	}

	@Override
	public <Return, T extends Throwable> Return performLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T {
		return AutoLock.lockAndGet(lock, supplier);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws T1, T2 {
		AutoLock.tryLockAndRun(lock, onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws T1, T2 {
		return AutoLock.tryLockAndGet(lock, onLockSuccess, onLockFail);
	}
}
