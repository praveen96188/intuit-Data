--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 

-- Moving them to post deploy as psp_lob tablespace doesnt exist in local env 

-- Prompt Column RESPONSE_DOCUMENT_OLD;
-- alter table PSP_SOURCE_SYSTEM_TRANSMISSION rename column RESPONSE_DOCUMENT to RESPONSE_DOCUMENT_OLD;

-- ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 -- ADD (RESPONSE_DOCUMENT CLOB) lob(RESPONSE_DOCUMENT) STORE AS SECUREFILE  ;

-- Prompt Column REQUEST_DOCUMENT_OLD;
-- alter table PSP_SOURCE_SYSTEM_TRANSMISSION rename column REQUEST_DOCUMENT to REQUEST_DOCUMENT_OLD;
-- ALTER TABLE PSP_SOURCE_SYSTEM_TRANSMISSION
 -- ADD (REQUEST_DOCUMENT  CLOB) lob(REQUEST_DOCUMENT) STORE AS SECUREFILE  ; 




PROMPT finishedDBUpgrade_002.013.004.003.sql