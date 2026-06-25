package com.ansill.autolock;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.LongSupplier;

/**
 * Utility class for working with {@link Lock} instances using try-with-resources semantics.
 *
 * <p>This class provides a set of convenience methods that wrap a {@link Lock} into an
 * AutoCloseable-style resource, ensuring proper unlocking even in the presence of exceptions.</p>
 *
 * <p>It also provides higher-level helper methods for executing code under lock protection
 * using either Runnable or Supplier-based APIs.</p>
 */
public final class AutoLock {

	/**
	 * Prevent instantiation.
	 */
	private AutoLock() {
		throw new UnsupportedOperationException(String.format("%s is a utility class and cannot be instantiated", AutoLock.class.getSimpleName()));
	}

	@SuppressWarnings("resource")
	@NonNull
	private static LockedAutoLock multipleLock(@NonNull Lock[] locks) {

		// Mutable boolean pointer so it can be mutated inside other function and changes be propagated back
		MutableBoolean success = new MutableBoolean(false);

		// Attempt to lock the first one
		locks[0].lock();
		try {

			// Do the second one
			locks[1].lock();
			try {

				// Check if we need to lock more
				if (locks.length == 2) {

					// Only 2 locks, no need to do a recursive lock, just set up unlock for close(), then exit
					success.value = true;
					return new LockedAutoLock(() -> {
						try {
							locks[1].unlock();
						} finally {
							locks[0].unlock();
						}
					});
				}

				// Recursive walk through the remaining locks and lock them
				LockedAutoLock lockedAutoLock = recursiveMultipleLock(2, locks, success);

				// Set up close method that unlocks the inner locks, then lock the remaining locks that has been
				// locked in this method
				return new LockedAutoLock(() -> {
					try {
						lockedAutoLock.close();
					} finally {
						try {
							locks[1].unlock();
						} finally {
							locks[0].unlock();
						}
					}
				});
			} finally {
				// Unlock only if inner locking process fails because there'd be no LockedAutoLock returned
				if (!success.value) locks[1].unlock();
			}
		} finally {
			// Unlock only if inner locking process fails because there'd be no LockedAutoLock returned
			if (!success.value) locks[0].unlock();
		}
	}

	/**
	 * Creates a {@link WithLock} builder for a single {@link Lock}.
	 *
	 * <p><b>No locking is performed by this method.</b> This is a lazy, fluent builder
	 * used to configure how the lock should be acquired and how work should be executed
	 * under the lock.</p>
	 *
	 * <p>The returned {@link WithLock} allows selecting the locking strategy, such as:
	 * <ul>
	 *   <li>{@code lock()} (blocking)</li>
	 *   <li>{@code lockInterruptibly()}</li>
	 *   <li>{@code tryLock()} / timed tryLock</li>
	 * </ul>
	 *
	 * <p>Lock acquisition only occurs when a <i>terminal operation</i> is invoked,
	 * such as {@code run(...)} or {@code get(...)}. Until then, no interaction with
	 * the underlying lock takes place.</p>
	 *
	 * @param lock the lock to operate on
	 * @return a {@link WithLock} builder for configuring lock behavior
	 * @throws NullPointerException if {@code lock} is null
	 */
	@NonNull
	public static WithLock with(@NonNull Lock lock) {
		Objects.requireNonNull(lock, "lock must not be null");
		return new SingleLock(lock);
	}

	/**
	 * Creates a {@link MultipleLocks} builder for coordinating multiple {@link Lock} instances.
	 *
	 * <p><b>No locking is performed by this method.</b> This method only collects the provided
	 * locks and returns a fluent builder used to configure how they should be acquired and
	 * how work should be executed while holding them.</p>
	 *
	 * <p>The returned {@link MultipleLocks} allows selecting the locking strategy, such as:
	 * <ul>
	 *   <li>{@code lock()} (blocking, all locks acquired in order)</li>
	 *   <li>{@code lockInterruptibly()}</li>
	 *   <li>{@code tryLock()} / timed tryLock</li>
	 * </ul>
	 *
	 * <p>Locks are acquired <b>in the order provided</b> when a terminal operation is invoked.
	 * It is the caller's responsibility to ensure a consistent global lock ordering across
	 * threads to avoid deadlocks.</p>
	 *
	 * <p>Lock acquisition only occurs when a <i>terminal operation</i> is invoked,
	 * such as {@code run(...)} or {@code get(...)}. Until then, no interaction with
	 * the underlying locks takes place.</p>
	 *
	 * @param lock1 the first lock (must not be null)
	 * @param lock2 the second lock (must not be null)
	 * @param locks additional locks (must not be null and must not contain null elements)
	 * @return a {@link MultipleLocks} builder for configuring coordinated locking behavior
	 * @throws NullPointerException if any lock is null
	 */
	@NonNull
	public static MultipleLocks with(@NonNull Lock lock1, @NonNull Lock lock2, @NonNull Lock... locks) {
		Objects.requireNonNull(lock1, "lock1 must not be null");
		Objects.requireNonNull(lock2, "lock2 must not be null");
		Objects.requireNonNull(locks, "locks must not be null");
		for (int i = 0; i < locks.length; i++) {
			Objects.requireNonNull(locks[i], String.format("lock%s must not be null", i + 3));
		}
		Lock[] fullLocks = new Lock[2 + locks.length];
		fullLocks[0] = lock1;
		fullLocks[1] = lock2;
		System.arraycopy(locks, 0, fullLocks, 2, locks.length);
		return new MultipleLocks(fullLocks);
	}

	/**
	 * Creates a {@link WithLock} builder from a collection of {@link Lock} instances.
	 *
	 * <p><b>No locking is performed by this method.</b> This method only validates and collects the provided locks
	 * and returns a builder used to configure how they should be acquired and how work should be executed while holding
	 * them.</p>
	 *
	 * <p>This overload is intended for cases where locks are already available as a {@link Collection}, and avoids the
	 * need for varargs construction.</p>
	 *
	 * <p><b>Validation rules:</b></p>
	 * <ul>
	 *   <li>The collection must not be {@code null}</li>
	 *   <li>It must not be empty</li>
	 *   <li>It must not contain {@code null} elements</li>
	 * </ul>
	 *
	 * <p><b>Behavior:</b></p>
	 * <ul>
	 *   <li>If the collection contains a single lock, a {@link SingleLock} builder is returned</li>
	 *   <li>If the collection contains multiple locks, a {@link MultipleLocks} builder is returned</li>
	 * </ul>
	 *
	 * <p><b>Ordering guarantee:</b><br>
	 * Lock acquisition order depends on the iteration order of the provided {@link Collection}
	 * and its {@code toArray} implementation. Therefore:</p>
	 * <ul>
	 *   <li>Ordered collections (e.g., {@link java.util.List}) preserve deterministic ordering</li>
	 *   <li>Unordered collections (e.g., {@link java.util.Set}) do not guarantee stable ordering</li>
	 * </ul>
	 *
	 * <p>Callers are responsible for ensuring a consistent global lock ordering strategy to avoid deadlocks.</p>
	 *
	 * <p><b>Lazy execution:</b><br>
	 * No interaction with the underlying locks occurs until a terminal operation is invoked
	 * on the returned {@link WithLock} (e.g., {@code run(...)} or {@code get(...)}).</p>
	 *
	 * @param locks the collection of locks to coordinate; must not be null, empty, or contain null elements
	 * @return a {@link WithLock} builder for configuring coordinated lock execution
	 * @throws NullPointerException     if {@code locks} is null or contains null elements
	 * @throws IllegalArgumentException if {@code locks} is empty
	 */
	@NonNull
	public static WithLock with(@NonNull Collection<? extends Lock> locks) {
		// Ensure input collection is valid
		requireNonNullElements(locks);
		if (locks.size() == 1) {
			return new SingleLock(locks.iterator().next());
		}
		return new MultipleLocks(locks.toArray(new Lock[0]));
	}

