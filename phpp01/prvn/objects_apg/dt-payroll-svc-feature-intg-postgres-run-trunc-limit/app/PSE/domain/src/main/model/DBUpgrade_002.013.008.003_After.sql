--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.008.003.sql
--
-- Developers can hand code logic here for data migration purposes
--

Update PSP_BANK_ACCOUNT
Set A_C_H_ACCOUNT_TYPE_CD = ACCOUNT_TYPE_CD
Where A_C_H_ACCOUNT_TYPE_CD is null;
/
Commit;