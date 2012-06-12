package net.ligreto.builders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.MiscUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This is the implementation of the {@code BuilderInterface} producing
 * Microsoft Excel HSSF/XSSF files using Apache POI library.
 * 
 * There are certain options that could be passed to this implementation.
 * The supported options are:
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
		
		/** The sheet index. */
		int sheet;
		
		/** The row number within the sheet. */
		int row;
		
		/** The columns number within the row. */
		int column;
		
		/** The last row where data were dumped to this target. */
		int lastRow;
		
		/** Indicates whether the target is in use. */
		boolean inUse;
		
		/** The string representation of this target. */
		String name;
		
		/** Creates the target information class. */
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
			final int prime = 31;
			return prime*prime*sheet + prime*row + column;
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
	 * It is used for append operation on the targets.
	 */
	protected Map<TargetInfo, TargetInfo> targetMap = new HashMap<TargetInfo, TargetInfo>(128);
	
	/** The output file type enumeration. */
	protected enum OutputFileFormat {HSSF, XSSF, SXSSF};
	
	/** The output file format. */
	protected OutputFileFormat outputFileFormat = OutputFileFormat.XSSF;

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ExcelReportBuilder.class);
	
	/** The output stream where the output file will be written to. */
	protected FileOutputStream out;
	
	/** The {@code Workbook} object for the output report. */
	protected Workbook wb;
	
	/** The workbook data format instance. */
	protected DataFormat dataFormat;
	
	/** The {@code Sheet} object where the result is placed. */
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
	
	/** Holds the highest column index used for current target. */
//	int lastColumnIndex = -1;
	
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
		newTarget.outputFileFormat = outputFileFormat;
		return newTarget;
	}
	
	@Override
	public  TargetInterface getTargetBuilder(String target, boolean append) throws TargetException {
		ExcelReportTarget newTarget = null;
		CellReference ref;
		try {
			ref = new CellReference(target);
		} catch (IllegalArgumentException e1) {
			String derivedTarget = target + "!A1";
			try {
				ref = new CellReference(derivedTarget);
			} catch (IllegalArgumentException e2) {
				throw new TargetException("Could not get the target reference: " + target);
			}
		}
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
				newTarget.setCurrentRow(prevInfo.lastRow);
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
		// Alter the output format if necessary
		if (System.getProperty("excel97") != null) {
			outputFileFormat = OutputFileFormat.HSSF;
		}		

		// Fix the file extension
		switch (outputFileFormat) {
		case HSSF:
			output = MiscUtils.fixFileExt(output, ".xls");
			break;
		case XSSF:
			output = MiscUtils.fixFileExt(output, ".xlsx");
			break;
		}
		out = new FileOutputStream(output);

		// Read the template file if the template was specified
		if (template != null) {
			log.info("Reading a template file: " + template);
			switch (outputFileFormat) {
			case HSSF:
				template = MiscUtils.fixFileExt(template, ".xls");
				wb = new HSSFWorkbook(new FileInputStream(template));
				hssfColors = HSSFColor.getTripletHash();
				break;
			case XSSF:
				template = MiscUtils.fixFileExt(template, ".xlsx");
				wb = new XSSFWorkbook(new FileInputStream(template));
				break;
			}
		} else {
			log.info("Creating the empty workbook.");
			switch (outputFileFormat) {
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

	/**
	 * @return true if there is a template file used.
	 */
	public boolean hasTemplate() {
		return template != null;
	}
}
