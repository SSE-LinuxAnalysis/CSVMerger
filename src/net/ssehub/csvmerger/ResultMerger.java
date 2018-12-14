package net.ssehub.csvmerger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.ssehub.kernel_haven.io.excel.ExcelBook;
import net.ssehub.kernel_haven.io.excel.ExcelSheetReader;
import net.ssehub.kernel_haven.io.excel.ExcelSheetWriter;

public class ResultMerger {

    public static File leftFolder;
    public static File rightFolder;
    public static File outFolder;
    
    public static List<File> leftFiles;
    public static List<File> rightFiles;
    public static HashMap<File, File> leftRightFiles;
    
    public static List<String[]> rightContent;
    
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            help();
            return;
        }
        
        leftFolder = new File(args[0]);
        rightFolder = new File(args[1]);
        outFolder = new File(args[2]);
        if (!leftFolder.isDirectory() || !rightFolder.isDirectory() || !outFolder.isDirectory()) {
            System.out.println("the specified folders do not exist");
            return;
        }
        
        leftFiles = ExcelMerger.getXLSXFiles(leftFolder);
        rightFiles = ExcelMerger.getXLSXFiles(rightFolder);
        
        leftRightFiles = new HashMap<>();
        for (File fl : leftFiles) {
            for (File fr : rightFiles) {
                if (fl.getName().equals(fr.getName())) {
                    if (leftRightFiles.containsKey(fl)) {
                        System.out.println("Error! Each file name can only be used once!");
                        return;
                    }
                    leftRightFiles.put(fl, fr);
                }
            }
        }
        
        for (File left : leftRightFiles.keySet()) {
            File right = leftRightFiles.get(left);
            System.out.println(left.getName());
            handleFile(left, right);
        }
    }
    
    public static void handleFile(File left, File right) throws Exception {
        String[] rightHeader;
        String[] leftHeader;
        rightContent = new ArrayList<>();
        try (ExcelBook book = new ExcelBook(right)) {
            ExcelSheetReader reader = book.getReader(0);
            rightHeader = reader.readNextRow();
            String[] row;
            while ((row = reader.readNextRow()) != null) {
                rightContent.add(row);
            }
        }

        try (ExcelBook bookw = new ExcelBook(new File(outFolder, left.getName()))) {
            ExcelSheetWriter writer = bookw.getWriter("Result");
            try (ExcelBook bookr = new ExcelBook(left)) {
                ExcelSheetReader reader = bookr.getReader(0);
                
                leftHeader = reader.readNextRow();
                int rightStart = leftHeader.length;
                Object[] outputRow = new String[leftHeader.length + rightHeader.length - 7];
                System.arraycopy(leftHeader, 0, outputRow, 0, leftHeader.length);
                System.arraycopy(rightHeader, 7, outputRow, rightStart, rightHeader.length - 7);
                writer.writeHeader(outputRow);
                
                String[] leftRow;
                int lastIndex = -1;
                while ((leftRow = reader.readNextRow()) != null) {
                    int rightRowIndex = findRowInRight(leftRow[4], leftRow[5], leftRow[6], lastIndex + 1);
                    if (rightRowIndex != -1) {
                        lastIndex = rightRowIndex;
                        String[] rightRow = rightContent.get(rightRowIndex);
                        outputRow = new String[rightStart + rightRow.length - 7];
                        System.arraycopy(leftRow, 0, outputRow, 0, leftRow.length);
                        System.arraycopy(rightRow, 7, outputRow, rightStart, rightRow.length - 7);
                        writer.writeRow(outputRow);
                    } else {
                        writer.writeRow((Object[]) leftRow);
                    }
                }
            }
        }
    }
    
    /**
     * Search for row in right file
     * @param sourceFile Source File
     * @param lineNumber Line No
     * @param element Element
     * @param startFrom row index where to start from
     * @return row index of found row or -1
     */
    public static int findRowInRight(String sourceFile, String lineNumber, String element, int startFrom) {
        for (int i = startFrom; i < rightContent.size(); i++) {
            String[] row = rightContent.get(i);
            if (sourceFile.equals(row[4]) && lineNumber.equals(row[5]) && element.equals(row[6])) {
                return i;
            }
        }
        return -1;
    }
        
    /**
     * Help screen
     */
    public static void help() {
        System.out.println("Expected syntax: <old result folder (left)> <new result folder (right)> <output folder>");
    }
}
