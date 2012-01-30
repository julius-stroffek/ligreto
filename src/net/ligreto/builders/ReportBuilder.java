package net.ligreto.builders;

import org.apache.poi.ss.usermodel.Sheet;

import net.ligreto.LigretoParameters;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.ReportNode;

/**
 * This class defines the interface between the various report executors
 * and the target report format. The main type of the output report format
 * is excel spreadsheet. However, any type of the output report format could
 * be used if the below interface could be implemented.
 * 
 * This is the overall overview on what methods should be called during
 * life-cycle of this class in pseudo-code as called by the executors.
 * 
 * <pre>
 *   setTemplate("Template.xls");
 *   setOutput("Output.xls");
 *   
 *   for (tgt : targets) {
 *   	setTarget("A target ID string"); // For spreadsheet - e.g. "B:2"
 *   	for (row : rows) {
 *   		nextRow();
 *   		// Dump the i-th column from the ResultSet
 *   		setColumn(i, rs); 
 *   		// Could dump more columns here using setColumn method call
 *   		setColumnPosition(10);
 *   		// Could dump more columns here to shifted location using setColumn method call
 *   	}
 *   }
 *   
 * </pre>
 *
 * The report builder could automatically highlight the differences
 * or otherwise highlight certain column values as specified
 * by invoking <code>setHighlightArray</code> before the call
 * to any <code>setCell</code> methods.
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
	
	/**
	 * Creates the target instance of the right super-type based on the parameters specified.
	 * 
	 * @param sheet
	 * @param baseRow
	 * @param baseCol
	 * @return
	 */
	protected abstract TargetInterface createTarget(Sheet sheet, int baseRow, int baseCol);

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setTemplate(java.lang.String)
	 */
	@Override
	public void setTemplate(String template) {
		if (template != null) {
			this.template = ligretoNode.substituteParams(template);
		} else {
			this.template = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setOutput(java.lang.String)
	 */
	@Override
	public void setOutput(String output) {
		this.output = ligretoNode.substituteParams(output);
	}
	
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
		this.ligretoParameters = ligretoParameters;
	}
}
