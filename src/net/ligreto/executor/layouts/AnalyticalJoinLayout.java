package net.ligreto.executor.layouts;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.Column;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.Assert;
import net.ligreto.util.LigretoComparator;

/**
 * The layout doing group by aggregation on the columns that are compared. It is expected
 * to offer analytical insight into the failures/mismatches. For every union of columns
 * from both of the data sources it calculates the number of occurences in the compared
 * data.
 * 
 * <p>
 * The results to be presented have to fit into java heap memory. You can limit the number
 * of rows processed and required to be stored into memory by specifying the limit
 * on the layout.
 * </p>
 * 
 * @author Julius Stroffek
 *
 */
public class AnalyticalJoinLayout extends JoinLayout {

	/**
	 * This is the inner class that is used to calculate the number of occurrences.
	 * 
	 * @author Julius Stroffek
	 */
	protected class AnalysisEntry implements Comparable<AnalysisEntry> {
		Column[] cols1 = null;
		Column[] cols2 = null;
		Integer count = 1;
		int rowDiffs = 0;
		
		/**
		 * The constructor getting the union of the specified columns. The number of columns
		 * on columns1 and columns2 have to be the same.
		 * 
		 * <p>
		 * We implement hashCode and equals in a way that if the column content is the same
		 * hashCode returns the same value and equals returns true. The compareTo is implemented
		 * only to compare the count as the data are finally sorted according the number of
		 * occurrences.
		 * </p>
		 * 
		 * @param dp1 the data provider of the first column set
		 * @param columns1 the columns to be stored for analysis from the first data provider
		 * @param dp2 the data provider of the second columns set
		 * @param columns2 the columns to be stored for analysis from the second data provider
		 * @throws DataException if there are any issues in getting the data
		 */
		AnalysisEntry(DataProvider dp1, int[] columns1, DataProvider dp2, int[] columns2) throws DataException {
			Assert.assertTrue(columns1.length == columns2.length);
			cols1 = null;
			if (dp1 != null && columns1 != null && columns1.length > 0) {
				cols1 = LigretoComparator.duplicate(dp1, columns1);
			} else {
				cols1 = new Column[columns1.length];
				for (int i=0; i < columns1.length; i++) {
					cols1[i] = null;
				}
			}
			if (dp2 != null && columns2 != null && columns2.length > 0) {
				cols2 = LigretoComparator.duplicate(dp2, columns2);
			} else {
				cols2 = new Column[columns2.length];
				for (int i=0; i < columns2.length; i++) {
					cols2[i] = null;
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(cols1);
			result = prime * result + Arrays.hashCode(cols2);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof AnalysisEntry)) {
				return false;
			}
			AnalysisEntry other = (AnalysisEntry) obj;
			for (int i=0; i < cols1.length; i++) {
				if (cols1[i] == null && other.cols1[i] == null)
					continue;
				
				if (cols1[i] == null || other.cols1[i] == null)
					return false;
				
				if (!cols1[i].equals(other.cols1[i])) {
					return false;
				}
			}
			for (int i=0; i < cols2.length; i++) {
				if (cols2[i] == null && other.cols2[i] == null)
					continue;
				
				if (cols2[i] == null || other.cols2[i] == null)
					return false;
				
				if (!cols2[i].equals(other.cols2[i])) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int compareTo(AnalysisEntry o) {
			// We do not care about nulls as there should not be nulls at all as counts
			return o.count.compareTo(count);
		}		
	}
	
	protected HashMap<AnalysisEntry, Integer> analysisMap = new HashMap<AnalysisEntry, Integer>();
	protected HashMap<Integer, Void> noResultColumns1 = new HashMap<Integer, Void>();
	protected HashMap<Integer, Void> noResultColumns2 = new HashMap<Integer, Void>();
	int[] resultColumns1 = null;
	int[] resultColumns2 = null;
	int resultCount = 0;

	public AnalyticalJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws DataException, DataSourceNotDefinedException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "# of Occur.", HeaderType.TOP);
		targetBuilder.setColumnPosition(1, 2, null);
		targetBuilder.dumpOtherHeader(dp1, on1, null, dataSourceDesc1);
		targetBuilder.setColumnPosition(2, 2, null);
		targetBuilder.dumpOtherHeader(dp2, on2, null, dataSourceDesc2);				
	}

