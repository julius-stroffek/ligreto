/**
 * 
 */
package net.ligreto;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.util.AssertionUtil;
import net.ligreto.util.MiscUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class Ligreto {

	/** The version string identifying the current version. */
	public static final String version = "2015.3";
	
	/** The maximal exit status returned as result of ligreto operations. */
	public static final int MAX_RESULT_EXIT_STATUS = 250;

	/** The exit status returned when the exception have occurred. */
	public static final int EXCEPTION_EXIT_STATUS = 255;
	
	/** The logger instance for the class. */
	private static Logger log = Logger.getLogger(Ligreto.class);

	public static void exit(int exitCode) {
		log.debug("EXIT STATUS: " + exitCode);
		System.exit(exitCode);
	}
	
	/**
	 * @param args The command-line arguments
	 */
	public static void main(String[] args) {
		long resultCount = 0;
		ResultStatus resultStatus = null;
		Options options = new Options();

		// Print out the version information
		System.out.println("##############################################################################".substring(10 - version.length()));		
		System.out.println("###                      ligreto - version " + version + "                      ###");
		System.out.println("##############################################################################".substring(10 - version.length()));
		
		// Prepare the command line option parsing
		Option help = new Option( "help", "print this help message" );
		Option concat = new Option( "concat", "logically concatenate the input files and process them as one input file. Currently this is the only behavior regardless the option. [obsolete]" );
		Option excel97 = new Option( "excel97", "uses \"Excel 97\" format instead of default \"Excel 2007\" format" );
		Option debug = new Option( "debug", "turn on debug messages" );
		Option trace = new Option( "trace", "turn on trace and debug messages" );
		Option param = new Option(
			"D",
			true,
			"specifies the report parameter value in a form PARAM=VALUE."
			 + " This overrides the values specified in input files."
		);
		options.addOption(help);
		options.addOption(concat);
		options.addOption(excel97);
		options.addOption(debug);
		options.addOption(trace);
		options.addOption(param);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.err.println("Command line option parsing failed.");
			System.exit(EXCEPTION_EXIT_STATUS);
		}
		String[] files = cmd.getArgs();
		
		Level logLevel = Level.INFO;
		if (cmd.hasOption("debug") || cmd.hasOption("trace")) {
			// Get the proper log level
			logLevel = Level.DEBUG;
			if (cmd.hasOption("trace")) {
				logLevel = Level.TRACE;
			}
			
			// Enable java assertions
			AssertionUtil.enableAssertions();
		}
		// Set the log severity
		Logger.getRootLogger().setLevel(logLevel);
		log.setLevel(logLevel);

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
			exit(1);
		}
		
		// Use Excel97 format if requested
		if (cmd.hasOption("excel97")) {
			System.setProperty("excel97", "yes");
		}
		
		// Get the parameter values and store them after reading the file 
		String[] params = cmd.getOptionValues("D");
		try {
			LigretoNode ligretoNode = new LigretoNode();

			// Get the system properties and store them as parameters
			for (Object property : System.getProperties().keySet()) {
				String name = property.toString();
				String value = System.getProperty(name);
				ligretoNode.addLockedParam("system." + name, value);
			}

			// Parse the command line arguments as parameters
			if (params != null) {
				for (int j=0; j < params.length; j++) {
					if (params[j].indexOf('=') >= 0) {
						String paramName = params[j].substring(0, params[j].indexOf('='));
						String paramValue = params[j].substring(params[j].indexOf('=') + 1);
						ligretoNode.addLockedParam(paramName, paramValue);
					}
				}
			}
			
			// Parse the specified input files
			for (int i=0; i < files.length; i++) {
				Parser.parse(files[i], ligretoNode);
			}

			// Add additional defined parameters
			Calendar now = Calendar.getInstance();
			
			// Store the value of timeStamp
			String timeStampFormat = ligretoNode.getParam("ligreto.timestampFormat");
			if (MiscUtils.isNotEmpty(timeStampFormat)) {
				ligretoNode.addParam("ligreto.timestamp", new SimpleDateFormat(timeStampFormat).format(now.getTime()));				
			} else {
				ligretoNode.addParam("ligreto.timestamp", DateFormat.getDateTimeInstance().format(now.getTime()));
			}
			
			// Store the value of time
			String timeFormat = ligretoNode.getParam("ligreto.timeFormat");
			if (MiscUtils.isNotEmpty(timeFormat)) {
				ligretoNode.addParam("ligreto.time", new SimpleDateFormat(timeFormat).format(now.getTime()));				
			} else {
				ligretoNode.addParam("ligreto.time", DateFormat.getTimeInstance().format(now.getTime()));
			}
			
			// Store the value of date
			String dateFormat = ligretoNode.getParam("ligreto.dateFormat");
			if (MiscUtils.isNotEmpty(dateFormat)) {
				ligretoNode.addParam("ligreto.date", new SimpleDateFormat(dateFormat).format(now.getTime()));				
			} else {
				ligretoNode.addParam("ligreto.date", DateFormat.getDateInstance().format(now.getTime()));
			}
			
			// Run the execution
			LigretoExecutor executor = new LigretoExecutor(ligretoNode);
			resultStatus = executor.execute();
			resultCount = resultStatus.getDifferentRowCount();
		} catch (SAXException e) {
			MiscUtils.printThrowableMessages(log, e);
			exit(EXCEPTION_EXIT_STATUS);
		} catch (IOException e) {
			MiscUtils.printThrowableMessages(log, e);
			exit(EXCEPTION_EXIT_STATUS);
		} catch (LigretoException e) {
			MiscUtils.printThrowableMessages(log, e);
			exit(EXCEPTION_EXIT_STATUS);
		}
		if (resultStatus.isAccepted()) {
			exit(0);
		} else if (resultCount > MAX_RESULT_EXIT_STATUS) {
			exit(MAX_RESULT_EXIT_STATUS);
		} else if (resultCount > 0) {
			exit((int)resultCount);
		}
		// Exit with +1 when we do not know the different row count
		exit(MAX_RESULT_EXIT_STATUS + 1);
	}
}
