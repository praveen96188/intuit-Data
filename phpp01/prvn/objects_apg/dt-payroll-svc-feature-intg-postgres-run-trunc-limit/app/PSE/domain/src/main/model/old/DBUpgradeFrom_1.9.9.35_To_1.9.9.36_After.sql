--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.35_To_1.9.9.36.sql
--
-- Developers can hand code logic here for data migration purposes
--
PROMPT C_PSP_AUTH_OPERATION0

ALTER TABLE PSP_AUTH_OPERATION DROP CONSTRAINT C_PSP_AUTH_OPERATION0
/
ALTER TABLE PSP_AUTH_OPERATION ADD CONSTRAINT C_PSP_AUTH_OPERATION0 CHECK(OPERATION_ID IN('BankReturnUpdate', 'AccessApplication', 'DDLimitUpdate', 'FundingModelUpdate', 'DDStatusUpdate', 'StrikeAdd', 'StrikeCancel', 'DDStatusPendingActivation', 'DDStatusActive', 'DDStatusPendingTermination', 'DDStatusTerminated', 'DDStatusOnHold', 'DDStatusSuspended', 'DDStatusCancelled', 'SettingUpdate', 'ViewFullBankAccountNumbers', 'BankReturnView', 'RecordNonACHRedebitTransaction', 'CreateFeeTransaction', 'CreateReversalTransaction', 'DDTransactionCancel', 'TransactionCancel', 'LedgerView', 'SelectNonStandardSettlementType', 'CreateRefundTransaction', 'VoidTransaction', 'BookTransferTransaction', 'ActivateBankAccount', 'DeActivateBankAccount', 'WriteoffBadDebtTransaction', 'RecoverBadDebtTransaction', 'EscalationCreditTransaction', 'IssueRedebitTransaction', 'AuthAccessApplication', 'AuthAddUpdateUsers', 'AuthRemoveUsers', 'AuthManageRoles', 'AuthAddUpdateHelpDesk', 'AuthAddRemoveHelpDesk', 'AuthAddUpdateDataCustodian', 'AuthRemoveDataCustodian', 'EditCompanyLegalInformation', 'EditCompanyContactInformation', 'ViewTransactionHistory', 'ViewVerificationDebits', 'ResetVerificationAmounts', 'GenerateRandomDebits', 'EditPayrollContact', 'EditPrincipalContacts', 'GeneratePin', 'AddBankAccountRandomDebits', 'AddOffering', 'AddOffer', 'EditChartOfAccounts', 'AddBankAccountByPassRandomDebits', 'RemoveOffer', 'ViewPayrollScreen', 'EnterWireExpectedDate', 'AdjustRedebitTransaction', 'ViewSignupFraudQueue', 'RemoveFromSignupFraudHold', 'RemoveFromPayrollSubmitFraudHold', 'UploadToGems', 'ViewOFX', 'RequestSecondOffload', 'ViewChaseReport', 'PrintChaseReport', 'AgentInitiatesRefundRebill', 'ViewPayrollStatus', 'ViewPendingReversalsScreen', 'EditPayrollAdminName', 'SavePrintOFX', 'ConfirmOffload', 'RefundEmployerFraudEscalation', 'EditTokens')) 
/
DECLARE

	index_exists PLS_INTEGER;

BEGIN
	SELECT COUNT(*) INTO index_exists
	FROM "USER_INDEXES"
	WHERE INDEX_NAME = 'FN_STATE_EFF_DATE';

	IF index_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP INDEX FN_STATE_EFF_DATE';
	END IF;

END;

/
CREATE INDEX FN_STATE_EFF_DATE ON PSP_FINANCIAL_TRANS_STATE
(TRUNC(TRANSACTION_STATE_EFF_DATE))
/
