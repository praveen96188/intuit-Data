--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\PSP\rel-1.2\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.44_To_1.9.9.45.sql
--
-- Developers can hand code logic here for data migration purposes
--

ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER DROP  CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM0;


ALTER TABLE PSP_SOURCE_PAYROLL_PARAMETER ADD (
  CONSTRAINT C_PSP_SOURCE_PAYROLL_PARAM0
 CHECK (PARAMETER_CD IN('ACHWaitPeriod', 'CompanyBankAccountDurationLimitForVerification', 'CompanyBankAccountVerificationAttemptLimit', 'ConsecutiveLimitViolationLimit', 'DDCompanyLimitDuration', 'DDEmployeeLimitDuration', 'DefaultDDCompanyLimit', 'DefaultDDEmployeeLimit', 'MaxDDCompanyLimitDefault', 'MinimumNonSuspectPayrollAmount', 'PayrollEntryDescription', 'MinPayrollRunsForLimitAutoIncrease', 'MinQBVersionSupported', 'BookTransferEntryDescription', 'ReversalEntryDescription', 'MaxNumberOfFailedLoginAttempts', 'AllowMultipleFundingModels', 'MaxWarehouseTransactionDays', 'DefaultFundingModel', 'LockAccountDuration', 'ShouldAddCompanyToPSP', 'AllowReverifyBankAccount', 'MinimumEarliestPayrollRunDays', 'DeactiveBankAccountOnReturnedVerificationDebit', 'UnsupportedQBVersionList', 'ResolveEmployeeNOC', 'AllowDuplicatePaycheckIdsIfStatusIsCancelled', 'QBVersionSunsetString')));

