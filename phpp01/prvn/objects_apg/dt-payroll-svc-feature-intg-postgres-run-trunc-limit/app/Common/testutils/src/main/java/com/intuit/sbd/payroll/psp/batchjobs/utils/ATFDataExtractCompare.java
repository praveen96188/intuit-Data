package com.intuit.sbd.payroll.psp.batchjobs.utils;

import org.apache.commons.lang.StringUtils;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 15, 2009
 * Time: 3:07:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ATFDataExtractCompare {
    private static TreeMap metaData = new TreeMap();
    private static ArrayList failureReasons = new ArrayList();
    private static int currentLine = 0;

    //maintain list of all the field positions which needs to ignore for comparision from the file
    private static final String IGNORE_FIELD_INFO = "CO_INFO:1;EE_INFO:1;EE_STDTL:1;TLR:2;CO_TAX:1;CO_TAX_RATE:1;CO_DEP_FREQ:1;CO_TXACI:1;EE_TOT:1;CO_LIA:1;CO_PAY:1;CO_ADDL_INFO:1;CO_W2_COUNT:1;CO_PAYROLL_ITEM:1";

    /**
     * This method compares two buffered readers line by line.
     *
     * @param inFile      - Expected file
     * @param compareFile - File being compared to the standard
     * @return CompareResults object containing reasons for failure.
     */
    public CompareResults compareATFDataExtractFile(BufferedReader inFile, BufferedReader compareFile) {
        metaData = getExtractFileCompareIngnoreFieldProps(IGNORE_FIELD_INFO);

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

    private boolean equalsLine(String inLine, String compareLine) {

        boolean same = true;
        String[] record = null;
        String ignoreFieldPosition = null;
        String inLineToken = null;
        String compareLineToken = null;
        int index = 1;
        if (inLine != null && compareLine != null) {
            //Since every line in the file starts with '"' and '"', ignore first & last characters from  each line.
            inLine = inLine.substring(1, inLine.length() - 1);
            compareLine = compareLine.substring(1, compareLine.length() - 1);

            StringTokenizer inLineTokenizer = new StringTokenizer(inLine, "\",\"");
            StringTokenizer compareLineTokenizer = new StringTokenizer(compareLine, "\",\"");

            int compareTknCount = compareLineTokenizer.countTokens();
            // Special handling for "CO_PAY" records
            if(compareTknCount > 0) {
                compareLineToken = compareLineTokenizer.nextToken();
                if (StringUtils.equals(compareLineToken, "CO_PAY")) {
                    compareTknCount--;
                }
            }

            // compare length
            if (inLineTokenizer.countTokens() == compareTknCount) {
                //get the first token for extract type id
                inLineToken = inLineTokenizer.nextToken();

                // compare record type
                if (inLineToken.equals(compareLineToken)) {
                    //Ignore the comparision for HDR record
                    if (!inLineToken.equals("HDR")) {
                        // get record information
                        record = (String[]) metaData.get(inLineToken);

                        while (inLineTokenizer.hasMoreTokens()) {
                            if (record.length >= index) {
                                //get the ignore field position from the record[] for the specified extract type
                                ignoreFieldPosition = record[index-1];
                            }
                            if (ignoreFieldPosition != null) {
                                //the token index and ingore field position matches ignore the comparision
                                if (Integer.parseInt(ignoreFieldPosition) == index) {
                                    inLineToken = inLineTokenizer.nextToken();
                                    compareLineToken = compareLineTokenizer.nextToken();
                                    index++;
                                    continue;
                                }

                                inLineToken = inLineTokenizer.nextToken();
                                compareLineToken = compareLineTokenizer.nextToken();
                                
                                if (!inLineToken.equals(compareLineToken)) {
                                    failureReasons.add("Line " + currentLine + ", does not match. " + "Found " + compareLineToken + " expected " + inLineToken + ".");
                                    same = false;
                                }
                            }
                            index++;
                        }
                    }
                } else {
                    failureReasons.add("Line " + currentLine + " record types do not match. Found type" +
                            compareLineToken + " expected type " + inLineToken);
                    same = false;
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

    //Method to load data required for Extract file fields to ignore
    private TreeMap getExtractFileCompareIngnoreFieldProps(String data) {
        TreeMap<String,String[]> metaData = new TreeMap<String, String[]>();
        if (data != null && data.length() > 0) {
            String tempArray1[] = null;
            String tempArray2[] = null;

            String FileStrings[] = data.split(";");
            for (String FileString : FileStrings) {
                tempArray1 = FileString.trim().split(":");
                tempArray2 = tempArray1[1].trim().split(",");
                metaData.put(tempArray1[0], tempArray2);
            }
        }
        return metaData;
    }
}
