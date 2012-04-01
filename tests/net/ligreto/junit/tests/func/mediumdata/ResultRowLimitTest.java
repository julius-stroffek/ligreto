package net.ligreto.junit.tests.func.mediumdata;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ResultRowLimitTest {
	@Test
	public void testResultRowTotalRowCountLimitSuccess() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-row-trc-succ-report", true);		
	}
	
	@Test
	public void testResultRowTotalRowCountLimitFailure() throws SAXException, IOException, LigretoException {	
		TestUtil.testReport("result-row-trc-fail-report", false);
	}
	
	@Test
	public void testResultRowRelativeDifferenceCountLimitSuccess() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-row-rdc-succ-report", true);		
	}
	
	@Test
	public void testResultRowRelativeDifferenceCountLimitFailure() throws SAXException, IOException, LigretoException {	
		TestUtil.testReport("result-row-rdc-fail-report", false);
	}
	
	@Test
	public void testResultRowRelativeNonMatchedCountLimitSuccess() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-rnmc-succ-report", true);		
	}

	@Test
	public void testResultRowRelativeNonMatchedCountLimitFailure() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-rnmc-fail-report", false);		
	}

	@Test
	public void testResultRowAbsoluteDifferenceCountLimitSuccess() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-row-adc-succ-report", true);		
	}
	
	@Test
	public void testResultRowAbsoluteDifferenceCountLimitFailure() throws SAXException, IOException, LigretoException {	
		TestUtil.testReport("result-row-adc-fail-report", false);
	}
	
	@Test
	public void testResultRowAbsoluteNonMatchedCountLimitSuccess() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-anmc-succ-report", true);		
	}

	@Test
	public void testResultRowAbsoluteNonMatchedCountLimitFailure() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-anmc-fail-report", false);		
	}
}
