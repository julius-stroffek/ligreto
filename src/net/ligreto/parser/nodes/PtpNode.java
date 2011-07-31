package net.ligreto.parser.nodes;

public class PtpNode extends Node {

	protected String name;
	protected PreprocessNode preprocessNode;
	protected TransferNode transferNode;
	protected PostprocessNode postprocessNode;
	
	public PtpNode(LigretoNode aLigretoNode) {
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

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
