package net.ligreto.junit.util;

import net.ligreto.builders.ExcelReportBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * This class provides the excel spreadsheet comparison functions.
 * 
 * @author Julius Stroffek
 *
 */
public class WorkbookComparator {
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ExcelReportBuilder.class);
	
	/** The first work book to be compared. */
	protected Workbook w1;
	
	/** The second workbook to be compared. */
	protected Workbook w2;
	
	/** Constructs the comparator instance. */
	public WorkbookComparator(Workbook w1, Workbook w2) {
		this.w1 = w1;
		this.w2 = w2;
	}

	/**
	 * This function will compare the excel workbook object instances.
	 * 
	 * @return true if the excel spreadsheets will match
	 */
	protected boolean areSame() {
		boolean result = true;
		if (w1.getNumberOfSheets() != w2.getNumberOfSheets()) {
			log.error("The number of sheets differs: " + w1.getNumberOfSheets() + ", " + w2.getNumberOfSheets());
			result = false;
		}
		if (w1.getNumCellStyles() != w2.getNumCellStyles()) {
			log.error("The number of cell styles differs: " + w1.getNumCellStyles() + ", " + w2.getNumCellStyles());
			result = false;
		}
		if (w1.getNumberOfFonts() != w2.getNumberOfFonts()) {
			log.error("The number of fonts differs: " + w1.getNumberOfFonts() + ", " + w2.getNumberOfFonts());
			result = false;
		}
		if (w1.getNumberOfNames() != w2.getNumberOfNames()) {
			log.error("The number of named areas differs: " + w1.getNumberOfNames() + ", " + w2.getNumberOfNames());
			result = false;
		}
		for (int i=0; i < w1.getNumberOfSheets(); i++) {
			if (!areSame(w1.getSheetAt(i), w2.getSheetAt(i))) {
				log.error("The sheet content differs: " + w1.getSheetAt(i).getSheetName() + ", " + w2.getSheetAt(i).getSheetName());
				result = false;
			}
		}
		if (result) {
			log.info("No differences encountered in the compared workbooks.");
		}
		return result;
	}

	/**
	 * This function will compare the content of the specified sheets.
	 * 
	 * @param s1 first sheet to be compared
	 * @param s2 second sheet to be compared
	 * @return
	 */
	protected boolean areSame(Sheet s1, Sheet s2) {
		boolean result = true;
		if (!s1.getSheetName().equals(s2.getSheetName())) {
			log.error("The sheet names differ for matching sheets: " + s1.getSheetName() + ", " + s2.getSheetName());
			result = false;
		}
		if (s1.getFirstRowNum() != s2.getFirstRowNum()) {
			log.error("The first row number differs for matching sheets: " + s1.getFirstRowNum() + ", " + s2.getFirstRowNum());
			result = false;
		}
		if (s1.getLastRowNum() != s2.getLastRowNum()) {
			log.error("The last row number differs for matching sheets: " + s1.getLastRowNum() + ", " + s2.getLastRowNum());
			result = false;
		}
		for (int i = s1.getFirstRowNum(); i <= s1.getLastRowNum(); i++) {
			if (!areSame(s1.getRow(i), s2.getRow(i))) {
				log.error("The row content differs; row: " + i);
				result = false;
			}
		}
		return result;
	}

	/**
	 * This function will compare the content of the specified rows.
	 * 
	 * @param r1 first row to be compared
	 * @param r2 second row to be compared
	 * @return
	 */
	protected boolean areSame(Row r1, Row r2) {
		boolean result = true;
		// First, we will take care of null values
		if (r1 == null && r2 == null) {
			return true;
		} else if (r1 == null || r2 == null) {
			return false;
		}

		if (r1.getFirstCellNum() != r2.getFirstCellNum()) {
			log.error("The first cell number differs for matching rows: " + r1.getFirstCellNum() + ", " + r2.getFirstCellNum());
			result = false;
		}
		if (r1.getLastCellNum() != r2.getLastCellNum()) {
			log.error("The last cell number differs for matching rows: " + r1.getLastCellNum() + ", " + r2.getLastCellNum());
			result = false;
		}
		for (int i = r1.getFirstCellNum(); i <= r1.getLastCellNum(); i++) {
			if (!areSame(r1.getCell(i), r2.getCell(i))) {
				log.error("The cell content differs in column: " + i);
				result = false;
			}
		}
		return result;
	}

	/**
	 * This function will compare the content of the specified rows.
	 * 
	 * @param c1 first cell to be compared
	 * @param c2 second cell to be compared
	 * @return
	 */
	protected boolean areSame(Cell c1, Cell c2) {
		boolean result = true;
		// First, we will take care of null values
		if (c1 == null && c2 == null) {
			return true;
		} else if (c1 == null || c2 == null) {
			return false;
		}
		
		if (!c1.toString().equals(c2.toString())) {
			log.error("The cell value differs for matching cells: \""
					+ c1.toString() + "\" \""
					+ c2.toString() + "\""
			);
			result = false;
		}
		if (!areSame(c1.getCellStyle(), c2.getCellStyle())) {
			log.error("The cell styles differ for matching cells.");
			result = false;
		}
		return result;
	}

	/**
	 * This function will compare the specified cell styles.
	 * 
	 * @param s1 first style to be compared
	 * @param s2 second style to be compared
	 * @return
	 */
	protected boolean areSame(CellStyle s1, CellStyle s2) {
		boolean result = true;
		if (!areSame(w1.getFontAt(s1.getFontIndex()), w2.getFontAt(s2.getFontIndex()))) {
			log.error("The style fonts differ for matching styles.");
			result = false;
		}
		if (s1.getFillForegroundColor() != s2.getFillForegroundColor()) {
			log.error("The style fill foreground color differs for matching styles.");
			result = false;
		}
		if (s1.getFillBackgroundColor() != s2.getFillBackgroundColor()) {
			log.error("The style fill background color differs for matching styles.");
			result = false;
		}
		if (s1.getFillPattern() != s2.getFillPattern()) {
			log.error("The style fill pattern differs for matching styles.");
			result = false;
		}
		return result;
	}

	/**
	 * This function will compare the specified fonts.
	 * 
	 * @param f1 first font to be compared
	 * @param f2 second font to be compared
	 * @return
	 */
	protected boolean areSame(Font f1, Font f2) {
		if (f1.getColor() != f2.getColor()) {
			log.error("The font colors differ for matching fonts: " + f1.getColor() + ", " + f2.getColor());
			return false;
		}
		return true;
	}
}
