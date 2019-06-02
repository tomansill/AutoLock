# AutoLock

A wrapper library that wraps `Lock` and allows you to use locks inside Try-with-resources scopes to automatically unlock locks when it exits the try scope.

By Tom Ansill

## Motivation

I have been using `Lock` for a while now and I'm getting tired of writing like this:

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
* Maven
* [My Java Validation library](https://github.com/tomansill/JavaValidation)

## Download

**No Maven Repository available yet ):**

For now, you need to build and install it on your machine.

```bash
$ git clone https://github.com/tomansill/autolock
$ cd autolock
$ mvn install
```

Then include the dependency in your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.ansill.lock</groupId>
    <artifactId>AutoLock</artifactId>
    <version>0.2.0</version>
</dependency>
```

## How to use

You wrap a `Lock` object using `ALock` (AutoLock implementation) to create a `AutoLock` object like this:

```
AutoLock lock = new ALock(new ReentrantLock());
```

Then you can go ahead and lock it. 
When you call `doLock()` to perform a lock, `LockedAutoLock` object will be created. 
`LockedAutoLock` is an `AutoCloseable` resource where it will be closed when it exits Try-with-resources block. 
When it is "closed", it will call `unlock()`. 
It is important that you don't "lose" `LockedAutoLock` object or you will never be able to unlock.

```
try(LockedAutoLock locked = lock.doLock()){ // Locked

    // Do stuff

} // Unlocks automatically when arrived here
```

Pretty cool, huh?

There's other methods that you can use:

* `doLockInterruptibly()` - calls into `Lock::lockInterruptibly()`
* `doTryLock()` - calls into `Lock::tryLock()` throws `TimeoutException` if `Lock::tryLock()` returns `false`;
* `doTryLock(long,TimeUnit)` - calls into `Lock::tryLock(long,TimeUnit)` throws `TimeoutException` if `Lock::tryLock()` returns `false`;

There's a couple of static methods that you can use to quickly create locks instead of using `ALock` constructor. Example with `doLock(Lock)`:

```
try(LockedAutoLock locked = ALock.doLock(new ReentrantLock()){

    // Do stuff

}
```