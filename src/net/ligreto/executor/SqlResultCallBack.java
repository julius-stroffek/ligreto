package net.ligreto.executor;

import java.sql.ResultSet;

import net.ligreto.parser.nodes.SqlNode;

public interface SqlResultCallBack {
	public boolean prepareProcessing(SqlNode sqlNode, ResultSet rs) throws Exception;
	public void processResultSetRow(ResultSet rs) throws Exception;
	public void finalizeProcessing() throws Exception;
}
