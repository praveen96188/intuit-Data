--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--                                                                      --
-- Please review the script before using it to make sure it won't       --
-- cause any unacceptable data loss.                                    --
--                                                                      --
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL
-- PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL

Prompt Column IS_VERIFIED;
ALTER TABLE PSP_BATCH_JOB_AUDIT_LOG
 ADD (IS_VERIFIED  NUMBER(1));

Prompt Trigger TR_INS_COMPANY_EVENT_TIMESTAMP;
--
-- TR_INS_COMPANY_EVENT_TIMESTAMP  (Trigger)
--
CREATE OR REPLACE TRIGGER TR_INS_COMPANY_EVENT_TIMESTAMP
BEFORE INSERT ON PSP_COMPANY_EVENT FOR EACH ROW
BEGIN
    IF (:NEW.EVENT_TYPE_CD <> 'Strike' AND :NEW.CREATOR_ID <> 'QBOEMigrationBatchJob') THEN
    	SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO :NEW.EVENT_TIME_STAMP FROM DUAL;
    END IF;
END;
/
SHOW ERRORS;

/