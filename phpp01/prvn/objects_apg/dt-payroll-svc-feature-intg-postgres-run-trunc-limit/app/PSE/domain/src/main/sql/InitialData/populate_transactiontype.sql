DECLARE
	
	table_exists PLS_INTEGER;

BEGIN


	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_TRANSACTION_TYPE';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_TRANSACTION_TYPE" CASCADE CONSTRAINTS';
	END IF;

END;

/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_TRANSACTION_TYPE
(
      "TRANSACTION_TYPE_CD" VARCHAR2(255 CHAR) NOT NULL, 
	  "VERSION"  NUMBER(19,0) NOT NULL, 
	  "DESCRIPTION" VARCHAR2(4000 CHAR),  
	  "NAME" VARCHAR2(4000 CHAR),  
	  "TRANSACTION_CATEGORY" VARCHAR2(255 CHAR),  
	  "ASSOCIATION_TYPE" VARCHAR2(255),
	  "N_A_C_H_A_BATCH_TYPE" VARCHAR2(255),
      "FEE_IND" NUMBER(1,0),
	  "TRANSACTION_TYPE_GROUP_CD" VARCHAR2(255 CHAR) NOT NULL,
	  "INCLUDE_IN_TXN_RESPONSE" NUMBER(1,0)
)

/

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------


INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerDoublePaymentRefundCredit', 'Employer Double Payment Refund Credit', 'Refund to employer due to receiving both employer redebit and employer wire, check or cash', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxDoublePaymentRefundCredit', 'Employer Tax Double Payment Refund Credit', 'Refund to employer due to receiving both employer redebit and employer wire, check or cash', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'Intuit5DayReturnTransfer', 'Intuit 5day Return Transfer', 'Transfer of money from Current Cash to ER Return Cash due to 5 day model return (will not redebit)', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'Intuit5DayFeeReturnTransfer', 'Intuit 5day Fee Return Transfer', 'Transfer of money from Fee account to ER Return Cash due to 5 day model return (will not redebit)', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'Intuit5DaySalesTaxReturnTransfer', 'Intuit 5day Sales Tax Return Transfer', 'Transfer of money from Fee account to ER Return Cash due to 5 day model return (will not redebit)', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'IntuitFeeTransfer', 'Intuit Fee Transfer', 'Transfer of fees from Intuit ER Return Cash to Intuit Fee account because of a fee deposited into the ER Return Account', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'IntuitEmployerVerificationReturnTransfer', 'Intuit ER Verification Return Transfer', 'Transfer of returned verification debits from Intuit Fee account to Intuit ER Return Cash to negate the effect of the transaction', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'IntuitTaxVoidTransfer', 'Intuit Tax Void Transfer', 'Transfer from the Tax bank account to the ER Returns account', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'ReissueTaxLiabilityTransfer', 'Intuit Tax Void Reissue Transfer', 'Transfer back from the ER Returns account to the Tax bank account', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployeeEscalationCredit', 'Employee Escalation Credit', 'Pay employee due to an Intuit error', 0, 'Employee', 'None', 0, 'Payroll', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerEscalationCredit', 'Employer Escalation Credit', 'Funds to Employer Due to an Intuit error', 0, 'Employer', 'None', 0, 'Payroll', 'EscalationOrFraud', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerDdDebit', 'ER DD Debit', 'Money Intuit will take from the employer via ACH', 0, 'Employer', 'Impound', 0, 'Payroll', 'Debit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployeeDdCredit', 'EE DD Credit', 'Money Intuit will pay to the employee via ACH', 0, 'Employee', 'None', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerDdRedebit', 'ER DD Redebit', 'Money Intuit will take from the employer via ACH as a result of a prior ACH failure', 0, 'Employer', 'Redebit', 0, 'RetryPayment', 'Redebit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployeeDdReversalDebit', 'EE DD Reversal Debit', 'Money returned to Intuit from an employee after Intuit transferred the money to that employee', 0, 'Employee', 'Reversal', 0, 'Reversal', 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerDdRefundCredit', 'ER DD Refund Credit', 'Money given back to the employer that Intuit is holding', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerDdRejectRefundCredit', 'ER DD Reject Refund Credit', 'Money given back to the employer as a result of an EE DD Credit return', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerDdReversalRefundCredit', 'ER DD Reversal Refund Credit', 'Money given back to the employer due to a successful EE DD Reversal Debit', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerDdReturnedRefundCredit', 'ER DD Returned Refund Credit', 'Second attempt for ER DD Refund Credit as a result of a prior failed ER refund', 0, 'Employer', 'Refund', 0, 'Payroll', 'Recredit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerVerificationDebit', 'ER Verification Debit', 'Small transaction to test the readiness of an account', 0, 'Employer', 'None', 0, 'Payroll', 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerVerificationCredit', 'ER Verification Credit', 'Money given back to the employer as a result of the completed ER Verification Debit', 0, 'Employer', 'None', 0, 'Payroll', 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerVerificationCreditReturnTransfer', 'ER Verification Credit Return Transfer', 'Return of the money given back to the employer as a result of the completed ER Verification Debit', 0, 'Employer', 'None', 0, 'Payroll', 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerWriteOff', 'ER Write Off', 'Intuit has given up on getting money from the customer and writes off the amount as bad debt and uncollected', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Writeoff', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerWriteOffTax', 'ER Write Off Tax', 'Intuit has given up on getting tax money from the customer and writes off the amount as bad debt and uncollected', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Writeoff', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerWriteOffFee', 'ER Write Off-Fee', 'Intuit has given up on getting fee money from the customer and writes off the amount as a reduction in revenue', 0, 'Intuit', 'None', 0, 'Payroll', 'Writeoff', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerWriteOffSalesAndUseTax', 'ER Write Off-Sales Tax', 'Intuit has given up on getting sales tax money from the customer and writes off the amount as a reduction in sales and use tax liability', 0, 'Intuit', 'None', 0, 'Payroll', 'Writeoff', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployeeReversalFailedWriteOff', 'Employee Reversal Failed Write Off', 'Intuit has given up on getting money from the customer via reversals and writes off the amount as bad debt and uncollected', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Writeoff', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'IntuitEmployeeReturnTransfer', 'Intuit EE Return Transfer', 'Transfer of money from Intuit employee return account to the Intuit employer account', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'BadDebtRecovery', 'Bad Debt Recovery', 'Recovery of previously written off funds via non-ACH', 0, 'Intuit', 'None', 0, 'Recovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'BadDebtRecoveryTax', 'Bad Debt Recovery Tax', 'Recovery of previously written off tax funds via non-ACH', 0, 'Intuit', 'None', 0, 'Recovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'BadDebtRecoveryFee', 'Bad Debt Recovery-Fee', 'Recovery of previously written off fees via non-ACH', 0, 'Intuit', 'None', 0, 'Recovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'BadDebtRecoverySalesAndUseTax', 'Bad Debt Recovery-Sales Tax', 'Recovery of previously written off sales and use tax via non-ACH', 0, 'Intuit', 'None', 0, 'Recovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'ThirdPartyCollectionExpense', 'Third Party Collection Expense', 'Expense Intuit pays to a collection agency to recover bad debt', 0, 'Intuit', 'None', 0, 'Recovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'BadDebtCustomerRecovery', 'Bad Debt Recovery/Customer', 'Recovery from a customer of previously written off funds via non-ACH', 0, 'Intuit', 'None', 0, 'CustomerRecovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'BadDebtCustomerRecoveryFee', 'Bad Debt Recovery-Fee/Customer', 'Recovery from a customer of previously written off fees via non-ACH', 0, 'Intuit', 'None', 0, 'CustomerRecovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'BadDebtCustomerRecoverySalesAndUseTax', 'Bad Debt Recovery-Sales Tax/Customer', 'Recovery from a customer of previously written off sales and use tax via non-ACH', 0, 'Intuit', 'None', 0, 'CustomerRecovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'BadDebtCustomerRecoveryTax', 'Bad Debt Recovery-Tax/Customer', 'Recovery from a customer of previously written off tax funds via non-ACH', 0, 'Intuit', 'None', 0, 'CustomerRecovery', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerFeeDebit', 'ER Fee Debit', 'Money Intuit will take from an employer as fee', 0, 'Employer', 'None', 1, 'Payroll', 'Debit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerFeeRedebit', 'ER Fee Redebit', 'Fees Intuit will take from the employer via ACH as a result of a prior fee ACH failure', 0, 'Employer', 'Redebit', 1, 'RetryPayment', 'Redebit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerFeeRefundCredit', 'ER Fee Refund Credit', 'Money Intuit gives back to an employer for services rendered', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'ERCourtesyRefundCredit', 'ER Courtesy Refund Credit', 'Money Intuit gives back to an employer as a courtesy fund', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'EmployerFeeReturnedRefundCredit', 'ER Fee Returned Refund Credit', 'Money Intuit gives back to an employer for return services rendered', 0, 'Employer', 'Refund', 0, 'Payroll', 'Recredit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'DdFraud', 'DD Fraud', 'Record a fraud transaction against any payroll that has not already been returned by the bank', 0, 'Employer', 'None', 0, 'Payroll', 'EscalationOrFraud', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'ServiceSalesAndUseTax', 'Service Sales and Use Tax', 'Service sales and use tax', 0, 'Employer', 'None', 0, 'Payroll', 'Debit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'ServiceSalesAndUseTaxRedebit', 'Service Sales and Use Tax Redebit', 'Sales and use tax Intuit will take from the employer via ACH as a result of a prior sales and use tax ACH failure', 0, 'Employer', 'Redebit', 0, 'RetryPayment', 'Redebit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'ServiceSalesAndUseTaxRefundCredit', 'Service Sales and Use Tax Refund Credit', 'Service sales and use tax refund credit', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'ServiceSalesAndUseTaxReturnedRefundCredit', 'Service Sales and Use Tax Returned Refund Credit', 'Record a refund of sales and use tax collected with a payroll, second attempt', 0, 'Employer', 'Refund', 0, 'Payroll', 'Recredit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'UntimelyReturnPreWriteOff', 'Untimely Return PRE Write-Off', 'Untimely return pre write off', 0, 'Intuit', 'None', 0, 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES ( 
'UntimelyReturnPostWriteOff', 'Untimely Return POST Write-Off', 'Untimely return post write off', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerFraudOrEscalationRefundCredit', 'Employer Fraud or Escalation Refund Credit', 'Record a fraud transaction against any payroll that has not already been returned by the bank', 0, 'Employer', 'Refund', 0, 'Payroll', 'EscalationOrFraud', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxFraudOrEscalationRefundCredit', 'Employer Tax Fraud or Escalation Refund Credit', 'Record a fraud transaction against any payroll that has not already been returned by the bank', 0, 'Employer', 'Refund', 0, 'Payroll', 'EscalationOrFraud', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxDebit', 'ER Tax Debit', 'Money Intuit will take from the employer via ACH for taxes', 0, 'Employer', 'Impound', 0, 'Payroll', 'Debit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxRedebit', 'ER Tax Redebit', 'Redebit to employer due to a returned ER Tax Debit', 0, 'Employer', 'Redebit', 0, 'RetryPayment', 'Redebit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxCredit', 'ER Tax Credit', 'Money Intuit will give back to the employer via ACH for taxes', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxCreditReturnedTransfer', 'ER Tax Credit Returned Transfer', 'Money Intuit will move from returns account back to tax account', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'ERPayableAppliedBalanceDue', 'ER Payable Applied - Balance Due', 'Money applied against a balance from what intuit owes to the company for collected taxes not paid to the agency', 0, 'Intuit', 'Refund', 0, 'Payroll', 'Redebit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxReturnedCredit', 'ER Tax Returned Credit', 'Recredit to employer due to a returned refund', 0, 'Employer', 'Refund', 0, 'Payroll', 'Recredit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyTaxDebit', 'Agency Tax Debit', 'Reduce the money Intuit will pay to the agency due to voided paychecks', 0, 'Agency', 'None', 0, null, 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyTaxCredit', 'Agency Tax Credit', 'Money Intuit will pay to the agency', 0, 'Agency', 'None', 0, 'TaxPayment', 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'ReissueAgencyTaxDebitOffset', 'Reissue Agency Tax Debit offset', 'Offset from reissued tax payments against the refund account', 0, 'Intuit', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyHPDEWarehousedTaxPayment', 'Agency HPDE Warehoused Tax Payment', 'Total money employer has paid to the agency - not associated with payroll', 0, 'Agency', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyHPDETaxPayment', 'Agency HPDE Tax Payment', 'Money employer paid to the agency', 0, 'Agency', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyHPDETaxRefund', 'Agency HPDE Tax Refund', 'Money an agency refunded back to the employer', 0, 'Agency', 'None', 0, null, 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyTaxRedebit', 'Agency Tax Redebit', 'Reduce the money Intuit will pay to the agency due to voided paychecks after an agency reject and resolution', 0, 'Agency', 'None', 0, null, 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyTaxRecredit', 'Agency Tax Recredit', 'Money Intuit will pay to the agency after an agency reject and resolution', 0, 'Agency', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxRefundCredit', 'ER Tax Refund Credit', 'Refund for tax money collected before the agency payment has been made', 0, 'Employer', 'Refund', 0, 'Payroll', 'Recredit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxReturnedRefundCredit', 'ER Tax Returned Refund Credit', 'Refund for the tax money collected before the agency payment has been made', 0, 'Employer', 'Refund', 0, 'Payroll', 'Recredit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyRefundTOR', 'Agency Refund TOR', 'Refund for the tax money given directly from the agency to the employer', 0, 'Agency', 'Refund', 0, null, 'Recredit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyCreditBalanceCarryForwardDebit', 'Agency Balance Carry Forward Debit', 'Amount available for future liabilities paid directly by the employer to the agency', 0, 'Agency', 'Refund', 0, 'Payroll', 'Debit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerCreditBalanceCarryForwardCredit', 'Employer Credit Carry Forward Credit', 'Credit balance available to the employer to use for future payments', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerInterestRefundCredit', 'Employer Interest Refund Credit', 'Refund for the interest money given directly from the employer to the agency', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerPenaltiesRefundCredit', 'Employer Penalties Refund Credit', 'Refund for the penalties money given directly from the employer to the agency', 0, 'Employer', 'Refund', 0, 'Payroll', 'Credit', 1)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxDirectDebit', 'ER Direct Tax Debit', 'Money IRS will take directly from the employer via EFTPS for taxes', 0, 'Employer', 'Impound', 0, 'Payroll', 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyDirectCredit', 'Agency Direct Credit', 'Money that will be directly credited from the employer to IRS via EFTPS for taxes', 0, 'Agency', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyDirectDebit', 'Agency Direct Debit', 'Money that will directly reduce the employer debit/payment for 100K EFTPS payments', 0, 'Agency', 'None', 0, null, 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyDirectOverpayment', 'Agency Direct Overpayment', 'Money that will be added to the agency tax refund account for 100K payrolls', 0, 'Agency', 'None', 0, null, 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxCreditApplied', 'Employer Tax Credit Applied', 'Money Intuit will credit back to the employer from voided payrolls for unpaid liabilities', 0, 'Employer', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxOverpaymentApplied', 'Employer Tax Overpayment Applied', 'Money Intuit will credit back to the employer from voided payrolls for paid liabilities', 0, 'Employer', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerTaxDirectOverpaymentApplied', 'Employer Tax Direct Overpayment Applied', 'Money Intuit will credit back to the employer from voided payrolls for paid liabilities', 0, 'Employer', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyTaxOverpayment', 'Agency Tax Overpayment', 'Tax money that was overpaid to an agency', 0, 'Agency', 'None', 0, null, 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyTaxOverpaymentApplied', 'Agency Tax Overpayment Applied', 'Tax money that was overpaid to an agency has been applied', 0, 'Agency', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyHPDEPriorPaymentApplied', 'Agency HPDE Prior Payment Applied', 'Tax money that was paid to an agency has been applied', 0, 'Agency', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyPostBALFHPDETaxPayment', 'Agency Post BALF HPDE Payment', 'Tax money received after balance file that was paid to an agency', 0, 'Agency', 'None', 0, null, 'Credit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'AgencyPostBALFHPDETaxRefund', 'Agency Post BALF HPDE Refund', 'Tax refund received after balance file that was paid to an agency', 0, 'Agency', 'None', 0, null, 'Debit', 0)
/

INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerInterestRefundDebit', 'Employer Interest Refund Debit', 'Money Intuit will take from the employer which is refunded for the interest money', 0, 'Employer', 'None', 0, 'Payroll', 'Debit', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerPenaltiesRefundDebit', 'Employer Penalties Refund Debit', 'Money Intuit will take from the employer which is refunded for the penalties money', 0, 'Employer', 'None', 0, 'Payroll', 'Debit', 0)
/

-- FLA transactions
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdTXCLcTXCC', 'FLA dTXCL cTXCC', 'Financial Ledger Adjustment: debit Tax Current Liability / credit Tax Current Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdTXCLcERLO', 'FLA dTXCL cERLO', 'Financial Ledger Adjustment: debit Tax Current Liability / credit ER Liability Offset', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdERRRcERRC', 'FLA dERRR cERRC', 'Financial Ledger Adjustment: debit ER Return Receivable / credit ER Return Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdTXCCcTXCL', 'FLA dTXCC cTXCL', 'Financial Ledger Adjustment: debit Tax Current Cash / credit Tax Current Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdATRcERPAY', 'FLA dATR cERPAY', 'Financial Ledger Adjustment: debit Agency Tax Refund / credit ER Payable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdDDCLcDDCC', 'FLA dDDCL cDDCC', 'Financial Ledger Adjustment: debit DD Current Liability / credit DD Current Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdDDCCcDDCL', 'FLA dDDCC cDDCL', 'Financial Ledger Adjustment: debit DD Current Cash / credit DD Current Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdTXCLcATR', 'FLA dTXCL cATR', 'Financial Ledger Adjustment: debit Tax Current Liability / credit Agency Tax Refund', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdEERLcEERC', 'FLA dEERL cEERC', 'Financial Ledger Adjustment: debit EE Return Liability / credit EE Return Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdTXCLcERPAY', 'FLA dTXCL cERPAY', 'Financial Ledger Adjustment: debit Tax Current Liability / credit ER Payable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdTXCCcERPAY', 'FLA dTXCC cERPAY', 'Financial Ledger Adjustment: debit Tax Current Cash / credit ER Payable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdERLOcTXCL', 'FLA dERLO cTXCL', 'Financial Ledger Adjustment: debit ER Liability Offset / credit Tax Current Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdERLOcERSUI', 'FLA dERLO cERSUI', 'Financial Ledger Adjustment: debit ER Liability Offset / credit ER SUI', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLAdTXCCcERLO', 'FLA dTXCC cERLO', 'Financial Ledger Adjustment: debit Tax Current Cash / credit ER Liability Offset', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/

INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERLOcERPAY', 'FLA dERLO cERPAY', 'Financial Ledger Adjustment: debit ER Liability Offset / credit ER Payable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERLOcTXCC', 'FLA dERLO cTXCC', 'Financial Ledger Adjustment: debit ER Liability Offset / credit Tax Current Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERPAYcERLO', 'FLA dERPAY cERLO', 'Financial Ledger Adjustment: debit ER Payable / credit ER Liability Offset', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERPAYcTXCL', 'FLA dERPAY cTXCL', 'Financial Ledger Adjustment: debit ER Payable / credit Tax Current Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdFCRcFI', 'FLA dFCR cFI', 'Financial Ledger Adjustment: debit Fee Cash Revenue / credit Fee Income', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/

--FLAdERRCcERRR
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERRCcERRR', 'FLA dERRC cERRR', 'Financial Ledger Adjustment: debit ER Return Cash / credit ER Return Receivable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdEERCcERRR
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdEERCcERRR', 'FLA dEERC cERRR', 'Financial Ledger Adjustment: debit EE Return Cash / credit ER Return Receivable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERPAYcTXCC
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERPAYcTXCC', 'FLA dERPAY cTXCC', 'Financial Ledger Adjustment: debit ER Payable / credit Tax Current Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdATRcTXCL
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdATRcTXCL', 'FLA dATR cTXCL', 'Financial Ledger Adjustment: debit Agency Tax Refund / credit Tax Current Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdFCBcCOGSINT
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdFCBcCOGSINT', 'FLA dFCB cCOGSINT', 'Financial Ledger Adjustment: debit Fee Cash Balance Sheet / credit Tax Interest Expense', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdTXCCcERSUI
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdTXCCcERSUI', 'FLA dTXCC cERSUI', 'Financial Ledger Adjustment: debit Tax Current Cash / credit Employer SUI Tax Due', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERSUIcTXCC
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERSUIcTXCC', 'FLA dERSUI cTXCC', 'Financial Ledger Adjustment: debit Employer SUI Tax Due / credit Tax Current Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERSUIcTXCL
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERSUIcTXCL', 'FLA dERSUI cTXCL', 'Financial Ledger Adjustment: debit Employer SUI Tax Due / credit Tax Current Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERSUIcATR
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERSUIcATR', 'FLA dERSUI cATR', 'Financial Ledger Adjustment: debit Employer SUI Tax Due / credit Agency Tax Refund', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERSUIcERLO
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERSUIcERLO', 'FLA dERSUI cERLO', 'Financial Ledger Adjustment: debit Employer SUI Tax Due / credit ER Liability Offset', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERSUIcERPAY
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERSUIcERPAY', 'FLA dERSUI cERPAY', 'Financial Ledger Adjustment: debit Employer SUI Tax Due / credit ER Payable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdTXCLcERSUI
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdTXCLcERSUI', 'FLA dTXCL cERSUI', 'Financial Ledger Adjustment: debit Tax Current Liability / credit Employer SUI Tax Due', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdATRcERSUI
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdATRcERSUI', 'FLA dATR cERSUI', 'Financial Ledger Adjustment: debit Agency Tax Refund / credit Employer SUI Tax Due', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERPAYcERSUI
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERPAYcERSUI', 'FLA dERPAY cERSUI', 'Financial Ledger Adjustment: debit ER Payable / credit Employer SUI Tax Due', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcERRR
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcERRR', 'FLA dBD cERRR', 'Financial Ledger Adjustment: debit Bad Debt / credit ER Return Receivable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcERRC
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcERRC', 'FLA dBD cERRC', 'Financial Ledger Adjustment: debit Bad Debt / credit ER Return Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcEERL
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcEERL', 'FLA dBD cEERL', 'Financial Ledger Adjustment: debit Bad Debt / credit EE Return Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcTXCC
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcTXCC', 'FLA dBD cTXCC', 'Financial Ledger Adjustment: debit Bad Debt / credit Tax Current Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcTXCL
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcTXCL', 'FLA dBD cTXCL', 'Financial Ledger Adjustment: debit Bad Debt / credit Tax Current Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcERPAY
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcERPAY', 'FLA dBD cERPAY', 'Financial Ledger Adjustment: debit Bad Debt / credit ER Payable', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcATR
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcATR', 'FLA dBD cATR', 'Financial Ledger Adjustment: debit Bad Debt / credit Agency Tax Refund', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcERLO
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcERLO', 'FLA dBD cERLO', 'Financial Ledger Adjustment: debit Bad Debt / credit ER Liability Offset', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdBDcERSUI
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdBDcERSUI', 'FLA dBD cERSUI', 'Financial Ledger Adjustment: debit Bad Debt / credit Employer SUI Tax Due', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERRRcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERRRcBD', 'FLA dERRR cBD', 'Financial Ledger Adjustment: debit ER Return Receivable / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERRCcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERRCcBD', 'FLA dERRC cBD', 'Financial Ledger Adjustment: debit ER Return Cash / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdEERLcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdEERLcBD', 'FLA dEERL cBD', 'Financial Ledger Adjustment: debit ER Return Liability / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdTXCCcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdTXCCcBD', 'FLA dTXCC cBD', 'Financial Ledger Adjustment: debit Tax Current Cash / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdTXCLcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdTXCLcBD', 'FLA dTXCL cBD', 'Financial Ledger Adjustment: debit Tax Current Liability / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERPAYcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERPAYcBD', 'FLA dERPAY cBD', 'Financial Ledger Adjustment: debit ER Payable / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdATRcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdATRcBD', 'FLA dATR cBD', 'Financial Ledger Adjustment: debit Agency Tax Refund / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERLOcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERLOcBD', 'FLAdERLOcBD', 'Financial Ledger Adjustment: debit ER Liability Offset / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdERSUIcBD
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdERSUIcBD', 'FLA dERSUI cBD', 'Financial Ledger Adjustment: debit Employer SUI Tax Due / credit Bad Debt', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdATRcTXCC
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdATRcTXCC', 'FLA dATR cTXCC', 'Financial Ledger Adjustment: debit Agency Tax Refund / credit Tax Current Cash', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdTXCCcATR
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdTXCCcATR', 'FLA dTXCC cATR', 'Financial Ledger Adjustment: debit Tax Current Cash / credit Agency Tax Refund', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
--FLAdEERCcEERL
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
  'FLAdEERCcEERL', 'FLA dEERC cEERL', 'Financial Ledger Adjustment: debit EE Return Cash / credit EE Return Liability', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/

