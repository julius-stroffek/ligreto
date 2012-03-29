package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;

/**
 * This class provides couple of JDBC related utility functions.
 * 
 * @author Julius Stroffek
 *
 */
public class DataProviderUtils {
	
	/** Holds the association of SQL type and type name. */
	protected static Map<Integer, String> sqlTypeMap = new HashMap<Integer, String>(128);
	
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
	private DataProviderUtils() {
	}
	
	/** @return The object created for the specified index in the result set. */
	public static Object getNumericObject(DataProvider dp, int columnIndex) throws DataException {
		// Check the null value first
		dp.getObject(columnIndex);
		if (dp.wasNull())
			return null;
		
		switch (dp.getColumnType(columnIndex)) {
		case Types.BIGINT:
			return new Long(dp.getLong(columnIndex));
		case Types.INTEGER:
			return new Integer(dp.getInteger(columnIndex));
		case Types.DOUBLE:
		case Types.FLOAT:
			return new Double(dp.getDouble(columnIndex));
		case Types.DECIMAL:
		case Types.NUMERIC:
			BigDecimal bd = dp.getBigDecimal(columnIndex);
			if (bd != null)
				return new BigDecimal(bd.unscaledValue(), bd.scale());
			else
				return null;
		default:
			return null;
		}
	}

	/** @return The numeric object created for the specified index in the data provider or string value. */
	public static Object getNumericObjectOrString(DataProvider dp, int columnIndex) throws DataException {
		Object result = getNumericObject(dp, columnIndex);
		if (result == null) {
			return dp.getString(columnIndex);
		} else {
			return result;
		}
	}

	/** @return The name of the specified JDBC data type. */
	public static String getJdbcTypeName(int jdbcType) {
		return sqlTypeMap.get(jdbcType);
	}
}
