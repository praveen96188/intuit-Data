-- TEAMTRACK NUM: tbd
-- CREATED  DATE: 1.16.2009
-- MODIFIED DATE: 1.16.2009  15:00
-- AUTHOR       : EMR
--
-- PURPOSE: 
--   This script will modify the customer's offer expiration date.  Customer
--   details are as follows ...
--   SOURCE CUSTOMER ID  = 606016345
--   COMPANY NAME        = RX Staffing
--   OFFER CODE          = P57553
--   NEW EXPIRATION DATE = 1/1/2010
--
-- LOGON AS : PSPADM


SET SERVEROUTPUT ON
SET HEADING      ON
SET LINESIZE     1000
SET DEFINE       OFF


SPOOL dbupgrade_606016345.log

SELECT USER AS LOGIN_ID FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS START_TIME FROM DUAL;

PROMPT .
PROMPT Gather the customer info prior to the update ...

-- offer code should be for P57553 but currently is WAIVE ALL

SELECT 'PSP_OFFER'                        AS PSP_TABLE,
       SUBSTR(P.OFFER_SEQ, 1, 50)         AS PSP_OFFER_SEQ, 
       SUBSTR(P.OFFER_CD,  1, 30)         AS PSP_OFFER_CD, 
       SUBSTR(P.NAME,      1, 30)         AS OFFER_NAME, 
       TRUNC(P.END_DATE)                  AS OFFER_EXP
  FROM PSP_OFFER P
 WHERE P.OFFER_SEQ = '80995ef3-0003-0003-e040-11ac3bda020f'   -- P57553
    OR P.OFFER_SEQ = '80995ef3-0001-0001-e040-11ac3bda020f';  -- WAIVE ALL

SELECT 'PSP_COMPANY'                      AS PSP_TABLE,
       SUBSTR(P.COMPANY_SEQ,       1, 50) AS COMPANY_SEQ, 
       SUBSTR(P.SOURCE_COMPANY_ID, 1, 30) AS SRC_COMPANY_ID, 
       SUBSTR(P.SOURCE_SYSTEM_CD,  1, 10) AS SRC_SYSTEM_CD, 
       SUBSTR(P.DBA_NAME,          1, 50) AS DBA_NAME, 
       SUBSTR(P.LEGAL_NAME,        1, 50) AS LEGAL_NAME
  FROM PSP_COMPANY P
 WHERE P.COMPANY_SEQ       = 'a4e60cef-ae52-4ada-8aa1-136a91f99ff9'
   AND P.SOURCE_COMPANY_ID = '606016345'
   AND P.SOURCE_SYSTEM_CD  = 'QBDT';
   
SELECT 'PSP_COMPANY_OFFER'                AS PSP_TABLE,
       SUBSTR(P.COMPANY_FK, 1, 50)        AS COMPANY_SEQ, 
       SUBSTR(P.COMPANY_OFFER_SEQ, 1, 50) AS COMPANY_OFFER_SEQ, 
       SUBSTR(P.OFFER_FK, 1, 50)          AS OFFER_SEQ,
       SUBSTR(Q.OFFER_CD, 1, 30)          AS OFFER_CD,
       'Should Be P57553'                 AS EXPECTING_CD,
       TRUNC(P.END_DATE)                  AS COMPANY_OFFER_EXP
  FROM PSP_COMPANY_OFFER P,
       PSP_OFFER         Q
 WHERE P.OFFER_FK   = Q.OFFER_SEQ 
   AND P.COMPANY_FK = 'a4e60cef-ae52-4ada-8aa1-136a91f99ff9';

  
PROMPT .
PROMPT Update the companys offer code and expiration date ...

UPDATE PSP_COMPANY_OFFER
   SET OFFER_FK = '80995ef3-0003-0003-e040-11ac3bda020f',           -- P57553
       END_DATE = TO_DATE ('01012010', 'MMDDYYYY')
 WHERE COMPANY_FK        = 'a4e60cef-ae52-4ada-8aa1-136a91f99ff9'
   AND COMPANY_OFFER_SEQ = '832a9f16-b361-4c98-9bda-5d6ea5966107'
   AND OFFER_FK          = '80995ef3-0001-0001-e040-11ac3bda020f';  -- WAIVE ALL;


PROMPT .
PROMPT Confirm the total number of records for interested tables ...

SELECT 'PSP_COMPANY_OFFER'                AS PSP_TABLE,
       SUBSTR(P.COMPANY_FK, 1, 50)        AS COMPANY_SEQ, 
       SUBSTR(P.COMPANY_OFFER_SEQ, 1, 50) AS COMPANY_OFFER_SEQ, 
       SUBSTR(P.OFFER_FK, 1, 50)          AS OFFER_SEQ,
       SUBSTR(Q.OFFER_CD, 1, 30)          AS OFFER_CD,
       'Should Be P57553'                 AS EXPECTING_CD,
       TRUNC(P.END_DATE)                  AS COMPANY_OFFER_EXP
  FROM PSP_COMPANY_OFFER P,
       PSP_OFFER         Q
 WHERE P.OFFER_FK   = Q.OFFER_SEQ 
   AND P.COMPANY_FK = 'a4e60cef-ae52-4ada-8aa1-136a91f99ff9';


PROMPT .
PROMPT Remember to manually commit or rollback.
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS END_TIME FROM DUAL;
PROMPT Done.

SPOOL OFF