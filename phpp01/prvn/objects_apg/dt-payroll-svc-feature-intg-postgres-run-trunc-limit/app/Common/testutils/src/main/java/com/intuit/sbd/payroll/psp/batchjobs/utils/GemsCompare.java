package com.intuit.sbd.payroll.psp.batchjobs.utils;

import com.intuit.sbd.payroll.psp.domain.ReportingFrequency;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: May 28, 2008
 * Time: 2:34:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GemsCompare {
    private static TreeMap metaData = new TreeMap();
    private static ArrayList failureReasons = new ArrayList();
    private static int currentLine = 0;

    private static String GEMS_FILE_LAYOUT = "Daily:1,19,ignore,20,6,Sku Name,27,4,Sku Quantity,32,13,Income Amount,46,10,Tax Amount,57,6,upload date;";
    public String GEMS_MONTHLY_FILE_LAYOUT = "Monthly:1,3,Company,5,4,Department,10,5,Account,16,3,Inter Company,20,13,Reported balance,34,6,upload date;";

    /**
     * This method compares two buffered readers line by line.
     *
     * @param inFile         - "Gold Standard" file
     * @param compareFile    - File being compared to the standard
     * @param pFrequencyType - ReportingFrequency Daily/Monthly
     * @return CompareResults object containing reasons for failure.
     */
    public CompareResults compareGemsUploadFile(BufferedReader inFile, BufferedReader compareFile, ReportingFrequency pFrequencyType) {
        TreeMap compareProps = getGemsUploadFileCompareProps(GEMS_FILE_LAYOUT);
        metaData = (TreeMap) compareProps.get(pFrequencyType.toString());

        // reset static vars
        failureReasons = new ArrayList();
        currentLine = 0;

        boolean valid = true;
        boolean eof = false;
        CompareResults result = new CompareResults();
        try {
            while (!eof) {
                // read one line at a time
                String compareLine = compareFile.readLine();
                String line = inFile.readLine();

                // check for EOF
                if (compareLine == null || line == null) {
                    if (compareLine == null && line == null)
                        eof = true;
                    else {
                        failureReasons.add("The files being compared are not the same length.");
                        valid = false;
                        eof = true;
                    }
                } else {
                    // if valid is changed to false it cannot be changed back to true
                    if (valid)
                        valid = equalsLine(line, compareLine);
                    else
                        equalsLine(line, compareLine);
                }
                // increment current line
                currentLine++;
            }

            // return results
            result.setReasons(failureReasons);
            result.setStatus(valid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * This method compares String content with buffered reader line by line.
     *
     * @param pExpectedContent         - "Gold Standard" file content
     * @param compareFile    - File being compared to the standard
     * @param pFrequencyType - ReportingFrequency Daily/Monthly
     * @return CompareResults object containing reasons for failure.
     */
    public CompareResults compareGemsUploadFile(String pExpectedContent, BufferedReader compareFile, ReportingFrequency pFrequencyType) {
        TreeMap compareProps = getGemsUploadFileCompareProps(GEMS_FILE_LAYOUT);
        metaData = (TreeMap) compareProps.get(pFrequencyType.toString());

        // reset static vars
        failureReasons = new ArrayList();
        currentLine = 0;

        boolean valid = true;
        boolean eof = false;
        CompareResults result = new CompareResults();
        try {

            StringTokenizer stringTokenizer = new StringTokenizer(pExpectedContent, "\\n");

            while (!eof) {
                // read one line at a time
                String compareLine = compareFile.readLine();
                String line = null;
                if (stringTokenizer.hasMoreTokens()) {
                    line = stringTokenizer.nextToken();
                }

                // check for EOF
                if (compareLine == null || line == null) {
                    if (compareLine == null && line == null)
                        eof = true;
                    else {
                        failureReasons.add("The files being compared are not the same length.");
                        valid = false;
                        eof = true;
                    }
                } else {
                    // if valid is changed to false it cannot be changed back to true
                    if (valid)
                        valid = equalsLine(line, compareLine);
                    else
                        equalsLine(line, compareLine);
                }
                // increment current line
                currentLine++;
            }

            // return results
            result.setReasons(failureReasons);
            result.setStatus(valid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Compare the generated content for GEMS monthly file with the Expected String.
     * This method compares String content with buffered reader line by line.
     *
     * @param pExpectedContent         - "Gold Standard" file content
     * @param compareFile    - File being compared to the standard
     * @param pFrequencyType - ReportingFrequency Daily/Monthly
     * @return CompareResults object containing reasons for failure.
     */
    public CompareResults compareGemsMonthlyFile(String pExpectedContent, BufferedReader compareFile, ReportingFrequency pFrequencyType) {
        TreeMap compareProps = getGemsUploadFileCompareProps(GEMS_MONTHLY_FILE_LAYOUT);
        metaData = (TreeMap) compareProps.get(pFrequencyType.toString());

        // reset static vars
        failureReasons = new ArrayList();
        currentLine = 0;

        boolean valid = true;
        boolean eof = false;
        CompareResults result = new CompareResults();
        try {

            StringTokenizer stringTokenizer = new StringTokenizer(pExpectedContent, "\\n");

            while (!eof) {
                // read one line at a time
                String compareLine = compareFile.readLine();
                if (null != compareLine) {
                    compareLine = compareLine.substring(compareLine.indexOf(",") + 1);
                }
                String line = null;
                if (stringTokenizer.hasMoreTokens()) {
                    line = stringTokenizer.nextToken();
                }

                // check for EOF
                if (compareLine == null) {
                        eof = true;
                } else {
                    // if valid is changed to false it cannot be changed back to true
                    if (pExpectedContent.indexOf(compareLine) != -1)
                        valid = true;
                    else {
                        valid = false;
                        failureReasons.add("Line " + currentLine + "does not have the expected content");
                    }
                }
                // increment current line
                currentLine++;
            }

            // return results
            result.setReasons(failureReasons);
            result.setStatus(valid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean equalsLine(String inLine, String compareLine) {
        StringTokenizer inLineTokenizer = new StringTokenizer(inLine, ",");
        StringTokenizer compareLineTokenizer = new StringTokenizer(compareLine, ",");
        boolean same = true;

        String[] metaData = new String[] {"ignore","ignore","ignore","SKU Name","SKU Quantity","Income Amount","Tax Amount","Upload Date"};
        String inLineToken = null;
        String compareLineToken = null;
        String skuName = null;

        if (inLine != null && compareLine != null) {
            // compare length

            if (inLineTokenizer.countTokens() == compareLineTokenizer.countTokens()) {
                for (int i = 0; i < metaData.length; i++) {
                    if (metaData[i].equals("ignore")) {
                        inLineToken = inLineTokenizer.nextToken();
                        compareLineToken = compareLineTokenizer.nextToken();
                        continue;
                    }

                    inLineToken = inLineTokenizer.nextToken();
                    compareLineToken = compareLineTokenizer.nextToken();

                    if (metaData[i].equals("SKU Name")) {
                        skuName = inLineToken;   
                    }

                    if (metaData[i].equals("Income Amount") && ("293935".equals(skuName) || "408178".equals(skuName))) {
                        // SKU 293935 is for bank verification debits...
                        // these amounts are random so, we skip the "Income Amount" comparison for this SKU
                    } else {
                        // compare substrings
                        if (!inLineToken.equals(compareLineToken)) {
                            failureReasons.add("Line " + currentLine +" "+ inLineToken + " does not match. " + "Found " + compareLineToken + " expected " + inLineToken + ".");
                            same = false;
                        }
                    }

                }
            } else {
                failureReasons.add("Line " + currentLine + " is not the same length");
                same = false;
            }
        } else {
            failureReasons.add("Line " + currentLine + " is null");
            same = false;
        }
        return same;
    }
    
    //Method to load data required for Gems Upload
    private TreeMap getGemsUploadFileCompareProps(String data) {
        TreeMap metaData = new TreeMap();
        if (data != null && data.length() > 0) {
            String tempArray1[] = null;
            String tempArray2[] = null;
            String tempArray3[] = null;
            TreeMap tempMap1 = new TreeMap();
            TreeMap tempMap2 = new TreeMap();

            String FileStrings[] = data.split(";");
            for (int h = 0; h < FileStrings.length; h++) {
                tempArray1 = FileStrings[h].trim().split(":");
                tempArray2 = tempArray1[1].trim().split(",");
                tempMap2 = new TreeMap();
                for (int i = 0; i < tempArray2.length; i += 3) {
                    tempArray3 = new String[2];
                    for (int j = 0; j < 2; j++)
                        tempArray3[j] = tempArray2[(j + i + 1)].trim();
                    //tempMap1.put(tempArray2[i + 1].trim(), tempArray3);
                    tempMap2.put(tempArray2[i].trim(), tempArray3);
                }
                metaData.put(tempArray1[0], tempMap2);
            }
        }
        return metaData;
    }
}
