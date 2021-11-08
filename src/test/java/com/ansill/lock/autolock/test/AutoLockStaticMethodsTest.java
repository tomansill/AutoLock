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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Static Methods")
class AutoLockStaticMethodsTest implements AutoLockTest{

  @DisplayName("Attempt to successfully run doLock(Lock) method")
  @Test
  void testDoLock(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

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

  @DisplayName("Attempt to successfully run doLockInterruptibly(Lock) method")
  @Test
  void testDoLockInterruptibly() throws InterruptedException{

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

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

  @DisplayName("Attempt to successfully run doTryLock(Lock) method")
  @Test
  void testDoTryLock() throws TimeoutException{

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

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


  @DisplayName("Attempt to run doLockInterruptibly(Lock) method and call interrupt")
  @Test
  void testDoLockInterruptiblyInterrupt() throws InterruptedException{

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
    Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

    // Interrupt
    thread.interrupt();

    // Wait for thread to join
    thread.join();

    // Ensure that lock has been interrupted
    assertTrue(success.get());

  }

  @DisplayName("Attempt to run doTryLock(Lock,long,TimeUnit) method and call interrupt")
  @Test
  void testDoTryLockTimeUnitInterrupt() throws InterruptedException{

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
    Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

    // Interrupt
    thread.interrupt();

    // Wait for thread to join
    thread.join();

    // Ensure that lock has been interrupted
    assertTrue(success.get());

  }

  @DisplayName("Attempt to run doTryLock(Lock,Duration) method and call interrupt")
  @Test
  void testDoTryLockDurationInterrupt() throws InterruptedException{

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
    Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

    // Interrupt
    thread.interrupt();

    // Wait for thread to join
    thread.join();

    // Ensure that lock has been interrupted
    assertTrue(success.get());

  }

  @DisplayName("Attempt to successfully run doTryLock(Lock,long,TimeUnit) method")
  @Test
  void testDoTryLockWithLongTimeUnitTimeout(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

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

  @DisplayName("Attempt to successfully run doTryLock(Lock,Duration) method")
  @Test
  void testDoTryLockWithDurationTimeout(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

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

  @DisplayName("Attempt to run doTryLock(Lock) method and force it to time out")
  @Test
  void testDoTryLockTimeout() throws InterruptedException{

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

  @DisplayName("Attempt to run doTryLock(Lock,long,TimeUnit) method and force it to time out")
  @Test
  void testDoTryLockTimeUnitTimeout() throws InterruptedException{

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
        () -> AutoLock.doTryLock(rl, AutoLockTest.EXECUTION_TIME.toMillis(), TimeUnit.MILLISECONDS)
      );

    }finally{
      cdl.countDown();
    }

    // Wait for thread to finish
    thread.join();

  }

  @DisplayName("Attempt to run doTryLock(Lock,Duration) method and force it to time out")
  @Test
  void testDoTryLockDurationTimeout() throws InterruptedException{

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
      assertThrows(TimeoutException.class, () -> AutoLock.doTryLock(rl, AutoLockTest.EXECUTION_TIME));

    }finally{
      cdl.countDown();
    }

    // Wait for thread to finish
    thread.join();

  }
}
