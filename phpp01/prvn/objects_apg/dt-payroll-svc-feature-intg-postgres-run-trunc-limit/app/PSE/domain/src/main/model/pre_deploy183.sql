-- pre-deploy for 1.8.3

--add 'CancelCloud' to operations
alter table psp_auth_operation drop constraint C_psp_auth_operation0
/

alter table PSP_AUTH_OPERATION ADD (CONSTRAINT C_PSP_AUTH_OPERATION0 CHECK (OPERATION_ID IN('BankReturnUpdate', 'AccessApplication', 'DDLimitUpdate', 'FundingModelUpdate', 'DDStatusUpdate', 'StrikeAdd', 'StrikeCancel', 'DDStatusPendingActivation', 'DDStatusActive', 'DDStatusPendingTermination', 'DDStatusTerminated', 'DDStatusOnHold', 'DDStatusSuspended', 'DDStatusCancelled', 'SettingUpdate', 'ViewFullBankAccountNumbers', 'BankReturnView', 'RecordNonACHRedebitTransaction', 'CreateFeeTransaction', 'CreateReversalTransaction', 'DDTransactionCancel', 'TransactionCancel', 'LedgerView', 'SelectNonStandardSettlementType', 'CreateRefundTransaction', 'VoidTransaction', 'BookTransferTransaction', 'ActivateBankAccount', 'DeActivateBankAccount', 'WriteoffBadDebtTransaction', 'RecoverBadDebtTransaction', 'EscalationCreditTransaction', 'IssueRedebitTransaction', 'AuthAccessApplication', 'AuthAddUpdateUsers', 'AuthRemoveUsers', 'AuthManageRoles', 'AuthAddUpdateHelpDesk', 'AuthAddRemoveHelpDesk', 'AuthAddUpdateDataCustodian', 'AuthRemoveDataCustodian', 'EditCompanyLegalInformation', 'EditCompanyContactInformation', 'ViewTransactionHistory', 'ViewVerificationDebits', 'ResetVerificationAmounts', 'GenerateRandomDebits', 'EditPayrollContact', 'EditPrincipalContacts', 'GeneratePin', 'AddBankAccountRandomDebits', 'AddOffering', 'AddOffer', 'EditChartOfAccounts', 'AddBankAccountByPassRandomDebits', 'RemoveOffer', 'ViewPayrollScreen', 'EnterWireExpectedDate', 'AdjustRedebitTransaction', 'ViewSignupFraudQueue', 'RemoveFromSignupFraudHold', 'RemoveFromPayrollSubmitFraudHold', 'UploadToGems', 'ViewOFX', 'RequestSecondOffload', 'ViewChaseReport', 'PrintChaseReport', 'AgentInitiatesRefundRebill', 'ViewPayrollStatus', 'ViewPendingReversalsScreen', 'EditPayrollAdminName', 'SavePrintOFX', 'ConfirmOffload', 'RefundEmployerFraudEscalation', 'EditTokens', 'EditDebugLogging', 'Activations', 'AssignChecklist', 'ViewACHSummary', 'ViewAdjustments', 'CreateRAFFile', 'PostActivations', 'CreateACHFile', 'ViewOperatorTab', 'AddCheckDistributionService', 'WriteoffEmployeeBadDebtTransaction', 'RecordPrefundingWire', 'ViewOffloadStatus', 'AddVendorPaymentService', 'AddAS400Company', 'ViewCheckPrintSignature', 'AddUpdateCheckPrintSignature', 'ViewCheckPrintQueue', 'UpdateCheckPrintBatchStatus', 'TaxCreditsWOTC', 'ViewCloudOnlyData', 'CancelCloud')))
/

--add operation
INSERT INTO PSP_AUTH_OPERATION ( OPERATION_ID, VERSION, NAME, DESCRIPTION) VALUES (
'CancelCloud', 0, 'Cancel Cloud', 'Cancel Cloud service')
/

--add role associations
INSERT INTO PSP_AUTHROLE_OPERATION_ASSOC (AUTH_ROLE_FK, AUTH_OPERATION_FK, REALM_ID) VALUES ('64de8b0f-334d-a5e3-7e45-4d46dea1c3d9', 'CancelCloud', -1)
/
INSERT INTO PSP_AUTHROLE_OPERATION_ASSOC (AUTH_ROLE_FK, AUTH_OPERATION_FK, REALM_ID) VALUES ('83de8b3e-d35d-f4e1-a461-d565dea1c352', 'CancelCloud', -1)
/

--add additional role sub status
INSERT INTO PSP_ROLE_SUB_STATUS ( ROLE_SUB_STATUS_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, ALLOWED_CHANGE_TYPE,
SERVICE_SUB_STATUS_FK, AUTH_ROLE_FK)
VALUES ('2843acea-e190-35a1-a474-186273929887', 0, SYSDATE, SYSDATE, 'CanMoveFromSubStatus','ActiveCurrent',
'83de8b3e-d35d-f4e1-a461-d565dea1c352')
/
