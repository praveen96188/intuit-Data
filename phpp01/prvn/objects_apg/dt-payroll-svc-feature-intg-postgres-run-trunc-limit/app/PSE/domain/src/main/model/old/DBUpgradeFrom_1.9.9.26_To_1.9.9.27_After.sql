--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.26_To_1.9.9.27.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE PSP_COMPANY SET NBR_FAILED_AUTHENTICATIONS = 0 WHERE NBR_FAILED_AUTHENTICATIONS IS NULL
/

