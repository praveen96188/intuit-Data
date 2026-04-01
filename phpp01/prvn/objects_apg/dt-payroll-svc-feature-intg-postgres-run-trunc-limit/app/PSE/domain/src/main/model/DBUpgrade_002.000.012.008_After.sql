--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\rel-1.12\PSE\Domain\src\main\model\DBUpgrade_002.000.012.008.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_EMPLOYEE_TAX
 SET EXTRA_WITHHOLDING = 0
WHERE EXTRA_WITHHOLDING IS NULL;
 
UPDATE PSP_TAX_TABLE_MISC_DATA
 SET MISC_DATA_ORDER = -1
WHERE MISC_DATA_ORDER IS NULL;