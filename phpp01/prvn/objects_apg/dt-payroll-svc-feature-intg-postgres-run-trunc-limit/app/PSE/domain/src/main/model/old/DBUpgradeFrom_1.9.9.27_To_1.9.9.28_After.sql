--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.27_To_1.9.9.28.sql
--
-- Developers can hand code logic here for data migration purposes
--
ALTER TABLE PSP_AUTH_USER MODIFY VERSION DEFAULT -1
/