DROP TABLE IF EXISTS TEMP_PSP_REPORTING_AGENT CASCADE;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_REPORTING_AGENT (LIKE PSP_REPORTING_AGENT INCLUDING ALL) ;


--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------
INSERT INTO TEMP_PSP_REPORTING_AGENT(REPORTING_AGENT_SEQ, VERSION, REALM_ID, FED_ID_ENC, FED_TAX_ID_ENC, LEGAL_NAME, PHONE, FAX, CONTACT, ADDRESS_FK, CREATED_DATE, MODIFIED_DATE) VALUES ('4c99e81c-0ac1-4f42-bea5-cf2e13a69314', -1, -1, null, (CASE WHEN FN_GET_ENV() = 'PROD' THEN '2gIAAAASAQAEAAAAAQIACGsxSVFVSHRWZEAnvXG0fActX1Ovus/NmxPW3jgIS+zgNHU=' ELSE '2gIAAAASAQAEAAAAAwIACHNZWXdKMU9R3ocqXOMvs6A/awkK4+TmYV2K8AZHMzsYb8g=' END), 'Computing Resources, Inc.', '775 424-8000', '866 293-2047', 'RAF Agent', 'a1544b11-7095-4e77-89c3-7253cdeb7ac6', timezone('UTC', CURRENT_TIMESTAMP), timezone('UTC', CURRENT_TIMESTAMP))
;
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
;

DELETE FROM PSP_REPORTING_AGENT
WHERE (REPORTING_AGENT_SEQ, REALM_ID) NOT IN (SELECT REPORTING_AGENT_SEQ, REALM_ID FROM TEMP_PSP_REPORTING_AGENT) 
;


UPDATE PSP_REPORTING_AGENT rt
SET ( 
    REPORTING_AGENT_SEQ, VERSION, REALM_ID, FED_ID_ENC, FED_TAX_ID_ENC, LEGAL_NAME, PHONE, FAX, CONTACT, ADDRESS_FK, CREATED_DATE, MODIFIED_DATE) =
(SELECT
    tt.REPORTING_AGENT_SEQ, tt.VERSION, tt.REALM_ID, tt.FED_ID_ENC, tt.FED_TAX_ID_ENC, tt.LEGAL_NAME, tt.PHONE, tt.FAX, tt.CONTACT, tt.ADDRESS_FK, tt.CREATED_DATE, tt.MODIFIED_DATE
 FROM TEMP_PSP_REPORTING_AGENT tt 
 WHERE tt.REPORTING_AGENT_SEQ = rt.REPORTING_AGENT_SEQ
   AND TT.REALM_ID  = RT.REALM_ID
)
;


--------------------------------------------------------
-- Drop temp table                                    --
--------------------------------------------------------
DROP TABLE TEMP_PSP_REPORTING_AGENT
;

COMMIT
;
