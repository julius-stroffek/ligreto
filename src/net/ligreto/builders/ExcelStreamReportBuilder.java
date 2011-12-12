package net.ligreto.builders;

import java.io.FileOutputStream;
import java.io.IOException;

import net.ligreto.exceptions.LigretoException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Provides the streaming version of XSSF Apache POI report generator. The produced
 * rows are flushed to disk each time the certain number of rows is produced.
 * 
 * @author Julius Stroffek
 *
 */
public class ExcelStreamReportBuilder extends ExcelReportBuilder {

	/**
	 * The number of rows which are kept in memory. If the number of rows produced
	 * in the report exceeds this number, all the rows are flushed to disk.
	 */
	protected static final int FLUSH_ROW_INTERVAL = 500;
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ExcelReportBuilder.class);

	public ExcelStreamReportBuilder() {
	}
	
	protected void flushRows() throws IOException {
		SXSSFSheet ss = (SXSSFSheet) sheet;
		finalizeTarget(true);
		ss.flushRows();
	}
	
	@Override
	public void nextRow() throws IOException {
		// Flush the rows if the number of produced rows matched the specified number
		if ((actRow - baseRow) % FLUSH_ROW_INTERVAL == 0)
			flushRows();
		// Do the rest of the job
		super.nextRow();
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
		wb = new SXSSFWorkbook();

		// Create the data format object
		dataFormat = wb.createDataFormat();
		
		sheet = null;
		reportExcelStatisctics();
		log.info("The output will be written to \"" + output + "\".");
	}

}
