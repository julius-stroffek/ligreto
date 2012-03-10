package net.ligreto.junit.tests.func.largedata;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ExcelStreamReportBuilderTest {
	@Test
	public void testExcelStreamReportBuilder() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("excelstreamreport", true);
	}
}
