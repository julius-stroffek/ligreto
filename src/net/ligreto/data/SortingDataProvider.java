package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;

import net.ligreto.exceptions.DataException;

public class SortingDataProvider extends DataProvider {

	protected DataProvider dataProvider;
	protected int columnCount;
	protected int[] columnTypes;
	protected int[] keyColumns;
	protected DataProviderRow[] rows;
	protected int currentRow;
	protected boolean prepared = false;
	protected boolean wasNull = false;
	protected int indexInDuplicates = 0;
	protected boolean duplicateKey = false;
	protected int cmpKey = -1;
	
	public SortingDataProvider(DataProvider dataProvider, int[] keyColumns) throws DataException {
		this.dataProvider = dataProvider;
		this.keyColumns = keyColumns;
		setCaption(dataProvider.getCaption());
	}

	public void prepareData() throws DataException {
		columnCount = dataProvider.getColumnCount();
		columnTypes = new int[columnCount];
		LinkedList<DataProviderRow> rowList = new LinkedList<DataProviderRow>();

		for (int i=0; i < columnCount; i++) {
			columnTypes[i] = dataProvider.getColumnType(i+1);
		}
		DataProviderRow row = null;
		while (dataProvider.next()) {
			row = new DataProviderRow(columnTypes, dataProvider, keyColumns);
			rowList.add(row);
		}
		DataProviderRow[] arrayType = new DataProviderRow[0];
		rows = rowList.toArray(arrayType);
		Arrays.sort(rows);
		currentRow = -1;
		cmpKey = -1;
		prepared = true;
	}
	
	@Override
	public boolean next() throws DataException {
		assert(prepared);
		currentRow++;
		
		// We are already over the data
		if (currentRow >= rows.length) {
			return false;
		}
		
		if (cmpKey == 0) {
			indexInDuplicates++;
			duplicateKey = true;
		} else {
			indexInDuplicates = 0;
			duplicateKey = false;
		}

		if (currentRow + 1 < rows.length) {
			cmpKey = rows[currentRow].compareTo(rows[currentRow + 1]);
			if (cmpKey == 0) {
				duplicateKey = true;
			} else {
				assert(cmpKey == -1);
			}
		}
		
		return true;
	}

	@Override
	public Boolean getBoolean(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.BOOLEAN);
		Boolean result = (Boolean) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Integer getInteger(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.INTEGER);
		Integer result = (Integer) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Long getLong(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.BIGINT);
		Long result = (Long) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Double getDouble(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index < columnTypes.length);
		Double result = null;
		switch (columnTypes[index-1]) {
		case Types.DOUBLE:
		case Types.FLOAT:
			result = (Double) rows[currentRow].columnValues[index-1];
			break;
		default:
			assert(false);
			// Just to keep the compiler quiet
			return null;
		}
		wasNull = (result == null);
		return result;
	}

	@Override
	public Timestamp getTimestamp(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.TIMESTAMP);
		Timestamp result = (Timestamp) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public BigDecimal getBigDecimal(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.TIMESTAMP);
		BigDecimal result = (BigDecimal) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Object getObject(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		Object result = rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public String getString(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.TIMESTAMP);
		String result = (String) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public int getColumnType(int index) throws DataException {
		assert(prepared);
		return columnTypes[index-1];
	}

	@Override
	public String getColumnLabel(int index) throws DataException {
		assert(prepared);
		return dataProvider.getColumnLabel(index);
	}

	@Override
	public String getColumnName(int index) throws DataException {
		assert(prepared);
		return dataProvider.getColumnName(index);
	}

	@Override
	public int getColumnCount() throws DataException {
		assert(prepared);
		return columnTypes.length;
	}

	@Override
	public int getOriginalIndex(int index) throws DataException {
		assert(prepared);
		return dataProvider.getOriginalIndex(index);
	}

	@Override
	public int getIndex(int originalIndex) throws DataException {
		assert(prepared);
		return dataProvider.getIndex(originalIndex);
	}

	@Override
	public boolean wasNull() throws DataException {
		assert(prepared);
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
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.TIMESTAMP);
		Time result = (Time) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public Date getDate(int index) throws DataException {
		assert(prepared && currentRow < rows.length);
		assert(index > 0 && index <= columnTypes.length);
		assert(columnTypes[index-1] == Types.TIMESTAMP);
		Date result = (Date) rows[currentRow].columnValues[index-1];
		wasNull = (result == null);
		return result;
	}

	@Override
	public boolean isActive() throws DataException {
		if (!prepared) {
			return false;
		}
		if (currentRow >= 0 && currentRow < rows.length) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hasDuplicateKey() throws DataException {
		return duplicateKey;
	}

	@Override
	public int getIndexInDuplicates() throws DataException {
		return indexInDuplicates;
	}
}
