
DECLARE
	
	table_exists PLS_INTEGER;

BEGIN

	SELECT COUNT(*) INTO table_exists
	FROM "USER_TABLES"
	WHERE TABLE_NAME = 'TEMP_PSP_REPORTING_AGENT';

	IF table_exists = 1 THEN
		EXECUTE IMMEDIATE 'DROP TABLE "TEMP_PSP_REPORTING_AGENT" CASCADE CONSTRAINTS';
	END IF;
	

END;
/

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------

CREATE TABLE TEMP_PSP_REPORTING_AGENT
(
  REPORTING_AGENT_SEQ  VARCHAR2(255 CHAR)       NOT NULL,
  VERSION              NUMBER(19)               NOT NULL,
  CREATOR_ID           VARCHAR2(30 CHAR),
  CREATED_DATE         TIMESTAMP(6)             NOT NULL,
  MODIFIER_ID          VARCHAR2(30 CHAR),
  MODIFIED_DATE        TIMESTAMP(6)             NOT NULL,
  REALM_ID             NUMBER(19)               DEFAULT -1                    NOT NULL,
  FAX                  VARCHAR2(100 CHAR),
  FED_ID_ENC               VARCHAR2(80 CHAR),
  FED_TAX_ID_ENC           VARCHAR2(80 CHAR),
  LEGAL_NAME           VARCHAR2(100 CHAR),
  PHONE                VARCHAR2(100 CHAR),
  CONTACT        VARCHAR2(255 CHAR),
  ADDRESS_FK           VARCHAR2(255 CHAR)
)
/


--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------
INSERT INTO TEMP_PSP_REPORTING_AGENT(REPORTING_AGENT_SEQ, VERSION, REALM_ID, FED_ID_ENC, FED_TAX_ID_ENC, LEGAL_NAME, PHONE, FAX, CONTACT, ADDRESS_FK, CREATED_DATE, MODIFIED_DATE) VALUES ('4c99e81c-0ac1-4f42-bea5-cf2e13a69314', -1, -1, null, DECODE(FN_GET_ENV(),'PROD','2gIAAAASAQAEAAAAAQIACGsxSVFVSHRWZEAnvXG0fActX1Ovus/NmxPW3jgIS+zgNHU=','2gIAAAASAQAEAAAAAwIACHNZWXdKMU9R3ocqXOMvs6A/awkK4+TmYV2K8AZHMzsYb8g='), 'Computing Resources, Inc.', '775 424-8000', '866 293-2047', 'RAF Agent', 'a1544b11-7095-4e77-89c3-7253cdeb7ac6', SYS_EXTRACT_UTC(SYSTIMESTAMP), SYS_EXTRACT_UTC(SYSTIMESTAMP))
/
--------------------------------------------------------
-- Sychronize temp table and real table by            --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_REPORTING_AGENT
    (REPORTING_AGENT_SEQ, VERSION, REALM_ID, FED_ID_ENC, FED_TAX_ID_ENC, LEGAL_NAME, PHONE, FAX, CONTACT, ADDRESS_FK, CREATED_DATE, MODIFIED_DATE)
SELECT 
     REPORTING_AGENT_SEQ, VERSION, REALM_ID, FED_ID_ENC, FED_TAX_ID_ENC, LEGAL_NAME, PHONE, FAX, CONTACT, ADDRESS_FK, CREATED_DATE, MODIFIED_DATE
FROM 
   TEMP_PSP_REPORTING_AGENT tt 
WHERE 
   tt.REPORTING_AGENT_SEQ NOT IN (SELECT REPORTING_AGENT_SEQ FROM PSP_REPORTING_AGENT)
/

DELETE FROM PSP_REPORTING_AGENT
WHERE (REPORTING_AGENT_SEQ, REALM_ID) NOT IN (SELECT REPORTING_AGENT_SEQ, REALM_ID FROM TEMP_PSP_REPORTING_AGENT) 
/


UPDATE PSP_REPORTING_AGENT rt
SET ( 
    rt.REPORTING_AGENT_SEQ, rt.VERSION, rt.REALM_ID, rt.FED_ID_ENC, rt.FED_TAX_ID_ENC, rt.LEGAL_NAME, rt.PHONE, rt.FAX, rt.CONTACT, rt.ADDRESS_FK, rt.CREATED_DATE, rt.MODIFIED_DATE) =
(SELECT
    tt.REPORTING_AGENT_SEQ, tt.VERSION, tt.REALM_ID, tt.FED_ID_ENC, tt.FED_TAX_ID_ENC, tt.LEGAL_NAME, tt.PHONE, tt.FAX, tt.CONTACT, tt.ADDRESS_FK, tt.CREATED_DATE, tt.MODIFIED_DATE
 FROM TEMP_PSP_REPORTING_AGENT tt 
 WHERE tt.REPORTING_AGENT_SEQ = rt.REPORTING_AGENT_SEQ
   AND TT.REALM_ID  = RT.REALM_ID
)
/


--------------------------------------------------------
-- Drop temp table                                    --
--------------------------------------------------------
DROP TABLE TEMP_PSP_REPORTING_AGENT
/

COMMIT
/
