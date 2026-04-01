--
-- This script will be executed BEFORE the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.004.004.sql
--
-- Developers can hand code logic here for data migration purposes
--


Prompt Purging all paystub tables;

-- DELETE FROM PSP_PSTUB_DDITEM;
-- DELETE FROM PSP_PSTUB_PAID_TIMEOFF_ITEM;
-- DELETE FROM PSP_PSTUB_MSG;
-- DELETE FROM PSP_PSTUB_PAY_ITEM;
-- DELETE FROM PSP_PAYSTUB;
-- DELETE FROM PSP_PSTUB_EMPLOYEE_PREFERENCE;
-- DELETE FROM PSP_PSTUB_EMPLOYEE_INFO;
-- DELETE FROM PSP_PSTUB_EMPLOYER_INFO;
-- DELETE FROM PSP_PSTUB_ADDRESS;

Prompt Finished Purging;
