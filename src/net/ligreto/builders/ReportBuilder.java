package net.ligreto.builders;

import java.io.File;

import net.ligreto.LigretoParameters;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.ReportNode;

/**
 * The {@link BuilderInterface} generic implementation.
 * 
 * @author Julius Stroffek
 *
 */
public abstract class ReportBuilder implements BuilderInterface {
	
	/** 
	 * The type of the report to be generated. This corresponds to the proper type
	 *  of the <code>ReportBuilder</code> class instance.
	 */
	protected ReportNode.ReportType reportType;
	
	/** The template file name. */
	protected String template;
	
	/** The output file name. */
	protected String output;
	
	/** The parent node from the XML file for reference to other objects. */
	protected LigretoNode ligretoNode;
		
	/** The global ligreto parameters. */
	protected LigretoParameters ligretoParameters;
	
	/** Nobody except child classes could create the instance. */
	protected ReportBuilder() {
	}
	
	/**
	 * This method will create the instance of the right child class
	 * corresponding to the report type specified.
	 * 
	 * @param ligretoNode The reference for the parent node.
	 * @param reportType The desired report type to be created.
	 * @return The instance of the child class of <code>ReportBuilder</code>
	 *         corresponding to the desired report type.
	 */
	public static BuilderInterface createInstance(LigretoNode ligretoNode, ReportNode.ReportType reportType) {
		ReportBuilder builder;
		switch (reportType) {
		case EXCEL:
			builder = new ExcelReportBuilder();
			break;
		case EXCELSTREAM:
			builder = new ExcelStreamReportBuilder();
			break;
		case HTML:
			builder = new HtmlReportBuilder();
			break;
		case TEX:
			throw new UnimplementedMethodException();
		case XML:
			throw new UnimplementedMethodException();
		default:
			throw new UnimplementedMethodException();			
		}
		builder.reportType = reportType;
		builder.ligretoNode = ligretoNode;
		return builder;
	}	
	
	@Override
	public void setTemplate(String template) {
		if (template != null && ligretoNode != null) {
			this.template = ligretoNode.substituteParams(template);
		} else {
			this.template = null;
		}
	}
	
	@Override
	public void setOutputFileName(String output) {
		if (ligretoNode != null) {
			this.output = ligretoNode.substituteParams(output);
		} else {
			this.output = output;			
		}
	}

	@Override
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
		this.ligretoParameters = ligretoParameters;
	}

	@Override
	public File getOutputFile() {
		return new File(output);
	}
}
