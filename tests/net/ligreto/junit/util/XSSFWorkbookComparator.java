package net.ligreto.junit.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.junit.Assert;

/**
 * This class provides excel2007 file comparison functions. It will call
 * the common comparator functions from <code>WorkbookComparator</code>
 * and it will additionally check the excel2007 specific things.
 * 
 * @author Julius Stroffek
 *
 */

public class XSSFWorkbookComparator extends WorkbookComparator {
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(XSSFWorkbookComparator.class);

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

	/**
	 * This function will compare the specified fonts.
	 * 
	 * @param f1 first font to be compared
	 * @param f2 second font to be compared
	 * @return
	 */
	@Override
	protected boolean areSame(Font f1, Font f2) {
		if (f1 instanceof XSSFFont && f2 instanceof XSSFFont) {
			XSSFFont xf1 = (XSSFFont) f1;
			XSSFFont xf2 = (XSSFFont) f2;
			XSSFColor xssfColor1 = xf1.getXSSFColor();
			XSSFColor xssfColor2 = xf2.getXSSFColor();
			boolean different = false;
			if (xssfColor1 == null && xssfColor2 == null) {
				return true;
			} else if (xssfColor1 == null || xssfColor2 == null) {
				log.error("The font colors differ for matching fonts: " + xssfColor1 + ", " + xssfColor2);
				return false;
			} else {
				byte[] rgb1 = xssfColor1.getARgb();
				byte[] rgb2 = xssfColor1.getARgb();
				Assert.assertTrue(rgb1.length == rgb2.length);
				for (int i = 0; i < rgb1.length; i++) {
					if (rgb1[i] != rgb2[i]) {
						different = true;
						break;
					}
				}
			}
			if (different) {
				log.error("The font colors differ for matching fonts: " + xf1.getXSSFColor().getARGBHex() + ", " + xf2.getXSSFColor().getARGBHex());
				return false;				
			}
			return true;
		}
		log.error("The font types are not instance of XSSFFont class.");
		return false;
	}
}
