package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class ImmediateTest implements AutoLockTest.LockRunTest, AutoLockTest.LockGetTest, AutoLockTest.LockInterruptiblyRunTest, AutoLockTest.LockInterruptiblyGetTest, AutoLockTest.TryLockInstantTest {

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		AutoLock.lockAndRun(lock, runnable);
	}

	@Override
	public void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable Runnable runnable) throws InterruptedException {
		AutoLock.lockInterruptiblyAndRun(lock, runnable == null ? null : runnable::run); // Cheat a bit
	}

	@Override
	public <Return> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable Supplier<Return> supplier) throws InterruptedException {
		return AutoLock.lockInterruptiblyAndGet(lock, supplier == null ? null : supplier::get); // Cheat a bit
	}

	@Override
	public <Return, T extends Throwable> Return performLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T {
		return AutoLock.lockAndGet(lock, supplier);
	}

	@Override
	public void performTryLockAndRun(@Nullable Lock lock, @Nullable Runnable onLockSuccess, @Nullable Runnable onLockFail) {
		AutoLock.tryLockAndRun(lock, onLockSuccess == null ? null : onLockSuccess::run, onLockFail == null ? null : onLockFail::run); // Cheat a bit
	}

	@Override
	public <Return> Return performTryLockAndGet(@Nullable Lock lock, @Nullable Supplier<Return> onLockSuccess, @Nullable Supplier<Return> onLockFail) {
		return AutoLock.tryLockAndGet(lock, onLockSuccess == null ? null : onLockSuccess::get, onLockFail == null ? null : onLockFail::get); // Cheat a bit
	}
}
