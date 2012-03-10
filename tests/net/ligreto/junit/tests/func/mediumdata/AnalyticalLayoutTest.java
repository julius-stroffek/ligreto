package net.ligreto.junit.tests.func.mediumdata;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class AnalyticalLayoutTest {
	@Test
	public void testAnalyticalLayout() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("analyticalreport", true);
	}
}