	@Override
	public void start() throws LigretoException {
		super.start();
		for (int i=0; i < on1.length; i++) {
			noResultColumns1.put(on1[i], null);
		}
		for (int i=0; i < on2.length; i++) {
			noResultColumns2.put(on2[i], null);
		}

		// Do some sanity checks
		int rs1Length = dp1.getColumnCount();
		int rs2Length = dp2.getColumnCount();

		int resultCount1 = rs1Length - noResultColumns1.size();
		int resultCount2 = rs2Length - noResultColumns2.size();
		
		if (resultCount1 != resultCount2) {
			throw new LigretoException(
				"The column count in analysis differs; 1st count: "
				+ resultCount1 + "; 2nd count: " + resultCount2
			);
		}
		resultCount = resultCount1;
		
		resultColumns1 = new int[resultCount];
		resultColumns2 = new int[resultCount];
		
		// Store the information about the result column's indices
		for (int i=0, i1=1, i2=1; i < resultCount; i++, i1++, i2++) {
			while (noResultColumns1.containsKey(i1))
				i1++;
			while (noResultColumns2.containsKey(i2))
				i2++;
			resultColumns1[i] = i1;
			resultColumns2[i] = i2;
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws DataException, LigretoException, IOException {

		AnalysisEntry entry = null;
		
		switch (resultType) {
		case INNER:
			entry = new AnalysisEntry(dp1, resultColumns1, dp2, resultColumns2);
			break;
		case LEFT:
			entry = new AnalysisEntry(dp1, resultColumns1, null, resultColumns2);
			break;
		case RIGHT:
			entry = new AnalysisEntry(null, resultColumns1, dp2, resultColumns2);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
		entry.rowDiffs = rowDiffs;
		
		// We have all the columns processed, so we will either merge the new results with
		// the previous one or we will store a new result into the aggregation map.
		Integer previousCount = analysisMap.get(entry);
		if (previousCount != null) {
			entry.count = previousCount + 1;
			analysisMap.remove(entry);
			analysisMap.put(entry, entry.count);
		} else {
			analysisMap.put(entry, 1);
		}
	}

	@Override
	public ResultStatus finish() throws IOException, DataException, LigretoException {
		// We will sort the result according the number of occurrences
		AnalysisEntry[] result = new AnalysisEntry[analysisMap.size()];
		int ai=0;
		for (AnalysisEntry ae : analysisMap.keySet()) {
			result[ai] = ae;
			ai++;
		}
		Arrays.sort(result);
		
		// Now we will dump the sorted data
		for (int r=0; r < result.length; r++) {
			AnalysisEntry ae = result[r];
			targetBuilder.nextRow();
			targetBuilder.dumpColumn(0, ae.count, CellFormat.UNCHANGED, ae.rowDiffs > 0);
			targetBuilder.setColumnPosition(1, 2, null);
			for (int i=0; i < ae.cols1.length; i++) {
				targetBuilder.dumpColumn(i, ae.cols1[i] != null ? ae.cols1[i].getColumnValue() : null, CellFormat.UNCHANGED, ae.rowDiffs > 0);
			}
			targetBuilder.setColumnPosition(2, 2, null);
			for (int i=0; i < ae.cols2.length; i++) {
				targetBuilder.dumpColumn(i, ae.cols2[i] != null ? ae.cols2[i].getColumnValue() : null, CellFormat.UNCHANGED, ae.rowDiffs > 0);
			}
		}
		return super.finish();
	}
}
