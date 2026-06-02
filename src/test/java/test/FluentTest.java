package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.ThrowableRunnable;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class FluentTest implements AutoLockTest.LockRunTest, AutoLockTest.LockInterruptiblyRunTest, AutoLockTest.LockInterruptiblyGetTest {

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		AutoLock.with(lock).run(runnable); // Cheat a bit
	}

	@Override
	public void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable Runnable runnable) throws InterruptedException {
		AutoLock.with(lock).interruptibly().run(runnable == null ? null : runnable::run); // Cheat a bit
	}

	@Override
	public <Return> Return performLockInterruptiblyAndGet(@Nullable Lock lock, @Nullable Supplier<Return> supplier) throws InterruptedException {
		return AutoLock.with(lock).interruptibly().get(supplier == null ? null : supplier::get); // Cheat a bit
	}
}
