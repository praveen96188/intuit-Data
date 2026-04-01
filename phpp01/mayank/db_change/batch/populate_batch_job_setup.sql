DECLARE

  table_exists PLS_INTEGER;

BEGIN

  SELECT COUNT(*) INTO table_exists
  FROM "USER_TABLES"
  WHERE TABLE_NAME = 'TEMP_PSP_BATCH_JOB_SETUP';

  IF table_exists = 1 THEN
    EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_BATCH_JOB_SETUP" CASCADE CONSTRAINTS';
  END IF;

END;
/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_BATCH_JOB_SETUP
(
  PRIMARY KEY(JOB_TYPE, "REALM_ID")
  , "JOB_TYPE" VARCHAR2(255 CHAR)  NOT NULL
  , "JOB_TIMER_EXPRESSION" VARCHAR2(100 CHAR)  NOT NULL
  , "VERSION" NUMBER(19,0)  NOT NULL
  , "REALM_ID" NUMBER(19,0) DEFAULT -1 NOT NULL
  , "DLY_BW_RETRIES_TIMER_EXPR" VARCHAR2(100 CHAR)
  , "IS_AUTOMATICALLY_SCHEDULED" NUMBER(1,0)
  , "JOB_PROCESSOR_CLASS_NAME" VARCHAR2(100 CHAR)
  , "JOB_NAMESPACE" VARCHAR2(100 CHAR)
  , "MAX_RETRIES" NUMBER(10,0)
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
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SalesTaxExceptionMonitor', '0 0 0 22 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.SalesTaxExceptionMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SalesTaxExceptionProcessor', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.SalesTaxExceptionProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AnnualBillingMonitor',   '+1s', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AnnualBillingMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AnnualBillingProcessor', '0 0 0 ? * * 1900', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.AnnualBillingProcessor',  '/PSP/NORMAL',  0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchOffloadCompleteMonitor', '0 0 0 20 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AchOffloadCompleteMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchReturnsMonitor', '0 0 0 6 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AchReturnsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchTransactionsMonitor', '0 0 0 6 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AchTransactionsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFDataExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.ATFDataExtractProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFCompanyLiabilityExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CompanyLiabilitiesExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFCompanyPaymentExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CompanyPaymentsExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFDepositFrequencyExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.DepositFrequencyExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFCompanyInfoExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CompanyInfoExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFEmployeeInfoExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.EmployeeInfoExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFWageLimitsExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.WageLimitsExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EmailGateway', '0 0 0-59/5 * * * * * * * *', 1, -1, '+5m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EmailGatewayProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EmailGatewayMonitor', '0 0 0-59/10 * * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EmailGatewayMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('OffloadedTransactionsEvents', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.OffloadedTransactionsEventsProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('OffloadedTransactionsEventsMonitor', '+45m', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.OffloadedTransactionsEventsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('FraudPayrolls', '+1m', 1, -1, '+1m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.FraudPayrollsProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('FraudPayrollsMonitor', '0 0 0-59/15 * * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.FraudPayrollsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('GemsAccountsReceivable', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.GemsAccountsReceivableProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('GemsAccountsReceivableMonitor', '0 0 0 20 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.GemsAccountsReceivableMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('GemsGeneralLedger', '0 0 0 3 1 * * * * * *', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.GemsGeneralLedgerProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('GemsGeneralLedgerMonitor', '0 0 0 7 2 * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.GemsGeneralLedgerMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('GemsGeneralLedgerUpload', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.GemsGeneralLedgerProcessor', '/PSP/NORMAL', 2)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('GemsGeneralLedgerUploadMonitor', '+15m', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.GemsGeneralLedgerUploadMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('LedgerBalance', '0 0 0 1 * * * * * * *', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.LedgerBalanceProcessor', '/PSP/HIGH', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('LedgerBalanceMonitor', '0 0 0 5 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.LedgerBalanceMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('LedgerOperations', '+1m', 1, -1, '+1m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.LedgerOperationsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('MissedPayrollsMonitor', '0 0 0 20 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.MissedPayrollsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('MissedTransactionsMonitor', '0 0 0 20 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.MissedTransactionsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('NightlyBatchJobs', '0 0 15 5 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.NightlyBatchJobsProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('NightlyBatchJobsMonitor', '0 0 20 5 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.NightlyBatchJobsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('PrimaryAchOffloadMonitor', '+1s', 1, -1, '+5m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.PrimaryAchOffloadMonitor', '/PSP/MONITOR', 99)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('PrimaryDailyBatchJobs', '0 0 00 18 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.PrimaryDailyBatchJobsProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('PrimaryDailyBatchJobsMonitor', '0 0 01 18 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.PrimaryDailyBatchJobsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('RAFWriter', '+1s', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.raf.RAFProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ScheduledAchOffloadMonitor', '+1s', 1, -1, '+5m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.ScheduledAchOffloadMonitor', '/PSP/MONITOR', 99)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ScheduledDailyBatchJobs', '0 0 5 19 * * * * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ScheduledDailyBatchJobsProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ScheduledDailyBatchJobsMonitor', '0 0 6 19 * * * * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.ScheduledDailyBatchJobsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ThirdParty401kOffload', '0 0 5 7 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ThirdParty401kOffloadProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ThirdParty401kOffloadMonitor', '0 0 20 7 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.ThirdParty401kOffloadMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ThirdParty401kValidation', '0 0 0 9 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ThirdParty401kValidationProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ThirdParty401kValidationMonitor', '0 0 15 9 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.ThirdParty401kValidationMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('CheckPrint', '0 0 0-59/10 * * * * * * * *', 1, -1, '+10m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.CheckPrintProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('CheckPrintMonitor', '0 0 0-59/15 * * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.CheckPrintMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EntitlementProcessor', '+1m', 1, -1, '+5m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EntitlementProcessorMonitor', '0 0 0-59/15 * * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EntitlementProcessorMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AMOMessageProcessor', '+1m', 1, -1, '+5m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.AMOMessageProcessor', '/PSP/FLUX03', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AMOMessageProcessorMonitor', '0 0 0-59/15 * * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AMOMessageProcessorMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('TaxCreditsEchoSign', '+5m', 1, -1, '+5m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.TaxCreditsEchoSignSyncProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('TaxCreditsEchoSignMonitor', '0 0 0-59/15 * * * * * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.TaxCreditsEchoSignSyncMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('QbdtUnprocessedRequestsRetry', '+5m', 1, -1, '+5m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.RetryQbdtUnprocessedRequestProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsEnrollments', '0 0 0 9,12,15 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsEnrollmentsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsEnrollmentsMonitor', '0 0 0 10,13,16 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EftpsEnrollmentsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsSend', '0 0 0-59/15 * * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsSendProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsSendMonitor', '0 0 0-59/30 * * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EftpsSendMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsResponse', '0 0 0-59/15 * * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsResponseProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsResponseMonitor', '0 0 0-59/30 * * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EftpsResponseMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsEnrollmentsAgeOut', '0 0 0 7 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsEnrollmentsAgeOutProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsEnrollmentsAgeOutMonitor', '0 0 0 8 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EftpsEnrollmentsAgeOutMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsPayment', '0 0 35 7,14 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EftpsPaymentProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EftpsPaymentMonitor', '0 0 45 12,15 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EftpsPaymentMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IOPDataSync', '0 0 0-59/5 * * * * * * * *', 1, -1, '+5m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.IopSyncProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IOPDataSyncMonitor', '0 0 0-59/10 * * * * * * * *', 1, -1, '+5m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.IOPDataSyncMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('PrintedCheckBatch', '0 0 35 6,15 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.PrintedCheckBatchProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('PrintedCheckBatchMonitor', '0 0 45 6,15 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.PrintedCheckBatchMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ReconPlus', '0 0 0 21 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ReconPlusProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ReconPlusMonitor', '0 0 15 21 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.ReconPlusMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchTaxPaymentOffload', '0 0 30 13 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.AchTaxPaymentOffloadProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchTaxPaymentOffloadMonitor', '0 0 45 13 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AchTaxPaymentOffloadMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchZeroPayments', '0 0 00 22 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.AchZeroPaymentsProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchZeroPaymentsMonitor', '0 0 01 22 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AchZeroPaymentsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchDebitOffload', '0 0 00 14 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.AchDebitOffloadProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AchDebitOffloadMonitor', '0 0 01 14 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.AchDebitOffloadMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('StateReport', '0 0 0 1,16 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.StateReportProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('StateReportMonitor', '0 0 30 1,16 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.StateReportMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EdiPayment', '0 0 5 13 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EdiPaymentProcessor', '/PSP/HIGH', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EdiPaymentMonitor', '0 0 35 13 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EdiPaymentMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EdiSend', '0 0 0 0-23 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EdiSendProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EdiSendMonitor', '0 0 30 0,2,4,6,8,10,12,14,16,18,20,22 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EdiSendMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EdiResponse', '0 0 30 0-23 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EdiResponseProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EdiResponseMonitor', '0 0 0 0,2,4,6,8,10,12,14,16,18,20,22 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EdiResponseMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EoqSUIAdjustments', '0 0 0 ? * * 1900', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EoqSUIAdjustmentsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EoqSUIAdjustmentsMonitor', '0 0 10 23 1 feb,may,aug,nov * * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EoqSUIAdjustmentsMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('PSPToEMSBSDataSyncProcessor', '+5m', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.PSPToEMSBSDataSyncProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EMSBSToBRMDataSyncProcessor', '0 0 00 20 $ * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EMSBSToBRMDataSyncProcessor', '/PSP/HIGH', 0)
/

INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('BRMUsageErrorFileProcessor', '0 0 0 0-23/6 1 * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.BRMUsageErrorFileProcessor', '/PSP/NORMAL', 0)
/

INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EnrollmentDeleteSelectionProcessor', '0 0 45 19 $ * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EnrollmentDeleteSelectionProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EnrollmentDeleteSelectionMonitor',   '0 0 30 21 $ * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitor.EnrollmentDeleteSelectionMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('MonthlyFee', '0 0 30 4 1 * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.MonthlyFeeProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('MonthlyFeeMonitor', '0 0 0 6 1 * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.MonthlyFeeMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFCompanyTaxExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CompanyTaxExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFCompanyTaxRateExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CompanyTaxRateExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFEmployeeTotalsExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.EmployeeTotalsExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EmployeeTotalsCalculationProcess', '+1H', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.EmployeeTotalsCalculationProcess', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EmployeeTotalsCalculationMonitor', '0 0 0 0-23/2 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.EmployeeTotalsCalculationMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('FsetFilingProcessor', '0 0 30 16 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.FsetFilingProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('FsetFilingMonitor', '0 0 0 17 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.FsetFilingMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('FsetResponseProcessor', '0 0 0 8 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.FsetResponseProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('FsetResponseMonitor', '0 0 30 8 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.FsetResponseMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ScheduledEmails', '0 0 6 7 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ScheduledEmailsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EmployeeW2TotalsCalculationProcessor', '0 0 0 ? * * 1900', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EmployeeW2TotalsCalculationProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SendW2AnnualDataToTFSProcessor',  '0 0 0 ? * * 1900', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.SendW2AnnualDataToTFSProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SendW2PreviewDataToTFSProcessor', '0 0 0 ? * * 1900', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.SendW2PreviewDataToTFSProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SendMonthlyDataToTFSProcessor', '0 0 0 3 12 feb,mar,may,jun,aug,sep,nov,dec * * * * *', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.SendMonthlyDataToTFSProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SendMonthlyDataToTFSMonitor', '0 0 30 3 12 feb,mar,may,jun,aug,sep,nov,dec * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.SendMonthlyDataToTFSMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('W2CountsExtract', '0 0 0 3 1 jan * * * * *', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.W2CountsExtractProcess', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('WorkersCompProcessor', '0 0 0 */1 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.WorkersCompProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('WorkersCompMonitor', '0 0 50 */1 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.WorkersCompMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('OFACReportProcessor', '0 0 0 23 * * mon-fri * * * *', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.OFACReportProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('OFACReportMonitor', '0 0 30 23 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.JPMCDDScreening.OFACReportMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AMLReportProcessor', '0 0 0 23 * * mon-fri * * * *', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.AMLReportProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AMLReportMonitor', '0 0 30 23 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.JPMCDDScreening.AMLReportMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IndustryReportProcessor', '0 0 0 23 * * mon-fri * * * *', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.IndustryReportProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IndustryReportMonitor', '0 0 25 00 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.JPMCDDScreening.IndustryReportMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('TPSUReportProcessor', '0 0 30 23 * * mon-fri * * * *', 1, -1, '+15m', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.JPMCDDScreening.TPSUReportProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('TPSUReportMonitor', '0 0 25 00 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.JPMCDDScreening.TPSUReportMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ACHEnrollmentBatchJob', '+1s', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ACHEnrollmentProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ACHDeEnrollmentBatchJob', '+1s', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ACHDeEnrollmentProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ACHEnrollmentResponseBatchJob', '+1s', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.ACHEnrollmentResponseProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IamEmailAddressProcessor', '0 0 0 */1 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.processors.IamEmailAddressProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IamEmailAddressMonitor', '0 0 0 */2 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.IamEmailAddressMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EmployeePayrollItemTotalsCalcProcess', '+1s', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.EmployeePayrollItemCalculationProcess', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IRSDepositFrequencyFileProcessor', '0 0 0 12 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.IRSDepositFrequencyFileProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('IRSDepositFrequencyFileProcessorMonitor', '0 0 0 13 * * mon-fri * * * *', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.batchjobs.monitors.IRSDepositFrequencyFileProcessorMonitor', '/PSP/MONITOR', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SUICreditsBatchJob', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.processors.SUICreditsProcessor', '/PSP/NORMAL', 5)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ATFCompanyPayrollItemExtract', '+1s', 1, -1, '+15m', 0, 'com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CompanyPayrollItemExtractProcess', '/PSP/NORMAL', 5)
/


--Write Cron Jobs
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('FailedPayrollPlSqlJobsProcessor', '0 0 0 5 28-31 * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
--INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
--VALUES ('DailyPayrollStatsPlSqlJobsProcessor', '0 0 0 19 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
--/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('PayrollFraudBatchPurgePlSqlJobsProcessor', '0 0 4 23 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
--need to check
--INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
--VALUES ('PSPEventLogPurgePlSqlJobsProcessor', '0 0 8 21 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
--/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EFTPSOnHoldPaymentPlSqlJobsProcessor', '0 0 31 * * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ValidateEmployeeWagePlansPlSqlJobsProcessor', '0 0 0 18 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('NCDFixPlSqlJobsProcessor', '0 0 30 8 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('OfferingUpdateUsageBillingPlSqlJobsProcessor', '0 0 0 23 * * sun * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EDRAssociationFixPlSqlJobsProcessor', '0 0 12 00-23 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('RetryEntitlementActivationPlSqlJobsProcessor', '0 0 0 22 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AssistedUsageDataSyncProcessor', '+5m', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.AssistedUsageDataSyncProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('NCDFixALLPlSqlJobsProcessor', '0 0 0 23 14,28 * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('CostCoPlSqlJobsProcessor', '0 0 30 23 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.PlSqlJobsProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AccountServiceSyncExceptionProcessor', '0 0 30 23 * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.AccountServiceSyncExceptionProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('AssistedUsageReportingToBRMProcessor', '0 0 30 20 $ * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.AssistedUsageReportingToBRMProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('RTBAutomation', '0 0 5 * * * * * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.rtbAutomation.RTBAutomationProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SoxDBUserReport', '0 0 0 23 * * sun * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.SoxDBUsersProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ComplianceToolKit', '0 0 0 ? * * 1900', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.ComplianceToolKitProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('ACHTraceIdProcessor', '0 0 00 23 * * mon-fri * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.ACHTraceIdProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EntityEvent', '+5m', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.entitylistener.EntityEventProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EntityEventRetry', '+3H', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.entitylistener.EntityEventRetryProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('MtlTransactionReportEnrichProcessor', '+3H', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.MtlTransactionReportEnrichProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('SoxReport', '0 0 0 23 * * sat * * * *', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.SoxReportProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EntityInitialLoadProcessor', '+5m', 1, -1, '', 0, 'com.intuit.sbd.payroll.psp.jss.processors.EntityInitialLoadProcessor', '/PSP/NORMAL', 0)
/
INSERT INTO TEMP_PSP_BATCH_JOB_SETUP ( JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
VALUES ('EVSCompanyProcessor', '+1H', 1, -1, '', 1, 'com.intuit.sbd.payroll.psp.jss.processors.EVSCompanyProcessor', '/PSP/NORMAL', 0)
/
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_BATCH_JOB_SETUP
(JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES)
  SELECT
    JOB_TYPE, JOB_TIMER_EXPRESSION, VERSION, REALM_ID, DLY_BW_RETRIES_TIMER_EXPR, IS_AUTOMATICALLY_SCHEDULED, JOB_PROCESSOR_CLASS_NAME, JOB_NAMESPACE, MAX_RETRIES
  FROM
    TEMP_PSP_BATCH_JOB_SETUP tt
  WHERE
    tt.JOB_TYPE NOT IN (SELECT JOB_TYPE FROM PSP_BATCH_JOB_SETUP)
/

DELETE FROM PSP_BATCH_JOB_PARAMETER
WHERE BATCH_JOB_SETUP_FK NOT IN (SELECT JOB_TYPE FROM TEMP_PSP_BATCH_JOB_SETUP)
/
DELETE FROM PSP_BATCH_JOB_SETUP
WHERE
  JOB_TYPE NOT IN (SELECT JOB_TYPE FROM TEMP_PSP_BATCH_JOB_SETUP)
/

UPDATE PSP_BATCH_JOB_SETUP RT
SET (RT.JOB_TIMER_EXPRESSION, RT.VERSION, RT.REALM_ID, RT.DLY_BW_RETRIES_TIMER_EXPR, RT.IS_AUTOMATICALLY_SCHEDULED, RT.JOB_PROCESSOR_CLASS_NAME, RT.JOB_NAMESPACE, RT.MAX_RETRIES) =
(SELECT
TT.JOB_TIMER_EXPRESSION, TT.VERSION, TT.REALM_ID, TT.DLY_BW_RETRIES_TIMER_EXPR, TT.IS_AUTOMATICALLY_SCHEDULED, TT.JOB_PROCESSOR_CLASS_NAME, TT.JOB_NAMESPACE, TT.MAX_RETRIES
FROM
TEMP_PSP_BATCH_JOB_SETUP TT
WHERE
TT.JOB_TYPE = RT.JOB_TYPE
)
/

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_BATCH_JOB_SETUP
/

COMMIT;
