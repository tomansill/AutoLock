package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.AutoLock;
import com.ansill.lock.autolock.ThrowableRunnable;
import com.ansill.lock.autolock.ThrowableSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lambda methods")
abstract class AutoLockLambdasTest implements AutoLockTest{

  abstract <T extends Throwable> void lockAndRun(Lock lock, ThrowableRunnable<T> runnable) throws T;

  abstract <R, T extends Throwable> R lockAndGet(Lock lock, ThrowableSupplier<R,T> throwableSupplier) throws T;

  abstract <T extends Throwable> void lockInterruptiblyAndRun(Lock lock, ThrowableRunnable<T> runnable)
  throws T, InterruptedException;

  abstract <R, T extends Throwable> R lockInterruptiblyAndGet(Lock lock, ThrowableSupplier<R,T> throwableSupplier)
  throws T, InterruptedException;

  abstract <T extends Throwable> void tryLockAndRun(Lock lock, ThrowableRunnable<T> runnable) throws T,
    TimeoutException;

  abstract <R, T extends Throwable> R tryLockAndGet(Lock lock, ThrowableSupplier<R,T> runnable)
  throws T, TimeoutException;

  abstract <T extends Throwable> void tryLockAndRun(Lock lock, long time, TimeUnit unit, ThrowableRunnable<T> runnable)
  throws T, TimeoutException, InterruptedException;

  abstract <R, T extends Throwable> R tryLockAndGet(
    Lock lock,
    long time,
    TimeUnit unit,
    ThrowableSupplier<R,T> supplier
  )
  throws T, TimeoutException, InterruptedException;

  abstract <T extends Throwable> void tryLockAndRun(Lock lock, Duration timeout, ThrowableRunnable<T> runnable)
  throws T, TimeoutException, InterruptedException;

  abstract <R, T extends Throwable> R tryLockAndGet(Lock lock, Duration timeout, ThrowableSupplier<R,T> supplier)
  throws T, TimeoutException, InterruptedException;

  @DisplayName("Attempt to successfully run lockAndRun(Lock,Runnable) method")
  @Test
  void testLockAndRun(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Lock it
    assertDoesNotThrow(() -> lockAndRun(rl, () -> afterLock.set(duringThread.get())));

    // Do after-lock test
    afterLock.get().run();
  }

  @DisplayName("Attempt to successfully run lockAndGet(Lock,Supplier) method")
  @Test
  void testLockAndGet(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Desired item
    int item = RNG.nextInt();

    // Lock it
    int value = assertDoesNotThrow(() -> lockAndGet(rl, () -> {
      afterLock.set(duringThread.get());
      return item;
    }));

    // Do after-lock test
    afterLock.get().run();

    // Check value
    assertEquals(item, value);
  }

  @DisplayName("Attempt to successfully run lockInterruptiblyAndRun(Lock,Runnable) method")
  @Test
  void testLockInterruptiblyAndRun(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Lock it
    assertDoesNotThrow(() -> lockInterruptiblyAndRun(rl, () -> afterLock.set(duringThread.get())));

    // Do after-lock test
    afterLock.get().run();
  }

  @DisplayName("Attempt to successfully run lockInterruptiblyAndGet(Lock,Supplier) method")
  @Test
  void testLockInterruptiblyAndGet(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Desired item
    int item = RNG.nextInt();

    // Lock it
    int value = assertDoesNotThrow(() -> lockInterruptiblyAndGet(rl, () -> {
      afterLock.set(duringThread.get());
      return item;
    }));

    // Do after-lock test
    afterLock.get().run();

    // Check value
    assertEquals(item, value);
  }

