# AutoLock

A wrapper library that wraps `Lock` and allows you to use locks inside Try-with-resources scopes to automatically unlock locks when it exits the try scope.

By Tom Ansill

## Motivation

I have been using `Lock` a lot for a while now, and I'm getting tired of writing like this:

```

Lock lock = new ReentrantLock();

try{
    lock.lock();
    
    // Do stuff
}finally{
    lock.unlock();
}

```

Where I will need to remember to write `finally` block. 
If I forget, then my locks won't work properly. 
Also, coding with `try` with `finally` is pretty ugly.

I liked how Java 8's Try-with-resources works and I thought that it could be applied to `Lock`s too.
So I created this library.

## Prerequisites

* Java 8 or better
* [My Java Validation library](https://github.com/tomansill/JavaValidation)

## Download and Install

### Package Repository

The library is availble for download on Sonatype public Maven repository (https://oss.sonatype.org/#nexus-search;quick~com.ansill.lock).
```xml
<dependency>
  <groupId>com.ansill.lock</groupId>
  <artifactId>AutoLock</artifactId>
  <version>0.3.1</version>
</dependency>
```

### Build and Install

Maven (or other similar build tools) is needed to build and install JavaUtility

```sh
$ git clone https://github.com/tomansill/autolock
$ cd autolock
$ mvn install
```

## Usage

To create `AutoLock`, you need an original `Lock` and create `AutoLock` with it using `AutoLock.create(Lock)` like this:

```java
// Create lock
Lock lock = new ReentrantLock();

// Wraps the original lock in AutoLock
AutoLock autoLock = AutoLock.create(lock);
```

Then you can perform locks in Try-with-resources block with `AutoLock` like this:

```java
try(LockedAutoLock lockedAutoLock = autoLock.doLock()){
    
  // Do stuff here

} // Lock will be automatically be unlocked at this line
```

`LockedAutoLock` is an `AutoCloseable` reference to `AutoLock`'s lock operation. Its `close()` operation will call `Lock::unlock` method. So, when try-with-resources exits, it will guarantee an unlock before continuing despite successful execution or with exceptions.

### Class Methods

`AutoLock` has several locking methods:

- `doLock()` - Acquires a lock. Same as `Lock::lock()`. Returns `LockedAutoLock`.
- `doLockInterruptibly()` - Acquires a lock unless the current thread is interrupted. Same as `Lock::lockInterruptibly()`. Returns `LockedAutoLock`.
- `doTryLock()` - Acquires the lock only if it is free at the time of invocation. Same as `Lock::tryLock()`. Returns `LockedAutoLock`.
- `doTryLock(long,TimeUnit)` - Acquires the lock if it is free within the given waiting time and the current thread has not been interrupted. Same as `Lock::tryLock(long,TimeUnit)`. Returns `LockedAutoLock`.
- `doTryLock(Duration)` - Same as `doTryLock(long,TimeUnit)` but `Duration` is used instead of `long` and `TimeUnit` combination. Returns `LockedAutoLock`.

### Static Methods

You do not need to create `AutoLock` in order to use it. `AutoLock` has static methods that you can use to directly lock.

#### `LockedAutoLock` methods

Using `Lock`, you can just create `LockedAutoLock` directly like this:

```java
Lock lock = new ReentrantLock();

try(LockedAutoLock lockedAutoLock = AutoLock.doLock(lock)){
    
  // Do stuff here

}
```

`AutoLock` has several static locking methods:

- `doLock(Lock)`
- `doLockInterruptibly(Lock)`
- `doTryLock()`
- `doTryLock(long,TimeUnit)`
- `doTryLock(Duration)`

#### Lambda methods

You can avoid using Try-with-resources and `LockedAutoLock` with `AutoLock.lockAndPerform(Lock,Runnable)` with lambdas like this:

```java
Lock lock = new ReentrantLock();

// Will unlock automatically when this method exits
AutoLock.lockAndPerform(lock, () -> {

  // Do stuff here

});
```

If you have something you want to return after the lock completes, Use `AutoLock.lockAndGet(Lock,Supplier<T>)` instead like this.

```java
Lock lock = new ReentrantLock();

// Will unlock automatically when this method exits
int value = AutoLock.lockAndGet(lock, () -> {

  // Do stuff here

  // Return value
  return 100;

});
```

`AutoLock` has several static lambda locking methods:

- `lockAndPerform(Lock,Runnable)`
- `lockAndGet(Lock,Supplier<T>)`
- `lockInterruptiblyAndPerform(Lock,Runnable)`
- `lockInterruptiblyAndGet(Lock,Supplier<T>)`
- `tryLockAndPerform(Lock,Runnable)`
- `tryLockAndGet(Lock,Supplier<T>)`
- `tryLockAndPerform(Lock,long,TimeUnit,Runnable)`
- `tryLockAndGet(Lock,long,TimeUnit,Supplier<T>)`
- `tryLockAndPerform(Lock,Duration,Runnable)`
- `tryLockAndGet(Lock,Duration,Supplier<T>)`

#### Lambda with `Exception`

Suppose you have some checked exceptions thrown inside of your `Runnable` and `Supplier` which can be painful to deal with; `AutoLock.Ex` has methods that are exactly same as the regular lambda locking methods but with `Exception` handling. All `Runnable` are replaced with `RunnableWithException` and `Supplier` to `SupplierWithException`. Those methods will wrap any `Exception` into `ExecutionException`

Example:

```java
Lock lock = new ReentrantLock();

try{
    AutoLock.Ex.lockAndPerform(lock, () -> {
    
      if(Math.random() < 0.5) throw new Exception("Error!");
    
    });
}catch(ExecutionException exException){
  Exception exception = (Exception) exException.getCause();
  throw exception;
}
```

**NOTE:** This exists as an option for users to use, but you probably should use Try-with-resources if you're dealing with a lot of checked exceptions that may be thrown during the lock.

`AutoLock.Ex` has several static lambda locking methods:

- `lockAndPerform(Lock,RunnableWIthException)`
- `lockAndGet(Lock,SupplierWithException<T>)`
- `lockInterruptiblyAndPerform(Lock,RunnableWIthException)`
- `lockInterruptiblyAndGet(Lock,SupplierWithException<T>)`
- `tryLockAndPerform(Lock,RunnableWIthException)`
- `tryLockAndGet(Lock,SupplierWithException<T>)`
- `tryLockAndPerform(Lock,long,TimeUnit,RunnableWIthException)`
- `tryLockAndGet(Lock,long,TimeUnit,SupplierWithException<T>)`
- `tryLockAndPerform(Lock,Duration,RunnableWIthException)`
- `tryLockAndGet(Lock,Duration,SupplierWithException<T>)`