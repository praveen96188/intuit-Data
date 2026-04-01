DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_LEDGER_ACCOUNT';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_LEDGER_ACCOUNT" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_LEDGER_ACCOUNT
( 
	"LEDGER_ACCOUNT_CD" VARCHAR2(255 CHAR) NOT NULL,
	"VERSION" NUMBER(19,0) NOT NULL, 
	"DESCRIPTION" VARCHAR2(4000 CHAR),
	"ACCOUNT_ABBREVIATION" VARCHAR2 (20 CHAR),
	"NAME" VARCHAR2(4000 CHAR),
    "BALANCE_CALCULATION_RULE" VARCHAR2(255 CHAR) NOT NULL,
    "LEDGER_ACCOUNT_TYPE" VARCHAR2(255 CHAR),
    "REPORTING_FREQUENCY" VARCHAR2(255 CHAR),
    "REQUIRES_QUARTER_LAW" NUMBER(1,0) NOT NULL,
    CONSTRAINT LEDGER_ACCOUNT_UNIQUE UNIQUE (LEDGER_ACCOUNT_CD),
    CONSTRAINT ACCOUNT_ABBREVIATION_UNIQUE UNIQUE (ACCOUNT_ABBREVIATION)
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REQUIRES_QUARTER_LAW) VALUES ('DDFutureReceivable', 'DDFR', 'DD Future Receivable', 'Money that we expect to get from the employer.  Calculate how much we are going to request from the employer.',0,'DebitAddsToBalance', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REQUIRES_QUARTER_LAW) VALUES ('DDFutureLiability', 'DDFL', 'DD Future Liability', 'Money that we expect to owe to someone else, usuaLy an employee.',0,'CreditAddsToBalance', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('DDCurrentCash', 'DDCC', 'DD Current Cash', 'Money we have requested from the employer.', 0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('DDCurrentLiability', 'DDCL', 'DD Current Liability', 'Money we own based upon received funds.', 0,'CreditAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('ERReturnReceivable', 'ERRR', 'ER Return Receivable', 'money that we expect to exchange with the employer. the offset of employer return cash - double entry bookkeeping.',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('ERReturnCash', 'ERRC', 'ER Return Cash', 'Money that we have requested from the employer or tried to refund to the employer that has been rejected.',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('EEReturnCash', 'EERC', 'EE Return Cash', 'Any employee credit that is rejected.',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('EEReturnLiablility', 'EERL', 'EE Return Liability ', 'the offset of employee return cash - double entry bookkeeping, credit one account debit another account of equal amount.',0,'CreditAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('FeeCashBalanceSheet', 'FCB', 'Fee Cash Balance Sheet', 'Money that we have requested from the employer for payroll transactions',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REQUIRES_QUARTER_LAW) VALUES ('FeeCashRevenue', 'FCR', 'Fee Cash Revenue', 'Money that we have requested from the employer for service fees.',0,'DebitAddsToBalance', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, LEDGER_ACCOUNT_TYPE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('FeeIncome', 'FI', 'Fee Income', 'the offset for fee receivable - e.g. debit fee receivable then credit fee income for the same amount.',0,'CreditAddsToBalance','Income','Daily', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('BadDebt', 'BD', 'Bad Debt', 'A write off where an employer does not pay Intuit for services rendered.',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, LEDGER_ACCOUNT_TYPE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('SalesAndUseTax', 'SUTAX', 'Sales & Use Tax Liability', 'Tax collected on behalf of the customer for sales and use tax associated with service fees.',0,'CreditAddsToBalance','SUTax','Daily', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REQUIRES_QUARTER_LAW) VALUES ('TaxFutureReceivable', 'TXFR', 'Tax Future Receivable', 'Money that we expect to get from the employer for Tax Payments.  Calculate how much we are going to request from the employer.',0,'DebitAddsToBalance', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REQUIRES_QUARTER_LAW) VALUES ('TaxFutureLiability', 'TXFL', 'Tax Future Liability','Money that we expect to owe to someone else, usually an agency, but that we do not owe yet because we have not collected funds from the employer.',0,'CreditAddsToBalance', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('TaxCurrentCash', 'TXCC', 'Tax Current Cash', 'Money we have requested from the employer and assume that we have in the Intuit bank account.',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('TaxCurrentLiability', 'TXCL', 'Tax Current Liability', 'Money we owe based upon received funds.',0,'CreditAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('ERPayable', 'ERPAY', 'ER Payable', 'Credit that is available to be used toward future payrolls or amounts owed Intuit (if cash is still on hand).',0,'CreditAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('AgencyTaxRefund', 'ATR', 'Agency Tax Refund', 'Account represents credit balances that will  be repaid by the Agency directly to the customer once we indicate on the reutrn to refund the overpayment amount.',0,'CreditAddsToBalance','Monthly', 1)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION, BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('ERLiabilityOffset', 'ERLO', 'ER Liability Offset', 'Funds applied toward future payrolls or transactions on behalf of the customer (cash is on hand)',0,'CreditAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION,BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('CollectionExpense', 'COLEX', 'Collection Expense', 'Money paid to a collection agency in order to recover bad debt.',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION,BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('ERSUITaxDue', 'ERSUI', 'Employer SUI Tax Due', 'Contains the amount owed to/by the customer due to adjustments made to the payment amounts during the quarter.',0,'CreditAddsToBalance','Monthly', 0)
/ 
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION,BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('TaxInterestExpense', 'COGSINT', 'Tax Interest Expense', 'Account is used to record interest payments to the agency and refunds to the customer for interest.',0,'DebitAddsToBalance','Monthly', 0)
/
INSERT INTO TEMP_PSP_LEDGER_ACCOUNT ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION,BALANCE_CALCULATION_RULE, REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW) VALUES ('TaxPenaltiesExpense', 'COGSPEN', 'Tax Penalties Expense', 'Account is used to record penalty payments and refunds to the customer.',0,'DebitAddsToBalance','Monthly', 0)
/
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_LEDGER_ACCOUNT
   ( LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION,BALANCE_CALCULATION_RULE,LEDGER_ACCOUNT_TYPE,REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW)
SELECT 
     LEDGER_ACCOUNT_CD, ACCOUNT_ABBREVIATION, NAME, DESCRIPTION, VERSION,BALANCE_CALCULATION_RULE,LEDGER_ACCOUNT_TYPE,REPORTING_FREQUENCY, REQUIRES_QUARTER_LAW
FROM 
   TEMP_PSP_LEDGER_ACCOUNT tt 
WHERE 
   tt.LEDGER_ACCOUNT_CD NOT IN (SELECT LEDGER_ACCOUNT_CD FROM PSP_LEDGER_ACCOUNT)

/

DELETE FROM PSP_POSTING_RULE WHERE LEDGER_ACCOUNT_FK NOT IN  (SELECT LEDGER_ACCOUNT_CD FROM TEMP_PSP_LEDGER_ACCOUNT)
/
DELETE FROM PSP_GEMS_LEDGER_POSTING_RULE WHERE LEDGER_ACCOUNT_FK NOT IN  (SELECT LEDGER_ACCOUNT_CD FROM TEMP_PSP_LEDGER_ACCOUNT)
/
DELETE FROM PSP_LEDGER_ACCOUNT
WHERE LEDGER_ACCOUNT_CD NOT IN (SELECT LEDGER_ACCOUNT_CD FROM TEMP_PSP_LEDGER_ACCOUNT) 
/

UPDATE PSP_LEDGER_ACCOUNT rt
SET ( rt.NAME, rt.DESCRIPTION, rt.VERSION,rt.BALANCE_CALCULATION_RULE,rt.LEDGER_ACCOUNT_TYPE,rt.REPORTING_FREQUENCY, rt.ACCOUNT_ABBREVIATION, rt.REQUIRES_QUARTER_LAW) =
(SELECT  tt.NAME, tt.DESCRIPTION, tt.VERSION,tt.BALANCE_CALCULATION_RULE,rt.LEDGER_ACCOUNT_TYPE,rt.REPORTING_FREQUENCY, tt.ACCOUNT_ABBREVIATION, tt.REQUIRES_QUARTER_LAW
 FROM TEMP_PSP_LEDGER_ACCOUNT tt WHERE tt.LEDGER_ACCOUNT_CD = rt.LEDGER_ACCOUNT_CD)
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_LEDGER_ACCOUNT

/
COMMIT
 

