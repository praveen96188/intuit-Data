--
-- This script will be executed AFTER the automatically generated
-- C:\dev\PSP\main\PSE\Domain\src\main\model\DBUpgradeFrom_1.9.9.23_To_1.9.9.24.sql
--
-- Developers can hand code logic here for data migration purposes
--
-- PSP_AUTH_USER no longer uses Hibernate optimistic concurrency
-- therefore, Hibernate does not send this value down on insert
ALTER TABLE PSP_AUTH_USER MODIFY VERSION DEFAULT -1
/