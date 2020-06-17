package com.ansill.lock.autolock;

import com.ansill.validation.Validation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/** AutoLock class that creates LockedAutoLock object AutoCloseable resource that can be used in Try-with-resources scope */
public interface AutoLock{

    /**
     * Creates AutoLock from lock
     *
     * @param lock lock
     * @return AutoLock
     */
    @Nonnull
    static AutoLock create(@Nonnull Lock lock){
        return new ALock(Validation.assertNonnull(lock, "lock"));
    }

    /**
     * Locks and creates a LockedAutoLock reference
     *
     * @param lock lock to lock on
     * @return LockedAutoLock
     */
    @Nonnull
    static LockedAutoLock doLock(@Nonnull Lock lock){
        return create(lock).doLock();
    }

    /**
     * Locks and creates a LockedAutoLock reference
     *
     * @param lock lock to lock on
     * @return LockedAutoLock
     * @throws InterruptedException thrown if the thread was interrupted
     */
    @Nonnull
    static LockedAutoLock doLockInterruptibly(@Nonnull Lock lock) throws InterruptedException{
        return create(lock).doLockInterruptibly();
    }

    /**
     * Locks and creates a LockedAutoLock reference
     *
     * @param lock lock to lock on
     * @return LockedAutoLock
     * @throws TimeoutException thrown if lock has failed to lock
     */
    @Nonnull
    static LockedAutoLock doTryLock(@Nonnull Lock lock) throws TimeoutException{
        return create(lock).doTryLock();
    }

    /**
     * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
     *
     * @param lock lock to lock on
     * @param time amount of time for lock to be attempted before timing out
     * @param unit unit for parameter 'time'
     * @return LockedAutoLock
     * @throws TimeoutException     thrown if lock has failed to lock
     * @throws InterruptedException thrown if the thread was interrupted
     */
    @Nonnull
    static LockedAutoLock doTryLock(@Nonnull Lock lock, long time, @Nonnull TimeUnit unit)
    throws TimeoutException, InterruptedException{
        return create(lock).doTryLock(time, unit);
    }

    /**
     * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
     *
     * @param lock     lock to lock on
     * @param duration duration of the lock attempt
     * @return LockedAutoLock
     * @throws TimeoutException     thrown if lock has failed to lock
     * @throws InterruptedException thrown if the thread was interrupted
     */
    @Nonnull
    static LockedAutoLock doTryLock(@Nonnull Lock lock, @Nonnull Duration duration)
    throws TimeoutException, InterruptedException{
        return create(lock).doTryLock(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Locks this AutoLock and creates AutoCloseable LockedAutoLock resource
     *
     * @return LockedAutoLock resource
     */
    @Nonnull
    LockedAutoLock doLock();

    /**
     * Locks this AutoLock and creates AutoCloseable LockedAutoLock resource
     *
     * @return LockedAutoLock resource
     * @throws InterruptedException thrown when the locking process was interrupted
     */
    @Nonnull
    LockedAutoLock doLockInterruptibly() throws InterruptedException;

    /**
     * Attempts to lock this AutoLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
     * thrown if the lock cannot be obtained
     *
     * @return LockedAutoLock resource
     * @throws TimeoutException thrown if the lock cannot be obtained
     */
    @Nonnull
    LockedAutoLock doTryLock() throws TimeoutException;

    /**
     * Attempts to lock this AutoLock and creates AutoCloseable LockedAutoLock resource if successful. TimeoutException will be
     * thrown if the lock cannot be obtained
     *
     * @param time timeout duration
     * @param unit timeout timeunit
     * @return LockedAutoLock resource
     * @throws TimeoutException     thrown if the lock cannot be obtained
     * @throws InterruptedException thrown when the locking process was interrupted
     */
    @Nonnull
    LockedAutoLock doTryLock(@Nonnegative long time, @Nonnull TimeUnit unit)
    throws TimeoutException, InterruptedException;

    /**
     * Attempts to lock. If lock is obtained, then a LockedAutoLock reference is created.
     *
     * @param duration duration of the lock attempt
     * @return LockedAutoLock
     * @throws TimeoutException     thrown if lock has failed to lock
     * @throws InterruptedException thrown if the thread was interrupted
     */
    @Nonnull
    LockedAutoLock doTryLock(@Nonnull Duration duration) throws TimeoutException, InterruptedException;

    /**
     * Returns lock state of this lock, true if locked, false if unlocked
     *
     * @return true if locked, false if unlocked
     */
    boolean isLocked();
}
