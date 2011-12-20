package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides couple of JDBC related utility functions.
 * 
 * @author Julius Stroffek
 *
 */
public class JdbcUtils {
	
	/** Holds the association of SQL type and type name. */
	protected static Map<Integer, String> sqlTypeMap = new HashMap<Integer, String>();
	
	/** Initialize the sql type name association array. */
	static {
		// Get all field in java.sql.Types
		java.lang.reflect.Field[] fields = java.sql.Types.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				String name = fields[i].getName();
				Integer value = (Integer) fields[i].get(null);
				sqlTypeMap.put(value, name);
			} catch (IllegalAccessException e) {
			}
		}
	}
	
	/** No instances allowed. */
	private JdbcUtils() {
	}
	
	/** @return The object created for the specified index in the result set. */
	public static Object getNumericObject(ResultSet rs, int columnIndex) throws SQLException {
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

	/** @return The numeric object created for the specified index in the result set or string value. */
	public static Object getNumericObjectOrString(ResultSet rs, int columnIndex) throws SQLException {
		Object result = getNumericObject(rs, columnIndex);
		if (result == null) {
			return rs.getString(columnIndex);
		} else {
			return result;
		}
	}

	/** @return The name of the specified JDBC data type. */
	public static String getJdbcTypeName(int jdbcType) {
		return sqlTypeMap.get(jdbcType);
	}
}
