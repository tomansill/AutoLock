package com.ansill.autolock;

/**
 * Represents an operation that accepts a single input argument and returns no result,
 * potentially throwing a checked exception.
 *
 * <p>This is a variation of {@link java.util.function.Consumer} whose
 * {@link #accept(Object)} method is allowed to throw a checked exception.
 * It is useful in scenarios where lambda expressions need to perform side effects
 * and propagate checked exceptions without wrapping them.</p>
 *
 * <p>This is a functional interface whose functional method is
 * {@link #accept(Object)}.</p>
 *
 * @param <Input> the type of the input to the operation
 * @param <T>     the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowableConsumer<Input, T extends Throwable> {
	/**
	 * Performs this operation on the given input.
	 *
	 * @param input the input argument
	 * @throws T if an error occurs during execution
	 */
	void accept(Input input) throws T;
}
