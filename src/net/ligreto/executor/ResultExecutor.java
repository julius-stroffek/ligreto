package net.ligreto.executor;

import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.layouts.JoinLayout;
import net.ligreto.parser.nodes.ResultNode;

public class ResultExecutor extends Executor {

	protected ResultNode resultNode;
	protected JoinLayout joinLayout;
	
	public ResultExecutor(ResultNode resultNode, JoinLayout joinLayout) {
		this.resultNode = resultNode;
		this.joinLayout = joinLayout;
	}

	@Override
	public ResultStatus execute() throws LigretoException {
		ResultStatus resultStatus = new ResultStatus();
		resultStatus.setDifferentRowCount(joinLayout.getDifferentRowCount());
		resultStatus.setTotalRowCount(joinLayout.getTotalRowCount());
		return resultStatus;
	}

}
