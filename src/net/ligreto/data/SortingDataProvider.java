package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Assert;

import net.ligreto.exceptions.DataException;
import net.ligreto.util.LigretoComparator;

public class SortingDataProvider extends DataProvider {

	protected class DataProviderRow implements Comparable<DataProviderRow> {
		
		protected int[] columnTypes;
		protected Object[] columnValues;
		protected int[] keyColumns;
		
		public DataProviderRow(int[] columnTypes, Object[] columnValues, int[] keyColumns) {
			this.columnTypes = columnTypes;
			this.columnValues = columnValues;
			this.keyColumns = keyColumns;
		}

		@Override
		public int compareTo(DataProviderRow other) {
			int result = 0;

			Assert.assertTrue(columnTypes.length == other.columnTypes.length);
			Assert.assertTrue(keyColumns.length == other.keyColumns.length);
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
	protected DataProvider dataProvider;
	protected int columnCount;
	protected int[] columnTypes;
	protected int[] keyColumns;
	protected DataProviderRow[] rows;
	protected int currentRow;
	protected boolean prepared = false;
	protected boolean wasNull = false;
	
	public SortingDataProvider(DataProvider dataProvider, int[] keyColumns) throws DataException {
		this.dataProvider = dataProvider;
		this.keyColumns = keyColumns;
	}

	public void prepareData() throws DataException {
		columnCount = dataProvider.getColumnCount();
		columnTypes = new int[columnCount];
		LinkedList<DataProviderRow> rowList = new LinkedList<DataProviderRow>();

		for (int i=0; i < columnCount; i++) {
			columnTypes[i] = dataProvider.getColumnType(i+1);
		}
		Object[] columnValues = null;
		DataProviderRow row = null;
		while (dataProvider.next()) {
			columnValues = new Object[columnCount];
			for (int i=0; i < columnCount; i++) {
				columnValues[i] = dataProvider.getObject(i+1);
			}
			row = new DataProviderRow(columnTypes, columnValues, keyColumns);
			rowList.add(row);
		}
		DataProviderRow[] arrayType = new DataProviderRow[0];
		rows = rowList.toArray(arrayType);
		Arrays.sort(rows);
		currentRow = -1;
		prepared = true;
	}
	
	@Override
	public boolean next() throws DataException {
		Assert.assertTrue(prepared);
		currentRow++;
		return (currentRow < rows.length);
	}

	@Override
	public Boolean getBoolean(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.BOOLEAN);
		Boolean result = (Boolean) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Integer getInteger(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.INTEGER);
		Integer result = (Integer) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Long getLong(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.BIGINT);
		Long result = (Long) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Double getDouble(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index < columnTypes.length);
		Double result = null;
		switch (columnTypes[index-1]) {
		case Types.DOUBLE:
		case Types.FLOAT:
			result = (Double) rows[currentRow].columnValues[index-1];
			break;
		default:
			Assert.assertTrue(false);
			// Just to keep the compiler quiet
			return null;
		}
		wasNull = (result == null);
		return result;
	}

	@Override
	public Timestamp getTimestamp(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.TIMESTAMP);
		Timestamp result = (Timestamp) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public BigDecimal getBigDecimal(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.TIMESTAMP);
		BigDecimal result = (BigDecimal) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Object getObject(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Object result = rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public String getString(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.TIMESTAMP);
		String result = (String) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public int getColumnType(int index) throws DataException {
		Assert.assertTrue(prepared);
		return columnTypes[index-1];
	}

	@Override
	public String getColumnLabel(int index) throws DataException {
		Assert.assertTrue(prepared);
		return dataProvider.getColumnLabel(index);
	}

	@Override
	public String getColumnName(int index) throws DataException {
		Assert.assertTrue(prepared);
		return dataProvider.getColumnName(index);
	}

	@Override
	public int getColumnCount() throws DataException {
		Assert.assertTrue(prepared);
		return columnTypes.length;
	}

	@Override
	public int getOriginalIndex(int index) throws DataException {
		Assert.assertTrue(prepared);
		return dataProvider.getOriginalIndex(index);
	}

	@Override
	public int getIndex(int originalIndex) throws DataException {
		Assert.assertTrue(prepared);
		return dataProvider.getIndex(originalIndex);
	}

	@Override
	public boolean wasNull() throws DataException {
		Assert.assertTrue(prepared);
		return wasNull;
	}

	@Override
	public boolean isNumeric(int index) throws DataException {
		switch (getColumnType(index)) {
		case Types.BIGINT:
		case Types.INTEGER:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.DECIMAL:
		case Types.NUMERIC:
			return true;
		default:
			return false;			
		}
	}

	@Override
	public Time getTime(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.TIMESTAMP);
		Time result = (Time) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Date getDate(int index) throws DataException {
		Assert.assertTrue(prepared && currentRow < rows.length);
		Assert.assertTrue(index > 0 && index <= columnTypes.length);
		Assert.assertTrue(columnTypes[index-1] == Types.TIMESTAMP);
		Date result = (Date) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}
}
