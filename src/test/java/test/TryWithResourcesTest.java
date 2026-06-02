package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.LockedAutoLock;
import com.ansill.autolock.ThrowableRunnable;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;

@SuppressWarnings("DataFlowIssue")
class TryWithResourcesTest implements AutoLockTest.LockRunTest, AutoLockTest.LockInterruptiblyRunTest {

	@Test
	@Disabled("Not applicable")
	@Override
	public void testLockRun_NullRunnable() {
		// Do nothing
	}

	@Test
	@Disabled("Not applicable")
	@Override
	public void testNullRunnableOnLockInterruptiblyRun() {
		// Do nothing
	}

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		try (LockedAutoLock ignored = AutoLock.lock(lock)) {
			runnable.run();
		}
	}

	@Override
	public void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable Runnable runnable) throws InterruptedException {
		try (LockedAutoLock ignored = AutoLock.lockInterruptibly(lock)) {
			runnable.run();
		}
	}
}
