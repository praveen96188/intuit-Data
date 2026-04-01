--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.011.004.sql
--
-- Developers can hand code logic here for data migration purposes
--
update psp_entity_change ec set ec.is_error = 0;
