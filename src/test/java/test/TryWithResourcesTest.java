package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.LockedAutoLock;
import com.ansill.autolock.ThrowableRunnable;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;

@SuppressWarnings("DataFlowIssue")
class TryWithResourcesTest implements LockRunTest, LockInterruptiblyRunTest {

	@Test
	@Disabled("Not applicable")
	@Override
	public void testLockRun_NullRunnable() {
		// Do nothing
	}

	@Test
	@Disabled("Not applicable")
	@Override
	public void testLockInterruptiblyRun_NullRunnable() {
		// Do nothing
	}

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		try (LockedAutoLock ignored = AutoLock.lock(lock)) {
			runnable.run();
		}
	}

	@Override
	public <T extends Throwable> void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException {
		try (LockedAutoLock ignored = AutoLock.lockInterruptibly(lock)) {
			runnable.run();
		}
	}
}
