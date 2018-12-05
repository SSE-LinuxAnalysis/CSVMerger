package net.ssehub.csvmerger;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import net.ssehub.csvmerger.CSVMerger;

public class CSVMergerTest {
    
    public static final File TESTFOLDER = new File("TESTDATA");

    public File firstFile = new File(TESTFOLDER, "testA.csv");
    public File secondFile = new File(TESTFOLDER, "testB.csv");
    public File thirdFile = new File(TESTFOLDER, "testBig.csv");
    public File outputFile = new File(TESTFOLDER, "output.csv");

    public CSVMergerTest() {
        
    }
    
    public interface Function {
        void exec() throws Exception;
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
    
    public ArrayList<String> readFile(File file) throws Exception {
        ArrayList<String> out = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null)
                out.add(line);
        }
        return out;
    }
    
    @Before
    public void setUp() {
        assertThat(firstFile.getAbsolutePath() + " did not exist before test.", firstFile.exists(), is(true));
        assertThat(secondFile.getAbsolutePath() + " did not exist before test.", secondFile.exists(), is(true));
        assertThat(thirdFile.getAbsolutePath() + " did not exist before test.", thirdFile.exists(), is(true));
        
        if (outputFile.exists())
            outputFile.delete();
        assertThat(outputFile.getAbsolutePath() + " did exist already before test.", outputFile.exists(), is(false));
        
        CSVMerger.LOG_INTERVAL = 30;
        CSVMerger.inp = null;
        CSVMerger.inpSize = 0;
        CSVMerger.colsA = new int[] {};
        CSVMerger.colsB = new int[] {};
        CSVMerger.bSize = 0;
        CSVMerger.pos = 0;
        CSVMerger.commits = null;
    }
    
    @Test
    public void testGetCols() {
        assertThat(CSVMerger.getCols("1,2,3"), is(new int[] { 1, 2, 3 }));
        assertThat(CSVMerger.getCols("7"), is(new int[] { 7 }));
    }

    @Test (expected = NumberFormatException.class)
    public void testGetColsNotANumber() {
        CSVMerger.getCols("3,X,5");
    }
    
    @Test (expected = NumberFormatException.class)
    public void testGetColsEmpty() {
        CSVMerger.getCols("");
    }
    
    @Test (expected = NumberFormatException.class)
    public void testGetColsSpace() {
        CSVMerger.getCols("");
    }
    
    @Test
    public void testCountLines() throws Exception {
        assertThat(CSVMerger.countLines(firstFile.getPath()), is(3));
        assertThat(CSVMerger.countLines(secondFile.getPath()), is(11));
    }    
    
    @Test
    public void testHelp() throws Exception {
        String out = getConsoleOutput(() -> {CSVMerger.help();});
        assertThat(out.startsWith("Expected syntax: "), is(true));
    }
    
    @Test
    public void testReadInputA() throws Exception {
        CSVMerger.readInputA(firstFile);
        assertThat(CSVMerger.inpSize, is(2));
        assertThat(CSVMerger.inp.size(), is(2));
        assertThat(CSVMerger.inp.get(0), is(new String[] {"13", "37", "123"}));
        assertThat(CSVMerger.inp.get(1), is(new String[] {"4", "2", "456"}));
    }
    
    @Test
    public void testCheckRowMatch() throws Exception {
        CSVMerger.readInputA(firstFile);
        CSVMerger.colsA = new int[] {0, 1};
        CSVMerger.colsB = new int[] {1, 0};
        assertThat(CSVMerger.pos, is(0));
        assertThat(CSVMerger.checkRowMatch(new String[] {"2", "4"}), is("4"));
        assertThat(CSVMerger.pos, is(1));
        assertThat(CSVMerger.checkRowMatch(new String[] {"7", "4"}), nullValue());
        assertThat(CSVMerger.pos, is(1));
    }
        
    @Test
    public void testMainInvalidSyntax() throws Exception {
        String out = getConsoleOutput(() -> {CSVMerger.main(new String[] { });});
        assertThat(out.startsWith("Expected syntax: "), is(true));
        assertThat(outputFile.exists(), is(false));
    }
    
    @Test
    public void testMainInvalidColsLength() throws Exception {
        String out = getConsoleOutput(() -> {CSVMerger.main(new String[] { 
                firstFile.getPath(), "0,1,2", 
                secondFile.getPath(), "1,0", 
                outputFile.getPath() 
        });});
        assertThat(out.startsWith("Error!\nInvalid cols length"), is(true));
        assertThat(outputFile.exists(), is(false));
    }
    
    @Test
    public void testMainBigFile() throws Exception {
        String out = getConsoleOutput(() -> {CSVMerger.main(new String[] { 
                firstFile.getPath(), "0,1",
                thirdFile.getPath(), "1,0",
                outputFile.getPath()
        });});
        String[] lines = out.split("\n");
        assertThat(lines[0].startsWith("Input A has 2 rows."), is(true));
        assertThat(lines[1].startsWith("Input B has 2000 rows."), is(true));

        assertThat(outputFile.exists(), is(true));
        ArrayList<String> outputLines = readFile(outputFile);
        assertThat(outputLines.size(), is(2));
        assertThat(outputLines.get(0), is("function_id,Hello,World,blubb"));
        assertThat(outputLines.get(1), is("4,2,4,8"));
    }
    
    @Test
    public void testMainBigFileWithProgMsg() throws Exception {
        CSVMerger.LOG_INTERVAL = 0;
        String out = getConsoleOutput(() -> {CSVMerger.main(new String[] { 
                firstFile.getPath(), "0,1",
                thirdFile.getPath(), "1,0",
                outputFile.getPath()
        });});

        String[] lines = out.split("\n");
        assertThat(lines[0].startsWith("Input A has 2 rows."), is(true));
        assertThat(lines[1].startsWith("Input B has 2000 rows."), is(true));
        assertThat(lines[2].startsWith("Time: 0 seconds; 50.0%"), is(true));
        assertThat(lines[3].startsWith("Time: 0 seconds; 100.0%"), is(true));
        assertThat(lines[5].startsWith("Merged 1 / 2000 rows in "), is(true));

        assertThat(outputFile.exists(), is(true));
        ArrayList<String> outputLines = readFile(outputFile);
        assertThat(outputLines.size(), is(2));
        assertThat(outputLines.get(0), is("function_id,Hello,World,blubb"));
        assertThat(outputLines.get(1), is("4,2,4,8"));
    }
}
