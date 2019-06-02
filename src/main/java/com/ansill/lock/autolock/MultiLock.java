package com.ansill.lock.autolock;

import com.ansill.validation.Validation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/** MultiLock class that handles multiple locks */
public class MultiLock implements Lock, AutoLock{

    /** Locks */
    private final Lock[] locks;

    /** Lock state */
    private AtomicBoolean lock_state = new AtomicBoolean(false);

    /**
     * Creates an MultiLock
     *
     * @param locks one or many locks
     * @throws IllegalArgumentException thrown if the value that was passed in 'locks' parameter is invalid
     */
    public MultiLock(@Nonnull Lock... locks) throws IllegalArgumentException{

        // Assert not null
        Validation.assertNonnullElements(locks, false);

        // Lock Queue to be collected later
        Queue<MultiLock> lock_queue = new LinkedList<>();

        // Assert not null in any member
        Set<Lock> test = new HashSet<>();
        for(int i = 0; i < locks.length; i++){

            // Make sure not null
            if(locks[i] == null){
                String message = "Lock at index " + i + " in 'locks' array is null.";
                message += " All locks in the array must be not null.";
                throw new IllegalArgumentException(message);
            }

            // Make sure no duplicate locks (not guaranteed to work however)
            if(test.contains(locks[i])){
                String message = "Duplicate lock found at index " + i + " in 'locks' array.";
                throw new IllegalArgumentException(message);
            }

            // Add it to the list
            else test.add(locks[i]);

            // Add to queue if it's an MultiLock (so we can dig deeper and find any duplicates)
            if(locks[i] instanceof MultiLock) lock_queue.add((MultiLock) locks[i]);
        }

        // Test inner locks
        while(!lock_queue.isEmpty()){

            // Get it
            MultiLock auto_lock = lock_queue.poll();

            // Iterate through its locks collection
            for(Lock inner_lock : auto_lock.locks){

                // Make sure no duplicate locks (not guaranteed to work however)
                if(test.contains(inner_lock)){
                    String message = "Duplicate lock found deep in a lock in 'locks' array.";
                    throw new IllegalArgumentException(message);
                }

                // Add to test set
                test.add(inner_lock);

                // If MultiLock, throw it in the queue
                if(inner_lock instanceof MultiLock) lock_queue.offer((MultiLock) inner_lock);
            }
        }

        // All good, assign it
        this.locks = locks;
    }

    /**
     * Locks this MultiLock and creates AutoCloseable LockedAutoLock resource
     *
     * @return LockedAutoLock resource
     */
    @Nonnull
    public LockedAutoLock doLock(){
        this.lock();
        this.lock_state.set(true);
        return new Locked(this, this.lock_state);
    }

    /**
     * Locks this MultiLock and creates AutoCloseable LockedAutoLock resource
     *
     * @return LockedAutoLock resource
     * @throws InterruptedException thrown when the locking process was interrupted
     */
    @Nonnull
    public LockedAutoLock doLockInterruptibly() throws InterruptedException{
        this.lockInterruptibly();
        this.lock_state.set(true);
        return new Locked(this, this.lock_state);
    }

    /**
     * Attempts to lock this MultiLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
     * thrown if the lock cannot be obtained
     *
     * @return LockedAutoLock resource
     * @throws TimeoutException thrown if the lock cannot be obtained
     */
    @Nonnull
    public LockedAutoLock doTryLock() throws TimeoutException{
        if(!this.tryLock()) throw new TimeoutException("Failed to obtain lock!");
        this.lock_state.set(true);
        return new Locked(this, this.lock_state);
    }

    /**
     * Attempts to lock this MultiLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
     * thrown if the lock cannot be obtained
     *
     * @param time timeout duration
     * @param unit timeout timeunit
     * @return LockedAutoLock resource
     * @throws TimeoutException     thrown if the lock cannot be obtained
     * @throws InterruptedException thrown when the locking process was interrupted
     */
    @Nonnull
    public LockedAutoLock doTryLock(@Nonnegative long time, @Nonnull TimeUnit unit)
    throws TimeoutException, InterruptedException{

        // Assert parameters
        Validation.assertNonnegative(time, "time");
        Validation.assertNonnull(unit, "unit");

        // Do it
        if(!this.tryLock(time, unit)) throw new TimeoutException("Failed to obtain lock!");

        // Set state
        this.lock_state.set(true);

        // Return new LockedAutoLock
        return new Locked(this, this.lock_state);
    }

    @Override
    public boolean isLocked(){
        return this.lock_state.get();
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

            // Successful
            if(successful) this.lock_state.set(true);

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

            // Successful
            if(successful) this.lock_state.set(true);

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

            // Successful
            if(successful) this.lock_state.set(true);

        }

        // Return success
        return true;
    }

    @Override
    public synchronized boolean tryLock(@Nonnegative long time, @Nonnull TimeUnit unit) throws InterruptedException{

        // Assert parameters
        Validation.assertNonnegative(time, "time");
        Validation.assertNonnull(unit, "unit");

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

            // Successful
            if(successful) this.lock_state.set(true);

        }

        // Return success
        return true;
    }

    @Override
    public synchronized void unlock(){
        for(Lock lock : this.locks) lock.unlock();
        this.lock_state.set(false);
    }

    @Override
    @Nonnull
    public Condition newCondition(){
        throw new UnsupportedOperationException();
    }

}