-- Temp FLA transactions
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLATemp1', 'FLATemp1', 'Reserved for a FLA transaction type that has not been created yet', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLATemp2', 'FLATemp2', 'Reserved for a FLA transaction type that has not been created yet', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLATemp3', 'FLATemp3', 'Reserved for a FLA transaction type that has not been created yet', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLATemp4', 'FLATemp4', 'Reserved for a FLA transaction type that has not been created yet', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'FLATemp5', 'FLATemp5', 'Reserved for a FLA transaction type that has not been created yet', 0, 'Intuit', 'FinancialLedgerAdjustment', 0, null, 'FinancialLedgerAdjustment', 0)
/

-- SUI Tax Payment Transactions (Variance Account)
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerSUITaxReceivable', 'Employer SUI Tax Receivable', 'Transaction created with Agency Tax Credit when the customer will not be debited immediately', 0, 'Intuit', 'None', 0, null, 'SUIPayments', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerSUITaxCollection', 'Employer SUI Tax Collection', 'Transaction created when the balance of the ER SUI Tax Due account is debited from the customer, if it is a debit balance', 0, 'Intuit', 'None', 0, null, 'SUIPayments', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerSUITaxPayable', 'Employer SUI Tax Payable', 'Transaction created with Agency Tax Debit when the customer will not be refunded immediately', 0, 'Intuit', 'None', 0, null, 'SUIPayments', 0)
/
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'EmployerSUITaxRefund', 'Employer SUI Tax Refund', 'Transaction created when the balance of the ER SUI Tax Due account is refunded to the customer, if it is a credit balance', 0, 'Intuit', 'None', 0, null, 'SUIPayments', 0)
/

