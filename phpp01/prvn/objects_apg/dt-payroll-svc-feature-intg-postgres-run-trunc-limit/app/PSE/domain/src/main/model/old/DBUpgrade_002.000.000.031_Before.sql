--
-- This script will be executed BEFORE the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgrade_002.000.000.031.sql
--
-- Developers can hand code logic here for data migration purposes
--
DELETE FROM PSP_PAYROLL_RUN_ACTION WHERE ACTION_EVENT_FK IN ('ERChangeRedebitToWireExpected', 'ERChangeWireExpectedToRedebit')
/
DELETE FROM PSP_ACTION_EVENT WHERE CODE='ERChangeRedebitToWireExpected'
/
DELETE FROM PSP_ACTION_EVENT WHERE CODE='ERChangeWireExpectedToRedebit'
/
COMMIT
/