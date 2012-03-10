package net.ligreto.junit.tests.func.smalldata;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class DetailedLayoutTest {
	@Test
	public void testDetailedLayout() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("detailedreport", false);
	}

	@Test
	public void testStreamDetailedLayout() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("streamdetailedreport", false);
	}
}
