--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.000.000.050.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE PSP_COMPANY SET CLOUD_CURRENT_TOKEN = 0;
COMMIT;