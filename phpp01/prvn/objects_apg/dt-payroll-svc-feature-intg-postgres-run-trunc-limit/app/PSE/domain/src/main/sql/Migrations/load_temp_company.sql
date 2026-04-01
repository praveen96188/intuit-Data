SELECT SYSTIMESTAMP AS START_LOAD_TEMP_COMP FROM DUAL;

PROMPT LOAD COMPANY INFO

EXEC pr_load_temp_company(&&p_batch_id);

SELECT SYSTIMESTAMP AS END_LOAD_TEMP_COMP FROM DUAL;



PROMPT UPDATE TEMP COMPANY WITH PAYROLL ADMIN CONTACT 

SELECT SYSTIMESTAMP AS START_PR_UPDATE_CONTACT FROM DUAL;

EXEC pr_update_contact_info(&&p_batch_id);


SELECT SYSTIMESTAMP AS END_PR_UPDATE_CONTACT FROM DUAL;

PROMPT UPDATE TEMP COMPANY WITH AGREEMENT CONTACT

SELECT SYSTIMESTAMP AS START_UPDATE_AGREEMENT_CONTACT FROM DUAL;

UPDATE PSPADM.TEMP_COMPANY tc
   SET (CONTACT_ADDRESS_LINE1,
        CONTACT_ADDRESS_LINE2,
        CONTACT_CITY,
        CONTACT_COUNTRY,
        CONTACT_STATE,
        CONTACT_ZIP_CODE,
        CONTACT_TITLE,
        CONTACT_JOB_TITLE,
        CONTACT_FAX,
        CONTACT_PHONE,
        CONTACT_FIRST_NAME,
        CONTACT_GENDER_CD,
        CONTACT_MIDDLE_NAME,
        CONTACT_LAST_NAME,
        CONTACT_EMAIL,
        CONTACT_SECOND_PHONE) =
          (SELECT /*+ DRIVING_SITE (addr ) DRIVING_SITE(con) DRIVING_SITE(rel) DRIVING_SITE(org ) */
                 ADDR.ADDR,
                  ADDR.ADDR_LINE_2,
                  ADDR.CITY,
                  ADDR.COUNTRY,
                  ADDR.STATE,
                  ADDR.ZIPCODE,
                  CON.PER_TITLE,
                  con.job_title,
                  con.fax_ph_num,
                  con.home_ph_num,
                  con.fst_name,
                  con.sex_mf,
                  con.x_mid_initial,
                  con.last_name,
                  con.x_email_addr,
                  con.work_ph_num
             FROM siebel.s_addr_per@CRIS_PROD addr,
                  siebel.s_contact@CRIS_PROD con,
                  siebel.s_doc_agree@CRIS_PROD doc
            WHERE doc.row_id = TC.AGREE_INFO_SOURCE_ID
                              AND doc.con_per_id = con.par_row_id
       AND con.pr_per_addr_id = ADDR.ROW_ID(+) )
            WHERE TC.CONTACT_FIRST_NAME IS NULL
            AND batch_id=&&p_batch_id LOG ERRORS REJECT LIMIT UNLIMITED;



commit;

SELECT SYSTIMESTAMP AS END_UPDATE_AGREEMENT_CONTACT FROM DUAL;

SELECT SYSTIMESTAMP AS START_DATA_CLEANUP FROM DUAL;

PROMPT UPDATE NULL PRICETYPE WITH STANDARD

UPDATE TEMP_COMPANY SET PRICE_TYPE='STANDARD' where PRICE_TYPE is null and batch_id=&&p_batch_id;

commit;

PROMPT UPDATE NULL NOTIFICATION EMAIL WITH PAYROLL ADMIN CONTACT EMAIL

UPDATE TEMP_COMPANY SET COMP_NOTIFICATION_EMAIL = CONTACT_EMAIL WHERE COMP_NOTIFICATION_EMAIL 
 IS NULL AND BATCH_ID=&&p_batch_id;

commit;

PROMPT CLEANUP CONTACT EMAIL

UPDATE TEMP_COMPANY
   SET CONTACT_EMAIL = 'noemail'||RECORD_PK||'@intuit.com'
 WHERE record_pk NOT IN
          (SELECT TC.RECORD_PK
             FROM temp_company tc
            WHERE REGEXP_LIKE (
                     CONTACT_EMAIL,
                     '^[a-z0-9!#$%&''*+/=?^_`{|}~-]+(\.[a-z0-9!#$%&''*+/=?^_`{|}~-]+)*@([a-z0-9]([a-z0-9-]*[a-z0-9])?\.)+([A-Z]{2}|arpa|biz|com|info|intww|name|net|org|pro|aero|asia|cat|coop|edu|gov|jobs|mil|mobi|museum|pro|tel|travel|post)$',
                     'i')
                  AND BATCH_ID = &&p_batch_id)
       AND BATCH_ID = &&p_batch_id;
       
       
COMMIT;

PROMPT CLEANUP NOTIFICATION EMAIL

UPDATE TEMP_COMPANY
   SET COMP_NOTIFICATION_EMAIL = 'noemail'||RECORD_PK||'@intuit.com'
 WHERE record_pk NOT IN
          (SELECT TC.RECORD_PK
             FROM temp_company tc
            WHERE REGEXP_LIKE (
                     CONTACT_EMAIL,
                     '^[a-z0-9!#$%&''*+/=?^_`{|}~-]+(\.[a-z0-9!#$%&''*+/=?^_`{|}~-]+)*@([a-z0-9]([a-z0-9-]*[a-z0-9])?\.)+([A-Z]{2}|arpa|biz|com|info|intww|name|net|org|pro|aero|asia|cat|coop|edu|gov|jobs|mil|mobi|museum|pro|tel|travel|post)$',
                     'i')
                  AND BATCH_ID = &&p_batch_id)
       AND BATCH_ID = &&p_batch_id;
       
       
       COMMIT;



PROMPT UPDATE NULL CONTACT PHONE NUMBERS WITH COMPANY PHONE NUMBERS  
    
UPDATE temp_company te
   SET CONTACT_PHONE = TE.PHONE
 WHERE TE.CONTACT_PHONE IS NULL AND TE.BATCH_ID = &&p_batch_id ;

commit;

PROMPT DELETE DUPLICATE AGREEMENT || EIN 'S
DELETE FROM TEMP_COMPANY
      WHERE RECORD_PK IN (  SELECT MIN (RECORD_PK)
                              FROM temp_company te
                             WHERE batch_id=&&p_batch_id
                          GROUP BY TE.AGREE_INFO_SUB_NBR, TE.FED_TAX_ID
                            HAVING COUNT (*) > 1);

commit;

SELECT SYSTIMESTAMP AS END_DATA_CLEANUP FROM DUAL;