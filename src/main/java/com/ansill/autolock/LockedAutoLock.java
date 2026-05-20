package com.ansill.autolock;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * A scope-bound lock holder that releases an underlying {@link java.util.concurrent.locks.Lock}
 * when closed.
 *
 * <p>This class is intended exclusively for use with try-with-resources:</p>
 *
 * <pre>{@code
 * try (LockedAutoLock lock = AutoLock.lock(mutex)) {
 *     // critical section
 * }
 * }</pre>
 *
 * <p>The lock is released exactly once when {@link #close()} is invoked.</p>
 *
 * <p>This implementation is <b>idempotent and safe for multiple calls to {@code close()}</b>.
 * Only the first invocation will release the underlying lock; subsequent calls are no-ops.</p>
 *
 * <p>Instances of this class are created internally by {@link AutoLock} and are not intended
 * for external construction or subclassing.</p>
 */
public final class LockedAutoLock implements AutoCloseable {

	/**
	 * The underlying lock being managed by this scope.
	 */
	@NonNull
	private final Lock lock;

	/**
	 * Indicates whether the lock has already been released.
	 *
	 * <p>This ensures that {@link #close()} is idempotent.</p>
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * Creates a new scoped lock wrapper.
	 *
	 * <p>This constructor is package-private and should only be used by {@link AutoLock}.</p>
	 *
	 * @param lock the lock to manage
	 * @throws NullPointerException if {@code lock} is null
	 */
	LockedAutoLock(@NonNull Lock lock) {
		this.lock = lock;
	}

	/**
	 * Releases the underlying lock if it has not already been released.
	 *
	 * <p>This method is idempotent: multiple calls have no additional effect.</p>
	 *
	 * <p>This method is automatically invoked by try-with-resources.</p>
	 */
	@Override
	public void close() {
		if (this.closed.compareAndSet(false, true)) {
			this.lock.unlock();
		}
	}
}
