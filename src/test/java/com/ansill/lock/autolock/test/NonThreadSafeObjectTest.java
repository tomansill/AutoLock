package com.ansill.lock.autolock.test;

import com.ansill.lock.autolock.NonThreadSafeObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NonThreadSafeObjectTest{

  @DisplayName("Test NonThreadSafeObject canary to make sure it actually works")
  @Test
  void testNonThreadSafeObject() throws InterruptedException{

    // Set up original number
    int original = new Random().nextInt();

    // Create thread-sensitive object
    NonThreadSafeObject<Integer> object = new NonThreadSafeObject<>(original);

    // Set up the desired number
    int desiredNumber = original - 1;
    int badNumber = original + 1;

    // Data CDL
    CountDownLatch dataCDL = new CountDownLatch(1);

    // Modify start CDL
    CountDownLatch modifyStartCDL = new CountDownLatch(1);

    // Start it
    Thread thread = new Thread(() -> object.modify(desiredNumber, modifyStartCDL, dataCDL));
    thread.start();

    // Wait for thread start
    modifyStartCDL.await();

    // Modify it with bad number
    object.modify(badNumber);

    // Proceed with modify in other thread
    dataCDL.countDown();

    // Wait for thread to finish
    thread.join();

    // Check if data is corrupted
    assertTrue(object.isCorrupted());

    // Check value
    assertEquals(original, object.getValue());
  }
}
