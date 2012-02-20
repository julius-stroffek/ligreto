package net.ligreto.builders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.UnimplementedMethodException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This is the implementation of the <code>ReportBuilder</code> to produce
 * Microsoft Excel HSSF files using Apache POI library.
 * 
 * There are certain options that could be passed to this implementation. The supported
 * options are:
 * <ul>
 *   <li>autoFilter</li>
 *   <li>autoSize</li>
 *   <li>formatHeader</li>
 *   <li>noDataFormat</li>
 * </ul>
 * 
 * @author Julius Stroffek
 *
 */
public class ExcelReportBuilder extends ReportBuilder {

	/**
	 * This class represents the target in the result spread sheet
	 * which is than used for hashing to associate any information
	 * with the corresponding target.
	 */
	protected class TargetInfo {
		int sheet;
		int row;
		int column;
		int lastRow;
		boolean inUse;
		String name;
		
		public TargetInfo(String name, int sheet, int row, int column) {
			this.name = name;
			this.sheet = sheet;
			this.row = row;
			this.column = column;
			this.lastRow = this.row - 1;
			this.inUse = false;
		}
		
		@Override
		public int hashCode() {
			return sheet + row + column;
		}
		
		@Override
		public boolean equals(Object object) {
			if (object instanceof TargetInfo) {
				TargetInfo info = (TargetInfo) object;
				return sheet == info.sheet && row == info.row && column == info.column;
			}
			return false;
		}
	}
	
	/**
	 * The hash map that stores the last free row number for the specified target.
	 * It is used for append operation on the target.
	 */
	HashMap<TargetInfo, TargetInfo> targetMap = new HashMap<TargetInfo, TargetInfo>();
	
	/** The output file type enumeration. */
	protected enum OutputFormat {HSSF, XSSF, SXSSF};
	
