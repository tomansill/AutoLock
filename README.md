# AutoLock

A small wrapper library around `Lock` that allows you to use locks inside try-with-resources scopes, automatically
unlocking when the scope exits.

It also provides helper methods that let you use lambda functions with locks, avoiding try-with-resources entirely.

By Tom Ansill

## Motivation

I have been using `Lock` a lot for a while now, and I'm getting tired of writing like this:

```java

Lock lock = new ReentrantLock();

lock.

lock();
try{
				// Do stuff
				}finally{
				lock.

unlock();
}

```

You always have to remember the `finally` block. If you forget it, your locks break and you can get stuck in bad
concurrency states. Also, it’s a bit noisy and repetitive.

I really like Java’s try-with-resources pattern, and I wanted the same ergonomics for Lock. So I built this library.

Now it becomes:

```java
Lock lock = new ReentrantLock();

try(
LockedAutoLock ignored = AutoLock.lock(lock)){
				// Do stuff
				}
```

Then I extended it further with lambda-based helpers so you don’t even need try-with-resources in simple cases:

```java
Lock lock = new ReentrantLock();

AutoLock.

lockAndRun(lock, () ->{
				// Do stuff
				});
```

You can also use supplier-style methods when you need a return value:

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

### Build and Install

Maven is required to build and install the library:

```sh
$ git clone https://github.com/tomansill/autolock
$ cd autolock
$ mvn install
```

## Usage

`AutoLock` is a purely static utility. You don’t need to create any `AutoLock` instance. All operations work directly on
a Lock.

### Using in Try-with-resources statement

At the core, you can acquire a lock and use try-with-resources like this:

```java
Lock lock = new ReentrantLock();

try(
LockedAutoLock ignored = AutoLock.lock(lock)){
				// Do stuff here
				} // automatically unlocked here
```

When using AutoLock with try-with-resources, the returned LockedAutoLock does not need to be used directly. It is only
there to bind the lock lifecycle to the scope, so that the lock is automatically released when the block exits. For
this reason, it is common to name the variable `ignored` to make it clear that it is not meant to be used. You can
also use any variable name if you prefer, for example lockHandle, but the object itself is not intended for direct use.

There are 2 available static methods you can use: `lock(Lock)` and `lockInterruptibly(Lock)`. These correspond to the
standard `Lock.lock()` and `Lock.lockInterruptibly()` methods respectively.

Unfortunately, there are no `tryLock` or `newCondition` equivalents available for use inside a try-with-resources
statement.

### Lambda methods

Try-with-resources can be avoided entirely while still getting the same guarantees by using lambda-based methods such as
`AutoLock.lockAndRun(Lock,ThrowableRunnable<T>)`:

```java
Lock lock = new ReentrantLock();

// Will lock, run, then unlock when this method exits
AutoLock.

lockAndRun(lock, () ->{
				// Do stuff here
				});
```

`AutoLock.lockAndRun(Lock,ThrowableRunnable<T>)` will first attempt to acquire the lock, then run the supplied runnable,
and finally automatically release the lock, regardless of whether the runnable completes normally or throws an
exception.

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

It is important to understand `ThrowableRunnable<T>` and `ThrowableSupplier<R, T>`. These are functional interfaces
similar to `Runnable` and `Supplier`, but they allow throwing any `Throwable`, including checked exceptions. This is
used as a convenience to avoid wrapping checked exceptions in `RuntimeException`. Instead, any exception thrown inside
the lambda is directly propagated by the method. Because these interfaces are parameterized by a single type `T`, they
can only represent one throwable type per invocation. If multiple different checked exceptions may be thrown inside the
lambda, Java’s type inference will require them to be compatible through a common supertype.

For example, if a lambda may throw both `IOException` and `SQLException`, the inferred type will typically widen to
their shared superclass (such as `Exception`). As a result, the method will declare and propagate that common type
rather than multiple distinct checked exceptions.

Because `Throwable` is the upper bound, these interfaces can technically propagate any exception type, including
unchecked exceptions and serious errors such as `OutOfMemoryError` or `StackOverflowError`. However, `AutoLock` itself
does not introduce or transform exceptions. It simply ensures the lock is released and then propagates the original
exception as-is.

`AutoLock` has several static lambda locking methods available:

#### Blocking

- `void lockAndRun(Lock,ThrowableRunnable<T>)`
- `<R> R lockAndGet(Lock,ThrowableSupplier<R, T>)`
- `void lockInterruptiblyAndRun(Lock,ThrowableRunnable<T>)`
- `<R> R lockInterruptiblyAndGet(Lock,ThrowableSupplier<R, T>)`

#### Non-blocking

- `void tryLockAndRun(Lock,ThrowableRunnable<T1>,ThrowableRunnable<T2>)`
- `<R> R tryLockAndGet(Lock,ThrowableSupplier<R, T1>,ThrowableSupplier<R, T2>)`

#### Timed

- `void tryLockAndRun(Lock,long,TimeUnit,ThrowableRunnable<T1>,ThrowableRunnable<T2>)`
- `<R> R tryLockAndGet(Lock,long,TimeUnit,ThrowableSupplier<R, T1>,ThrowableSupplier<R, T2>)`
- `void tryLockAndRun(Lock,Duration,ThrowableRunnable<T1>,ThrowableRunnable<T2>)`
- `<R> R tryLockAndGet(Lock,Duration,ThrowableSupplier<R, T1>,ThrowableSupplier<R, T2>)`