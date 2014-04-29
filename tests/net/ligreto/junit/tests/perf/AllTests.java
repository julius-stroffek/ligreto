package net.ligreto.junit.tests.perf;

import net.ligreto.junit.util.TestUtil;
import net.ligreto.util.AssertionUtil;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	PtpPerformance.class
})
public class AllTests {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AssertionUtil.enableAssertions();
		if ("true".equals(System.getProperty("debug", "false"))) {
			Logger.getRootLogger().setLevel(Level.DEBUG);					
		}
		TestUtil.createDBs();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}	
}
