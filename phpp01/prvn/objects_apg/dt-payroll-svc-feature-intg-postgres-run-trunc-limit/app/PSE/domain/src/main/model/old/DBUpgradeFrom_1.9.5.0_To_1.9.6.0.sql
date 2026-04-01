--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
Prompt Constraint C_PSP_SYSTEM_CAPABILITY0;
ALTER TABLE PSP_SYSTEM_CAPABILITY DROP CONSTRAINT C_PSP_SYSTEM_CAPABILITY0;
ALTER TABLE PSP_SYSTEM_CAPABILITY ADD CONSTRAINT C_PSP_SYSTEM_CAPABILITY0 CHECK (SYSTEM_CAPABILITY_CD IN('ChangeEmployerBankAccount', 'DebitMonthlySubscriptionFee', 'FileCompanyTaxes', 'OffloadPendingPayrolls', 'PayCompanyTaxes', 'RefundOrCredit', 'SubmitPayroll', 'SynchronizeAccount', 'AddService', 'CancelService', 'ChangeCompanyInfo', 'ChangeEmployeeBankAccount', 'TransmitBalanceFile', 'VerifyCompanyBankAccount', 'UpgradeFundingModel', 'RecallPayroll'));

select 'finished DBUpgradeFrom_1.9.5.0_To_1.9.6.0.sql ' || to_char(sysdate, 'MM/DD/YYYY HH24:MI:SS') from dual