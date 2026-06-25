package com.ansill.autolock;

import org.jspecify.annotations.NonNull;

import java.util.function.LongSupplier;

public class Bypass {
	public static void injectTimeSupplier(AutoLock.MultipleLocks.@NonNull TryWithTimeout ctx, @NonNull LongSupplier supplier) {
		ctx.stubGetNanos = supplier;
	}
}
