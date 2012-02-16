package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TreeSet;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.AggregationResult;
import net.ligreto.data.ColumnAggregationResult;
import net.ligreto.data.Column;
import net.ligreto.data.Row;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.LigretoComparator;
import net.ligreto.util.MiscUtils;

/**
 * The layout doing group by aggregation on the calculated comparison results. The output
 * is similar to the output of {@code DetailedJoinLayout} but the results might be aggregated
 * based on the different columns. If the specified 'group-by' columns are the same
 * as join 'on' columns then the result should be the same as for {@code DetailedJoinLayout}
 * (except field values are missing in aggregated layout).
 * 
 * <p>
 * The results to be presented by have to fit aggregated into java heap memory. This is not the case
 * for {@code DetailedJoinLayout} as it produces the output sequentially as the input data
 * are provided.
 * </p>
 * 
 * @author Julius Stroffek
 *
 */
public class AggregatedLayout extends JoinLayout {

	protected HashMap<Row, AggregationResult> aggregationMap = new HashMap<Row, AggregationResult>();
	protected HashMap<Integer, Void> noResultColumns1 = new HashMap<Integer, Void>();
	protected HashMap<Integer, Void> noResultColumns2 = new HashMap<Integer, Void>();
	int[] resultColumns1 = null;
	int[] resultColumns2 = null;
	int resultCount = 0;

	public AggregatedLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, DataSourceNotDefinedException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Column Name", HeaderType.TOP);
		targetBuilder.setColumnPosition(1, 1, null);
		if (groupByLength > 0)
			targetBuilder.dumpJoinOnHeader(rs1, groupBy, null);
		targetBuilder.setColumnPosition(groupByLength + 1, 1, null);

		targetBuilder.dumpHeaderColumn(0, "# of Diffs", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(1, "Ratio of Diffs", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(2, "Relative Difference", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(3, "Difference", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(4, "# of Rows", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(5, "Total Value", HeaderType.TOP);
	}

	@Override
	public void start() throws SQLException, LigretoException {
		super.start();
		for (int i=0; i < on1.length; i++) {
			noResultColumns1.put(on1[i], null);
		}
		for (int i=0; i < excl1.length; i++) {
			noResultColumns1.put(excl1[i], null);
		}
		for (int i=0; i < on2.length; i++) {
			noResultColumns2.put(on2[i], null);
		}
		for (int i=0; i < excl2.length; i++) {
			noResultColumns2.put(excl2[i], null);
		}
		if (groupBy != null) {
			for (int i=0; i < groupBy.length; i++) {
				noResultColumns1.put(groupBy[i], null);
				noResultColumns2.put(groupBy[i], null);
				if (!MiscUtils.arrayContains(on1, groupBy[i])) {
					throw new LigretoException(
						"Columns listed in group-by have to be also listed in 'on' columns in join; column: "
						+ groupBy[i] + "; data source: "
						+ dataSourceDesc1
					);
				}
				if (!MiscUtils.arrayContains(on2, groupBy[i])) {
					throw new LigretoException(
						"Columns listed in 'group-by' have to be also listed in 'on' columns in join; column: "
						+ groupBy[i] + "; data source: "
						+ dataSourceDesc2
					);
				}
			}
		}
		// Do some sanity checks
		int rs1Length = rs1.getMetaData().getColumnCount();
		int rs2Length = rs2.getMetaData().getColumnCount();

		int resultCount1 = rs1Length - noResultColumns1.size();
		int resultCount2 = rs2Length - noResultColumns2.size();
		
		if (resultCount1 != resultCount2) {
			throw new LigretoException(
				"The column count in aggregation differs; 1st count: "
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
	public void dumpRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException, IOException {

		// Get the value of group by columns first
		Row row = new Row();
		AggregationResult result = new AggregationResult(resultCount);
		switch (resultType) {
		case INNER:
		case LEFT:
			row.setFields(LigretoComparator.duplicate(rs1, groupBy));
			break;
		case RIGHT:
			row.setFields(LigretoComparator.duplicate(rs2, groupBy));
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}

		// Loop through all the columns to be in the result
		for (int i = 0; i < resultCount; i++) {
			
			// Get the indices of result columns into the result sets
			int i1 = resultColumns1[i];
			int i2 = resultColumns2[i];
			
			Column columnValue1, columnValue2;
			switch (resultType) {
			case LEFT:
				columnValue1 = new Column(rs1, i1);
				result.setColumnResult(i, new ColumnAggregationResult(columnValue1, null));
				break;
			case RIGHT:
				columnValue2 = new Column(rs2, i2);
				result.setColumnResult(i, new ColumnAggregationResult(null, columnValue2));
				break;
			case INNER:
				columnValue1 = new Column(rs1, i1);
				columnValue2 = new Column(rs2, i2);
				result.setColumnResult(i, new ColumnAggregationResult(columnValue1, columnValue2));
				break;
			default:
				throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
			}
		}
		
		// We have all the columns processed, so we will either merge the new results with
		// the previous one or we will store a new result into the aggregation map.
		AggregationResult previousResult = aggregationMap.get(row);
		if (previousResult != null) {
			previousResult.merge(result);
		} else {
			aggregationMap.put(row, result);
		}
	}

	@Override
	public ResultStatus finish() throws IOException, SQLException, LigretoException {
		TreeSet<Row> treeSet = new TreeSet<Row>(aggregationMap.keySet());
		for (Row f : treeSet) {
			AggregationResult result = aggregationMap.get(f);
			for (int i=0; i < result.getColumnCount(); i++) {
				ColumnAggregationResult cResult = result.getColumnResult(i);
				targetBuilder.nextRow();
				targetBuilder.dumpHeaderColumn(0, getResultColumnName(i), HeaderType.ROW);
				targetBuilder.setColumnPosition(1);
				for (int j=0; j < f.getFields().length; j++) {
					targetBuilder.dumpColumn(j, f.getFields()[j].getColumnValue(), CellFormat.UNCHANGED);
				}
				targetBuilder.setColumnPosition(1 + f.getFields().length);
				targetBuilder.dumpColumn(0, cResult.getDifferenceCount(), CellFormat.UNCHANGED);
				targetBuilder.dumpColumn(1, cResult.getDifferenceRatio(), CellFormat.PERCENTAGE_3_DECIMAL_DIGITS);

				// Dump the difference metrics if we have numeric column
				if (cResult.isNumeric()) {
					targetBuilder.dumpColumn(2,
							Double.isNaN(cResult.getRelativeDifference()) ? ligretoParameters.getNanString() : cResult.getRelativeDifference(),
									CellFormat.PERCENTAGE_3_DECIMAL_DIGITS
					);
					targetBuilder.dumpColumn(3, cResult.getDifference(), CellFormat.UNCHANGED);
					targetBuilder.dumpColumn(5, cResult.getTotalValue(), CellFormat.UNCHANGED);
				}
				
				// Dump the other column values
				targetBuilder.dumpColumn(4, cResult.getRowCount(), CellFormat.UNCHANGED);
			}
		}
		return super.finish();
	}
}
