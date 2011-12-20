package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TreeSet;

import net.ligreto.Database;
import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.AggregationResult;
import net.ligreto.util.ColumnAggregationResult;
import net.ligreto.util.Fields;
import net.ligreto.util.MiscUtils;
import net.ligreto.util.ResultSetComparator;

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

	protected HashMap<Fields, AggregationResult> aggregationMap = new HashMap<Fields, AggregationResult>();
	protected HashMap<Integer, Void> noResultColumns1 = new HashMap<Integer, Void>();
	protected HashMap<Integer, Void> noResultColumns2 = new HashMap<Integer, Void>();
	int[] resultColumns1 = null;
	int[] resultColumns2 = null;
	int resultCount = 0;

	public AggregatedLayout(BuilderInterface reportBuilder, LigretoParameters ligretoParameters) {
		super(reportBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, DataSourceNotDefinedException, IOException {
		reportBuilder.nextRow();
		reportBuilder.setHeaderColumn(0, "Field Name", HeaderType.TOP);
		reportBuilder.setColumnPosition(1, 1, null);
		reportBuilder.dumpJoinOnHeader(rs1, groupBy);
		reportBuilder.setColumnPosition(groupByLength + 1, 1, null);

		reportBuilder.setHeaderColumn(0, "# of Diffs", HeaderType.TOP);
		reportBuilder.setHeaderColumn(1, "Difference Ratio", HeaderType.TOP);
		reportBuilder.setHeaderColumn(2, "Relative Difference", HeaderType.TOP);
		reportBuilder.setHeaderColumn(3, "Difference", HeaderType.TOP);
		reportBuilder.setHeaderColumn(4, "Row Count", HeaderType.TOP);
		reportBuilder.setHeaderColumn(5, "Value", HeaderType.TOP);
	}

	@Override
	public void start() throws SQLException, LigretoException {
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
					String dSrc0 =  joinNode.getSqlQueries().get(0).getDataSource();
					throw new LigretoException(
						"Columns listed in group-by have to be also listed in 'on' columns in join; column: "
						+ groupBy[i] + "; data source: "
						+ Database.getInstance().getDataSourceNode(dSrc0).getDescription()
					);
				}
				if (!MiscUtils.arrayContains(on2, groupBy[i])) {
					String dSrc1 =  joinNode.getSqlQueries().get(1).getDataSource();		
					throw new LigretoException(
						"Columns listed in 'group-by' have to be also listed in 'on' columns in join; column: "
						+ groupBy[i] + "; data source: "
						+ Database.getInstance().getDataSourceNode(dSrc1).getDescription()
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
	public void dumpRow(int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException, IOException {
		int rs1Length = rs1.getMetaData().getColumnCount();
		int rs2Length = rs2.getMetaData().getColumnCount();
		
		// Get the value of group by columns first
		Fields fields = new Fields();
		AggregationResult result = new AggregationResult(resultCount);
		switch (resultType) {
		case INNER:
		case LEFT:
			fields.setFields(ResultSetComparator.duplicate(rs1, groupBy));
			break;
		case RIGHT:
			fields.setFields(ResultSetComparator.duplicate(rs2, groupBy));
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}

		// Loop trough all the columns in the result sets.
		for (int i = 0, i1 = 0, i2 = 0; i1 < rs1Length && i2 < rs2Length; i++, i1++, i2++) {
			// Find the next column in the first result set that
			// is not part of 'on', 'exclude' nor 'group by' column list
			boolean col1Found = false;
			while (i1 < rs1Length) {
				if (noResultColumns1.containsKey(i1+1)) {
					i1++;
				} else {
					col1Found = true;
					break;
				}
			}
			// Find the next column in the second result set that
			// is not part of 'on', 'exclude' nor 'group by' column list
			boolean col2Found = false;
			while (i2 < rs1Length) {
				if (noResultColumns2.containsKey(i2+1)) {
					i2++;
				} else {
					col2Found = true;
					break;
				}
			}
			if (col1Found && col2Found) {
				Object columnValue1, columnValue2;
				switch (resultType) {
				case LEFT:
					columnValue1 = rs1.getObject(i1);
					result.setColumnResult(i, new ColumnAggregationResult(columnValue1, null));
					break;
				case RIGHT:
					columnValue2 = rs2.getObject(i2);
					result.setColumnResult(i, new ColumnAggregationResult(null, columnValue2));
					break;
				case INNER:
					columnValue1 = rs1.getObject(i1);
					columnValue2 = rs2.getObject(i2);
					result.setColumnResult(i, new ColumnAggregationResult(columnValue1, columnValue2));
					break;
				default:
					throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
				}
			} else if (col1Found || col2Found) {
				throw new RuntimeException("Internal inconsistency found.");
			}
		}
		
		// We have all the columns processed, so we will either merge the new results with
		// the previous one or we will store a new result into the aggregation map.
		AggregationResult previousResult = aggregationMap.get(fields);
		if (previousResult != null) {
			previousResult.merge(result);
		} else {
			aggregationMap.put(fields, result);
		}
	}

	@Override
	public void finish() throws IOException, SQLException {
		TreeSet<Fields> treeSet = new TreeSet<Fields>(aggregationMap.keySet());
		for (Fields f : treeSet) {
			AggregationResult result = aggregationMap.get(f);
			for (int i=0; i < result.getColumnCount(); i++) {
				ColumnAggregationResult cResult = result.getColumnResult(i);
				String colName = rs1.getMetaData().getColumnName(resultColumns1[i]);
				String col2Name = rs1.getMetaData().getColumnName(resultColumns2[i]);
				if (! colName.equalsIgnoreCase(col2Name)) {
					colName = colName + " / " + col2Name;
				}
				reportBuilder.nextRow();
				reportBuilder.setHeaderColumn(0, colName, HeaderType.ROW);
				reportBuilder.setColumnPosition(1);
				for (int j=0; j < f.getFields().length; j++) {
					reportBuilder.setColumn(j, f.getFields()[j].getColumnValue(), CellFormat.UNCHANGED);
				}
				reportBuilder.setColumnPosition(1 + f.getFields().length);
				reportBuilder.setColumn(0, cResult.getDifferenceCount(), CellFormat.UNCHANGED);
				reportBuilder.setColumn(1, cResult.getDifferenceRatio(), CellFormat.PERCENTAGE_3_DECIMAL_DIGITS);
				reportBuilder.setColumn(2,
					cResult.getRelativeDifference() >= 0 ? cResult.getRelativeDifference() : "",
					CellFormat.PERCENTAGE_3_DECIMAL_DIGITS
				);
				reportBuilder.setColumn(3, cResult.getDifference(), CellFormat.UNCHANGED);
				reportBuilder.setColumn(4, cResult.getRowCount(), CellFormat.UNCHANGED);
				reportBuilder.setColumn(5, cResult.getTotalValue(), CellFormat.UNCHANGED);
			}
		}
	}
}
