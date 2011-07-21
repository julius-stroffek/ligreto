package net.ligreto.util;

import net.ligreto.exceptions.AssertionException;

public class Assert {
	public static void assertTrue(boolean expression) {
		if (!expression)
			throw new AssertionException();
	}

	public static void assertFalse(boolean expression) {
		if (expression)
			throw new AssertionException();
	}
}
