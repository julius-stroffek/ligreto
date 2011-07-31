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

	/**
	 * @param args The command-line arguments
	 */
	public static void main(String[] args) {
		Options options = new Options();

		Option help = new Option( "help", "print this help message" );
		Option concat = new Option( "concat", "logically concatenate the input files and process them as one input file" );
		options.addOption(help);
		options.addOption(concat);
		
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
			formatter.printHelp( "ligreto [OPTIONS] file1.xml [file2.xml...]", options);
			System.exit(1);
		}
		
		try {
			if (cmd.hasOption("concat")) {
				LigretoNode ligretoNode = new LigretoNode();
				for (int i=0; i < files.length; i++) {
					Parser.parse(files[i], ligretoNode);
				}
				LigretoExecutor executor = new LigretoExecutor(ligretoNode);
				executor.executeReports();
			} else {
				for (int i=0; i < files.length; i++) {
					LigretoNode ligreto = Parser.parse(files[i]);
					LigretoExecutor executor = new LigretoExecutor(ligreto);
					executor.execute();
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LigretoException e) {
			e.printStackTrace();
		}
	}

}
