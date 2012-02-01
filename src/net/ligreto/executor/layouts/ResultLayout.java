package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.layouts.JoinLayout.JoinResultType;

public class ResultLayout extends AggregatedLayout {

	public ResultLayout(LigretoParameters ligretoParameters) {
		super(null, ligretoParameters);
	}

	/**
	 * This re-implementation will make sure that group-by will be always empty in result layout.
	 */
	@Override
	public void setGroupByColumns(int[] unusedGroupBy) {
		groupBy = null;
		groupByLength = 0;
	}

	/**
	 * This re-implementation will make sure that targetBuilder will not be used.
	 */
	@Override
	public void dumpHeader() throws SQLException, DataSourceNotDefinedException, IOException {
	}
	
	/**
	 * This re-implementation will make sure that targetBuilder will not be used.
	 */
	@Override
	public void finish() throws IOException, SQLException {
	}
	
	/**
	 * @return The result status object with the aggregated result.
	 */
	public ResultStatus getResultStatus() {
		ResultStatus resultStatus = new ResultStatus();
		return resultStatus;
	}
	
	public boolean processRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException, IOException {
		super.processRow(rowDiffs, highlightArray, resultType);
		return false;
	}
}
