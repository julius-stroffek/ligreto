package net.ligreto.config.nodes;

public class PTPNode extends Node {

	protected PreprocessNode preprocessNode;
	protected TransferNode transferNode;
	protected PostprocessNode postprocessNode;
	
	public PTPNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	/**
	 * @return the preprocessNode
	 */
	public PreprocessNode getPreprocessNode() {
		return preprocessNode;
	}

	/**
	 * @param preprocessNode the preprocessNode to set
	 */
	public void setPreprocessNode(PreprocessNode preprocessNode) {
		this.preprocessNode = preprocessNode;
	}

	/**
	 * @return the transferNode
	 */
	public TransferNode getTransferNode() {
		return transferNode;
	}

	/**
	 * @param transferNode the transferNode to set
	 */
	public void setTransferNode(TransferNode transferNode) {
		this.transferNode = transferNode;
	}

	/**
	 * @return the postprocessNode
	 */
	public PostprocessNode getPostprocessNode() {
		return postprocessNode;
	}

	/**
	 * @param postprocessNode the postprocessNode to set
	 */
	public void setPostprocessNode(PostprocessNode postprocessNode) {
		this.postprocessNode = postprocessNode;
	}

}
