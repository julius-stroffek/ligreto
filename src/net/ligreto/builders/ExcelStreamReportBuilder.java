package net.ligreto.builders;

import java.io.FileOutputStream;
import java.io.IOException;

import net.ligreto.exceptions.LigretoException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelStreamReportBuilder extends ExcelReportBuilder {

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ExcelReportBuilder.class);

	public ExcelStreamReportBuilder() {
	}

	@Override
	public void start() throws IOException, LigretoException {
		out = new FileOutputStream(output);

		outputFormat = OutputFormat.SXSSF;
		
		// Read the template file if the template was specified
		if (template != null) {
			log.error("For streaming excel Template file could not be used for excel.stream report.");
			throw new LigretoException("For streaming excel Template file could not be used for excel.stream report.");
		}
		
		log.info("Creating the empty workbook.");
		wb = new SXSSFWorkbook(100);

		// Create the data format object
		dataFormat = wb.createDataFormat();
		
		sheet = null;
		reportExcelStatisctics();
		log.info("The output will be written to \"" + output + "\".");
	}

}
