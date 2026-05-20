package com.ansill.autolock;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.locks.Lock;

/**
 * A scope-bound lock holder that releases an underlying {@link java.util.concurrent.locks.Lock}
 * when closed.
 *
 * <p>This class is intended for use with try-with-resources to ensure that locks
 * are reliably released:</p>
 *
 * <pre>{@code
 * try (LockedAutoLock ignored = AutoLock.lock(mutex)) {
 *     // critical section
 * }
 * }</pre>
 *
 * <p>When {@link #close()} is invoked, this instance delegates directly to
 * {@link Lock#unlock()} on the underlying lock.</p>
 *
 * <p><b>Usage contract:</b> Each instance must be closed exactly once. Violations
 * of this contract (such as double-closing or closing from a non-owning thread)
 * will result in the underlying {@link Lock} throwing an exception, typically
 * {@link IllegalMonitorStateException}.</p>
 *
 * <p>This class does not attempt to track or guard against misuse; it is a thin,
 * zero-overhead wrapper over {@link Lock} intended to provide structured locking
 * via try-with-resources.</p>
 *
 * <p>Instances are created internally by {@link AutoLock} and are not intended
 * for external construction or subclassing.</p>
 */
public final class LockedAutoLock implements AutoCloseable {

	/**
	 * The underlying lock being managed by this scope.
	 */
	@NonNull
	private final Lock lock;

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
	 * Releases the underlying lock.
	 *
	 * <p>This method is automatically invoked by try-with-resources. It directly
	 * calls {@link Lock#unlock()} on the underlying lock.</p>
	 *
	 * @throws IllegalMonitorStateException if the current thread does not hold
	 *         the lock or if the lock has already been released
	 */
	@Override
	public void close() {
		this.lock.unlock();
	}
}
