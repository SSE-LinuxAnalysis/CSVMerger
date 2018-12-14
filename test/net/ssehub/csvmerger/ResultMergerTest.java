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

import net.ssehub.kernel_haven.io.excel.ExcelBook;
import net.ssehub.kernel_haven.io.excel.ExcelSheetReader;

public class ResultMergerTest {
    
    public static final File TESTFOLDER = new File("TESTDATA");
    
    public static final File LEFT_JULY_FILE = new File(TESTFOLDER, "2018-July.xlsx");
    public static final File RIGHT_JULY_FILE = new File(TESTFOLDER, "Right/2018-July.xlsx");
    
    public static final File OUTPUT_FOLDER = new File(TESTFOLDER, "output");
    public static final File LEFT_FOLDER = new File(TESTFOLDER, "KbuildTestRobot");
    public static final File RIGHT_FOLDER = new File(TESTFOLDER, "Right");

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
        assertThat(LEFT_JULY_FILE.exists(), is(true));
        assertThat(RIGHT_JULY_FILE.exists(), is(true));
        
        assertThat(LEFT_FOLDER.isDirectory(), is(true));
        assertThat(RIGHT_FOLDER.isDirectory(), is(true));
        assertThat(OUTPUT_FOLDER.isDirectory(), is(true));
        
        for (File f : OUTPUT_FOLDER.listFiles()) {
            f.delete();
        }
        
        ResultMerger.leftFolder = null;
        ResultMerger.rightFolder = null;
        ResultMerger.outFolder = null;
        ResultMerger.leftFiles = null;
        ResultMerger.rightFiles = null;
        ResultMerger.leftRightFiles = null;
        ResultMerger.rightContent = null;
    }
    
    @Test
    public void testHelp() throws Exception {
        String out = getConsoleOutput(() -> {ResultMerger.help();});
        assertThat(out.startsWith("Expected syntax: "), is(true));
    }
    
    @Test
    public void testFindRowInRight() throws Exception {
        ResultMerger.rightContent = new ArrayList<>();
        try (ExcelBook book = new ExcelBook(RIGHT_JULY_FILE)) {
            ExcelSheetReader reader = book.getReader(0);
            String[] line = reader.readNextRow();
            while ((line = reader.readNextRow()) != null) {
                ResultMerger.rightContent.add(line);
            }
        }
        assertThat(ResultMerger.rightContent.size(), is(3));
        
        assertThat(ResultMerger.findRowInRight("srcfile", "42.0", "elem", 0), is(0));
        assertThat(ResultMerger.findRowInRight("srcfile", "42.0", "elem", 1), is(-1));
        assertThat(ResultMerger.findRowInRight("scfile", "42.0", "elem", 0), is(-1));
        assertThat(ResultMerger.findRowInRight("srcfile", "x42.0", "elem", 0), is(-1));
        assertThat(ResultMerger.findRowInRight("srcfile", "42.0", "elEm", 0), is(-1));

        assertThat(ResultMerger.findRowInRight("arch/x86/kernel/cpu/perf_event_intel_bts.c",
                "526.0", "bts_init", 0), is(1));
        assertThat(ResultMerger.findRowInRight("lib/devres.c", "110.0", "devm_ioremap_exec", 0), is(2));
    }
    
    @Test
    public void testHandleFile() throws Exception {
        ResultMerger.outFolder = OUTPUT_FOLDER;
        ResultMerger.handleFile(LEFT_JULY_FILE, RIGHT_JULY_FILE);
        
        File output = new File(OUTPUT_FOLDER, "2018-July.xlsx");
        File expected = new File(TESTFOLDER, "2018-July-expected-output.xlsx");
        assertThat(output.exists(), is(true));
        try (ExcelBook book = new ExcelBook(output)) {
            ExcelSheetReader reader = book.getReader(0);
            try (ExcelBook b = new ExcelBook(expected)) {
                ExcelSheetReader r = b.getReader(0);
                String[] row;
                while ((row = reader.readNextRow()) != null) {
                    assertThat(row, is(r.readNextRow()));
                }
                assertThat(r.readNextRow(), nullValue());
            }
        }
    }
    
    @Test
    public void testMainHelpScreen() throws Exception {
        String out = getConsoleOutput(() -> {ResultMerger.main(new String[] {  });});
        assertThat(out.startsWith("Expected syntax: "), is(true));
    }
    
    @Test
    public void testMainFolderNotExisting() throws Exception {
        String out = getConsoleOutput(() -> {ResultMerger.main(new String[] { "does", "not", "exist" });});
        assertThat(out.startsWith("the specified folders do not exist"), is(true));
    }
    
    @Test
    public void testMain() throws Exception {
        ResultMerger.main(new String[] { LEFT_FOLDER.getPath(), RIGHT_FOLDER.getPath(), OUTPUT_FOLDER.getPath() });
    }
}
