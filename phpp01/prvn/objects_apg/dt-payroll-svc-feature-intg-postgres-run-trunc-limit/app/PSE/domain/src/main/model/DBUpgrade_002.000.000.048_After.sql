--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.000.000.048.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE PSP_BPCOMPANY_SERVICE_INFO
SET OVERRIDE_PAYEE_LIMIT_AMOUNT = 0,
CONS_LIMIT_VIOLATION_CNT = 0
