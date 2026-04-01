--
-- This script will be executed AFTER the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.005.002.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt Update system paramter;

update psp_system_parameter
set system_parameter_value = 500
where system_parameter_cd = 'NUM_EE_PER_OFX_ALERT_LEVEL';

commit;

Prompt Finished updating system paramter;