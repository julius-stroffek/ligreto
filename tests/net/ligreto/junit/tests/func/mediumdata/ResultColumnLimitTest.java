package net.ligreto.junit.tests.func.mediumdata;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ResultColumnLimitTest {
	@Test
	public void testResultColumnLimitsNoColSpecSuccess() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-col-succ-report", "result-col-report", true);
	}
	
	@Test
	public void testResultColumnLimitsNoColSpecFailure() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-col-fail-report", "result-col-report", false);
	}
	
	@Test
	public void testResultColLimitsWithExcludeSuccess() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-col-exclude-succ-report", "result-col-exclude-report", true);
	}
	
	@Test
	public void testResultColLimitsWithExcludeFailure() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-col-exclude-fail-report", "result-col-exclude-report", false);
	}
}
