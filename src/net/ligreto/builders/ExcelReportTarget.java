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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

/**
 * This is the implementation of the {@link TargetInterface} to produce
 * Microsoft Excel HSSF/XSSF files using Apache POI library.
 * 
 * @author Julius Stroffek
 * @see ExcelReportBuilder
 * 
 */
public class ExcelReportTarget extends ReportTarget {

	/**
	 * This is a work-around constant that limits the row number in references
	 * to functionality like auto filter. Apache POI library will fail if the cell
	 * reference contains higher number.
	 */
	protected static int cellReferenceMaxRow = 1000000;

	/**
	 * The information about the target location that is maintained in the report builder.
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

	/**
	 * The hash map associating the requested {@link OutputStyle} and data format with the spreadsheet
	 * style. This is used for speed optimization when using styles.
	 */
	protected Map<Pair<OutputStyle, String>, CellStyle> cellStyles = new HashMap<Pair<OutputStyle, String>, CellStyle>(512);
	
	/**
	 * Creates the target instance bound to ExcelReportBuilder.
	 * 
	 * @param reportBuilder the parent report builder
	 * @param sheet the sheet where the output is dumped
	 * @param baseRowNumber the row number where the dumps starts
	 * @param baseColumnPosition the column number where the dump starts
	 */
	public ExcelReportTarget(ExcelReportBuilder reportBuilder, Sheet sheet, int baseRowNumber, int baseColumnPosition) {
		super(reportBuilder);
		this.reportBuilder = reportBuilder;
		this.sheet = sheet;
		this.wb = sheet.getWorkbook();
		this.baseRowNumber = baseRowNumber;
		this.baseColumnPosition = baseColumnPosition;
		currentRowNumber = baseRowNumber - 1;
		currentColumnPosition = baseColumnPosition;
	}

