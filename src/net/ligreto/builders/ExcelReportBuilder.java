package net.ligreto.builders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.ligreto.exceptions.InvalidTargetExpection;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

public class ExcelReportBuilder extends ReportBuilder {
	FileOutputStream out;
	Workbook wb;
	Sheet sheet;
	Row row;
	int baseRow = 0;
	int baseCol = 0;
	int actRow = baseRow-1;
	int actCol = baseCol;
	
	public void setTarget(String target) throws InvalidTargetExpection {
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
			throw new InvalidTargetExpection("The target reference is invalid: \"" + target + "\"");
		}
		
	}

	public void nextRow() {
		actRow++;
		actCol = baseCol;
		row = sheet.getRow(actRow);
		if (row == null)
			row = sheet.createRow(actRow);
	}
	
	protected Cell createCell(Row row, int col) {
		Cell cell = row.getCell(actCol + col);
		if (cell == null)
			cell = row.createCell(actCol + col);
		return cell;
	}

	public void setColumnPosition(int column) {
		actCol = baseCol + column;
	}

	public void setColumnPosition(int column, int step) {
	}

	public void setColumn(int i, Object o) {
		Cell cell = createCell(row, actCol + i);
		cell.setCellValue(o.toString());
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
