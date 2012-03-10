package net.ligreto.junit.tests.func.mediumdata;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class SummaryLayoutTest {
	@Test
	public void testSummaryLayout() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("summaryreport", true);
	}

	@Test
	public void testMultiSummaryLayout() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("multisummaryreport", true);
	}

	@Test
	public void testStreamMultiSummaryLayout() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("multisummarystreamreport", true);
	}
}
