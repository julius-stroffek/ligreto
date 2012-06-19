/**
 * Provides the dialects for different data types for DDL creation.
 */
package net.ligreto.executor.ddl;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import net.ligreto.exceptions.LigretoException;

/**
 * 
 * @author Julius Stroffek
 *
 */
public class DataTypeDialect {

	/** The singleton instance. */
	protected static DataTypeDialect instance = new DerbyDataTypeDialect();
	
	/**
	 * @return the singleton instance
	 */
	public static DataTypeDialect getInstance() {
		return instance;
	}
	
	/**
	 * @param cnn the connection the dialect should correspond to
	 * @return the data
	 */
	public static DataTypeDialect getInstance(Connection cnn) {
		if (cnn.getClass().getName().startsWith("com.oracle")) {
			return OracleDataTypeDialect.getInstance();
		} else if (cnn.getClass().getName().startsWith("org.apache.derby")) {
			return DerbyDataTypeDialect.getInstance();
		} else {
			return getInstance();
		}
	}
	
	/**
	 * The default constructor.
	 */
	protected DataTypeDialect() {
		
	}
	
	protected String getLongDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		assert(metaData.getColumnType(columnIndex) == Types.BIGINT);
		return "long";
	}

	protected String getBooleanDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		assert(metaData.getColumnType(columnIndex) == Types.BOOLEAN);
		return "boolean";
	}

	protected String getCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "char(" + metaData.getPrecision(columnIndex) + ")";
	}
	
	protected String getDateDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "date";
	}
	
	protected String getDecimalDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		StringBuffer sb = new StringBuffer();
		sb.append("decimal(");
		int precision = metaData.getPrecision(columnIndex);
		int scale = metaData.getScale(columnIndex);
		
		if (precision > 31) {
			precision = 31;
		}

		if (precision > 0) {
			sb.append(precision);
			if (scale >= 0) {
				sb.append(",");
				sb.append(scale);
			}
			sb.append(")");
			return sb.toString();
		} else {
			return "double";
		}
	}
	
	protected String getDoubleDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "double";
	}
	
	protected String getFloatDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "float";
	}
	
	protected String getIntegerDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "int";
	}
	
	protected String getLongNVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "longnvarchar(" + metaData.getPrecision(columnIndex) + ")";
	}
	
	protected String getLongVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "longvarchar(" + metaData.getPrecision(columnIndex) + ")";
	}
	
	protected String getNCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "nchar(" + metaData.getPrecision(columnIndex) + ")";
	}
	
	protected String getNumericDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return getDecimalDeclaration(metaData, columnIndex);
	}
	
	protected String getNVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "nvarchar(" + metaData.getPrecision(columnIndex) + ")";
	}
	
	protected String getRealDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return getFloatDeclaration(metaData, columnIndex);
	}
	
	protected String getSmallIntegerDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "short";
	}
	
	protected String getTimeStampDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "timestamp";
	}
	
	protected String getTinyIntegerDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "short";
	}
	
	protected String getVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "varchar(" + metaData.getPrecision(columnIndex) + ")";
	}
	
	/**
	 * 
	 * @param metaData
	 * @param columnIndex
	 * @return
	 * @throws LigretoException 
	 * @throws SQLException 
	 */
	public String getTypeDeclaration(ResultSetMetaData metaData, int columnIndex) throws LigretoException, SQLException {
		switch (metaData.getColumnType(columnIndex)) {
		case Types.BIGINT:
			return getLongDeclaration(metaData, columnIndex);
		case Types.BOOLEAN:
			return getBooleanDeclaration(metaData, columnIndex);
		case Types.CHAR:
			return getCharDeclaration(metaData, columnIndex);
		case Types.DATE:
			return getDateDeclaration(metaData, columnIndex);
		case Types.DECIMAL: 
			return getDecimalDeclaration(metaData, columnIndex);
		case Types.DOUBLE: 
			return getDoubleDeclaration(metaData, columnIndex);
		case Types.FLOAT: 
			return getFloatDeclaration(metaData, columnIndex);
		case Types.INTEGER: 
			return getIntegerDeclaration(metaData, columnIndex);
		case Types.LONGNVARCHAR:
			return getLongNVarCharDeclaration(metaData, columnIndex);
		case Types.LONGVARCHAR: 
			return getLongVarCharDeclaration(metaData, columnIndex);
		case Types.NCHAR:
			return getNCharDeclaration(metaData, columnIndex);
		case Types.NUMERIC:
			return getNumericDeclaration(metaData, columnIndex);
		case Types.NVARCHAR:
			return getNVarCharDeclaration(metaData, columnIndex);
		case Types.REAL:
			return getRealDeclaration(metaData, columnIndex);
		case Types.SMALLINT: 
			return getSmallIntegerDeclaration(metaData, columnIndex);
		case Types.TIMESTAMP:
			return getTimeStampDeclaration(metaData, columnIndex);
		case Types.TINYINT:
			return getTinyIntegerDeclaration(metaData, columnIndex);
		case Types.VARCHAR:
			return getVarCharDeclaration(metaData, columnIndex);
		case Types.ARRAY: 
		case Types.BINARY: 
		case Types.BIT: 
		case Types.BLOB: 
		case Types.CLOB: 
		case Types.DATALINK:
		case Types.DISTINCT: 
		case Types.JAVA_OBJECT:
		case Types.LONGVARBINARY: 
		case Types.NCLOB: 
		case Types.OTHER:
		case Types.REF: 
		case Types.ROWID: 
		case Types.SQLXML: 
		case Types.STRUCT: 
		case Types.TIME: 
		case Types.VARBINARY: 
		default:
			throw new LigretoException("Unsupported data type.");
		}
	}
}
