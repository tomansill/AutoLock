/**
 * Provides utilities for working with {@link java.util.concurrent.locks.Lock}
 * using scope-based (try-with-resources) semantics.
 *
 * <h2>Overview</h2>
 *
 * <p>This package introduces a small abstraction layer over {@link java.util.concurrent.locks.Lock}
 * that enables safer and more structured locking patterns inspired by RAII (resource acquisition
 * is initialization).</p>
 *
 * <p>The primary goal is to ensure that locks are always released deterministically,
 * even in the presence of exceptions, without requiring explicit unlock calls.</p>
 *
 * <h2>Core Concepts</h2>
 *
 * <ul>
 *   <li><b>{@link com.ansill.autolock.AutoLock}</b> – Entry point utilities for acquiring locks and executing
 *   code under lock protection.</li>
 *
 *   <li><b>{@link com.ansill.autolock.LockedAutoLock}</b> – A scope-bound lock handle that releases the underlying
 *   lock when closed (typically via try-with-resources).</li>
 *
 *   <li><b>{@link com.ansill.autolock.ThrowableRunnable}</b> – A {@code Runnable}-like functional interface that
 *   can throw checked exceptions.</li>
 *
 *   <li><b>{@link com.ansill.autolock.ThrowableSupplier}</b> – A {@code Supplier}-like functional interface that
 *   can throw checked exceptions.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * Lock lock = new ReentrantLock();
 *
 * try (LockedAutoLock ignored = AutoLock.lock(lock)) {
 *     // critical section
 * }
 * }</pre>
 *
 * <h2>Design Notes</h2>
 *
 * <p>This API is intentionally minimal and focused on deterministic lock management.
 * It does not attempt to extend or replace {@link java.util.concurrent.locks.Lock},
 * but instead provides a convenience layer for safer usage patterns.</p>
 *
 * <p>All lock acquisition methods block unless explicitly stated otherwise,
 * and all resources are guaranteed to be released when the scope ends.</p>
 */
package com.ansill.autolock;