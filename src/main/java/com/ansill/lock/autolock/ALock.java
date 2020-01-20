package com.ansill.lock.autolock;

import com.ansill.validation.Validation;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/** AutoLock Implementation */
public class ALock implements AutoLock{

    /** Lock */
    @Nonnull
    private final Lock lock;
    /** Lock state */
    @Nonnull
    private final AtomicBoolean lock_state = new AtomicBoolean(false);

    /**
     * Creates AutoLock
     *
     * @param lock lock
     */
    public ALock(@Nonnull Lock lock){
        Validation.assertNonnull(lock, "lock");
        this.lock = lock;
    }

    /**
     * Locks and creates a LockedAutoLock reference
     *
     * @param lock lock to lock on
     * @return LockedAutoLock
     */
    @Nonnull
    public static LockedAutoLock doLock(@Nonnull Lock lock){
        Validation.assertNonnull(lock, "lock");
        return new ALock(lock).doLock();
    }

    /**
     * Locks and creates a LockedAutoLock reference
     *
     * @param lock lock to lock on
     * @return LockedAutoLock
     * @throws InterruptedException thrown if the thread was interrupted
     */
    @Nonnull
    public static LockedAutoLock doLockInterruptibly(@Nonnull Lock lock) throws InterruptedException{
        Validation.assertNonnull(lock, "lock");
        return new ALock(lock).doLockInterruptibly();
    }

    /**
     * Locks and creates a LockedAutoLock reference
     *
     * @param lock lock to lock on
     * @return LockedAutoLock
     * @throws TimeoutException thrown if lock has failed to lock
     */
    @Nonnull
    public static LockedAutoLock doTryLock(@Nonnull Lock lock) throws TimeoutException{
        Validation.assertNonnull(lock, "lock");
        return new ALock(lock).doTryLock();
    }

    /**
     * Locks and creates a LockedAutoLock reference
     *
     * @param lock lock to lock on
     * @return LockedAutoLock
     * @throws TimeoutException     thrown if lock has failed to lock
     * @throws InterruptedException thrown if the thread was interrupted
     */
    @Nonnull
    public static LockedAutoLock doTryLock(@Nonnull Lock lock, long time, @Nonnull TimeUnit unit)
    throws TimeoutException, InterruptedException{
        Validation.assertNonnull(lock, "lock");
        return new ALock(lock).doTryLock(time, unit);
    }

    @Nonnull
    @Override
    public LockedAutoLock doLock(){
        this.lock.lock();
        this.lock_state.set(true);
        return new Locked(this.lock, this.lock_state);
    }

    @Nonnull
    @Override
    public LockedAutoLock doLockInterruptibly() throws InterruptedException{
        this.lock.lockInterruptibly();
        this.lock_state.set(true);
        return new Locked(this.lock, this.lock_state);
    }

    @Nonnull
    @Override
    public LockedAutoLock doTryLock() throws TimeoutException{
        if(!this.lock.tryLock()) throw new TimeoutException("Timed out");
        this.lock_state.set(true);
        return new Locked(this.lock, this.lock_state);
    }

    @Nonnull
    @Override
    public LockedAutoLock doTryLock(long time, @Nonnull TimeUnit unit) throws TimeoutException, InterruptedException{
        if(!this.lock.tryLock(time, unit)) throw new TimeoutException("Timed out");
        this.lock_state.set(true);
        return new Locked(this.lock, this.lock_state);
    }

    @Override
    public boolean isLocked(){
        return this.lock_state.get();
    }
}
