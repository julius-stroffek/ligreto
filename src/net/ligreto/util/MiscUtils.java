package net.ligreto.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.exceptions.InvalidValueException;

public class MiscUtils {
	
	/** The logger instance for the class. */
	private static Log log = LogFactory.getLog(MiscUtils.class);

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
	 * Parses the specified string as double.
	 * 
	 * @param doubleString
	 * @return the parsed double value
	 */
	public static Double parseDouble(String doubleString) {
		return Double.parseDouble(doubleString);
	}

	/**
	 * Parses the specified string as double. It will adjust the number (divide by 100)
	 * of the value is specified as percentage.
	 * 
	 * @param doublePercentage
	 * @return the parsed double value
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
	
	/**
	 * Dumps exception messages across all the exception causes.
	 * 
	 * @param log the log where the messages are printed out
	 * @param t the throwable to dump the information
	 */
	public static void printThrowableMessages(Logger log, Throwable t) {
		if (Level.DEBUG.isGreaterOrEqual(Logger.getRootLogger().getLevel())) {
			log.error(null, t);
		} else {
			ArrayList<Throwable> causes = new ArrayList<Throwable>();
			while (t != null) {
				causes.add(t);
				t = t.getCause();
			}
			String previousMessage = null;
			for (int i = causes.size() - 1; i >= 0; i--) {
				String newMessage = causes.get(i).getMessage();
				if (previousMessage == null || !previousMessage.equals(newMessage)) {
					log.error(newMessage);
				}
				previousMessage = newMessage;
			}
		}
	}
	
	public static String fixFileExt(String fileName, String fileExt) {
		if (!fileName.endsWith(fileExt)) {
			log.info("Changing the file extension on file: " + fileName);
			log.info("New extension will be \"" + fileExt + "\".");
			int dotIndex = fileName.lastIndexOf('.');
			if (dotIndex >= 0) {
				fileName = fileName.substring(0, dotIndex) + fileExt;
			} else {
				fileName += "fileExt";
			}
		}
		return fileName;
	}
}
