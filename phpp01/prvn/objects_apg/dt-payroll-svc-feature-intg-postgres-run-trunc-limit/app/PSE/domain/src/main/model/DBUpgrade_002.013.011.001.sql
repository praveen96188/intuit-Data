--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column ALERT;
ALTER TABLE PSP_COMPANY_NOTE
 ADD (ALERT  NUMBER(1));

Prompt Constraint C_PSP_AGENCY_ID_REQUIREMENT0;
ALTER TABLE PSP_AGENCY_ID_REQUIREMENT
 DROP CONSTRAINT C_PSP_AGENCY_ID_REQUIREMENT0;

ALTER TABLE PSP_AGENCY_ID_REQUIREMENT
 ADD CONSTRAINT C_PSP_AGENCY_ID_REQUIREMENT0
  CHECK (CUSTOM_REQUIREMENT IN('MustNotInExemptedIdList', 'MustNotContainFedTaxId', 'IfNotMEorTRMustFollowFedTaxId', 'MustNotFollowFedTaxId', 'MustStartWithFedTaxId', 'MustFollowFedTaxId', 'Digits4Through12FollowFedTaxId', 'Digits2Through10FollowFedTaxId', 'None', 'MustNotFollowFedTaxIdSubstitueIf8Digits'));

PROMPT finished DBUpgrade_002.013.011.001.sql