	private static void requireNonNullElements(@NonNull Collection<? extends Lock> locks) {
		Objects.requireNonNull(locks, "locks must not be null");
		if (locks.isEmpty()) {
			throw new IllegalArgumentException("locks must not be empty");
		}
		if (locks instanceof List) {
			List<? extends Lock> list = (List<? extends Lock>) locks;
			for (int i = 0; i < list.size(); i++) {
				Objects.requireNonNull(list.get(i), String.format("lock%s must not be null", i + 1));
			}
		} else {
			if (locks.stream().anyMatch(Objects::isNull)) {
				throw new NullPointerException("locks must not contain null elements");
			}
		}
	}

	/**
	 * Acquires the given lock and returns an {@link LockedAutoLock} that will release the lock
	 * when closed.
	 *
	 * <p>This method blocks until the lock is acquired.</p>
	 *
	 * @param lock the lock to acquire
	 * @return a scoped lock that will release the lock on close
	 * @throws NullPointerException if {@code lock} is null
	 */
	@NonNull
	public static LockedAutoLock lock(@NonNull Lock lock) {
		Objects.requireNonNull(lock, "lock must not be null");
		lock.lock();
		return new LockedAutoLock(lock::unlock);
	}

	/**
	 * Acquires multiple {@link Lock}s and returns an {@link LockedAutoLock} that will release them when closed.
	 *
	 * <p>This overload accepts at least two required locks followed by an optional varargs array of additional locks.
	 * It is a convenience method for callers who already have multiple lock references without needing to
	 * construct a collection or array explicitly.</p>
	 *
	 * <p><b>Validation rules:</b></p>
	 * <ul>
	 *   <li>{@code lock1} must not be {@code null}</li>
	 *   <li>{@code lock2} must not be {@code null}</li>
	 *   <li>{@code locks} array must not be {@code null}</li>
	 *   <li>None of the elements in {@code locks} may be {@code null}</li>
	 * </ul>
	 *
	 * <p><b>Lock acquisition order:</b><br>
	 * {@code lock1} → {@code lock2} → {@code locks[0..n]}.
	 * This ordering is preserved when delegating to the underlying multi-lock implementation.</p>
	 *
	 * <p><b>Deadlock safety:</b><br>
	 * This method does not perform any reordering or deduplication of locks. Callers are responsible
	 * for ensuring a consistent global lock ordering strategy to avoid deadlocks.</p>
	 *
	 * @param lock1 the first lock to acquire; must not be null
	 * @param lock2 the second lock to acquire; must not be null
	 * @param locks additional locks to acquire (optional); must not be null and must not contain null elements
	 * @return a {@link LockedAutoLock} that will release all acquired locks when closed
	 * @throws NullPointerException if {@code lock1}, {@code lock2}, {@code locks}, or any element in {@code locks} is null
	 */
	@NonNull
	public static LockedAutoLock lock(@NonNull Lock lock1, @NonNull Lock lock2, @NonNull Lock... locks) {

		// Ensure input parameters are valid
		Objects.requireNonNull(lock1, "lock1 must not be null");
		Objects.requireNonNull(lock2, "lock2 must not be null");
		Objects.requireNonNull(locks, "locks must not be null");
		for (int i = 0; i < locks.length; i++) {
			Objects.requireNonNull(locks[i], String.format("lock%s must not be null", i + 3));
		}

		// Bundle up the locks into a single array
		Lock[] fullLocks = new Lock[2 + locks.length];
		fullLocks[0] = lock1;
		fullLocks[1] = lock2;
		System.arraycopy(locks, 0, fullLocks, 2, locks.length);

		// Lock and return
		return multipleLock(fullLocks);
	}

	/**
	 * Acquires a set of {@link Lock}s and returns an {@link LockedAutoLock} that will release them when closed.
	 *
	 * <p>This method enforces strict validation of the input collection:</p>
	 * <ul>
	 *   <li>The collection itself must not be {@code null}</li>
	 *   <li>It must not contain {@code null} elements</li>
	 *   <li>It must not be empty</li>
	 * </ul>
	 *
	 * <p><strong>Ordering guarantee:</strong><br>
	 * Lock acquisition order depends on the iteration order of the input {@link Collection}
	 * and its {@code toArray} implementation. Therefore:</p>
	 * <ul>
	 *   <li>Ordered collections (e.g., {@link java.util.List}) will preserve lock order.</li>
	 *   <li>Unordered collections (e.g., {@link java.util.Set}) do not guarantee a stable ordering.</li>
	 * </ul>
	 *
	 * <p>Passing an unordered collection may result in non-deterministic lock ordering, which is discouraged
	 * when consistent locking order is required to avoid potential deadlocks.</p>
	 *
	 * @param locks the collection of locks to acquire; must be non-null, non-empty, and contain no null elements
	 * @return a {@link LockedAutoLock} that will release all acquired locks when closed
	 * @throws NullPointerException     if {@code locks} is null or contains null elements
	 * @throws IllegalArgumentException if {@code locks} is empty
	 */
	@NonNull
	public static LockedAutoLock lock(@NonNull Collection<? extends Lock> locks) {
		// Ensure input collection is valid
		requireNonNullElements(locks);
		if (locks.size() == 1) {
			Lock lock = locks.iterator().next();
			lock.lock();
			return new LockedAutoLock(lock::unlock);
		}
		return multipleLock(locks.toArray(new Lock[0]));
	}

	/**
	 * Acquires a set of {@link Lock}s interruptibly and returns an {@link LockedAutoLock}
	 * that will release them when closed.
	 *
	 * <p>This method is equivalent in behavior to {@link #lock(Collection)}, except that it responds to thread
	 * interruption while attempting to acquire the locks.</p>
	 *
	 * <p>If the current thread is interrupted while waiting to acquire any of the locks, an
	 * {@link InterruptedException} is thrown and any locks already acquired during the attempt are released
	 * before returning.</p>
	 *
	 * <p>Input validation rules are identical to {@link #lock(Collection)}:</p>
	 * <ul>
	 *   <li>The collection must not be {@code null}</li>
	 *   <li>It must not contain {@code null} elements</li>
	 *   <li>It must not be empty</li>
	 * </ul>
	 *
	 * <p><strong>Ordering guarantee:</strong><br>
	 * Lock acquisition order depends on the iteration order of the input {@link Collection}
	 * and its {@code toArray} implementation. Therefore:</p>
	 * <ul>
	 *   <li>Ordered collections (e.g., {@link java.util.List}) preserve lock ordering</li>
	 *   <li>Unordered collections (e.g., {@link java.util.Set}) do not guarantee deterministic ordering</li>
	 * </ul>
	 *
	 * <p>Passing an unordered collection is discouraged when consistent lock ordering is required.</p>
	 *
	 * @param locks the collection of locks to acquire interruptibly; must be non-null,
	 *              non-empty, and contain no null elements
	 * @return a {@link LockedAutoLock} that will release all acquired locks when closed
	 * @throws NullPointerException     if {@code locks} is null or contains null elements
	 * @throws IllegalArgumentException if {@code locks} is empty
	 * @throws InterruptedException     if the current thread is interrupted while acquiring locks
	 */
	@NonNull
	public static LockedAutoLock lockInterruptibly(@NonNull Collection<? extends Lock> locks) throws InterruptedException {
		// Ensure input collection is valid
		requireNonNullElements(locks);
		if (locks.size() == 1) {
			Lock lock = locks.iterator().next();
			lock.lockInterruptibly();
			return new LockedAutoLock(lock::unlock);
		}
		return multipleLockInterruptibly(locks.toArray(new Lock[0]));
	}

