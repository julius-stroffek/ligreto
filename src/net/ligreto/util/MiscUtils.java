package net.ligreto.util;

public class MiscUtils {
	/**
	 * This function searches the specified array for the given element.
	 * 
	 * @param on1 The array to be searched
	 * @param i1 The element to be found
	 * @return True if the element is found in the array
	 */
	public static <T> boolean arrayContains(int[] on1, int i1) {
		for (int i=0; i < on1.length; i++) {
			if (on1[i] == i1)
				return true;
		}
		return false;
	}
}
