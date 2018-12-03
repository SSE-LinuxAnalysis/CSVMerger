package net.ssehub.csvmerger;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CSVMergerTest.class, ExcelMergerTest.class, ProcessedFunctionCommitsTest.class })
public class AllTests {

}
