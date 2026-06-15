package test;

import com.ansill.autolock.ThrowableConsumer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DynamicTest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestUtility {

	public static final String SEED = "holy_moly_seed";

	private static final String ALPHABET_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final String NUMERICS = "0123456789";

	private static final String ALPHABET_UPPERCASE_LOWERCASE_NUMBERS = ALPHABET_UPPERCASE + ALPHABET_UPPERCASE.toLowerCase() + NUMERICS;

	public static String getSeed(int offset) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement callingSTE = stackTraceElements[offset];
		return SEED + callingSTE.getClassName() + callingSTE.getMethodName();
	}

	@NonNull
	public static Iterable<DynamicTest> generateTests(int repetitions, @NonNull ThrowableConsumer<Random, ?> consumer) {
		String seed = getSeed(1);
		Random testGroupLocalRandom = new Random(seed.hashCode());
		List<DynamicTest> container = new LinkedList<>();
		for (int repetition = 0; repetition < repetitions; repetition++) {
			Random testLocalRandom = new Random(testGroupLocalRandom.nextLong());
			container.add(DynamicTest.dynamicTest(String.format("%s of %s repetitions", repetition + 1, repetitions), () -> consumer.accept(testLocalRandom)));
		}
		return container;
	}

	public static @NonNull String generateAlphanumericString(@NonNull Random random, int minimum, int maximum) {
		int range = maximum - minimum;
		int amount = random.nextInt(range);
		int actual = amount + minimum;
		return generateAlphanumericString(random, actual);
	}

	public static @NonNull String generateAlphanumericString(@NonNull Random random, int length) {
		char[] charArray = new char[length];
		for (int i = 0; i < length; i++) {
			charArray[i] = ALPHABET_UPPERCASE_LOWERCASE_NUMBERS.charAt(random.nextInt(ALPHABET_UPPERCASE_LOWERCASE_NUMBERS.length()));
		}
		return new String(charArray);
	}

	@NonNull
	public static Duration generateDuration(@NonNull Random random, @NonNull Duration minimum, @NonNull Duration maximum) {
		return minimum.plus(Duration.ofMillis(random.nextInt((int) maximum.minus(minimum).toMillis())));
	}

	@NonNull
	static Map<String, Supplier<Object>> getRandomObjects(@NonNull Random random1) {
		Map<String, Supplier<Object>> returnObj = new HashMap<>();
		{
			long seed = random1.nextLong();
			returnObj.put("string", () -> generateAlphanumericString(new Random(seed), 3, 32));
		}
		{
			long seed = random1.nextLong();
			returnObj.put("list of random strings", () -> {
				Random random = new Random(seed);
				return IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toSet());
			});
		}
		{
			long seed = random1.nextLong();
			returnObj.put("map of random strings", () -> {
				Random random = new Random(seed);
				return IntStream.range(0, random.nextInt(30) + 2).mapToObj(i -> generateAlphanumericString(random, 5, 15)).collect(Collectors.toMap(i -> i, i -> IntStream.range(0, random.nextInt(15) + 2).mapToObj(j -> generateAlphanumericString(random, 3, 20)).collect(Collectors.toSet())));
			});
		}
		{
			long seed = random1.nextLong();
			returnObj.put("big decimal", () -> new BigDecimal(new Random(seed).nextDouble()));
		}
		{
			long seed = random1.nextLong();
			returnObj.put("int", () -> new Random(seed).nextInt());
		}
		{
			returnObj.put("null", () -> null);
		}
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends RuntimeException>> getRandomRuntimeExceptions(@NonNull Random random) {
		Map<String, Supplier<? extends RuntimeException>> returnObj = new HashMap<>();
		{
			long seed = random.nextLong();
			returnObj.put("RuntimeException", () -> new RuntimeException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("SecurityException", () -> new SecurityException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("CancellationException", () -> new CancellationException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IllegalStateException", () -> new IllegalStateException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IllegalArgumentException", () -> new IllegalArgumentException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("UnsupportedOperationException", () -> new UnsupportedOperationException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IllegalMonitorStateException", () -> new IllegalMonitorStateException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Error>> getRandomErrors(@NonNull Random random) {
		Map<String, Supplier<? extends Error>> returnObj = new HashMap<>();
		{
			long seed = random.nextLong();
			returnObj.put("AssertionError", () -> new AssertionError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("Error", () -> new Error("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("OutOfMemoryError", () -> new OutOfMemoryError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("StackOverflowError", () -> new StackOverflowError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("LinkageError", () -> new LinkageError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("ExceptionInInitializerError", () -> new ExceptionInInitializerError("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Exception>> getRandomCheckedExceptions(@NonNull Random random) {
		Map<String, Supplier<? extends Exception>> returnObj = new HashMap<>();
		{
			long seed = random.nextLong();
			returnObj.put("Exception", () -> new Exception("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("InterruptedException", () -> new InterruptedException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		{
			long seed = random.nextLong();
			returnObj.put("IOException", () -> new IOException("fake exception" + generateAlphanumericString(new Random(seed), 3, 32)));
		}
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Throwable>> getRandomUncheckeds(@NonNull Random random) {
		Map<String, Supplier<? extends Throwable>> returnObj = new HashMap<>(getRandomRuntimeExceptions(random));
		returnObj.putAll(getRandomErrors(random));
		return returnObj;
	}

	@NonNull
	static Map<String, Supplier<? extends Throwable>> getRandomThrowables(@NonNull Random random) {
		Map<String, Supplier<? extends Throwable>> returnObj = new HashMap<>(getRandomCheckedExceptions(random));
		returnObj.putAll(getRandomRuntimeExceptions(random));
		returnObj.putAll(getRandomErrors(random));
		return returnObj;
	}
}
