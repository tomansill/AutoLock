package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@SuppressWarnings("DataFlowIssue")
public class FluentTest implements
				AutoLockTest.LockRunTest,
				AutoLockTest.LockGetTest,
				AutoLockTest.LockInterruptiblyRunTest,
				AutoLockTest.LockInterruptiblyGetTest,
				AutoLockTest.TryLockInstantRunTest,
				AutoLockTest.TryLockInstantGetTest,
				AutoLockTest.TryLockTimeoutRunTest {

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		AutoLock.with(lock).run(runnable);
	}

	@Override
	public <T extends Throwable> void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException {
		AutoLock.with(lock).interruptibly().run(runnable);
	}

	@Override
	public <Return, T extends Throwable> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T, InterruptedException {
		return AutoLock.with(lock).interruptibly().get(supplier);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws T1, T2 {
		AutoLock.with(lock).tryAcquire().run(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T1 extends Throwable, T2 extends Throwable> Return performTryLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T1> onLockSuccess, @Nullable ThrowableSupplier<Return, T2> onLockFail) throws T1, T2 {
		return AutoLock.with(lock).tryAcquire().get(onLockSuccess, onLockFail);
	}

	@Override
	public <Return, T extends Throwable> Return performLockAndGet(@Nullable Lock lock, @Nullable ThrowableSupplier<Return, T> supplier) throws T {
		return AutoLock.with(lock).get(supplier);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunLongAndTimeUnit(@Nullable Lock lock, long time, @Nullable TimeUnit unit, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
		AutoLock.with(lock).tryAcquire(time, unit).run(onLockSuccess, onLockFail);
	}

	@Override
	public <T1 extends Throwable, T2 extends Throwable> void performTryLockAndRunDuration(@Nullable Lock lock, @Nullable Duration duration, @Nullable ThrowableRunnable<T1> onLockSuccess, @Nullable ThrowableRunnable<T2> onLockFail) throws InterruptedException, T1, T2 {
		AutoLock.with(lock).tryAcquire(duration).run(onLockSuccess, onLockFail);
	}
}
