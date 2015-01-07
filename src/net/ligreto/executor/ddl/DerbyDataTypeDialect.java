package net.ligreto.executor.ddl;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 
 * @author Julius Stroffek
 *
 */
public class DerbyDataTypeDialect extends DataTypeDialect {

	/** The singleton instance. */
	protected static DerbyDataTypeDialect instance = new DerbyDataTypeDialect();
	
	/**
	 * @return the singleton instance
	 */
	public static DerbyDataTypeDialect getInstance() {
		return instance;
	}
	
	/**
	 * The default constructor
	 */
	public DerbyDataTypeDialect() {
	}

	@Override
	protected String getLongNVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		int precision = metaData.getPrecision(columnIndex);
		if (precision > 0) {
			return "longvarchar(" + metaData.getPrecision(columnIndex) + ")";			
		} else {
			return "longvarchar(8192)";
		}
	}
	
	@Override
	protected String getNCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		int precision = metaData.getPrecision(columnIndex);
		if (precision > 0) {
			return "char(" + metaData.getPrecision(columnIndex) + ")";			
		} else {
			return "char(8192)";
		}
	}	
	
	@Override
	protected String getNVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		int precision = metaData.getPrecision(columnIndex);
		if (precision > 0) {
			return "varchar(" + metaData.getPrecision(columnIndex) + ")";			
		} else {
			return "varchar(8192)";
		}
	}
}
