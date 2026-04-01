--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\rel-1.12\PSE\Domain\src\main\model\DBUpgrade_002.000.012.005.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_PAYCHECK_SPLIT
SET PAY_STUB_ORDER = 0
WHERE PAY_STUB_ORDER IS NULL;

UPDATE PSP_QBDT_PAYCHECK_INFO
SET VACATION_HOURS_ACCRUED = 0, SICK_HOURS_ACCRUED = 0, VOID_TOKEN = -1
WHERE VACATION_HOURS_ACCRUED IS NULL 
AND SICK_HOURS_ACCRUED IS NULL
AND VOID_TOKEN IS NULL;