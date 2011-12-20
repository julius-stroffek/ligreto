package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.exceptions.LigretoException;

public class ResultSetComparator {
	
	/** Determines where the null values should be ordered. */
	protected enum NullOrdering {Unspecified, OrderFirst, OrderLast};
	
	/** Determines the actual null ordering policy. */
	protected NullOrdering nullOrdering = NullOrdering.Unspecified;
	
	/** The collator object used for comparisons. */
	protected Comparator<Object> comparator;
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ResultSetComparator.class);

	public ResultSetComparator() {
		comparator = Collator.getInstance();
	}
	
	public ResultSetComparator(Comparator<Object> comparator) {
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
	 * on the same data source. Otherwise, the overall behavior is undefined and the parameter
	 * {@code adjust} should be {@code false}.
	 * </p>
	 * @param isNull1
	 * @param isNull2
	 * @param adjust Indicates whether to adjust the settings to the order which was received.
	 *               This makes sense only if orderNulls has value 'Unspecified'.
	 * @return -1 if the first entry is lower, 1 if the second entry is lower, 0 if both
	 * values are null or both are non-null.
	 */
	public int compareNulls(boolean isNull1, boolean isNull2, boolean adjust) {
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
			if (adjust) {
				if (isNull1)
					nullOrdering = NullOrdering.OrderFirst;
				else
					nullOrdering = NullOrdering.OrderLast;
			}
			return -1;
		default:
			throw new RuntimeException("Unexpected value of NullOrdering enumeration.");
		}
	}
	
	/**
	 * Resets the adjustment of null ordering policy done previously.
	 */
	public void restNullOrdering() {
		nullOrdering = NullOrdering.Unspecified;
	}
	
	public int compare(ResultSet rs1, int on1, ResultSet rs2, int on2) throws SQLException {
		int result = 0;
		// Deal with the case when at least one of the values is null.
		rs1.getString(on1);
		rs2.getString(on2);
		boolean isNull1 = rs1.wasNull();
		boolean isNull2 = rs2.wasNull();
		
		result = compareNulls(isNull1, isNull2, false);
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
			result = compare(rs1.getString(on1), rs2.getString(on2));
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
				result = compare(rs1.getString(on1), rs2.getString(on2));
				break;
			default:
				log.debug("Unknown data type used, using string comparison: " + JdbcUtils.getJdbcTypeName(ct1));
				result = compare(rs1.getString(on1), rs2.getString(on2));
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
		
		int result = compareNulls(isNull1, isNull2, false);
		if (result != 0)
			return result;
		if (isNull1 && isNull2)
			return 0;	
		
		return comparator.compare(s1.trim(), s2.trim());
	}

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
	
	public int compare(ResultSet rs1, int[] on1, ResultSet rs2, int[] on2) throws SQLException {
		Assert.assertTrue(on1.length == on2.length);
		
		int cResult;
		for (int i=0; i < on1.length; i++) {
			cResult = compare(rs1, on1[i], rs2, on2[i]);
			if (cResult != 0)
				return cResult;
		}
		return 0;
	}

	public static Field[] duplicate(ResultSet rs, int[] on) throws SQLException {
		Field[] result = new Field[on.length];

		for (int i=0; i < on.length; i++) {
			result[i] = new Field(rs, on[i]);
		}
		return result;
	}

	public int compare(Field[] cols1, Field[] cols2) throws LigretoException {
		int result = 0;
		if (cols1.length != cols2.length) {
			throw new LigretoException("The column arrays to compare have different lengths.");
		}
		for (int i=0; i < cols1.length; i++) {
			if (cols1[i].columnType != cols2[i].columnType) {
				throw new LigretoException("The columns to compare have different types.");
			}
			
			// Take care of the null values first
			boolean isNull1 = cols1[i].columnValue == null;
			boolean isNull2 = cols2[i].columnValue == null;
			result = compareNulls(isNull1, isNull2, true);
			if (result != 0) {
				break;
			} else if (isNull1 && isNull2) {
				continue;
			} else {
				switch (cols1[i].columnType) {
				case Types.BOOLEAN:
					result = compare((Boolean)cols1[i].columnValue, (Boolean)cols2[i].columnValue);
					break;
				case Types.BIGINT:
				case Types.INTEGER:
					result = compare((Long)cols1[i].columnValue, (Long)cols2[i].columnValue);
					break;
				case Types.DOUBLE:
				case Types.FLOAT:
					result = compare((Double)cols1[i].columnValue, (Double)cols2[i].columnValue);
					break;
				case Types.DATE:
				case Types.TIMESTAMP:
				case Types.TIME:
					result = compare((Timestamp)cols1[i].columnValue, (Timestamp)cols2[i].columnValue);
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					result = compare((BigDecimal)cols1[i].columnValue, (BigDecimal)cols2[i].columnValue);
					break;
				default:
					result = compare((String)cols1[i].columnValue, (String)cols2[i].columnValue);
					break;
				}
			}
			if (result != 0)
				break;
		}
		
		if (result < 0)
			result = -1;
		if (result > 0)
			result = 1;
		return result;
	}

	public int[] compareOthers(ResultSet rs1, int[] on1, int[] excl1, ResultSet rs2, int[] on2, int[] excl2) throws SQLException, LigretoException {
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
				result[i] = compare(rs1, i1, rs2, i2);
		}
		for (int j=i+1; j < cmpCount; j++, i1++, i2++) {
			if (colCount1 > colCount2) {
				rs1.getString(i1);
				result[j] = compareNulls(rs1.wasNull(), true, false);
			} else if (colCount1 < colCount2) {
				rs2.getString(i1);
				result[j] = compareNulls(true, rs2.wasNull(), false);
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
