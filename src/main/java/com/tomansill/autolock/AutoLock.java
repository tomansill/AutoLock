package com.tomansill.autolock;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/** AutoLock class that implements methods that returns an AutoCloseable Lock for convenience with try-with-resources */
public class AutoLock implements Lock{

	/** Locks */
	private final Lock[] locks;

	/**
	 * Creates an AutoLock
	 * @param locks one or many locks
	 * @throws IllegalArgumentException thrown if the value that was passed in 'locks' parameter is invalid
	 */
	public AutoLock(Lock... locks) throws IllegalArgumentException{

		// Assert not null
		if(locks == null) throw new IllegalArgumentException("'locks' input cannot be null.");

		// Lock Queue to be collected later
		Queue<AutoLock> lock_queue = new LinkedList<>();

		// Assert not null in any member
		Set<Lock> test = new HashSet<>();
		for(int i = 0; i < locks.length; i++){

			// Make sure not null
			if(locks[i] == null){
				throw new IllegalArgumentException("Lock at index " + i + " in 'locks' array is null. All locks in the array must be not null.");
			}

			// Make sure no duplicate locks (not guaranteed to work however)
			if(test.contains(locks[i])) throw new IllegalArgumentException("Duplicate lock found at index " + i + " in 'locks' array.");
			else test.add(locks[i]);

			// Add to queue if it's an AutoLock (so we can dig deeper and find any duplicates)
			if(locks[i] instanceof AutoLock) lock_queue.add((AutoLock)locks[i]);
		}

		// Test inner locks
		while(!lock_queue.isEmpty()){

			// Get it
			AutoLock auto_lock = lock_queue.poll();

			// Iterate through its locks collection
			for(Lock inner_lock : auto_lock.locks){

				// Make sure no duplicate locks (not guaranteed to work however)
				if(test.contains(inner_lock)) throw new IllegalArgumentException("Duplicate lock found deep in a lock in 'locks' array.");

				// Add to test set
				test.add(inner_lock);

				// If AutoLock, throw it in the queue
				if(inner_lock instanceof AutoLock) lock_queue.offer((AutoLock)inner_lock);
			}
		}

		// All good, assign it
		this.locks = locks;
	}

	/**
	 * Locks this AutoLock and creates AutoCloseable LockedAutoLock resource
	 * @return LockedAutoLock resource
	 */
	public LockedAutoLock doLock(){
		this.lock();
		return new LockedAutoLock(this);
	}

	/**
	 * Locks this AutoLock and creates AutoCloseable LockedAutoLock resource
	 * @return LockedAutoLock resource
	 * @throws InterruptedException thrown when the locking process was interrupted
	 */
	public LockedAutoLock doLockInterruptibly() throws InterruptedException{
		this.lockInterruptibly();
		return new LockedAutoLock(this);
	}

	/**
	 * Attempts to lock this AutoLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
	 * thrown if the lock cannot be obtained
	 * @return LockedAutoLock resource
	 * @throws TimeoutException thrown if the lock cannot be obtained
	 */
	public LockedAutoLock doTryLock() throws TimeoutException{
		if(!this.tryLock()) throw new TimeoutException("Failed to obtain lock!");
		return new LockedAutoLock(this);
	}

	/**
	 * Attempts to lock this AutoLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
	 * thrown if the lock cannot be obtained
	 * @param time timeout duration
	 * @param unit timeout timeunit
	 * @return LockedAutoLock resource
	 * @throws TimeoutException thrown if the lock cannot be obtained
	 * @throws InterruptedException thrown when the locking process was interrupted
	 */
	public LockedAutoLock doTryLock(long time, TimeUnit unit) throws TimeoutException, InterruptedException{
		if(!this.tryLock(time, unit)) throw new TimeoutException("Failed to obtain lock!");
		return new LockedAutoLock(this);
	}

	@Override
	public synchronized void lock(){

		// Keep track of successful locks
		LinkedList<Lock> locked = new LinkedList<>();

		// Successful flag
		boolean successful = false;

		// Try-and-finally in case if any one of locks fails
		try{

			// Lock the locks in succession
			for(Lock lock : this.locks){

				// Lock it
				lock.lock();

				// Add to locks (addFirst() so it'll act as FIFO when iterated)
				locked.addFirst(lock);
			}

			// Flag as successful
			successful = true;

		}finally{

			// If unsuccessful, unlock all in reverse order
			if(!successful) for(Lock lock : locked) lock.unlock();

		}
	}

	@Override
	public synchronized void lockInterruptibly() throws InterruptedException{

		// Keep track of successful locks
		LinkedList<Lock> locked = new LinkedList<>();

		// Successful flag
		boolean successful = false;

		// Try-and-finally in case if any one of locks fails
		try{

			// Lock the locks in succession
			for(Lock lock : this.locks){

				// Lock it
				lock.lockInterruptibly();

				// Add to locks (addFirst() so it'll act as FIFO when iterated)
				locked.addFirst(lock);
			}

			// Flag as successful
			successful = true;

		}finally{

			// If unsuccessful, unlock all in reverse order
			if(!successful) for(Lock lock : locked) lock.unlock();

		}
	}

	@Override
	public synchronized boolean tryLock(){

		// Keep track of successful locks
		LinkedList<Lock> locked = new LinkedList<>();

		// Successful flag
		boolean successful = false;

		// Try-and-finally in case if any one of locks fails
		try{

			// Lock the locks in succession
			for(Lock lock : this.locks){

				// Try to lock it
				if(!lock.tryLock()) return false;

				// Add to locks (addFirst() so it'll act as FIFO when iterated)
				locked.addFirst(lock);
			}

			// Flag as successful
			successful = true;

		}finally{

			// If unsuccessful, unlock all in reverse order
			if(!successful) for(Lock lock : locked) lock.unlock();

		}

		// Return success
		return true;
	}

	@Override
	public synchronized boolean tryLock(long time, TimeUnit unit) throws InterruptedException{

		// Keep track of successful locks
		LinkedList<Lock> locked = new LinkedList<>();

		// Successful flag
		boolean successful = false;

		// Try-and-finally in case if any one of locks fails
		try{

			// Get start time
			long start_millis = System.currentTimeMillis();

			// Convert input timeout to millis
			long timeout_remaining = TimeUnit.MILLISECONDS.convert(time, unit);

			// Lock the locks in succession
			for(Lock lock : this.locks){

				// Try to lock it
				if(timeout_remaining < 0 || !lock.tryLock(timeout_remaining, TimeUnit.MILLISECONDS)) return false;

				// Count down the time
				timeout_remaining -= System.currentTimeMillis() - start_millis;
				start_millis = System.currentTimeMillis();

				// Add to locks (addFirst() so it'll act as FIFO when iterated)
				locked.addFirst(lock);
			}

			// Flag as successful
			successful = true;

		}finally{

			// If unsuccessful, unlock all in reverse order
			if(!successful) for(Lock lock : locked) lock.unlock();

		}

		// Return success
		return true;
	}

	@Override
	public synchronized void unlock(){
		for(Lock lock : this.locks) lock.unlock();
	}

	@Override
	public Condition newCondition(){
		throw new UnsupportedOperationException();
	}

}
