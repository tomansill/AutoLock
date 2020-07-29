package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.LockedAutoLock;
import com.ansill.lock.autolock.NonThreadSafeObject;
import com.ansill.utility.function.RunnableWithException;
import com.ansill.utility.function.SupplierWithException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
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

    @DisplayName("Attempt to successfully double-unlock")
    @Test
    void testAutoLockDoubleUnlock(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Create AutoLock
      AutoLock al = AutoLock.create(rl);

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      Runnable afterLock;

      // Lock it
      try(LockedAutoLock lock = al.doLock()){

        // Do during-thread test and get post lock runnable
        afterLock = duringThread.get();

        // Unlock here
        lock.unlock();

      } // Will unlock here

      // Check if it's unlocked
      assertFalse(al.isLocked());

      // Do after-lock test
      afterLock.run();

    }

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

  @DisplayName("Lambda methods")
  static abstract class Lambdas{

    abstract void lockAndPerform(Lock lock, RunnableWithException runnable) throws ExecutionException;

    abstract <T> T lockAndGet(Lock lock, SupplierWithException<T> supplierWithException) throws ExecutionException;

    abstract void lockInterruptiblyAndPerform(Lock lock, RunnableWithException runnable)
    throws InterruptedException, ExecutionException;

    abstract <T> T lockInterruptiblyAndGet(Lock lock, SupplierWithException<T> supplierWithException)
    throws InterruptedException, ExecutionException;

    abstract void tryLockAndPerform(Lock lock, RunnableWithException runnable)
    throws TimeoutException, ExecutionException;

    abstract <T> T tryLockAndGet(Lock lock, SupplierWithException<T> runnable)
    throws TimeoutException, ExecutionException;

    abstract void tryLockAndPerform(Lock lock, long time, TimeUnit unit, RunnableWithException runnable)
    throws TimeoutException, InterruptedException, ExecutionException;

    abstract <T> T tryLockAndGet(Lock lock, long time, TimeUnit unit, SupplierWithException<T> supplier)
    throws TimeoutException, ExecutionException, InterruptedException;

    abstract void tryLockAndPerform(Lock lock, Duration timeout, RunnableWithException runnable)
    throws TimeoutException, InterruptedException, ExecutionException;

    abstract <T> T tryLockAndGet(Lock lock, Duration timeout, SupplierWithException<T> supplier)
    throws TimeoutException, ExecutionException, InterruptedException;

    @DisplayName("Attempt to successfully perform lockAndPerform(Lock,Runnable) method")
    @Test
    void testAutoLockLockAndPerform() throws ExecutionException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it
      lockAndPerform(rl, () -> afterLock.set(duringThread.get()));

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform lockAndGet(Lock,Supplier) method")
    @Test
    void testAutoLockLockAndGet() throws ExecutionException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Desired item
      int item = RNG.nextInt();

      // Lock it
      int value = lockAndGet(rl, () -> {
        afterLock.set(duringThread.get());
        return item;
      });

      // Do after-lock test
      afterLock.get().run();

      // Check value
      assertEquals(item, value);
    }

    @DisplayName("Attempt to successfully perform lockInterruptiblyAndPerform(Lock,Runnable) method")
    @Test
    void testAutoLockLockInterruptiblyAndPerform() throws InterruptedException, ExecutionException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it
      lockInterruptiblyAndPerform(rl, () -> afterLock.set(duringThread.get()));

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform lockInterruptiblyAndGet(Lock,Supplier) method")
    @Test
    void testAutoLockLockInterruptiblyAndGet() throws InterruptedException, ExecutionException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Desired item
      int item = RNG.nextInt();

      // Lock it
      int value = lockInterruptiblyAndGet(rl, () -> {
        afterLock.set(duringThread.get());
        return item;
      });

      // Do after-lock test
      afterLock.get().run();

      // Check value
      assertEquals(item, value);
    }

    @DisplayName("Attempt to perform lockInterruptiblyAndPerform(Lock,Runnable) method and call interrupt")
    @Test
    void testAutoLockLockInterruptiblyAndPerformInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        try{
          lockInterruptiblyAndPerform(rl, () -> fail("Lock obtained"));
        }catch(InterruptedException e){
          success.set(true);
        }catch(ExecutionException e){
          e.printStackTrace();
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

    @DisplayName("Attempt to perform lockInterruptiblyAndGet(Lock,Supplier) method and call interrupt")
    @Test
    void testAutoLockLockInterruptiblyAndGetInterrupt() throws InterruptedException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Lock it
      rl.lock();

      // Flag for successful interrupt
      AtomicBoolean success = new AtomicBoolean(false);

      // Create thread
      Thread thread = new Thread(() -> {

        try{
          lockInterruptiblyAndGet(rl, () -> {
            fail("Lock obtained");
            return 10;
          });
        }catch(InterruptedException e){
          success.set(true);
        }catch(ExecutionException e){
          e.printStackTrace();
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

    @DisplayName("Attempt to perform tryLockAndPerform(Lock,long,TimeUnit,Runnable) method and call interrupt")
    @Test
    void testAutoLockTryLockAndPerformTimeUnitInterrupt() throws InterruptedException{

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
          try{
            tryLockAndPerform(rl, 1, TimeUnit.MINUTES, () -> fail("Lock obtained"));
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

    @DisplayName("Attempt to perform tryLockAndGet(Lock,long,TimeUnit,Supplier) method and call interrupt")
    @Test
    void testAutoLockTryLockAndGetTimeUnitInterrupt() throws InterruptedException{

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
          try{
            tryLockAndGet(rl, 1, TimeUnit.MINUTES, () -> {
              fail("Lock obtained");
              return 1;
            });
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

    @DisplayName("Attempt to perform tryLockAndPerform(Lock,Duration,Runnable) method and call interrupt")
    @Test
    void testAutoLockTryLockAndPerformDurationInterrupt() throws InterruptedException{

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
          try{
            tryLockAndPerform(rl, Duration.ofMinutes(1), () -> fail("Lock obtained"));
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

    @DisplayName("Attempt to perform tryLockAndGet(Lock,Duration,Supplier) method and call interrupt")
    @Test
    void testAutoLockTryLockAndGetDurationInterrupt() throws InterruptedException{

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
          try{
            tryLockAndGet(rl, Duration.ofMinutes(1), () -> {
              fail("Lock obtained");
              return 1;
            });
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

    @DisplayName("Attempt to successfully perform tryLockAndPerform(Lock,Runnable) method")
    @Test
    void testAutoLockTryLockAndPerform() throws TimeoutException, ExecutionException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it
      tryLockAndPerform(rl, () -> {

        // Do during-thread test and get post lock runnable
        afterLock.set(duringThread.get());

      });

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform tryLockAndGet(Lock,Supplier) method")
    @Test
    void testAutoLockTryLockAndGet() throws TimeoutException, ExecutionException{

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Expected value
      int expected = RNG.nextInt();

      // Lock it and ensure that tryLock is instantaneous
      int value = tryLockAndGet(rl, () -> {

        // Do during-thread test and get post lock runnable
        afterLock.set(duringThread.get());

        // Return expected value
        return expected;
      });

      // Check value
      assertEquals(expected, value);

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform tryLockAndPerform(Lock,long,TimeUnit,Runnable) method")
    @Test
    void testAutoLockTryLockAndPerformTimeUnit(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> tryLockAndPerform(rl, 1, TimeUnit.MINUTES, () -> {

        // Do during-thread test and get post lock runnable
        afterLock.set(duringThread.get());

      }));

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform tryLockAndGet(Lock,long,TimeUnit,Supplier) method")
    @Test
    void testAutoLockTryLockAndGetTimeUnit(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Expected value
      int expected = RNG.nextInt();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> {
        int value = tryLockAndGet(rl, 1, TimeUnit.MINUTES, () -> {

          // Do during-thread test and get post lock runnable
          afterLock.set(duringThread.get());

          // Return expected value
          return expected;
        });

        // Check value
        assertEquals(expected, value);
      });

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform tryLockAndPerform(Lock,Duration,Runnable) method")
    @Test
    void testAutoLockTryLockAndPerformDuration(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> tryLockAndPerform(rl, Duration.ofMinutes(1), () -> {

        // Do during-thread test and get post lock runnable
        afterLock.set(duringThread.get());
      }));

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to successfully perform tryLockAndGet(Lock,Duration,Supplier) method")
    @Test
    void testAutoLockTryLockAndGetDuration(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Do before
      Supplier<Runnable> duringThread = beforeLock(rl);

      // Expected value
      int expected = RNG.nextInt();

      // Set up after-lock runnable reference
      AtomicReference<Runnable> afterLock = new AtomicReference<>();

      // Lock it and ensure that tryLock is instantaneous
      assertTimeout(Duration.ofSeconds(1), () -> {
        int value = tryLockAndGet(rl, Duration.ofMinutes(1), () -> {

          // Do during-thread test and get post lock runnable
          afterLock.set(duringThread.get());

          // Return expected value
          return expected;
        });

        // Check value
        assertEquals(expected, value);
      });

      // Do after-lock test
      afterLock.get().run();
    }

    @DisplayName("Attempt to perform tryLockAndPerform(Lock,Runnable) method and force it to time out")
    @Test
    void testAutoLockTryLockAndPerformTimeout() throws InterruptedException{

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
        assertThrows(TimeoutException.class, () -> tryLockAndPerform(rl, () -> fail("Lock obtained")));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform tryLockAndGet(Lock,Supplier) method and force it to time out")
    @Test
    void testAutoLockTryLockAndGetTimeout() throws InterruptedException{

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
        assertThrows(TimeoutException.class, () -> tryLockAndGet(rl, () -> {
          fail("Lock obtained");
          return 1;
        }));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform tryLockAndPerform(Lock,long,TimeUnit,Runnable) method and force it to time out")
    @Test
    void testAutoLockTryLockAndPerformTimeUnitTimeout() throws InterruptedException{

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
          () -> tryLockAndPerform(rl, EXECUTION_TIME.toMillis(), TimeUnit.MILLISECONDS, () -> fail("Lock obtained"))
        );

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform tryLockAndGet(Lock,long,TimeUnit,Supplier) method and force it to time out")
    @Test
    void testAutoLockTryLockAndGetTimeUnitTimeout() throws InterruptedException{

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
          () -> tryLockAndGet(rl, EXECUTION_TIME.toMillis(), TimeUnit.MILLISECONDS, () -> {
            fail("Lock obtained");
            return 1;
          })
        );

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform tryLockAndPerform(Lock,Duration,Runnable) method and force it to time out")
    @Test
    void testAutoLockTryLockAndPerformDurationTimeout() throws InterruptedException{

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
        assertThrows(TimeoutException.class, () -> tryLockAndPerform(rl, EXECUTION_TIME, () -> fail("obtained lock")));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    @DisplayName("Attempt to perform tryLockAndGet(Lock,Duration,Supplier) method and force it to time out")
    @Test
    void testAutoLockTryLockAndGetDurationTimeout() throws InterruptedException{

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
        assertThrows(TimeoutException.class, () -> tryLockAndGet(rl, EXECUTION_TIME, () -> {
          fail("obtained lock");
          return 1;
        }));

      }finally{
        cdl.countDown();
      }

      // Wait for thread to finish
      thread.join();

    }

    static class LambdaWithoutException extends Lambdas{

      @Override
      void lockAndPerform(Lock lock, RunnableWithException runnable){
        AutoLock.lockAndPerform(lock, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T lockAndGet(Lock lock, SupplierWithException<T> supplierWithException){
        return AutoLock.lockAndGet(lock, () -> {
          try{
            return supplierWithException.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void lockInterruptiblyAndPerform(Lock lock, RunnableWithException runnable) throws InterruptedException{
        AutoLock.lockInterruptiblyAndPerform(lock, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T lockInterruptiblyAndGet(Lock lock, SupplierWithException<T> supplierWithException)
      throws InterruptedException{
        return AutoLock.lockInterruptiblyAndGet(lock, () -> {
          try{
            return supplierWithException.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void tryLockAndPerform(Lock lock, RunnableWithException runnable) throws TimeoutException{
        AutoLock.tryLockAndPerform(lock, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T tryLockAndGet(Lock lock, SupplierWithException<T> supplier) throws TimeoutException{
        return AutoLock.tryLockAndGet(lock, () -> {
          try{
            return supplier.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void tryLockAndPerform(Lock lock, long time, TimeUnit unit, RunnableWithException runnable)
      throws TimeoutException, InterruptedException{
        AutoLock.tryLockAndPerform(lock, time, unit, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T tryLockAndGet(Lock lock, long time, TimeUnit unit, SupplierWithException<T> supplier)
      throws TimeoutException, InterruptedException{
        return AutoLock.tryLockAndGet(lock, time, unit, () -> {
          try{
            return supplier.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void tryLockAndPerform(Lock lock, Duration timeout, RunnableWithException runnable)
      throws TimeoutException, InterruptedException{
        AutoLock.tryLockAndPerform(lock, timeout, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T tryLockAndGet(Lock lock, Duration timeout, SupplierWithException<T> supplier)
      throws TimeoutException, InterruptedException{
        return AutoLock.tryLockAndGet(lock, timeout, () -> {
          try{
            return supplier.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }
    }

    static class LambdaWithException extends Lambdas{

      @Override
      void lockAndPerform(Lock lock, RunnableWithException runnable) throws ExecutionException{
        AutoLock.Ex.lockAndPerform(lock, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T lockAndGet(Lock lock, SupplierWithException<T> supplierWithException) throws ExecutionException{
        return AutoLock.Ex.lockAndGet(lock, () -> {
          try{
            return supplierWithException.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void lockInterruptiblyAndPerform(Lock lock, RunnableWithException runnable)
      throws InterruptedException, ExecutionException{
        AutoLock.Ex.lockInterruptiblyAndPerform(lock, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T lockInterruptiblyAndGet(Lock lock, SupplierWithException<T> supplierWithException)
      throws InterruptedException, ExecutionException{
        return AutoLock.Ex.lockInterruptiblyAndGet(lock, () -> {
          try{
            return supplierWithException.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void tryLockAndPerform(Lock lock, RunnableWithException runnable) throws TimeoutException, ExecutionException{
        AutoLock.Ex.tryLockAndPerform(lock, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T tryLockAndGet(Lock lock, SupplierWithException<T> supplier) throws TimeoutException, ExecutionException{
        return AutoLock.Ex.tryLockAndGet(lock, () -> {
          try{
            return supplier.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void tryLockAndPerform(Lock lock, long time, TimeUnit unit, RunnableWithException runnable)
      throws TimeoutException, InterruptedException, ExecutionException{
        AutoLock.Ex.tryLockAndPerform(lock, time, unit, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T tryLockAndGet(Lock lock, long time, TimeUnit unit, SupplierWithException<T> supplier)
      throws TimeoutException, ExecutionException, InterruptedException{
        return AutoLock.Ex.tryLockAndGet(lock, time, unit, () -> {
          try{
            return supplier.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      void tryLockAndPerform(Lock lock, Duration timeout, RunnableWithException runnable)
      throws TimeoutException, InterruptedException, ExecutionException{
        AutoLock.Ex.tryLockAndPerform(lock, timeout, () -> {
          try{
            runnable.run();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Override
      <T> T tryLockAndGet(Lock lock, Duration timeout, SupplierWithException<T> supplier)
      throws TimeoutException, ExecutionException, InterruptedException{
        return AutoLock.Ex.tryLockAndGet(lock, timeout, () -> {
          try{
            return supplier.get();
          }catch(Exception exception){
            throw new RuntimeException(exception);
          }
        });
      }

      @Test
      void testAutoLockLockAndPerformWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> AutoLock.Ex.lockAndPerform(rl, () -> {
          throw exception;
        }));

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockLockAndGetWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> AutoLock.Ex.lockAndGet(rl, () -> {
          throw exception;
        }));

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockLockInterruptiblyAndPerformWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(
          ExecutionException.class,
          () -> AutoLock.Ex.lockInterruptiblyAndPerform(rl, () -> {
            throw exception;
          })
        );

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockLockInterruptiblyAndGetWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(
          ExecutionException.class,
          () -> AutoLock.Ex.lockInterruptiblyAndGet(rl, () -> {
            throw exception;
          })
        );

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockTryLockAndPerformWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(
          ExecutionException.class,
          () -> AutoLock.Ex.tryLockAndPerform(rl, () -> {
            throw exception;
          })
        );

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockTryLockAndGetWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> AutoLock.Ex.tryLockAndGet(rl, () -> {
          throw exception;
        }));

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockTryLockAndPerformTimeUnitWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(
          ExecutionException.class,
          () -> AutoLock.Ex.tryLockAndPerform(rl, 1, TimeUnit.MINUTES, () -> {
            throw exception;
          })
        );

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockTryLockAndGetTimeUnitWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(
          ExecutionException.class,
          () -> AutoLock.Ex.tryLockAndGet(rl, 1, TimeUnit.MINUTES, () -> {
            throw exception;
          })
        );

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockTryLockAndPerformDurationWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(
          ExecutionException.class,
          () -> AutoLock.Ex.tryLockAndPerform(rl, Duration.ofMinutes(1), () -> {
            throw exception;
          })
        );

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }

      @Test
      void testAutoLockTryLockAndGetDurationWithException(){

        // Create lock
        ReentrantLock rl = new ReentrantLock();

        // Exception
        Exception exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

        // Do it
        ExecutionException thrown = assertThrows(
          ExecutionException.class,
          () -> AutoLock.Ex.tryLockAndGet(rl, Duration.ofMinutes(1), () -> {
            throw exception;
          })
        );

        // Get cause and compare
        assertEquals(exception, thrown.getCause());
      }
    }
  }
}