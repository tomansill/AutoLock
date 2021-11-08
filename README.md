# AutoLock

A wrapper library that wraps `Lock` and allows you to use locks inside Try-with-resources scopes to automatically unlock
locks when it exits the try scope.

The wrapper library also have methods that allows you to use lambda functions with locks instead of using
Try-with-resources.

By Tom Ansill

## Motivation

I have been using `Lock` a lot for a while now, and I'm getting tired of writing like this:

```java

Lock lock = new ReentrantLock();

try{  
  lock.lock();
    
  // Do stuff
  
}finally{
  lock.unlock();
}

```

Where I will need to remember to write `finally` block. If I forget, then my locks won't work properly. Also, coding
with `try` with `finally` is pretty ugly and takes a lot of lines.

I liked how Java 8's Try-with-resources works, and I thought that it could be applied to `Lock`s too. So I created this
library and made the locking process much easier like this:

```java
Lock lock = new ReentrantLock();

try(LockedAutoLock ignored = AutoLock.doLock(lock)){
  
  // Do stuff
  
}
```

Then I added methods that allows us to use locks with Java 8 lambda functions to make it even easier to use.

```java
Lock lock = new ReentrantLock();

AutoLock.lockAndRun(lock, () -> {
  
  // Do stuff
  
});
```

Also, supplier functions are available to use to simplify variable initialization while using locks like this:

```java
Lock lock = new ReentrantLock();

int value = AutoLock.lockAndGet(lock, () -> {
  
  // Do stuff
  
  // Return value
  return 100;
});
```

## Prerequisites

* Java 8 or better
* [My Java Validation library](https://github.com/tomansill/JavaValidation)

## Download and Install

### Package Repository

The library is available for download on Sonatype public Maven
repository (https://oss.sonatype.org/#nexus-search;quick~com.ansill.lock).
```xml
<dependency>
  <groupId>com.ansill.lock</groupId>
  <artifactId>AutoLock</artifactId>
  <version>0.4.0</version>
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

Let's start with the basics. `AutoLock` is a wrapper class that you wrap your `Lock` in then you can invoke `AutoLock`
commands and use it in Try-with-resources scopes.

To create `AutoLock` object, you need an original `Lock` and create `AutoLock` with it using `AutoLock.create(Lock)`
like this:

```java
// Create lock
Lock lock = new ReentrantLock();

// Wraps the original lock in AutoLock
AutoLock autoLock = AutoLock.create(lock);
```

Then you can lock the lock in Try-with-resources scope with `AutoLock` class like this:

```java
try(LockedAutoLock lockedAutoLock = autoLock.doLock()){
    
  // Do stuff here

} // Lock will be automatically be unlocked at this line
```

`LockedAutoLock` is an `AutoCloseable` reference to `AutoLock`'s lock operation. Its `close()` operation will
call `Lock::unlock` method. So, when try-with-resources exits, it will guarantee that the lock will unlock before
continuing regardless of successful execution or failure due to exceptions being thrown.

### Class Methods

`AutoLock` has several locking methods:

- `doLock()` - Acquires a lock. Same as `Lock.lock()`. Returns `LockedAutoLock`.
- `doLockInterruptibly()` - Acquires a lock unless the current thread is interrupted. Same
  as `Lock::lockInterruptibly()`. Returns `LockedAutoLock`.
- `doTryLock()` - Acquires the lock only if it is free at the time of invocation. Same as `Lock.tryLock()`.
  Returns `LockedAutoLock`.
- `doTryLock(long,TimeUnit)` - Acquires the lock if it is free within the given waiting time and the current thread has
  not been interrupted. Same as `Lock.tryLock(long,TimeUnit)`. Returns `LockedAutoLock`.
- `doTryLock(Duration)` - Same as `doTryLock(long,TimeUnit)` but `Duration` is used instead of `long` and `TimeUnit`
  combination. Returns `LockedAutoLock`.

### Static Methods

Creating `AutoLock` objects is not necessary to take advantage of Try-with-resources. `AutoLock` has static methods that
you can use to directly lock your `Lock`s without creating any objects.

#### `LockedAutoLock` methods

Using `Lock`, you can just create `LockedAutoLock` directly like this:

```java
Lock lock = new ReentrantLock();

try(LockedAutoLock lockedAutoLock = AutoLock.doLock(lock)){
    
  // Do stuff here

}
```

If you have Java 11 or above, you can just use `var` like this:

```java
Lock lock = new ReentrantLock();

try(var locked = AutoLock.doLock(lock)){
    
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

Try-with-resources can be avoided entirely and still get the same benefits by using lambda functions
in `AutoLock.lockAndRun(Lock,ThrowableRunnable<T>)` function like this:

```java
Lock lock = new ReentrantLock();

// Will lock, run, then unlock when this method exits
AutoLock.lockAndRun(lock, () -> {

  // Do stuff here

});
```

`AutoLock.lockAndRun(Lock,ThrowableRunnable<T>)` will first attempt to lock your `Lock`, then runs the supplied
runnable, then it will automatically unlock your `Lock` regardless of success or failure of your runnable function.

If you have something you want to return after the lock completes,
Use `AutoLock.lockAndGet(Lock,ThrowableSupplier<R,T>)` instead like this.

```java
Lock lock = new ReentrantLock();

// Will lock, retrieve, unlock, then return (if no exception) when this method exits
int value = AutoLock.lockAndGet(lock, () -> {

  // Do stuff here

  // Return value
  return 100;

});
```

Note: `ThrowableRunner<T>` and `ThrowableSupplier<R,T>` throws `<T>` generic that extends `Throwable` so you can use
checked exceptions inside of lambdas and the locking methods will throw the checked exception. There's something to note
that if there's multiple checked `Exception`s in either lambdas, the function cannot throw both of those checked
`Exception`s, it can only throw their "least common" super-class of `Exception` which is usually `Exception` or
`Throwable`.

`AutoLock` has several static lambda locking methods:

- `lockAndRun(Lock,Runnable)`
- `lockAndGet(Lock,Supplier<T>)`
- `lockInterruptiblyAndRun(Lock,Runnable)`
- `lockInterruptiblyAndGet(Lock,Supplier<T>)`
- `tryLockAndRun(Lock,Runnable)`
- `tryLockAndGet(Lock,Supplier<T>)`
- `tryLockAndRun(Lock,long,TimeUnit,Runnable)`
- `tryLockAndGet(Lock,long,TimeUnit,Supplier<T>)`
- `tryLockAndRun(Lock,Duration,Runnable)`
- `tryLockAndGet(Lock,Duration,Supplier<T>)`