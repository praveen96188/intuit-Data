--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\git\Payroll-Services\PSE\domain\src\main\model\DBUpgrade_002.019.007.001.sql
--
-- Developers can hand code logic here for data migration purposes
--
ALTER TABLE psp_pstub_employee_preference
ADD CONSTRAINT emp_pref_unq_indx UNIQUE (app_name, preference_name, employee_fk);