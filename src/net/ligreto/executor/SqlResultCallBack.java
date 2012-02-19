package net.ligreto.executor;

import net.ligreto.data.DataProvider;
import net.ligreto.parser.nodes.SqlNode;

public interface SqlResultCallBack {
	public boolean prepareProcessing(SqlNode sqlNode, DataProvider dp) throws Exception;
	public void processResultSetRow(DataProvider dp) throws Exception;
	public void finalizeProcessing() throws Exception;
}
