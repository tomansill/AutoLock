package com.tomansill.autolock;

/**
 * Locked Resource that will unlock when close() was called. Ideal for use in Try-with-resources scope.
 */
public class LockedAutoLock implements AutoCloseable{

	/** Lock */
	private final AutoLock lock;

	/**
	 * Creates Locked Resource
	 * @param lock lock that was locked
	 */
	LockedAutoLock(AutoLock lock){
		this.lock = lock;
	}

	@Override
	public void close(){
		this.lock.unlock();
	}
}
