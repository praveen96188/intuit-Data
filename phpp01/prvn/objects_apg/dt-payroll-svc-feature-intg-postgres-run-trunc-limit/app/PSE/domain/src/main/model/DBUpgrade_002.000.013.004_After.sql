--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\dev\PSE\Domain\src\main\model\DBUpgrade_002.000.013.004.sql
--
-- Developers can hand code logic here for data migration purposes
--

update PSP_AGENCY_CHECK_BATCH
set SUPER_CHECK = 0;

Commit;