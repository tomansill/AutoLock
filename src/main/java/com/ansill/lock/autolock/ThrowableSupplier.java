package com.ansill.lock.autolock;

/**
 * A functional interface similar to {@link java.util.function.Supplier},
 * but allowing a checked exception to be thrown.
 *
 * <p>This interface is intended for lambda expressions or method references
 * where a value must be computed but the computation may fail with a checked exception.</p>
 *
 * @param <R> the type of value supplied
 * @param <T> the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowableSupplier<R, T extends Throwable> {

	/**
	 * Computes a value.
	 *
	 * @return the computed value
	 * @throws T if the computation fails with a checked exception
	 */
	R get() throws T;

}
