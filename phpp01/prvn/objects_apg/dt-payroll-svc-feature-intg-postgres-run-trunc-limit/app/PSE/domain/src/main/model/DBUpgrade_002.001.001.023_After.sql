--
-- This script will be executed AFTER the automatically generated
-- D:\Dev\PSP\dev\PSE\Domain\src\main\model\DBUpgrade_002.001.001.023.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_OFFERING_SVCCHG_GRP
SET NAME='Bank Verification Credit', DESCRIPTION='Bank Verification Credit', APPLIES_TO='BankVerificationCredit', OFFERING_FK='c63a047c-bb74-4941-b8f5-dbb433c66984'
WHERE OFFERING_SVCCHG_GRP_SEQ = '7f93f037-7da0-44dd-b5f9-f780deb4c711'
/

UPDATE PSP_OFFERING_SVCCHG_GRP
SET NAME='Bank Verification Debit', DESCRIPTION='Bank Verification Debit', APPLIES_TO='BankVerificationDebit', OFFERING_FK='c63a047c-bb74-4941-b8f5-dbb433c66984'
WHERE OFFERING_SVCCHG_GRP_SEQ = 'a9c0b535-68d7-4408-92b4-be79eee11cd6'
/

UPDATE PSP_OFFERING_SVCCHG_GRP
SET NAME='Payroll Adjustment', DESCRIPTION='Payroll Adjustment', APPLIES_TO='PayrollAdjustment', OFFERING_FK='5e6dac2d-f7ca-4565-9700-d6455a69ceda'
WHERE OFFERING_SVCCHG_GRP_SEQ = '3eb49b85-b44d-4671-944e-30b96e678f0b'
/

UPDATE PSP_OFFERING_SVCCHG_GRP
SET NAME='Direct Deposit Reversals', DESCRIPTION='Direct Deposit Reversals', APPLIES_TO='ReversalFee', OFFERING_FK='5e6dac2d-f7ca-4565-9700-d6455a69ceda'
WHERE OFFERING_SVCCHG_GRP_SEQ = '642c135e-850c-4d17-8543-5aca64d73857'
/

UPDATE PSP_OFFERING_SVCCHG_GRP
SET NAME='Bank Verification Credit', DESCRIPTION='Bank Verification Credit', APPLIES_TO='BankVerificationCredit', OFFERING_FK='5e6dac2d-f7ca-4565-9700-d6455a69ceda'
WHERE OFFERING_SVCCHG_GRP_SEQ = '686484ae-a160-4ee5-acb5-311254e0b741'
/

UPDATE PSP_OFFERING_SVCCHG_GRP
SET NAME='Bank Verification Debit', DESCRIPTION='Bank Verification Debit', APPLIES_TO='BankVerificationDebit', OFFERING_FK='5e6dac2d-f7ca-4565-9700-d6455a69ceda'
WHERE OFFERING_SVCCHG_GRP_SEQ = 'b88064e0-6a7e-4c27-a263-734b3705b905'
/

COMMIT
/