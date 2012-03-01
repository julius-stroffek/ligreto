package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.ResultNode;
import net.ligreto.parser.nodes.RowLimitNode;

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
public class SummaryJoinLayout extends JoinLayout {

	public SummaryJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws DataException, DataSourceNotDefinedException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Summary of Rows", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(1, "Value", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(2, "Relative", HeaderType.TOP);
		if (layoutNode.getResultNode() != null) {
			targetBuilder.dumpHeaderColumn(3, "Limit [%]", HeaderType.TOP);
			targetBuilder.dumpHeaderColumn(4, "Limit [rows]", HeaderType.TOP);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException, IOException {
		// We do nothing here
	}

	@Override
	public ResultStatus finish() throws IOException, LigretoException {
		ResultNode resultNode = layoutNode.getResultNode();
		RowLimitNode rowLimitNode = resultNode != null ? resultNode.getRowLimitNode() : null;
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Total Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, totalRowCount, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, totalRowCount/(double)totalRowCount, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Equal Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, equalRowCount, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, equalRowCount/(double)totalRowCount, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);

		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Different Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, differentRowCount, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, differentRowCount/(double)totalRowCount, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		if (rowLimitNode != null) {
			if (rowLimitNode.getRelativeDifference() != null) {
				targetBuilder.dumpColumn(2, rowLimitNode.getRelativeDifference(), CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
			}
			if (rowLimitNode.getAbsoluteDifference() != null) {
				targetBuilder.dumpColumn(3, rowLimitNode.getAbsoluteDifference(), CellFormat.UNCHANGED);
			}
		}
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, matchingRowCount, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, matchingRowCount/(double)totalRowCount, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Non-matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, totalRowCount - matchingRowCount, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, (totalRowCount - matchingRowCount)/(double)totalRowCount, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		if (rowLimitNode != null) {
			if (rowLimitNode.getRelativeNonMatched() != null) {
				targetBuilder.dumpColumn(2, rowLimitNode.getRelativeNonMatched(), CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
			}
			if (rowLimitNode.getAbsoluteNonMatched() != null) {
				targetBuilder.dumpColumn(3, rowLimitNode.getAbsoluteNonMatched(), CellFormat.UNCHANGED);
			}
		}
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc1 +") - Total Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, rowCountSrc1, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, rowCountSrc1/(double)totalRowCount, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc1 +") - Matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, rowCountSrc1 - nonMatchingRowsSrc1, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, (rowCountSrc1 - nonMatchingRowsSrc1)/(double)rowCountSrc1, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc1 +") - Non-matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, nonMatchingRowsSrc1, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, nonMatchingRowsSrc1/(double)rowCountSrc1, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc2 +") - Total Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, rowCountSrc2, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, rowCountSrc2/(double)totalRowCount, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc2 +") - Matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, rowCountSrc2 - nonMatchingRowsSrc2, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, (rowCountSrc2 - nonMatchingRowsSrc2)/(double)rowCountSrc2, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc2 +") - Non-matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, nonMatchingRowsSrc2, CellFormat.UNCHANGED);
		targetBuilder.dumpColumn(1, nonMatchingRowsSrc2/(double)rowCountSrc2, CellFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		return super.finish();
	}
}
