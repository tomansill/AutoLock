package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.LockedAutoLock;
import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DataFlowIssue")
class TryWithResourcesTest implements LockRunTest, LockInterruptiblyRunTest, MultiLockGetTest {

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

	@Test
	@Disabled("Not applicable")
	@Override
	public void testMultiLockGet_NullLocks() {

	}

	@DisplayName("multi-lock-get: with null rest")
	@Test
	void testMultiLockGet_NullRest() {
		try (StubbedLock lock1 = new StubbedLock(); StubbedLock lock2 = new StubbedLock()) {
			NullPointerException exception = assertThrows(NullPointerException.class, () -> {
				try (LockedAutoLock ignored = AutoLock.lock(lock1, lock2, (Lock[]) null)) {
					fail();
				}
			});
			assertEquals("locks must not be null", exception.getMessage());
		}
	}

	@Override
	public <Return, T extends Throwable> Return performLockAndGet(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T> supplier) throws T {
		Lock lock1 = locks[0];
		Lock lock2 = locks[1];
		Lock[] rest = locks.length == 2 ? new Lock[0] : Arrays.copyOfRange(locks, 2, locks.length);
		try (LockedAutoLock ignored = AutoLock.lock(lock1, lock2, rest)) {
			return supplier.get();
		}
	}
}
