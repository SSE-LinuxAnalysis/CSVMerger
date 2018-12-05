package net.ssehub.csvmerger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ssehub.kernel_haven.io.excel.ExcelBook;
import net.ssehub.kernel_haven.io.excel.ExcelSheetReader;
import net.ssehub.kernel_haven.util.io.csv.CsvReader;
import net.ssehub.kernel_haven.util.io.csv.CsvWriter;

public class ExcelMerger {
    
    static File csvInput;
    static File csvOutput;
    
    static ProcessedFunctionCommits commits;
    static List<String[]> csvContent;
    
    static CsvWriter writer;

    public static void main(String[] args) throws Exception {
        // show help
        if (args.length != 3) {
            help();
            return;
        }
        
        // check folder
        File folder = new File(args[1]);
        if (!folder.exists() || !folder.isDirectory() ) {
            System.out.println(folder.getPath() + " is no directory");
            return;
        }
        
        // check csv input
        csvInput = new File(args[0]);
        if (!csvInput.exists() || !csvInput.isFile()) {
            System.out.println(csvInput.getPath() + " is no file");
            return;
        }

        // read csv input
        readCsvInputContent();
        
        // create output
        csvOutput = new File(args[2]);
        writer = new CsvWriter(new FileOutputStream(csvOutput), ',');
        
        commits = new ProcessedFunctionCommits();
        
        long millis = System.currentTimeMillis();
        int rowCount = 0;
        
        // copy header of first file only
        boolean first = true;
        for (File file : getXLSXFiles(folder)) {
            System.out.println(file.getPath());
            rowCount += handleFile(file, first);
            first = false;
        }
        writer.flush();
        writer.close();
        
        System.out.println("Merged " + rowCount + " rows in " + ((System.currentTimeMillis() - millis) / 1000) + " seconds.");
    }
    
    /**
     * Process one file
     * @param file The file to process
     * @param writeHeader <tt>true</tt> if first line should be written to output
     * @return number of output lines (without header)
     * @throws Exception
     */
    public static int handleFile(File file, boolean writeHeader) throws Exception {
        int rowCount = 0;
        try (ExcelBook book = new ExcelBook(file)) {
            ExcelSheetReader reader = book.getReader(0);
            String[] header = reader.readNextRow();
            if (writeHeader) {
                Object[] result = new Object[header.length + 1];
                result[0] = "function_id";
                System.arraycopy(header, 0, result, 1, header.length);
                writer.writeRow(result);
            }
            String[] line;
            while ((line = reader.readNextRow()) != null) {
                // skip if not an error
                if (!line[3].equals("ERROR"))
                    continue;
                
                // check if line has been processed before
                String commit = line[2];
                if (commits.functionProcessed(commit, line[4], line[6], line[5])) {
                    continue;
                }
                
                // check if line has a function id
                String functionId = checkRowMatch(line);
                if (functionId != null) {
                    // remove '.0' at end of line number
                    if (null != line[5] && line[5].endsWith(".0")) {
                        line[5] = line[5].substring(0, line[5].length() - 2);
                    }
                    
                    // write line to output
                    Object[] result = new Object[line.length + 1];
                    result[0] = functionId;
                    System.arraycopy(line, 0, result, 1, line.length);
                    writer.writeRow(result);
                    rowCount++;
                }
            }
        }
        return rowCount;
    }

    /**
     * Check if row can be found in csv input
     * @param line A line to search for in the csv file
     * @return function_id if line was found, <tt>null</tt> otherwise
     */
    public static String checkRowMatch(String[] line) {
        for (String[] csvRow : csvContent) {
            if (csvRow[1].equals(line[4]) && csvRow[2].equals(line[6]))
                return csvRow[0];
        }
        return null;
    }
    
    /**
     * List all .xlsx files in given directory (recursively)
     * @param folder The directory to search in
     * @return List of .xlsx files
     */
    public static List<File> getXLSXFiles(File folder) {
        File[] files = folder.listFiles();
        List<File> excelFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                for (File f : getXLSXFiles(file))
                    excelFiles.add(f);
            } else if (file.isFile() && file.getName().endsWith(".xlsx")) {
                excelFiles.add(file);
            }
        }
        Collections.sort(excelFiles);
        return excelFiles;
    }

    /**
     * Read a csv file and write content to csvContent
     * @throws Exception
     */
    public static void readCsvInputContent() throws Exception {
        csvContent = new ArrayList<>();
        try (CsvReader reader = new CsvReader(new FileInputStream(csvInput))) {
            String[] line;
            reader.readNextRow();
            while ((line = reader.readNextRow()) != null) {
                csvContent.add(line);
            }
        }
    }
    
    /**
     * Help screen
     */
    public static void help() {
        System.out.println("Expected syntax: <csv file> <xlsx folder> <output csv file>");
    }
}
