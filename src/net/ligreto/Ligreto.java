/**
 * 
 */
package net.ligreto;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class Ligreto {

	/** The maximal exit status returned as result of ligreto operations. */
	public static final int MAX_RESULT_EXIT_STATUS = 250;

	/** The exit status returned when the exception have occurred. */
	public static final int EXCEPTION_EXIT_STATUS = 255;
	
	/**
	 * @param args The command-line arguments
	 */
	public static void main(String[] args) {
		int result = 0;
		Options options = new Options();

		Option help = new Option( "help", "print this help message" );
		Option concat = new Option( "concat", "logically concatenate the input files and process them as one input file. Currently this is the only behavior regardless the option. [obsolete]" );
		Option excel97 = new Option( "excel97", "uses \"Excel 97\" format instead of default \"Excel 2007\" format" );
		Option param = new Option(
			"D",
			true,
			"specifies the report parameter value in a form PARAM=VALUE."
			 + " This overrides the values specified in input files."
		);
		options.addOption(help);
		options.addOption(concat);
		options.addOption(excel97);
		options.addOption(param);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
		}
		String[] files = cmd.getArgs();
		
		if (cmd.hasOption("help") || files.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
				"ligreto.sh [OPTIONS] file1.xml [file2.xml...]",
				"---",
				options,
				"\nExample:\n ligreto.sh -D sysdate=2011-12-31 config.xml report1.xml\n---",
				false
			);
			//formatter.printHelp(cmdLineSyntax, header, options, footer, autoUsage)
			System.exit(1);
		}
		
		// Use Excel97 format if requested
		if (cmd.hasOption("excel97")) {
			System.setProperty("excel97", "yes");
		}
		
		// Get the parameter values and store them after reading the file 
		String[] params = cmd.getOptionValues("D");
		try {
			LigretoNode ligretoNode = new LigretoNode();
			for (int i=0; i < files.length; i++) {
				Parser.parse(files[i], ligretoNode);
			}
			if (params != null) {
				for (int j=0; j < params.length; j++) {
					if (params[j].indexOf('=') >=0) {
						String paramName = params[j].substring(0, params[j].indexOf('='));
						String paramValue = params[j].substring(params[j].indexOf('=') + 1);
						ligretoNode.addParam(paramName, paramValue);
					}
				}
			}
			LigretoExecutor executor = new LigretoExecutor(ligretoNode);
			result = executor.execute();
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(EXCEPTION_EXIT_STATUS);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(EXCEPTION_EXIT_STATUS);
		} catch (LigretoException e) {
			e.printStackTrace();
			System.exit(EXCEPTION_EXIT_STATUS);
		}
		if (result > MAX_RESULT_EXIT_STATUS)
			System.exit(MAX_RESULT_EXIT_STATUS);
		else
			System.exit(result);
	}
}
