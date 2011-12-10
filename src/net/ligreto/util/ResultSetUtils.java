package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class provides couple of java.sql.ResultSet based utility functions.
 * 
 * @author Julius Stroffek
 *
 */
public class ResultSetUtils {
	
	/** No instances allowed. */
	private ResultSetUtils() {
	}
	
	/** @return The object created for the specified index in the result set. */
	public static Object getResultSetNumericObject(ResultSet rs, int columnIndex) throws SQLException {
		// Check the null value first
		rs.getObject(columnIndex);
		if (rs.wasNull())
			return null;
		
		switch (rs.getMetaData().getColumnType(columnIndex)) {
		case Types.BIGINT:
		case Types.INTEGER:
			return new Long(rs.getLong(columnIndex));
		case Types.DOUBLE:
		case Types.FLOAT:
			return new Double(rs.getDouble(columnIndex));
		case Types.DECIMAL:
		case Types.NUMERIC:
			BigDecimal bd = rs.getBigDecimal(columnIndex);
			if (bd != null)
				return new BigDecimal(bd.unscaledValue(), bd.scale());
			else
				return null;
		default:
			return null;
		}
	}
}
