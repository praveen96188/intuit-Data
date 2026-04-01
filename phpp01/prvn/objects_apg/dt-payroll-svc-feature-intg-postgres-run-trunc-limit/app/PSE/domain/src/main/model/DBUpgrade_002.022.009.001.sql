--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Constraint C_PSP_EMPLOYEE_WAGE_PLAN0;
ALTER TABLE PSP_EMPLOYEE_WAGE_PLAN
 DROP CONSTRAINT C_PSP_EMPLOYEE_WAGE_PLAN0;

ALTER TABLE PSP_EMPLOYEE_WAGE_PLAN
 ADD CONSTRAINT C_PSP_EMPLOYEE_WAGE_PLAN0
  CHECK (NAME IN('WPC', 'GC', 'OC', 'FCC')) ENABLE NOVALIDATE;

PROMPT finished DBUpgrade_002.022.009.001.sql