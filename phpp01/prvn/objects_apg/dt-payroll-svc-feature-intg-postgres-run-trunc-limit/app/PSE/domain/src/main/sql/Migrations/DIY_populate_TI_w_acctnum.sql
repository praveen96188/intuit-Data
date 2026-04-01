-- CREATED: 11.18.2008
--  AUTHOR: EMR
-- PURPOSE:
--   The intent of this script is to populate DIY migrated companies that are
--   terminated, which were brought over without a bank account number.  This
--   is needed due to a dependency with EWS validation apis.  These termed 
--   companies will have a fixed bogus routing number, bank name and account
--   number.
--
--   This script should be run as PSPADM.


SET SERVEROUTPUT ON
SET HEADING      ON 
SET DEFINE       OFF

SPOOL DIY_populate_TI_w_acctnum.log

SELECT USER FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') FROM DUAL;


PROMPT The number of DIY terminated companies without a bank account before the update.

SELECT COUNT(*) 
  FROM psp_company         c,
       psp_company_service cs  
 WHERE cs.company_fk      = c.company_seq
   AND c.source_system_cd = 'QBDT'
   AND cs.status_cd       = 'Terminated'
   AND NOT EXISTS (
         SELECT 1 
           FROM psp_company_bank_account cba 
          WHERE cba.company_fk = c.company_seq
       ); 


PROMPT Update DIY terminated companies that do not have a bank account.

DECLARE
  v_count                          NUMBER := 0;
  v_bank_acct_seq                  PSP_BANK_ACCOUNT.BANK_ACCOUNT_SEQ%TYPE;
  
BEGIN
  
  FOR i IN (
    SELECT company_seq 
      FROM psp_company         c,
           psp_company_service cs  
     WHERE cs.company_fk      = c.company_seq
       AND c.source_system_cd = 'QBDT'
       AND cs.status_cd       = 'Terminated'
       AND NOT EXISTS (
             SELECT 1 
               FROM psp_company_bank_account cba 
              WHERE cba.company_fk = c.company_seq
           ) 
  )
  LOOP
  
    v_count := v_count + 1;
    
    -- confirm fields: audit fields, acct and rtn num
    INSERT INTO PSP_BANK_ACCOUNT (
      BANK_ACCOUNT_SEQ, 
      VERSION, 
      CREATOR_ID, 
      CREATED_DATE, 
      MODIFIER_ID, 
      MODIFIED_DATE, 
      REALM_ID, 
      ACCOUNT_NUMBER, 
      ACCOUNT_TYPE_CD, 
      BANK_NAME, 
      EFFECTIVE_DATE, 
      EXPIRATION_DATE, 
      ROUTING_NUMBER
    )
    VALUES (
      FN_FORMAT_SYSGUID(SYS_GUID()), 
      0, 
      'AS400MigrationBatchJob',
      TO_TIMESTAMP('11/21/2008 09:00:00.000000 PM', 'fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
      'AS400MigrationBatchJob', 
      TO_TIMESTAMP('11/21/2008 09:00:00.000000 PM', 'fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
      -1, 
      '9999999999', -- ficticious acct num
      'Checking', 
      'DIY TI MIGRATED COMPANY MANUAL UPDATE', 
      TO_TIMESTAMP('11/14/2008 09:00:00.000000 PM', 'fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
      NULL, 
      '121201063'   -- real rounting number
    )
    RETURNING BANK_ACCOUNT_SEQ INTO v_bank_acct_seq;

    
    -- verified fields: audit fields, status cd
    INSERT INTO PSP_COMPANY_BANK_ACCOUNT (
      COMPANY_BANK_ACCOUNT_SEQ, 
      VERSION, 
      CREATOR_ID, 
      CREATED_DATE, 
      MODIFIER_ID, 
      MODIFIED_DATE, 
      REALM_ID, 
      INSERT_USER_ID, 
      EFFECTIVE_DATE, 
      EXPIRATION_DATE, 
      SOURCE_BANK_ACCOUNT_ID, 
      STATUS_CD, 
      STATUS_EFFECTIVE_DATE, 
      TOTAL_RETRY_COUNT, 
      VERIFY_RETRY_COUNT, 
      SOURCE_BANK_ACCOUNT_NAME, 
      LAST_RETRY_DATE, 
      BANK_ACCOUNT_FK, 
      COMPANY_FK
    )
    VALUES (
      FN_FORMAT_SYSGUID(SYS_GUID()), 
      0, 
      'AS400MigrationBatchJob', 
      TO_TIMESTAMP('11/21/2008 09:00:00.000000 PM', 'fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
      'AS400MigrationBatchJob', 
      TO_TIMESTAMP('11/21/2008 09:00:00.000000 PM', 'fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
      -1, 
      NULL, 
      TO_TIMESTAMP('11/21/2008 09:00:00.000000 PM', 'fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'), 
      NULL, 
      '1', 
      'Inactive', 
      TO_TIMESTAMP('11/21/2008 09:00:00.000000 PM', 'fmMMfm/fmDDfm/YYYY fmHH12fm:MI:SS.FF AM'),
      0, 
      0, 
      'DIY TI MIGRATED COMPANY MANUAL UPDATE', 
      NULL, 
      v_bank_acct_seq, 
      i.company_seq
    );

  END LOOP;

  DBMS_OUTPUT.PUT_LINE ('Number of DIY DD TI companies updated : ' || v_count);
  
END;
/

SHOW ERRORS


PROMPT The number of DIY terminated companies without a bank account after the update.

SELECT COUNT(*) 
  FROM psp_company         c,
       psp_company_service cs  
 WHERE cs.company_fk      = c.company_seq
   AND c.source_system_cd = 'QBDT'
   AND cs.status_cd       = 'Terminated'
   AND NOT EXISTS (
         SELECT 1 
           FROM psp_company_bank_account cba 
          WHERE cba.company_fk = c.company_seq
       ); 


PROMPT Reminder to manually commit or rollback.

SPOOL OFF
