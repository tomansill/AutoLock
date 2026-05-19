package com.ansill.lock.autolock;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/**
 * AutoLock Implementation
 */
final class AutoLockImplementation implements AutoLock {

	/**
	 * Lock
	 */
	@Nonnull
	private final Lock lock;

	/**
	 * Lock state
	 */
	@Nonnull
	private final AtomicBoolean lockState = new AtomicBoolean(false);

	/**
	 * Creates AutoLock
	 *
	 * @param lock lock
	 */
	AutoLockImplementation(@Nonnull Lock lock) {
		this.lock = lock;
	}

	/**
	 * Locks and create a LockAutoLock reference
	 *
	 * @return LockedAutoLock
	 */
	@Nonnull
	@Override
	public LockedAutoLock doLock() {
		this.lock.lock();
		this.lockState.set(true);
		return new LockedAutoLockImplementation(this.lock, this.lockState);
	}

	/**
	 * Locks and create a LockAutoLock reference
	 *
	 * @return LockedAutoLock
	 * @throws InterruptedException thrown if the thread was interrupted
	 */
	@Nonnull
	@Override
	public LockedAutoLock doLockInterruptibly() throws InterruptedException {
		this.lock.lockInterruptibly();
		this.lockState.set(true);
		return new LockedAutoLockImplementation(this.lock, this.lockState);
	}

	/**
	 * Get lock's state
	 *
	 * @return true if lock is currently locked, false it is not currently locked
	 */
	@Override
	public boolean isLocked() {
		return this.lockState.get();
	}
}
