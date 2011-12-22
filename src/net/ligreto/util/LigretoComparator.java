package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Collator;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.exceptions.LigretoException;

/**
 * Provides the compare functions for the whole ligreto code. It is important to keep
 * the ordering (collating) the same across various functions.
 * 
 * The class holds separate instance for each thread as each thread might be running
 * different ligreto operations at the same time. The ordering could be adjusted by
 * the user in the configuration file and it could change during processing the same
 * configuration.
 * 
 * @author Julius Stroffek
 * 
 */
public class LigretoComparator {
	
	/** Determines where the null values should be ordered. */
	protected enum NullOrdering {Unspecified, OrderFirst, OrderLast};
	
	/** Determines the actual null ordering policy. */
	protected NullOrdering nullOrdering = NullOrdering.Unspecified;
	
	/** The collator object used for comparisons. */
	protected Comparator<Object> comparator;
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(LigretoComparator.class);

	/** The map holding instances for the threads. */
	private static Map<Long, LigretoComparator> instanceMap = new Hashtable<Long, LigretoComparator>();
	
	/** Only static method could create instances. */
	private LigretoComparator() {
		comparator = Collator.getInstance();
	}
	
	/**
	 * @return The instance for the thread in which the function was called.
	 */
	public static LigretoComparator getInstance() {
		long threadId = Thread.currentThread().getId();
		LigretoComparator instance = instanceMap.get(threadId);
		if (instance == null) {
			instance = new LigretoComparator();
			instanceMap.put(threadId, instance);
		}
		return instance;
	}
	
	/**
	 * It will drop the comparator instance that is stored for the thread
	 * in which the function was called.
	 */
	public static void dropInstance() {
		long threadId = Thread.currentThread().getId();
		instanceMap.remove(threadId);
	}
	
	/**
	 * @param comparator
	 * 				The comparator to set.
	 */
	public void setComparator(Comparator<Object> comparator) {
		this.comparator = comparator;
	}

	/**
	 * Determines whether the first entry should be lower or higher than the second entry
	 * based on the current null ordering policy. If both values are null or both values
	 * are not null the return value is 0.
	 * 
	 * <p>
	 * It is expected that this function will math the null ordering as was in the database.
	 * Thus this method have to be first called from JoinExecutor on the column of subsequent rows
	 * from the same data source where exactly one field is null and one is not null.
	 * </p>
	 * <p>
	 * The ordering should be adjusted only in case of comparing the subsequent rows ordered
	 * on the same data source. Otherwise, the overall behavior is undefined.
	 * </p>
	 * @param isNull1
	 * @param isNull2
	 * @return -1 if the first entry is lower, 1 if the second entry is lower, 0 if both
	 * values are null or both are non-null.
	 */
	public int compareNullsAsDataSource(boolean isNull1, boolean isNull2) {
		if (!isNull1 && !isNull2)
			return 0;
		if (isNull1 && isNull2)
			return 0;
		switch (nullOrdering) {
		case OrderFirst:
			if (isNull1)
				return -1;
			else
				return 1;
		case OrderLast:
			if (isNull2)
				return -1;
			else
				return 1;
		case Unspecified:
			if (isNull1)
				nullOrdering = NullOrdering.OrderFirst;
			else
				nullOrdering = NullOrdering.OrderLast;
			return -1;
		default:
			throw new RuntimeException("Unexpected value of NullOrdering enumeration.");
		}
	}
	
	/**
	 * Do the comparison on null/non-null basis.
	 * 
	 * @param isNull1 Indicates whether first field is null.
	 * @param isNull2 Indicates whether second field is null.
	 * @return -1 if the first entry is lower, 1 if the second entry is lower, 0 if both
	 * values are null or both are non-null.
	 */
	 public int compareNulls(boolean isNull1, boolean isNull2) {
		if (!isNull1 && !isNull2)
			return 0;
		if (isNull1 && isNull2)
			return 0;
		if (isNull1)
			return -1;
		return 1;
	}
	
	/**
	 * Resets the adjustment of null ordering policy done previously.
	 */
	public void resetNullOrdering() {
		nullOrdering = NullOrdering.Unspecified;
	}
	
