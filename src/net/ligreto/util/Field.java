package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/** This class holds the information about duplicate column from the result set. */
public class Field {
	/**
	 * Creates the instance from the result set. 
	 * @throws SQLException
	 */
	Field(ResultSet rs, int index) throws SQLException {
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