--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
/* Prompt COLUMN IS_PRIMARY;
ALTER TABLE PSP_ENTITLEMENT_CODE
 ADD (IS_PRIMARY  NUMBER(1));

Prompt COLUMN LAST_MESSAGE_TIMESTAMP;
ALTER TABLE PSP_ENTITLEMENT
 ADD (LAST_MESSAGE_TIMESTAMP  TIMESTAMP(6));

Prompt COLUMN REQUIRES_UNIQUE_E_I_N;
ALTER TABLE PSP_SERVICE DROP COLUMN REQUIRES_UNIQUE_E_I_N;

Prompt INDEX PSP_ENTITLEMENT_U1;
--
-- PSP_ENTITLEMENT_U1  (Index) 
--
CREATE UNIQUE INDEX PSP_ENTITLEMENT_U1 ON PSP_ENTITLEMENT
(LICENSE_NUMBER, ENTITLEMENT_OFFERING_CODE)
LOGGING
NOPARALLEL;

Prompt COLUMN VERSION;
ALTER TABLE PSP_COMPANY
MODIFY(VERSION  DEFAULT 0);

Prompt COLUMN VERSION;
ALTER TABLE PSP_AUTH_USER
MODIFY(VERSION  DEFAULT 0);

Prompt COLUMN FED_TAX_ID;
ALTER TABLE PSP_ENTITLEMENT_UNIT
 ADD (FED_TAX_ID  VARCHAR2(9 CHAR));

-- copy FEIN -> FedTaxId
UPDATE PSP_ENTITLEMENT_UNIT
SET FED_TAX_ID = F_E_I_N;


Prompt COLUMN F_E_I_N;
ALTER TABLE PSP_ENTITLEMENT_UNIT DROP COLUMN F_E_I_N;  */


PROMPT finishedDBUpgrade_002.001.000.004.sql