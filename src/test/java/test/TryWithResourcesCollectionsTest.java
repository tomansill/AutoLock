package test;

import com.ansill.autolock.AutoLock;
import com.ansill.autolock.LockedAutoLock;
import com.ansill.autolock.ThrowableRunnable;
import com.ansill.autolock.ThrowableSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

@SuppressWarnings({"DataFlowIssue", "try"})
class TryWithResourcesCollectionsTest implements LockRunTest, LockInterruptiblyRunTest, MultiLockRunTest, MultiLockGetTest, MultiLockInterruptiblyRunTest, MultiLockInterruptiblyGetTest {

	@Override
	public <T extends Throwable> void performLockAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T {
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(runnable, "runnable must not be null");
		try (LockedAutoLock ignored = AutoLock.lock(Collections.singleton(lock))) {
			runnable.run();
		}
	}

	@Override
	public <T extends Throwable> void performLockInterruptiblyAndRun(@Nullable Lock lock, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException {
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(runnable, "runnable must not be null");
		try (LockedAutoLock ignored = AutoLock.lockInterruptibly(Collections.singleton(lock))) {
			runnable.run();
		}
	}

	@Override
	public <Return, T extends Throwable> Return performLockAndGet(@Nullable Lock[] locks, @Nullable ThrowableSupplier<Return, T> supplier) throws T {
		try (LockedAutoLock ignored = AutoLock.lock(Arrays.asList(locks))) {
			return supplier.get();
		}
	}

	@Override
	public <Return, T extends Throwable> Return performLockInterruptiblyAndGet(@NonNull Lock[] locks, @Nullable ThrowableSupplier<Return, T> supplier) throws T, InterruptedException {
		try (LockedAutoLock ignored = AutoLock.lockInterruptibly(Arrays.asList(locks))) {
			return supplier.get();
		}
	}

	@Override
	public <T extends Throwable> void performLockAndRun(@NonNull Lock[] locks, @Nullable ThrowableRunnable<T> runnable) throws T {
		try (LockedAutoLock ignored = AutoLock.lock(Arrays.asList(locks))) {
			runnable.run();
		}
	}

	@Override
	public <T extends Throwable> void performLockInterruptiblyAndRun(@NonNull Lock[] locks, @Nullable ThrowableRunnable<T> runnable) throws T, InterruptedException {
		try (LockedAutoLock ignored = AutoLock.lockInterruptibly(Arrays.asList(locks))) {
			runnable.run();
		}
	}
}