	/**
	 * This function will get the existing {@code Cell} object if it
	 * already exists in the file or it will create a new one.
	 * 
	 * @param row the row to get a cell
	 * @param col column number
	 * @return the created or already existing {@code Cell} object
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
	 * The method called to flush the target. It auto-sizes the columns, creates the auto-filter, etc.
	 * 
	 * @param lastFlush indicates whether this is the last call on the target
	 * @param increaseSizeOnly indicates whether the size should be only increased
	 */
	protected void flush(boolean lastFlush, boolean increaseSizeOnly) {
		if (lastFlush && autoFilter && lastColumnIndex > baseColumnPosition) {
			if (baseRowNumber < cellReferenceMaxRow) {
				if (currentRowNumber < cellReferenceMaxRow) {
					sheet.setAutoFilter(new CellRangeAddress(baseRowNumber, currentRowNumber, baseColumnPosition, lastColumnIndex));
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

	/**
	 * Report the number of styles, fonts and similar characteristics of the excel file. This
	 * information is dumped as debug message into the log.
	 */
	protected void reportExcelStatisctics() {
		log.debug("The number of workbook styles: " + wb.getNumCellStyles());
		log.debug("The number of workbook fonts:" + wb.getNumberOfFonts());
	}

	/**
	 * This function will set the cell style to the specified output style.
	 * 
	 * The function will make sure that there will be only one <code>Font</code>
	 * object created for the whole file even if the function is called multiple
	 * times for different cells.
	 * 
	 * @param cell the cell where the style should be updated
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

	/**
	 * Set the specified color for {code HSSFCell} like cell object.
	 * 
	 * @param cell the cell to set the color
	 * @param rgb the color to set
	 */
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
		CellStyle newStyle = PoiUtils.findOrCreateStyle(wb, style);
		cell.setCellStyle(newStyle);

		// Revert back the font on the old cell style
		style.setFont(font);
	}

	/**
	 * Set the cell style for the specified cell. If the style already exists, it is
	 * used instead of creating a new style.
	 * 
	 * @param cell the cell to set the style
	 * @param outputStyle the style type to set
	 */
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
		
		CellStyle newStyle = PoiUtils.findOrCreateStyle(wb, style);
		Font newFont = PoiUtils.cloneFont(wb, wb.getFontAt(style.getFontIndex()));
		newFont.setBoldweight(boldFont);
		newFont.setColor(fontColor);
		
		newStyle.setFillPattern(cellStyle);
		newStyle.setFillForegroundColor(fillColor);
		cell.setCellStyle(newStyle);
	}

	/**
	 * This method will create a new cell style for the specified style type.
	 * 
	 * @param outputStyle the style type
	 * @param formatString the data format
	 * @return the newly created cell style object
	 * @throws LigretoException if there was an error creating the style
	 */
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
	
	/**
	 * This method will look for the map of the already created cell styles and if the style
	 * does not yet exist new one will get created.
	 * 
	 * @param outputStyle the desired cell style
	 * @param formatString the data format string
	 * @return the cell style matching the style type and data format
	 * @throws LigretoException if there was an error creating the style
	 */
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
		Font newFont = PoiUtils.findOrCreateFont(wb, font, boldFont, fontColor);
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
		CellStyle newStyle = PoiUtils.findOrCreateStyle(wb, style);
		cell.setCellStyle(newStyle);

		// Revert back the font on the old cell style
		style.setFont(font);
		style.setFillPattern(oldFillPattern);
		style.setFillForegroundColor(oldFillColor);
		style.setDataFormat(oldDataFormat);
	}

	@Override
	public void dumpCell(int i, Object value, OutputFormat outputFormat, OutputStyle outputStyle) throws LigretoException {
		String dataFormat = null;
		Cell cell = createCell(row, currentColumnPosition + columnStep * i);
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
		targetInfo.lastRow = currentRowNumber;
		targetInfo.inUse = false;
	}

	/**
	 * Return the workbook's data format object.
	 * 
	 * @return the dataFormat
	 */
	public DataFormat getDataFormat() {
		return dataFormat;
	}

	/**
	 * Return the target info.
	 * 
	 * @return the target information structure
	 */
	public ExcelReportBuilder.TargetInfo getTargetInfo() {
		return targetInfo;
	}

	/**
	 * Indicates whether auto-filter will be created.
	 * 
	 * @return the true if auto-filter will get created
	 */
	public boolean isAutoFilter() {
		return autoFilter;
	}

	/**
	 * Indicates whether columns will be auto-sized.
	 * 
	 * @return the true if the columns will get auto-sized
	 */
	public boolean isAutoSize() {
		return autoSize;
	}

	/**
	 * Indicates whether cell style will be applied to header cells.
	 * 
	 * @return the true if the cell style will be applied to header cells
	 */
	public boolean isHeaderStyle() {
		return headerStyle;
	}

	/**
	 * Indicates whether data format will be set based on the data type.
	 * 
	 * @return the true if data format will not be set
	 */
	public boolean isNoDataFormat() {
		return noDataFormat;
	}

	@Override
	public void nextRow() throws IOException {
		super.nextRow();
		row = sheet.getRow(currentRowNumber);
		if (row == null)
			row = sheet.createRow(currentRowNumber);
	}

	@Override
	public void setHighlightColor(short[] rgbHighlightColor) {
		this.rgbHighlightColor = rgbHighlightColor;
	}

	/**
	 * Set whether auto-filter should be created.
	 * 
	 * @param autoFilter true if auto-filter should be created
	 */
	public void setAutoFilter(boolean autoFilter) {
		this.autoFilter = autoFilter;
	}

	/**
	 * Set whether columns should be auto-sized.
	 *
	 * @param autoSize true if the columns should be auto-sized
	 */
	public void setAutoSize(boolean autoSize) {
		this.autoSize = autoSize;
	}

	/**
	 * Set data format maintaining the data formats for the whole workbook.
	 * 
	 * @param dataFormat the data format to set
	 */
	public void setDataFormat(DataFormat dataFormat) {
		this.dataFormat = dataFormat;
	}

	/**
	 * Set whether cell style should be adjusted for the header cells.
	 * 
	 * @param headerStyle true if the style should be adjusted for the header cells
	 */
	public void setHeaderStyle(boolean headerStyle) {
		this.headerStyle = headerStyle;
	}

	/**
	 * Set whether cells should have format set based on the data type.
	 * 
	 * @param noDataFormat true if the data format should not be set
	 */
	public void setNoDataFormat(boolean noDataFormat) {
		this.noDataFormat = noDataFormat;
	}

	/**
	 * Set the target info used to manage the position for the target.
	 * 
	 * @param targetInfo the target info to set
	 * @throws TargetException if there was a problem with the target
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
