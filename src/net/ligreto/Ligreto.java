/**
 * 
 */
package net.ligreto;

import java.io.IOException;

import net.ligreto.config.Parser;
import net.ligreto.config.nodes.LigretoNode;

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
		options.addOption(help);
		
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
			for (int i=0; i < files.length; i++) {
				LigretoNode ligreto = Parser.parse(files[i]);
				ReportBuilder.buildReport(ligreto);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
