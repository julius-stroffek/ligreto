package net.ligreto.executor;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.PtpNode;

public class PtpExecutor extends Executor {

	/** Iterable object holding the PTP nodes to be processed. */ 
	Iterable<PtpNode> ptpNodes;
	
	@Override
	public void execute() throws LigretoException {
		for(PtpNode ptpNode: ptpNodes) {
			executePTP(ptpNode);
		}
	}

	private void executePTP(PtpNode ptpNode) throws LigretoException {
		try {
			// Do pre-processing
			SqlExecutor sqlExecutor = new SqlExecutor();
			if (ptpNode.getPreprocessNode() != null) {
				sqlExecutor.setSqlNodes(ptpNode.getPreprocessNode().sqlQueries());
				sqlExecutor.execute();
			}
			
			// Do the transfer of data
			// TODO Not yet implemented
			
			// Do post-processing
			if (ptpNode.getPostprocessNode() != null) {
				sqlExecutor.setSqlNodes(ptpNode.getPostprocessNode().sqlQueries());
				sqlExecutor.execute();
			}
		} catch (Exception e) {
			throw new LigretoException("Error processing the PTP transfer: " + ptpNode.getName(), e);
		}
		
	}

	/**
	 * @return the ptpNodes
	 */
	public Iterable<PtpNode> getPtpNodes() {
		return ptpNodes;
	}

	/**
	 * @param ptpNodes the ptpNodes to set
	 */
	public void setPtpNodes(Iterable<PtpNode> ptpNodes) {
		this.ptpNodes = ptpNodes;
	}

}
