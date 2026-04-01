DECLARE

  table_exists PLS_INTEGER;

BEGIN

  SELECT COUNT(*) INTO table_exists
  FROM "USER_TABLES"
  WHERE TABLE_NAME = 'TEMP_PSP_REPORT_JOB_SETUP';

  IF table_exists = 1 THEN
    EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_REPORT_JOB_SETUP" CASCADE CONSTRAINTS';
  END IF;

END;
/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_REPORT_JOB_SETUP
(
  PRIMARY KEY(REPORT_NAME, "REALM_ID")
  , "REPORT_NAME" VARCHAR2(255 CHAR)  NOT NULL
  , "REPORT_SCHEDULE" VARCHAR2(4000 CHAR)
  , "VERSION" NUMBER(19,0)  NOT NULL
  , "REALM_ID" NUMBER(19,0) DEFAULT -1 NOT NULL
  , "REPORT_MAILING_LIST" VARCHAR2(4000 CHAR)
  , "IS_AUTOMATICALLY_SCHEDULED" NUMBER(1,0)
  , "QUERY_FILENAME" VARCHAR2(4000 CHAR)
  , "REPORT_NAMESPACE" VARCHAR2(4000 CHAR)
  , "ENCRYPTED_FIELDS" VARCHAR2(4000 CHAR)
)
/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------
/*
    Time expressions may be either relative or cron style. They can both be stated simply or can be used to state very complex timing requirements.

    See the "Time Expressions" section of the Flux manual for details and examples.

    Relative time expressions specify an offset, relative to a particular point in time.
    Relative time expression format: [+/-][number][unit]
        [+/-]        : Move forward/backward in time by the specified number of time units.
        [number]    : The number of units to move.
        [unit]        : [ydHms] (year, day, hour, minute, second) - These are the common unit types; there are many other unit types in the manual.
    Example: +15m  (fire timer every 15 minutes)

    Cron style time expressions are based on Unix cron expressions.
    Cron style time expression format:  [milliseconds][seconds][minutes][hours][days-of-month][months][days-of-week][day-of-year][week-of-month][week-of-year][year]
        [milliseconds]    : 0-999
        [seconds]    : 0-59
        [minutes]    : 0-59
        [hours]        : 0-23
        [days-of-month]    : 1-31
        [months]    : 0-11 or jan-dec
        [days-of-week]    : 1-7 or sun-sat
        [day-of-year]    : 1-366
        [week-of-month]    : minimum up to 6, where minimum is either 0 or 1, depending on your locale. In the United States locale, minimum is 1.
        [week-of-year]    : minimum up to 53, where minimum is either 0 or 1, depending on your locale. In the United States locale, minimum is 1.
        [year]        : 1970-3000
    Example: 0 0 0-59/5 * * * * * * * *  (fire the timer every 5 minutes on the clock face)
    Example: 0 0 0 3 1 * * * * * *  (fire the timer on the first of every month at 3 AM)

*/
/*
Removing DIYDD-Fraud-Suspect-Report since no longer required
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,

 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('DIYDD-Fraud-Suspect-Report','0 0 17 1/1 * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','Anthony_Tasler@intuit.com,APRIL_FLYNT@intuit.com,praveenkumar_hoolimath@intuit.com,Anand_Patil@intuit.com,Karthikeyan_Muthurangam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'diydd_fraud_suspect.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"PSID","dbColumnName":"null","encrypted":"false"},"1":{"columnName":"LEGAL_NAME","dbColumnName":"null","encrypted":"false"},"2":{"columnName":"BANK_NAME","dbColumnName":"null","encrypted":"false"},"3":{"columnName":"ACCOUNT_NUMBER","dbColumnName":"PSP/Bank_Acount_AccNo_AES256SIV","encrypted":"true"},"4":{"columnName":"ROUTING_NUMBER","dbColumnName":"null","encrypted":"false"},"5":{"columnName":"SIGN_UP_DATE","dbColumnName":"null","encrypted":"false"},"6":{"columnName":"QUICKBOOKS_SKU","dbColumnName":"null","encrypted":"false"},"7":{"columnName":"CONTACT1","dbColumnName":"null","encrypted":"false"},"8":{"columnName":"CONTACT2","dbColumnName":"null","encrypted":"false"},"9":{"columnName":"PPO_EMAIL_OLD","dbColumnName":"null","encrypted":"false"},"10":{"columnName":"PPO_EMAIL_NEW","dbColumnName":"null","encrypted":"false"},"11":{"columnName":"ADDRESS_LINE_1","dbColumnName":"null","encrypted":"false"},"12":{"columnName":"ADDRESS_LINE_2","dbColumnName":"null","encrypted":"false"},"13":{"columnName":"ADDRESS_LINE_3","dbColumnName":"null","encrypted":"false"},"14":{"columnName":"CITY","dbColumnName":"null","encrypted":"false"},"15":{"columnName":"STATE","dbColumnName":"null","encrypted":"false"},"16":{"columnName":"ZIP_CODE","dbColumnName":"null","encrypted":"false"},"17":{"columnName":"COUNTRY","dbColumnName":"null","encrypted":"false"}}')
/
*/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Bank_Report','0 1 10,15,17 1/1 * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','Michael_Magness@intuit.com,SBGOPSIDCDBA@intuit.com,mayank_choubey@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'bnk_rpt_test_1.sql', 1, '/PSP/MONITOR', '{ "0":{ "columnName":"SOURCE_SYSTEM_CD", "dbColumnName":"null", "encrypted":"false" },
   "1":{ "columnName":"PSID", "dbColumnName":"null", "encrypted":"false" },
   "2":{ "columnName":"LEGAL_NAME", "dbColumnName":"null", "encrypted":"false" },
   "3":{ "columnName":"FIRST_NAME", "dbColumnName":"null", "encrypted":"false" },
   "4":{ "columnName":"LAST_NAME", "dbColumnName":"null", "encrypted":"false" },
   "5":{ "columnName":"MOD_DATE", "dbColumnName":"null", "encrypted":"false" },
   "6":{ "columnName":"ROUTING_NUMBER", "dbColumnName":"null", "encrypted":"false" },
   "7":{ "columnName":"BANK_NAME", "dbColumnName":"null", "encrypted":"false" },
   "8":{ "columnName":"ACCOUNT_NUMBER", "dbColumnName":"PSP/Bank_Acount_AccNo_AES256SIV", "encrypted":"true" },
   "9":{ "columnName":"PAYCHECK_AMOUNT", "dbColumnName":"null", "encrypted":"false" },
   "10":{ "columnName":"NUM_OF_PAYROLLS", "dbColumnName":"null", "encrypted":"false" },
   "11":{ "columnName":"FIRST_PAYROLL", "dbColumnName":"null", "encrypted":"false" }}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('Nachafile_Report', '0 0 9 1 1/1 ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','pspprodstats@intuit.com,mayank_choubey@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'nachafile_rpt.sql', '/PSP/MONITOR', '{"0":{"columnName": "FileType","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "Offload Date","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "Credit Amount","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "Debit Amount","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "Elapsed Time","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  VALUES ('100k_Query', '0 6 22 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','michaelle_mcmullin@intuit.com,linda_holler@intuit.com,Elaine_perry@intuit.com,mayank_choubey@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, '100K_query.sql', '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "DBA_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "PAYROLL_RUN_DATE","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "MM_TRANSACTION_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "DUE_DATE","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "INITIATION_DATE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "PAYMENT_TEMPLATE_FK","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('Financial_Transaction', '0 30 6 ? * MON,TUE,WED,THU,FRI,SAT *', 1, -1, DECODE(FN_GET_ENV(),'PROD','sbdriskmanagement@intuit.com,pspprodstats@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'ft1.sql', '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "AMOUNT_RETURNED","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "RETURN_DATE","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "REDEBIT_CREATED","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
   (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID,  REPORT_MAILING_LIST,
    QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values
   ('Missed_Payments_High_Priority', '0 10 9 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','psp_tax_payment_notify_prod@intuit.com,pspprodstats@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
    'missed_payments_High_priority_review.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "Due Date","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "Template","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "Payment Status","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "Payment Method","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "FEIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "6":{"columnName": "Amount","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "Company Hold","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "Payment Company Hold","dbColumnName":"null","encrypted": "false"},
"9":{"columnName": "Payment Agent Hold","dbColumnName": "null","encrypted": "false"},
"10":{"columnName": "Payment Enroll Hold","dbColumnName": "null","encrypted": "false"},
"11":{"columnName": "Payment BackDate Hold","dbColumnName": "null","encrypted": "false"},
"12":{"columnName": "MMT GUID","dbColumnName": "null","encrypted": "false"},
"13":{"columnName": "Initiation Date","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
   (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID,  REPORT_MAILING_LIST,
    QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values
   ('Missed_payments_not_immediate', '0 10 9 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','psp_tax_payment_notify_prod@intuit.com,pspprodstats@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
    'missed_payments_not_immediate.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "Due Date","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "Template","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "Payment Status","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "Payment Method","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "FEIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "6":{"columnName": "Amount","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "Company Hold","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "Payment Company Hold","dbColumnName":"null","encrypted": "false"},
"9":{"columnName": "Payment Agent Hold","dbColumnName": "null","encrypted": "false"},
"10":{"columnName": "Payment Enroll Hold","dbColumnName": "null","encrypted": "false"},
"11":{"columnName": "Payment BackDate Hold","dbColumnName": "null","encrypted": "false"},
"12":{"columnName": "MMT GUID","dbColumnName": "null","encrypted": "false"},
"13":{"columnName": "Initiation Date","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('Tax_Payments_Forecast', '0 5 6 ? * MON-FRI *', 1, -1, DECODE(FN_GET_ENV(),'PROD','psp_tax_payment_notify_prod@intuit.com,mayank_choubey@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'tax_payments_forecast.sql', '/PSP/MONITOR', '{"0":{"columnName": "Init Date","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "Payment Template","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "Transaction Count","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "Payment Method","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID,  REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Eu_Err_Aging_Info', '0 10 6 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','SBCGPayrollCRT@intuit.com, Adam_Barnes@intuit.com,Melissa_Niswonger@Intuit.com, Alfred_Bouton@Intuit.com,Iris_Cubero@intuit.com,Dawn_Pfiel@intuit.com,Patrick_Riley@intuit.com,Simon_Santiago@intuit.com,Stephen_Weinzetl@Intuit.com,winnie_wong@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'eu_err_aging_info.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "EIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "EU_STATUS","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "LICENSE_NUMBER","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "EOC","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "FIRST_ATTEMPT_TIME_STAMP","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('Company_Agency_Info', '0 17 5 ? * MON-FRI *', 1, -1, DECODE(FN_GET_ENV(),'PROD','TaxDeposits@intuit.com,mayank_choubey@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'agencyinfo_15719.sql', '/PSP/MONITOR', ' {"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
  "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
  "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
  "3":{"columnName": "ADDRESS_LINE_1","dbColumnName": "null","encrypted": "false"},
  "4":{"columnName": "ADDRESS_LINE_2","dbColumnName": "null","encrypted": "false"},
  "5":{"columnName": "ADDRESS_LINE_3","dbColumnName": "null","encrypted": "false"},
  "6":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
  "7":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
  "8":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"},
  "9":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
  "10":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
  "11":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
  "12":{"columnName": "DEPOSIT_FREQUENCY","dbColumnName": "null","encrypted": "false"},
  "13":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
  "14":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
  "15":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
  "16":{"columnName": "AID","dbColumnName": "null","encrypted": "false"},
  "17":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
  "18":{"columnName": "PAYMENT_METHOD","dbColumnName": "null","encrypted": "false"},
  "19":{"columnName": "PAYMENT_METHOD_ENABLED","dbColumnName": "null","encrypted": "false"},
  "20":{"columnName": "ACH_REGISTERED","dbColumnName": "null","encrypted": "false"},
  "21":{"columnName": "LAW_ID","dbColumnName": "null","encrypted": "false"},
  "22":{"columnName": "LAW_DESC","dbColumnName": "null","encrypted": "false"},
  "23":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
  "24":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
  "25":{"columnName": "COMPANY_SERVICE","dbColumnName": "null","encrypted": "false"},
  "26":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
   (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID,  REPORT_MAILING_LIST,
    QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values
   ('Company_Agency_Info_SUI', '0 20 5 ? * MON-FRI *', 1, -1, DECODE(FN_GET_ENV(),'PROD','TaxDeposits@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 'agencyinfo_2_15719.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
  "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
  "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
  "3":{"columnName": "ADDRESS_LINE_1","dbColumnName": "null","encrypted": "false"},
  "4":{"columnName": "ADDRESS_LINE_2","dbColumnName": "null","encrypted": "false"},
  "5":{"columnName": "ADDRESS_LINE_3","dbColumnName": "null","encrypted": "false"},
  "6":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
  "7":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
  "8":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"},
  "9":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
  "10":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
  "11":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
  "12":{"columnName": "DEPOSIT_FREQUENCY","dbColumnName": "null","encrypted": "false"},
  "13":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
  "14":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
  "15":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
  "16":{"columnName": "AID","dbColumnName": "null","encrypted": "false"},
  "17":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
  "18":{"columnName": "PAYMENT_METHOD","dbColumnName": "null","encrypted": "false"},
  "19":{"columnName": "PAYMENT_METHOD_ENABLED","dbColumnName": "null","encrypted": "false"},
  "20":{"columnName": "ACH_REGISTERED","dbColumnName": "null","encrypted": "false"},
  "21":{"columnName": "LAW_ID","dbColumnName": "null","encrypted": "false"},
  "22":{"columnName": "LAW_DESC","dbColumnName": "null","encrypted": "false"},
  "23":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
  "24":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
  "25":{"columnName": "COMPANY_SERVICE","dbColumnName": "null","encrypted": "false"},
  "26":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
   (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID,  REPORT_MAILING_LIST,
    QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values
   ('Company_Agency_Info_SUI_Debit', '0 10 5 ? * MON-FRI *', 1, -1, DECODE(FN_GET_ENV(),'PROD','TaxDeposits@intuit.com, Ronnie_Jacobson@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 'agencyinfo_3_15719.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
  "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
  "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
  "3":{"columnName": "ADDRESS_LINE_1","dbColumnName": "null","encrypted": "false"},
  "4":{"columnName": "ADDRESS_LINE_2","dbColumnName": "null","encrypted": "false"},
  "5":{"columnName": "ADDRESS_LINE_3","dbColumnName": "null","encrypted": "false"},
  "6":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
  "7":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
  "8":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"},
  "9":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
  "10":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
  "11":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
  "12":{"columnName": "DEPOSIT_FREQUENCY","dbColumnName": "null","encrypted": "false"},
  "13":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
  "14":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
  "15":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
  "16":{"columnName": "AID","dbColumnName": "null","encrypted": "false"},
  "17":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
  "18":{"columnName": "PAYMENT_METHOD","dbColumnName": "null","encrypted": "false"},
  "19":{"columnName": "PAYMENT_METHOD_ENABLED","dbColumnName": "null","encrypted": "false"},
  "20":{"columnName": "ACH_REGISTERED","dbColumnName": "null","encrypted": "false"},
  "21":{"columnName": "LAW_ID","dbColumnName": "null","encrypted": "false"},
  "22":{"columnName": "LAW_DESC","dbColumnName": "null","encrypted": "false"},
  "23":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
  "24":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
  "25":{"columnName": "COMPANY_SERVICE","dbColumnName": "null","encrypted": "false"},
  "26":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('Duplicate_Adjustments', '0 0 7 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Linda_Holler@intuit.com,taxdeposits@intuit.com,mayank_choubey@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'jira_24717.sql', '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "EIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "AMOUNT","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "MIN_PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "MAX_PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "ADJ_CREATED_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values ('Complia_Adjustments', '0 30 6,8,10,12,14,16,18 * * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','TeamMoore@Intuit.com, Wendy_Moore@Intuit.com, Krista_Richmond@Intuit.com, Trina_Cotta@Intuit.com, Evan_Ramirez@Intuit.com,Anita_StAndry@Intuit.com, Wendy_Moore@Intuit.com, Krista_Richmond@Intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'Jira_26703.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "FEIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "QB_VERSION","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "TAX_TABLE","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "VOIDED","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "TXID","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "CHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "EMP_ID","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "LINE_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "TOTAL_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "CREATE_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('Assisted_On_Hold_Info', '0 45 9 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','dify_cancellations@intuit.com, Alanna_Ainsworth@intuit.com, Wesley_Jorgensen@Intuit.com, SBG-EMSAssistedHoldDistributionList@intuit.com, Julie_Merica@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'jira_27019.sql', '/PSP/MONITOR', '{"0":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "EIN ","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "SERVICE_FK","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "STATUS_CD","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "ONHOLD_CREATED_DATE","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "ON_HOLD_REASON_CD","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "PR_ADM_FIRST_NAME","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "PR_ADM_LAST_NAME","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "PR_ADM_EMAIL","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "TRANSACTION_TYPE","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "REDEBIT_TXN_DATE","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "BALANCE_DUE","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "LIC","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "ENT_UNIT_STATUS","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "ENT_STATUS","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "LAST_PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('Financial_Ledger', '0 30 20 ? * SAT *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Kavita_Rivers@intuit.com, Iris_Matthews@intuit.com, EMSAccounting@intuit.com,mayank_choubey@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'finledger.sql', '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "VARIANCE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES ('ms_m89_payment_info', '0 10 0 15,16,17,18,19,20 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','taxdeposits@intuit.com,mayank_choubey@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'), 1, 'jira_psp-5003.sql', '/PSP/MONITOR', '{"0":{"columnName": "fed_tax_id","dbColumnName": "PSP/FSET_FLDtl_EIN_AES256SIV","encrypted": "true"},
 "1":{"columnName": "agency_id ","dbColumnName": "PSP/FSET_FLDtl_AID_AES256SIV","encrypted": "true"},
 "2":{"columnName": "filing_due_date","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "status","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "business_name","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "address_line1","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "address_line2","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "city","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "state","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "zip","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "filing_amount","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "submission_id","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "period_end_date","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "error_message","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "file_name","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "transmission_id","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID,  REPORT_MAILING_LIST,
  QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values ('Late_Payment_Stats', '0 12 5 1 1/1 ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Linda_Holler@Intuit.com, Payrollservicesmaintenance@Intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'late_pmt_stats.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "total_amount","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "no_of_pmts ","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "month","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "no_of_zero_pmts","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "no_of_late_pmts","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Late_Payment_Details', '0 12 5 1 1/1 ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Linda_Holler@Intuit.com, Payrollservicesmaintenance@Intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'late_pmt_details.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PMT_MONTH","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "EIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "3":{"columnName": "PAYROLL_RUN_DATE","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PAYROLL_RUN_TYPE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "PAYROLL_RUN_STATUS","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "IS_BACK_DATE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "PAYMENT_TEMPLATE_FK","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "TOTAL_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "SETTLEMENT_DATE","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "DUE_DATE","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "PMT_PERIOD_END","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "IS_LATE_PMT","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('EIN_DEACTIVATION_LIST', '0 15 1 7-13,21-28 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Manjunatha_Ramakrishnappa@intuit.com, Raveena_Mahajan@intuit.com, Malthi_SS@intuit.com, Vanaja_Nataraj@intuit.com, Srinivas_Vinti@intuit.com, Praveenkumar_Hoolimath@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'jira_47671.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "EIN_TO_DEACTIVATE","dbColumnName": "PSP/EntUnit_FedTaxId_AES256SIV","encrypted": "true"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Assisted_Usage_Billing', '0 30 7 ? * SUN *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Alanna_Ainsworth@intuit.com, Wesley_Jorgensen@Intuit.com, SBG-EMSAssistedHoldDistributionList@intuit.com, Julie_Merica@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'jira_69637.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "psid","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "ein ","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "business_name","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "current_offering","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "correct_offering","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "status","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "asset_item_cd","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "entitlement_unit_status","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "subtype_description","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Back_Dated_Paychecks', '0 20 7 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','linda_holler@intuit.com, PQAReport@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'jira_12545.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "1":{"columnName": "PSID ","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "PAYROLL_RUN_DATE","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "CREATOR","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "MODIFIER","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "PAYROLL_TYPE","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "PAYROLL_STATUS","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('ASSISTED_NEW_CUSTOMER_LIST', '0 30 18 15 3,6,9,12 ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Kimberly_Han@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'psp-8615_test.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "FEIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "OFFERING_CODE","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PR_ADMIN_LAST_NAME","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "PR_ADMIN_FIRST_NAME","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "pr_admin_email","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "first_payroll_date","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "last_payroll_date","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "Service_start_date","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "Status_effec_date","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "Sign_up_date","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "QTR_FIRST_DATE","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "QTR_END_DATE","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "IS_VALID","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Foreign_Routing_Number', '0 5 17 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Iolanta_Green@intuit.com,Manjunatha_Ramakrishnappa@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'jira_11190_2.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "MODIFIED_DATE","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "4":{"columnName": "FINANCIAL_TRANSACTION_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "ACCOUNT_NUMBER","dbColumnName": "PSP/Bank_Acount_AccNo_AES256SIV","encrypted": "true"},
 "6":{"columnName": "ROUTING_NUMBER","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "SOURCE_EMPLOYEE_ID","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "SOURCE_PAYEE_ID","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "SOURCE_BANK_ACCOUNT_NAME","dbColumnName": "null","encrypted": "false"},
"11":{"columnName": "BASTATUS","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Manual_DataSync_Rep', '0 5 5 2 1/1 ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Theresa_Arnold@intuit.com, Dawn_Pfiel@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'emsops-73759.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FEIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "1":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "CREATED_DATE","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "AGENT_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "ACTION","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "COMMENT_TYPE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('Psp_Cancelled_Assisted_Info', '0 30 15 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','DIFY_Cancellations@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'jira_15845.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "EIN","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "CAN","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "SERVICE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "STATUS","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "LAST_QTR_TO_FILE","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "LAST_PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('ASSISTED_QB_DISCO_CUSTOMER_LIST', '0 20 5 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Alex_Sutich@intuit.com, Joyce_Lee2@intuit.com, David_Jost@intuit.com, Jennifer_Stansbury@intuit.com, Julie_Merica@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'jira_16159.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "LICENSE_NUMBER","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "ENTITLEMENT_OFFERING_CODE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "SERVICE","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "STATUS","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "LAST_PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "QB_APPLICATION_VERSION","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "PAYROLL_ADMIN_NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "PAYROLL_ADMIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "PRIMPARY_PRI_FIRST_NAME","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "PRIMARY_PRI_LAST_NAME","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "PRIMARY_PRIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "PRIMARY_PRIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "MAILING_ADDR","dbColumnName": "null","encrypted": "false"},
 "17":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
 "18":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
 "19":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Back_Dated_voids', '0 5 6 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Linda_Holler@intuit.com,Haique_Aguilar@Intuit.com,Payrollservicesmaintenance@Intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'jira_20515.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "ADJUST_SUBMISSION_CREATED_DT","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "PAYROLL_RUN_DATE","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "PAYROLL_RUN_TYPE","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "PAYROLL_RUN_STATUS","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "PAYROLL_CREATOR_ID","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Assisted_Agency_Info_Rpt_SUI', '0 14 7 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','TaxDeposits@Intuit.com,Tax_ServiceSetup@intuit.com, Payrollservicesmaintenance@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'assisted_agency_info_rpt_SUI.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "ADDRESS_LINE_1","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "ADDRESS_LINE_2","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "ADDRESS_LINE_3","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "PAYROLL_ADMIN_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "PAYROLL_ADMIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "DEPOSIT_FREQUENCY","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "DEP_FREQ_CREATOR","dbColumnName": "null","encrypted": "false"},
 "17":{"columnName": "DEP_FREQ_CREATED_DATE","dbColumnName": "null","encrypted": "false"},
 "18":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
 "19":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
 "20":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
 "21":{"columnName": "AID","dbColumnName": "PSP/CPTA_ATaxPayerId_AES256SIV","encrypted": "true"},
 "22":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
 "23":{"columnName": "PAYMENT_METHOD","dbColumnName": "null","encrypted": "false"},
 "24":{"columnName": "PAYMENT_METHOD_ENABLED","dbColumnName": "null","encrypted": "false"},
 "25":{"columnName": "ACH_REGISTERED","dbColumnName": "null","encrypted": "false"},
 "26":{"columnName": "LAW_ID","dbColumnName": "null","encrypted": "false"},
 "27":{"columnName": "LAW_DESC","dbColumnName": "null","encrypted": "false"},
 "28":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
 "29":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
 "30":{"columnName": "COMPANY_SERVICE","dbColumnName": "null","encrypted": "false"},
 "31":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"},
 "32":{"columnName": "CURRENT_YR_LIA","dbColumnName": "null","encrypted": "false"},
 "33":{"columnName": "LAW_CREATED_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Assisted_Agency_Info_Rpt_SIT', '0 14 7 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','TaxDeposits@Intuit.com,Tax_ServiceSetup@intuit.com, Payrollservicesmaintenance@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'assisted_agency_info_rpt_SIT.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "ADDRESS_LINE_1","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "ADDRESS_LINE_2","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "ADDRESS_LINE_3","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "PAYROLL_ADMIN_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "PAYROLL_ADMIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "DEPOSIT_FREQUENCY","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "DEP_FREQ_CREATOR","dbColumnName": "null","encrypted": "false"},
 "17":{"columnName": "DEP_FREQ_CREATED_DATE","dbColumnName": "null","encrypted": "false"},
 "18":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
 "19":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
 "20":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
 "21":{"columnName": "AID","dbColumnName": "PSP/CPTA_ATaxPayerId_AES256SIV","encrypted": "true"},
 "22":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
 "23":{"columnName": "PAYMENT_METHOD","dbColumnName": "null","encrypted": "false"},
 "24":{"columnName": "PAYMENT_METHOD_ENABLED","dbColumnName": "null","encrypted": "false"},
 "25":{"columnName": "ACH_REGISTERED","dbColumnName": "null","encrypted": "false"},
 "26":{"columnName": "LAW_ID","dbColumnName": "null","encrypted": "false"},
 "27":{"columnName": "LAW_DESC","dbColumnName": "null","encrypted": "false"},
 "28":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
 "29":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
 "30":{"columnName": "COMPANY_SERVICE","dbColumnName": "null","encrypted": "false"},
 "31":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"},
 "32":{"columnName": "CURRENT_YR_LIA","dbColumnName": "null","encrypted": "false"},
 "33":{"columnName": "LAW_CREATED_DATE","dbColumnName": "null","encrypted": "false"}}')
 /
 INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('PSP_COMPANY','0 15 0 * * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','EMSIDC_OpsTeam@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'PSP_COMPANY.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true","format": {
            "regex":"(\\d{2})(\\d{7})",
            "replace": "$1-$2"
          }},
 "1":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "DBA_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "LEGAL_ADDRESS","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "MAILING_ADDRESS","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "EXTDEM1","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "PAYROLL_ADMIN_MAIL","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "PAYROLL_ADMIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
"10":{"columnName": "EXTDEM2","dbColumnName": "null","encrypted": "false"},
"11":{"columnName": "EXTDEM3","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "PAYROLL_ADMIN_NAME","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "PRIMARY_PRINCIPAL_NAME","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": " STATUS","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('PSP_contact','0 15 0 * * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','EMSIDC_OpsTeam@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'PSP_contact.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "EMAIL","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "CONTACT_ROLE_CD","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PHONE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('PSP_STATEID','0 15 0 * * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','EMSIDC_OpsTeam@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'PSP_STATEID.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "AGENCY_FK","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "AGENCY_TAXPAYER_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"}}')
 /
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('Bad_EE_Accounts','0 0 17 1/1 * ? *',  1, -1, DECODE(FN_GET_ENV(),'PROD','Michael_Magness@intuit.com, EMSPayrollFraud@intuit.com, SBGOPSIDCDBA@intuit.com, Mayank_Choubey@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'Bad_EE_Accounts.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
  "1":{"columnName": "FULLNAME","dbColumnName": "null","encrypted": "false"},
  "2":{"columnName": "ROUTING_NUMBER","dbColumnName": "null","encrypted": "false"},
  "3":{"columnName": "ACCOUNT_NUMBER","dbColumnName": "PSP/Bank_Acount_AccNo_AES256SIV","encrypted": "true"},
  "4":{"columnName": "PAYROLL_RUN_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('Bad_Funding_Account','0 0 17 1/1 * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','Michael_Magness@intuit.com, EMSPayrollFraud@intuit.com, SBGOPSIDCDBA@intuit.com, Mayank_Choubey@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'Bad_Funding_Account.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
"1":{"columnName": "ROUTING_NUMBER","dbColumnName": "null","encrypted": "false"},
"2":{"columnName": "ACCOUNT_NUMBER","dbColumnName": "PSP/Bank_Acount_AccNo_AES256SIV","encrypted": "true"},
"3":{"columnName": "STATUS_EFFECTIVE_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('bank_report_withvac','0 06 10,15,17 * * ? *',  1, -1,  DECODE(FN_GET_ENV(),'PROD','Michael_Magness@intuit.com,EMSPAYROLLFRAUD@INTUIT.COM,Iolanta_Green@intuit.com,Swarnashis_Sarkar@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'bnk_rep_withvac_3.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_SYSTEM_CD","dbColumnName": "null","encrypted": "false"},
"1":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
"2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
"3":{"columnName": "PAYEE_FIRST_NAME","dbColumnName": "null","encrypted": "false"},
"4":{"columnName": "BANK_NAME","dbColumnName": "null","encrypted": "false"},
"5":{"columnName": "NET_AMT","dbColumnName": "null","encrypted": "false"},
"6":{"columnName": "NEW_ACCOUNT_NUMBER","dbColumnName": "null","encrypted": "false"},
"7":{"columnName": "NEW_ROUTING_NUMBER","dbColumnName": "null","encrypted": "false"},
"8":{"columnName": "OLD_ACCOUNT_NUMBER","dbColumnName": "null","encrypted": "false"},
"9":{"columnName": "OLD_ROUTING_NUMBER","dbColumnName": "null","encrypted": "false"},
"10":{"columnName": "MODIFIED_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  VALUES ('Assisted_Activation_Report', '0 0 6 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Tax_ServiceSetup@Intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
  'assisted_activation.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "CAN","dbColumnName": "null","encrypted": "false"},
  "1":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
  "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
  "3":{"columnName": "AGENCY_TAXPAYER_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
  "4":{"columnName": "PAYMENT_TEMPLATE_FK","dbColumnName": "null","encrypted": "false"},
  "5":{"columnName": "SOURCE_DESCRIPTION","dbColumnName": "null","encrypted": "false"},
  "6":{"columnName": "TAX_TYPE","dbColumnName": "null","encrypted": "false"},
  "7":{"columnName": "PRIMARY_PRINCIPAL_NAME","dbColumnName": "null","encrypted": "false"},
  "8":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
  "9":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
  "10":{"columnName": "PAYROLL_ADMIN_NAME","dbColumnName": "null","encrypted": "false"},
  "11":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
  "12":{"columnName": "LEGAL_ADDRESS","dbColumnName": "null","encrypted": "false"},
  "13":{"columnName": "City","dbColumnName": "null","encrypted": "false"},
  "14":{"columnName": "State","dbColumnName": "null","encrypted": "false"},
  "15":{"columnName": "ZipCode","dbColumnName": "null","encrypted": "false"},
  "16":{"columnName": "EVENT_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Assisted_Payroll_Discount_Rpt', '0 0 11 2 2,5,8,11 ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Alex_Sinai@intuit.com, Josh_Baker@intuit.com, Sumit_Poddar@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'assisted_payroll_discount_rpt.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "PRODUCT_NAME","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "AGENT_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "ORDER_DATE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "OFFER_NAME","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "OFFER_DESCRIPTION","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "DISCOUNT_TYPE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "DISCOUNT_DURATION_IN_MONTHS","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "DISCOUNT_PERCENT","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "BASE_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "DISCOUNT_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "NET_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "DISCOUNT_AMOUNT_CUMULATIVE","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "MEMO","dbColumnName": "null","encrypted": "false"}}')
 /
 INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('Assisted_Payroll_Refund_Rpt', '0 10 11 2 2,5,8,11 ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Alex_Sinai@intuit.com, Josh_Baker@intuit.com, Sumit_Poddar@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'assisted_payroll_refund_rpt.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "PRODUCT_NAME","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "AGENT_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "REFUND_DATE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "REFUND_TYPE","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "DESCRIPTION","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "REFUND_AMOUNT","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 Values ('ASSISTED_DISCO_CUSTOMER_LIST_NEW', '0 30 5 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','Alex_Sutich@intuit.com, Joyce_Lee2@intuit.com, Jennifer_Stansbury@intuit.com, dify_cancellations@intuit.com, nathan_stieg@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'assisted_disco_customer_list_new.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "LICENSE_NUMBER","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "3":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "ENTITLEMENT_OFFERING_CODE","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "SERVICE","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "STATUS","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "LAST_PAYCHECK_DATE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "QB_APPLICATION_VERSION","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "PAYROLL_ADMIN_NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "PAYROLL_ADMIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "PRIMPARY_PRI_FIRST_NAME","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "PRIMARY_PRI_LAST_NAME","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "PRIMARY_PRIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "PRIMARY_PRIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "MAILING_ADDR","dbColumnName": "null","encrypted": "false"},
 "17":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
 "18":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
 "19":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"},
 "20":{"columnName": "MM_TRANSACTION_AMOUNT","dbColumnName": "null","encrypted": "false"},
 "21":{"columnName": "DUE_DATE","dbColumnName": "null","encrypted": "false"},
 "22":{"columnName": "MONEY_MOVEMENT_PAYMENT_METHOD","dbColumnName": "null","encrypted": "false"}}')
 /
 INSERT INTO TEMP_PSP_REPORT_JOB_SETUP ( REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  VALUES ('New_Signups_Report', '0 0 6 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','IntuitPayrollAccountReview@intuit.com, Kavita_Rivers@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
  'new_signups_report.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
  "1":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
  "2":{"columnName": "DBA_NAME","dbColumnName": "null","encrypted": "false"},
  "3":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
  "4":{"columnName": "NOTIFICATION_EMAIL","dbColumnName": "null","encrypted": "false"},
  "5":{"columnName": "NAME","dbColumnName": "null","encrypted": "false"},
  "6":{"columnName": "STATUS_EFFECTIVE_DATE","dbColumnName": "null","encrypted": "false"},
  "7":{"columnName": "SERVICE_FK","dbColumnName": "null","encrypted": "false"},
  "8":{"columnName": "OVERRIDE_COMPANY_LIMIT_AMOUNT","dbColumnName": "null","encrypted": "false"},
  "9":{"columnName": "OVERRIDE_EMPLOYEE_LIMIT_AMOUNT","dbColumnName": "null","encrypted": "false"},
  "10":{"columnName": "ADDRESS_LINE1","dbColumnName": "null","encrypted": "false"},
  "11":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
  "12":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
  "13":{"columnName": "ZIP_CODE","dbColumnName": "null","encrypted": "false"},
  "14":{"columnName": "PHONE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('EU_activ_err_count_exceed_limit','0 1 7 * * ? *',  1, -1,  DECODE(FN_GET_ENV(),'PROD','RNPSPALERTPROD@intuit.com,melissa_niswonger@intuit.com,Theresa_Arnold@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'jira_25922.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "count","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('mmt_rec_count_exceed','0 30 10 * * ? *',  1, -1,  DECODE(FN_GET_ENV(),'PROD','Tricia_Parks@intuit.com,Rob_Histing@Intuit.com,Iolanta_Green@intuit.com,Linda_Holler@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'jira_7907.sql', 1, '/PSP/MONITOR',
'{"0":{"columnName": "count","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('dd_limit_inf_jirapsp_6681','0 5 0 * * ? *',  1, -1,  DECODE(FN_GET_ENV(),'PROD','IntuitPayrollAccountReview@intuit.com,Kavita_Rivers@intuit.com,SBDRiskManagement@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'jira_psp-6681.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"PSID","dbColumnName":"null","encrypted":"false"},
"1":{"columnName":"SERVICE_TYPE","dbColumnName":"null","encrypted":"false"},
"2":{"columnName":"CLASS_NAME","dbColumnName":"null","encrypted":"false"},
"3":{"columnName":"PROPERTY_NAME","dbColumnName":"null","encrypted":"false"},
"4":{"columnName":"MODIFIED_DATE","dbColumnName":"null","encrypted":"false"},
"5":{"columnName":"OLD_LIMIT_AMOUNT","dbColumnName":"null","encrypted":"false"},
"6":{"columnName":"NEW_LIMIT_AMOUNT","dbColumnName":"null","encrypted":"false"},
"7":{"columnName":"CONS_LIMIT_VIOLATION_CNT","dbColumnName":"null","encrypted":"false"},
"8":{"columnName":"AGENT_ID","dbColumnName":"null","encrypted":"false"},
"9":{"columnName":"FIRST_NAME","dbColumnName":"null","encrypted":"false"},
"10":{"columnName":"LAST_NAME","dbColumnName":"null","encrypted":"false"},
"11":{"columnName":"SERVICE","dbColumnName":"null","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('Direct_Deposit_Paycheck_Data','0 5 5 4 * ? *',  1, -1,  DECODE(FN_GET_ENV(),'PROD','Kristin_Stroupe@Intuit.com,Iolanta_Green@intuit.com,Tricia_Parks@intuit.com,Shivananda_Devannavar@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'emsops-60470.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "PAYROLL_RUN_TYPE","dbColumnName": "null","encrypted": "false"},
"1":{"columnName": "MM_TRANSACTION_AMOUNT","dbColumnName": "null","encrypted": "false"},
"2":{"columnName": "DD_Transactions_Count","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP (REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('Cancelled_By_MissedPayrollsBatchJob','0 7 5 * * ? *',  1, -1, DECODE(FN_GET_ENV(),'PROD','SBDRiskManagement@intuit.com,iris_matthews@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'jira_15826.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "SOURCE_COMPANY_ID","dbColumnName": "null","encrypted": "false"},
"1":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
"2":{"columnName": "SERVICE_FK","dbColumnName": "null","encrypted": "false"},
"3":{"columnName": "STATUS_CD","dbColumnName": "null","encrypted": "false"},
"4":{"columnName": "TRANSACTION_TYPE_FK","dbColumnName": "null","encrypted": "false"},
"5":{"columnName": "CURRENT_TRANSACTION_STATE_FK","dbColumnName": "null","encrypted": "false"},
"6":{"columnName": "FINANCIAL_TRANSACTION_AMOUNT","dbColumnName": "null","encrypted": "false"},
"7":{"columnName": "CREATOR_ID","dbColumnName": "null","encrypted": "false"},
"8":{"columnName": "CREATED_DATE","dbColumnName": "null","encrypted": "false"},
"9":{"columnName": "MODIFIED_DATE","dbColumnName": "null","encrypted": "false"},
"10":{"columnName": "MODIFIER_ID","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('mmt_info_per_payment_template','0 10 6 ? * 2-6 *',  1, -1, DECODE(FN_GET_ENV(),'PROD','TaxDeposits@Intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'jira_16401.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"COUNT","dbColumnName":"null","encrypted":"false"},
"1":{"columnName":"PAYMENT_TEMPLATE","dbColumnName":"null","encrypted":"false"},
"2":{"columnName":"MM_STATUS","dbColumnName":"null","encrypted":"false"},
"3":{"columnName":"MM_PAYMENT_METHOD","dbColumnName":"null","encrypted":"false"},
"4":{"columnName":"No_of_Transaction_With_Money","dbColumnName":"null","encrypted":"false"},
"5":{"columnName":"MM_AMT","dbColumnName":"null","encrypted":"false"},
"6":{"columnName":"No_of_Zero_AchPayments","dbColumnName":"null","encrypted":"false"},
"7":{"columnName":"No_of_Zero_CheckPayments","dbColumnName":"null","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('VMP_Counts_Jira_20870','0 0 2 * * ? *',  1, -1,  DECODE(FN_GET_ENV(),'PROD','THR-TAVMPResults@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'jira_20870.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"DATE","dbColumnName":"null","encrypted":"false"},"1":{"columnName":"VMP_COUNTS","dbColumnName":"null","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('Manual_Ledger_Entry_Report','0 22 4 1 * ? *',  1, -1, DECODE(FN_GET_ENV(),'PROD','SBCPayrollCRT@intuit.com,Linda_Holler@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'psp-11365.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"PSID","dbColumnName":"null","encrypted":"false"},
"1":{"columnName":"ENTRY_DATE","dbColumnName":"null","encrypted":"false"},
"2":{"columnName":"MANUAL_LEDGER_ENTRY","dbColumnName":"null","encrypted":"false"},
"3":{"columnName":"PAYROLL_RUN_ID","dbColumnName":"null","encrypted":"false"},
"4":{"columnName":"SOURCE_PAY_RUN_ID","dbColumnName":"null","encrypted":"false"},
"5":{"columnName":"PAYROLL_RUN_TYPE","dbColumnName":"null","encrypted":"false"},
"6":{"columnName":"PAYMENT_TEMPLATE","dbColumnName":"null","encrypted":"false"},
"7":{"columnName":"PAYMENT_PERIOD","dbColumnName":"null","encrypted":"false"},
"8":{"columnName":"AGENT_ID","dbColumnName":"null","encrypted":"false"},
"9":{"columnName":"AGENT_NAME","dbColumnName":"null","encrypted":"false"},
"10":{"columnName":"NOTES","dbColumnName":"null","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('psp_6672_4_query','0 30 5 1 * ? *',  1, -1, DECODE(FN_GET_ENV(),'PROD','aditya_bhardwaj@intuit.com,linda_holler@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'PSP-6672-4.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"PSID","dbColumnName":"null","encrypted":"false"},
"1":{"columnName":"FEIN","dbColumnName":"PSP/Company_FedTaxId_AES256SIV","encrypted":"true"},
"2":{"columnName":"LEGAL_NAME","dbColumnName":"null","encrypted":"false"},
"3":{"columnName":"PAYMENT_SETTLEMENT_MONTH","dbColumnName":"null","encrypted":"false"},
"4":{"columnName":"PAYROLL_RUN_DATE","dbColumnName":"null","encrypted":"false"},
"5":{"columnName":"PAYROLL_RUN_TYPE","dbColumnName":"null","encrypted":"false"},
"6":{"columnName":"PAYROLL_RUN_STATUS","dbColumnName":"null","encrypted":"false"},
"7":{"columnName":"PAYCHECK_DATE","dbColumnName":"null","encrypted":"false"},
"8":{"columnName":"IS_BACKDATED","dbColumnName":"null","encrypted":"false"},
"9":{"columnName":"IS_NSF_RETURNED","dbColumnName":"null","encrypted":"false"},
"10":{"columnName":"IS_RETURNED_PMT_TRANS","dbColumnName":"null","encrypted":"false"},
"11":{"columnName":"PAYMENT_TEMPLATE_FK","dbColumnName":"null","encrypted":"false"},
"12":{"columnName":"MM_TRANSACTION_AMOUNT","dbColumnName":"null","encrypted":"false"},
"13":{"columnName":"SETTLEMENT_DATE","dbColumnName":"null","encrypted":"false"},
"14":{"columnName":"DUE_DATE","dbColumnName":"null","encrypted":"false"},
"15":{"columnName":"PAYMENT_PERIOD_END","dbColumnName":"null","encrypted":"false"}}')
/
 INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('PSP_Entitlement_AppUser','0 15 0 * * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','EMSIDC_OpsTeam@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'entitlement_appuser.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"CORP_ID","dbColumnName":"null","encrypted":"false"},
"1":{"columnName":"UID","dbColumnName":"null","encrypted":"false"},
"2":{"columnName":"FIRST_NAME","dbColumnName":"null","encrypted":"false"},
"3":{"columnName":"LAST_NAME","dbColumnName":"null","encrypted":"false"},
"4":{"columnName":"GROUPS.GROUPID","dbColumnName":"null","encrypted":"false"},
"5":{"columnName":"GROUPS.GROUPNAME","dbColumnName":"null","encrypted":"false"},
"6":{"columnName":"AUTH_USER_SEQ","dbColumnName":"null","encrypted":"false"}}')
/
 INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('PSP_Entitlement_DBUser','0 15 0 * * ? *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','EMSIDC_OpsTeam@intuit.com,SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'entitlement_dbuser.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"profileName","dbColumnName":"null","encrypted":"false"},
"1":{"columnName":"accountStatus","dbColumnName":"null","encrypted":"false"},
"2":{"columnName":"referenceID","dbColumnName":"null","encrypted":"false"},
"3":{"columnName":"defaultTableSpaceQuota","dbColumnName":"null","encrypted":"false"},
"4":{"columnName":"tempTableSpace","dbColumnName":"null","encrypted":"false"},
"5":{"columnName":"userName","dbColumnName":"null","encrypted":"false"},
"6":{"columnName":"globalDN","dbColumnName":"null","encrypted":"false"},
"7":{"columnName":"authenticationType","dbColumnName":"null","encrypted":"false"},
"8":{"columnName":"defaultTableSpace","dbColumnName":"null","encrypted":"false"},
"9":{"columnName":"groups.groupName","dbColumnName":"null","encrypted":"false"},
"10":{"columnName":"privilege.privilegeName","dbColumnName":"null","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  Values
  ('PSP_SOX_AppUser','0 0 8 ? * SUN *', 1, -1,  DECODE(FN_GET_ENV(),'PROD','Tech-t4i_IT_RISK_Compliance@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
   'soc_appuser.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"CORP_ID","dbColumnName":"null","encrypted":"false"},
"1":{"columnName":"UID","dbColumnName":"null","encrypted":"false"},
"2":{"columnName":"FIRST_NAME","dbColumnName":"null","encrypted":"false"},
"3":{"columnName":"LAST_NAME","dbColumnName":"null","encrypted":"false"},
"4":{"columnName":"GROUPS.GROUPID","dbColumnName":"null","encrypted":"false"},
"5":{"columnName":"GROUPS.GROUPNAME","dbColumnName":"null","encrypted":"false"},
"6":{"columnName":"AUTH_USER_SEQ","dbColumnName":"null","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, VERSION, REALM_ID, REPORT_SCHEDULE, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 VALUES
 ('Total_Paychecks', 1, -1, '0 0 2 ? * MON-FRI *', 'SBGPayrollPSPScrumTeam@intuit.com',
  'PSP-20407-1.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"TOTAL_PAYCHECK","dbColumnName":"null","dataType":"Long","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, VERSION, REALM_ID, REPORT_SCHEDULE, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
 VALUES
 ('Total_DD_Paychecks', 1, -1, '0 0 2 ? * MON-FRI *', 'SBGPayrollPSPScrumTeam@intuit.com',
  'PSP-20407-2.sql', 1, '/PSP/MONITOR', '{"0":{"columnName":"TOTAL_DD_PAYCHECK","dbColumnName":"null","dataType":"Long","encrypted":"false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
 (REPORT_NAME, VERSION, REALM_ID, REPORT_SCHEDULE, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES
('Assisted_Agency_aid_rpt_SUI', 1, -1, '0 14 7 1/1 * ? *', DECODE(FN_GET_ENV(),'PROD','Tax_ServiceSetup@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
 'assisted_agency_aid_rpt_SUI.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "CAN","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "PAYROLL_ADMIN_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
 "8":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "AID","dbColumnName": "PSP/CPTA_ATaxPayerId_AES256SIV","encrypted": "true"},
 "11":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "CURRENT_YR_LIA","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "LAW_CREATED_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
 (REPORT_NAME, VERSION, REALM_ID, REPORT_SCHEDULE, REPORT_MAILING_LIST,
 QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
VALUES
('Assisted_Agency_aid_rpt_SIT', 1, -1, '0 14 7 1/1 * ? *', DECODE(FN_GET_ENV(),'PROD','Tax_ServiceSetup@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
 'assisted_agency_aid_rpt_SIT.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "CAN","dbColumnName": "null","encrypted": "false"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "PAYROLL_ADMIN_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
 "8":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "AID","dbColumnName": "PSP/CPTA_ATaxPayerId_AES256SIV","encrypted": "true"},
 "11":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "CURRENT_YR_LIA","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "LAW_CREATED_DATE","dbColumnName": "null","encrypted": "false"}}')
/
INSERT INTO TEMP_PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST,
QUERY_FILENAME, IS_AUTOMATICALLY_SCHEDULED, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
Values
('Assisted_Agency_Info_Rpt_Others', '0 14 7 1/1 * ? *', 1, -1, DECODE(FN_GET_ENV(),'PROD','TaxDeposits@Intuit.com,Tax_ServiceSetup@intuit.com, Payrollservicesmaintenance@intuit.com, SBGPayrollPSPDevteam@intuit.com','SBGPayrollPSPScrumTeam@intuit.com'),
'assisted_agency_info_rpt_Others.sql', 1, '/PSP/MONITOR', '{"0":{"columnName": "FED_TAX_ID","dbColumnName": "PSP/Company_FedTaxId_AES256SIV","encrypted": "true"},
 "1":{"columnName": "PSID","dbColumnName": "null","encrypted": "false"},
 "2":{"columnName": "LEGAL_NAME","dbColumnName": "null","encrypted": "false"},
 "3":{"columnName": "ADDRESS_LINE_1","dbColumnName": "null","encrypted": "false"},
 "4":{"columnName": "ADDRESS_LINE_2","dbColumnName": "null","encrypted": "false"},
 "5":{"columnName": "ADDRESS_LINE_3","dbColumnName": "null","encrypted": "false"},
 "6":{"columnName": "CITY","dbColumnName": "null","encrypted": "false"},
 "7":{"columnName": "STATE","dbColumnName": "null","encrypted": "false"},
 "8":{"columnName": "ZIPCODE","dbColumnName": "null","encrypted": "false"},
 "9":{"columnName": "PRIMARY_PRINCIPAL_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "10":{"columnName": "PRIMARY_PRINCIPAL_EMAIL","dbColumnName": "null","encrypted": "false"},
 "11":{"columnName": "PRIMARY_PRINCIPAL_PHONE","dbColumnName": "null","encrypted": "false"},
 "12":{"columnName": "PAYROLL_ADMIN_CONTACT_NAME","dbColumnName": "null","encrypted": "false"},
 "13":{"columnName": "PAYROLL_ADMIN_EMAIL","dbColumnName": "null","encrypted": "false"},
 "14":{"columnName": "PAYROLL_ADMIN_PHONE","dbColumnName": "null","encrypted": "false"},
 "15":{"columnName": "DEPOSIT_FREQUENCY","dbColumnName": "null","encrypted": "false"},
 "16":{"columnName": "DEP_FREQ_CREATOR","dbColumnName": "null","encrypted": "false"},
 "17":{"columnName": "DEP_FREQ_CREATED_DATE","dbColumnName": "null","encrypted": "false"},
 "18":{"columnName": "AGENCY_ID","dbColumnName": "PSP/CAPT_ATaxPayerId_AES256SIV","encrypted": "true"},
 "19":{"columnName": "PAYMENT_TEMPLATE","dbColumnName": "null","encrypted": "false"},
 "20":{"columnName": "AID_NAME","dbColumnName": "null","encrypted": "false"},
 "21":{"columnName": "AID","dbColumnName": "PSP/CPTA_ATaxPayerId_AES256SIV","encrypted": "true"},
 "22":{"columnName": "Agency_Info_No","dbColumnName": "null","encrypted": "false"},
 "23":{"columnName": "PAYMENT_METHOD","dbColumnName": "null","encrypted": "false"},
 "24":{"columnName": "PAYMENT_METHOD_ENABLED","dbColumnName": "null","encrypted": "false"},
 "25":{"columnName": "ACH_REGISTERED","dbColumnName": "null","encrypted": "false"},
 "26":{"columnName": "LAW_ID","dbColumnName": "null","encrypted": "false"},
 "27":{"columnName": "LAW_DESC","dbColumnName": "null","encrypted": "false"},
 "28":{"columnName": "LAW_ACTIVE_FLAG","dbColumnName": "null","encrypted": "false"},
 "29":{"columnName": "LAW_EXEMPT_FLAG","dbColumnName": "null","encrypted": "false"},
 "30":{"columnName": "COMPANY_SERVICE","dbColumnName": "null","encrypted": "false"},
 "31":{"columnName": "COMPANY_SERVICE_STATUS","dbColumnName": "null","encrypted": "false"},
 "32":{"columnName": "CURRENT_YR_LIA","dbColumnName": "null","encrypted": "false"},
 "33":{"columnName": "LAW_CREATED_DATE","dbColumnName": "null","encrypted": "false"}}')
/
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_REPORT_JOB_SETUP
(REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS)
  SELECT
    REPORT_NAME, REPORT_SCHEDULE, VERSION, REALM_ID, REPORT_MAILING_LIST, IS_AUTOMATICALLY_SCHEDULED, QUERY_FILENAME, REPORT_NAMESPACE, ENCRYPTED_FIELDS
  FROM
    TEMP_PSP_REPORT_JOB_SETUP tt
  WHERE
    tt.REPORT_NAME NOT IN (SELECT REPORT_NAME FROM PSP_REPORT_JOB_SETUP)
/

DELETE FROM PSP_REPORT_JOB_SETUP
WHERE
  REPORT_NAME NOT IN (SELECT REPORT_NAME FROM TEMP_PSP_REPORT_JOB_SETUP)
/

UPDATE PSP_REPORT_JOB_SETUP RT
SET (RT.REPORT_SCHEDULE, RT.VERSION, RT.REALM_ID, RT.REPORT_MAILING_LIST, RT.IS_AUTOMATICALLY_SCHEDULED, RT.QUERY_FILENAME, RT.REPORT_NAMESPACE, RT.ENCRYPTED_FIELDS) =
(SELECT
TT.REPORT_SCHEDULE, TT.VERSION, TT.REALM_ID, TT.REPORT_MAILING_LIST, TT.IS_AUTOMATICALLY_SCHEDULED, TT.QUERY_FILENAME, TT.REPORT_NAMESPACE, TT.ENCRYPTED_FIELDS
FROM
TEMP_PSP_REPORT_JOB_SETUP TT
WHERE
TT.REPORT_NAME = RT.REPORT_NAME
)
/

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_REPORT_JOB_SETUP
/

COMMIT;
