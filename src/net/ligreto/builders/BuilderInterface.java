package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.LigretoException;

/**
 * Provides the interface for the output report generation.
 * 
 * This interface have to be implemented to provide various types of reports,
 * e.g. XML, CSV, database table output.
 * 
 * @author Julius Stroffek
 *
 */
public interface BuilderInterface {
	
	/** The style types for the output cell. */
	public enum OutputStyle {
		/** Default output style. */
		DEFAULT,
		/** Highlighted text. For example differences are highlighted. */
		HIGHLIGHTED,
		/** The text marked as disabled. For example columns that are not compared in comparisons. */
		DISABLED,
		/** The header shown on top. */
		TOP_HEADER,
		/** The header shown in a row. */
		ROW_HEADER,
		/** Top header for disabled column. */
		TOP_HEADER_DISABLED,
		/** Row header for disabled column. */
		ROW_HEADER_DISABLED
	};
	
	/** Keep the enumeration of the possible cell formatting changes. */
	public enum OutputFormat {
		/** The default format based on the data type. */
		DEFAULT,
		/** The percentage with no decimal digits. */
		PERCENTAGE_NO_DECIMAL_DIGITS,
		/** The percentage with two decimal digits. */
		PERCENTAGE_2_DECIMAL_DIGITS,
		/** The percentage with three decimal digits. */
		PERCENTAGE_3_DECIMAL_DIGITS
	};
	
	/** The string representation of NULL values. */
	public static final String NULL = "";
	
	/** Set up the template file.
	 * 
	 * @param template the template file to set
	 */
	public abstract void setTemplate(String template);

	/**
	 * Set up the output file name.
	 * 
	 * @param outputFileName the name of the output file
	 */
	public abstract void setOutputFileName(String outputFileName);

	/**
	 * Sets up the report type specific options.
	 * 
	 * @param options the options to set
	 * @throws LigretoException if the processing error occurs
	 */
	public abstract void setOptions(Iterable<String> options) throws LigretoException;

	/**
	 * This method will create the target builder used to build the result target.
	 * 
	 * @param target the target location to set
	 * @param append indicates whether the data should be appended to target location
	 * @throws InvalidTargetException if the specified target is invalid
	 * @throws TargetException if there is another problem creating the target
	 * @see TargetInterface
	 */
	public abstract TargetInterface getTargetBuilder(String target, boolean append) throws TargetException;

	/**
	 * The function called before the building of the report is started, i.e. function
	 * {@link #getTargetBuilder} is called.
	 * 
	 * It prepares the target builder, reads the template file if used, etc.
	 * 
	 * @throws IOException if there is problem with I/O operations 
	 * @throws LigretoException if there is any other problem
	 */
	public abstract void start() throws IOException, LigretoException;

	/**
	 * The method that will write the output to the required destination, i.e. file.
	 *
	 * @throws IOException if there is a I/O error
	 */
	public abstract void writeOutput() throws IOException;

	/**
	 * Sets up the global ligreto parameters object.
	 * 
	 * @param ligretoParameters the parameter object to set.
	 * @see LigretoParameters
	 */
	public void setLigretoParameters(LigretoParameters ligretoParameters);
}