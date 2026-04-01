-- You must have access to the DB Admin module to access the sync script.

Prompt Constraint C_PSP_SOURCE_PAYROLL_PARAM0;

ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 DROP CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM0;
ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER
 ADD CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM0
  CHECK (PARAMETER_CD IN('ACHWaitPeriod', 'CompanyBankAccountDurationLimitForVerification', 'CompanyBankAccountVerificationAttemptLimit', 'ConsecutiveLimitViolationLimit', 'DDCompanyLimitDuration', 'DDEmployeeLimitDuration', 'DefaultDDCompanyLimit', 'DefaultDDEmployeeLimit', 'MaxDDCompanyLimitDefault', 'MinimumNonSuspectPayrollAmount', 'PayrollEntryDescription', 'MinPayrollRunsForLimitAutoIncrease', 'MinQBVersionSupported', 'BookTransferEntryDescription', 'ReversalEntryDescription', 'MaxNumberOfFailedLoginAttempts', 'AllowMultipleFundingModels', 'MaxWarehouseTransactionDays', 'DefaultFundingModel', 'LockAccountDuration', 'ShouldAddCompanyToPSP', 'AllowReverifyBankAccount', 'MinimumEarliestPayrollRunDays', 'DeactiveBankAccountOnReturnedVerificationDebit', 'UnsupportedQBVersionList', 'ResolveEmployeeNOC', 'FraudEEPaidMax', 'FraudEEPaidMaxXPayrolls', 'FraudEERoundPaidXPayrolls', 'FraudPRMax', 'FraudPRMaxXPayrolls', 'FraudEEPercentIncreaseMax', 'FraudEEPercentIncreaseMaxXPayrolls', 'FraudPRPercentIncreaseMax', 'FraudPRPercentIncreaseMaxXPayrolls', 'AllowDuplicatePaycheckIdsIfStatusIsCancelled', 'QBVersionSunsetString'));

PROMPT finishedDBUpgradeFrom_1.9.9.47_To_1.9.9.48.sql
