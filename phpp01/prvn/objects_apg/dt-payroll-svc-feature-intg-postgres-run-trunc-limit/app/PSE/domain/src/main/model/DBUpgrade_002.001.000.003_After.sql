--
-- This script will be executed AFTER the automatically generated
-- D:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.001.000.003.sql
--
-- Developers can hand code logic here for data migration purposes
--

/*

-- INIT NUMBER COLUMN
UPDATE PSP_ENTITLEMENT_UNIT SET ERROR_COUNT = 0
/

-- INIT subscription end date
UPDATE PSP_ENTITLEMENT SET SUBSCRIPTION_END_DATE = NEXT_CHARGE_DATE WHERE NEXT_CHARGE_DATE IS NOT NULL
/

COMMIT
/

*/