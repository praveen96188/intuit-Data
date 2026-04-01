--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 


Prompt Column AGENCY_TAXPAYER_ID;
ALTER TABLE PSP_COMPANYAGENCY_PMTTEMPLATE
 ADD (AGENCY_TAXPAYER_ID  VARCHAR2(80 CHAR));

Prompt Column AGENCY_TAXPAYER_ID;
ALTER TABLE PSP_COMPANY_AGENCY SET UNUSED COLUMN AGENCY_TAXPAYER_ID; 
 
PROMPT finishedDBUpgrade_002.000.013.002.sql