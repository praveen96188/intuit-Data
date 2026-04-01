--
-- This script will be executed AFTER the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.004.011.sql
--
-- Developers can hand code logic here for data migration purposes
--
Prompt Update system paramter;

update psp_system_parameter
set system_parameter_value = '1100520,1100521,1101310,1101313,1101349,1101348,1101351'
where system_parameter_cd = 'USAGE_BILLING_ASSET_ITEM_NUMBERS';

commit;

Prompt Finished updating system paramter;