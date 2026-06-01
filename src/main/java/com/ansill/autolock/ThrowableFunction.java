package com.ansill.autolock;

/**
 * Represents a function that accepts one input argument and produces a result,
 * potentially throwing a checked exception.
 *
 * <p>This is a variation of {@link java.util.function.Function} whose
 * {@link #apply(Object)} method is allowed to throw a checked exception.
 * It is useful in scenarios where lambda expressions need to propagate
 * checked exceptions without wrapping them.</p>
 *
 * <p>This is a functional interface whose functional method is
 * {@link #apply(Object)}.</p>
 *
 * @param <Input>  the type of the input to the function
 * @param <Output> the type of the result of the function
 * @param <T>      the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowableFunction<Input, Output, T extends Throwable> {
	/**
	 * Applies this function to the given input.
	 *
	 * @param input the input argument
	 * @return the function result
	 * @throws T if an error occurs during function execution
	 */
	Output apply(Input input) throws T;
}
