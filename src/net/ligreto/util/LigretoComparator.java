package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Collator;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;

import net.ligreto.LigretoParameters;
import net.ligreto.data.Column;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.DataTypeMismatchException;
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
	
	/** The map holding instances for the threads. */
	private static Map<Long, LigretoComparator> instanceMap = new Hashtable<Long, LigretoComparator>();
	
	/** The ligreto global parameters. */
	protected LigretoParameters ligretoParameters = null;
	
	/** Only static method could create instances. */
	private LigretoComparator() {
		comparator = Collator.getInstance();
	}
	
	/**
	 * @return The instance for the thread in which the function was called.
	 */
	public static LigretoComparator getInstance(LigretoParameters ligretoParameters) {
		long threadId = Thread.currentThread().getId();
		LigretoComparator instance = instanceMap.get(threadId);
		if (instance == null) {
			instance = new LigretoComparator();
			instanceMap.put(threadId, instance);
		}
		instance.ligretoParameters = ligretoParameters;
		return instance;
	}
	
	/**
	 * @return The instance for the thread in which the function was called.
	 */
	public static LigretoComparator getInstance() {
		long threadId = Thread.currentThread().getId();
		LigretoComparator instance = instanceMap.get(threadId);
		if (instance == null) {
			throw new RuntimeException("LigretoComparator was not previously created. Use getInstance(LigretoParameters) to create the object first.");
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
	
	public int compareAsDataSource(DataProvider dp1, int column1, DataProvider dp2, int column2) throws DataException, DataTypeMismatchException {
		int retValue;
		try {
			retValue = compareAsDataSource(dp1.getColumnType(column1), dp1.getObject(column1), dp2.getColumnType(column2), dp2.getObject(column2));
		} catch (DataTypeMismatchException e) {
			e.setColumnIndices(dp1.getOriginalIndex(column1), dp2.getOriginalIndex(column2));
			e.setColumnNames(dp1.getColumnName(column1), dp2.getColumnName(column2));
			throw e;
		}
		return retValue;
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
	
	public int compareAsDataSource(DataProvider dp1, int[] columns1, DataProvider dp2, int[] columns2) throws DataException, DataTypeMismatchException {
		assert(columns1.length == columns2.length);
		
		int cResult;
		for (int i=0; i < columns1.length; i++) {
			cResult = compareAsDataSource(dp1, columns1[i], dp2, columns2[i]);
			if (cResult != 0)
				return cResult;
		}
		return 0;
	}

	public static Column[] duplicate(DataProvider dp, int[] on) throws DataException {
		if (on == null) {
			return new Column[0];
		}
		Column[] result = new Column[on.length];

		for (int i=0; i < on.length; i++) {
			result[i] = new Column(dp, on[i]);
		}
		return result;
	}

	public int compareAsDataSource(Column field1, Column field2) throws LigretoException {
		return compareAsDataSource(field1.getColumnType(), field1.getColumnValue(), field2.getColumnType(), field2.getColumnValue());
	}
	
	public int compareAsDataSource(int fieldType1, Object fieldValue1, int fieldType2, Object fieldValue2) throws DataException, DataTypeMismatchException {
		int result = 0;
		// Take care of the null values first
		boolean isNull1 = fieldValue1 == null;
		boolean isNull2 = fieldValue2 == null;
		if (isNull1 && isNull2)
			return 0;

		result = compareNullsAsDataSource(isNull1, isNull2);
		if (result != 0)
			return result;
		
		if (fieldType1 != fieldType2) {
			if (ligretoParameters.getStrictTypes()) {
				throw new DataTypeMismatchException(fieldType1, fieldType2);
			} else {
				result = compareAsDataSource(fieldValue1.toString(), fieldValue2.toString());
			}
		} else {
			switch (fieldType1) {
			case Types.BOOLEAN:
				result = compare((Boolean) fieldValue1, (Boolean) fieldValue2);
				break;
			case Types.BIGINT:
				result = compare((Long) fieldValue1, (Long) fieldValue2);
				break;
			case Types.INTEGER:
				result = compare((Integer) fieldValue1, (Integer) fieldValue2);
				break;
			case Types.DOUBLE:
			case Types.FLOAT:
				result = compare((Double) fieldValue1, (Double) fieldValue2);
				break;
			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIME:
				result = compare((Timestamp) fieldValue1, (Timestamp) fieldValue2);
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				result = compare((BigDecimal) fieldValue1, (BigDecimal) fieldValue2);
				break;
			default:
				result = compareAsDataSource((String) fieldValue1, (String) fieldValue2);
				break;
			}
		}
		return result;
	}

	public int compareAsDataSource(Column[] fields1, Column[] fields2) throws LigretoException {
		int result = 0;
		if (fields1.length != fields2.length) {
			throw new LigretoException("The field arrays to compare have different lengths.");
		}
		
		for (int i=0; i < fields1.length; i++) {
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

	public int compare(Column field1, Column field2) throws DataException {
		return compare(field1.getColumnType(), field1.getColumnValue(), field2.getColumnType(), field2.getColumnValue());
	}

	public int compare(int fieldType1, Object fieldValue1, int fieldType2, Object fieldValue2) throws DataException {
		int result = 0;
		
		// Take care of the null values first
		boolean isNull1 = fieldValue1 == null;
		boolean isNull2 = fieldValue2 == null;
		result = compareNulls(isNull1, isNull2);
		if (result != 0)
			return result;

		if (fieldType1 != fieldType2) {
			if (ligretoParameters.getStrictTypes()) {
				throw new DataException(
					"Data types differ: "
					+ DataProviderUtils.getJdbcTypeName(fieldType1) + "; "
					+ DataProviderUtils.getJdbcTypeName(fieldType2)
				);
			} else {
				result = compareAsDataSource(fieldValue1.toString(), fieldValue1.toString());
			}
		} else {
			switch (fieldType1) {
			case Types.BOOLEAN:
				result = compare((Boolean) fieldValue1, (Boolean) fieldValue2);
				break;
			case Types.BIGINT:
				result = compare((Long) fieldValue1, (Long) fieldValue2);
				break;
			case Types.INTEGER:
				result = compare((Integer) fieldValue1, (Integer) fieldValue2);
				break;
			case Types.DOUBLE:
			case Types.FLOAT:
				result = compare((Double) fieldValue1, (Double) fieldValue2);
				break;
			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIME:
				result = compare((Timestamp) fieldValue1, (Timestamp) fieldValue2);
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				result = compare((BigDecimal) fieldValue1, (BigDecimal) fieldValue2);
				break;
			default:
				result = compare((String) fieldValue1, (String) fieldValue2);
				break;
			}
		}
		return result;
	}

	public int compare(Column[] fields1, Column[] fields2) throws LigretoException {
		int result = 0;
		if (fields1.length != fields2.length) {
			throw new LigretoException("The field arrays to compare have different lengths.");
		}
		
		for (int i=0; i < fields1.length; i++) {
			if (fields1[i].getColumnType() != fields2[i].getColumnType()) {
				throw new LigretoException("The columns to compare have different types.");
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
	 * @param dp1 The first data provider.
	 * @param key1 The join columns for the first result set.
	 * @param columns1 The list of columns to compare for the first result set.
	 * @param dp2 The second data provider.
	 * @param key2 The join columns for the second result set.
	 * @param columns2 The list of columns to compare for the second result set.
	 * @return The array of comparison results
	 * @throws SQLException
	 * @throws LigretoException
	 */
	public int[] compareOthersAsDataSource(DataProvider dp1, int[] key1, int[] columns1, DataProvider dp2, int[] key2, int[] columns2) throws SQLException, LigretoException {
		assert(key1.length == key2.length);
		assert(columns1.length == columns2.length);
		
		int colCount1 = dp1.getColumnCount();
		int colCount2 = dp2.getColumnCount();
		
		// The assertion checks that on1/2[] pointers are less than
		// the number of columns in result set should be done.
		for (int i=0; i < key1.length; i++) {
			if (key1[i] > colCount1)
				throw new LigretoException("The index in \"key\" attribute (" + key1[i] + ") is larger than the number of columns (" + colCount1 + ").");
			if (key2[i] > colCount2)
				throw new LigretoException("The index in \"key\" attribute (" + key2[i] + ") is larger than the number of columns (" + colCount2 + ").");
		}
		int cmpCount1 = dp1.getColumnCount() - key1.length;
		int cmpCount2 = dp2.getColumnCount() - key2.length;

		int maxCount = cmpCount1 > cmpCount2 ? cmpCount1 : cmpCount2;
		int minCount = cmpCount1 < cmpCount2 ? cmpCount1 : cmpCount2;
		int[] result = new int[maxCount];
		int i=0, i1=1, i2=1;
		for (; i < minCount && i1 <= colCount1 && i2 <= colCount2; i1++, i2++, i++) {
			while (MiscUtils.arrayContains(key1, i1))
				i1++;
			while (MiscUtils.arrayContains(key2, i2))
				i2++;
			
			assert(i1 <= colCount1);
			assert(i2 <= colCount2);
			
			boolean i1Valid = MiscUtils.arrayContains(columns1, i1);
			boolean i2Valid = MiscUtils.arrayContains(columns2, i2);

			if (i1Valid && i2Valid) {
				result[i] = compareAsDataSource(dp1, i1, dp2, i2);
			} else if (i1Valid || i2Valid) {
				throw new LigretoException("Only one from two matching columns is listed for comparison. 1st: " + i1 + "; 2nd: "
						+ i2);
			} else {
				result[i] = 0;
			}
		}
		for (int j=i+1; j < maxCount; j++, i1++, i2++) {
			while (MiscUtils.arrayContains(key1, i1))
				i1++;
			while (MiscUtils.arrayContains(key2, i2))
				i2++;
			
			assert(i1 <= colCount1);
			assert(i2 <= colCount2);
			
			boolean i1Valid = MiscUtils.arrayContains(columns1, i1);
			boolean i2Valid = MiscUtils.arrayContains(columns2, i2);

			if (i1Valid && i2Valid) {
				if (colCount1 > colCount2) {
					dp1.getString(i1);
					result[j] = compareNulls(dp1.wasNull(), true);
				} else if (colCount1 < colCount2) {
					dp2.getString(i1);
					result[j] = compareNulls(true, dp2.wasNull());
				} else {
					assert(false);
				}
			} else if (i1Valid || i2Valid) {
				throw new LigretoException("Only one from two matching columns is listed for comparison. 1st: " + i1 + "; 2nd: "
						+ i2);
			} else {
				result[i] = 0;
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

	public void error(Log log, Column[] col) {
		log.error("Key columns:");
		for (int c=0; c < col.length; c++) {
			if (col[c] != null && col[c].getColumnValue() != null) {
				log.error(col[c].getColumnValue().toString());
			} else {
				log.error("<null>");
			}
		}
	}
}
