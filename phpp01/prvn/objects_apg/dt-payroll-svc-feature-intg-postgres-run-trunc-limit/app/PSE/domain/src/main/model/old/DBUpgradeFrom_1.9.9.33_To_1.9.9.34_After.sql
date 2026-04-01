--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.33_To_1.9.9.34.sql
--
-- Developers can hand code logic here for data migration purposes
--
-- previous default value was -1; SpcfEntity uses that as a marker for new entities when read in
ALTER TABLE PSP_AUTH_USER MODIFY VERSION DEFAULT 0
/
