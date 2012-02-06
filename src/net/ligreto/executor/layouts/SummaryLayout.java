package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;

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
public class SummaryLayout extends JoinLayout {

	public SummaryLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, DataSourceNotDefinedException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Summary of Rows", HeaderType.TOP);
		targetBuilder.dumpHeaderColumn(1, "Value", HeaderType.TOP);
	}

	@Override
	public void dumpRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException, IOException {
		// We do nothing here
	}

	@Override
	public void finish() throws IOException, SQLException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Equal Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, equalRowCount, CellFormat.UNCHANGED);

		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, matchingRowCount, CellFormat.UNCHANGED);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Different Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, differentRowCount, CellFormat.UNCHANGED);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Total Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, totalRowCount, CellFormat.UNCHANGED);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc1 +") - Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, rowCountSrc1, CellFormat.UNCHANGED);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc1 +") - Non-matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, nonMatchingRowsSrc1, CellFormat.UNCHANGED);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc2 +") - Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, rowCountSrc2, CellFormat.UNCHANGED);
		
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "(" + dataSourceDesc2 +") - Non-matching Rows", HeaderType.ROW);
		targetBuilder.setColumnPosition(1);
		targetBuilder.dumpColumn(0, nonMatchingRowsSrc2, CellFormat.UNCHANGED);
		
		super.finish();
	}
}
