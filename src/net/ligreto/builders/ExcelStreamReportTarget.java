package net.ligreto.builders;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;

/**
 * Provides the streaming version of XSSF Apache POI report generator. The produced
 * rows are flushed to disk each time the certain number of rows is produced.
 * 
 * @author Julius Stroffek
 *
 */
public class ExcelStreamReportTarget extends ExcelReportTarget {

	/**
	 * The number of rows which are kept in memory. If the number of rows produced
	 * in the report exceeds this number, all the rows are flushed to disk.
	 */
	public static final int FLUSH_ROW_INTERVAL = 500;
	
	///** The logger instance for the class. */
	//private Log log = LogFactory.getLog(ExcelReportBuilder.class);

	/** Creates the target instance bound to ExcelReportBuilder. */
	public  ExcelStreamReportTarget(ExcelReportBuilder reportBuilder, Sheet sheet, int baseRow, int baseCol) {
		super(reportBuilder, sheet, baseRow, baseCol);
	}
	
	protected void flushRows() throws IOException {
		SXSSFSheet ss = (SXSSFSheet) sheet;
		flush(false, true);
		ss.flushRows();
	}
	
	@Override
	public void nextRow() throws IOException {
		// Flush the rows if the number of produced rows matched the specified number
		if ((actualRowNumber - baseRowNumber) % FLUSH_ROW_INTERVAL == 0)
			flushRows();
		// Do the rest of the job
		super.nextRow();
	}
	
	@Override
	public void finish() throws IOException {
		SXSSFSheet ss = (SXSSFSheet) sheet;
		flush(true, true);
		ss.flushRows();
		targetInfo.lastRow = actualRowNumber;
		targetInfo.inUse = false;
	}

}
