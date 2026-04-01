--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.1_To_1.9.9.2.sql
--
-- Developers can hand code logic here for data migration purposes
--


update psp_company set QB_INFO_AS400_PAYROLL_COUNT = 0;