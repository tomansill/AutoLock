package com.ansill.lock.autolock;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Non thread safe object.
 *
 * @param <T> the type parameter
 */
public final class NonThreadSafeObject<T>{

  @Nonnull
  private final AtomicBoolean modifying = new AtomicBoolean(false);

  @Nonnull
  private final AtomicBoolean corrupted = new AtomicBoolean(false);

  private T value;

  /**
   * Instantiates a new Non thread safe object.
   *
   * @param initialValue the initial value
   */
  public NonThreadSafeObject(T initialValue){
    this.value = initialValue;
  }

  /**
   * Is corrupted boolean.
   *
   * @return the boolean
   */
  public boolean isCorrupted(){
    return corrupted.get();
  }

  /**
   * Modify.
   *
   * @param newValue the new value
   */
  public void modify(T newValue){

    // If already corrupted, do not do any further changes
    if(corrupted.get()) return;

    try{
      // Set modify flag, if flag is already true, then consider data corrupted
      if(modifying.getAndSet(true)){
        corrupted.set(true);
        return;
      }

      // Change value
      value = newValue;

    }finally{
      modifying.set(false);
    }
  }

  /**
   * Modify.
   *
   * @param newValue the new value
   * @param start    the start
   * @param cdl      the cdl
   */
  public void modify(T newValue, @Nonnull CountDownLatch start, @Nonnull CountDownLatch cdl){

    try{
      // Set modify flag, if flag is already true, then consider data corrupted
      if(modifying.getAndSet(true)){
        corrupted.set(true);
        return;
      }

      // Tick the start
      start.countDown();

      // Simulate the slow execution time
      cdl.await();

      // If already corrupted, do not do any further changes
      if(corrupted.get()) return;

      // Change value
      value = newValue;

    }catch(InterruptedException e){
      throw new RuntimeException(e);
    }finally{
      modifying.set(false);
    }
  }

  /**
   * Modify.
   *
   * @param newValue      the new value
   * @param start         the start
   * @param executionTime the execution time
   */
  public void modify(T newValue, @Nonnull CountDownLatch start, @Nonnull Duration executionTime){

    try{
      // Set modify flag, if flag is already true, then consider data corrupted
      if(modifying.getAndSet(true)){
        corrupted.set(true);
        return;
      }

      // Tick the start
      start.countDown();

      // Simulate the slow execution time
      Thread.sleep(executionTime.toMillis());

      // If already corrupted, do not do any further changes
      if(corrupted.get()) return;

      // Change value
      value = newValue;

    }catch(InterruptedException e){
      throw new RuntimeException(e);
    }finally{
      modifying.set(false);
    }
  }

  /**
   * Get value t.
   *
   * @return the t
   */
  public T getValue(){
    return value;
  }

}
