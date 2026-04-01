DECLARE

	table_exists PLS_INTEGER;

BEGIN

	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_SERVICE_STATUS';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_SERVICE_STATUS" CASCADE CONSTRAINTS';
	END IF;

	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_SERVICE_SUB_STATUS';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_SERVICE_SUB_STATUS" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_SERVICE_STATUS
(
  SERVICE_STATUS_CD  VARCHAR2(255)         NOT NULL,
  VERSION            NUMBER(19)            NOT NULL,
  DESCRIPTION        VARCHAR2(4000),
  NAME               VARCHAR2(100)
)
/

CREATE TABLE TEMP_PSP_SERVICE_SUB_STATUS
(
  SERVICE_SUB_STATUS_CD  VARCHAR2(255)         NOT NULL,
  VERSION            NUMBER(19)            NOT NULL,
  SERVICE_STATUS_FK  VARCHAR2(255) NOT NULL,
  DESCRIPTION        VARCHAR2(4000),
  IS_SET_MANUALLY    NUMBER(1,0),
  IS_REMOVED_MANUALLY NUMBER(1,0),
  NAME               VARCHAR2(100)
)
/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------
INSERT INTO TEMP_PSP_SERVICE_STATUS (SERVICE_STATUS_CD, NAME, DESCRIPTION, VERSION) VALUES (
'Active', 'Active', 'Customer is active on the service', 0)
/

INSERT INTO TEMP_PSP_SERVICE_STATUS (SERVICE_STATUS_CD, NAME, DESCRIPTION, VERSION) VALUES (
'Cancelled', 'Cancelled', 'Customer cancelled the service', 0)
/

INSERT INTO TEMP_PSP_SERVICE_STATUS (SERVICE_STATUS_CD, NAME, DESCRIPTION, VERSION) VALUES (
'OnHold', 'On Hold', 'Customer is on hold', 0)
/

INSERT INTO TEMP_PSP_SERVICE_STATUS (SERVICE_STATUS_CD, NAME, DESCRIPTION, VERSION) VALUES (
'PendingActivation', 'Pending Activation', 'Customer is pending activation on the service', 0)
/

INSERT INTO TEMP_PSP_SERVICE_STATUS (SERVICE_STATUS_CD, NAME, DESCRIPTION, VERSION) VALUES (
'Terminated', 'Terminated', 'Customer was terminated by Intuit', 0)
/


INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'AchRejectOther', 'ACH Reject Other', 'Payroll debit rejects, Intuit receives bank return file and file is imported into PSP.  In the file, returns are coded, the system needs to automatically place this hold to all returns other than R01 or R09. Client is placed on hold until Intuit receives the funds from the customer.', 'OnHold', 0, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'AchRejectR1R9', 'ACH Reject R01-R09', 'Anything that is an ER payroll debit rejects and coded R01-R09 receives this hold code. Intuit receives bank return file and file is imported into PSP.  In the file, returns are coded, the system needs to automatically place this hold code for R01 or R09', 'OnHold', 0, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'ActiveCurrent', 'Active Current', 'Customer is active on the service with no restrictions', 'Active', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'ActiveSeasonal', 'Active Seasonal', 'Customer has not cancelled service and are actively paying for their monthly fees�just not processing any payrolls. Inactivity fee is charged on the first day of the new month for any Assisted client who did not submit a payroll in the previous month, regardless of whether the date is a holiday or weekend it is billed that day, then offloads on the next possible business day. - Assisted only', 'Active', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'AuditCorrections', 'Audit Corrections', 'Back end corrections to ledger and balance file, prevent money from being paid that has already been paid', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'AS400Hold', 'AS400 Hold Reason', 'The company is on hold in the AS400 for a general OnHold reason', 'OnHold', 0, 0, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'AS400DirectDepositLimitHold', 'AS400 Direct Deposit Limit Hold Reason', 'The company is on hold in the AS400 for a DDLimits OnHold reason', 'OnHold', 0, 0, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'Cancelled', 'Cancelled', 'Customer has voluntarily cancelled tax service and all services can be ''turned off''.', 'Cancelled', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'DirectDepositLimit', 'Direct Deposit Limit', 'Client sent payroll over DD Limit, Exposure Limit four times.  Account is automatically placed on hold.', 'OnHold', 0, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'BillPaymentLimit', 'Bill Payment Limit', 'Client sent payroll over BP Limit, Exposure Limit four times.  Account is automatically placed on hold.', 'OnHold', 0, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'Fraud', 'Fraud', 'SBD Risk Management has identified client as processing fraudulent payrolls.', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'AMLHold', 'AML Hold', 'SBD Risk Management has identified client has failed to clear AML Fraud Check.', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'FraudReview', 'Fraud Review', 'PSP has sytematically identified potential fraud based on criteria.  Risk Assessmenet team would need to remove these holds, but customers can continue to update their accounts, just not process payroll until hold is removed.', 'OnHold', 0, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'IntuitCollections', 'Intuit Collections', 'Client is placed on hold manually for internal collections which includes the following reasons - Assisted Collectable, Historical Collectable, Repayment Agreement, Notes Receivable etc.', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'MissingPaperwork', 'Missing Paperwork', 'Assumption is that customer is active but a change has required new paperwork to be sent.  Create a generic subStatusCd that prohibits the ability to upload a payroll, create a notes field for tax to key in why, this will cover all tax sub subStatusCd hold reasons', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingBalanceFile', 'Pending Balance File', 'Status for a Tax Service customer who has created their PINs, but has not yet sent their balance file and before they''ve sent their first payroll', 'PendingActivation', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingBankVerification', 'Pending Bank Verification', 'Pre-reqs done for set up and now awaiting the 2 small bank debits.', 'PendingActivation', 0, 0, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingEnrollment', 'Pending Enrollment', 'Pre-reqs done for set up and now awaiting enrollment activities.', 'PendingActivation', 0, 0, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingTaxAcceptance', 'Pending Tax Acceptance', 'Pre-reqs done for enrollments now awaiting customer acceptance.', 'PendingActivation', 0, 0, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingFirstPayroll', 'Pending First Payroll', 'Status after the customer Balance file has been received and approved, but before the first payroll has been received ready for that payroll.  Last step in Pending Activation service subStatusCd.', 'PendingActivation', 0, 0, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingPinCreation', 'Pending PIN Creation', 'Status after the customer signup request has been received, and bank verifcation is complete, but prior to the customer creating their PIN', 'PendingActivation', 0, 0, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingPrefundingWire', 'Pending Prefunding Wire', 'Agent places a customer in this status when they are waiting for a customer to submit a payroll that is greater than their dd limits, that will be funded by a wire transaction', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingTermination', 'Pending Termination', 'Customer is under review and is pending termination from all services', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'RiskAssessment', 'Risk Assessment', 'Possible Fraud or Risk to Intuit.  Client that has 3 or more NSFs on file within 12 month period.  Client will also be placed on Risk Assessment hold, when identified as possible fraudulent activity.', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'RiskCollections', 'Risk Collections', 'Hold code changes when ACH Reject is not paid in full, or required information is not received by Intuit from client. ', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'SuspendedDirectDeposit', 'Suspended Direct Deposit', 'Financial resolutions has made arrangements with a customer to bar them from using the direct deposit service for a period of time (most likely in increments of 3,6,9, or 12 months?)', 'OnHold', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'Terminated', 'Terminated', 'Intuit has determined the customer in question poses a financial risk and has been terminated from all services', 'Terminated', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'PendingSetup', 'Pending Setup', 'Status for a Tax Service customer who has not finished the setup process', 'PendingActivation', 1, 1, 0)
/
INSERT INTO TEMP_PSP_SERVICE_SUB_STATUS ( SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)  VALUES (
'MTLHold', 'MTL Compliance Hold', 'Non-compliance to MTL workflow', 'OnHold', 1, 1, 0)
/
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_SERVICE_STATUS
   (SERVICE_STATUS_CD, NAME, DESCRIPTION, VERSION)
