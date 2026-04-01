SET SERVEROUTPUT ON;

SPOOL dbupgrade_PSRV001059.log

PROMPT Count number of rows to update.  Can take up to 10 minutes


SELECT COUNT(*) AS NUM_ROWS_TO_UPDATE
  FROM PSP_SOURCE_SYSTEM_TRANSMISSION
 WHERE INSTR(REQUEST_DOCUMENT, '<OFX>')           = 1
   AND INSTR(REQUEST_DOCUMENT, '<USERPASS>*****') = 0
   AND (
              FROM_SOURCE_SYSTEM = 'QBDT' 
              OR 
              FROM_SOURCE_SYSTEM IS NULL
       )
   AND (TO_SOURCE_SYSTEM IS NULL)
   AND DESCRIPTION   = 'Error'
   AND TYPE          = 'Unknown'
   AND REQUEST_TOKEN = 0;    


PROMPT Modify existing rows.  This can take up to 10 minutes
---  Find those records where the USERPASS tag in the OFX is in the clear and substitute a new entry in its place
---  And further ensure that this only touches QBDT 
UPDATE PSP_SOURCE_SYSTEM_TRANSMISSION
SET REQUEST_DOCUMENT = 'An OFX Parse Error Occured.  Please have the customer retry the transmission and refresh the connection log.',
    FROM_SOURCE_SYSTEM = 'QBDT',
    MODIFIER_ID = 'QBDTAdapter',
    MODIFIED_DATE = SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) + 3/(24*60)
WHERE INSTR(REQUEST_DOCUMENT, '<OFX>') =1
AND INSTR(REQUEST_DOCUMENT, '<USERPASS>*****') = 0
AND (FROM_SOURCE_SYSTEM = 'QBDT' OR FROM_SOURCE_SYSTEM IS NULL)
AND (TO_SOURCE_SYSTEM IS NULL)
AND DESCRIPTION = 'Error'
AND TYPE = 'Unknown'
AND REQUEST_TOKEN = 0;

COMMIT;

PROMPT done

SPOOL OFF;

