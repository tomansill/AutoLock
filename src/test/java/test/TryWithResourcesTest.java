package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.LockedAutoLock;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"DataFlowIssue", "resource"})
class TryWithResourcesTest implements AutoLockTest.LockRunTest, AutoLockTest.LockInterruptiblyTest {

	@DisplayName("test with null lock")
	@Test
	void testNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> AutoLock.lock(null));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@Override
	public void performLockAndRun(@NonNull Lock lock, @NonNull Runnable runnable) {
		try (LockedAutoLock ignored = AutoLock.lock(lock)) {
			runnable.run();
		}
	}

	@Override
	public void performLockInterruptiblyAndRun(@NonNull Lock lock, @NonNull Runnable runnable) throws InterruptedException {
		try (LockedAutoLock ignored = AutoLock.lockInterruptibly(lock)) {
			runnable.run();
		}
	}
}
