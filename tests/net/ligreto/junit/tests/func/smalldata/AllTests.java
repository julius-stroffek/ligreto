package net.ligreto.junit.tests.func.smalldata;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	PrepareTestData.class,
	ComparisonReportTest.class,
	DetailedLayoutTest.class,
	Excel97ReportTest.class,
	ExcludeColumnsTest.class,	
	InternalSortTest.class,
	PtpReportTest.class
})
public class AllTests {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
	}	
}
