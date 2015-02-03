package net.ligreto.executor.ddl;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 
 * @author Julius Stroffek
 *
 */
public class OracleDataTypeDialect extends DataTypeDialect {

	/** The singleton instance. */
	protected static OracleDataTypeDialect instance = new OracleDataTypeDialect();
	
	/**
	 * @return the singleton instance
	 */
	public static OracleDataTypeDialect getInstance() {
		return instance;
	}
	
	/**
	 * The default constructor
	 */
	protected OracleDataTypeDialect() {
	}

	@Override
	protected String getDecimalDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		StringBuffer sb = new StringBuffer();
		sb.append("number(");
		int precision = metaData.getPrecision(columnIndex);
		int scale = metaData.getScale(columnIndex);
		
		if (precision > 38) {
			precision = 38;
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
			return "number";
		}
	}
	
	protected String getDoubleDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		return "double precision";
	}
	
	@Override
	protected String getNVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		int precision = metaData.getPrecision(columnIndex);
		if (precision > 0) {
			return "nvarchar2(" + metaData.getPrecision(columnIndex) + ")";			
		} else {
			return "nvarchar2(3980)";
		}
	}	

	@Override
	protected String getVarCharDeclaration(ResultSetMetaData metaData, int columnIndex) throws SQLException {
		int precision = metaData.getPrecision(columnIndex);
		if (precision > 0) {
			return "varchar2(" + metaData.getPrecision(columnIndex) + ")";			
		} else {
			return "varchar2(3980)";
		}
	}	
}