SELECT
    SERVICE_STATUS_CD, NAME, DESCRIPTION, VERSION
FROM
   TEMP_PSP_SERVICE_STATUS ss
WHERE
   ss.SERVICE_STATUS_CD NOT IN (SELECT SERVICE_STATUS_CD FROM PSP_SERVICE_STATUS)
/

INSERT INTO PSP_SERVICE_SUB_STATUS
   (SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION)
SELECT
    SERVICE_SUB_STATUS_CD, NAME, DESCRIPTION, SERVICE_STATUS_FK, IS_SET_MANUALLY, IS_REMOVED_MANUALLY, VERSION
FROM
   TEMP_PSP_SERVICE_SUB_STATUS ss
WHERE
   ss.SERVICE_SUB_STATUS_CD NOT IN (SELECT SERVICE_SUB_STATUS_CD FROM PSP_SERVICE_SUB_STATUS)
/

DELETE FROM PSP_SERVICE_SUB_STATUS
WHERE SERVICE_SUB_STATUS_CD NOT IN (SELECT SERVICE_SUB_STATUS_CD FROM TEMP_PSP_SERVICE_SUB_STATUS)
/

DELETE FROM PSP_SERVICE_STATUS
WHERE SERVICE_STATUS_CD NOT IN (SELECT SERVICE_STATUS_CD FROM TEMP_PSP_SERVICE_STATUS)
/

UPDATE PSP_SERVICE_STATUS rt
SET ( rt.NAME, rt.DESCRIPTION, rt.VERSION) =
(SELECT ss.NAME, ss.DESCRIPTION, ss.VERSION
 FROM TEMP_PSP_SERVICE_STATUS ss WHERE ss.SERVICE_STATUS_CD = rt.SERVICE_STATUS_CD)
/

UPDATE PSP_SERVICE_SUB_STATUS rt
SET ( rt.NAME, rt.DESCRIPTION, rt.VERSION, rt.SERVICE_STATUS_FK, rt.IS_SET_MANUALLY, rt.IS_REMOVED_MANUALLY) =
(SELECT ss.NAME, ss.DESCRIPTION, ss.VERSION, ss.SERVICE_STATUS_FK, ss.IS_SET_MANUALLY, ss.IS_REMOVED_MANUALLY
 FROM TEMP_PSP_SERVICE_SUB_STATUS ss WHERE ss.SERVICE_SUB_STATUS_CD = rt.SERVICE_SUB_STATUS_CD)
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_SERVICE_STATUS
/

DROP TABLE TEMP_PSP_SERVICE_SUB_STATUS
/

COMMIT