	public int compareAsDataSource(ResultSet rs1, int on1, ResultSet rs2, int on2) throws SQLException {
		int result = 0;
		// Deal with the case when at least one of the values is null.
		rs1.getString(on1);
		rs2.getString(on2);
		boolean isNull1 = rs1.wasNull();
		boolean isNull2 = rs2.wasNull();
		
		result = compareNullsAsDataSource(isNull1, isNull2);
		if (result != 0)
			return result;
		if (isNull1 && isNull2)
			return 0;
		
		int ct1 = rs1.getMetaData().getColumnType(on1);
		int ct2 = rs2.getMetaData().getColumnType(on2);
		if (ct1 != ct2) {
			log.debug(
				"Data types differ, using string comparison: "
				+ JdbcUtils.getJdbcTypeName(ct1) + "; " + JdbcUtils.getJdbcTypeName(ct2)
			);
			result = compareAsDataSource(rs1.getString(on1), rs2.getString(on2));
		} else {
			switch (ct1) {
			case Types.BOOLEAN:
				result = compare(rs1.getBoolean(on1), rs2.getBoolean(on2));
				break;
			case Types.BIGINT:
			case Types.INTEGER:
				result = compare(rs1.getLong(on1), rs2.getLong(on2));
				break;
			case Types.DOUBLE:
			case Types.FLOAT:
				result = compare(rs1.getDouble(on1), rs2.getDouble(on2));
				break;
			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIME:
				result = compare(rs1.getTimestamp(on1), rs2.getTimestamp(on2));
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				result = compare(rs1.getBigDecimal(on1), rs2.getBigDecimal(on2));
				break;
			case Types.CHAR:
			case Types.VARCHAR:
				result = compareAsDataSource(rs1.getString(on1), rs2.getString(on2));
				break;
			default:
				log.debug("Unknown data type used, using string comparison: " + JdbcUtils.getJdbcTypeName(ct1));
				result = compareAsDataSource(rs1.getString(on1), rs2.getString(on2));
				break;
			}
		}
		// Make sure we are returning only -1, 0 and 1
		if (result > 0)
			return 1;
		if (result < 0)
			return -1;
		return 0;
	}
	
	public int compare(String s1, String s2) {
		boolean isNull1 = s1 == null;
		boolean isNull2 = s2 == null;
		
		int result = compareNulls(isNull1, isNull2);
		if (result != 0)
			return result;
		if (isNull1 && isNull2)
			return 0;	
		
		return comparator.compare(s1.trim(), s2.trim());
	}

	public int compareAsDataSource(String s1, String s2) {
		boolean isNull1 = s1 == null;
		boolean isNull2 = s2 == null;
		
		int result = compareNullsAsDataSource(isNull1, isNull2);
		if (result != 0)
			return result;
		if (isNull1 && isNull2)
			return 0;	
		
		return comparator.compare(s1.trim(), s2.trim());
	}

	/*
	public int compare(ResultSet rs1, ResultSet rs2) throws SQLException {
		int colCount1 = rs1.getMetaData().getColumnCount();
		int colCount2 = rs2.getMetaData().getColumnCount();
		int cmpCount = colCount1 < colCount2 ? colCount1 : colCount2;
		
		int cResult;
		for (int i=1; i <= cmpCount; i++) {
			cResult = compare(rs1, i, rs2, i);
			if (cResult != 0)
				return cResult;
		}
		return 0;
	}
	*/
	
	public int compareAsDataSource(ResultSet rs1, int[] on1, ResultSet rs2, int[] on2) throws SQLException {
		Assert.assertTrue(on1.length == on2.length);
		
		int cResult;
		for (int i=0; i < on1.length; i++) {
			cResult = compareAsDataSource(rs1, on1[i], rs2, on2[i]);
			if (cResult != 0)
				return cResult;
		}
		return 0;
	}

	public static Field[] duplicate(ResultSet rs, int[] on) throws SQLException {
		if (on == null) {
			return new Field[0];
		}
		Field[] result = new Field[on.length];

		for (int i=0; i < on.length; i++) {
			result[i] = new Field(rs, on[i]);
		}
		return result;
	}

	public int compareAsDataSource(Field field1, Field field2) throws LigretoException {
		int result = 0;
		// Take care of the null values first
		boolean isNull1 = field1.columnValue == null;
		boolean isNull2 = field2.columnValue == null;
		result = compareNullsAsDataSource(isNull1, isNull2);
		if (result != 0)
			return result;

		switch (field1.columnType) {
		case Types.BOOLEAN:
			result = compare((Boolean) field1.columnValue, (Boolean) field2.columnValue);
			break;
		case Types.BIGINT:
		case Types.INTEGER:
			result = compare((Long) field1.columnValue, (Long) field2.columnValue);
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			result = compare((Double) field1.columnValue, (Double) field2.columnValue);
			break;
		case Types.DATE:
		case Types.TIMESTAMP:
		case Types.TIME:
			result = compare((Timestamp) field1.columnValue, (Timestamp) field2.columnValue);
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
			result = compare((BigDecimal) field1.columnValue, (BigDecimal) field2.columnValue);
			break;
		default:
			result = compareAsDataSource((String) field1.columnValue, (String) field2.columnValue);
			break;
		}
		return result;
	}

	public int compareAsDataSource(Field[] fields1, Field[] fields2) throws LigretoException {
		int result = 0;
		if (fields1.length != fields2.length) {
			throw new LigretoException("The field arrays to compare have different lengths.");
		}
		
		for (int i=0; i < fields1.length; i++) {
			if (fields1[i].columnType != fields2[i].columnType) {
				throw new LigretoException("The fields to compare have different types.");
			}
			result = compareAsDataSource(fields1[i], fields2[i]);
			if (result != 0)
				break;
		}
		
		if (result < 0)
			result = -1;
		if (result > 0)
			result = 1;
		return result;
	}

