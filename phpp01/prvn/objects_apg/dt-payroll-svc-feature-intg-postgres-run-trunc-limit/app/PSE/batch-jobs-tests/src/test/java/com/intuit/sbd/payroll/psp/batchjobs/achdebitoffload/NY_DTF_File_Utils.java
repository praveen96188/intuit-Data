package com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Utilities class to read NY_DTF file.
 * Usage - Unit Tests & debugging.
 * Created by Ankit on 6/9/2015.
 */
public class NY_DTF_File_Utils {

    public static final String HEADER_RECORD = "HEADER_RECORD";
    public static final String DATA_RECORD = "DATA_RECORD";
    public static final String HASH_RECORD = "HASH_RECORD";
    public static final String TRAILER_RECORD = "TRAILER_RECORD";

    public static HashMap<String, String> readHeaderRecord(String headerRecord) {
        HashMap<String, String> returnMap = new LinkedHashMap<String, String>();

        returnMap.put("Header_Label_Identifier", headerRecord.substring(0, 4));
        returnMap.put("Tape_Creation_Date", headerRecord.substring(20, 26));
        returnMap.put("Form_Type_Indicator", headerRecord.substring(51, 55));
        returnMap.put("Submitter_Identification_Number", headerRecord.substring(58, 69));
        returnMap.put("Submitter_Check_Digit", headerRecord.substring(69, 70));
        returnMap.put("Submitter_Name", headerRecord.substring(70, 110));
        returnMap.put("Submitter_Street_Address", headerRecord.substring(110, 140));
        returnMap.put("Submitter_City", headerRecord.substring(140, 165));
        returnMap.put("Submitter_Zip_Code", headerRecord.substring(167, 176));

        return returnMap;
    }

    public static HashMap<String, String> readDataRecord(String dataRecord) {
        HashMap<String, String> returnMap = new LinkedHashMap<String, String>();

        returnMap.put("Tax_Id", dataRecord.substring(0, 11));
        returnMap.put("Check_Digit", dataRecord.substring(11, 12));
        returnMap.put("Last_Payroll_Date", dataRecord.substring(35, 41));
        returnMap.put("New_York_State_Tax_Withheld", new BigDecimal(new BigInteger(dataRecord.substring(44, 55)), 2).toString());
        returnMap.put("New_York_City_Tax_Withheld", new BigDecimal(new BigInteger(dataRecord.substring(55, 66)), 2).toString());
        returnMap.put("Yonkers_Tax_Withheld", new BigDecimal(new BigInteger(dataRecord.substring(66, 77)), 2).toString());
        returnMap.put("Amount_Of_Credit_Claimed", new BigDecimal(new BigInteger(dataRecord.substring(110, 121)), 2).toString());
        returnMap.put("Total_Tax_Withheld", new BigDecimal(new BigInteger(dataRecord.substring(121, 132)), 2).toString());
        returnMap.put("Total_Remittance_Paid", new BigDecimal(new BigInteger(dataRecord.substring(132, 143)), 2).toString());

        return returnMap;
    }

    public static HashMap<String, String> readHashRecord(String hashRecord) {
        HashMap<String, String> returnMap = new LinkedHashMap<String, String>();

        returnMap.put("Hash_Label_Identifier", hashRecord.substring(0, 4));
        returnMap.put("Item_Type", hashRecord.substring(5, 10));
        returnMap.put("Hash Amount", new BigDecimal(new BigInteger(hashRecord.substring(11, 24)), 2).toString());
        returnMap.put("Hash_Count", new Integer(hashRecord.substring(26, 32)).toString());

        return returnMap;
    }

    public static HashMap<String, String> readTrailerRecord(String trailerRecord) {
        HashMap<String, String> returnMap = new LinkedHashMap<String, String>();

        returnMap.put("Trailer_Label_Identifier", trailerRecord.substring(0, 4));
        returnMap.put("Number_Of_Records", new Integer(trailerRecord.substring(5, 12)).toString());

        return returnMap;
    }

    public static List<HashMap<String, String>> readFileToList(List<String> dataList) {
        List<HashMap<String, String>> returnList = new ArrayList<HashMap<String, String>>();
        if (dataList != null && dataList.size() < 3) {
            return returnList;
        }
        returnList.add(readHeaderRecord(dataList.get(0)));
        int numOfDataRecords = dataList.size() - 3;
        int currentRecord = 1; //start with first data record
        while (currentRecord <= numOfDataRecords) {
            returnList.add(readDataRecord(dataList.get(currentRecord)));
            currentRecord++;
        }
        returnList.add(readHashRecord(dataList.get(dataList.size() - 2)));
        returnList.add(readTrailerRecord(dataList.get(dataList.size() - 1)));

        return returnList;
    }

    public static List<HashMap<String, String>> readFileToList(File inputFile) {
        List<String> dataLineList = new ArrayList<String>();
        try {
            String line;
            InputStream fis = new FileInputStream(inputFile);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                dataLineList.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readFileToList(dataLineList);
    }

    public static List<HashMap<String, String>> readFileToList(String[] inputDataLines) {
        return readFileToList(new ArrayList<String>(Arrays.asList(inputDataLines)));
    }

    public static List<HashMap<String, String>> readFileToList(String inputData) {
        return readFileToList(new ArrayList<String>(Arrays.asList(inputData.split("\\r?\\n"))));
    }

    public static void printFileData(File inputFile) {
        List<HashMap<String, String>> fileDataMapList = readFileToList(inputFile);
        printFileData(fileDataMapList);
    }

    public static void printRecord(HashMap<String, String> recordHashMap) {
        for (Map.Entry<String, String> entry : recordHashMap.entrySet()) {
            System.out.println(entry.getKey() + "  =  " + entry.getValue());
        }
    }

    public static void printFileData(List<HashMap<String, String>> fileDataMapList) {
        for (HashMap<String, String> recordHashMap : fileDataMapList) {
            System.out.println("########### Record Begin ##########");
            printRecord(recordHashMap);
            System.out.println("###########  Record End  ##########");
        }
    }

    public static void printFileData(String inputDataString) {
        List<HashMap<String, String>> fileDataMapList = readFileToList(inputDataString.split("\\r?\\n"));
        printFileData(fileDataMapList);
    }

    public static void main(String[] args) {

        if(args.length == 0 || StringUtils.isEmpty(args[0])){
            System.out.println("ERROR: Please provide absolute path to the file");
            return;
        }
        String filename = args[0];
        System.out.println("Processing file - "+args[0]);
        File file = new File(filename);
        printFileData(file);
    }


}
