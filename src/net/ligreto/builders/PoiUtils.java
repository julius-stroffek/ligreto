/**
 * 
 */
package net.ligreto.builders;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * The Apache POI utility functions.
 * 
 * @author Julius Stroffek
 *
 */
public class PoiUtils {

	/**
	 * No instances are allowed.
	 */
	private PoiUtils() {
	}

	/**
	 * Clones the specified style. First, all existing styles are scanned and if
	 * the same style already exists it is re-used. Otherwise, new style is
	 * created. This method should be used for style de-duplication. First, you
	 * should alter the already existing style as you need. Then call cloneStyle
	 * method and then reverse back your changes on the original style object.
	 * Further, use the style object returned by cloneStyle as an altered style.
	 * 
	 * See the example below:
	 * 
	 * <pre>
	 * font = style.getFont();
	 * style.setFont(newFont);
	 * CellStyle newStyle = cloneStyle(style);
	 * cell.setCellStyle(newStyle);
	 * 
	 * // Revert back the font on the old cell style
	 * style.setFont(font);
	 * </pre>
	 * 
	 * @param style the style to be cloned
	 * @return the already existing style if it already exists, otherwise new style will get created and returned
	 */
	public static CellStyle findOrCreateStyle(Workbook wb, CellStyle style) {
		// Go through all the existing styles and re-use it if there is a match
		for (short i = 0; i < wb.getNumCellStyles(); i++) {
			// Do not compare the style to itself
			if (style.getIndex() == i)
				continue;
			// Compare the styles and use the already existing one instead of
			// creating a new one
			if (compareStyles(style, wb.getCellStyleAt(i))) {
				return wb.getCellStyleAt(i);
			}
		}

		// Create a new style since the same one does not exist
		CellStyle newStyle = wb.createCellStyle();
		newStyle.cloneStyleFrom(style);

		return newStyle;
	}

	/**
	 * Creates a new clone of the specified font.
	 * 
	 * @param wb workbook to create a clone in
	 * @param font font to be cloned
	 * @return
	 */
	public static Font cloneFont(Workbook wb, Font font) {
		Font newFont = wb.createFont();
		newFont.setBoldweight(font.getBoldweight());
		newFont.setColor(font.getColor());
		newFont.setFontHeight(font.getFontHeight());
		newFont.setFontName(font.getFontName());
		newFont.setItalic(font.getItalic());
		newFont.setStrikeout(font.getStrikeout());
		newFont.setTypeOffset(font.getTypeOffset());
		newFont.setUnderline(font.getUnderline());

		return newFont;
	}
	
	public static Font findOrCreateFont(Workbook wb, Font font, short boldFont, short fontColor) {
		Font newFont = wb.findFont(boldFont, fontColor, font.getFontHeight(), font.getFontName(), font.getItalic(),
							font.getStrikeout(), font.getTypeOffset(), font.getUnderline());
		if (newFont == null) {
			newFont = wb.createFont();
			newFont.setBoldweight(boldFont);
			newFont.setColor(fontColor);
			newFont.setFontHeight(font.getFontHeight());
			newFont.setFontName(font.getFontName());
			newFont.setItalic(font.getItalic());
			newFont.setStrikeout(font.getStrikeout());
			newFont.setTypeOffset(font.getTypeOffset());
			newFont.setUnderline(font.getUnderline());
		}
		
		return newFont;
	}
	/**
	 * Compare the specified styles.
	 * 
	 * @param s1 1st style to comapre
	 * @param s2 2nd style to compare
	 * @return true if the styles are equal
	 */
	public static boolean compareStyles(CellStyle s1, CellStyle s2) {
		if (s1.getAlignment() != s2.getAlignment())
			return false;
		if (s1.getBorderBottom() != s2.getBorderBottom())
			return false;
		if (s1.getBorderLeft() != s2.getBorderLeft())
			return false;
		if (s1.getBorderRight() != s2.getBorderRight())
			return false;
		if (s1.getBorderTop() != s2.getBorderTop())
			return false;
		if (s1.getBottomBorderColor() != s2.getBottomBorderColor())
			return false;
		if (s1.getDataFormat() != s2.getDataFormat())
			return false;
		if (s1.getDataFormatString() != s2.getDataFormatString())
			return false;
		if (s1.getFillBackgroundColor() != s2.getFillBackgroundColor())
			return false;
		if (s1.getFillForegroundColor() != s2.getFillForegroundColor())
			return false;
		if (s1.getFillPattern() != s2.getFillPattern())
			return false;
		if (s1.getFontIndex() != s2.getFontIndex())
			return false;
		if (s1.getHidden() != s2.getHidden())
			return false;
		if (s1.getIndention() != s2.getIndention())
			return false;
		if (s1.getLeftBorderColor() != s2.getLeftBorderColor())
			return false;
		if (s1.getRightBorderColor() != s2.getRightBorderColor())
			return false;
		if (s1.getRotation() != s2.getRotation())
			return false;
		if (s1.getTopBorderColor() != s2.getTopBorderColor())
			return false;
		if (s1.getVerticalAlignment() != s2.getVerticalAlignment())
			return false;
		if (s1.getWrapText() != s2.getWrapText())
			return false;
		return true;
	}
}
