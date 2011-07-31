package net.ligreto.executor;

import java.sql.ResultSet;

import net.ligreto.parser.nodes.JoinNode;

public interface JoinResultCallBack {
	public boolean prepareProcessing(JoinNode joinNode, ResultSet rs1, ResultSet rs2) throws Exception;
	public void processLeftResult(JoinNode joinNode, ResultSet rs1) throws Exception;
	public void processRightResult(JoinNode joinNode, ResultSet rs2) throws Exception;
	public void processJoinResult(JoinNode joinNode, ResultSet rs1, ResultSet rs2) throws Exception;
}
