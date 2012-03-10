package net.ligreto.junit.tests.func.mediumdata;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class LayoutTest {
	@Test
	public void testMultipleLayouts() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("multilayoutreport", true);
	}
			
	@Test
	public void testLayoutLimits() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("layoutlimitsreport", true);
	}
}
