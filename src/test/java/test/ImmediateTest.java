package test;

import com.ansill.autolock.AutoLock;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("DataFlowIssue")
public class ImmediateTest implements AutoLockTest.LockRunTest, AutoLockTest.LockGetTest, AutoLockTest.LockInterruptiblyTest {

	@DisplayName("test lockAndRun with null lock")
	@Test
	void testLockAndRunNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> AutoLock.lockAndRun(null, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("test lockAndGet with null lock")
	@Test
	void testLockAndGetNullLock() {
		NullPointerException exception = assertThrows(NullPointerException.class, () -> AutoLock.lockAndGet(null, Assertions::fail));
		assertEquals("lock must not be null", exception.getMessage());
	}

	@DisplayName("test lockAndRun with null runnable")
	@Test
	void testLockAndRunNullRunnable() {
		StubbedLock stubbedLock = new StubbedLock();
		NullPointerException exception = assertThrows(NullPointerException.class, () -> AutoLock.lockAndRun(stubbedLock, null));
		assertEquals("runnable must not be null", exception.getMessage());
	}

	@DisplayName("test lockAndGet with null runnable")
	@Test
	void testLockAndGetNullRunnable() {
		StubbedLock stubbedLock = new StubbedLock();
		NullPointerException exception = assertThrows(NullPointerException.class, () -> AutoLock.lockAndGet(stubbedLock, null));
		assertEquals("supplier must not be null", exception.getMessage());
	}

	@Override
	public void performLockAndRun(@NonNull Lock lock, @NonNull Runnable runnable) {
		AutoLock.lockAndRun(lock, runnable::run);
	}

	@Override
	public void performLockInterruptiblyAndRun(@NonNull Lock lock, @NonNull Runnable runnable) throws InterruptedException {
		AutoLock.lockInterruptiblyAndRun(lock, runnable::run);
	}

	@Override
	public <Return> Return performLockAndGet(@NonNull Lock lock, @NonNull Supplier<Return> supplier) {
		return AutoLock.lockAndGet(lock, supplier::get);
	}
}
