package com.ansill.lock.autolock;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

/** LockedAutoLock implementation */
public class Locked implements LockedAutoLock{

    /** Lock object */
    @Nonnull
    private final Lock lock;

    /** Lock state */
    @Nonnull
    private final AtomicBoolean lock_state;

    /**
     * Creates locked resource
     *
     * @param lock       lock
     * @param lock_state lock state
     */
    Locked(@Nonnull Lock lock, @Nonnull AtomicBoolean lock_state){
        this.lock = lock;
        this.lock_state = lock_state;
    }

    @Override
    public void unlock(){
        if(this.lock_state.compareAndSet(true, false)) this.lock.unlock();
    }
}
