package net.ligreto.builders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.ligreto.exceptions.InvalidTargetException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

public class ExcelReportBuilder extends ReportBuilder {
	FileOutputStream out;
	Workbook wb;
	Sheet sheet;
	Row row;
	
	public void setTarget(String target) throws InvalidTargetException {
		CellReference ref = new CellReference(target);
		Sheet sheet;
		if (ref.getSheetName() != null) {
			sheet = wb.getSheet(ref.getSheetName());
		} else {
			sheet = this.sheet;
		}
		int row = ref.getRow();
		int col = ref.getCol();
		if (sheet != null) {
			this.sheet = sheet;
			this.baseRow = row;
			this.baseCol = col;
			actRow = baseRow-1;
			actCol = baseCol;
		} else {
			throw new InvalidTargetException("The target reference is invalid: \"" + target + "\"");
		}
	}

	public void nextRow() {
		super.nextRow();
		row = sheet.getRow(actRow);
		if (row == null)
			row = sheet.createRow(actRow);
	}
	
	protected Cell createCell(Row row, int col) {
		Cell cell = row.getCell(col);
		if (cell == null)
			cell = row.createCell(col);
		return cell;
	}

	public void setColumn(int i, Object o, String color) {
		Cell cell = createCell(row, actCol + i);
		cell.setCellValue(o.toString());
		if (color != null)
			setCellColor(cell, color);
	}
	
	protected void setCellColor(Cell cell, String newColor) {
		CellStyle style = cell.getCellStyle();
		CellStyle newStyle = wb.createCellStyle();
		Font font = wb.getFontAt(style.getFontIndex());
		HSSFColor color = new HSSFColor.RED();
		Font newFont = wb.findFont(
				font.getBoldweight(),
				color.getIndex(),
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
			newFont.setColor(color.getIndex());
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

	public void writeOutput() throws IOException {
		wb.write(out);
	}

	@Override
	public void start() throws IOException {
		out = new FileOutputStream(output);
		wb = new HSSFWorkbook(new FileInputStream(template));
		sheet = wb.getSheetAt(wb.getActiveSheetIndex());

	}
}
