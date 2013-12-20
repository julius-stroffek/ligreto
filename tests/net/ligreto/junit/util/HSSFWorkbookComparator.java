package net.ligreto.junit.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * This class provides excel97 file comparison functions. It will call
 * the common comparator functions from <code>WorkbookComparator</code>
 * and it will additionally check the excel97 specific things.
 * 
 * @author Julius Stroffek
 *
 */
public class HSSFWorkbookComparator extends WorkbookComparator {
	
	/** Constructs the comparator instance. */
	public HSSFWorkbookComparator(HSSFWorkbook w1, HSSFWorkbook w2) {
		super(w1, w2);
	}

	/**
	 * This function will compare the excel 97 workbook object instances.
	 * 
	 * @return true if the excel spreadsheets will match
	 */
	@Override
	public boolean areSame() {
		return super.areSame();
	}
	

}