	@SuppressWarnings("resource")
	@NonNull
	private static LockedAutoLock recursiveMultipleLock(int index, @NonNull Lock[] locks, @NonNull MutableBoolean success) {

		// Grab the current lock and lock that one
		Lock currentLock = locks[index++];
		currentLock.lock();

		try {

			// If no more locks left to lock, mark as success and build close() for later unlock
			if (index == locks.length) {
				success.value = true;
				return new LockedAutoLock(currentLock::unlock);
			}

			// Continue with inner locking then build close() that unlocks the inner lock then lock from this level
			LockedAutoLock innerLock = recursiveMultipleLock(index, locks, success);
			return new LockedAutoLock(() -> {
				try {
					innerLock.close();
				} finally {
					currentLock.unlock();
				}
			});

		} finally {
			// Unlock only if inner locking process fails because there'd be no LockedAutoLock returned
			if (!success.value) currentLock.unlock();
		}
	}

	/**
	 * Acquires multiple {@link Lock}s interruptibly and returns an {@link LockedAutoLock}
	 * that will release them when closed.
	 *
	 * <p>This overload accepts at least two required locks followed by an optional varargs array of additional locks.
	 * It is a convenience method for callers who already have multiple lock references without needing to construct a
	 * collection or array explicitly.</p>
	 *
	 * <p>This method behaves like {@link #lock(Lock, Lock, Lock...)} except that it responds to thread interruption
	 * while acquiring locks.</p>
	 *
	 * <p>If the current thread is interrupted while attempting to acquire any lock, an {@link InterruptedException}
	 * is thrown and any locks already acquired during the attempt are released before the exception is propagated.</p>
	 *
	 * <p><b>Validation rules:</b></p>
	 * <ul>
	 *   <li>{@code lock1} must not be {@code null}</li>
	 *   <li>{@code lock2} must not be {@code null}</li>
	 *   <li>{@code locks} array must not be {@code null}</li>
	 *   <li>None of the elements in {@code locks} may be {@code null}</li>
	 * </ul>
	 *
	 * <p><b>Lock acquisition order:</b><br>
	 * {@code lock1} → {@code lock2} → {@code locks[0..n]}.
	 * This ordering is preserved when delegating to the underlying interruptible multi-lock implementation.</p>
	 *
	 * <p><b>Deadlock safety:</b><br>
	 * No reordering or deduplication is performed. Callers are responsible for ensuring
	 * a consistent global lock ordering strategy across the system to avoid deadlocks.</p>
	 *
	 * @param lock1 the first lock to acquire; must not be null
	 * @param lock2 the second lock to acquire; must not be null
	 * @param locks additional locks to acquire (optional); must not be null and must not contain null elements
	 * @return a {@link LockedAutoLock} that will release all acquired locks when closed
	 * @throws NullPointerException if {@code lock1}, {@code lock2}, {@code locks}, or any element in {@code locks} is null
	 * @throws InterruptedException if the current thread is interrupted while acquiring locks
	 */
	@NonNull
	public static LockedAutoLock lockInterruptibly(@NonNull Lock lock1, @NonNull Lock lock2, @NonNull Lock... locks) throws InterruptedException {

		// Ensure input parameters are valid
		Objects.requireNonNull(lock1, "lock1 must not be null");
		Objects.requireNonNull(lock2, "lock2 must not be null");
		Objects.requireNonNull(locks, "locks must not be null");
		for (int i = 0; i < locks.length; i++) {
			Objects.requireNonNull(locks[i], String.format("lock%s must not be null", i + 3));
		}

		// Bundle up the locks into a single array
		Lock[] fullLocks = new Lock[2 + locks.length];
		fullLocks[0] = lock1;
		fullLocks[1] = lock2;
		System.arraycopy(locks, 0, fullLocks, 2, locks.length);

		// Lock and return
		return multipleLockInterruptibly(fullLocks);
	}

	@SuppressWarnings("resource")
	@NonNull
	private static LockedAutoLock multipleLockInterruptibly(@NonNull Lock[] locks) throws InterruptedException {

		// Mutable boolean pointer so it can be mutated inside other function and changes be propagated back
		MutableBoolean success = new MutableBoolean(false);

		// Attempt to lock the first one
		locks[0].lockInterruptibly();
		try {

			// Do the second one
			locks[1].lockInterruptibly();
			try {

				// Check if we need to lock more
				if (locks.length == 2) {

					// Only 2 locks, no need to do a recursive lock, just set up unlock for close(), then exit
					success.value = true;
					return new LockedAutoLock(() -> {
						try {
							locks[1].unlock();
						} finally {
							locks[0].unlock();
						}
					});
				}

				// Recursive walk through the remaining locks and lock them
				LockedAutoLock lockedAutoLock = recursiveMultipleLockInterruptibly(2, locks, success);

				// Set up close method that unlocks the inner locks, then lock the remaining locks that has been
				// locked in this method
				return new LockedAutoLock(() -> {
					try {
						lockedAutoLock.close();
					} finally {
						try {
							locks[1].unlock();
						} finally {
							locks[0].unlock();
						}
					}
				});
			} finally {
				// Unlock only if inner locking process fails because there'd be no LockedAutoLock returned
				if (!success.value) locks[1].unlock();
			}
		} finally {
			// Unlock only if inner locking process fails because there'd be no LockedAutoLock returned
			if (!success.value) locks[0].unlock();
		}
	}

	@SuppressWarnings("resource")
	@NonNull
	private static LockedAutoLock recursiveMultipleLockInterruptibly(int index, @NonNull Lock[] locks, @NonNull MutableBoolean success) throws InterruptedException {

		// Grab the current lock and lock that one
		Lock currentLock = locks[index++];
		currentLock.lockInterruptibly();

		try {

			// If no more locks left to lock, mark as success and build close() for later unlock
			if (index == locks.length) {
				success.value = true;
				return new LockedAutoLock(currentLock::unlock);
			}

			// Continue with inner locking then build close() that unlocks the inner lock then lock from this level
			LockedAutoLock innerLock = recursiveMultipleLockInterruptibly(index, locks, success);
			return new LockedAutoLock(() -> {
				try {
					innerLock.close();
				} finally {
					currentLock.unlock();
				}
			});

		} finally {
			// Unlock only if inner locking process fails because there'd be no LockedAutoLock returned
			if (!success.value) currentLock.unlock();
		}
	}

	/**
	 * Acquires the given lock interruptibly and returns an {@link LockedAutoLock}
	 * that will release the lock when closed.
	 *
	 * @param lock the lock to acquire
	 * @return a scoped lock that will release the lock on close
	 * @throws NullPointerException if {@code lock} is null
	 * @throws InterruptedException if the current thread is interrupted while waiting
	 */
	@NonNull
	public static LockedAutoLock lockInterruptibly(@NonNull Lock lock) throws InterruptedException {
		Objects.requireNonNull(lock, "lock must not be null");
		lock.lockInterruptibly();
		return new LockedAutoLock(lock::unlock);
	}

