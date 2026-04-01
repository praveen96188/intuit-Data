--
-- This script will be executed AFTER the automatically generated
-- D:\dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.004.006.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_SYSTEM_PARAMETER
SET SYSTEM_PARAMETER_VALUE = '1100520,1100521,1101310,1101313'
WHERE SYSTEM_PARAMETER_CD = 'USAGE_BILLING_ASSET_ITEM_NUMBERS';

COMMIT;