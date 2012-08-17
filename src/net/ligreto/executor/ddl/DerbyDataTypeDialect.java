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

	protected String getLongNVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "longvarchar(" + metaData.getPrecision(columnIndex) + ")";
	}
	
	protected String getNCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "char(" + metaData.getPrecision(columnIndex) + ")";
	}	
	
	protected String getNVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "varchar(" + metaData.getPrecision(columnIndex) + ")";
	}
}
