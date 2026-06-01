package test;

import org.jspecify.annotations.NonNull;

public class TestUtility {
	@NonNull
	public static StubbedLock getLock() {
		return new StubbedLock();
	}
}
