package net.ligreto.executor.layouts;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.AggregationResult;
import net.ligreto.data.ColumnAggregationResult;
import net.ligreto.data.Column;
import net.ligreto.data.Row;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.LigretoComparator;

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
public class AggregatedJoinLayout extends JoinLayout {

	protected HashMap<Row, AggregationResult> aggregationMap = new HashMap<Row, AggregationResult>();
	protected HashMap<Integer, Void> noResultColumns = new HashMap<Integer, Void>();

	public AggregatedJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Column Name", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1);
		if (groupByColumns != null && groupByColumns.length > 0) {
			for (int i = 0; i < groupByColumns.length; i++) {
				targetBuilder.dumpCell(i, getColumnName(groupByColumns[i]), OutputStyle.TOP_HEADER);
			}
			targetBuilder.shiftPosition(groupByColumns.length);
		}

		targetBuilder.dumpCell(0, "# of Diffs", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(1, "Ratio of Diffs", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(2, "Relative Difference", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(3, "Difference", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(4, "# of Rows", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(5, "Total Value", OutputStyle.TOP_HEADER);
	}

	@Override
	public void start() throws LigretoException {
		super.start();
		for (int i=0; i < keyColumns.length; i++) {
			noResultColumns.put(keyColumns[i], null);
		}
		if (groupByColumns != null) {
			for (int i=0; i < groupByColumns.length; i++) {
				if (!noResultColumns.containsKey(groupByColumns[i])) {
					throw new LigretoException(
						"Columns listed in group-by have to be also listed in 'key' columns; column: "
						+ groupByColumns[i] + "; data sources: "
						+ dp1.getCaption()
						+ ", "
						+ dp2.getCaption()
					);
				}
			}
		}
	}

	protected ColumnAggregationResult processColumn(JoinResultType resultType, int index) throws DataException {
		Column columnValue1, columnValue2;
		ColumnAggregationResult colResult = null;
		switch (resultType) {
		case LEFT:
			columnValue1 = new Column(dp1, index);
			colResult = new ColumnAggregationResult(columnValue1, null);
			break;
		case RIGHT:
			columnValue2 = new Column(dp2, index);
			colResult = new ColumnAggregationResult(null, columnValue2);
			break;
		case INNER:
			columnValue1 = new Column(dp1, index);
			columnValue2 = new Column(dp2, index);
			colResult = new ColumnAggregationResult(columnValue1, columnValue2);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
		return colResult;
	}
	
	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws DataException, LigretoException, IOException {

		// Get the value of group by columns first
		Row row = new Row();
		AggregationResult result = new AggregationResult(comparedColumns.length + ignoredColumns.length);
		switch (resultType) {
		case INNER:
		case LEFT:
			row.setFields(LigretoComparator.duplicate(dp1, groupByColumns));
			break;
		case RIGHT:
			row.setFields(LigretoComparator.duplicate(dp2, groupByColumns));
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}

		// Loop through all the columns to be in the result
		for (int i = 0; i < comparedColumns.length; i++) {
			ColumnAggregationResult colResult = processColumn(resultType, comparedColumns[i]);
			/*
			if (cmpArray[i] == 0) {
				colResult.setDifference(0);
				colResult.setDifferenceCount(0);
				colResult.setDifferenceRatio(0);
			}
			*/
			result.setColumnResult(i, colResult);
		}
		for (int i = 0; i < ignoredColumns.length; i++) {
			ColumnAggregationResult colResult = processColumn(resultType, ignoredColumns[i]);
			result.setColumnResult(comparedColumns.length + i, colResult);
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
	public ResultStatus finish() throws IOException, DataException, LigretoException {
		// We will sort the result according group-by columns by creating a tree set
		TreeSet<Row> treeSet = new TreeSet<Row>(aggregationMap.keySet());
		for (Row f : treeSet) {
			AggregationResult result = aggregationMap.get(f);
			for (int i=0; i < result.getColumnCount(); i++) {

				OutputStyle style;
				targetBuilder.nextRow();
				if (i >= comparedColumns.length) {
					targetBuilder.dumpCell(0, getColumnName(ignoredColumns[i - comparedColumns.length]), OutputStyle.ROW_HEADER_DISABLED);
					style = OutputStyle.DISABLED;
				} else {
					targetBuilder.dumpCell(0, getColumnName(comparedColumns[i]), OutputStyle.ROW_HEADER);
					style = OutputStyle.DEFAULT;
				}
				targetBuilder.shiftPosition(1);
				for (int j=0; j < f.getFields().length; j++) {
					targetBuilder.dumpCell(j, f.getFields()[j].getColumnValue(), OutputFormat.DEFAULT, style);
				}
				targetBuilder.shiftPosition(f.getFields().length);
				
				ColumnAggregationResult cResult = result.getColumnResult(i);
				targetBuilder.dumpCell(0, cResult.getDifferenceCount(), OutputFormat.DEFAULT, style);
				targetBuilder.dumpCell(1, cResult.getDifferenceRatio(), OutputFormat.PERCENTAGE_3_DECIMAL_DIGITS, style);

				// Dump the difference metrics if we have numeric column
				if (cResult.isNumeric()) {
					targetBuilder.dumpCell(2, cResult.getRelativeDifference(), OutputFormat.PERCENTAGE_3_DECIMAL_DIGITS, style);
					targetBuilder.dumpCell(3, cResult.getDifference(), OutputFormat.DEFAULT, style);
					targetBuilder.dumpCell(5, cResult.getTotalValue(), OutputFormat.DEFAULT, style);
				}
				
				// Dump the other column values
				targetBuilder.dumpCell(4, cResult.getRowCount(), OutputFormat.DEFAULT, style);
			}
		}
		return super.finish();
	}
}
