--
-- This script will be executed AFTER the automatically generated
-- C:\dev\psp\main\pse\domain\src\main\model\DBUpgradeFrom_1.9.9.20_To_1.9.9.21.sql
--
-- Developers can hand code logic here for data migration purposes
--
DECLARE
v_psp_date TIMESTAMP;
v_utc_date TIMESTAMP;
v_event_id VARCHAR2(100); 
BEGIN
 SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;
 SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL;
   FOR REC IN (
                  SELECT COMPANY_NOTE_SEQ, CREATOR_ID, MODIFIER_ID, COMPANY_FK FROM PSP_COMPANY_NOTE WHERE company_event_fk IS NULL
              )
   LOOP
       v_event_id := FN_FORMAT_SYSGUID(SYS_GUID());
       INSERT INTO PSP_COMPANY_EVENT
       (COMPANY_EVENT_SEQ,VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, EVENT_TIME_STAMP, STATUS_EFFECTIVE_DATE,
       STATUS_CD, EVENT_TYPE_CD, EVENT_TOKEN, EMAIL_STATUS_EFFECTIVE_DATE, EMAIL_RETRY_COUNT, EMAIL_STATUS, SOURCE_ID, NOTE_LAST_UPDATED_DATE, 
       COMPANY_FK)
       VALUES (v_event_id ,0,REC.CREATOR_ID,v_utc_date,REC.MODIFIER_ID,v_utc_date, -1,
       v_psp_date, v_psp_date, 'Active', 'ManualNoteEvent', 0, v_psp_date, 0, 'Ignore', NULL, v_psp_date, REC.COMPANY_FK);  
       UPDATE PSP_COMPANY_NOTE SET company_event_fk=v_event_id WHERE COMPANY_NOTE_SEQ = REC.COMPANY_NOTE_SEQ;
   END LOOP;
END;

/

SHOW ERRORS;

ALTER TABLE PSP_COMPANY_NOTE 
MODIFY (COMPANY_EVENT_FK NOT NULL); 