/**
 * 
 */
package net.ligreto.util;

/**
 * This class encapsulates only enable assertion code. This needs to be in a separate class
 * as the settings will take effect only on classes that were not yet loaded.
 * 
 * @author Julius Stroffek
 *
 */
public class AssertionUtil {
	/**
	 * Enable the assertions globally except XSSF sheets as they
	 * complain even if the sheet does not exist.
	 */
	public static void enableAssertions() {
		AssertionUtil.class.getClassLoader().setDefaultAssertionStatus(true);
		AssertionUtil.class.getClassLoader().setPackageAssertionStatus("org.apache.poi.xssf.streaming", false);
	}
}
