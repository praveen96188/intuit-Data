--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Constraint C_PSP_FRAUD_VALUE0;
ALTER TABLE PSP_FRAUD_VALUE
 DROP CONSTRAINT C_PSP_FRAUD_VALUE0;

ALTER TABLE PSP_FRAUD_VALUE
 ADD CONSTRAINT C_PSP_FRAUD_VALUE0
  CHECK (NAME IN('FraudBPXPayrollAmount', 'FraudBPInactivityDays', 'FraudBPInactivityPayrollAmount', 'FraudBPMax', 'FraudBPMaxXPayrolls', 'FraudBPNumberOfDaysForXPayments', 'FraudBPNumberOfPaymentsInXDays', 'FraudDDInactivityDays', 'FraudDDInactivityPayrollAmount', 'FraudEENewEmployeeAddedXDays', 'FraudEENumberOfDaysBankAcctUpdated', 'FraudEENumberOfDaysMultiplePaychecks', 'FraudEENumberOfPaychecksSpikeInPay', 'FraudEEPaidMax', 'FraudPRMax', 'FraudPRMaxXPayrolls', 'FraudPRNumberOfDaysForXPayrolls', 'FraudPRNumberOfPayrollsInXDays', 'FraudPRNumberOfPayrollsToCheckSameBank', 'FraudPRPercentEmployeesPaidSameBank', 'FraudPRPercentIncreaseMax', 'FraudPRPercentIncreaseMaxXPayrolls', 'FraudEEPaidMaxXPayrolls', 'FraudEEPaidXTimes', 'FraudEEPercentGreaterThanAverage', 'FraudEEPercentGreaterThanOtherEEs', 'FraudEEPercentIncreaseMax', 'FraudEEPercentIncreaseMaxXPayrolls', 'FraudEERoundPaidXPayrolls', 'FraudPREmployeesSameBankAccountMax', 'FraudPRTotalEmployeesToCheckSameBank', 'FraudPayeeNumberOfDaysMultiplePayments', 'FraudPayeePaidMax', 'FraudPayeePaidMaxXPayrolls', 'FraudPayeePaidXTimes', 'FraudBPRoundPaidXPayrolls', 'FraudBPNumberOfPaymentsToCheckSameBank', 'FraudBPPercentPayeesPaidSameBank', 'FraudBPTotalPayeesToCheckSameBank', 'FraudEERoundPaidXAmount', 'FraudBPRoundPaidXAmount', 'FraudPRXPayrollAmount', 'FraudBPAcctUpdateMax', 'FraudBPAcctUpdateXDays', 'FraudEEAcctUpdateMax', 'FraudEEAcctUpdateXDays'));

PROMPT finished DBUpgrade_002.018.003.003.sql