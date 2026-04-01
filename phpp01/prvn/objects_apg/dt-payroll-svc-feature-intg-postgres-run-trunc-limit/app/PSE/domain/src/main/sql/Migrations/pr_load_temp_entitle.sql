
PROMPT CREATE PROCEDURE PR_LOAD_TEMP_ENTITLE


CREATE OR REPLACE procedure PSPADM.pr_load_temp_entitle
  (
  p_status in varchar2
    )
  is
cursor c1 is SELECT
 RECORD_PK,
 SYSTIMESTAMP CREATED_DTTM,
BATCH_ID,
       FED_TAX_ID,
       AGREE_INFO_SUB_NBR,
       LICENSE_NUMBER,
       ENTITLEMENT_OFFERING_CODE,
       EC_EDITION_TYPE,
       EC_NUM_EMPL_TYPE,
       EC_ASSET_ITEM_NUM,
       ORDER_NUMBER,
       ORDER_SOURCE,
       CUSTOMER_ID,
       NEXT_CHARGE_DATE,
       PAYMENT_METHOD_TYPE,
       CREDIT_CARD_TYPE,
       CREDIT_CARD_NUMBER,
       CREDIT_CARD_EXPIRATION,
       CONTACT_EMAIL,
       ENTITLEMENT_STATE,
       CONTACT_NAME,
       SUBSCRIPTION_NUMBER,
       SUBSCRIPTION_END_DATE,
       BILLING_ZIP_CODE,
       CANCELLATION_REASON,
       ENTITLEMENT_UNIT_STATUS,
       SERVICE_KEY,
       EXTENSION_KEY,
       LAST_VALIDATION_DATE,
        SYSTIMESTAMP MIGRATION_STATUS_DTTM,
        'NONE' ERROR_MESSAGE

  FROM (SELECT   /*+  DRIVING_SITE (ORD ) DRIVING_SITE(ORDX) DRIVING_SITE(ORDITM) DRIVING_SITE(ORDITMO ) DRIVING_SITE(ASTX ) DRIVING_SITE(AST ) DRIVING_SITE(ORG ) DRIVING_SITE (INV ) DRIVING_SITE(CON) DRIVING_SITE(ADDR) DRIVING_SITE (DR ) 
DRIVING_SITE (spi ) DRIVING_SITE(saxa) DRIVING_SITE(saxa1) */
              ORD.order_num
                  ORDER_NUMBER,
                  pk RECORD_PK,
               ORDX.attrib_37 ORDER_SOURCE,
               AST.ref_number_1 LICENSE_NUMBER,
               ORG.integration_id CUSTOMER_ID,
               ORDITMO.service_start_dt NEXT_CHARGE_DATE,
               INV.paymt_meth_cd PAYMENT_METHOD_TYPE,
               INV.crdt_crd_appr_cd CREDIT_CARD_TYPE,
               substr(INV.cc_number,-4) CREDIT_CARD_NUMBER,
               INV.CRDT_CRD_EXP_MO_CD || '/' || INV.CRDT_CRD_EXP_YR_CD
                  CREDIT_CARD_EXPIRATION,
               ASTX.attrib_04 ENTITLEMENT_OFFERING_CODE,
               CON.email_addr CONTACT_EMAIL,
               AST.status_cd ENTITLEMENT_STATE,
               CON.FST_NAME || ' ' || CON.LAST_NAME CONTACT_NAME,
               DR.CRIS_AGREEMENT_NUMBER AGREE_INFO_SUB_NBR,
                DR.CRIS_AGREEMENT_NUMBER SUBSCRIPTION_NUMBER,
               DR.CRIS_AGREEMENT_END_DATE SUBSCRIPTION_END_DATE,
               DR.BATCH_ID BATCH_ID,
               ADDR.zipcode BILLING_ZIP_CODE,
               DR.CANCELLATION_REASON CANCELLATION_REASON,
               DR.ENTITLEMENT_UNIT_STATUS ENTITLEMENT_UNIT_STATUS,
               DR.SERVICE_KEY SERVICE_KEY,
               DR.EXTENSION_KEY EXTENSION_KEY,
               DR.FEIN FED_TAX_ID,
               DR.LAST_VALIDATION_DATE LAST_VALIDATION_DATE,
               spi.part_num EC_ASSET_ITEM_NUM,
               saxa.char_val EC_EDITION_TYPE,
               saxa1.char_val EC_NUM_EMPL_TYPE,
               ROW_NUMBER ()
               OVER (PARTITION BY DR.CRIS_AGREEMENT_NUMBER, DR.FEIN
                     ORDER BY DR.BATCH_ID DESC)
                  RN
          FROM SIEBEL.S_ORDER@SIEBEL_PROD ORD,
               SIEBEL.S_ORDER_X@SIEBEL_PROD ORDX,
               SIEBEL.S_ORDER_ITEM@SIEBEL_PROD ORDITM,
               SIEBEL.S_ORDER_ITEM_OM@SIEBEL_PROD ORDITMO,
               SIEBEL.S_ASSET_X@SIEBEL_PROD ASTX,
               SIEBEL.S_ASSET@SIEBEL_PROD AST,
               SIEBEL.S_ORG_EXT@SIEBEL_PROD ORG,
               SIEBEL.S_INV_PROF@SIEBEL_PROD INV,
               SIEBEL.S_CONTACT@SIEBEL_PROD CON,
               SIEBEL.S_ADDR_PER@SIEBEL_PROD ADDR,
               DE_PSP_DRIVER@DRIVER_LNK DR,
               siebel.s_prod_int@SIEBEL_PROD spi,
               siebel.s_asset_xa@SIEBEL_PROD saxa,
               siebel.s_asset_xa@SIEBEL_PROD saxa1
         WHERE     ORD.ROW_ID = ORDX.PAR_ROW_ID
               AND ORD.ROW_ID = ORDITM.ORDER_ID
               AND ORDITM.ASSET_INTEG_ID = AST.INTEGRATION_ID
               AND ORDITM.ROW_ID = ORDITMO.PAR_ROW_ID
               AND AST.owner_con_id = CON.PAR_ROW_ID
               AND AST.BILL_PROFILE_ID = INV.ROW_ID
               AND AST.ROW_ID = ASTX.PAR_ROW_ID
               AND ORG.ROW_ID = AST.BILL_ACCNT_ID
               AND ORG.PR_BL_ADDR_ID = ADDR.ROW_ID
               AND AST.ROW_ID = DR.SIEBEL_ASSET_ROW_ID
               AND ast.prod_id = spi.row_id
               AND ast.row_id = saxa.asset_id
               AND ast.row_id = saxa1.asset_id
               AND saxa.attr_name = 'Edition'
               AND saxa1.attr_name = 'Number of Employees'
               AND DR.status =p_status)
 WHERE RN = '1';
 type   INS_ARR is table of  c1%ROWTYPE  ;
   REC_ARR INS_ARR;

BEGIN
   OPEN C1;
   LOOP
   FETCH C1 BULK COLLECT INTO REC_ARR LIMIT 5000;
   BEGIN
   FORALL I IN 1..REC_ARR.COUNT
   /* Formatted on 9/14/2011 12:06:52 AM (QP5 v5.163.1008.3004) */
INSERT /*+  append*/  INTO TEMP_ENTITLE  VALUES REC_ARR(I) LOG ERRORS REJECT LIMIT UNLIMITED;
   END;
   COMMIT;
   EXIT WHEN REC_ARR.COUNT=0;
   END LOOP;
   CLOSE C1;
end pr_load_temp_entitle;
/
