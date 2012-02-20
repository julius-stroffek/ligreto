package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.LigretoException;

public interface BuilderInterface {
	
	/** The types of the headers. */
	public enum HeaderType {TOP, ROW}
	
	/** Keep the enumeration of the possible cell formatting changes. */
	public enum CellFormat {
		UNCHANGED,
		PERCENTAGE_NO_DECIMAL_DIGITS,
		PERCENTAGE_2_DECIMAL_DIGITS,
		PERCENTAGE_3_DECIMAL_DIGITS
	};
	
	/** The string representation of NULL values. */
	public static final String NULL = "";
	
	/** Set up the template file. */
	public abstract void setTemplate(String template);

	/** Set up the output file name. */
	public abstract void setOutput(String output);


	/**
	 * Sets up the report type specific options. 
	 *
	 * @throws LigretoException
	 */
	public abstract void setOptions(Iterable<String> options) throws LigretoException;

	/**
	 * This method will create the target builder used to build the result target. 
	 * @param target The target location to set.
	 * @param append Indicates whether the data should be appended.
	 * @throws InvalidTargetException
	 * @throws TargetException 
	 */
	public abstract TargetInterface getTargetBuilder(String target, boolean append) throws TargetException;

	/**
	 * The function called before the building of the report is started, i.e. any of the
	 * dump functions is called.
	 * 
	 * @throws IOException
	 * @throws LigretoException
	 */
	public abstract void start() throws IOException, LigretoException;

	/**
	 * The method that will write the output to the required destination, i.e. file.
	 * @throws IOException
	 */
	public abstract void writeOutput() throws IOException;
	
	/**
	 * Sets up the global ligreto parameters object.
	 * @param ligretoParameters the parameter object to set.
	 */
	public void setLigretoParameters(LigretoParameters ligretoParameters);
}