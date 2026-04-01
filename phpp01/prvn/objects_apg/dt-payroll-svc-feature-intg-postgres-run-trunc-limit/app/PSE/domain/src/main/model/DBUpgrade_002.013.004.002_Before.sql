--
-- This script will be executed BEFORE the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.004.002.sql
--
-- Developers can hand code logic here for data migration purposes
--

Prompt Purging all paystub tables;

TRUNCATE TABLE  PSP_PSTUB_DDITEM;
TRUNCATE TABLE  PSP_PSTUB_PAID_TIMEOFF_ITEM;
TRUNCATE TABLE  PSP_PSTUB_MSG;
TRUNCATE TABLE PSP_PSTUB_PAY_ITEM;
TRUNCATE TABLE  PSP_PAYSTUB;
TRUNCATE TABLE  PSP_PSTUB_EMPLOYEE_PREFERENCE;
TRUNCATE TABLE  PSP_PSTUB_EMPLOYEE_INFO;
TRUNCATE TABLE  PSP_PSTUB_EMPLOYER_INFO;
TRUNCATE TABLE  PSP_PSTUB_ADDRESS;

Prompt Finished Purging;



