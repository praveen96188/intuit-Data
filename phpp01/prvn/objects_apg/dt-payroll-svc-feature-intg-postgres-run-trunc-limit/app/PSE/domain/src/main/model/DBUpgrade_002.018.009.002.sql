--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Constraint C_PSP_LAW0;
ALTER TABLE PSP_LAW
 DROP CONSTRAINT C_PSP_LAW0;

ALTER TABLE PSP_LAW
 ADD CONSTRAINT C_PSP_LAW0
  CHECK (LAW_CATEGORY_CODE IN('Withholding', 'SocialSecurityEmployee', 'SocialSecurityEmployer', 'Local', 'Supplemental', 'UnemploymentEmployer',
  'WorkersCompensationEmployee', 'DisabilityEmployer', 'UnemploymentEmployee', 'MedicareEmployee', 'DisabilityEmployee', 'UnemploymentHealthInsurance',
  'Unused', 'MedicareEmployer', 'TransitTax')) NOVALIDATE;

PROMPT finished DBUpgrade_002.018.009.002.sql