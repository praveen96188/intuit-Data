CREATE OR REPLACE procedure PSPADM.pr_load_temp_company
    (
    p_batch_id in NUMBER

    )
is
v_ins_cnt number:=0;
v_ins_sofar number;
 cursor c1 is
SELECT /*+ DRIVING_SITE(DOC) DRIVING_SITE(ENT) DRIVING_SITE(ENT_X) DRIVING_SITE(PRI) DRIVING_SITE(ADDR) DRIVING_SITE(ORG) DRIVING_SITE(CON) DRIVING_SITE(DR)  parallel(ORG,8) parallel(CON,8) parallel(DOC,8) parallel(ENT,8) parallel(ENT_X,8) parallel(ADDR,8) */
     dr.pK RECORD_PK,
           DR.BATCH_ID BATCH_ID,
       ent_x.name FED_TAX_ID,
       doc.x_sub_num_stage AGREE_INFO_SUB_NBR,
DR.CRIS_ACCOUNT_ROW_ID CRIS_ACCT_ROWID,
       org.main_ph_num PHONE,
       org.name LEGAL_NAME,
       pri.subtype_cd PRICE_TYPE,
       NULL IAM_REALM_ID,
       ent_x.x_app_ver_lic QB_INFO_APP_VERSION,
       NULL QB_INFO_APPLICATION_ID,
       ent_x.x_attrib_24 QB_INFO_LICENSE_NUMBER,
       doc.created AGREE_INFO_AGREE_CREATE_DATE,
       doc.name AGREE_INFO_NAME,
       doc.x_service_type AGREE_INFO_SERVICE_TYPE,
       doc.row_id AGREE_INFO_SOURCE_ID,
       ent_x.x_attrib_02 AGREE_INFO_SERVICE_KEY,
       doc.x_subtype AGREE_INFO_AGREE_SUB_TYPE,
       addr.addr_line_2 COMP_ADDRESS_LINE2,
       addr.addr COMP_ADDRESS_LINE1,
       addr.city COMP_CITY,
       addr.country COMP_COUNTRY,
       addr.state COMP_STATE,
       addr.zipcode COMP_ZIP_CODE,
       con.email_addr COMP_NOTIFICATION_EMAIL,
       NULL CONTACT_TITLE,
       NULL CONTACT_JOB_TITLE,
       NULL CONTACT_FAX,
       NULL CONTACT_SECOND_PHONE,
       NULL CONTACT_IAM_AUTH_ID,
       NULL CONTACT_FIRST_NAME,
       NULL CONTACT_LAST_NAME,
       NULL CONTACT_MIDDLE_NAME,
       NULL CONTACT_GENDER_CD,
       NULL CONTACT_EMAIL,
       NULL CONTACT_PHONE,
       NULL CONTACT_ADDRESS_LINE2,
       NULL CONTACT_ADDRESS_LINE1,
       NULL CONTACT_CITY,
       NULL CONTACT_COUNTRY,
       NULL CONTACT_STATE,
       NULL CONTACT_ZIP_CODE,
       systimestamp MIGRATION_STATUS_DTTM,
       NULL ERROR_MESSAGE,
       systimestamp CREATED_DTTM,
           DR.BATCH_ID original_batch_id
            FROM SIEBEL.S_ORG_EXT@SIEBEL_PROD ORG,
SIEBEL.S_ORG_EXT@CRIS_PROD  ORG1,
          SIEBEL.S_DOC_AGREE@CRIS_PROD DOC,
          SIEBEL.S_ENTLMNT@CRIS_PROD ENT,
         SIEBEL.CX_ENTLMNT_XM@CRIS_PROD ENT_X,
          SIEBEL.S_CONTACT@SIEBEL_PROD CON,
          SIEBEL.S_PRI_LST@CRIS_PROD PRI,
          SIEBEL.S_ADDR_ORG@CRIS_PROD ADDR,
          MIG102.DE_PSP_DRIVER@driver_lnk DR
    WHERE    DOC.ROW_ID = ENT.PAR_AGREE_ID
  AND DOC.TARGET_OU_ID=ORG1.ROW_ID
          AND ENT.ROW_ID = ENT_X.PAR_ROW_ID
          AND PRI.ROW_ID(+) = DOC.PRI_LST_ID
          AND CON.PAR_ROW_ID(+) = ORG.PR_CON_ID
/* AND DOC.BILL_TO_ADDR_ID=ADDR.ROW_ID */
          AND ORG1. PR_BL_ADDR_ID=ADDR.ROW_ID
          AND DOC.ROW_ID = DR.CRIS_AGREEMENT_ROW_ID
          and ENT_X.ROW_ID=DR.CRIS_ENT_EXT_ROW_ID
         AND ORG.ROW_ID=DR.SIEBEL_ACCOUNT_ROW_ID
         AND DR.BATCH_ID= p_batch_id
         AND DR.STATUS = 'FOS : PRECUTOVER COMPLETE';


 type   INS_ARR is table of  C1%ROWTYPE  ;
   REC_ARR INS_ARR;
 BEGIN
   OPEN C1;
   LOOP
   FETCH C1 BULK COLLECT INTO REC_ARR LIMIT 5000;
   BEGIN
   FORALL I IN 1..REC_ARR.COUNT
   /* Formatted on 9/14/2011 12:06:52 AM (QP5 v5.163.1008.3004) */
INSERT /*+ append*/  INTO PSPADM.TEMP_COMPANY VALUES REC_ARR(I) LOG ERRORS REJECT LIMIT UNLIMITED;
   END;
v_ins_cnt:=SQL%ROWCOUNT;
--   COMMIT;
v_ins_sofar:=v_ins_sofar+v_ins_cnt;
dbms_application_info.set_action( 'INS ' || v_ins_sofar || ' to TEMP_COMPANY');
dbms_output.put_line('inserted ' || v_ins_sofar || 'so far');
   EXIT WHEN REC_ARR.COUNT=0;
   END LOOP;
   CLOSE C1;
   COMMIT;

end pr_load_temp_company;
/
