package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataSourceNotDefinedException;

public class ResultLayout extends AggregatedLayout {

	public ResultLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
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
}