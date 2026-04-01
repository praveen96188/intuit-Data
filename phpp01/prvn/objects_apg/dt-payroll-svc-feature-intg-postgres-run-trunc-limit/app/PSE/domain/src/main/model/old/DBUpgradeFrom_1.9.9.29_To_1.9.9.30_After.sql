--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.29_To_1.9.9.30.sql
--
-- Developers can hand code logic here for data migration purposes
--
update psp_offload_batch set IS_FEE_EVENT_CREATION_COMPLETE=1;
commit;