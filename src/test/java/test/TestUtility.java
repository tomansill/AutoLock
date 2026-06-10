package test;

import com.ansill.autolock.ThrowableConsumer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DynamicTest;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TestUtility {

	public static final String SEED = "holy_moly_seed";

	private static final String ALPHABET_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final String NUMERICS = "0123456789";

	private static final String ALPHABET_UPPERCASE_LOWERCASE_NUMBERS = ALPHABET_UPPERCASE + ALPHABET_UPPERCASE.toLowerCase() + NUMERICS;

	public static String getSeed(int offset) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement callingSTE = stackTraceElements[stackTraceElements.length - 1 - offset];
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
}
