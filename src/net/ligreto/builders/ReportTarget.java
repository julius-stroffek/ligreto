package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.LigretoNode;

/**
 * The {@link TargetInterface} generic implementation.
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
	 * The base row which is defined by the call to <code>getTargetBuilder</code>.
	 *
	 * The numbering starts from 0.
	 */
	protected int baseRowNumber = 0;
	
	/**
	 * The base column which is defined by the call to <code>getTargetBuilder</code>.
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
	protected int currentRowNumber = baseRowNumber-1;
	
	/**
	 * The actual column number where the output is produced. The exact column number
	 * where the function <code>setColumn</code> stores the content is produced by taking
	 * this <code>actCol</code> value and adding the value of the column number argument
	 * specified in the <code>setColumn</code> method call.
	 */
	protected int currentColumnPosition = baseColumnPosition;
		
	/** The global ligreto parameters. */
	protected LigretoParameters ligretoParameters;
	
	/** Instances should be created by static methods. */
	protected ReportTarget(ReportBuilder reportBuilder) {
		this.reportBuilder = reportBuilder;
	}
	
	@Override
	public void dumpCell(int i, Object value) throws LigretoException {
		dumpCell(i, value, OutputFormat.DEFAULT, OutputStyle.DEFAULT);
	}
	
	@Override
	public void dumpCell(int i, Object value, OutputFormat outputFormat) throws LigretoException {
		dumpCell(i, value, outputFormat, OutputStyle.DEFAULT);
	}
	
	@Override
	public void dumpCell(int i, Object value, OutputStyle outputStyle) throws LigretoException {
		dumpCell(i, value, OutputFormat.DEFAULT, outputStyle);
	}
		
	/**
	 * Set the current row number. This
	 * @param actualRowNumber the actualRowNumber to set
	 */
	public void setCurrentRow(int actualRowNumber) {
		this.currentRowNumber = actualRowNumber;
	}

	@Override
	public void nextRow() throws IOException {
		currentRowNumber++;
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
		currentColumnPosition = baseColumnPosition + columnPosition;
		this.columnStep = columnStep;
	}

	@Override
	public void shiftPosition(int columnsToShift) {
		shiftPosition(columnsToShift, columnStep);
	}
	
	@Override
	public void shiftPosition(int columnsToShift, int columnStep) {
		currentColumnPosition += columnsToShift;
		this.columnStep = columnStep;
	}
}
