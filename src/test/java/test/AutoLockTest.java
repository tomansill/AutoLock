package test;

import com.ansill.autolock.AutoLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutoLock Test")
class AutoLockTest {

	static final int MAX_REPETITIONS = 5;

	@Test
	void testUtilityClassInstantiation() {
		for (Constructor<?> ctor : AutoLock.class.getDeclaredConstructors()) {
			if (ctor.isSynthetic()) continue;

			assertEquals(0, ctor.getParameterCount(), "Constructor should have no parameters: " + ctor);

			ctor.setAccessible(true);
			InvocationTargetException ite = assertThrows(InvocationTargetException.class, ctor::newInstance,
							"Constructor should throw when invoked: " + ctor);
			Throwable cause = ite.getCause();
			assertNotNull(cause, "InvocationTargetException must have a cause");
			assertInstanceOf(UnsupportedOperationException.class, cause, () -> "Expected UnsupportedOperationException but was: " + cause.getClass());
		}
	}

}
