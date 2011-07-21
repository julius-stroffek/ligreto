package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

public class Comparator {
	public static int compare(ResultSet rs1, int[] on1, ResultSet rs2, int[] on2) throws SQLException {
		Assert.assertTrue(on1.length == on2.length);
		
		int cResult;
		for (int i=0; i < on1.length; i++) {			
			// Deal with the case when at least one of the values is null.
			rs1.getString(on1[i]);
			rs2.getString(on2[i]);
			boolean isNull1 = rs1.wasNull();
			boolean isNull2 = rs2.wasNull();
			
			if (isNull1 && isNull2)
				return 0;
			if (isNull1)
				return -1;
			if (isNull2)
				return 1;
			
			int ct1 = rs1.getMetaData().getColumnType(on1[i]);
			int ct2 = rs2.getMetaData().getColumnType(on2[i]);
			if (ct1 != ct2) {
				cResult = rs1.getString(on1[i]).compareTo(rs2.getString(on2[i]));
			} else {
				switch (ct1) {
				case Types.BOOLEAN:
					cResult = compare(rs1.getBoolean(on1[i]), rs2.getBoolean(on2[i]));
					break;
				case Types.BIGINT:
				case Types.INTEGER:
					cResult = compare(rs1.getLong(on1[i]), rs2.getLong(on2[i]));
					break;
				case Types.DOUBLE:
				case Types.FLOAT:
					cResult = compare(rs1.getDouble(on1[i]), rs2.getDouble(on2[i]));
					break;
				case Types.DATE:
				case Types.TIMESTAMP:
				case Types.TIME:
					cResult = compare(rs1.getTimestamp(on1[i]), rs2.getTimestamp(on2[i]));
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					cResult = compare(rs1.getBigDecimal(on1[i]), rs2.getBigDecimal(on2[i]));
				default:
					cResult = rs1.getString(on1[i]).compareTo(rs2.getString(on2[i]));
					break;
				}
			}
			// Make sure we are returning only -1, 0 and 1
			if (cResult > 0)
				return 1;
			if (cResult < 0)
				return -1;
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
