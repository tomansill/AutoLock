package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.LockedAutoLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static com.ansill.lock.autolock.test.AutoLockTest.beforeLock;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Instance Methods")
class AutoLockInstanceMethodsTest implements AutoLockTest{

  @DisplayName("Attempt to successfully double-unlock")
  @Test
  void testDoubleUnlock(){

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

  @DisplayName("Attempt to successfully run doLock() method")
  @Test
  void testDoLock(){

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

  @DisplayName("Attempt to successfully run doLockInterruptibly() method")
  @Test
  void testDoLockInterruptibly() throws InterruptedException{

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

  @DisplayName("Attempt to successfully run doTryLock() method")
  @Test
  void testDoTryLock() throws TimeoutException{

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

  @DisplayName("Attempt to successfully run doTryLock(long,TimeUnit) method")
  @Test
  void testDoTryLockWithLongTimeUnitTimeout(){

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

  @DisplayName("Attempt to successfully run doTryLock(Duration) method")
  @Test
  void testDoTryLockWithDurationTimeout(){

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

  @DisplayName("Attempt to run doLockInterruptibly() method and call interrupt")
  @Test
  void testDoLockInterruptiblyInterrupt() throws InterruptedException{

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

  @DisplayName("Attempt to run doTryLock(long,TimeUnit) method and call interrupt")
  @Test
  void testDoTryLockTimeUnitInterrupt() throws InterruptedException{

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

  @DisplayName("Attempt to run doTryLock(Duration) method and call interrupt")
  @Test
  void testDoTryLockDurationInterrupt() throws InterruptedException{

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

  @DisplayName("Attempt to run doTryLock() method and force it to time out")
  @Test
  void testDoTryLockTimeout() throws InterruptedException{

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

  @DisplayName("Attempt to run doTryLock(long,TimeUnit) method and force it to time out")
  @Test
  void testDoTryLockTimeUnitTimeout() throws InterruptedException{

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

  @DisplayName("Attempt to run doTryLock(Duration) method and force it to time out")
  @Test
  void testDoTryLockDurationTimeout() throws InterruptedException{

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
