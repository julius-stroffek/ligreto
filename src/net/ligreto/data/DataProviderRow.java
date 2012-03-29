package net.ligreto.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.ligreto.exceptions.DataException;
import net.ligreto.util.LigretoComparator;

public class DataProviderRow implements Comparable<DataProviderRow> {
	
	protected int[] columnTypes;
	protected Object[] columnValues;
	protected int[] keyColumns;
	
	public DataProviderRow(int[] columnTypes, Object[] columnValues, int[] keyColumns) {
		this.columnTypes = columnTypes;
		this.columnValues = columnValues;
		this.keyColumns = keyColumns;
	}

	public DataProviderRow(int[] columnTypes, DataProvider dataProvider, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;

		columnValues = new Object[dataProvider.getColumnCount()];
		for (int i=0; i < columnValues.length; i++) {
			columnValues[i] = dataProvider.getObject(i+1);
		}
	}

	public DataProviderRow(int[] columnTypes, ResultSet resultSet, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;

		try {
			columnValues = new Object[resultSet.getMetaData().getColumnCount()];
			for (int i = 0; i < columnValues.length; i++) {
				columnValues[i] = resultSet.getObject(i + 1);
			}
		} catch (SQLException e) {
			throw new DataException("Failed to get the data.", e);
		}
	}

	public DataProviderRow(int[] columnTypes, DataProvider dataProvider, int[] columns, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;
		assert(columnTypes.length == columns.length);
		
		columnValues = new Object[columns.length];
		for (int i=0; i < columnValues.length; i++) {
			columnValues[i] = dataProvider.getObject(columns[i]);
		}
	}

	public DataProviderRow(int[] columnTypes, ResultSet resultSet, int[] columns, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;
		assert(columnTypes.length == columns.length);
		
		try {
			columnValues = new Object[columns.length];
			for (int i = 0; i < columnValues.length; i++) {
				columnValues[i] = resultSet.getObject(columns[i]);
			}
		} catch (SQLException e) {
			throw new DataException("Failed to get the data.", e);
		}
	}

	@Override
	public int compareTo(DataProviderRow other) {
		int result = 0;

		assert(columnTypes.length == other.columnTypes.length);
		assert(keyColumns.length == other.keyColumns.length);
		try {
			for (int i = 0; i < keyColumns.length; i++) {
				result = LigretoComparator.getInstance().compare(columnTypes[keyColumns[i] - 1],
						columnValues[keyColumns[i] - 1], other.columnTypes[other.keyColumns[i] - 1],
						other.columnValues[other.keyColumns[i] - 1]);
				if (result != 0)
					break;
			}
		} catch (DataException e) {
			throw new IllegalArgumentException(e);
		}
		
		return result;
	}
}