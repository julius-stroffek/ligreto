package net.ligreto.junit.tests.func.mediumdata;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class AggregatedLayoutTest {
	@Test
	public void testAggregatedLayout() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("aggregatedreport", true);
	}
}
