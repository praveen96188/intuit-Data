--
-- This script will be executed AFTER the automatically generated
-- D:\Dev\PSP\dev-2012R7\PSE\Domain\src\main\model\DBUpgrade_002.001.001.008.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_ENTITLEMENT SET BILLING_DAY_OF_MONTH=1
/

COMMIT
/