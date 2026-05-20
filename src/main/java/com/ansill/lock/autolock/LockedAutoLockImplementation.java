package com.ansill.lock.autolock;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.Lock;

/**
 * LockedAutoLock implementation
 */
final class LockedAutoLockImplementation implements LockedAutoLock {

	/**
	 * Lock object
	 */
	@Nonnull
	private final Lock lock;

	/**
	 * Closed state
	 */
	private boolean closed;

	/**
	 * Creates locked resource
	 *
	 * @param lock lock
	 */
	LockedAutoLockImplementation(@Nonnull Lock lock) {
		this.lock = lock;
		this.closed = false;
	}

	@Override
	public void unlock() {
		if (!closed) {
			closed = true;
			this.lock.unlock();
		}
	}
}