	/**
	 * Acquires the given lock, executes the provided action, and releases the lock.
	 *
	 * <p>The lock is held for the duration of the runnable execution.</p>
	 *
	 * @param lock     the lock to acquire
	 * @param runnable the action to execute under lock protection
	 * @param <T>      the type of exception thrown by the runnable
	 * @throws T                    if the runnable throws an exception
	 * @throws NullPointerException if {@code lock} or {@code runnable} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T extends Throwable> void lockAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T> runnable) throws T {

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(runnable, "runnable must not be null");

		// Lock it
		lock.lock();
		try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {

			// Run it
			runnable.run();
		}
	}

	/**
	 * Acquires the given lock, executes the supplier, releases the lock, and returns the result.
	 *
	 * @param lock     the lock to acquire
	 * @param supplier the computation to execute under lock protection
	 * @param <R>      the return type
	 * @param <T>      the exception type thrown by the supplier
	 * @return the result of the supplier
	 * @throws T                    if the supplier throws an exception
	 * @throws NullPointerException if {@code lock} or {@code supplier} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T extends Throwable> R lockAndGet(@NonNull Lock lock, @NonNull ThrowableSupplier<R, T> supplier) throws T {

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(supplier, "supplier must not be null");

		// Lock it
		lock.lock();
		try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {

			// Get it
			return supplier.get();
		}
	}

	/**
	 * Acquires the given lock interruptibly, executes the provided action, and releases the lock.
	 *
	 * @param lock     the lock to acquire
	 * @param runnable the action to execute under lock protection
	 * @param <T>      the exception type thrown by the runnable
	 * @throws T                    if the runnable throws an exception
	 * @throws InterruptedException if interrupted while waiting for the lock
	 * @throws NullPointerException if {@code lock} or {@code runnable} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T extends Throwable> void lockInterruptiblyAndRun(
					@NonNull Lock lock,
					@NonNull ThrowableRunnable<T> runnable
	) throws T, InterruptedException {

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(runnable, "runnable must not be null");

		// Lock it
		lock.lockInterruptibly();
		try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {

			// Run it
			runnable.run();
		}
	}

	/**
	 * Acquires the given lock interruptibly, executes the supplier, releases the lock,
	 * and returns the result.
	 *
	 * @param lock     the lock to acquire
	 * @param supplier the computation to execute under lock protection
	 * @param <R>      the return type
	 * @param <T>      the exception type thrown by the supplier
	 * @return the result of the supplier
	 * @throws T                    if the supplier throws an exception
	 * @throws InterruptedException if interrupted while waiting for the lock
	 * @throws NullPointerException if {@code lock} or {@code supplier} is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T extends Throwable> R lockInterruptiblyAndGet(
					@NonNull Lock lock,
					@NonNull ThrowableSupplier<R, T> supplier
	) throws T, InterruptedException {

		// Ensure inputs are not null
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(supplier, "supplier must not be null");

		// Lock it
		lock.lockInterruptibly();
		try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {

			// Get it
			return supplier.get();
		}
	}

	/**
	 * Attempts to acquire the lock. If successful, executes {@code onLockSuccess}
	 * while holding the lock. Otherwise, executes {@code onLockFail}.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param onLockSuccess action executed if the lock is acquired
	 * @param onLockFail    action executed if the lock is not acquired
	 * @param <T1>          exception type from success action
	 * @param <T2>          exception type from failure action
	 * @throws T1                   if the success action throws an exception
	 * @throws T2                   if the failure action throws an exception
	 * @throws NullPointerException if any argument is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(@NonNull Lock lock, @NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2 {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				onLockSuccess.run();
			}
		} else {
			onLockFail.run();
		}
	}

	/**
	 * Attempts to acquire the lock. If successful, executes {@code onLockSuccess}
	 * and returns its result. Otherwise, executes {@code onLockFail} and returns its result.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param onLockSuccess computation executed if lock is acquired
	 * @param onLockFail    computation executed if lock is not acquired
	 * @param <R>           return type
	 * @param <T1>          exception type from success computation
	 * @param <T2>          exception type from failure computation
	 * @return result of either computation
	 * @throws T1                   if success computation throws an exception
	 * @throws T2                   if failure computation throws an exception
	 * @throws NullPointerException if any argument is null
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(@NonNull Lock lock, @NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2 {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock()) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess}. Otherwise, executes {@code onLockFail}.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param timeout       maximum time to wait for the lock
	 * @param onLockSuccess action executed if lock is acquired
	 * @param onLockFail    action executed if lock is not acquired
	 * @param <T1>          exception type from success action
	 * @param <T2>          exception type from failure action
	 * @throws T1                       if success action throws an exception
	 * @throws T2                       if failure action throws an exception
	 * @throws InterruptedException     if interrupted while waiting
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if timeout is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@NonNull Lock lock,
					@NonNull Duration timeout,
					@NonNull ThrowableRunnable<T1> onLockSuccess,
					@NonNull ThrowableRunnable<T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(timeout, "timeout must not be null");
		if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				onLockSuccess.run();
			}
		} else {
			onLockFail.run();
		}
	}

	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess} and returns its result. Otherwise, executes
	 * {@code onLockFail} and returns its result.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param timeout       maximum time to wait for the lock
	 * @param onLockSuccess computation executed if lock is acquired
	 * @param onLockFail    computation executed if lock is not acquired
	 * @param <R>           return type
	 * @param <T1>          exception type from success computation
	 * @param <T2>          exception type from failure computation
	 * @return result of either computation
	 * @throws T1                       if success computation throws an exception
	 * @throws T2                       if failure computation throws an exception
	 * @throws InterruptedException     if interrupted while waiting
	 * @throws NullPointerException     if any argument is null
	 * @throws IllegalArgumentException if timeout is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@NonNull Lock lock,
					@NonNull Duration timeout,
					@NonNull ThrowableSupplier<R, T1> onLockSuccess,
					@NonNull ThrowableSupplier<R, T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		Objects.requireNonNull(timeout, "timeout must not be null");
		if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess}. Otherwise, executes {@code onLockFail}.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param time          maximum time to wait for the lock
	 * @param unit          time unit of the timeout
	 * @param onLockSuccess action executed if lock is acquired
	 * @param onLockFail    action executed if lock is not acquired
	 * @param <T1>          exception type from success action
	 * @param <T2>          exception type from failure action
	 * @throws T1                       if success action throws an exception
	 * @throws T2                       if failure action throws an exception
	 * @throws InterruptedException     if interrupted while waiting for the lock
	 * @throws NullPointerException     if {@code lock}, {@code unit}, {@code onLockSuccess}, or {@code onLockFail} is null
	 * @throws IllegalArgumentException if {@code time} is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <T1 extends Throwable, T2 extends Throwable> void tryLockAndRun(
					@NonNull Lock lock,
					long time,
					@NonNull TimeUnit unit,
					@NonNull ThrowableRunnable<T1> onLockSuccess,
					@NonNull ThrowableRunnable<T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		if (time < 0) throw new IllegalArgumentException("time must be non-negative");
		Objects.requireNonNull(unit, "unit must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				onLockSuccess.run();
			}
		} else {
			onLockFail.run();
		}
	}

	/**
	 * Attempts to acquire the lock within the given timeout. If successful,
	 * executes {@code onLockSuccess} and returns its result. Otherwise, executes
	 * {@code onLockFail} and returns its result.
	 *
	 * @param lock          the lock to attempt to acquire
	 * @param time          maximum time to wait for the lock
	 * @param unit          time unit of the timeout
	 * @param onLockSuccess computation executed if lock is acquired
	 * @param onLockFail    computation executed if lock is not acquired
	 * @param <R>           return type
	 * @param <T1>          exception type from success computation
	 * @param <T2>          exception type from failure computation
	 * @return result of either computation
	 * @throws T1                       if success computation throws an exception
	 * @throws T2                       if failure computation throws an exception
	 * @throws InterruptedException     if interrupted while waiting for the lock
	 * @throws NullPointerException     if {@code lock}, {@code unit}, {@code onLockSuccess}, or {@code onLockFail} is null
	 * @throws IllegalArgumentException if {@code time} is negative
	 */
	@SuppressWarnings("try") // Suppresses warning about 'ignored' is never referenced in try statement
	public static <R, T1 extends Throwable, T2 extends Throwable> R tryLockAndGet(
					@NonNull Lock lock,
					long time,
					@NonNull TimeUnit unit,
					@NonNull ThrowableSupplier<R, T1> onLockSuccess,
					@NonNull ThrowableSupplier<R, T2> onLockFail
	) throws T1, T2, InterruptedException {

		// Ensure inputs are valid
		Objects.requireNonNull(lock, "lock must not be null");
		if (time < 0) throw new IllegalArgumentException("time must be non-negative");
		Objects.requireNonNull(unit, "unit must not be null");
		Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
		Objects.requireNonNull(onLockFail, "onLockFail must not be null");

		// Lock it
		if (lock.tryLock(time, unit)) {
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				return onLockSuccess.get();
			}
		} else {
			return onLockFail.get();
		}
	}

	/**
	 * A fluent, strategy-based lock execution API.
	 *
	 * <p>This interface represents a <b>lazy lock execution builder</b>. It allows the caller
	 * to configure how a {@link java.util.concurrent.locks.Lock} (or multiple locks) should be
	 * acquired before executing a critical section.</p>
	 *
	 * <h2>Key concept</h2>
	 * <p><b>No locking occurs while building the chain.</b> All methods on this interface
	 * are purely configuration-level until a terminal operation is invoked.</p>
	 *
	 * <p>Lock acquisition only happens when a terminal method such as {@link #run} or {@link #get}
	 * is called.</p>
	 *
	 * <h2>Execution model</h2>
	 * <ul>
	 *   <li>{@code with(lock).run(...)} → blocking lock</li>
	 *   <li>{@code with(lock).interruptibly().run(...)} → interruptible lock</li>
	 *   <li>{@code with(lock).tryAcquire().run(...)} → non-blocking try-lock</li>
	 * </ul>
	 *
	 * <p>All implementations must ensure that locks are released deterministically
	 * after execution, regardless of success or failure.</p>
	 */
	public interface WithLock {

		/**
		 * Executes the given operation under the configured lock strategy.
		 *
		 * <p>This is a <b>terminal operation</b>. Lock acquisition happens immediately
		 * before execution of the provided runnable.</p>
		 *
		 * @param runnable the operation to execute under lock
		 * @param <T>      the type of exception that may be thrown by the runnable
		 * @throws T if the runnable throws a checked or unchecked exception
		 */
		<T extends Throwable> void run(@NonNull ThrowableRunnable<T> runnable) throws T;

		/**
		 * Executes the given supplier under the configured lock strategy and returns a result.
		 *
		 * <p>This is a <b>terminal operation</b>. Lock acquisition happens immediately
		 * before execution of the supplier.</p>
		 *
		 * @param supplier the computation to execute under lock
		 * @param <R>      the return type of the supplier
		 * @param <T>      the type of exception that may be thrown by the runnable
		 * @return result of the computation
		 * @throws T if the supplier throws a checked or unchecked exception
		 */
		<R, T extends Throwable> R get(@NonNull ThrowableSupplier<R, T> supplier) throws T;

		/**
		 * Configures interruptible lock acquisition.
		 *
		 * <p>In this mode, lock acquisition may be interrupted by the current thread,
		 * resulting in {@link InterruptedException} being thrown from terminal operations.</p>
		 *
		 * <p>This is useful when lock acquisition should not block indefinitely.</p>
		 *
		 * @return interruptible lock execution strategy
		 */
		@NonNull Interruptibly interruptibly();

		/**
		 * Configures non-blocking lock acquisition.
		 *
		 * <p>This mode attempts to acquire the lock immediately and executes one of
		 * two branches depending on success or failure.</p>
		 *
		 * <p>No waiting occurs.</p>
		 *
		 * @return try-lock execution strategy
		 */
		@NonNull TryInstant tryAcquire();

		/**
		 * Configures timed lock acquisition.
		 *
		 * <p>The lock will be attempted for the given timeout duration. If the lock
		 * cannot be acquired within the time limit, the failure branch will be executed.</p>
		 *
		 * @param timeout maximum time to wait for lock acquisition
		 * @return timed try-lock execution strategy
		 */
		@NonNull TryWithTimeout tryAcquire(@NonNull Duration timeout);

		/**
		 * Configures timed lock acquisition.
		 *
		 * <p>The lock will be attempted for the given timeout duration. If the lock
		 * cannot be acquired within the time limit, the failure branch will be executed.</p>
		 *
		 * <p>This overload is equivalent to {@link #tryAcquire(Duration)} but accepts
		 * a numeric timeout and {@link TimeUnit} for interoperability with legacy APIs.</p>
		 *
		 * @param time maximum time to wait for lock acquisition
		 * @param unit the time unit of the {@code time} argument
		 * @return timed try-lock execution strategy
		 * @throws NullPointerException if {@code unit} is null
		 */
		@NonNull TryWithTimeout tryAcquire(long time, @NonNull TimeUnit unit);

		/**
		 * Interruptible execution mode for lock acquisition.
		 *
		 * <p>Lock acquisition is performed using interruptible semantics
		 * (e.g. {@link java.util.concurrent.locks.Lock#lockInterruptibly()}).</p>
		 *
		 * <p>If the thread is interrupted while waiting for the lock,
		 * operations will terminate with {@link InterruptedException}.</p>
		 */
		interface Interruptibly {

			/**
			 * Executes the given operation under the configured lock strategy.
			 *
			 * <p>This is a <b>terminal operation</b>. Lock acquisition happens immediately
			 * before execution of the provided runnable.</p>
			 *
			 * @param runnable the operation to execute under lock
			 * @param <T>      the type of exception that may be thrown by the runnable
			 * @throws T                    if the runnable throws a checked or unchecked exception
			 * @throws InterruptedException if the thread is interrupted during interruptible lock acquisition
			 */
			<T extends Throwable> void run(@NonNull ThrowableRunnable<T> runnable) throws T, InterruptedException;

			/**
			 * Executes the given supplier under the configured lock strategy and returns a result.
			 *
			 * <p>This is a <b>terminal operation</b>. Lock acquisition happens immediately
			 * before execution of the supplier.</p>
			 *
			 * @param supplier the computation to execute under lock
			 * @param <R>      return type of the operation
			 * @param <T>      the type of exception that may be thrown by the runnable
			 * @return result of the computation
			 * @throws T                    if the supplier throws a checked or unchecked exception
			 * @throws InterruptedException if the thread is interrupted during interruptible lock acquisition
			 */
			<R, T extends Throwable> R get(@NonNull ThrowableSupplier<R, T> supplier) throws T, InterruptedException;
		}

		/**
		 * Immediate try-lock execution mode.
		 *
		 * <p>The operation is split into two branches:</p>
		 * <ul>
		 *   <li>onLockSuccess → executed if lock is acquired</li>
		 *   <li>onLockFail → executed if lock is not acquired</li>
		 * </ul>
		 *
		 * <p>No blocking occurs in this mode.</p>
		 */
		interface TryInstant {

			/**
			 * Executes one of two operations depending on whether the lock can be acquired immediately.
			 *
			 * <p>This method attempts to acquire the lock without waiting. If the lock is available
			 * at the time of invocation, the {@code onLockSuccess} branch is executed. Otherwise,
			 * the {@code onLockFail} branch is executed.</p>
			 *
			 * <p>No blocking or waiting occurs in this mode.</p>
			 *
			 * @param onLockSuccess operation executed if the lock is successfully acquired
			 * @param onLockFail    operation executed if the lock cannot be acquired immediately
			 * @param <T1>          exception type thrown by the success operation
			 * @param <T2>          exception type thrown by the failure operation
			 * @throws T1 if the success operation throws an exception
			 * @throws T2 if the failure operation throws an exception
			 */
			<T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2;

			/**
			 * Executes one of two suppliers depending on whether the lock can be acquired immediately,
			 * and returns the resulting value.
			 *
			 * <p>If the lock is acquired successfully, the {@code onLockSuccess} supplier is executed.
			 * Otherwise, the {@code onLockFail} supplier is executed.</p>
			 *
			 * <p>No blocking or waiting occurs in this mode.</p>
			 *
			 * @param onLockSuccess supplier executed if the lock is successfully acquired
			 * @param onLockFail    supplier executed if the lock cannot be acquired immediately
			 * @param <R>           return type of the operation
			 * @param <T1>          exception type thrown by the success operation
			 * @param <T2>          exception type thrown by the failure operation
			 * @return the result of either the success or failure supplier
			 * @throws T1 if the success supplier throws an exception
			 * @throws T2 if the failure supplier throws an exception
			 */
			<R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2;
		}

		/**
		 * Timed try-lock execution mode.
		 *
		 * <p>Allows execution of separate success and failure branches depending on
		 * whether the lock was acquired within the specified timeout.</p>
		 *
		 * <p>If the thread is interrupted while waiting, {@link InterruptedException}
		 * is thrown.</p>
		 */
		interface TryWithTimeout {

			/**
			 * Executes one of two operations depending on whether the lock can be acquired immediately.
			 *
			 * <p>This method attempts to acquire the lock without waiting. If the lock is available
			 * at the time of invocation, the {@code onLockSuccess} branch is executed. Otherwise,
			 * the {@code onLockFail} branch is executed.</p>
			 *
			 * <p>No blocking or waiting occurs in this mode.</p>
			 *
			 * @param onLockSuccess operation executed if the lock is successfully acquired
			 * @param onLockFail    operation executed if the lock cannot be acquired immediately
			 * @param <T1>          exception type thrown by the success operation
			 * @param <T2>          exception type thrown by the failure operation
			 * @throws T1                   if the success operation throws an exception
			 * @throws T2                   if the failure operation throws an exception
			 * @throws InterruptedException if the thread is interrupted while attempting to acquire locks
			 */
			<T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2, InterruptedException;

			/**
			 * Executes one of two suppliers depending on whether the lock can be acquired immediately,
			 * and returns the resulting value.
			 *
			 * <p>If the lock is acquired successfully, the {@code onLockSuccess} supplier is executed.
			 * Otherwise, the {@code onLockFail} supplier is executed.</p>
			 *
			 * <p>No blocking or waiting occurs in this mode.</p>
			 *
			 * @param onLockSuccess supplier executed if the lock is successfully acquired
			 * @param onLockFail    supplier executed if the lock cannot be acquired immediately
			 * @param <R>           return type of the operation
			 * @param <T1>          exception type thrown by the success operation
			 * @param <T2>          exception type thrown by the failure operation
			 * @return the result of either the success or failure supplier
			 * @throws T1                   if the success supplier throws an exception
			 * @throws T2                   if the failure supplier throws an exception
			 * @throws InterruptedException if the thread is interrupted while attempting to acquire locks
			 */
			<R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2, InterruptedException;
		}
	}

	/* Mutable Boolean */
	private static class MutableBoolean {
		private boolean value;

		private MutableBoolean(boolean value) {
			this.value = value;
		}
	}

	/* Mutable Reference */
	private static class MutableReference<Type> {
		private Type value;

		private MutableReference(Type initialValue) {
			this.value = initialValue;
		}
	}

	/**
	 * Single-lock implementation of {@link WithLock}.
	 *
	 * <p>This class provides a concrete implementation of the {@link WithLock} fluent API
	 * for a single {@link java.util.concurrent.locks.Lock} instance.</p>
	 *
	 * <p>It acts as a thin wrapper around a single lock and delegates all execution strategy
	 * selection (blocking, interruptible, try-lock) to the fluent builder methods defined
	 * in {@link WithLock}.</p>
	 *
	 * <p>This class is an internal implementation detail of {@link AutoLock} and is not
	 * intended for direct use by library consumers.</p>
	 */
	private final static class SingleLock implements WithLock {
		@NonNull
		private final Lock lock;

		private SingleLock(@NonNull Lock lock) {
			this.lock = lock;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("try")
		public <T extends Throwable> void run(@NonNull ThrowableRunnable<T> runnable) throws T {
			Objects.requireNonNull(runnable, "runnable must not be null");
			lock.lock();
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				runnable.run();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("try")
		public <R, T extends Throwable> R get(@NonNull ThrowableSupplier<R, T> supplier) throws T {
			Objects.requireNonNull(supplier, "supplier must not be null");
			lock.lock();
			try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
				return supplier.get();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@NonNull
		public Interruptibly interruptibly() {
			return new Interruptibly();
		}

		/**
		 * {@inheritDoc}
		 */
		@NonNull
		public TryInstant tryAcquire() {
			return new TryInstant();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public WithLock.@NonNull TryWithTimeout tryAcquire(@NonNull Duration timeout) {
			Objects.requireNonNull(timeout, "timeout must not be null");
			if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
			return tryAcquire(timeout.toNanos(), TimeUnit.NANOSECONDS);
		}

		/**
		 * {@inheritDoc}
		 */
		@NonNull
		public TryWithTimeout tryAcquire(long time, @NonNull TimeUnit unit) {
			if (time < 0) throw new IllegalArgumentException("time must be non-negative");
			Objects.requireNonNull(unit, "unit must not be null");
			return new TryWithTimeout(time, unit);
		}

		/**
		 * {@inheritDoc}
		 */
		private final class Interruptibly implements WithLock.Interruptibly {

			private Interruptibly() {
			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <T extends Throwable> void run(@NonNull ThrowableRunnable<T> runnable) throws T, InterruptedException {
				Objects.requireNonNull(runnable, "runnable must not be null");
				lock.lockInterruptibly();
				try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
					runnable.run();
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <R, T extends Throwable> R get(@NonNull ThrowableSupplier<R, T> supplier) throws T, InterruptedException {
				Objects.requireNonNull(supplier, "supplier must not be null");
				lock.lockInterruptibly();
				try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
					return supplier.get();
				}
			}
		}

		/**
		 * Immediate try-lock execution mode.
		 *
		 * <p>The operation is split into two branches:</p>
		 * <ul>
		 *   <li>onLockSuccess → executed if lock is acquired</li>
		 *   <li>onLockFail → executed if lock is not acquired</li>
		 * </ul>
		 *
		 * <p>No blocking occurs in this mode.</p>
		 */
		private final class TryInstant implements WithLock.TryInstant {

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2 {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				if (lock.tryLock()) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						onLockSuccess.run();
					}
				} else {
					onLockFail.run();
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2 {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				if (lock.tryLock()) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						return onLockSuccess.get();
					}
				} else {
					return onLockFail.get();
				}
			}
		}

		/**
		 * Timed try-lock execution mode.
		 *
		 * <p>Allows execution of separate success and failure branches depending on
		 * whether the lock was acquired within the specified timeout.</p>
		 *
		 * <p>If the thread is interrupted while waiting, {@link InterruptedException}
		 * is thrown.</p>
		 */
		private final class TryWithTimeout implements WithLock.TryWithTimeout {
			private final long timeout;
			@NonNull
			private final TimeUnit timeUnit;

			private TryWithTimeout(long timeout, @NonNull TimeUnit timeUnit) {
				this.timeout = timeout;
				this.timeUnit = timeUnit;
			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2, InterruptedException {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				if (lock.tryLock(timeout, timeUnit)) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						onLockSuccess.run();
					}
				} else {
					onLockFail.run();
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2, InterruptedException {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				if (lock.tryLock(timeout, timeUnit)) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						return onLockSuccess.get();
					}
				} else {
					return onLockFail.get();
				}
			}
		}
	}

	/**
	 * Multi-lock implementation of {@link WithLock}.
	 *
	 * <p>This class provides a concrete implementation of the {@link WithLock} fluent API
	 * for coordinating multiple {@link java.util.concurrent.locks.Lock} instances.</p>
	 *
	 * <p>Locks are acquired in the order provided to {@code AutoLock.with(...)} and released
	 * in reverse order upon completion of the terminal operation.</p>
	 *
	 * <h2>Deadlock responsibility</h2>
	 * <p>It is the caller's responsibility to ensure a consistent global lock ordering across
	 * threads. This class does not perform deadlock detection or reordering.</p>
	 *
	 * <h2>Additional capabilities</h2>
	 * <p>Unlike {@link WithLock}, this implementation exposes additional diagnostic-aware
	 * acquisition methods that provide visibility into which lock caused a failure during
	 * {@code tryLock}-based operations.</p>
	 *
	 * <p>These extensions are only available for multi-lock scenarios and are not part of
	 * the base {@link WithLock} contract.</p>
	 *
	 * <p>This class is intended for direct use via {@code AutoLock.with(Lock...)} and is
	 * not designed for subclassing.</p>
	 */
	public final static class MultipleLocks implements WithLock {
		@NonNull
		private final Lock[] fullLocks;

		private MultipleLocks(@NonNull Lock[] fullLocks) {
			this.fullLocks = fullLocks;
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("try")
		public <T extends Throwable> void run(@NonNull ThrowableRunnable<T> runnable) throws T {
			Objects.requireNonNull(runnable, "runnable must not be null");
			try (LockedAutoLock ignored = multipleLock(fullLocks)) {
				runnable.run();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("try")
		public <R, T extends Throwable> R get(@NonNull ThrowableSupplier<R, T> supplier) throws T {
			Objects.requireNonNull(supplier, "supplier must not be null");
			try (LockedAutoLock ignored = multipleLock(fullLocks)) {
				return supplier.get();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public WithLock.@NonNull Interruptibly interruptibly() {
			return new Interruptibly();
		}

		/**
		 * {@inheritDoc}
		 */
		@NonNull
		public TryInstant tryAcquire() {
			return new TryInstant();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@NonNull
		public TryWithTimeout tryAcquire(long time, @NonNull TimeUnit unit) {
			if (time < 0) throw new IllegalArgumentException("time must be non-negative");
			Objects.requireNonNull(unit, "unit must not be null");
			return new TryWithTimeout(time, unit);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@NonNull
		public TryWithTimeout tryAcquire(@NonNull Duration timeout) {
			Objects.requireNonNull(timeout, "timeout must not be null");
			if (timeout.isNegative()) throw new IllegalArgumentException("timeout must be non-negative");
			return tryAcquire(timeout.toNanos(), TimeUnit.NANOSECONDS);
		}

		/**
		 * Context information describing a failed attempt to acquire a lock in a multi-lock
		 * {@code tryLock} operation.
		 *
		 * <p>This object is provided to failure callbacks when a lock acquisition attempt fails
		 * before all locks could be successfully acquired.</p>
		 *
		 * <p>It allows the caller to inspect which lock in the acquisition sequence caused the
		 * failure, and which specific lock instance was not acquired.</p>
		 *
		 * <p>This context is read-only and represents a snapshot of the failure state at the
		 * moment the lock acquisition failed.</p>
		 */
		public static final class TryLockFailContext {

			/**
			 * The index of the lock in the acquisition sequence that failed.
			 *
			 * <p>Lock acquisition is performed in the order provided to
			 * {@code AutoLock.with(Lock...)}. This index corresponds to that order.</p>
			 */
			private final int lockIndex;

			/**
			 * The lock instance that failed to be acquired.
			 */
			@NonNull
			private final Lock lock;

			/**
			 * Creates a new failure context describing a failed lock acquisition.
			 *
			 * @param lockIndex index of the lock in the acquisition sequence that failed
			 * @param lock      the lock instance that could not be acquired
			 */
			private TryLockFailContext(int lockIndex, @NonNull Lock lock) {
				this.lockIndex = lockIndex;
				this.lock = lock;
			}

			/**
			 * Returns the index of the lock in the acquisition sequence that failed.
			 *
			 * @return the zero-based index of the failed lock
			 */
			public int getSequenceIndex() {
				return lockIndex;
			}

			/**
			 * Returns the lock instance that failed to be acquired.
			 *
			 * @return the failed {@link Lock}
			 */
			@NonNull
			public Lock getFailedLock() {
				return lock;
			}
		}

		/**
		 * Interruptible execution mode for lock acquisition.
		 *
		 * <p>Lock acquisition is performed using interruptible semantics
		 * (e.g. {@link java.util.concurrent.locks.Lock#lockInterruptibly()}).</p>
		 *
		 * <p>If the thread is interrupted while waiting for the lock,
		 * operations will terminate with {@link InterruptedException}.</p>
		 */
		public final class Interruptibly implements WithLock.Interruptibly {

			private Interruptibly() {

			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <T extends Throwable> void run(@NonNull ThrowableRunnable<T> runnable) throws T, InterruptedException {
				Objects.requireNonNull(runnable, "runnable must not be null");
				try (LockedAutoLock ignored = multipleLockInterruptibly(fullLocks)) {
					runnable.run();
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@SuppressWarnings("try")
			public <R, T extends Throwable> R get(@NonNull ThrowableSupplier<R, T> supplier) throws T, InterruptedException {
				Objects.requireNonNull(supplier, "supplier must not be null");
				try (LockedAutoLock ignored = multipleLockInterruptibly(fullLocks)) {
					return supplier.get();
				}
			}
		}

		/**
		 * Immediate try-lock execution mode.
		 *
		 * <p>The operation is split into two branches:</p>
		 * <ul>
		 *   <li>onLockSuccess → executed if lock is acquired</li>
		 *   <li>onLockFail → executed if lock is not acquired</li>
		 * </ul>
		 *
		 * <p>No blocking occurs in this mode.</p>
		 */
		public final class TryInstant implements WithLock.TryInstant {

			private TryInstant() {
			}

			/**
			 * {@inheritDoc}
			 */
			public <T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2 {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				innerRun(0, onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					onLockFail.run();
				}
			}

			/**
			 * Executes one of two operations depending on whether the lock can be acquired.
			 *
			 * <p>If the lock is successfully acquired, the {@code onLockSuccess} operation is executed
			 * under the lock. If the lock cannot be acquired, the {@code onLockFail} operation is executed
			 * instead with diagnostic information about the failure.</p>
			 *
			 * <p>The failure callback receives a {@link TryLockFailContext}, which contains information
			 * about the lock acquisition attempt that failed (such as which lock in the sequence failed).</p>
			 *
			 * <p>No locks are held when the failure callback is executed.</p>
			 *
			 * @param onLockSuccess operation executed if all locks are successfully acquired
			 * @param onLockFail    operation executed if lock acquisition fails, receiving failure context
			 * @param <T1>          exception type thrown by the success operation
			 * @param <T2>          exception type thrown by the failure operation
			 * @throws T1 if the success operation throws an exception
			 * @throws T2 if the failure operation throws an exception
			 */
			public <T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableConsumer<TryLockFailContext, T2> onLockFail) throws T1, T2 {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				innerRun(0, onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					onLockFail.accept(tryLockFailContext);
				}
			}

			@SuppressWarnings("try")
			private <T1 extends Throwable> void innerRun(int lockIndex, @NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull MutableReference<TryLockFailContext> mutableRef) throws T1 {
				Lock lock = fullLocks[lockIndex++];
				if (lock.tryLock()) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						if (lockIndex == fullLocks.length) {
							onLockSuccess.run();
						} else {
							innerRun(lockIndex, onLockSuccess, mutableRef);
						}
					}
				} else {
					mutableRef.value = new TryLockFailContext(lockIndex - 1, lock);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			public <R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2 {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				R original = innerGet(0, onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					return onLockFail.get();
				} else {
					return original;
				}
			}

			/**
			 * Executes one of two operations depending on whether the lock can be acquired,
			 * and returns a computed result.
			 *
			 * <p>If the lock is successfully acquired, the {@code onLockSuccess} supplier is executed
			 * under the lock. If lock acquisition fails, the {@code onLockFail} function is executed
			 * with diagnostic information about the failure.</p>
			 *
			 * <p>The failure callback receives a {@link TryLockFailContext}, which provides details
			 * about the lock acquisition attempt that failed (such as which lock in the sequence failed).</p>
			 *
			 * <p>No locks are held when the failure callback is executed.</p>
			 *
			 * @param onLockSuccess supplier executed if all locks are successfully acquired
			 * @param onLockFail    function executed if lock acquisition fails, receiving failure context
			 *                      and producing a fallback result
			 * @param <R>           return type of the operation
			 * @param <T1>          exception type thrown by the success operation
			 * @param <T2>          exception type thrown by the failure operation
			 * @return result from either the success or failure branch
			 * @throws T1 if the success supplier throws an exception
			 * @throws T2 if the failure function throws an exception
			 */
			public <R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableFunction<TryLockFailContext, R, T2> onLockFail) throws T1, T2 {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				R original = innerGet(0, onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					return onLockFail.apply(tryLockFailContext);
				} else {
					return original;
				}
			}

			@SuppressWarnings("try")
			private <R, T1 extends Throwable> R innerGet(int lockIndex, @NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull MutableReference<TryLockFailContext> mutableRef) throws T1 {
				Lock lock = fullLocks[lockIndex++];
				if (lock.tryLock()) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						if (lockIndex == fullLocks.length) {
							return onLockSuccess.get();
						} else {
							return innerGet(lockIndex, onLockSuccess, mutableRef);
						}
					}
				} else {
					mutableRef.value = new TryLockFailContext(lockIndex - 1, lock);
					return null;
				}
			}
		}

		/**
		 * Timed try-lock execution mode.
		 *
		 * <p>Allows execution of separate success and failure branches depending on
		 * whether the lock was acquired within the specified timeout.</p>
		 *
		 * <p>If the thread is interrupted while waiting, {@link InterruptedException}
		 * is thrown.</p>
		 */
		public final class TryWithTimeout implements WithLock.TryWithTimeout {
			private final long time;
			@NonNull
			private final TimeUnit unit;

			// mutable field used to stub for unit testing - generally should not be tweaked in production
			// Default to nanoTime as the actual behavior
			LongSupplier stubGetNanos = System::nanoTime;

			private TryWithTimeout(long time, @NonNull TimeUnit unit) {
				this.time = time;
				this.unit = unit;
			}

			/**
			 * {@inheritDoc}
			 */
			public <T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableRunnable<T2> onLockFail) throws T1, T2, InterruptedException {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				innerRun(0, unit.toNanos(time), stubGetNanos.getAsLong(), onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					onLockFail.run();
				}
			}

			/**
			 * Executes one of two operations depending on whether all locks can be acquired immediately.
			 *
			 * <p>This method attempts to acquire all managed locks using a non-blocking {@code tryLock} strategy.
			 * If all locks are successfully acquired, the {@code onLockSuccess} branch is executed.</p>
			 *
			 * <p>If any lock cannot be acquired immediately, the attempt is aborted and the {@code onLockFail} branch is
			 * executed instead. No waiting or retrying occurs.</p>
			 *
			 * <p><b>Failure context:</b><br>
			 * When the failure branch is executed, a {@link TryLockFailContext} is provided to the {@code onLockFail}
			 * consumer. This context contains diagnostic information about the failed acquisition attempt, including:</p>
			 * <ul>
			 *   <li>Which specific {@link Lock} caused the failure</li>
			 *   <li>The time at which the try-lock attempt failed</li>
			 * </ul>
			 *
			 * <p>This allows callers to implement detailed logging, metrics, or fallback behavior based on the exact point of
			 * contention.</p>
			 *
			 * @param onLockSuccess operation executed if all locks are successfully acquired
			 * @param onLockFail    operation executed if lock acquisition fails; receives contextual failure information
			 * @param <T1>          exception type thrown by the success operation
			 * @param <T2>          exception type thrown by the failure operation
			 * @throws T1                   if the success operation throws an exception
			 * @throws T2                   if the failure operation throws an exception
			 * @throws InterruptedException if the thread is interrupted while attempting to acquire locks
			 */
			public <T1 extends Throwable, T2 extends Throwable> void run(@NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull ThrowableConsumer<TryLockFailContext, T2> onLockFail) throws T1, T2, InterruptedException {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				innerRun(0, unit.toNanos(time), stubGetNanos.getAsLong(), onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					onLockFail.accept(tryLockFailContext);
				}
			}

			@SuppressWarnings("try")
			private <T1 extends Throwable> void innerRun(int lockIndex, final long timeoutNanos, final long epochInNanos, @NonNull ThrowableRunnable<T1> onLockSuccess, @NonNull MutableReference<TryLockFailContext> mutableRef) throws T1, InterruptedException {
				Lock lock = fullLocks[lockIndex++];
				long budget = timeoutNanos - (stubGetNanos.getAsLong() - epochInNanos);
				if (budget > -1 && lock.tryLock(budget, TimeUnit.NANOSECONDS)) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						if (lockIndex == fullLocks.length) {
							onLockSuccess.run();
						} else {
							innerRun(lockIndex, timeoutNanos, epochInNanos, onLockSuccess, mutableRef);
						}
					}
				} else {
					mutableRef.value = new TryLockFailContext(lockIndex - 1, lock);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			public <R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableSupplier<R, T2> onLockFail) throws T1, T2, InterruptedException {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				R original = innerGet(0, unit.toNanos(time), stubGetNanos.getAsLong(), onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					return onLockFail.get();
				} else {
					return original;
				}
			}

			/**
			 * Attempts to acquire all managed locks and returns a value from either a success or failure path.
			 *
			 * <p>This method uses a non-blocking {@code tryLock} strategy. If all locks are successfully  acquired, the
			 * {@code onLockSuccess} supplier is executed and its result is returned.</p>
			 *
			 * <p>If any lock cannot be acquired immediately, the attempt is aborted and the {@code onLockFail} function is
			 * executed instead. No waiting or retrying occurs.</p>
			 *
			 * <p><b>Failure context:</b><br>
			 * When the failure branch is executed, a {@link TryLockFailContext} is provided to the {@code onLockFail}
			 * function. This context can be used to determine which lock caused the failure (e.g., by index in the
			 * acquisition sequence), enabling targeted diagnostics, logging, or fallback behavior.</p>
			 *
			 * @param onLockSuccess supplier executed if all locks are successfully acquired
			 * @param onLockFail    function executed if lock acquisition fails; receives failure context and returns a fallback value
			 * @param <R>           return type of the operation
			 * @param <T1>          exception type thrown by the success supplier
			 * @param <T2>          exception type thrown by the failure function
			 * @return the result of either the success or failure supplier
			 * @throws T1                   if the success supplier throws an exception
			 * @throws T2                   if the failure function throws an exception
			 * @throws InterruptedException if the thread is interrupted while attempting to acquire locks
			 */
			public <R, T1 extends Throwable, T2 extends Throwable> R get(@NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull ThrowableFunction<TryLockFailContext, R, T2> onLockFail) throws T1, T2, InterruptedException {
				Objects.requireNonNull(onLockSuccess, "onLockSuccess must not be null");
				Objects.requireNonNull(onLockFail, "onLockFail must not be null");
				MutableReference<TryLockFailContext> mutableRef = new MutableReference<>(null);
				R original = innerGet(0, unit.toNanos(time), stubGetNanos.getAsLong(), onLockSuccess, mutableRef);
				TryLockFailContext tryLockFailContext = mutableRef.value;
				if (tryLockFailContext != null) {
					return onLockFail.apply(tryLockFailContext);
				} else {
					return original;
				}
			}

			@SuppressWarnings("try")
			private <R, T1 extends Throwable> R innerGet(int lockIndex, final long timeoutNanos, final long epochInNanos, @NonNull ThrowableSupplier<R, T1> onLockSuccess, @NonNull MutableReference<TryLockFailContext> mutableRef) throws T1, InterruptedException {
				Lock lock = fullLocks[lockIndex++];
				long budget = timeoutNanos - (stubGetNanos.getAsLong() - epochInNanos);
				if (budget > -1 && lock.tryLock(budget, TimeUnit.NANOSECONDS)) {
					try (LockedAutoLock ignored = new LockedAutoLock(lock::unlock)) {
						if (lockIndex == fullLocks.length) {
							return onLockSuccess.get();
						} else {
							return innerGet(lockIndex, timeoutNanos, epochInNanos, onLockSuccess, mutableRef);
						}
					}
				} else {
					mutableRef.value = new TryLockFailContext(lockIndex - 1, lock);
					return null;
				}
			}
		}
	}
}