	/** The output file format. */
	protected OutputFormat outputFormat = OutputFormat.XSSF;

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ExcelReportBuilder.class);
	
	/** The output stream where the output file will be written to. */
	protected FileOutputStream out;
	
	/** The <code>Workbook</code> object for the output report. */
	protected Workbook wb;
	
	/** The workbook data format instance. */
	protected DataFormat dataFormat;
	
	/** The <code>Sheet</code> object where the result is placed. */
	protected Sheet sheet;
	
	/** The actual row where the processed result row is stored. */
	protected Row row;
	
	/** This is the hash table with all the colors from HSSF color palette. */
	protected Hashtable<String,HSSFColor> hssfColors = null;
	
	/** Indicates that the auto-filter should be created once the result is dumped out. */
	protected boolean autoFilter = false;
	
	/** Indicates that the result columns should be auto-sized once the result is dumped out. */
	protected boolean autoSize = false;
	
	/** Indicates that the header row should have style adjustments (background color; bold fond). */
	protected boolean headerStyle = false;
	
	/** Indicates whether the date/time cells should be auto formatted. */
	protected boolean noDataFormat = false;
	
	/** Maximal column width for auto sized columns. */
	protected int maxColumnWidth = 20480; 
	
	/** Holds the highest column index used for actual target. */
	int lastColumnIndex = -1;
	
	/**
	 * @return the autoFilter
	 */
	public boolean isAutoFilter() {
		return autoFilter;
	}

	/**
	 * @param autoFilter the autoFilter to set
	 */
	public void setAutoFilter(boolean autoFilter) {
		this.autoFilter = autoFilter;
	}

	/**
	 * @return the autoSize
	 */
	public boolean isAutoSize() {
		return autoSize;
	}

	/**
	 * @param autoSize the autoSize to set
	 */
	public void setAutoSize(boolean autoSize) {
		this.autoSize = autoSize;
	}

	/**
	 * @return the headerStyle
	 */
	public boolean isHeaderStyle() {
		return headerStyle;
	}

	/**
	 * @param headerStyle the headerStyle to set
	 */
	public void setHeaderStyle(boolean headerStyle) {
		this.headerStyle = headerStyle;
	}

	/**
	 * @return the noDataFormat
	 */
	public boolean isNoDataFormat() {
		return noDataFormat;
	}

	/**
	 * @param noDataFormat the noDataFormat to set
	 */
	public void setNoDataFormat(boolean noDataFormat) {
		this.noDataFormat = noDataFormat;
	}

	/**
	 * This method will create the target builder.
	 * 
	 * @param sheet
	 * @param baseRow
	 * @param baseCol
	 * @return The newly create target object.
	 */
	@Override
	protected ExcelReportTarget createTarget(Sheet sheet, int baseRow, int baseCol) {
		ExcelReportTarget newTarget = new ExcelReportTarget(this, sheet, baseRow, baseCol);
		newTarget.setAutoFilter(isAutoFilter());
		newTarget.setAutoSize(isAutoSize());
		newTarget.setHeaderStyle(isHeaderStyle());
		newTarget.setNoDataFormat(isNoDataFormat());
		newTarget.setLigretoParameters(ligretoParameters);
		newTarget.setDataFormat(dataFormat);
		newTarget.outputFormat = outputFormat;
		return newTarget;
	}
	
	@Override
	public  TargetInterface getTargetBuilder(String target, boolean append) throws TargetException {
		ExcelReportTarget newTarget = null;
		CellReference ref = new CellReference(target);
		Sheet sheet;
		if (ref.getSheetName() != null) {
			sheet = wb.getSheet(ref.getSheetName());
			if (sheet == null) {
				sheet = wb.createSheet(ref.getSheetName());
			}
		} else {
			if (this.sheet == null) {
				this.sheet = wb.createSheet("Sheet1");
			}
			sheet = this.sheet;
		}
		int rowNum = ref.getRow();
		int colNum = ref.getCol();
		if (sheet != null) {
			TargetInfo info = new TargetInfo(target, wb.getSheetIndex(sheet), rowNum, colNum);
			newTarget = createTarget(sheet, rowNum, colNum);
			TargetInfo prevInfo = targetMap.get(info);
			if (append &&  prevInfo != null) {
				newTarget.setActRow(prevInfo.lastRow);
				log.info(
						"Moved from the specified target row \""
						+ prevInfo.row
						+ "\" to next row \""
						+ (prevInfo.lastRow + 1)
						+ "\" due to append."
					);
				info = prevInfo;
			}			
			newTarget.setTargetInfo(info);
			targetMap.put(info, info);
		} else {
			throw new InvalidTargetException("The target reference is invalid: \"" + target + "\"");
		}
		return newTarget;
	}
	
	/**
	 * This function will get the existing <code>Cell</code>
	 * object if it already exists in the file or it will
	 * create a new one.
	 * 
	 * @param row cell row number
	 * @param col cell column number
	 * @return The <code>Cell</code> object. 
	 */
	protected Cell createCell(Row row, int col) {
		Cell cell = row.getCell(col);
		if (cell == null)
			cell = row.createCell(col);
		
		// Manage the last column index
		if (col > lastColumnIndex) {
			lastColumnIndex = col;
		}
		
		return cell;
	}
		
	/**
	 * This function will set the cell style for the header row.
	 * 
	 * The function will make sure that there will be only one
	 * <code>Font</code> object created for the whole file
	 * even if the function is called multiple times for different
	 * cells.
	 * 
	 * @param cell The cell where to set the font color.
	 */
	protected void setHeaderStyle(Cell cell, HeaderType headerType) {
		switch (outputFormat) {
		case HSSF:
			setHSSFHeaderStyle(cell, headerType);
			break;
		case XSSF:
			// HSSF approach should work anyway
			setHSSFHeaderStyle(cell, headerType);
			break;
		case SXSSF:
			// HSSF approach should work anyway
			setHSSFHeaderStyle(cell, headerType);
			break;
		default:
			throw new UnimplementedMethodException("Unknown output format for format processing.");
		}
	}
	
	protected void setHSSFHeaderStyle(Cell cell, HeaderType headerType) {
		CellStyle style = cell.getCellStyle();
		
		Font font = wb.getFontAt(style.getFontIndex());
		
		Font newFont = wb.findFont(
				Font.BOLDWEIGHT_BOLD,
				font.getColor(),
				font.getFontHeight(),
				font.getFontName(),
				font.getItalic(),
				font.getStrikeout(),
				font.getTypeOffset(),
				font.getUnderline()
		);
		if (newFont == null) {
			newFont = wb.createFont();
			newFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			newFont.setColor(font.getColor());
			newFont.setFontHeight(font.getFontHeight());
			newFont.setFontName(font.getFontName());
			newFont.setItalic(font.getItalic());
			newFont.setStrikeout(font.getStrikeout());
			newFont.setTypeOffset(font.getTypeOffset());
			newFont.setUnderline(font.getUnderline());
		}
		style.setFont(newFont);
		short fillPattern = style.getFillPattern();
		short fillColor = style.getFillForegroundColor();
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		switch (headerType) {
		case TOP:
			style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
			break;
		case ROW:
			style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
			break;
		default:
			throw new RuntimeException("Unexpected value for HeaderType enumeration.");
		}
		CellStyle newStyle = cloneStyle(style);
		cell.setCellStyle(newStyle);
		
		// Revert back the font on the old cell style
		style.setFont(font);
		style.setFillPattern(fillPattern);
		style.setFillForegroundColor(fillColor);
	}
	
	protected void setBoldFont(Cell cell) {
		CellStyle style = cell.getCellStyle();
		
		Font font = wb.getFontAt(style.getFontIndex());
		
		Font newFont = wb.findFont(
				Font.BOLDWEIGHT_BOLD,
				font.getColor(),
				font.getFontHeight(),
				font.getFontName(),
				font.getItalic(),
				font.getStrikeout(),
				font.getTypeOffset(),
				font.getUnderline()
		);
		if (newFont == null) {
			newFont = wb.createFont();
			newFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			newFont.setColor(font.getColor());
			newFont.setFontHeight(font.getFontHeight());
			newFont.setFontName(font.getFontName());
			newFont.setItalic(font.getItalic());
			newFont.setStrikeout(font.getStrikeout());
			newFont.setTypeOffset(font.getTypeOffset());
			newFont.setUnderline(font.getUnderline());
		}
		style.setFont(newFont);
		CellStyle newStyle = cloneStyle(style);
		cell.setCellStyle(newStyle);
		
		// Revert back the font on the old cell style
		style.setFont(font);
	}

	/**
	 * This function will set the font color of the specified cell
	 * to the requested value. 
	 * 
	 * The function will make sure that there will be only one
	 * <code>Font</code> object created for the whole file
	 * even if the function is called multiple times for different
	 * cells.
	 * 
	 * @param cell The cell where to set the font color.
	 * @param rgb The new color to set.
	 */
	protected void setCellColor(Cell cell, short[] rgb) {
		switch (outputFormat) {
		case HSSF:
			setHSSFCellColor(cell, rgb);
			break;
		case XSSF:
			setXSSFCellColor((XSSFCell)cell, rgb);
			break;
		case SXSSF:
			setSXSSFCellColor((SXSSFCell)cell, rgb);
			break;
		default:
			throw new UnimplementedMethodException("Unknown output format for color processing.");
		}
	}

	protected void setHSSFCellColor(Cell cell, short[] rgb) {
		HSSFWorkbook hwb = (HSSFWorkbook) wb;
		CellStyle style = cell.getCellStyle();
		
		Font font = wb.getFontAt(style.getFontIndex());

		HSSFColor newColor = hwb.getCustomPalette().findSimilarColor(rgb[0], rgb[1], rgb[2]);
		
		Font newFont = wb.findFont(
				font.getBoldweight(),
				newColor.getIndex(),
				font.getFontHeight(),
				font.getFontName(),
				font.getItalic(),
				font.getStrikeout(),
				font.getTypeOffset(),
				font.getUnderline()
		);
		if (newFont == null) {
			newFont = wb.createFont();
			newFont.setBoldweight(font.getBoldweight());
			newFont.setColor(newColor.getIndex());
			newFont.setFontHeight(font.getFontHeight());
			newFont.setFontName(font.getFontName());
			newFont.setItalic(font.getItalic());
			newFont.setStrikeout(font.getStrikeout());
			newFont.setTypeOffset(font.getTypeOffset());
			newFont.setUnderline(font.getUnderline());
		}
		style.setFont(newFont);
		CellStyle newStyle = cloneStyle(style);
		cell.setCellStyle(newStyle);
		
		// Revert back the font on the old cell style
		style.setFont(font);
	}
	
	protected void setXSSFCellColor(XSSFCell cell, short[] rgb) {
		XSSFWorkbook twb = (XSSFWorkbook) wb;
		XSSFCellStyle style = cell.getCellStyle();
		XSSFFont font = style.getFont();
		XSSFFont newFont = null;
		XSSFColor newColor = new XSSFColor(new java.awt.Color(rgb[0], rgb[1], rgb[2]));
		
		log.debug("Setting color of cell c: " + cell.getColumnIndex() + " r: " + cell.getRowIndex() + " color: " + newColor.getIndexed());
				
		// Look if the font already exists
		newFont = twb.findFont(
			font.getBoldweight(),
			newColor.getIndexed(),
			font.getFontHeight(),
			font.getFontName(),
			font.getItalic(),
			font.getStrikeout(),
			font.getTypeOffset(),
			font.getUnderline()
		);
		
		// Clone the font if it does not exist yet
		if (newFont == null || !newFont.getXSSFColor().equals(newColor)) {
		    newFont = twb.createFont();
			newFont.setBoldweight(font.getBoldweight());
			newFont.setColor(new XSSFColor(new java.awt.Color(rgb[0], rgb[1], rgb[2])));
			newFont.setFontHeight(font.getFontHeight());
			newFont.setFontName(font.getFontName());
			newFont.setItalic(font.getItalic());
			newFont.setStrikeout(font.getStrikeout());
			newFont.setTypeOffset(font.getTypeOffset());
			newFont.setUnderline(font.getUnderline());
		}
		style.setFont(newFont);
		CellStyle newStyle = cloneStyle(style);
		cell.setCellStyle(newStyle);
		
		// Revert back the font on the old cell style
		style.setFont(font);
	}
	
	protected void setSXSSFCellColor(Cell cell, short[] rgb) {
		CellStyle style = cell.getCellStyle();
			
		Font font = wb.getFontAt(style.getFontIndex());

		// Just a temporary hard coding until SXSSF will implement
		// the proper color setup.
		short newColor = HSSFColor.BLACK.index;
		if (rgb[0] > rgb[1] && rgb[0] > rgb[2]) {
			newColor = HSSFColor.RED.index;
		}
			
		Font newFont = wb.findFont(
				font.getBoldweight(),
				newColor,
				font.getFontHeight(),
				font.getFontName(),
				font.getItalic(),
				font.getStrikeout(),
				font.getTypeOffset(),
				font.getUnderline()
		);
		if (newFont == null) {
			newFont = wb.createFont();
			newFont.setBoldweight(font.getBoldweight());
			newFont.setColor(newColor);
			newFont.setFontHeight(font.getFontHeight());
			newFont.setFontName(font.getFontName());
			newFont.setItalic(font.getItalic());
			newFont.setStrikeout(font.getStrikeout());
			newFont.setTypeOffset(font.getTypeOffset());
			newFont.setUnderline(font.getUnderline());
		}
		style.setFont(newFont);
		CellStyle newStyle = cloneStyle(style);
		cell.setCellStyle(newStyle);
		
		// Revert back the font on the old cell style
		style.setFont(font);
	}

	/**
	 *  Clones the specified style. First all existing styles are scanned and if the
	 *  same style already exists it is re-used. Otherwise new style is created. This
	 *  method should be used for style de-duplication. First, you should alter the
	 *  already existing style as you need, then call cloneStyle method and then
	 *  reverse back your changes on the original style object. Further, use the style
	 *  object returned by cloneStyle as an altered style.
	 *  
	 *  See the example below:
	 *  <pre>
	 *  style.setFont(newFont);
	 *	CellStyle newStyle = cloneStyle(style);
	 *	cell.setCellStyle(newStyle);
	 *	
	 *	// Revert back the font on the old cell style
	 *	style.setFont(font);
	 *  </pre>
	 */
	protected CellStyle cloneStyle(CellStyle style) {
		// Go through all the existing styles and re-use it if there is a match
		for (short i=0; i < wb.getNumCellStyles(); i++) {
			// Do not compare the style to itself
			if (style.getIndex() == i)
				continue;
			// Compare the styles and use the already existing one instead of creating a new one
			if (compareStyles(style, wb.getCellStyleAt(i))) {
				return wb.getCellStyleAt(i);
			}
		}
		
		// Create a new style since the same one does not exist
		CellStyle newStyle = wb.createCellStyle();
		newStyle.cloneStyleFrom(style);
		
		return newStyle;
	}
	
	private boolean compareStyles(CellStyle s1, CellStyle s2) {
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

	protected void reportExcelStatisctics() {
		log.debug("The number of workbook styles: " + wb.getNumCellStyles());
		log.debug("The number of workbook fonts:" + wb.getNumberOfFonts());
	}
	
	@Override
	public void writeOutput() throws IOException {		
		reportExcelStatisctics();
		log.info("Writing the result into the file: " + output);
		wb.write(out);
	}

	@Override
	public void start() throws IOException, LigretoException {
		out = new FileOutputStream(output);

		// Alter the output format if necessary
		if (System.getProperty("excel97") != null) {
			outputFormat = OutputFormat.HSSF;
		}		
		
		// Read the template file if the template was specified
		if (template != null) {
			log.info("Reading a template file: " + template);
			switch (outputFormat) {
			case HSSF:
				wb = new HSSFWorkbook(new FileInputStream(template));
				hssfColors = HSSFColor.getTripletHash();
				break;
			case XSSF:
				wb = new XSSFWorkbook(new FileInputStream(template));
				break;
			}
		} else {
			log.info("Creating the empty workbook.");
			switch (outputFormat) {
			case HSSF:
				wb = new HSSFWorkbook();
				hssfColors = HSSFColor.getTripletHash();
				break;
			case XSSF:
				wb = new XSSFWorkbook();
				break;
			}
		}
		
		// Create the data format object
		dataFormat = wb.createDataFormat();
		
		if (wb.getNumberOfSheets() > 0) {
			sheet = wb.getSheetAt(wb.getActiveSheetIndex());
		} else {
			sheet = null;
		}
		reportExcelStatisctics();
		log.info("The output will be written to \"" + output + "\".");
	}
	
	@Override
	public void setOptions(Iterable<String> options) throws LigretoException {
		for (String o : options) {
			if ("autoFilter".equals(o)) {
				autoFilter = true;
			} else if ("autoSize".equals(o)) {
				autoSize = true;
			} else if ("headerStyle".equals(o)) {	
				headerStyle = true;
			} else if ("noDataFormat".equals(o)) {
				noDataFormat = true;
			} else {
				throw new LigretoException("Unsupported option specified: '" + o + "'");
			}
		}
	}
}
