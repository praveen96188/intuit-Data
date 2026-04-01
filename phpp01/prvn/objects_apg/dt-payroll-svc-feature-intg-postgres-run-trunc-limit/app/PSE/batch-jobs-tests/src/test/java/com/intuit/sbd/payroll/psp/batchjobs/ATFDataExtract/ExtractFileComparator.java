package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Jan 25, 2012
 * Time: 2:46:01 PM
 */

public class ExtractFileComparator {

    public static void main(String args[]) {

        if (args.length < 2) {
            System.out.println("Usage <LHSFileName> <RHSFileName> [<boolean-EE-Totals>]");
            System.exit(0);
        }

        boolean ignoreWeeksWorked = false;
        if (args.length == 3) {
            ignoreWeeksWorked = Boolean.valueOf(args[2]);
        }

        String lhsFileName = args[0];
        String rhsFileName = args[1];

        ExtractFileComparator extractFileComparator = new ExtractFileComparator();
        CompareResults results;
        if (ignoreWeeksWorked) {
            //EE -Totals, WeeksWorked is the 12th field to ignore
            results = extractFileComparator.compareExtractFiles(lhsFileName, rhsFileName, 12);
        } else {
            results = extractFileComparator.compareExtractFiles(lhsFileName, rhsFileName);
        }

        System.out.print(results);
        
        System.out.println("File Compare is completed.. ");

    }

    public CompareResults compareExtractFiles(String pLhsFileName, String pRhsFileName) {
        return compareExtractFiles(pLhsFileName, pRhsFileName, -1);
    }

    public CompareResults compareExtractFiles(String pLhsFileName, String pRhsFileName, int ignoreField) {
        CompareResults results = new CompareResults();
        try {
            List<String> lhsFileContents = getFileContents(pLhsFileName, ignoreField);
            List<String> rhsFileContents = getFileContents(pRhsFileName, ignoreField);

            if (lhsFileContents.containsAll(rhsFileContents) && rhsFileContents.containsAll(lhsFileContents)) {
                results.setFileMatched(true);
            } else {
                results.setFileMatched(false);
                for (String line : lhsFileContents) {
                    if(rhsFileContents.contains(line)) {
                        rhsFileContents.remove(line);
                        results.matchedLinesCount++;
                    } else {
                        results.addLhsFileLine(line);
                    }
                }

                for (String line : rhsFileContents) {
                    if (!lhsFileContents.contains(line)) {
                        results.addRhsFileLine(line);
                    }
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return results;
    }

    public static List<String> getFileContents(String pFileName, int pIgnoreField) throws Throwable {
        List<String> lines = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(pFileName));

        String mNextLine = bufferedReader.readLine();
        while (mNextLine != null) {
            //Ignore the HDR and TLR lines
            if (!mNextLine.startsWith("\"HDR\"") && !mNextLine.startsWith("\"TLR\"")) {
                if (pIgnoreField > 0) {
                    StringBuilder lineContents = new StringBuilder(mNextLine);
                    int counter = (pIgnoreField - 1) * 2 + 1;
                    int startIndex = nthOccurrence(mNextLine, '"', counter) + 1;

                    lines.add(lineContents.replace(startIndex, nthOccurrence(mNextLine, startIndex, '"', 0, 1), "*").toString());

                } else {
                    lines.add(mNextLine);
                }
            }

            mNextLine = bufferedReader.readLine();

        }
        bufferedReader.close();

        return lines;
    }

    public static int nthOccurrence(String s, char c, int occurrence) {
        return nthOccurrence(s, 0, c, 0, occurrence);
    }

    public static int nthOccurrence(String s, int from, char c, int curr, int expected) {
        final int index = s.indexOf(c, from);
        if (index == -1) return -1;
        return (curr + 1 == expected) ? index :
                nthOccurrence(s, index + 1, c, curr + 1, expected);
    }

    public class CompareResults {
        boolean fileMatched;
        List<String> lhsFileDifferenceContents = new ArrayList<String>();
        List<String> rhsFileDifferenceContents = new ArrayList<String>();
        int matchedLinesCount = 0;

        public void addLhsFileLine(String pLine) {
            lhsFileDifferenceContents.add(pLine);
        }

        public void addRhsFileLine(String pLine) {
            rhsFileDifferenceContents.add(pLine);
        }

        public boolean isFileMatched() {
            return fileMatched;
        }

        public void setFileMatched(boolean pFileMatched) {
            fileMatched = pFileMatched;
        }

        @Override
        public String toString() {
            StringBuilder differenceText = new StringBuilder();

            if (fileMatched) {
                differenceText.append("Files matched !!\n");
            } else {
                differenceText.append("Files does not match.. Lines Matched count:"+matchedLinesCount + "\n");

                int count = 0;
                differenceText.append("\r\nIn LHS file but not in RHS file Count:" + lhsFileDifferenceContents.size() + "\n");
                for (String line : lhsFileDifferenceContents) {
                    differenceText.append(++count + ": " + line + "\n");
                }

                count = 0;
                differenceText.append("\r\nIn RHS file but not in LHS file Count:" + rhsFileDifferenceContents.size() + "\n");
                for (String line : rhsFileDifferenceContents) {
                    differenceText.append(++count + ": " + line + "\n");
                }
            }
            return differenceText.toString();
        }
    }
}

