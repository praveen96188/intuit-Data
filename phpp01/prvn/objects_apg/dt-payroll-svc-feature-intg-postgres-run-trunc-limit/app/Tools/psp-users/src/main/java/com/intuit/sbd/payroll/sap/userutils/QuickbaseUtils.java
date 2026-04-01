package com.intuit.sbd.payroll.sap.userutils;

import com.intuit.quickbase.util.QuickBaseClient;

import java.util.Vector;
import java.util.HashMap;

//import com.intuit.quickbase.util.QuickBaseClient;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Dec 22, 2008
 * Time: 4:12:16 PM
 * To change this template use File | Settings | File Templates.
 */

public class QuickbaseUtils {

    private static final String QUICKBASE_DB_ID = "bd3hcb482";
    private static final String QBASE_USERID = "ptcserviceuser@intuit.com";
    private static final String QBASE_PASSWORD = "Intuit1020";
    private static final String QBASE_URL = "https://intuitcorp.quickbase.com/db/";

    //Field IDs for the Quickbase
    private static final String QB_FIRST_NAME = "6";
    private static final String QB_LAST_NAME = "7";
    private static final String QB_ROLE = "8";
    private static final String QB_CORP_ID = "21";
    private static final String QB_QA1 = "10";
    private static final String QB_QA2 = "11";
    private static final String QB_QA3 = "12";
    private static final String QB_QA4 = "13";
    private static final String QB_QA5 = "14";
    private static final String QB_QA6 = "19";
    private static final String QB_DEV1 = "15";
    private static final String QB_DEV2 = "16";
    private static final String QB_LT = "17";
    private static final String QB_PP = "18";
    //private static final String QB_RECORD_ID = "3";    (if you want to update a record)

    private static QuickBaseClient quickbaseClient = null;

    private static HashMap<String, String> quickBaseEnvSyncMap;

    static {
          //Field Id
        quickBaseEnvSyncMap = new HashMap<String, String>();

        quickBaseEnvSyncMap.put("qa1", QB_QA1);
        quickBaseEnvSyncMap.put("qa2", QB_QA2);
        quickBaseEnvSyncMap.put("qa3", QB_QA3);
        quickBaseEnvSyncMap.put("qa4", QB_QA4);
        quickBaseEnvSyncMap.put("qa5", QB_QA5);
        quickBaseEnvSyncMap.put("qa6", QB_QA6);
        quickBaseEnvSyncMap.put("dev1", QB_DEV1);
        quickBaseEnvSyncMap.put("dev2", QB_DEV2);
        quickBaseEnvSyncMap.put("lt", QB_LT);
        quickBaseEnvSyncMap.put("pp", QB_PP);
    }

    public static boolean validateQuickbaseConnectionWithUser() {
        try {
           quickbaseClient = new QuickBaseClient(QBASE_USERID,QBASE_PASSWORD, QBASE_URL);
           return true;
        } catch(Exception e) {
            LoggingUtils.logException(e);
        }
        return false;
    }

    public static QuickBaseClient getQuickBaseClient() throws Exception{
        if(quickbaseClient == null) {
           validateQuickbaseConnectionWithUser();
        }
        return quickbaseClient;
    }

    public static String getTable() throws Exception {
        QuickBaseClient qbc = getQuickBaseClient();
        return qbc.findDbByName("PSP DDM User Management");
    }

    public static Vector getRecords() throws Exception {
        QuickBaseClient qbc = getQuickBaseClient();

        //Execute the query to read quickbase records for the EIN passed in.
		Vector v = qbc.doQuery(QUICKBASE_DB_ID, "{0.CT.''}", "6.7.8.9.10.11.12.13.14.15.16.17.18.21", "slist", "options");

       
		return v;
    }

    public static Vector getRecordsForEnvironment(String envName) throws Exception {
        QuickBaseClient qbc = getQuickBaseClient();

        String fieldId = quickBaseEnvSyncMap.get(envName);

        Vector v = null;

        //Execute the query to read quickbase records for the env passed in.
        //If it is an unknown environment (or local), be safe and load all records.
		if(fieldId == null)
        {
            v = getRecords();
        } else {
            v = qbc.doQuery(QUICKBASE_DB_ID, "{" + fieldId + ".EX.'true'}", "6.7.8.9.10.11.12.13.14.15.16.17.18.21", "slist", "options");
        }

		return v;
    }

    public static boolean insertUserForAllEnvironments(String firstName, String lastName, String corpId, String role) throws Exception
    {
        QuickBaseClient qbc = getQuickBaseClient();

        Vector<HashMap> results = qbc.doQuery(QUICKBASE_DB_ID, "{" + QB_CORP_ID + ".EX.'" + corpId + "'}", "3.6.7.8.9.10.11.12.13.14.15.16.17.18.21", "slist", "options");

        if(results.size() > 0)
            return false;

        HashMap<String,String> userAttributes = new HashMap<String,String>();

        //quickbase can't handle null entries
        userAttributes.put(QB_FIRST_NAME,blankForNullReplace(firstName));
        userAttributes.put(QB_LAST_NAME,blankForNullReplace(lastName));
        userAttributes.put(QB_ROLE,blankForNullReplace(role));
        userAttributes.put(QB_CORP_ID,blankForNullReplace(corpId));

        qbc.addRecord(QUICKBASE_DB_ID, userAttributes);

        return true;

        //Uncomment if you want to update records.
        
       /* if(results.size() > 0)
        {
            HashMap<String,String> userAttributes = new HashMap<String,String>();
            userAttributes.put(QB_ROLE,blankForNullReplace(role));

            //Update record
            qbc.editRecord(results.get(0).get("Record ID#").toString(), userAttributes,QUICKBASE_DB_ID);

        } else {
            HashMap<String,String> userAttributes = new HashMap<String,String>();

            //quickbase can't handle null entries
            userAttributes.put(QB_FIRST_NAME,blankForNullReplace(firstName));
            userAttributes.put(QB_LAST_NAME,blankForNullReplace(lastName));
            userAttributes.put(QB_ROLE,blankForNullReplace(role));
            userAttributes.put(QB_CORP_ID,blankForNullReplace(corpId));

            qbc.addRecord(QUICKBASE_DB_ID, userAttributes);
        }  */
    }

    private static String blankForNullReplace(String s)
    {
        return s == null ? "" : s;
    }

}
