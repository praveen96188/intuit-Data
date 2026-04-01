--
-- This script will be executed AFTER the automatically generated
-- D:\Dev\PSP\dev\PSE\Domain\src\main\model\DBUpgrade_002.001.001.015.sql
--
-- Developers can hand code logic here for data migration purposes
--

UPDATE PSP_PAYCHECK_USAGE
SET REASON_FOR_FREE_CHARGE = 'None';

COMMIT;
