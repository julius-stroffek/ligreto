package net.ligreto.junit.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ParserTest.class, SQLReportTest.class, JoinReportTest.class })
public class AllTests {

}
