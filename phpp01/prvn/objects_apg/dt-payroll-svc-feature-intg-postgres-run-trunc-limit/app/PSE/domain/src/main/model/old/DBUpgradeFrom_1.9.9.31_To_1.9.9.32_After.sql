--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.31_To_1.9.9.32.sql
--
-- Developers can hand code logic here for data migration purposes
--

DROP TABLE "PSP_BATCH_JOB_SETUP" CASCADE CONSTRAINTS
    
/

CREATE TABLE "PSP_BATCH_JOB_SETUP"
(
	PRIMARY KEY(JOB_TYPE, "REALM_ID")
	, "JOB_TYPE" VARCHAR2(255 CHAR)  NOT NULL 
	, "VERSION" NUMBER(19,0)  NOT NULL 
	, "REALM_ID" NUMBER(19,0) DEFAULT -1 NOT NULL 
	, "DLY_BW_RETRIES_TIMER_EXPR" VARCHAR2(100 CHAR)   
	, "IS_AUTOMATICALLY_SCHEDULED" NUMBER(1,0)   
	, "JOB_PROCESSOR_CLASS_NAME" VARCHAR2(100 CHAR)   
	, "MAX_RETRIES" NUMBER(10,0)   
	, "JOB_TIMER_EXPRESSION" VARCHAR2(100 CHAR)   
)

/

ALTER TABLE PSP_BATCH_JOB_SETUP ADD CONSTRAINT C_PSP_BATCH_JOB_SETUP0 CHECK(JOB_TYPE IN('AchOffload', 'AchOffloadConfirmationReceivedMonitor', 'AchOffloadFileCreationCompleteMonitor', 'AchOffloadFileCreationStartedMonitor', 'AchOffloadFileSendCompleteMonitor', 'AchOffloadFileSendStartedMonitor', 'AchOffloadSuccessfulMonitor', 'AchReturns', 'AchReturnsFileReceivedMonitor', 'AchReturnsSuccessfulMonitor', 'Migration', 'MissedPayrolls', 'MissedPayrollsSuccessful', 'MissedTransactions', 'Emails', 'FeeEvents', 'SalesTaxException', 'LedgerBalanceMonitor', 'As400ToCris', 'CrisToSourceSystem', 'PspToCris', 'DicrFiles', 'DicrFilesSuccessfulMonitor', 'FraudPayrolls', 'GemsDailyUpload', 'GemsMonthlyUpload', 'MissedTransactionsSuccessful', 'LedgerBalance', 'FeeEventsMonitor'))

/


update PSP_BATCH_JOB_AUDIT_LOG set IS_VERIFIED = 1
/