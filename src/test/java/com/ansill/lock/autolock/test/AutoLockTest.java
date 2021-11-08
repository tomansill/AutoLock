package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.NonThreadSafeObject;
import org.junit.jupiter.api.function.Executable;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

interface AutoLockTest{

  Random RNG = new SecureRandom();

  Duration EXECUTION_TIME = Duration.ofMillis(10);

  static Supplier<Runnable> beforeLock(ReentrantLock lock){

    // Create thread-sensitive object
    NonThreadSafeObject<Integer> object = new NonThreadSafeObject<>(RNG.nextInt());

    // Desired numbers
    int desiredNumberOne = object.getValue() + 1;
    int desiredNumberTwo = desiredNumberOne + 1;

    // Return supplier
    return () -> duringLock(lock, object, desiredNumberOne, desiredNumberTwo);
  }

  static Runnable duringLock(
    ReentrantLock lock,
    NonThreadSafeObject<Integer> object,
    Integer desiredNumberOne,
    Integer desiredNumberTwo
  ){

    // Assert that lock is locked
    assertTrue(lock.isLocked());

    // Set up CDLs to control execution
    CountDownLatch startCDL = new CountDownLatch(1);

    // Create thread
    Thread thread = new Thread(() -> {

      // Old-fashioned lock
      try{

        // Wait for a okay from main thread
        startCDL.await();

        // Lock it
        lock.lock();

        // Run what we need to do
        object.modify(desiredNumberTwo);

      }catch(InterruptedException e){
        throw new RuntimeException(e);
      }finally{

        // Unlock it
        lock.unlock();
      }

    });

    // Run it
    thread.start();

    // Attempt to modify object
    object.modify(desiredNumberOne, startCDL, EXECUTION_TIME);

    // Ensure not corrupted
    assertFalse(object.isCorrupted());

    // Check value
    assertEquals(desiredNumberOne, object.getValue());

    // Assert that lock is locked
    assertTrue(lock.isLocked());

    // Build runnable to run afterwards
    return () -> {

      // Wait for other thread to finish
      assertDoesNotThrow((Executable) thread::join);

      // Ensure not corrupted
      assertFalse(object.isCorrupted());

      // Check value
      assertEquals(desiredNumberTwo, object.getValue());

      // Assert that lock is unlocked
      assertFalse(lock.isLocked());
    };
  }

}