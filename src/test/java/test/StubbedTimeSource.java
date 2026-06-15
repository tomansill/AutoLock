package test;

import org.junit.jupiter.api.Assertions;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.LongSupplier;

public class StubbedTimeSource implements AutoCloseable, LongSupplier {

	private final Queue<Long> times = new ConcurrentLinkedDeque<>();

	@Override
	public void close() {
		if (times.isEmpty()) return;
		Assertions.fail("uninvoked calls");
	}

	void insertTime(long time) {
		times.add(time);
	}

	@Override
	public long getAsLong() {
		Long value = times.poll();
		if (value == null) Assertions.fail("unstubbed");
		return value;
	}
}