--Book Transfer transaction type
INSERT INTO TEMP_PSP_TRANSACTION_TYPE ( TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)  VALUES (
'GlobalBookTransfer', 'Global Book Transfer', 'Transaction created to transfer from one Intuit account to other Intuit account', 0, 'Intuit', 'None', 0, 'BookTransfer', 'Other', 0)
/

--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_TRANSACTION_TYPE
   (TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE , FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE)
SELECT 
    TRANSACTION_TYPE_CD, NAME, DESCRIPTION,VERSION, TRANSACTION_CATEGORY, ASSOCIATION_TYPE, FEE_IND, N_A_C_H_A_BATCH_TYPE, TRANSACTION_TYPE_GROUP_CD, INCLUDE_IN_TXN_RESPONSE
FROM 
   TEMP_PSP_TRANSACTION_TYPE tt 
WHERE 
   tt.TRANSACTION_TYPE_CD NOT IN (SELECT TRANSACTION_TYPE_CD FROM PSP_TRANSACTION_TYPE)

/
DELETE FROM PSP_INTUIT_BANK_ACC_TXN_TYPE
/
DELETE FROM PSP_POSTING_RULE
/
DELETE FROM PSP_TRANSACTION_TYPE
WHERE TRANSACTION_TYPE_CD NOT IN (SELECT TRANSACTION_TYPE_CD FROM TEMP_PSP_TRANSACTION_TYPE) 

/

UPDATE PSP_TRANSACTION_TYPE rt
SET ( rt.NAME, rt.DESCRIPTION, rt.VERSION, rt.TRANSACTION_CATEGORY, rt.ASSOCIATION_TYPE, rt.FEE_IND, rt.N_A_C_H_A_BATCH_TYPE, rt.TRANSACTION_TYPE_GROUP_CD, rt.INCLUDE_IN_TXN_RESPONSE) =
(SELECT   tt.NAME, tt.DESCRIPTION, tt.VERSION, tt.TRANSACTION_CATEGORY, tt.ASSOCIATION_TYPE, tt.FEE_IND, tt.N_A_C_H_A_BATCH_TYPE, tt.TRANSACTION_TYPE_GROUP_CD, tt.INCLUDE_IN_TXN_RESPONSE
 FROM TEMP_PSP_TRANSACTION_TYPE tt WHERE tt.TRANSACTION_TYPE_CD = rt.TRANSACTION_TYPE_CD)
 WHERE rt.TRANSACTION_TYPE_CD not like 'FLATemp%'
/



--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_TRANSACTION_TYPE

/
COMMIT
 
