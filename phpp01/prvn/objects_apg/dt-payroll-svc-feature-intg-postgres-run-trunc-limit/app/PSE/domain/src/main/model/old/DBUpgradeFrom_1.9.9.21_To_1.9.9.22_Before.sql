--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.21_To_1.9.9.22.sql
--
-- Developers can hand code logic here for data migration purposes
--

ALTER TABLE PSP_TRANSACTION_TYPE DROP CONSTRAINT C_PSP_TRANSACTION_TYPE0;
ALTER TABLE PSP_TRANSACTION_TYPE ADD CONSTRAINT C_PSP_TRANSACTION_TYPE0 CHECK(TRANSACTION_TYPE_CD IN('BadDebtRecovery', 'EmployeeDdCredit', 'EmployeeDdReversalDebit', 'EmployeeEscalationCredit', 'EmployerDdDebit', 'EmployerDdRedebit', 'EmployerDdRefundCredit', 'EmployerDdRejectRefundCredit', 'EmployerDdReturnedRefundCredit', 'EmployerDdReversalRefundCredit', 'EmployerDoublePaymentRefundCredit', 'EmployerEscalationCredit', 'EmployerFeeDebit', 'EmployerFeeRedebit', 'EmployerFeeRefundCredit', 'DdFraud', 'EmployerWriteOff', 'Intuit5DayReturnTransfer', 'IntuitEmployeeReturnTransfer', 'IntuitFeeTransfer', 'EmployerFeeReturnedRefundCredit', 'IntuitEmployerVerificationReturnTransfer', 'EmployerVerificationDebit', 'ServiceSalesAndUseTax', 'ServiceSalesAndUseTaxRefundCredit', 'UntimelyReturnPostWriteOff', 'UntimelyReturnPreWriteOff', 'ServiceSalesAndUseTaxRedebit', 'ServiceSalesAndUseTaxReturnedRefundCredit', 'BadDebtRecoveryFee', 'BadDebtRecoverySalesAndUseTax', 'EmployerWriteOffFee', 'EmployerWriteOffSalesAndUseTax', 'EmployerFraudOrEscalationRefundCredit'));
