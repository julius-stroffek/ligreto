package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import net.ligreto.exceptions.LigretoException;

public class ResultSetComparator {
	public static int compare(ResultSet rs1, int on1, ResultSet rs2, int on2) throws SQLException {
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
			result = rs1.getString(on1).compareTo(rs2.getString(on2));
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
				result = rs1.getString(on1).compareTo(rs2.getString(on2));
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
	
	public static int compare(ResultSet rs1, ResultSet rs2) throws SQLException {
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
	
	public static int compare(ResultSet rs1, int[] on1, ResultSet rs2, int[] on2) throws SQLException {
		Assert.assertTrue(on1.length == on2.length);
		
		int cResult;
		for (int i=0; i < on1.length; i++) {			
			cResult = compare(rs1, on1[i], rs2, on2[i]);
			if (cResult != 0)
				return cResult;
		}
		return 0;
	}

	public static int compareOthers(ResultSet rs1, int[] on1, ResultSet rs2, int[] on2) throws SQLException, LigretoException {
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
		
		int cResult;
		for (int i1=1, i2=1; i1 <= on1.length && i2 <= on2.length; i1++, i2++) {
			while (MiscUtils.arrayContains(on1, i1))
				i1++;
			while (MiscUtils.arrayContains(on2, i2))
				i2++;
			cResult = compare(rs1, i1, rs2, i2);
			if (cResult != 0)
				return cResult;
		}
		return 0;
	}

	private static int compare(boolean b1, boolean b2) {
		return new Boolean(b1).compareTo(b2);
	}

	private static int compare(BigDecimal bd1, BigDecimal bd2) {
		return (bd1.compareTo(bd2));
	}

	private static int compare(Timestamp t1, Timestamp t2) {
		if (t1.before(t2))
			return -1;
		if (t1.after(t2))
			return 1;
		return 0;
	}

	private static int compare(double n1, double n2) {
		if (n1 < n2)
			return -1;
		if (n1 > n2)
			return 1;
		return 0;
	}

	private static int compare(long n1, long n2) {
		if (n1 < n2)
			return -1;
		if (n1 > n2)
			return 1;
		return 0;
	}
}
