package net.ligreto.junit.util;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * This class provides excel2007 file comparison functions. It will call
 * the common comparator functions from <code>WorkbookComparator</code>
 * and it will additionally check the excel2007 specific things.
 * 
 * @author Julius Stroffek
 *
 */

public class XSSFWorkbookComparator extends WorkbookComparator {
	
	/** Constructs the comparator instance. */
	public XSSFWorkbookComparator(Workbook w1, Workbook w2) {
		super(w1, w2);
	}

	/**
	 * This function will compare the excel 2007 workbook object instances.
	 * 
	 * @return true if the excel spreadsheets will match
	 */
	public boolean areSame() {
		return super.areSame();
	}

}
