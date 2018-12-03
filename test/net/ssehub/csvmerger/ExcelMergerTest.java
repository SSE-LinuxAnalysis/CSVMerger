package net.ssehub.csvmerger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.ssehub.csvmerger.ExcelMerger;
import net.ssehub.csvmerger.ProcessedFunctionCommits;
import net.ssehub.kernel_haven.util.io.csv.CsvWriter;

public class ExcelMergerTest {
    
    public static final File TESTFOLDER = new File("TESTDATA");

    public File csvFile = new File(TESTFOLDER, "all_error_functions.csv");
    public File smallCsvFile = new File(TESTFOLDER, "error_functions_small.csv");
    public File outputFile = new File(TESTFOLDER, "output.csv");
    
    public File expectedJune = new File(TESTFOLDER, "expected_june.csv");
    public File expectedMain = new File(TESTFOLDER, "expected_main.csv");
    
    public File folder = new File(TESTFOLDER, "KbuildTestRobot");
    public File inp1 = new File(TESTFOLDER, "KbuildTestRobot/2018-06/2018-June.xlsx");
    public File inp2 = new File(TESTFOLDER, "KbuildTestRobot/2018-07/2018-July.xlsx");
    public File inp3 = new File(TESTFOLDER, "KbuildTestRobot/2018-08/2018-August.xlsx");
    public File inp4 = new File(TESTFOLDER, "KbuildTestRobot/2018-09/2018-September.xlsx");

    public ExcelMergerTest() {

    }
    
    public interface Function {
        void exec() throws Exception;
    }
    
    public ArrayList<String> readFile(File file) throws Exception {
        ArrayList<String> out = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                out.add(line);
        }
        return out;
    }

    public String getConsoleOutput(Function func) throws Exception {
        PrintStream oldOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(out);

        System.setOut(ps);
        func.exec();
        System.setOut(oldOut);
        
        return out.toString();
    }
    
    @Before
    public void setUp() {
        assertThat(csvFile.getAbsolutePath() + " did not exist before test.", csvFile.exists(), is(true));
        assertThat(smallCsvFile.getAbsolutePath() + " did not exist before test.", smallCsvFile.exists(), is(true));
        assertThat(inp1.getAbsolutePath() + " did not exist before test.", inp1.exists(), is(true));
        assertThat(inp2.getAbsolutePath() + " did not exist before test.", inp2.exists(), is(true));
        assertThat(inp3.getAbsolutePath() + " did not exist before test.", inp3.exists(), is(true));
        assertThat(inp4.getAbsolutePath() + " did not exist before test.", inp4.exists(), is(true));

        assertThat(expectedJune.getAbsolutePath() + " did not exist before test.", expectedJune.exists(), is(true));
        assertThat(expectedMain.getAbsolutePath() + " did not exist before test.", expectedMain.exists(), is(true));

        if (outputFile.exists())
            outputFile.delete();
        assertThat(outputFile.getAbsolutePath() + " did exist already before test.", outputFile.exists(), is(false));
        
        ExcelMerger.csvInput = null;
        ExcelMerger.csvOutput = null;
        ExcelMerger.commits = null;
        ExcelMerger.csvContent = null;
        ExcelMerger.writer = null;
    }
    
    @After
    public void tearDown() {
        if (outputFile.exists())
            outputFile.delete();
        assertThat("could not delete " + outputFile.getAbsolutePath() + ".", outputFile.exists(), is(false));
    }

    @Test
    public void testHelp() throws Exception {
        String out = getConsoleOutput(() -> {ExcelMerger.help();});
        assertThat(out.startsWith("Expected syntax: "), is(true));
    }

    @Test
    public void testReadCSVInputContent() throws Exception {
        ExcelMerger.csvInput = smallCsvFile;
        ExcelMerger.readCsvInputContent();
        assertThat(ExcelMerger.csvContent.size(), is(3));
        assertThat(ExcelMerger.csvContent.get(0), is(new String[] { "4378", "arch/x86/boot/compressed/eboot.c", "make_boot_params", "1" }));
        assertThat(ExcelMerger.csvContent.get(1), is(new String[] { "3321", "arch/x86/boot/compressed/string.c", "memcpy", "1" }));
        assertThat(ExcelMerger.csvContent.get(2), is(new String[] { "9643", "arch/x86/crypto/chacha20_glue.c", "chacha20_simd_mod_init", "1" }));
    }
    
    @Test
    public void testGetXLSXFiles() throws Exception {
        List<File> files = ExcelMerger.getXLSXFiles(folder);
        assertThat(files.size(), is(4));
        assertThat(files.get(0), is(inp1));
        assertThat(files.get(1), is(inp2));
        assertThat(files.get(2), is(inp3));
        assertThat(files.get(3), is(inp4));
    }
    
    @Test
    public void testCheckRowMatch() throws Exception {
        ExcelMerger.csvInput = csvFile;
        ExcelMerger.readCsvInputContent();
        
        assertThat(ExcelMerger.checkRowMatch(new String[] { 
                "foo", "bar", "bazcommit", "blubb", "arch/x86/boot/compressed/eboot.c", "barline", "make_boot_params", 
        }), is("4378"));
    }
    
    @Test
    public void testCheckRowMatchReturnNull() throws Exception {
        ExcelMerger.csvInput = csvFile;
        ExcelMerger.readCsvInputContent();
        
        assertThat(ExcelMerger.checkRowMatch(new String[] { 
                "foo", "bar", "bazcommit", "blubb", "arch/x86/boot/boot.c", "barline", "make_boot_params", 
        }), nullValue());
        assertThat(ExcelMerger.checkRowMatch(new String[] { 
                "foo", "bar", "bazcommit", "blubb", "arch/x86/boot/compressed/eboot.c", "barline", "maboot_params", 
        }), nullValue());
    }
    
    @Test
    public void testHandleFile() throws Exception {
        ExcelMerger.csvInput = csvFile;
        ExcelMerger.readCsvInputContent();

        ExcelMerger.writer = new CsvWriter(new FileOutputStream(outputFile), ',');
        ExcelMerger.commits = new ProcessedFunctionCommits();
        assertThat(ExcelMerger.handleFile(inp1, true), is(19));
        ExcelMerger.writer.close();

        ArrayList<String> actual = readFile(outputFile);
        ArrayList<String> expected = readFile(expectedJune);
        assertThat(actual, is(expected));
    }
    
    @Test
    public void testHandleFileWithoutHeader() throws Exception {
        ExcelMerger.csvInput = csvFile;
        ExcelMerger.readCsvInputContent();
        
        ExcelMerger.writer = new CsvWriter(new FileOutputStream(outputFile), ',');
        ExcelMerger.commits = new ProcessedFunctionCommits();
        assertThat(ExcelMerger.handleFile(inp1, false), is(19));
        ExcelMerger.writer.close();
        
        ArrayList<String> actual = readFile(outputFile);
        ArrayList<String> expected = readFile(expectedJune);
        expected.remove(0);
        assertThat(actual, is(expected));
    }
    
