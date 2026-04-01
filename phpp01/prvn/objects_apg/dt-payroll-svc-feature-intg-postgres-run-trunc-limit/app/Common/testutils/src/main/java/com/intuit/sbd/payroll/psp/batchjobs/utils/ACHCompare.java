package com.intuit.sbd.payroll.psp.batchjobs.utils;


import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.domain.NACHAFileType;

import java.io.BufferedReader;
import java.util.*;

/**
 * @author Zack Norcross, Dawn Martens
 * @version 1.0
 */
public class ACHCompare {
    private static HashMap metaData = new HashMap();
    private static ArrayList failureReasons = new ArrayList();
    private static int currentLine = 0;

    private static String ACH_FILE_LAYOUT = "CCD:1,2,2,Priority Code,1,4,10,Imediate Destination,1,14,10,Immediate Origin,1,24,6,File Creation Date,1,30,4,File Creation Time,1,34,1,ignore,1,35,3,Record Size,1,38,2,Blocking Factor,1,40,1,Format Code,1,41,23,Imediate Destination Name,1,64,23,Immediate Origin Name,1,87,8,Reference Code,5,2,3,Service Class Code,5,5,16,Company Name,5,21,20,Company Discretion,5,41,10,Company ID,5,51,3,Standard Entry Class Code,5,54,10,CompanyEntry Description,5,64,6,Company Descriptive Date,5,70,6,Effective Entry Date,5,76,3,Settlement Date,5,79,1,Originator Status Code,5,80,8,Originating DFI ID,5,88,7,Batch Number,6,2,2,TransAction Code,6,4,8,Reciving DFI ID,6,12,1,Check Digit,6,13,17,DFI AccountNumber,6,30,10,Amount,6,40,15,ID Number,6,55,22,Reciving Company Name,6,77,2,Discretionary Data,6,79,1,Addenda Record Indicator,6,80,15,Trace Number,7,2,2,Addenda Type Code,7,4,80,Payment Related Information,7,84,4,Addenda Sequence Number,7,88,7,ignore,8,2,3,Service Classe Code,8,5,6,Entry/Agenda Count,8,11,10,Entry Hash,8,21,12,Total Debit Entry Dollar Amount,8,33,12,Total Credit Entry Dollar Amount,8,45,10,Company ID,8,55,19,Message Authentication Code,8,74,6,Reserved,8,80,8,Originating DFI ID,8,88,7,Batch Number,9,2,6,Batch Count,9,8,6,Block Count,9,14,8,Entry/Addenda Count,9,22,10,Entry Hash,9,32,12,Total Debit Entry Dollar Amount In File,9,44,12,Total Credit Entry Dollar Amount In File,9,56,39,Reserved;PPD:1,2,2,Priority Code,1,4,10,Imediate Destination,1,14,10,Immediate Origin,1,24,6,File Creation Date,1,30,4,File Creation Time,1,34,1,ignore,1,35,3,Record Size,1,38,2,Blocking Factor,1,40,1,Format Code,1,41,23,Imediate Destination Name,1,64,23,Immediate Origin Name,1,87,8,Reference Code,5,2,3,Service Class Code,5,5,16,Company Name,5,21,20,Company Discretion,5,41,10,Company ID,5,51,3,Standard Entry Class Code,5,54,10,CompanyEntry Description,5,64,6,Company Descriptive Date,5,70,6,Effective Entry Date,5,76,3,Settlement Date,5,79,1,Originator Status Code,5,80,8,Originating DFI ID,5,88,7,Batch Number,6,2,2,TransAction Code,6,4,8,Reciving DFI ID,6,12,1,Check Digit,6,13,17,DFI AccountNumber,6,30,10,Amount,6,40,15,Individual ID Number,6,55,22,Individual Name,6,77,2,Discretionary Data,6,79,1,Addenda Record Indicator,6,80,15,Trace Number,7,2,2,Addenda Type Code,7,4,80,Payment Related Information,7,84,4,Addenda Sequence Number,7,88,7,ignore,8,2,3,Service Classe Code,8,5,6,Entry/Agenda Count,8,11,10,Entry Hash,8,21,12,Total Debit Entry Dollar Amount,8,33,12,Total Credit Entry Dollar Amount,8,45,10,Company ID,8,55,19,Message Authentication Code,8,74,6,Reserved,8,80,8,Originating DFI ID,8,88,7,Batch Number,9,2,6,Batch Count,9,8,6,Block Count,9,14,8,Entry/Addenda Count,9,22,10,Entry Hash,9,32,12,Total Debit Entry Dollar Amount In File,9,44,12,Total Credit Entry Dollar Amount In File,9,56,39,Reserved;CCDPlus:1,2,2,Priority Code,1,4,10,Imediate Destination,1,14,10,Immediate Origin,1,24,6,File Creation Date,1,30,4,File Creation Time,1,34,1,ignore,1,35,3,Record Size,1,38,2,Blocking Factor,1,40,1,Format Code,1,41,23,Imediate Destination Name,1,64,23,Immediate Origin Name,1,87,8,Reference Code,5,2,3,Service Class Code,5,5,16,Company Name,5,21,20,PSIDEIN,5,41,10,EIN,5,51,3,Standard Entry Class Code,5,54,10,CompanyEntry Description,5,64,6,Company Descriptive Date,5,70,6,Effective Entry Date,5,76,3,Settlement Date,5,79,1,Originator Status Code,5,80,8,Originating DFI ID,5,88,7,Batch Number,6,2,2,TransAction Code,6,4,8,Reciving DFI ID,6,12,1,Check Digit,6,13,17,DFI AccountNumber,6,30,10,Amount,6,40,15,PSID,6,55,22,Reciving Company Name,6,77,2,Discretionary Data,6,79,1,Addenda Record Indicator,6,80,15,Trace Number,7,2,2,Addenda Type Code,7,4,80,Payment Related Information,7,84,4,Addenda Sequence Number,7,88,7,ignore,8,2,3,Service Classe Code,8,5,6,Entry/Agenda Count,8,11,10,Entry Hash,8,21,12,Total Debit Entry Dollar Amount,8,33,12,Total Credit Entry Dollar Amount,8,45,10,Company ID,8,55,19,Message Authentication Code,8,74,6,Reserved,8,80,8,Originating DFI ID,8,88,7,Batch Number,9,2,6,Batch Count,9,8,6,Block Count,9,14,8,Entry/Addenda Count,9,22,10,Entry Hash,9,32,12,Total Debit Entry Dollar Amount In File,9,44,12,Total Credit Entry Dollar Amount In File,9,56,39,Reserved";