	public int compare(Field field1, Field field2) {
		int result = 0;
		// Take care of the null values first
		boolean isNull1 = field1.columnValue == null;
		boolean isNull2 = field2.columnValue == null;
		result = compareNulls(isNull1, isNull2);
		if (result != 0)
			return result;

		switch (field1.columnType) {
		case Types.BOOLEAN:
			result = compare((Boolean) field1.columnValue, (Boolean) field2.columnValue);
			break;
		case Types.BIGINT:
		case Types.INTEGER:
			result = compare((Long) field1.columnValue, (Long) field2.columnValue);
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			result = compare((Double) field1.columnValue, (Double) field2.columnValue);
			break;
		case Types.DATE:
		case Types.TIMESTAMP:
		case Types.TIME:
			result = compare((Timestamp) field1.columnValue, (Timestamp) field2.columnValue);
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
			result = compare((BigDecimal) field1.columnValue, (BigDecimal) field2.columnValue);
			break;
		default:
			result = compare((String) field1.columnValue, (String) field2.columnValue);
			break;
		}
		return result;
	}

	public int compare(Field[] fields1, Field[] fields2) throws LigretoException {
		int result = 0;
		if (fields1.length != fields2.length) {
			throw new LigretoException("The field arrays to compare have different lengths.");
		}
		
		for (int i=0; i < fields1.length; i++) {
			if (fields1[i].columnType != fields2[i].columnType) {
				throw new LigretoException("The fields to compare have different types.");
			}
			result = compare(fields1[i], fields2[i]);
			if (result != 0)
				break;
		}
		
		if (result < 0)
			result = -1;
		if (result > 0)
			result = 1;
		return result;
	}

	/**
	 * Compare the other columns (all columns excel join columns and exclude columns)
	 * based on the database collation specified in configuration. The collation
	 * is defined using {@code ligreto.collatorName} and {@code ligreto.collationName}
	 * parameters. Null values are always collated first.
	 * 
	 * @param rs1 The first result set.
	 * @param on1 The join columns for the first result set.
	 * @param excl1 The exclude columns for the first result set.
	 * @param rs2 The second result set.
	 * @param on2 The join columns for the second result set.
	 * @param excl2 The exclude columns for the second result set.
	 * @return The array of comparison results
	 * @throws SQLException
	 * @throws LigretoException
	 */
	public int[] compareOthersAsDataSource(ResultSet rs1, int[] on1, int[] excl1, ResultSet rs2, int[] on2, int[] excl2) throws SQLException, LigretoException {
		Assert.assertTrue(on1.length == on2.length);
		
		int colCount1 = rs1.getMetaData().getColumnCount();
		int colCount2 = rs2.getMetaData().getColumnCount();
		
		// The assertion checks that on1/2[] pointers are less than
		// the number of columns in result set should be done.
		for (int i=0; i < on1.length; i++) {
			if (on1[i] > colCount1)
				throw new LigretoException("The index in \"on\" attribute (" + on1[i] + ") is larger than the number of columns (" + colCount1 + ").");
			if (on2[i] > colCount2)
				throw new LigretoException("The index in \"on\" attribute (" + on2[i] + ") is larger than the number of columns (" + colCount2 + ").");
		}
		int cmpCount = colCount1 > colCount2 ? colCount1 : colCount2;
		int[] result = new int[cmpCount];
		int i=0, i1=1, i2=1;
		for (; i1 <= colCount1 && i2 <= colCount2; i1++, i2++, i++) {
			while (MiscUtils.arrayContains(on1, i1) || MiscUtils.arrayContains(excl1, i1))
				i1++;
			while (MiscUtils.arrayContains(on2, i2) || MiscUtils.arrayContains(excl2, i2))
				i2++;
			if (i1 <= colCount1 && i2 <= colCount2)
				result[i] = compareAsDataSource(rs1, i1, rs2, i2);
		}
		for (int j=i+1; j < cmpCount; j++, i1++, i2++) {
			if (colCount1 > colCount2) {
				rs1.getString(i1);
				result[j] = compareNulls(rs1.wasNull(), true);
			} else if (colCount1 < colCount2) {
				rs2.getString(i1);
				result[j] = compareNulls(true, rs2.wasNull());
			} else {
				result[j] = 0;
			}
		}
		return result;
	}

	public int compare(boolean b1, boolean b2) {
		return new Boolean(b1).compareTo(b2);
	}

	public int compare(BigDecimal bd1, BigDecimal bd2) {
		return (bd1.compareTo(bd2));
	}

	public int compare(Timestamp t1, Timestamp t2) {
		if (t1.before(t2))
			return -1;
		if (t1.after(t2))
			return 1;
		return 0;
	}

	public int compare(double n1, double n2) {
		if (n1 < n2)
			return -1;
		if (n1 > n2)
			return 1;
		return 0;
	}

	public int compare(long n1, long n2) {
		if (n1 < n2)
			return -1;
		if (n1 > n2)
			return 1;
		return 0;
	}

	public void error(Log log, Field[] col) {
		log.error("Key columns:");
		for (int c=0; c < col.length; c++) {
			if (col[c] != null && col[c].columnValue != null) {
				log.error(col[c].columnValue.toString());
			} else {
				log.error("<null>");
			}
		}
	}
}
