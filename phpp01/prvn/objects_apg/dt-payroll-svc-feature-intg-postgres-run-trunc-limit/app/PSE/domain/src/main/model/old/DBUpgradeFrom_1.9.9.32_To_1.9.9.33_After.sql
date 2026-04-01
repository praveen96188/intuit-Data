--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.32_To_1.9.9.33.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt Constraint  C_PSP_AUTH_OPERATION0;
 

ALTER TABLE PSP_AUTH_OPERATION DROP
  CONSTRAINT C_PSP_AUTH_OPERATION0 
/

ALTER TABLE PSP_AUTH_OPERATION ADD (
  CONSTRAINT C_PSP_AUTH_OPERATION0
 CHECK (OPERATION_ID IN('BankReturnUpdate', 'AccessApplication', 'DDLimitUpdate', 'FundingModelUpdate', 'DDStatusUpdate', 'StrikeAdd', 'StrikeCancel', 'DDStatusPendingActivation', 'DDStatusActive', 'DDStatusPendingTermination', 'DDStatusTerminated', 'DDStatusOnHold', 'DDStatusSuspended', 'DDStatusCancelled', 'SettingUpdate', 'ViewFullBankAccountNumbers', 'BankReturnView', 'RecordNonACHRedebitTransaction', 'CreateFeeTransaction', 'CreateReversalTransaction', 'DDTransactionCancel', 'TransactionCancel', 'LedgerView', 'SelectNonStandardSettlementType', 'CreateRefundTransaction', 'VoidTransaction', 'BookTransferTransaction', 'ActivateBankAccount', 'DeActivateBankAccount', 'WriteoffBadDebtTransaction', 'RecoverBadDebtTransaction', 'EscalationCreditTransaction', 'IssueRedebitTransaction', 'AuthAccessApplication', 'AuthAddUpdateUsers', 'AuthRemoveUsers', 'AuthManageRoles', 'AuthAddUpdateHelpDesk', 'AuthAddRemoveHelpDesk', 'AuthAddUpdateDataCustodian', 'AuthRemoveDataCustodian', 'EditCompanyLegalInformation', 'EditCompanyContactInformation', 'ViewTransactionHistory', 'ViewVerificationDebits', 'ResetVerificationAmounts', 'GenerateRandomDebits', 'EditPayrollContact', 'EditPrincipalContacts', 'GeneratePin', 'AddBankAccountRandomDebits', 'AddOffering', 'AddOffer', 'EditChartOfAccounts', 'AddBankAccountByPassRandomDebits', 'RemoveOffer', 'ViewPayrollScreen', 'EnterWireExpectedDate', 'AdjustRedebitTransaction', 'ViewSignupFraudQueue', 'RemoveFromSignupFraudHold', 'RemoveFromPayrollSubmitFraudHold', 'UploadToGems', 'ViewOFX', 'RequestSecondOffload', 'ViewChaseReport', 'PrintChaseReport', 'AgentInitiatesRefundRebill', 'ViewPayrollStatus', 'ViewPendingReversalsScreen', 'EditPayrollAdminName', 'SavePrintOFX', 'ConfirmOffload', 'RefundEmployerFraudEscalation')))
/
