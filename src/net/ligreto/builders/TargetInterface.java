package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;

public interface TargetInterface {
	
	public abstract void nextRow() throws IOException;
	public abstract void setPosition(int baseColumnPosition);
	public abstract void setPosition(int baseColumnPosition, int columnStep);
	public abstract void shiftPosition(int columnsToShift);
	public abstract void shiftPosition(int columnsToShift, int columnStep);
	public abstract void dumpCell(int i, Object value);
	public abstract void dumpCell(int i, Object value, OutputFormat outputFormat);
	public abstract void dumpCell(int i, Object value, OutputStyle outputStyle);
	public abstract void dumpCell(int i, Object value, OutputFormat outputFormat, OutputStyle outputStyle);
	public abstract void finish() throws IOException;
	public abstract void setHighlight(boolean highlight);
	public abstract void setHighlightColor(short[] rgbHlColor);

	/**
	 * Sets up the global ligreto parameters to be used.
	 * @param ligretoParameters
	 */
	public abstract void setLigretoParameters(LigretoParameters ligretoParameters);
}