//    @Test
//    public void testString() {
//        String expected = "Hallo Welt";
//        String actual = "HalLo Welt";
//        Assert.assertEquals("Strings are not equal", expected, actual);
//    }
//    
    @Test
    public void testMainHelpScreen() throws Exception {
        String out = getConsoleOutput(() -> {ExcelMerger.main(new String[] {  });});
        assertThat(out.startsWith("Expected syntax: "), is(true));
    }
    
    @Test
    public void testMainInvalidFolder() throws Exception {
        String out = getConsoleOutput(() -> {ExcelMerger.main(new String[] { csvFile.getPath(), "doesnotexist", outputFile.getPath() });});
        assertThat(out.startsWith("doesnotexist is no"), is(true));
        
        out = getConsoleOutput(() -> {ExcelMerger.main(new String[] { csvFile.getPath(), smallCsvFile.getPath(), outputFile.getPath() });});
        assertThat(out.startsWith(smallCsvFile.getPath() + " is no"), is(true));
    }
    
    @Test
    public void testMainInvalidCSVInputFile() throws Exception {
        String out = getConsoleOutput(() -> {ExcelMerger.main(new String[] { "blubb", folder.getPath(), outputFile.getPath() });});
        assertThat(out.startsWith("blubb is no"), is(true));
        
        out = getConsoleOutput(() -> {ExcelMerger.main(new String[] { folder.getPath(), folder.getPath(), outputFile.getPath() });});
        assertThat(out.startsWith(folder.getPath() + " is no"), is(true));
    }
    
    @Test
    public void testMain() throws Exception {
        assertThat(outputFile.exists(), is(false));
        String out = getConsoleOutput(() -> {ExcelMerger.main(new String[] { csvFile.getPath(), folder.getPath(), outputFile.getPath() });});
        String[] lines = out.split("\n");
        assertThat(lines.length, is(5));
        assertThat(lines[0].startsWith(inp1.getPath()), is(true));
        assertThat(lines[1].startsWith(inp2.getPath()), is(true));
        assertThat(lines[2].startsWith(inp3.getPath()), is(true));
        assertThat(lines[3].startsWith(inp4.getPath()), is(true));
        assertThat(lines[4].startsWith("Merged 45 rows in "), is(true));
        
        assertThat(outputFile.exists(), is(true));
        ArrayList<String> actual = readFile(outputFile);
        ArrayList<String> expected = readFile(expectedMain);
        
        int size = expected.size();
        assertThat(actual.size(), is(size));
        for (int i = 0; i < size; i++)
            assertThat("line " + (i + 1), actual.get(i), is(expected.get(i)));
    }
}
