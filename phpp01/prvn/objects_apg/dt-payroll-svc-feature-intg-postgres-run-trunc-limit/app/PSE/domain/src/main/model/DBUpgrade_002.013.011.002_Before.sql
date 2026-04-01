--
-- This script will be executed BEFORE the automatically generated
-- D:\Dev\psp\dev\PSE\Domain\src\main\model\DBUpgrade_002.013.011.002.sql
--
-- Developers can hand code logic here for data migration purposes
--

Prompt drop Index PSP_PAYROLL_RUN_FK1;
 DROP INDEX PSP_PAYROLL_RUN_FK1;

Prompt drop Constraint PSP_PAYROLL_RUN_FK1;
ALTER TABLE PSP_PAYROLL_RUN
 DROP CONSTRAINT PSP_PAYROLL_RUN_FK1;
 
PROMPT finished DBUpgrade_002.013.011.002_Before.sql