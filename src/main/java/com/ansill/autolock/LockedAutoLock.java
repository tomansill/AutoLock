package com.ansill.autolock;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.locks.Lock;

/**
 * A scope-bound resource that executes a release action when closed.
 *
 * <p>This class is primarily used to represent acquired lock ownership in a
 * try-with-resources block, ensuring that locks are reliably released:</p>
 *
 * <pre>{@code
 * try (LockedAutoLock ignored = AutoLock.lock(mutex)) {
 *     // critical section
 * }
 * }</pre>
 *
 * <p>When {@link #close()} is invoked, this instance executes an internal
 * release action, typically unlocking one or more {@link Lock} instances.</p>
 *
 * <p><b>Usage contract:</b> Each instance must be closed exactly once. Violations
 * of this contract (such as double-closing or closing from a non-owning thread)
 * may result in exceptions thrown by the underlying lock implementation,
 * commonly {@link IllegalMonitorStateException}.</p>
 *
 * <p>This class does not enforce ownership or idempotency guarantees; it is a
 * minimal wrapper intended to support structured locking patterns.</p>
 *
 * <p>Instances are created internally by {@link AutoLock} and are not intended
 * for external construction or subclassing.</p>
 */
public final class LockedAutoLock implements AutoCloseable {

	/**
	 * Runnable to run on close()
	 */
	@NonNull
	private final Runnable onClose;

	/**
	 * Constructor with onClose runnable
	 * @param onClose runnable to run when close() is called
	 */
	LockedAutoLock(@NonNull Runnable onClose) {
		this.onClose = onClose;
	}

	/**
	 * Releases the underlying lock.
	 *
	 * <p>This method is automatically invoked by try-with-resources. It directly
	 * calls {@link Lock#unlock()} on the underlying lock.</p>
	 *
	 * @throws IllegalMonitorStateException if the current thread does not hold
	 *                                      the lock or if the lock has already been released
	 */
	@Override
	public void close() {
		onClose.run();
	}
}
