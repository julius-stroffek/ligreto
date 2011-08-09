package net.ligreto.builders;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.Hashtable;

import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.UnimplementedMethodException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This is the implementation of the <code>ReportBuilder</code> to produce
 * Microsoft Excel HSSF files using Apache POI library.
 * 
 * @author Julius Stroffek
 *
 */
public class ExcelReportBuilder extends ReportBuilder {

	/** The output file type enumeration. */
	protected enum OutputFormat {HSSF, XSSF};
	
	/** The output file format. */
	protected OutputFormat outputFormat = OutputFormat.XSSF;

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ExcelReportBuilder.class);
	
	/** The output stream where the output file will be written to. */
	protected FileOutputStream out;
	
	/** The <code>Workbook</code> object for the output report. */
	protected Workbook wb;
	
	/** The <code>Sheet</code> object where the result is placed. */
	protected Sheet sheet;
	
	/** The actual row where the processed result row is stored. */
	protected Row row;
	
	/** This is the hash table with all the colors from HSSF color palette. */
	protected Hashtable<String,HSSFColor> hssfColors = null;
	
	@Override
	public void setTarget(String target, boolean append) throws InvalidTargetException {
		CellReference ref = new CellReference(target);
		Sheet sheet;
		if (ref.getSheetName() != null) {
			sheet = wb.getSheet(ref.getSheetName());
			if (sheet == null) {
				sheet = wb.createSheet(ref.getSheetName());
			}
		} else {
			sheet = this.sheet;
		}
		int rowNum = ref.getRow();
		int colNum = ref.getCol();
		if (sheet != null) {
			this.sheet = sheet;
			this.baseRow = rowNum;
			this.baseCol = colNum;
			actRow = baseRow-1;
			actCol = baseCol;
			if (append) {
				Row row = sheet.getRow(actRow+1);
				while (append && row != null) {
					actRow++;
					row = sheet.getRow(actRow+1);
				}
				if (actRow != baseRow-1) {
					log.info(
						"Moved from the specified target row \""
						+ baseRow
						+ "\" to nearest empty row \""
						+ (actRow+1)
						+ "\" due to append."
					);
				}
			}
		} else {
			throw new InvalidTargetException("The target reference is invalid: \"" + target + "\"");
		}
	}

	@Override
	public void nextRow() {
		super.nextRow();
		row = sheet.getRow(actRow);
		if (row == null)
			row = sheet.createRow(actRow);
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
		return cell;
	}

	@Override
	public void setColumn(int i, Object o, short[] rgb) {
		Cell cell = createCell(row, actCol + i);
		if (o instanceof Integer)
			cell.setCellValue(((Integer)o).intValue());
		else if (o instanceof Long)
			cell.setCellValue(((Long)o).longValue());
		else if (o instanceof Double)
			cell.setCellValue(((Double)o).doubleValue());
		else if (o instanceof Float)
			cell.setCellValue(((Float)o).floatValue());
		else if (o instanceof BigDecimal)
			cell.setCellValue(((BigDecimal)o).toString());
		else if (o instanceof Date)
			cell.setCellValue(((Date)o));
		else if (o instanceof Timestamp)
			cell.setCellValue(((Timestamp)o));
		else if (o instanceof Time)
			cell.setCellValue(((Time)o));
		else
			cell.setCellValue(o.toString());
		if (rgb != null)
			setCellColor(cell, rgb);
	}
	
	/**
	 * This function will set the font color of the specified cell
	 * to the requested value. Currently, the function is not
	 * properly implemented and it always sets the value to RED.
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
		default:
			throw new UnimplementedMethodException("Unknown output format for color processing.");
		}
	}

	protected void setHSSFCellColor(Cell cell, short[] rgb) {
		CellStyle style = cell.getCellStyle();
		CellStyle newStyle = wb.createCellStyle();
		Font font = wb.getFontAt(style.getFontIndex());

		HSSFColor closest = new HSSFColor.BLACK();
		double minDiff = Double.MAX_VALUE;
		
		short[] hssfRgb = null;
		float[] hsb = null, hssfHsb = null;
		hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		
		for (HSSFColor color : hssfColors.values()) {
			hssfRgb = color.getTriplet();
			hssfHsb = Color.RGBtoHSB(hssfRgb[0], hssfRgb[1], hssfRgb[2], hssfHsb);
			double diff =
					3d * Math.abs(hssfHsb[0] - hsb[0]) +
					Math.abs(hssfHsb[1] - hsb[1]) +
					Math.abs(hssfHsb[2] - hsb[2]);
			if (diff < minDiff) {
				minDiff = diff;
				closest = color;
			}
			if (minDiff == 0)
				break;
		}
		
		Font newFont = wb.findFont(
				font.getBoldweight(),
				closest.getIndex(),
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
			newFont.setColor(closest.getIndex());
			newFont.setFontHeight(font.getFontHeight());
			newFont.setFontName(font.getFontName());
			newFont.setItalic(font.getItalic());
			newFont.setStrikeout(font.getStrikeout());
			newFont.setTypeOffset(font.getTypeOffset());
			newFont.setUnderline(font.getUnderline());
		}
		newStyle.setFont(newFont);
		cell.setCellStyle(newStyle);
	}
	
	protected void setXSSFCellColor(XSSFCell cell, short[] rgb) {
		XSSFWorkbook twb = (XSSFWorkbook) wb;
		XSSFCellStyle style = cell.getCellStyle();
		XSSFCellStyle newStyle = twb.createCellStyle();
		XSSFFont font = style.getFont();
		XSSFFont newFont = twb.createFont();
		
		// Clone the style and font
		newStyle.cloneStyleFrom(style);
		newFont.setBoldweight(font.getBoldweight());
		newFont.setColor(new XSSFColor(new java.awt.Color(rgb[0], rgb[1], rgb[2])));
		newFont.setFontHeight(font.getFontHeight());
		newFont.setFontName(font.getFontName());
		newFont.setItalic(font.getItalic());
		newFont.setStrikeout(font.getStrikeout());
		newFont.setTypeOffset(font.getTypeOffset());
		newFont.setUnderline(font.getUnderline());
		
		// Look if the font already exists
		font = twb.findFont(
			newFont.getBoldweight(),
			newFont.getColor(),
			newFont.getFontHeight(),
			newFont.getFontName(),
			newFont.getItalic(),
			newFont.getStrikeout(),
			newFont.getTypeOffset(),
			newFont.getUnderline()
		);
		
		// If we have found the font and the color matches, use the already existing font.
		if (font == null || !font.getXSSFColor().equals(newFont.getXSSFColor())) {
			newStyle.setFont(font);
		} else {
			newStyle.setFont(newFont);
		}
		cell.setCellStyle(newStyle);
	}
	
	@Override
	public void writeOutput() throws IOException {
		log.info("Writing the result into the file: " + output);
		wb.write(out);
	}

	@Override
	public void start() throws IOException {
		out = new FileOutputStream(output);
		log.info("Reading a template file: " + template);
		if (System.getProperty("excel97") != null) {
			outputFormat = OutputFormat.HSSF;
		}
		switch (outputFormat) {
		case HSSF:
			wb = new HSSFWorkbook(new FileInputStream(template));
			hssfColors = HSSFColor.getTripletHash();
			break;
		case XSSF:
			wb = new XSSFWorkbook(new FileInputStream(template));
			break;
		}
		sheet = wb.getSheetAt(wb.getActiveSheetIndex());
	}
}
