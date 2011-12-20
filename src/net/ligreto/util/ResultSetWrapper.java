package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;

public class ResultSetWrapper extends DataSource {
	
	protected ResultSet resultSet;
	
	public ResultSetWrapper(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public ResultSetMetaData getMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean next() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getBoolean(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLong(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDouble(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public Timestamp getTimestamp(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public BigDecimal getBigDecimal(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean wasNull() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getString(int index) {
		// TODO Auto-generated method stub
		return null;
	}
}
