--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

Prompt Column VERSION;
ALTER TABLE PSP_AUTH_USER
MODIFY(VERSION  DEFAULT 0);

Prompt Column VERSION;
ALTER TABLE PSP_COMPANY
MODIFY(VERSION  DEFAULT 0);

Prompt Column VERSION;
ALTER TABLE PSP_QUICKBOOKS_INFO
MODIFY(VERSION  DEFAULT 0);

Prompt Column P_I_N_VALUE;
ALTER TABLE PSP_COMPANY_PIN
MODIFY(P_I_N_VALUE VARCHAR2(256 CHAR));


Prompt Column HASH_TYPE;
ALTER TABLE PSP_COMPANY_PIN
 ADD (HASH_TYPE  VARCHAR2(255 CHAR));

Prompt Column VERSION;
ALTER TABLE PSP_ENTITLEMENT_UNIT
MODIFY(VERSION  DEFAULT 0);

PROMPT finishedDBUpgrade_002.013.004.007.sql