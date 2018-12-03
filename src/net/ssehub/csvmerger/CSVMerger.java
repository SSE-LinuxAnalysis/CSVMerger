package net.ssehub.csvmerger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.util.io.csv.CsvReader;
import net.ssehub.kernel_haven.util.io.csv.CsvWriter;

public class CSVMerger {

    // log interval in seconds
    static int LOG_INTERVAL = 30;
    
    static List<String[]> inp;
    static int inpSize;
    static int[] colsA;
    static int[] colsB;
    static int bSize;
    static int pos;
    
    static ProcessedFunctionCommits commits;
    
    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            help();
            return;
        }
        
        File inputA = new File(args[0]);
        File inputB = new File(args[2]);
        File output = new File(args[4]);
        
        colsA = getCols(args[1]);
        colsB = getCols(args[3]);
        
        if (colsA.length != colsB.length) {
            System.out.println("Error!\nInvalid cols length");
            return;
        }
        
        commits = new ProcessedFunctionCommits();
        
        readInputA(inputA);
        System.out.println("Input A has " + inpSize + " rows.");
        bSize = countLines(args[2]) - 1;
        System.out.println("Input B has " + bSize + " rows.");
        
        int cnt = 0;
        int i = 0;
        long millis = System.currentTimeMillis();
        long lastmsg = millis;
        try (CsvReader reader = new CsvReader(new FileInputStream(inputB)); CsvWriter writer = new CsvWriter(new FileOutputStream(output), ',')) {
            pos = 0;
            String[] line = reader.readNextRow();
            writer.writeRow((Object[]) line);
            while ((line = reader.readNextRow()) != null) {
                if (commits.functionProcessed("", line[0], line[2], line[1])) {
                    continue;
                }
                String id = checkRowMatch(line);
                if (id != null) {
                    Object[] result = new Object[line.length + 1];
                    result[0] = id;
                    System.arraycopy(line, 0, result, 1, line.length);
//                    writer.writeRow((Object[]) line);
                    writer.writeRow(result);
                    cnt++;
                }
                if (++i % 1000 > 0)
                    continue;
                long time = System.currentTimeMillis();
                if (time - lastmsg >= LOG_INTERVAL * 1000) {
                    System.out.println("Time: " + ((time - millis) / 1000) + 
                            " seconds; " + (i * 100 / ((double) bSize)) + "%");
                    lastmsg = time;
                }
            }
        }
        System.out.println("\nMerged " + cnt + " / " + i + " rows in " + ((System.currentTimeMillis() - millis) / 1000) + " seconds.");
    }
    
    /**
     * Read a file and initialize some variables.
     * @param inputA File to read
     * @throws Exception
     */
    public static void readInputA(File inputA) throws Exception {
        inp = new ArrayList<>();
        try (CsvReader reader = new CsvReader(new FileInputStream(inputA))) {
            reader.readNextRow();
            String[] line;
            while ((line = reader.readNextRow()) != null) {
                inp.add(line);
            }
        }
        inpSize = inp.size();
    }
    
    /**
     * Check if row can be found in smaller input
     * @param lineB A line to search for in the big file
     * @return <tt>true</tt> if line was found, <tt>false</tt> otherwise
     */
    public static String checkRowMatch(String[] lineB) {
        for (int j = pos; j < inpSize; j++) {
            boolean ok = true;
            for (int i = 0; i < colsA.length; i++) {
                if (!inp.get(j)[colsA[i]].equals(lineB[colsB[i]])) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                pos = j;
                return inp.get(j)[0];
            }
        }
        return null;
    }
        
    /**
     * Split comma separated column indices for input to integer array
     * @param cols  comma separated column indices ("1,2,3")
     * @return integer array
     */
    public static int[] getCols(String cols) {
        String[] colStrs = cols.split(",");
        int[] cls = new int[colStrs.length];
        for (int i = 0; i < colStrs.length; i++) {
            cls[i] = Integer.parseInt(colStrs[i]);
        }
        return cls;
    }

    /**
     * Counts the lines in a file
     * @param file Name of the file
     * @return Number of lines in that file
     * @throws IOException
     */
    public static int countLines(String file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int lines = 0;
            while (reader.readLine() != null) 
                lines++;
            return lines;
        }
    }

    
    /**
     * Help screen
     */
    public static void help() {
        System.out.println("Expected syntax: <inputA> <colsA> <inputB> <colsB> <output>");
        System.out.println("    inputA: small csv file");
        System.out.println("    colsA: comma separated column indices for inputA (zero based)");
        System.out.println("    inputB: big csv file");
        System.out.println("    colsB: comma separated column indices for inputB (zero based)");
        System.out.println("    output: output csv file");
    }
}
