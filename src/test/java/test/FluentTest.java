package test;

import com.ansill.autolock.AutoLock;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.locks.Lock;

public class FluentTest implements AutoLockTest.LockRunTest, AutoLockTest.LockInterruptiblyTest {

	@Override
	public void performLockAndRun(@NonNull Lock lock, @NonNull Runnable runnable) {
		AutoLock.with(lock).run(runnable::run);
	}

	@Override
	public void performLockInterruptiblyAndRun(@NonNull Lock lock, @NonNull Runnable runnable) throws InterruptedException {
		AutoLock.with(lock).interruptibly().run(runnable::run);
	}
}
