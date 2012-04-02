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

	@Test
	public void testResultRowAbsoluteEqualCountLimitSuccess() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-aec-succ-report", true);		
	}

	@Test
	public void testResultRowAbsoluteEqualCountLimitFailure() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-aec-fail-report", false);		
	}

	@Test
	public void testResultRowRelativeEqualCountLimitSuccess() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-rec-succ-report", true);		
	}

	@Test
	public void testResultRowRelativeEqualCountLimitFailure() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-rec-fail-report", false);		
	}

	@Test
	public void testResultRowAbsoluteMatchedCountLimitSuccess() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-amc-succ-report", true);		
	}

	@Test
	public void testResultRowAbsoluteMatchedCountLimitFailure() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-amc-fail-report", false);		
	}

	@Test
	public void testResultRowRelativeMatchedCountLimitSuccess() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-rmc-succ-report", true);		
	}

	@Test
	public void testResultRowRelativeMatchedCountLimitFailure() throws SAXException, IOException, LigretoException {		
		TestUtil.testReport("result-row-rmc-fail-report", false);		
	}
}
