--
-- This script will be executed AFTER the automatically generated
-- C:\Dev\PSP\dev\PSE\Domain\src\main\model\DBUpgrade_002.000.013.005.sql
--
-- Developers can hand code logic here for data migration purposes
--
UPDATE PSP_SYSTEM_PARAMETER
    SET IS_SECURED = 0
 /
Commit
/
