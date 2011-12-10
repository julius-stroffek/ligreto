package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Collator;
import java.util.Comparator;

import org.apache.commons.logging.Log;

import net.ligreto.exceptions.LigretoException;

public class ResultSetComparator {
	
	/** This class holds the information about duplicate column from the result set. */
	public class Column {
		/**
		 * Creates the instance from the result set. 
		 * @throws SQLException
		 */
		private Column(ResultSet rs, int index) throws SQLException {
			columnType = rs.getMetaData().getColumnType(index);
			switch (columnType) {
			case Types.BOOLEAN:
				columnValue = new Boolean(rs.getBoolean(index));
				break;
			case Types.BIGINT:
			case Types.INTEGER:
				columnValue = new Long(rs.getLong(index));
				break;
			case Types.DOUBLE:
			case Types.FLOAT:
				columnValue = new Double(rs.getDouble(index));
				break;
			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIME:
				columnValue = new Timestamp(rs.getTimestamp(index).getTime());
				break;
			case Types.DECIMAL:
			case Types.NUMERIC:
				BigDecimal bd = rs.getBigDecimal(index);
				columnValue = new BigDecimal(bd.unscaledValue(), bd.scale());
				break;
			default:
				columnType = Types.VARCHAR;
				String tmpValue = rs.getString(index);
				if (tmpValue != null)
					columnValue = new String(tmpValue);
				else
					columnValue = null;
				break;			
			}
			if (rs.wasNull())
				columnValue = null;
		}
		
		/** The column type that correspond to java.sql.Types definitions. */
		public int columnType;
		
		/** The column value. */
		public Object columnValue;
	}
	
	/** The collator object used for comparisons. */
	protected Comparator<Object> comparator;
	
	public ResultSetComparator() {
		comparator = Collator.getInstance();
	}
	
	public ResultSetComparator(Comparator<Object> comparator) {
		this.comparator = comparator;
	}

	public int compare(ResultSet rs1, int on1, ResultSet rs2, int on2) throws SQLException {
		int result = 0;
		// Deal with the case when at least one of the values is null.
		rs1.getString(on1);
		rs2.getString(on2);
		boolean isNull1 = rs1.wasNull();
		boolean isNull2 = rs2.wasNull();
		
		if (isNull1 && isNull2)
			return 0;
		if (isNull1)
			return -1;
		if (isNull2)
			return 1;
		
		int ct1 = rs1.getMetaData().getColumnType(on1);
		int ct2 = rs2.getMetaData().getColumnType(on2);
		if (ct1 != ct2) {
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
			default:
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

	public Column[] duplicate(ResultSet rs, int[] on) throws SQLException {
		Column[] result = new Column[on.length];
		for (int i=0; i < on.length; i++) {
			result[i] = new Column(rs, on[i]);
		}
		return result;
	}

	public int compare(Column[] cols1, Column[] cols2) throws LigretoException {
		int result = 0;
		if (cols1.length != cols2.length) {
			throw new LigretoException("The column arrays to compare have different lengths.");
		}
		for (int i=0; i < cols1.length; i++) {
			if (cols1[i].columnType != cols2[i].columnType) {
				throw new LigretoException("The columns to compare have different types.");
			}
			
			// Take care of the null values first
			if (cols1[i] == null && cols2[i] == null) {
				result = 0;
			} else if (cols1[i] == null) {
				result = -1;
			} else if (cols2[i] == null) {
				result = 1;
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
				if (rs1.wasNull())
					result[j] = 0;
				else
					result[j] = 1;
			} else if (colCount1 < colCount2) {
				rs2.getString(i1);
				if (rs2.wasNull())
					result[j] = 0;
				else
					result[j] = -1;
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

	public void error(Log log, Column[] col) {
		log.error("Key columns:");
		for (int c=0; c < col.length; c++) {
			log.error(col[c].columnValue.toString());
		}
	}
}
