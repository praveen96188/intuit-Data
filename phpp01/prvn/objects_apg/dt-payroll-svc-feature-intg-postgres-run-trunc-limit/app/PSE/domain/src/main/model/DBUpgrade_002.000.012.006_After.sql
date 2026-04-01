--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\rel-1.12\PSE\Domain\src\main\model\DBUpgrade_002.000.012.006.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_LIABILITY_CHECK
SET SYSTEM_MODIFIED_TOKEN = -1;