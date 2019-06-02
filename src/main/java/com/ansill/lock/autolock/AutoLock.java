package com.ansill.lock.autolock;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** AutoLock class that creates LockedAutoLock object AutoCloseable resource that can be used in Try-with-resources scope */
public interface AutoLock{

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
     * Returns lock state of this lock, true if locked, false if unlocked
     *
     * @return true if locked, false if unlocked
     */
    boolean isLocked();
}
