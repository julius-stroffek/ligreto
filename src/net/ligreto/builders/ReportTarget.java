package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.parser.nodes.LigretoNode;

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
 *   		tgt.nextRow();
 *   		// Dump the i-th column from the ResultSet
 *   		tgt.dumpColumn(i, objectValue); 
 *   		// Could dump more cells here
 *   		tgt.shiftPosition(10);
 *   		// Could dump more cells here
 *   	}
 *   	tgt.finish();
 *   }
 *   
 * </pre>
 *  
 * @author Julius Stroffek
 *
 */
public abstract class ReportTarget implements TargetInterface {
	
	/** 
	 * The report builder that created this target
	 */
	protected ReportBuilder reportBuilder;
	
	/** The parent node from the XML file for reference to other objects. */
	protected LigretoNode ligretoNode;
	
	/** The number of columns to shift right after the call to <code>setColumn</code>. */
	protected int columnStep = 1;
	
	/**
	 * The base row which is defined by the call to <code>setTarget</code>.
	 *
	 * The numbering starts from 0.
	 */
	protected int baseRowNumber = 0;
	
	/**
	 * The base column which is defined by the call to <code>setTarget</code>.
	 * 
	 * The numbering starts from 0.
	 */
	protected int baseColumnPosition = 0;
	
	/**
	 * The actual row number where the output is being produced.
	 * <p>
	 * Initially this starts one row before the base row as this
	 * will get shifted by the call to <code>nextRow</code>
	 * </p>
	 */
	protected int actualRowNumber = baseRowNumber-1;
	
	/**
	 * The actual column number where the output is produced. The exact column number
	 * where the function <code>setColumn</code> stores the content is produced by taking
	 * this <code>actCol</code> value and adding the value of the column number argument
	 * specified in the <code>setColumn</code> method call.
	 */
	protected int actualColumnPosition = baseColumnPosition;
		
	/** The global ligreto parameters. */
	protected LigretoParameters ligretoParameters;
	
	/** Nobody except child classes could create the instance. */
	protected ReportTarget(ReportBuilder reportBuilder) {
		this.reportBuilder = reportBuilder;
	}
	
	@Override
	public void dumpCell(int i, Object value) {
		dumpCell(i, value, OutputFormat.DEFAULT, OutputStyle.DEFAULT);
	}
	
	@Override
	public void dumpCell(int i, Object value, OutputFormat outputFormat) {
		dumpCell(i, value, outputFormat, OutputStyle.DEFAULT);
	}
	
	@Override
	public void dumpCell(int i, Object value, OutputStyle outputStyle) {
		dumpCell(i, value, OutputFormat.DEFAULT, outputStyle);
	}
		
	/**
	 * @param actualRowNumber the actualRowNumber to set
	 */
	public void setActualRowNumber(int actualRowNumber) {
		this.actualRowNumber = actualRowNumber;
	}

	@Override
	public void nextRow() throws IOException {
		actualRowNumber++;
		setPosition(0,1);
	}
			
	@Override
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
		this.ligretoParameters = ligretoParameters;
	}

	@Override
	public void setPosition(int columnPosition) {
		setPosition(columnPosition, columnStep);
	}

	@Override
	public void setPosition(int columnPosition, int columnStep) {
		actualColumnPosition = baseColumnPosition + columnPosition;
		this.columnStep = columnStep;
	}

	@Override
	public void shiftPosition(int columnsToShift) {
		shiftPosition(columnsToShift, columnStep);
	}
	
	@Override
	public void shiftPosition(int columnsToShift, int columnStep) {
		actualColumnPosition += columnsToShift;
		this.columnStep = columnStep;
	}
}
