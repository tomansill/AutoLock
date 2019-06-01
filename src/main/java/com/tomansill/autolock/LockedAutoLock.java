package com.tomansill.autolock;

import javax.annotation.Nonnull;

/**
 * Locked Resource that will unlock when close() was called. Ideal for use in Try-with-resources scope.
 */
public class LockedAutoLock implements AutoCloseable{

    /** Lock */
    @Nonnull
    private final AutoLock lock;

    /**
     * Creates Locked Resource
     *
     * @param lock lock that was locked
     */
    LockedAutoLock(@Nonnull AutoLock lock){
        this.lock = lock;
    }

    @Override
    public void close(){
        this.lock.unlock();
    }
}
