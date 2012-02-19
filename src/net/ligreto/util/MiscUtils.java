package net.ligreto.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.exceptions.InvalidValueException;

public class MiscUtils {
	/**
	 * This function searches the specified array for the given element.
	 * 
	 * @param array The array to be searched
	 * @param index The element to be found
	 * @return True if the element is found in the array
	 */
	public static <T> boolean arrayContains(int[] array, int index) {
		if (array != null) {
			for (int i=0; i < array.length; i++) {
				if (array[i] == index)
					return true;
			}
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
	 * @param diffs The array of integer numbers (result from comparison)
	 * @return The number of non zero elements in the array.
	 */
	public static int countNonZeros(int[] diffs) {
		int retValue = 0;
		for (int i=0; i < diffs.length; i++) {
			if (diffs[i] != 0)
				retValue++;
		}
		return retValue;
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
	
	public static int findColumnIndex(DataProvider dp, String columnName) throws DataException {
		for (int i=1; i <= dp.getColumnCount(); i++) {
			if (dp.getColumnName(i).equalsIgnoreCase(columnName))
				return i;
		}
		return -1;
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

	/**
	 * 
	 * @param doubleString
	 * @return
	 */
	public static Double parseDouble(String doubleString) {
		return Double.parseDouble(doubleString);
	}

	/**
	 * 
	 * @param doublePercentage
	 * @return
	 */
	public static Double parseDoublePercentage(String doublePercentage) {
		String trimmed = doublePercentage.trim();
		if (trimmed.endsWith("%")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
			return Double.parseDouble(trimmed)/100;
		} else {
			return Double.parseDouble(trimmed);
		}
	}
}
