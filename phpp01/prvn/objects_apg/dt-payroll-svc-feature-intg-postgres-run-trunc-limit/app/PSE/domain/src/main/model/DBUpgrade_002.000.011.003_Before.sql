--
-- This script will be executed BEFORE the automatically generated
-- C:\Dev\PSP\rel-1.11\PSE\Domain\src\main\model\DBUpgrade_002.000.011.003.sql
--
-- Developers can hand code logic here for data migration purposes
--

DELETE FROM PSP_AGENCY_ID_REQUIREMENT;

DELETE FROM PSP_MANUAL_REQUIREMENT;

DELETE FROM PSP_SYSTEM_REQUIREMENT;

DELETE FROM PSP_PAYMENT_METHOD_REQUIREMENT;

COMMIT;

PROMPT finishedDBUpgrade_002.000.011.003_Before.sql