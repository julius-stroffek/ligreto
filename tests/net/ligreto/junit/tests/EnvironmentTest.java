/**
 * 
 */
package net.ligreto.junit.tests;

import org.junit.Test;

/**
 * This is the test whether the testing environment has the right setup.
 * 
 * @author Julius Stroffek
 *
 */
public class EnvironmentTest {

	/**
	 * Test that the java assertions are enabled.
	 */
	@Test
	public void testAssertions() {
		boolean thrown;
		try {
			assert false;
			thrown = false;
		} catch (AssertionError e) {
			// This is fine, we need assertions for testing purposes.
			thrown = true;
		}
		if (!thrown) {
			throw new AssertionError("Assertions are disabled!");
		}
	}
}
