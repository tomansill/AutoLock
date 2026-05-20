package com.ansill.lock.autolock;

/**
 * A functional interface similar to {@link Runnable}, but allowing a checked exception to be thrown.
 *
 * <p>This interface is intended for use in lambda expressions or method references where
 * the executed block may throw a checked exception.</p>
 *
 * @param <T> the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowableRunnable<T extends Throwable> {

	/**
	 * Executes the action.
	 *
	 * @throws T if the execution fails with a checked exception
	 */
	void run() throws T;

}
