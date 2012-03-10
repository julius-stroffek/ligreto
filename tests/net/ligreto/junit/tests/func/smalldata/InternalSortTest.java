package net.ligreto.junit.tests.func.smalldata;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class InternalSortTest {
	@Test
	public void testInternalSort() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("internalsortreport", true);
	}
}
