package net.ligreto.util;

import net.ligreto.exceptions.AssertionException;

public class Assert {
	public static void assertTrue(boolean expression) {
		assertTrue(expression, "");
	}

	public static void assertTrue(boolean expression, String message) {
		if (!expression)
			throw new AssertionException(message);
	}

	public static void assertFalse(boolean expression) {
		assertFalse(expression, "");
	}

	public static void assertFalse(boolean expression, String message) {
		if (expression)
			throw new AssertionException(message);
	}
}