  @DisplayName("Attempt to run lockInterruptiblyAndRun(Lock,Runnable) method and call interrupt")
  @Test
  void testLockInterruptiblyAndRunInterrupt() throws InterruptedException{

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Lock it
    rl.lock();

    // Flag for successful interrupt
    AtomicBoolean success = new AtomicBoolean(false);

    // Create thread
    Thread thread = new Thread(() -> {

      try{
        lockInterruptiblyAndRun(rl, () -> fail("Lock obtained"));
      }catch(InterruptedException e){
        success.set(true);
      }catch(Exception e){
        fail(e);
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

  @DisplayName("Attempt to run lockInterruptiblyAndGet(Lock,Supplier) method and call interrupt")
  @Test
  void testLockInterruptiblyAndGetInterrupt() throws InterruptedException{

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
      }catch(Exception e){
        fail(e);
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

  @DisplayName("Attempt to run tryLockAndRun(Lock,long,TimeUnit,Runnable) method and call interrupt")
  @Test
  void testTryLockAndRunTimeUnitInterrupt() throws InterruptedException{

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
          tryLockAndRun(rl, 1, TimeUnit.MINUTES, () -> fail("Lock obtained"));
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

  @DisplayName("Attempt to run tryLockAndGet(Lock,long,TimeUnit,Supplier) method and call interrupt")
  @Test
  void testTryLockAndGetTimeUnitInterrupt() throws InterruptedException{

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
    Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

    // Interrupt
    thread.interrupt();

    // Wait for thread to join
    thread.join();

    // Ensure that lock has been interrupted
    assertTrue(success.get());

  }

  @DisplayName("Attempt to run tryLockAndRun(Lock,Duration,Runnable) method and call interrupt")
  @Test
  void testTryLockAndRunDurationInterrupt() throws InterruptedException{

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
          tryLockAndRun(rl, Duration.ofMinutes(1), () -> fail("Lock obtained"));
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

  @DisplayName("Attempt to run tryLockAndGet(Lock,Duration,Supplier) method and call interrupt")
  @Test
  void testTryLockAndGetDurationInterrupt() throws InterruptedException{

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
    Thread.sleep(AutoLockTest.EXECUTION_TIME.toMillis());

    // Interrupt
    thread.interrupt();

    // Wait for thread to join
    thread.join();

    // Ensure that lock has been interrupted
    assertTrue(success.get());

  }

  @DisplayName("Attempt to successfully run tryLockAndRun(Lock,Runnable) method")
  @Test
  void testTryLockAndRun(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Lock it
    assertDoesNotThrow(() -> tryLockAndRun(rl, () -> {

      // Do during-thread test and get post lock runnable
      afterLock.set(duringThread.get());

    }));

    // Do after-lock test
    afterLock.get().run();
  }

  @DisplayName("Attempt to successfully run tryLockAndGet(Lock,Supplier) method")
  @Test
  void testTryLockAndGet(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Expected value
    int expected = RNG.nextInt();

    // Lock it and ensure that tryLock is instantaneous
    int value = assertDoesNotThrow(() -> tryLockAndGet(rl, () -> {

      // Do during-thread test and get post lock runnable
      afterLock.set(duringThread.get());

      // Return expected value
      return expected;
    }));

    // Check value
    assertEquals(expected, value);

    // Do after-lock test
    afterLock.get().run();
  }

  @DisplayName("Attempt to successfully run tryLockAndRun(Lock,long,TimeUnit,Runnable) method")
  @Test
  void testTryLockAndRunTimeUnit(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Lock it and ensure that tryLock is instantaneous
    assertTimeout(Duration.ofSeconds(1), () -> tryLockAndRun(rl, 1, TimeUnit.MINUTES, () -> {

      // Do during-thread test and get post lock runnable
      afterLock.set(duringThread.get());

    }));

    // Do after-lock test
    afterLock.get().run();
  }

  @DisplayName("Attempt to successfully run tryLockAndGet(Lock,long,TimeUnit,Supplier) method")
  @Test
  void testTryLockAndGetTimeUnit(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

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

  @DisplayName("Attempt to successfully run tryLockAndRun(Lock,Duration,Runnable) method")
  @Test
  void testTryLockAndRunDuration(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

    // Set up after-lock runnable reference
    AtomicReference<Runnable> afterLock = new AtomicReference<>();

    // Lock it and ensure that tryLock is instantaneous
    assertTimeout(Duration.ofSeconds(1), () -> tryLockAndRun(rl, Duration.ofMinutes(1), () -> {

      // Do during-thread test and get post lock runnable
      afterLock.set(duringThread.get());
    }));

    // Do after-lock test
    afterLock.get().run();
  }

  @DisplayName("Attempt to successfully run tryLockAndGet(Lock,Duration,Supplier) method")
  @Test
  void testTryLockAndGetDuration(){

    // Create lock
    ReentrantLock rl = new ReentrantLock();

    // Do before
    Supplier<Runnable> duringThread = AutoLockTest.beforeLock(rl);

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

  @DisplayName("Attempt to run tryLockAndRun(Lock,Runnable) method and force it to time out")
  @Test
  void testTryLockAndRunTimeout() throws InterruptedException{

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
      assertThrows(TimeoutException.class, () -> tryLockAndRun(rl, () -> fail("Lock obtained")));

    }finally{
      cdl.countDown();
    }

    // Wait for thread to finish
    thread.join();

  }

  @DisplayName("Attempt to run tryLockAndGet(Lock,Supplier) method and force it to time out")
  @Test
  void testTryLockAndGetTimeout() throws InterruptedException{

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

  @DisplayName("Attempt to run tryLockAndRun(Lock,long,TimeUnit,Runnable) method and force it to time out")
  @Test
  void testTryLockAndRunTimeUnitTimeout() throws InterruptedException{

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
        () -> tryLockAndRun(
          rl,
          AutoLockTest.EXECUTION_TIME.toMillis(),
          TimeUnit.MILLISECONDS,
          () -> fail("Lock obtained")
        )
      );

    }finally{
      cdl.countDown();
    }

    // Wait for thread to finish
    thread.join();

  }

  @DisplayName("Attempt to run tryLockAndGet(Lock,long,TimeUnit,Supplier) method and force it to time out")
  @Test
  void testTryLockAndGetTimeUnitTimeout() throws InterruptedException{

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
        () -> tryLockAndGet(rl, AutoLockTest.EXECUTION_TIME.toMillis(), TimeUnit.MILLISECONDS, () -> {
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

  @DisplayName("Attempt to run tryLockAndRun(Lock,Duration,Runnable) method and force it to time out")
  @Test
  void testTryLockAndRunDurationTimeout() throws InterruptedException{

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
        () -> tryLockAndRun(rl, AutoLockTest.EXECUTION_TIME, () -> fail("obtained lock"))
      );

    }finally{
      cdl.countDown();
    }

    // Wait for thread to finish
    thread.join();

  }

  @DisplayName("Attempt to run tryLockAndGet(Lock,Duration,Supplier) method and force it to time out")
  @Test
  void testTryLockAndGetDurationTimeout() throws InterruptedException{

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
      assertThrows(TimeoutException.class, () -> tryLockAndGet(rl, AutoLockTest.EXECUTION_TIME, () -> {
        fail("obtained lock");
        return 1;
      }));

    }finally{
      cdl.countDown();
    }

    // Wait for thread to finish
    thread.join();

  }

  @DisplayName("Lambda methods (with Exception)")
  static class LambdaWithException extends AutoLockLambdasTest{

    @Override
    <T extends Throwable> void lockAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T> runnable) throws T{
      AutoLock.lockAndRun(lock, runnable);
    }

    @Override
    <R, T extends Throwable> R lockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R,T> throwableSupplier)
    throws T{
      return AutoLock.lockAndGet(lock, throwableSupplier);
    }

    @Override
    <T extends Throwable> void lockInterruptiblyAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T> runnable)
    throws T, InterruptedException{
      AutoLock.lockInterruptiblyAndRun(lock, runnable);
    }

    @Override
    <R, T extends Throwable> R lockInterruptiblyAndGet(
      @Nonnull Lock lock,
      @Nonnull ThrowableSupplier<R,T> throwableSupplier
    )
    throws T, InterruptedException{
      return AutoLock.lockInterruptiblyAndGet(lock, throwableSupplier);
    }

    @Override
    <T extends Throwable> void tryLockAndRun(@Nonnull Lock lock, @Nonnull ThrowableRunnable<T> runnable)
    throws T, TimeoutException{
      AutoLock.tryLockAndRun(lock, runnable);
    }

    @Override
    <R, T extends Throwable> R tryLockAndGet(@Nonnull Lock lock, @Nonnull ThrowableSupplier<R,T> supplier)
    throws T, TimeoutException{
      return AutoLock.tryLockAndGet(lock, supplier);
    }

    @Override
    <T extends Throwable> void tryLockAndRun(
      @Nonnull Lock lock,
      long time,
      @Nonnull TimeUnit unit,
      @Nonnull ThrowableRunnable<T> runnable
    )
    throws T, InterruptedException, TimeoutException{
      AutoLock.tryLockAndRun(lock, time, unit, runnable);
    }

    @Override
    <R, T extends Throwable> R tryLockAndGet(
      @Nonnull Lock lock,
      long time,
      @Nonnull TimeUnit unit,
      @Nonnull ThrowableSupplier<R,T> supplier
    )
    throws T, InterruptedException, TimeoutException{
      return AutoLock.tryLockAndGet(lock, time, unit, supplier);
    }

    @Override
    <T extends Throwable> void tryLockAndRun(
      @Nonnull Lock lock,
      @Nonnull Duration timeout,
      @Nonnull ThrowableRunnable<T> runnable
    )
    throws T, InterruptedException, TimeoutException{
      AutoLock.tryLockAndRun(lock, timeout, runnable);
    }

    @Override
    <R, T extends Throwable> R tryLockAndGet(
      @Nonnull Lock lock,
      @Nonnull Duration timeout,
      @Nonnull ThrowableSupplier<R,T> supplier
    )
    throws T, InterruptedException, TimeoutException{
      return AutoLock.tryLockAndGet(lock, timeout, supplier);
    }

    @DisplayName("Attempt to run lockAndRun(Lock,Runnable) method and have the inside function to throw exception")
    @Test
    void testLockAndRunWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.lockAndRun(rl, () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run lockAndGet(Lock,Supplier) method and have the inside function to throw exception")
    @Test
    void testLockAndGetWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(FileNotFoundException.class, () -> AutoLock.lockAndGet(rl, () -> {
        throw exception;
      }));

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run lockInterruptiblyAndRun(Lock,Runnable) method and have the inside function to throw exception")
    @Test
    void testLockInterruptiblyAndRunWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.lockInterruptiblyAndRun(rl, () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run lockInterruptiblyAndGet(Lock,Supplier) method and have the inside function to throw exception")
    @Test
    void testLockInterruptiblyAndGetWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.lockInterruptiblyAndGet(rl, () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run tryLockAndRun(Lock,Runnable) method and have the inside function to throw exception")
    @Test
    void testTryLockAndRunWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.tryLockAndRun(rl, () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run tryLockAndGet(Lock,Supplier) method and have the inside function to throw exception")
    @Test
    void testTryLockAndGetWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.tryLockAndGet(rl, () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run tryLockAndRun(Lock,long,TimeUnit,Runnable) method and have the inside function to throw exception")
    @Test
    void testTryLockAndRunTimeUnitWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.tryLockAndRun(rl, 1, TimeUnit.MINUTES, () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run tryLockAndGet(Lock,long,TimeUnit,Supplier) method and have the inside function to throw exception")
    @Test
    void testTryLockAndGetTimeUnitWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.tryLockAndGet(rl, 1, TimeUnit.MINUTES, () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run tryLockAndRun(Lock,Duration,Runnable) method and have the inside function to throw exception")
    @Test
    void testTryLockAndRunDurationWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.tryLockAndRun(rl, Duration.ofMinutes(1), () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }

    @DisplayName("Attempt to run tryLockAndGet(Lock,Duration,Supplier) method and have the inside function to throw exception")
    @Test
    void testTryLockAndGetDurationWithException(){

      // Create lock
      ReentrantLock rl = new ReentrantLock();

      // Exception
      FileNotFoundException exception = new FileNotFoundException("Where is that file? " + RNG.nextInt());

      // Do it
      FileNotFoundException thrown = assertThrows(
        FileNotFoundException.class,
        () -> AutoLock.tryLockAndGet(rl, Duration.ofMinutes(1), () -> {
          throw exception;
        })
      );

      // Get cause and compare
      assertEquals(exception, thrown);
    }
  }
}
