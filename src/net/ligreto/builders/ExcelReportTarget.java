package net.ligreto.builders;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.ExcelReportBuilder.OutputFileFormat;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.util.Pair;

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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This is the implementation of the <code>ReportBuilder</code> to produce
 * Microsoft Excel HSSF files using Apache POI library.
 * 
 * There are certain options that could be passed to this implementation. The
 * supported options are:
 * <ul>
 * <li>autoFilter</li>
 * <li>autoSize</li>
 * <li>formatHeader</li>
 * <li>noDataFormat</li>
 * </ul>
 * 
 * @author Julius Stroffek
 * 
 */
public class ExcelReportTarget extends ReportTarget {

	/**
	 * This is a work-around constant that limits the row number in references
	 * to things like auto filter. Apache POI library will fail if the cell
	 * reference contains somehow higher number.
	 */
	protected static int cellReferenceMaxRow = 1000000;

	/**
	 * The information about the target location. It is used for append
	 * operation on the target.
	 */
	protected ExcelReportBuilder.TargetInfo targetInfo;

	/** The output file format. */
	protected OutputFileFormat outputFileFormat = OutputFileFormat.XSSF;

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ExcelReportTarget.class);

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

	/** Specifies the highlight text color for highlighted cells. */
	protected short[] rgbHighlightColor = { 200, 0, 0 };

	/** Specifies the highlight text color for highlighted cells. */
	protected short[] rgbDisabledColor = { 150, 150, 150 };

	/** This is the hash table with all the colors from HSSF color palette. */
	protected Hashtable<String, HSSFColor> hssfColors = null;

	/**
	 * Indicates that the auto-filter should be created once the result is
	 * dumped out.
	 */
	protected boolean autoFilter = false;

	/**
	 * Indicates that the result columns should be auto-sized once the result is
	 * dumped out.
	 */
	protected boolean autoSize = false;

	/**
	 * Indicates that the header row should have style adjustments (background
	 * color; bold fond).
	 */
	protected boolean headerStyle = false;

	/** Indicates whether the date/time cells should be auto formatted. */
	protected boolean noDataFormat = false;

	/** Maximal column width for auto sized columns. */
	protected int maxColumnWidth = 20480;

	/**
	 * Keep the report builder as ExcelReportBuilder as well to avoid type-cast.
	 */
	protected ExcelReportBuilder reportBuilder;

	/** Holds the highest column index used for actual target. */
	int lastColumnIndex = -1;

	/** Indicates whether the cells should be highlighted when applicable. */
	protected boolean highlight;

	protected Map<Pair<OutputStyle, String>, CellStyle> cellStyles = new HashMap<Pair<OutputStyle, String>, CellStyle>(512);
	
	/** Creates the target instance bound to ExcelReportBuilder. */
	public ExcelReportTarget(ExcelReportBuilder reportBuilder, Sheet sheet, int baseRowNumber, int baseColumnPosition) {
		super(reportBuilder);
		this.reportBuilder = reportBuilder;
		this.sheet = sheet;
		this.wb = sheet.getWorkbook();
		this.baseRowNumber = baseRowNumber;
		this.baseColumnPosition = baseColumnPosition;
		actualRowNumber = baseRowNumber - 1;
		actualColumnPosition = baseColumnPosition;
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

	/**
	 * Clones the specified style. First all existing styles are scanned and if
	 * the same style already exists it is re-used. Otherwise new style is
	 * created. This method should be used for style de-duplication. First, you
	 * should alter the already existing style as you need, then call cloneStyle
	 * method and then reverse back your changes on the original style object.
	 * Further, use the style object returned by cloneStyle as an altered style.
	 * 
	 * See the example below:
	 * 
	 * <pre>
	 * style.setFont(newFont);
	 * CellStyle newStyle = cloneStyle(style);
	 * cell.setCellStyle(newStyle);
	 * 
	 * // Revert back the font on the old cell style
	 * style.setFont(font);
	 * </pre>
	 */
	protected CellStyle cloneStyle(CellStyle style) {
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
	 * This function will get the existing <code>Cell</code> object if it
	 * already exists in the file or it will create a new one.
	 * 
	 * @param row
	 *            cell row number
	 * @param col
	 *            cell column number
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
	 * The method called to flush the target - i.e. create auto-filter, etc.
	 */
	protected void flush(boolean lastFlush, boolean increaseSizeOnly) {
		if (lastFlush && autoFilter && lastColumnIndex > baseColumnPosition) {
			if (baseRowNumber < cellReferenceMaxRow) {
				if (actualRowNumber < cellReferenceMaxRow) {
					sheet.setAutoFilter(new CellRangeAddress(baseRowNumber, actualRowNumber, baseColumnPosition, lastColumnIndex));
				} else {
					sheet.setAutoFilter(new CellRangeAddress(baseRowNumber, cellReferenceMaxRow, baseColumnPosition,
							lastColumnIndex));
					log.warn("The cell reference exceeded the allowed range for excel library (Apache POI).");
					log.warn("Auto-filter was made on a smaller row range.");
				}
			} else {
				// There is a bug in POI library that references to too high
				// rows throw exceptions. We will therefore do nothing
				// in such a case.
				log.warn("The cell reference exceeded the allowed range for excel library (Apache POI).");
				log.warn("Auto-filter was therefore disabled.");
			}
		}

		if (autoSize && lastColumnIndex > baseColumnPosition) {
			for (int i = baseColumnPosition; i <= lastColumnIndex; i++) {
				int columnWidth = sheet.getColumnWidth(i);
				sheet.autoSizeColumn(i);
				int newColumnWidth = sheet.getColumnWidth(i) + 1024;
				if (increaseSizeOnly && columnWidth > newColumnWidth) {
					newColumnWidth = columnWidth;
				}
				if (newColumnWidth > maxColumnWidth) {
					newColumnWidth = maxColumnWidth;
				}
				sheet.setColumnWidth(i, newColumnWidth);
			}
		}
	}

	protected void reportExcelStatisctics() {
		log.debug("The number of workbook styles: " + wb.getNumCellStyles());
		log.debug("The number of workbook fonts:" + wb.getNumberOfFonts());
	}

	protected void setBoldFont(Cell cell) {
		CellStyle style = cell.getCellStyle();

		Font font = wb.getFontAt(style.getFontIndex());

		Font newFont = wb.findFont(Font.BOLDWEIGHT_BOLD, font.getColor(), font.getFontHeight(), font.getFontName(),
				font.getItalic(), font.getStrikeout(), font.getTypeOffset(), font.getUnderline());
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
	 * This function will set the font color of the specified cell to the
	 * requested value.
	 * 
	 * The function will make sure that there will be only one <code>Font</code>
	 * object created for the whole file even if the function is called multiple
	 * times for different cells.
	 * 
	 * @param cell
	 *            The cell where to set the font color.
	 * @param rgb
	 *            The new color to set.
	 */
	protected void setCellColor(Cell cell, short[] rgb) {
		switch (outputFileFormat) {
		case HSSF:
			setHSSFCellColor(cell, rgb);
			break;
		case XSSF:
			setHSSFCellColor(cell, rgb);
//			setXSSFCellColor((XSSFCell) cell, rgb);
			break;
		case SXSSF:
			setSXSSFCellColor((SXSSFCell) cell, rgb);
			break;
		default:
			throw new UnimplementedMethodException("Unknown output format for color processing.");
		}
	}

	/**
	 * This function will set the cell style for the header row.
	 * 
	 * The function will make sure that there will be only one <code>Font</code>
	 * object created for the whole file even if the function is called multiple
	 * times for different cells.
	 * 
	 * @param cell
	 *            The cell where to set the font color.
	 * @throws LigretoException 
	 */
	protected void updateCellStyle(Cell cell, OutputStyle outputStyle, String formatString) throws LigretoException {
		switch (outputFileFormat) {
		case HSSF:
			updateHSSFCellStyle(cell, outputStyle, formatString);
			break;
		case XSSF:
			// HSSF approach should work anyway
			updateHSSFCellStyle(cell, outputStyle, formatString);
			break;
		case SXSSF:
			// HSSF approach should work anyway
			updateHSSFCellStyle(cell, outputStyle, formatString);
			break;
		default:
			throw new UnimplementedMethodException("Unknown output format for format processing.");
		}
	}

	protected void setHSSFCellColor(Cell cell, short[] rgb) {
		HSSFWorkbook hwb = (HSSFWorkbook) wb;
		CellStyle style = cell.getCellStyle();

		Font font = wb.getFontAt(style.getFontIndex());

		HSSFColor newColor = hwb.getCustomPalette().findSimilarColor(rgb[0], rgb[1], rgb[2]);

		Font newFont = wb.findFont(font.getBoldweight(), newColor.getIndex(), font.getFontHeight(), font.getFontName(),
				font.getItalic(), font.getStrikeout(), font.getTypeOffset(), font.getUnderline());
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

	protected void setSXSSFCellStyle(SXSSFCell cell, OutputStyle outputStyle) {
		XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();

		// Get the font based on the style
		short boldFont;
		short fillColor;
		short fontColor;
		short cellStyle;
		switch (outputStyle) {
		case TOP_HEADER_DISABLED:
			fillColor = HSSFColor.GREY_25_PERCENT.index;
			fontColor = HSSFColor.GREY_50_PERCENT.index;
			cellStyle = CellStyle.SOLID_FOREGROUND;
			boldFont = Font.BOLDWEIGHT_BOLD;
			break;
		case ROW_HEADER_DISABLED:
			fillColor = HSSFColor.GREY_25_PERCENT.index;
			fontColor = HSSFColor.GREY_50_PERCENT.index;
			cellStyle = CellStyle.SOLID_FOREGROUND;
			boldFont = Font.BOLDWEIGHT_BOLD;
			break;
		case TOP_HEADER:
			fillColor = HSSFColor.GREY_40_PERCENT.index;
			fontColor = HSSFColor.BLACK.index;
			cellStyle = CellStyle.SOLID_FOREGROUND;
			boldFont = Font.BOLDWEIGHT_BOLD;
			break;
		case ROW_HEADER:
			fillColor = HSSFColor.GREY_25_PERCENT.index;
			fontColor = HSSFColor.BLACK.index;
			cellStyle = CellStyle.SOLID_FOREGROUND;
			boldFont = Font.BOLDWEIGHT_BOLD;
			break;
		case DISABLED:
			fillColor = HSSFColor.AUTOMATIC.index;
			fontColor = HSSFColor.GREY_40_PERCENT.index;
			cellStyle = CellStyle.NO_FILL;
			boldFont = Font.BOLDWEIGHT_NORMAL;
			break;
		case HIGHLIGHTED:
			fillColor = HSSFColor.AUTOMATIC.index;
			fontColor = HSSFColor.RED.index;
			cellStyle = CellStyle.NO_FILL;
			boldFont = Font.BOLDWEIGHT_NORMAL;
			break;
		default:
			fillColor = HSSFColor.AUTOMATIC.index;
			fontColor = HSSFColor.BLACK.index;
			cellStyle = CellStyle.NO_FILL;
			boldFont = Font.BOLDWEIGHT_NORMAL;
			break;
		}
		
		CellStyle newStyle = cloneStyle(style);
		Font font = wb.getFontAt(style.getFontIndex());
		Font newFont = wb.createFont();
		newFont.setBoldweight(boldFont);
		newFont.setColor(fontColor);
		newFont.setFontHeight(font.getFontHeight());
		newFont.setFontName(font.getFontName());
		newFont.setItalic(font.getItalic());
		newFont.setStrikeout(font.getStrikeout());
		newFont.setTypeOffset(font.getTypeOffset());
		newFont.setUnderline(font.getUnderline());
		
		newStyle.setFillPattern(cellStyle);
		newStyle.setFillForegroundColor(fillColor);
		cell.setCellStyle(newStyle);
	}

	protected CellStyle createCellStyle(OutputStyle outputStyle, String formatString) throws LigretoException {
		short boldFont;
		short fillColor;
		short fontColor;
		short fillPattern;
		if (outputStyle != null) {
			switch (outputStyle) {
			case TOP_HEADER_DISABLED:
				fillColor = HSSFColor.GREY_25_PERCENT.index;
				fontColor = HSSFColor.GREY_50_PERCENT.index;
				fillPattern = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case ROW_HEADER_DISABLED:
				fillColor = HSSFColor.GREY_25_PERCENT.index;
				fontColor = HSSFColor.GREY_50_PERCENT.index;
				fillPattern = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case TOP_HEADER:
				fillColor = HSSFColor.GREY_40_PERCENT.index;
				fontColor = HSSFColor.BLACK.index;
				fillPattern = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case ROW_HEADER:
				fillColor = HSSFColor.GREY_25_PERCENT.index;
				fontColor = HSSFColor.BLACK.index;
				fillPattern = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case DISABLED:
				fillColor = HSSFColor.AUTOMATIC.index;
				fontColor = HSSFColor.GREY_40_PERCENT.index;
				fillPattern = CellStyle.NO_FILL;
				boldFont = Font.BOLDWEIGHT_NORMAL;
				break;
			case HIGHLIGHTED:
				fillColor = HSSFColor.AUTOMATIC.index;
				fontColor = HSSFColor.RED.index;
				fillPattern = CellStyle.NO_FILL;
				boldFont = Font.BOLDWEIGHT_NORMAL;
				break;
			default:
				fillColor = HSSFColor.AUTOMATIC.index;
				fontColor = HSSFColor.BLACK.index;
				fillPattern = CellStyle.NO_FILL;
				boldFont = Font.BOLDWEIGHT_NORMAL;
				break;
			}
		} else {
			fillColor = HSSFColor.AUTOMATIC.index;
			fontColor = HSSFColor.BLACK.index;
			fillPattern = CellStyle.NO_FILL;
			boldFont = Font.BOLDWEIGHT_NORMAL;

		}
		Font font = wb.createFont();
		font.setBoldweight(boldFont);
		font.setColor(fontColor);
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setFont(font);
		cellStyle.setFillForegroundColor(fillColor);
		cellStyle.setFillPattern(fillPattern);
		if (formatString != null) {
			try {
				cellStyle.setDataFormat(dataFormat.getFormat(formatString));
			} catch (Exception e) {
				throw new LigretoException("Unknown format string: " + formatString, e);
			}
		}
		
		return cellStyle;
	}
	
	protected CellStyle getCellStyle(OutputStyle outputStyle, String formatString) throws LigretoException {
		Pair<OutputStyle, String> pair = new Pair<OutputStyle, String>(outputStyle, formatString);
		CellStyle style = cellStyles.get(pair);
		if (style == null) {
			style = createCellStyle(outputStyle, formatString);
			cellStyles.put(pair, style);
		}
		return style;
	}
	
	protected void updateHSSFCellStyle(Cell cell, OutputStyle outputStyle, String formatString) throws LigretoException {
		CellStyle style = cell.getCellStyle();

		Font font = wb.getFontAt(style.getFontIndex());

		// Get the font based on the style
		short boldFont;
		short fillColor;
		short fontColor;
		short cellStyle;
		if (outputStyle != null) {
			switch (outputStyle) {
			case TOP_HEADER_DISABLED:
				fillColor = HSSFColor.GREY_25_PERCENT.index;
				fontColor = HSSFColor.GREY_50_PERCENT.index;
				cellStyle = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case ROW_HEADER_DISABLED:
				fillColor = HSSFColor.GREY_25_PERCENT.index;
				fontColor = HSSFColor.GREY_50_PERCENT.index;
				cellStyle = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case TOP_HEADER:
				fillColor = HSSFColor.GREY_40_PERCENT.index;
				fontColor = HSSFColor.BLACK.index;
				cellStyle = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case ROW_HEADER:
				fillColor = HSSFColor.GREY_25_PERCENT.index;
				fontColor = HSSFColor.BLACK.index;
				cellStyle = CellStyle.SOLID_FOREGROUND;
				boldFont = Font.BOLDWEIGHT_BOLD;
				break;
			case DISABLED:
				fillColor = HSSFColor.AUTOMATIC.index;
				fontColor = HSSFColor.GREY_40_PERCENT.index;
				cellStyle = CellStyle.NO_FILL;
				boldFont = Font.BOLDWEIGHT_NORMAL;
				break;
			case HIGHLIGHTED:
				fillColor = style.getFillForegroundColor();
				fontColor = HSSFColor.RED.index;
				cellStyle = style.getFillPattern();
				boldFont = font.getBoldweight();
				break;
			default:
				fillColor = style.getFillForegroundColor();
				fontColor = font.getColor();
				cellStyle = style.getFillPattern();
				boldFont = font.getBoldweight();
				break;
			}
		} else {
			fillColor = style.getFillForegroundColor();
			fontColor = font.getColor();
			cellStyle = style.getFillPattern();
			boldFont = font.getBoldweight();			
		}
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
		style.setFont(newFont);
		short oldFillPattern = style.getFillPattern();
		short oldFillColor = style.getFillForegroundColor();
		short oldDataFormat = style.getDataFormat();
		style.setFillPattern(cellStyle);
		style.setFillForegroundColor(fillColor);
		if (formatString != null) {
			try {
				style.setDataFormat(dataFormat.getFormat(formatString));
			} catch (Exception e) {
				throw new LigretoException("Unknown format string: " + formatString, e);
			}
		}
		CellStyle newStyle = cloneStyle(style);
		cell.setCellStyle(newStyle);

		// Revert back the font on the old cell style
		style.setFont(font);
		style.setFillPattern(oldFillPattern);
		style.setFillForegroundColor(oldFillColor);
		style.setDataFormat(oldDataFormat);
	}

	protected void setSXSSFCellColor(SXSSFCell cell, short[] rgb) {
		SXSSFWorkbook swb = (SXSSFWorkbook) wb;
		CellStyle style = cell.getCellStyle();

		Font font = swb.getFontAt(style.getFontIndex());

		// Just a temporary hard coding until SXSSF will implement
		// the proper color setup.
		short newColor = HSSFColor.BLACK.index;
		if (rgb[0] > rgb[1] && rgb[0] > rgb[2]) {
			newColor = HSSFColor.RED.index;
		}

		Font newFont = swb.findFont(font.getBoldweight(), newColor, font.getFontHeight(), font.getFontName(), font.getItalic(),
				font.getStrikeout(), font.getTypeOffset(), font.getUnderline());
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

	protected void setXSSFCellColor(XSSFCell cell, short[] rgb) {
		XSSFWorkbook twb = (XSSFWorkbook) wb;
		XSSFCellStyle style = cell.getCellStyle();
		XSSFFont font = style.getFont();
		XSSFFont newFont = null;
		XSSFColor newColor = new XSSFColor(new java.awt.Color(rgb[0], rgb[1], rgb[2]));

		log.debug("Setting color of cell c: " + cell.getColumnIndex() + " r: " + cell.getRowIndex() + " color: "
				+ newColor.getIndexed());

		// Look if the font already exists
		newFont = twb.findFont(font.getBoldweight(), newColor.getIndexed(), font.getFontHeight(), font.getFontName(),
				font.getItalic(), font.getStrikeout(), font.getTypeOffset(), font.getUnderline());

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

	@Override
	public void dumpCell(int i, Object value, OutputFormat outputFormat, OutputStyle outputStyle) throws LigretoException {
		String dataFormat = null;
		Cell cell = createCell(row, actualColumnPosition + columnStep * i);
		if (value == null) {
			cell.setCellValue(ligretoParameters.getNullString());
			dataFormat = ligretoParameters.getExcelStringFormat();
		} else if (value instanceof String){
			cell.setCellValue((String)value);
			dataFormat = ligretoParameters.getExcelStringFormat();
		} else if (value instanceof Integer) {
			dataFormat = ligretoParameters.getExcelIntegerFormat();
			cell.setCellValue(((Integer) value).intValue());
		} else if (value instanceof Long) {
			dataFormat = ligretoParameters.getExcelIntegerFormat();
			cell.setCellValue(((Long) value).longValue());
		} else if (value instanceof Double) {
			double dVal = (Double) value;
			dataFormat = ligretoParameters.getExcelFloatFormat();
			if (Double.isNaN(dVal)) {
				cell.setCellValue(ligretoParameters.getNanString());
			} else {
				cell.setCellValue(dVal);
			}
		} else if (value instanceof Float) {
			float fVal = (Float) value;
			dataFormat = ligretoParameters.getExcelFloatFormat();
			if (Float.isNaN(fVal)) {
				cell.setCellValue(ligretoParameters.getNanString());
			} else {
				cell.setCellValue(fVal);
			}
		} else if (value instanceof BigDecimal) {
			BigDecimal bdVal = (BigDecimal) value;
			dataFormat = ligretoParameters.getExcelBigDecimalFormat();
			if (Double.isNaN(bdVal.doubleValue())) {
				cell.setCellValue(ligretoParameters.getNanString());
			} else if (!noDataFormat && dataFormat != null && !"@".equals(dataFormat)) {
				cell.setCellValue(((BigDecimal) value).doubleValue());
			} else {
				cell.setCellValue(((BigDecimal) value).toString());
			}
		} else if (value instanceof Date) {
			dataFormat = ligretoParameters.getExcelDateFormat();
			cell.setCellValue(((Date) value));
		} else if (value instanceof Timestamp) {
			dataFormat = ligretoParameters.getExcelTimestampFormat();
			cell.setCellValue(((Timestamp) value));
		} else if (value instanceof Time) {
			dataFormat = ligretoParameters.getExcelTimeFormat();
			cell.setCellValue(((Time) value));
		} else {
			cell.setCellValue(value.toString());
			dataFormat = ligretoParameters.getExcelStringFormat();
		}

		if (noDataFormat) {
			dataFormat = null;
		}
		
		if (outputStyle != OutputStyle.HIGHLIGHTED && !headerStyle) {
			outputStyle = null;
		}

		if (outputStyle == OutputStyle.HIGHLIGHTED && !highlight) {
			outputStyle = null;
		}

		// Set up the desired output format given internally (overrides the
		// above formats)
		switch (outputFormat) {
		case DEFAULT:
			break;
		case PERCENTAGE_NO_DECIMAL_DIGITS:
			dataFormat = "0%";
			break;
		case PERCENTAGE_2_DECIMAL_DIGITS:
			dataFormat = "0.00%";
			break;
		case PERCENTAGE_3_DECIMAL_DIGITS:
			dataFormat = "0.000%";
			break;
		default:
			throw new RuntimeException("Unexpected value of CellFormat enumeration.");
		}

		if (reportBuilder.hasTemplate()) {
			updateCellStyle(cell, outputStyle, dataFormat);
		} else if (outputStyle != OutputStyle.DEFAULT || dataFormat != null) {
			cell.setCellStyle(getCellStyle(outputStyle, dataFormat));
		}
	}

	@Override
	public void finish() throws IOException {
		flush(true, false);
		targetInfo.lastRow = actualRowNumber;
		targetInfo.inUse = false;
	}

	/**
	 * @return the dataFormat
	 */
	public DataFormat getDataFormat() {
		return dataFormat;
	}

	/**
	 * @return the targetInfo
	 */
	public ExcelReportBuilder.TargetInfo getTargetInfo() {
		return targetInfo;
	}

	/**
	 * @return the autoFilter
	 */
	public boolean isAutoFilter() {
		return autoFilter;
	}

	/**
	 * @return the autoSize
	 */
	public boolean isAutoSize() {
		return autoSize;
	}

	/**
	 * @return the headerStyle
	 */
	public boolean isHeaderStyle() {
		return headerStyle;
	}

	/**
	 * @return the noDataFormat
	 */
	public boolean isNoDataFormat() {
		return noDataFormat;
	}

	@Override
	public void nextRow() throws IOException {
		super.nextRow();
		row = sheet.getRow(actualRowNumber);
		if (row == null)
			row = sheet.createRow(actualRowNumber);
	}

	@Override
	public void setHighlightColor(short[] rgbHighlightColor) {
		this.rgbHighlightColor = rgbHighlightColor;
	}

	/**
	 * @param autoFilter
	 *            the autoFilter to set
	 */
	public void setAutoFilter(boolean autoFilter) {
		this.autoFilter = autoFilter;
	}

	/**
	 * @param autoSize
	 *            the autoSize to set
	 */
	public void setAutoSize(boolean autoSize) {
		this.autoSize = autoSize;
	}

	/**
	 * @param dataFormat
	 *            the dataFormat to set
	 */
	public void setDataFormat(DataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}

	/**
	 * @param headerStyle
	 *            the headerStyle to set
	 */
	public void setHeaderStyle(boolean headerStyle) {
		this.headerStyle = headerStyle;
	}

	/**
	 * @param noDataFormat
	 *            the noDataFormat to set
	 */
	public void setNoDataFormat(boolean noDataFormat) {
		this.noDataFormat = noDataFormat;
	}

	/**
	 * @param targetInfo
	 *            the targetInfo to set
	 * @throws TargetException
	 */
	public void setTargetInfo(ExcelReportBuilder.TargetInfo targetInfo) throws TargetException {
		if (targetInfo.inUse) {
			throw new TargetException("Target \"" + targetInfo.name + "\" already in use.");
		}
		this.targetInfo = targetInfo;
		this.targetInfo.inUse = true;
	}

	@Override
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}
}
