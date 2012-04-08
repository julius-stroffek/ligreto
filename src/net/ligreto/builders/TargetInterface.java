package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.exceptions.LigretoException;

/**
 * Provides the interface to dump the data to the particular part of the report.
 *
 * The entire design is driven as the output is written into spreadsheet. All other
 * implementations for report types have to adopt this approach.
 * 
 * <p>
 * The output is driven by the output rows. Within each row the multiple cell values
 * could be dumped. Each cell that is dumped has the position within the row (like
 * column number within spreadsheet).
 * </p>
 * 
 * <p>
 * The sample pseudo-code working with the target:
 * </p>
 * 
 * <pre>
 * BuiderInterface bi = ReportBuilder.createInstance(...);
 * TargetInterface ti = bi.getTargetBuilder(...);
 * 
 * while (rows to be dumped exist) {
 *     ti.nextRow();
 *     ti.dumpCell(0, value1);
 *     ti.dumpCell(1, value2);
 *     ti.shiftPosition(2, 1);
 *     ti.dumpCell(0, value3);
 *     ri.dumpCell(1, value4);
 * }
 * </pre>
 * 
 * This interface have to be implemented to provide various types of reports,
 * e.g. XML, CSV, database table output.
 *
 * @author Julius Stroffek
 *
 */
public interface TargetInterface {
	
	/**
	 * Move the output to the next row.
	 * 
	 * @throws IOException if there was I/O error
	 */
	public abstract void nextRow() throws IOException;
	
	/**
	 * Sets up the output cell position within the current row.
	 * 
	 * @param baseColumnPosition the cell position to set
	 */
	public abstract void setPosition(int baseColumnPosition);
	
	/**
	 * Sets up the output cell position within the current row. This method
	 * could be used to arrange the dump of values to be interlaced. This
	 * could be achieved by setting up the {@code columnsStep} to value
	 * larger than 1.
	 * 
	 * @param baseColumnPosition the cell position to set
	 * @param columnStep the step to shift for each cell on the position larger than 0
	 * @see #dumpCell(int, Object)
	 */
	public abstract void setPosition(int baseColumnPosition, int columnStep);
	
	/**
	 * Shifts the output cell position from the current position by
	 * the specified number of cells.
	 * 
	 * @param columnsToShift the number of cells to shift
	 */
	public abstract void shiftPosition(int columnsToShift);
	
	/**
	 * Shifts the output cell position from the current position by
	 * the specified number of cells. This method could be used
	 * to arrange the dump of values to be interlaced. This could
	 * be achieved by setting up the {@code columnsStep} to value
	 * larger than 1.
	 * 
	 * @param columnsToShift the number of cells to shift
	 * @param columnStep the step to shift for each cell on the position larger than 0
	 */
	public abstract void shiftPosition(int columnsToShift, int columnStep);
	
	/**
	 * Dump the specified object to the position shifted by the specified index. The position
	 * is adjusted by the step specified in the call to {@link #setPosition(int, int)} or
	 * {@link #shiftPosition(int, int)} and multiplied by the value of parameter {@code index}.
	 * 
	 * @param index the index of the output cell measured by the actual possition in the current row
	 * @param value the value to be dumped
	 * @throws LigretoException if there was processing error
	 * @see #setPosition(int, int)
	 * @see #shiftPosition(int, int)
	 */
	public abstract void dumpCell(int index, Object value) throws LigretoException;

	/**
	 * Dump the specified object to the position shifted by the specified index. The position
	 * is adjusted by the step specified in the call to {@link #setPosition(int, int)} or
	 * {@link #shiftPosition(int, int)} and multiplied by the value of parameter {@code index}.
	 * 
	 * @param index the index of the output cell measured by the actual possition in the current row
	 * @param value the value to be dumped
	 * @param outputFormat the data format to be used
	 * @throws LigretoException if there was processing error
	 * @see #setPosition(int, int)
	 * @see #shiftPosition(int, int)
	 */
	public abstract void dumpCell(int index, Object value, OutputFormat outputFormat) throws LigretoException;

	/**
	 * Dump the specified object to the position shifted by the specified index. The position
	 * is adjusted by the step specified in the call to {@link #setPosition(int, int)} or
	 * {@link #shiftPosition(int, int)} and multiplied by the value of parameter {@code index}.
	 * 
	 * @param index the index of the output cell measured by the actual possition in the current row
	 * @param value the value to be dumped
	 * @param outputStyle the output style to be used
	 * @throws LigretoException if there was processing error
	 * @see #setPosition(int, int)
	 * @see #shiftPosition(int, int)
	 */
	public abstract void dumpCell(int index, Object value, OutputStyle outputStyle) throws LigretoException;
	
	/**
	 * Dump the specified object to the position shifted by the specified index. The position
	 * is adjusted by the step specified in the call to {@link #setPosition(int, int)} or
	 * {@link #shiftPosition(int, int)} and multiplied by the value of parameter {@code index}.
	 * 
	 * @param index the index of the output cell measured by the actual possition in the current row
	 * @param value the value to be dumped
	 * @param outputFormat the data format to be used
	 * @param outputStyle the output style to be used
	 * @throws LigretoException if there was processing error
	 * @see #setPosition(int, int)
	 * @see #shiftPosition(int, int)
	 */
	public abstract void dumpCell(int index, Object value, OutputFormat outputFormat, OutputStyle outputStyle) throws LigretoException;

	/**
	 * Finalize the dump of data into this target.
	 * 
	 * @throws IOException if there were any I/O related errors
	 */
	public abstract void finish() throws IOException;
	
	/**
	 * Sets up the highlighting on this target.
	 * 
	 * @param highlight the highlighting behavior to set
	 */
	public abstract void setHighlight(boolean highlight);
	
	/**
	 * Set up the highlight color for this target.
	 * 
	 * @param rgbHlColor the rgb color of highlighted text
	 */
	public abstract void setHighlightColor(short[] rgbHlColor);

	/**
	 * Sets up the global ligreto parameters to be used.
	 * 
	 * @param ligretoParameters the parameters to set
	 * @see LigretoParameters
	 */
	public abstract void setLigretoParameters(LigretoParameters ligretoParameters);
}
