package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.LockedAutoLock;
import com.ansill.lock.autolock.NonThreadSafeObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class AutoLockTest{

  private final static Random RNG = new SecureRandom();

  private final static Duration EXECUTION_TIME = Duration.ofMillis(10);

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

        // Perform what we need to do
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

  @DisplayName("Instance Methods")
  static class InstanceMethods{

    @DisplayName("Attempt to successfully perform doLock() method")
    @Test
    void testAutoLockDoLock(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      Runnable afterLock;

      // Lock it
      try(LockedAutoLock ignored = al.doLock()){

        // Do during-thread test and get post lock runnable
        afterLock = duringThread.get();
      }

      // Check if it's unlocked
      assertFalse(al.isLocked());

      // Do after-lock test
      afterLock.run();
    }

    @DisplayName("Attempt to successfully perform doLockInterruptibly() method")
    @Test
    void testAutoLockDoLockInterruptibly() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      Runnable afterLock;

      // Lock it
      try(LockedAutoLock ignored = al.doLockInterruptibly()){

        // Check if its locked
        assertTrue(al.isLocked());

        // Do during-thread test and get post lock runnable
        afterLock = duringThread.get();
      }

      // Check if it's unlocked
      assertFalse(al.isLocked());

      // Do after-lock test
      afterLock.run();
    }

    @DisplayName("Attempt to successfully perform doTryLock() method")
    @Test
    void testAutoLockDoTryLock() throws TimeoutException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      Runnable afterLock;

      // Lock it
      try(LockedAutoLock ignored = al.doTryLock()){

        // Check if its locked
        assertTrue(al.isLocked());

        // Do during-thread test and get post lock runnable
        afterLock = duringThread.get();
      }

      // Check if it's unlocked
      assertFalse(al.isLocked());

      // Do after-lock test
      afterLock.run();
    }

    @DisplayName("Attempt to successfully perform doTryLock(long,TimeUnit) method")
    @Test
    void testAutoLockDoTryLockWithLongTimeUnitTimeout(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> {
        try(LockedAutoLock ignored = al.doTryLock(1, TimeUnit.MINUTES)){

          // Check if its locked
          assertTrue(al.isLocked());

          // Do during-thread test and get post lock runnable
          afterLock.set(duringThread.get());
        }
      });

      // Check if it's unlocked
      assertFalse(al.isLocked());

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform doTryLock(Duration) method")
    @Test
    void testAutoLockDoTryLockWithDurationTimeout(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> {
        try(LockedAutoLock ignored = al.doTryLock(Duration.ofMinutes(1))){

          // Check if its locked
          assertTrue(al.isLocked());

          // Do during-thread test and get post lock runnable
          afterLock.set(duringThread.get());
        }
      });

      // Check if it's unlocked
      assertFalse(al.isLocked());

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to perform doLockInterruptibly() method and call interrupt")
    @Test
    void testAutoLockDoLockInterruptiblyInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        try(LockedAutoLock ignored = al.doLockInterruptibly()){
          fail("Lock obtained");
        }catch(InterruptedException e){
          success.set(true);
        }

      });

      // Start thread
      thread.start();

      // Wait a tiny bit
      Thread.sleep(EXECUTION_TIME.toMillis());

      // Interrupt
      thread.interrupt();

      // Wait for thread to join
      thread.join();

      // Ensure that lock has been interrupted
      assertTrue(success.get());

    }

    @DisplayName("Attempt to perform doTryLock(long,TimeUnit) method and call interrupt")
    @Test
    void testAutoLockDoTryLockTimeUnitInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        // Ensure doTryLock doesn't take too long
        assertTimeout(Duration.ofSeconds(1), () -> {

          // Do it
          try(LockedAutoLock ignored = al.doTryLock(1, TimeUnit.MINUTES)){
            fail("Lock obtained");
          }catch(InterruptedException e){
            success.set(true);
          }catch(TimeoutException e){
            fail("Timed out");
          }
        });
      });

      // Start thread
      thread.start();

      // Wait a tiny bit
      Thread.sleep(EXECUTION_TIME.toMillis());

      // Interrupt
      thread.interrupt();

      // Wait for thread to join
      thread.join();

      // Ensure that lock has been interrupted
      assertTrue(success.get());

    }

    @DisplayName("Attempt to perform doTryLock(Duration) method and call interrupt")
    @Test
    void testAutoLockDoTryLockDurationInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        // Ensure doTryLock doesn't take too long
        assertTimeout(Duration.ofSeconds(1), () -> {

          // Do it
          try(LockedAutoLock ignored = al.doTryLock(Duration.ofMinutes(1))){
            fail("Lock obtained");
          }catch(InterruptedException e){
            success.set(true);
          }catch(TimeoutException e){
            fail("Timed out");
          }
        });
      });

      // Start thread
      thread.start();

      // Wait a tiny bit
      Thread.sleep(EXECUTION_TIME.toMillis());

      // Interrupt
      thread.interrupt();

      // Wait for thread to join
      thread.join();

      // Ensure that lock has been interrupted
      assertTrue(success.get());

    }

    @DisplayName("Attempt to perform doTryLock() method and force it to time out")
    @Test
    void testAutoLockDoTryLockTimeout() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Go-ahead CDL
      CountDownLatch goAhead = new CountDownLatch(1);

      // Unlock CDL
      CountDownLatch cdl = new CountDownLatch(1);

      // Create thread
      Thread thread = new Thread(() -> {
        try{
          rl.lock();
          goAhead.countDown();
          cdl.await();
        }catch(InterruptedException e){
          fail();
        }finally{
          rl.unlock();
        }
      });

      try{

        // Start thread to lock
        thread.start();

        // Wait for thread to lock
        goAhead.await();

        // Attempt to tryLock
        assertThrows(TimeoutException.class, al::doTryLock);

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform doTryLock(long,TimeUnit) method and force it to time out")
    @Test
    void testAutoLockDoTryLockTimeUnitTimeout() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Go-ahead CDL
      CountDownLatch goAhead = new CountDownLatch(1);

      // Unlock CDL
      CountDownLatch cdl = new CountDownLatch(1);

      // Create thread
      Thread thread = new Thread(() -> {
        try{
          rl.lock();
          goAhead.countDown();
          cdl.await();
        }catch(InterruptedException e){
          e.printStackTrace();
          fail();
        }finally{
          rl.unlock();
        }
      });

      try{

        // Start thread to lock
        thread.start();

        // Wait for thread to lock
        goAhead.await();

        // Attempt to tryLock
        assertThrows(TimeoutException.class, () -> al.doTryLock(EXECUTION_TIME.toMillis(), TimeUnit.MILLISECONDS));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform doTryLock(Duration) method and force it to time out")
    @Test
    void testAutoLockDoTryLockDurationTimeout() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Go-ahead CDL
      CountDownLatch goAhead = new CountDownLatch(1);

      // Unlock CDL
      CountDownLatch cdl = new CountDownLatch(1);

      // Create thread
      Thread thread = new Thread(() -> {
        try{
          rl.lock();
          goAhead.countDown();
          cdl.await();
        }catch(InterruptedException e){
          e.printStackTrace();
          fail();
        }finally{
          rl.unlock();
        }
      });

      try{

        // Start thread to lock
        thread.start();

        // Wait for thread to lock
        goAhead.await();

        // Attempt to tryLock
        assertThrows(TimeoutException.class, () -> al.doTryLock(EXECUTION_TIME));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }
  }

  @DisplayName("Static Methods")
  static class StaticMethods{

    @DisplayName("Attempt to successfully perform doLock(Lock) method")
    @Test
    void testAutoLockDoLock(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      Runnable afterLock;

      // Lock it
      try(LockedAutoLock ignored = AutoLock.doLock(rl)){

        // Do during-thread test and get post lock runnable
        afterLock = duringThread.get();
      }

      // Do after-lock test
      afterLock.run();
    }

    @DisplayName("Attempt to successfully perform doLockInterruptibly(Lock) method")
    @Test
    void testAutoLockDoLockInterruptibly() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      Runnable afterLock;

      // Lock it
      try(LockedAutoLock ignored = AutoLock.doLockInterruptibly(rl)){

        // Do during-thread test and get post lock runnable
        afterLock = duringThread.get();
      }

      // Do after-lock test
      afterLock.run();
    }

    @DisplayName("Attempt to successfully perform doTryLock(Lock) method")
    @Test
    void testAutoLockDoTryLock() throws TimeoutException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      Runnable afterLock;

      // Lock it
      try(LockedAutoLock ignored = AutoLock.doTryLock(rl)){

        // Do during-thread test and get post lock runnable
        afterLock = duringThread.get();
      }

      // Do after-lock test
      afterLock.run();
    }


    @DisplayName("Attempt to perform doLockInterruptibly(Lock) method and call interrupt")
    @Test
    void testAutoLockDoLockInterruptiblyInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        try(LockedAutoLock ignored = AutoLock.doLockInterruptibly(rl)){
          fail("Lock obtained");
        }catch(InterruptedException e){
          success.set(true);
        }

      });

      // Start thread
      thread.start();

      // Wait a tiny bit
      Thread.sleep(EXECUTION_TIME.toMillis());

      // Interrupt
      thread.interrupt();

      // Wait for thread to join
      thread.join();

      // Ensure that lock has been interrupted
      assertTrue(success.get());

    }

    @DisplayName("Attempt to perform doTryLock(Lock,long,TimeUnit) method and call interrupt")
    @Test
    void testAutoLockDoTryLockTimeUnitInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        // Ensure doTryLock doesn't take too long
        assertTimeout(Duration.ofSeconds(1), () -> {

          // Do it
          try(LockedAutoLock ignored = AutoLock.doTryLock(rl, 1, TimeUnit.MINUTES)){
            fail("Lock obtained");
          }catch(InterruptedException e){
            success.set(true);
          }catch(TimeoutException e){
            fail("Timed out");
          }
        });
      });

      // Start thread
      thread.start();

      // Wait a tiny bit
      Thread.sleep(EXECUTION_TIME.toMillis());

      // Interrupt
      thread.interrupt();

      // Wait for thread to join
      thread.join();

      // Ensure that lock has been interrupted
      assertTrue(success.get());

    }

    @DisplayName("Attempt to perform doTryLock(Lock,Duration) method and call interrupt")
    @Test
    void testAutoLockDoTryLockDurationInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        // Ensure doTryLock doesn't take too long
        assertTimeout(Duration.ofSeconds(1), () -> {

          // Do it
          try(LockedAutoLock ignored = AutoLock.doTryLock(rl, Duration.ofMinutes(1))){
            fail("Lock obtained");
          }catch(InterruptedException e){
            success.set(true);
          }catch(TimeoutException e){
            fail("Timed out");
          }
        });
      });

      // Start thread
      thread.start();

      // Wait a tiny bit
      Thread.sleep(EXECUTION_TIME.toMillis());

      // Interrupt
      thread.interrupt();

      // Wait for thread to join
      thread.join();

      // Ensure that lock has been interrupted
      assertTrue(success.get());

    }

    @DisplayName("Attempt to successfully perform doTryLock(Lock,long,TimeUnit) method")
    @Test
    void testAutoLockDoTryLockWithLongTimeUnitTimeout(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> {
        try(LockedAutoLock ignored = AutoLock.doTryLock(rl, 1, TimeUnit.MINUTES)){

          // Do during-thread test and get post lock runnable
          afterLock.set(duringThread.get());
        }
      });

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform doTryLock(Lock,Duration) method")
    @Test
    void testAutoLockDoTryLockWithDurationTimeout(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> {
        try(LockedAutoLock ignored = AutoLock.doTryLock(rl, Duration.ofMinutes(1))){

          // Do during-thread test and get post lock runnable
          afterLock.set(duringThread.get());
        }
      });

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to perform doTryLock(Lock) method and force it to time out")
    @Test
    void testAutoLockDoTryLockTimeout() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Go-ahead CDL
      CountDownLatch goAhead = new CountDownLatch(1);

      // Unlock CDL
      CountDownLatch cdl = new CountDownLatch(1);

      // Create thread
      Thread thread = new Thread(() -> {
        try{
          rl.lock();
          goAhead.countDown();
          cdl.await();
        }catch(InterruptedException e){
          fail();
        }finally{
          rl.unlock();
        }
      });

      try{

        // Start thread to lock
        thread.start();

        // Wait for thread to lock
        goAhead.await();

        // Attempt to tryLock
        assertThrows(TimeoutException.class, () -> AutoLock.doTryLock(rl));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform doTryLock(Lock,long,TimeUnit) method and force it to time out")
    @Test
    void testAutoLockDoTryLockTimeUnitTimeout() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Go-ahead CDL
      CountDownLatch goAhead = new CountDownLatch(1);

      // Unlock CDL
      CountDownLatch cdl = new CountDownLatch(1);

      // Create thread
      Thread thread = new Thread(() -> {
        try{
          rl.lock();
          goAhead.countDown();
          cdl.await();
        }catch(InterruptedException e){
          e.printStackTrace();
          fail();
        }finally{
          rl.unlock();
        }
      });

      try{

        // Start thread to lock
        thread.start();

        // Wait for thread to lock
        goAhead.await();

        // Attempt to tryLock
        assertThrows(
          TimeoutException.class,
          () -> AutoLock.doTryLock(rl, EXECUTION_TIME.toMillis(), TimeUnit.MILLISECONDS)
        );

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform doTryLock(Lock,Duration) method and force it to time out")
    @Test
    void testAutoLockDoTryLockDurationTimeout() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Go-ahead CDL
      CountDownLatch goAhead = new CountDownLatch(1);

      // Unlock CDL
      CountDownLatch cdl = new CountDownLatch(1);

      // Create thread
      Thread thread = new Thread(() -> {
        try{
          rl.lock();
          goAhead.countDown();
          cdl.await();
        }catch(InterruptedException e){
          e.printStackTrace();
          fail();
        }finally{
          rl.unlock();
        }
      });

      try{

        // Start thread to lock
        thread.start();

        // Wait for thread to lock
        goAhead.await();

        // Attempt to tryLock
        assertThrows(TimeoutException.class, () -> AutoLock.doTryLock(rl, EXECUTION_TIME));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }
  }

}