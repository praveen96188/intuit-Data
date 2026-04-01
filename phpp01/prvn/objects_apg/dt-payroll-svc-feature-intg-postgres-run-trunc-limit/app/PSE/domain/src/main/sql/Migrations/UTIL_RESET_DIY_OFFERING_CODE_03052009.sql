-- CREATED: 03.03.2009 
-- AUTHOR : EMR
-- PURPOSE:
-- this script will change the price code of companies from DIYDDSTD-3 (waive the
-- 3 dollar payroll fee) to DIYDDSTD.  This is due to a bug in the adapters that
-- accidentally reset the price codes after migration.
--
-- It is assumed that a list of companies will be provided, thus no search criteria
-- is needed.
--
-- A temporary table will be created to hold the list of companies to update - see other
-- file that loads the list of companies.
--
--   CREATE_TABLE Z_TEMP_DIY_RESET_COMPANY
--
-- This must be run as PSPADM user.

SET serveroutput ON
SET linesize     500
SET pagesize     1000
SET define       OFF

SPOOL util_reset_DIY_price_code_03052009.log

SELECT USER AS LOGIN_ID FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS START_TIME FROM DUAL;


PROMPT .
PROMPT Number of companies in the list ...

SELECT COUNT(*)
  FROM Z_TEMP_DIY_RESET_COMPANY;
  

PROMPT .
PROMPT Number of companies to update ...

SELECT COUNT(*)
  FROM Z_TEMP_DIY_RESET_COMPANY tdrc,
       PSP_COMPANY              c,
       PSP_OFFERING             o
 WHERE tdrc.SOURCE_COMPANY_ID = c.SOURCE_COMPANY_ID
   AND c.OFFERING_FK          = o.OFFERING_SEQ
   AND C.SOURCE_SYSTEM_CD     = 'QBDT'
   AND o.NAME                 = 'DIYDDSTD-3';


PROMPT .
PROMPT Update the price code back to standard ...

DECLARE
  
  v_pc_diyddstd3        VARCHAR2(50)  := 'DIYDDSTD-3';
  v_pc_diyddstd         VARCHAR2(50)  := 'DIYDD-STD';
  v_pc_diyddstd_seq     VARCHAR2(255);
  v_modifier_id         VARCHAR2(255) := 'AS400MigrationBatchJob';
  v_upd_count           PLS_INTEGER   := 0;
    
BEGIN

  -- a predetermined list has been made.  Refine that list further
  -- by omitting companies that have already been reset back to a
  -- price code of standard.
  
  SELECT Offering_SEQ
    INTO v_pc_diyddstd_seq
    FROM PSP_OFFERING
   WHERE Name = v_pc_diyddstd;
   
  DBMS_OUTPUT.PUT_LINE ('The DIYDDSTD offering PK used = ' || v_pc_diyddstd_seq);
    
  FOR i IN (
    SELECT tdrc.SOURCE_COMPANY_ID AS SRC_COMP_ID,
           c.COMPANY_SEQ          AS COMP_SEQ
      FROM Z_TEMP_DIY_RESET_COMPANY tdrc,
           PSP_COMPANY              c,
           PSP_OFFERING             o
     WHERE tdrc.SOURCE_COMPANY_ID = c.SOURCE_COMPANY_ID
       AND c.OFFERING_FK          = o.OFFERING_SEQ
       AND C.SOURCE_SYSTEM_CD     = 'QBDT'
       AND o.NAME                 = v_pc_diyddstd3
  )
  LOOP
    
    -- update the offering 
    UPDATE PSP_COMPANY
       SET Offering_FK   = v_pc_diyddstd_seq,
           Modifier_ID   = v_modifier_id,
           Modified_Date = SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP)
     WHERE COMPANY_SEQ = i.COMP_SEQ;
     
    -- mark them as updated
    IF (SQL%ROWCOUNT > 0) THEN
      
      UPDATE Z_TEMP_DIY_RESET_COMPANY
         SET UPDATED_YN_IND = 'Y',
             MODIFIED_DTTM  = SYSDATE
       WHERE SOURCE_COMPANY_ID = i.SRC_COMP_ID;
       
      v_upd_count := v_upd_count + SQL%ROWCOUNT;

    END IF;
                
  END LOOP;
  
  DBMS_OUTPUT.PUT_LINE ('Number of companies actually updated = ' || v_upd_count);
  
EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE ('ERROR : ' || SQLERRM);
END;
/


PROMPT .
PROMPT Final count of which companies were updated ...

SELECT Updated_YN_IND,
       COUNT(*)
  FROM Z_TEMP_DIY_RESET_COMPANY
 GROUP
    BY UPDATED_YN_IND;
  

PROMPT .
PROMPT Recount of number of companies to update - should be 0 ...

SELECT COUNT(*)
  FROM Z_TEMP_DIY_RESET_COMPANY tdrc,
       PSP_COMPANY              c,
       PSP_OFFERING             o
 WHERE tdrc.SOURCE_COMPANY_ID = c.SOURCE_COMPANY_ID
   AND c.OFFERING_FK          = o.OFFERING_SEQ
   AND C.SOURCE_SYSTEM_CD     = 'QBDT'
   AND o.NAME                 = 'DIYDDSTD-3';


PROMPT .
PROMPT Done Dude.

SELECT TO_CHAR(SYSDATE, 'MM.DD.YYYY HH24:MI') AS END_TIME FROM DUAL;


SPOOL OFF