    private HashMap<String, String> recordsTraceNumbers = new HashMap<String, String>();
    private HashMap<String, String> psIds = new HashMap<String, String>();
    private HashMap<String, String> psIdsWithEINs = new HashMap<String, String>();
    private ArrayList<String> traceNumbers = new ArrayList<String>();
    private int recordLength = 79;

    public ACHCompare() {

        super();
    }

    /**This method compares two buffered readers line by line.
     * @param inFile - "Gold Standard" file
     * @param compareFile - File being compared to the standard
     * @param pFileType - NACHA File Type
     * @return CompareResults object containing reasons for failure.
     */
    public CompareResults compareACH(BufferedReader inFile, PgpReader compareFile, NACHAFileType pFileType){
        HashMap compareProps = getACHCompareProps(ACH_FILE_LAYOUT);
        metaData = (HashMap)compareProps.get(pFileType.toString());

        // reset static vars
        failureReasons = new ArrayList();
        currentLine = 0;

        recordsTraceNumbers = new HashMap<String, String>();
        psIds = new HashMap<String, String>();
        psIdsWithEINs = new HashMap<String, String>();

        boolean valid = true;
        boolean eof = false;
        CompareResults result = new CompareResults();

        List<String> expectedSet = new ArrayList<String>();
        List<String> actualSet = new ArrayList<String>();
        try {
            while(!eof)
            {
                // read one line at a time
                String compareLine = compareFile.readLine();
                String line = inFile.readLine();

                // check for EOF
                if(compareLine == null || line == null){
                    if(compareLine == null && line == null)
                        eof = true;
                    else{
                        failureReasons.add("The files being compared are not the same length.");
                        valid = false;
                        eof = true;
                    }
                } else {
                    actualSet.add(line);
                    expectedSet.add(compareLine);
                }
                // increment current line
                currentLine++;
            }

            if(valid) {
                Collections.sort(actualSet);
                Collections.sort(expectedSet);
                String actualLine = null;
                String expectedLine = null;
                for(int i=0; i<actualSet.size()-1;i++) {
                    actualLine = actualSet.get(i);
                    expectedLine = expectedSet.get(i);
                    valid = equalsLine(actualLine, expectedLine);
                }
            }

            // return results
            result.setReasons(failureReasons);
            result.setStatus(valid);
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**This method compares two buffered readers line by line.
     * @param inFile - "Gold Standard" file
     * @param compareFile - File being compared to the standard
     * @param pFileType - NACHA File Type
     * @return CompareResults object containing reasons for failure.
     */
    public CompareResults compareACH(BufferedReader inFile, BufferedReader compareFile, NACHAFileType pFileType){
        HashMap compareProps = getACHCompareProps(ACH_FILE_LAYOUT);
        metaData = (HashMap)compareProps.get(pFileType.toString());

        // reset static vars
        failureReasons = new ArrayList();
        currentLine = 0;

        recordsTraceNumbers = new HashMap<String, String>();
        psIds = new HashMap<String, String>();
        psIdsWithEINs = new HashMap<String, String>();

        boolean valid = true;
        boolean eof = false;
        CompareResults result = new CompareResults();
        try {
            while(!eof)
            {
                // read one line at a time
                String compareLine = compareFile.readLine();
                String line = inFile.readLine();

                // check for EOF
                if(compareLine == null || line == null){
                    if(compareLine == null && line == null)
                        eof = true;
                    else{
                        failureReasons.add("The files being compared are not the same length.");
                        valid = false;
                        eof = true;
                    }
                }
                else{
                    // if valid is changed to false it cannot be changed back to true
                    if(valid)
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
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**This method compares two buffered readers line by line.
     * @param pExpectedContent - "Gold Standard" file
     * @param pCompareFile - File being compared to the standard
     * @param pFileType - NACHA File type
     * @return CompareResults object containing reasons for failure.
     */
    public CompareResults compareACH(String pExpectedContent, BufferedReader pCompareFile, NACHAFileType pFileType){
        HashMap compareProps = getACHCompareProps(ACH_FILE_LAYOUT);
        metaData = (HashMap)compareProps.get(pFileType.toString());

        // reset static vars
        failureReasons = new ArrayList();
        currentLine = 0;

        recordsTraceNumbers = new HashMap<String, String>();

        boolean valid = true;
        boolean eof = false;
        CompareResults result = new CompareResults();
        try {

            int i=0;
            String[] expectedFileLines = pExpectedContent.split("\\\\n");

            while(!eof)
            {
                // read one line at a time
                String compareLine = pCompareFile.readLine();
                String line = null;
                if(i < expectedFileLines.length) {
                    line = expectedFileLines[i++];
                }

                // check for EOF
                if(compareLine == null || line == null){
                    if(compareLine == null && line == null)
                        eof = true;
                    else{
                        failureReasons.add("The files being compared are not the same length.");
                        valid = false;
                        eof = true;
                    }
                }
                else{
                    // if valid is changed to false it cannot be changed back to true
                    if(valid)
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
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }


    public HashMap<String, String> getRecordTraceNumMap() {
        return recordsTraceNumbers;
    }

    public ArrayList<String> getTraceNumbers() {
        return traceNumbers;
    }

    /**This method compares one line of a ACH file to another. It uses the metaData hashmap
     * to distinguish record and field types
     * @param inLine - "Gold standard"
     * @param compareLine - Line being compared to the standard
     * @return boolean: True - the lines are the same; False - the lines are not the same
     */
    private boolean equalsLine(String inLine, String compareLine){
        boolean same = true;
        int length = 0;
        HashMap record = new HashMap();
        String fieldInfo[] = null;
        String fieldName = null;
        String compareStr = null;
        String foundStr = null;
        String psIdEin = null;
        String ein = null;

        if(inLine!= null && compareLine != null){
            // compare length
            if(inLine.length() == compareLine.length()){
                // compare record type
                if(inLine.charAt(0) == compareLine.charAt(0)){
                    // get record information
                    record = (HashMap)metaData.get(inLine.substring(0,1));
                    if(record != null){
                        psIdEin = null;
                        ein = null;
                        for(int i=1; i<inLine.length(); i++){
                            // get field information (name and length)
                            fieldInfo = (String[])record.get(Integer.toString(i+1));
                            if(fieldInfo != null){
                                length = Integer.parseInt(fieldInfo[0]);
                                fieldName = fieldInfo[1];
                                // skip fields marked ignore
                                if (fieldName.equalsIgnoreCase("Trace Number")) {
                                    String traceNumber = compareLine.substring(i,i+length);
                                    String recordData = compareLine.substring(0,i);
                                    recordsTraceNumbers.put(recordData, traceNumber);
                                    traceNumbers.add(traceNumber);
                                } else if (fieldName.equalsIgnoreCase("PSID")) {
                                    String traceNumber = compareLine.substring(i,i+length);
                                    String recordData = compareLine.substring(0,recordLength);
                                    psIds.put(recordData, traceNumber);
                                } else if (fieldName.equalsIgnoreCase("PSIDEIN")) {
                                    psIdEin = compareLine.substring(i,i+length);
                                } else if(!fieldName.equalsIgnoreCase("ignore")){
                                    foundStr = inLine.substring(i,i+length);
                                    compareStr = compareLine.substring(i,i+length);
                                    if(fieldName.equalsIgnoreCase("EIN")) {
                                        ein = compareStr;
                                    }
                                    // compare substrings
                                    if(!foundStr.equals(compareStr)){
                                        failureReasons.add("Line " + currentLine + ", record type " + inLine.substring(0,1) + " " + fieldName + " does not match. " +	"Found " + compareStr +" expected " + foundStr + ".");
                                        same = false;
                                    }
                                }
                                fieldInfo = null;
                            }
                        }
                        if(ein != null) {
                            psIdsWithEINs.put(ein, psIdEin); 
                        }
                    }
                    else{
                        if(!inLine.equals(compareLine)){
                            failureReasons.add("Line " + currentLine + ", does not match. " +	"Found " + compareLine +" expected " + inLine + ".");
                            same = false;
                        }
                    }
                }
                else{
                    failureReasons.add("Line " + currentLine + " record types do not match. Found type" +
                            compareLine.charAt(0) + " expected type " + inLine.charAt(0));
                    same = false;
                }
            }
            else{
                failureReasons.add("Line " + currentLine + " is not the same length");
                same = false;
            }
        }
        else{
            failureReasons.add("Line " + currentLine + " is null");
            same = false;
        }
        return same;
    }

    //Method to load data required for ACHC
    private HashMap getACHCompareProps(String data) {
        HashMap metaData = new HashMap();
        if (data != null && data.length() > 0) {
            String tempArray1[] = null;
            String tempArray2[] = null;
            String tempArray3[] = null;
            HashMap tempMap1 = new HashMap();
            HashMap tempMap2 = new HashMap();

            String FileStrings[] = data.split(";");
            for (int h = 0; h < FileStrings.length; h++) {
                tempArray1 = FileStrings[h].trim().split(":");
                tempArray2 = tempArray1[1].trim().split(",");
                tempMap2 = new HashMap();
                for (int i = 0; i < tempArray2.length; i += 4) {
                    if (tempMap2.containsKey(tempArray2[i].trim()))
                        tempMap1 = (HashMap) tempMap2.get(tempArray2[i].trim());
                    else
                        tempMap1 = new HashMap();
                    tempArray3 = new String[2];
                    for (int j = 0; j < 2; j++)
                        tempArray3[j] = tempArray2[(j + i + 2)].trim();
                    tempMap1.put(tempArray2[i + 1].trim(), tempArray3);
                    tempMap2.put(tempArray2[i].trim(), tempMap1);
                }
                metaData.put(tempArray1[0], tempMap2);
            }
        }
        return metaData;
    }

    public HashMap<String, String> getPsIds() {
        return psIds;
    }

    public HashMap<String, String> getPsIdsWithEINs() {
        return psIdsWithEINs;
    }

}