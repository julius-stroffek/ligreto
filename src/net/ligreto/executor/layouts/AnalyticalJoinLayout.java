package net.ligreto.executor.layouts;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.Field;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.LigretoException;
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
		Field[] cols1 = null;
		Field[] cols2 = null;
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
			assert(columns1.length == columns2.length);
			cols1 = null;
			if (dp1 != null && columns1 != null && columns1.length > 0) {
				cols1 = LigretoComparator.duplicate(dp1, columns1);
			} else {
				cols1 = new Field[columns1.length];
				for (int i=0; i < columns1.length; i++) {
					cols1[i] = null;
				}
			}
			if (dp2 != null && columns2 != null && columns2.length > 0) {
				cols2 = LigretoComparator.duplicate(dp2, columns2);
			} else {
				cols2 = new Field[columns2.length];
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
			int result = o.count.compareTo(count);
			if (result == 0) {
				assert(cols1.length == cols2.length);
				assert(cols1.length == o.cols1.length);
				assert(o.cols1.length == o.cols2.length);
				for (int i=0; i < cols1.length; i++) {
					if (cols1[i] != null || o.cols1[i] != null) {
						if (cols1[i] != null) {
							result = cols1[i].compareTo(o.cols1[i]);
						} else {
							result = -o.cols1[i].compareTo(cols1[i]);
						}
					}
					if (result != 0) {
						return result;
					}
					if (cols2[i] != null || o.cols2[i] != null) {
						if (cols2[i] != null) {
							result = cols2[i].compareTo(o.cols2[i]);
						} else {
							result = -o.cols2[i].compareTo(cols2[i]);
						}
					}
					if (result != 0) {
						return result;
					}
				}
			} else {
				return result;
			}
			throw new RuntimeException("Unexpected part of the code was reached.");
		}		
	}
	
	protected Map<AnalysisEntry, Integer> analysisMap = new HashMap<AnalysisEntry, Integer>(4096);

	public AnalyticalJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "# of Occur.", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1, 2);
		for (int i = 0; i < resultColumns.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(resultColumns[i]) + " (" + dp1.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(1);
		for (int i = 0; i < resultColumns.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(resultColumns[i]) + " (" + dp2.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, boolean[] cmpArray, JoinResultType resultType) throws DataException, LigretoException, IOException {

		AnalysisEntry entry = null;
		
		switch (resultType) {
		case INNER:
			entry = new AnalysisEntry(dp1, resultColumns, dp2, resultColumns);
			break;
		case LEFT:
			entry = new AnalysisEntry(dp1, resultColumns, null, resultColumns);
			break;
		case RIGHT:
			entry = new AnalysisEntry(null, resultColumns, dp2, resultColumns);
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
			OutputStyle style = ae.rowDiffs > 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT;
			targetBuilder.dumpCell(0, ae.count, OutputFormat.DEFAULT, style);
			targetBuilder.shiftPosition(1, 2);
			for (int i=0; i < ae.cols1.length; i++) {
				targetBuilder.dumpCell(i, ae.cols1[i] != null ? ae.cols1[i].getColumnValue() : null, OutputFormat.DEFAULT, style);
			}
			targetBuilder.shiftPosition(1, 2);
			for (int i=0; i < ae.cols2.length; i++) {
				targetBuilder.dumpCell(i, ae.cols2[i] != null ? ae.cols2[i].getColumnValue() : null, OutputFormat.DEFAULT, style);
			}
		}
		return super.finish();
	}
}
