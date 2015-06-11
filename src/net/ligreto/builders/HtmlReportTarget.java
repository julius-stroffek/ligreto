/**
 * 
 */
package net.ligreto.builders;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.MiscUtils;

/**
 * @author julo
 *
 */
public class HtmlReportTarget extends ReportTarget {

	protected HtmlReportTarget(ReportBuilder reportBuilder) {
		super(reportBuilder);
	}

	private class Cell {
		String text;
		OutputStyle style;
		OutputFormat format;
	};
	
	private LigretoParameters ligretoParameters;
	private String targetName = null;
	private Map<Integer, Cell> headerRow = new HashMap<Integer, Cell>();
	private List<Map<Integer, Cell>> allRows = new LinkedList<Map<Integer,Cell>>();
	private Map<Integer, Cell> currentRow = null;
	private int maxColumnIndex = 0;
	
	@Override
	public void nextRow() throws IOException {
		super.nextRow();
		if (currentRow != null) {
			allRows.add(currentRow);
		}
		currentRow = new HashMap<Integer, Cell>();
	}

	@Override
	public void dumpCell(int i, Object value, OutputFormat outputFormat, OutputStyle outputStyle) throws LigretoException {
		// TODO Formatting of the data
		Cell cell = new Cell();
		cell.format = outputFormat;
		cell.style = outputStyle;
		if (value != null) {
			switch (outputFormat) {
			case DEFAULT:
				cell.text = value.toString();
				break;
			case PERCENTAGE_2_DECIMAL_DIGITS:
				cell.text = value.toString();
				break;
			case PERCENTAGE_3_DECIMAL_DIGITS:
				cell.text = value.toString();
				break;
			case PERCENTAGE_NO_DECIMAL_DIGITS:
				cell.text = value.toString();
				break;
			default:
				cell.text = value.toString();
				break;			
			}
		} else {
			cell.text = ligretoParameters.getNullString();
		}
		int column = currentColumnPosition + columnStep * i;
		currentRow.put(column, cell);
		if (maxColumnIndex < column) {
			maxColumnIndex = column;
		}
	}

	@Override
	public void finish() throws IOException {
	}

	@Override
	public void setHighlight(boolean highlight) {
	}

	@Override
	public void setHighlightColor(short[] rgbHlColor) {
	}

	@Override
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
		this.ligretoParameters = ligretoParameters;
	}

	public void setName(String targetName) {
		this.targetName = targetName;
	}

	private void writeCell(PrintWriter pw, Cell cell) {
		if (cell != null) {
			switch (cell.style) {
			case DEFAULT:
				pw.write("<td class=\"default\">");
				break;
			case DISABLED:
				pw.write("<td class=\"disabled\">");
				break;
			case HIGHLIGHTED:
				pw.write("<td class=\"highlighted\">");
				break;
			case ROW_HEADER:
				pw.write("<td class=\"rowHeader\">");
				break;
			case ROW_HEADER_DISABLED:
				pw.write("<td class=\"rowHeaderDisabled\">");
				break;
			case TOP_HEADER:
				pw.write("<td class=\"topHeader\">");
				break;
			case TOP_HEADER_DISABLED:
				pw.write("<td class=\"topHeaderDisabled\">");
				break;
			default:
				pw.write("<td class=\"default\">");
				break;
			}
			pw.write(cell.text);
			pw.write("</td>");
		} else {
			pw.write("<td class=\"default\"></td>");
		}
	}
	

	private void writeRow(PrintWriter pw, Map<Integer, Cell> row) {
		pw.write("<tr>");
		if (row != null && row.size() > 0) {
			Integer[] keys = row.keySet().toArray(new Integer[0]);
			Arrays.sort(keys);
			for (int i = 0; i < maxColumnIndex; i++) {
				writeCell(pw, row.get(i));
			}
		}
		pw.write("</tr>");
	}
	
	public void writeOutput(PrintWriter pw) {
		if (MiscUtils.isNotEmpty(targetName)) {
			pw.print("<h2>");
			pw.print(targetName);
			pw.print("</h2>");
		}
		pw.print("<table>");
		if (headerRow != null) {
			writeRow(pw, headerRow);
		}
		for (Map<Integer, Cell> row : allRows) {
			writeRow(pw, row);
		}
		pw.print("</table>");
	}
}
