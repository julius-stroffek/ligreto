package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
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
		targetBuilder.dumpCell(0, "Summary of Rows", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(1, "Value", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(2, "Relative", OutputStyle.TOP_HEADER);
		if (layoutNode.getResultNode() != null) {
			targetBuilder.dumpCell(3, "Limit [%]", OutputStyle.TOP_HEADER);
			targetBuilder.dumpCell(4, "Limit [rows]", OutputStyle.TOP_HEADER);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws LigretoException, IOException {
		// We do nothing here
	}

	@Override
	public ResultStatus finish() throws IOException, LigretoException {
		ResultNode resultNode = layoutNode.getResultNode();
		RowLimitNode rowLimitNode = resultNode != null ? resultNode.getRowLimitNode() : null;
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Total Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, totalRowCount, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, totalRowCount/(double)totalRowCount, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Equal Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, equalRowCount, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, equalRowCount/(double)totalRowCount, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);

		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Different Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, differentRowCount, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, differentRowCount/(double)totalRowCount, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		if (rowLimitNode != null) {
			if (rowLimitNode.getRelativeDifference() != null) {
				targetBuilder.dumpCell(2, rowLimitNode.getRelativeDifference(), OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
			}
			if (rowLimitNode.getAbsoluteDifference() != null) {
				targetBuilder.dumpCell(3, rowLimitNode.getAbsoluteDifference(), OutputFormat.DEFAULT);
			}
		}
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Matching Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, matchingRowCount, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, matchingRowCount/(double)totalRowCount, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Non-matching Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, totalRowCount - matchingRowCount, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, (totalRowCount - matchingRowCount)/(double)totalRowCount, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		if (rowLimitNode != null) {
			if (rowLimitNode.getRelativeNonMatched() != null) {
				targetBuilder.dumpCell(2, rowLimitNode.getRelativeNonMatched(), OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
			}
			if (rowLimitNode.getAbsoluteNonMatched() != null) {
				targetBuilder.dumpCell(3, rowLimitNode.getAbsoluteNonMatched(), OutputFormat.DEFAULT);
			}
		}
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "(" + dp1.getCaption() +") - Total Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, rowCountSrc1, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, rowCountSrc1/(double)totalRowCount, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "(" + dp1.getCaption() +") - Matching Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, rowCountSrc1 - nonMatchingRowsSrc1, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, (rowCountSrc1 - nonMatchingRowsSrc1)/(double)rowCountSrc1, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "(" + dp1.getCaption() +") - Non-matching Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, nonMatchingRowsSrc1, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, nonMatchingRowsSrc1/(double)rowCountSrc1, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "(" + dp2.getCaption() +") - Total Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, rowCountSrc2, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, rowCountSrc2/(double)totalRowCount, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "(" + dp2.getCaption() +") - Matching Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, rowCountSrc2 - nonMatchingRowsSrc2, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, (rowCountSrc2 - nonMatchingRowsSrc2)/(double)rowCountSrc2, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "(" + dp2.getCaption() +") - Non-matching Rows", OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);
		targetBuilder.dumpCell(0, nonMatchingRowsSrc2, OutputFormat.DEFAULT);
		targetBuilder.dumpCell(1, nonMatchingRowsSrc2/(double)rowCountSrc2, OutputFormat.PERCENTAGE_2_DECIMAL_DIGITS);
		
		return super.finish();
	}
}
