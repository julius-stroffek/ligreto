package net.ligreto.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.exceptions.InvalidValueException;

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

	/**
	 * 
	 * @param diffs The array of integer numbers (result from comparison)
	 * @return true if all the elements of the array are 0.
	 */
	public static boolean allZeros(int[] diffs) {
		for (int i=0; i < diffs.length; i++) {
			if (diffs[i] != 0)
				return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param rgb color in a form #RRGGBB where RR, GG and BB are hex numbers for red, green and blue.
	 * @return parsed array of red, green and blue values
	 * @throws InvalidFormatException 
	 */
	public static short[] parseRGB(String rgb) throws InvalidFormatException {
		short[] result = new short[3];
		
		if (!rgb.startsWith("#"))
			throw new InvalidFormatException("Invalid color string (should be '#RRGGBB'): " + rgb);
		
		String rr = rgb.substring(1, 3);
		String gg = rgb.substring(3, 5);
		String bb = rgb.substring(5, 7);
		
		result[0] = Short.parseShort(rr, 16);
		result[1] = Short.parseShort(gg, 16);
		result[2] = Short.parseShort(bb, 16);
		
		return result;
	}
	
	public static int findColumnIndex(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=1; i <= rsmd.getColumnCount(); i++) {
			if (rsmd.getColumnName(i).equalsIgnoreCase(columnName))
				return i;
		}
		return -1;
	}
	
	public static boolean parseBoolean(String aBoolean) throws InvalidValueException {
		if ("true".equals(aBoolean))
			return true;
		if ("yes".equals(aBoolean))
			return true;
		if ("false".equals(aBoolean))
			return false;
		if ("no".equals(aBoolean))
			return false;
		throw new InvalidValueException("Invalid string specified as boolean value: " + aBoolean);
	}
}
