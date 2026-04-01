--VALIDATE siebel vs temp_entitle and display any differences

SELECT   /*+  DRIVING_SITE (ORD ) DRIVING_SITE(ORDX) DRIVING_SITE(ORDITM) DRIVING_SITE(ORDITMO ) DRIVING_SITE(ASTX ) DRIVING_SITE(AST ) DRIVING_SITE(ORG ) DRIVING_SITE (INV ) DRIVING_SITE(CON) DRIVING_SITE(ADDR) DRIVING_SITE (DR ) 
DRIVING_SITE (spi ) DRIVING_SITE(saxa) DRIVING_SITE(saxa1) */
              ORD.order_num ORDER_NUM_SBL   ,
                  TE.ORDER_NUMBER,
                      ORDX.attrib_37 ORDER_SOURCE_SBL,
                      TE.ORDER_SOURCE,
               AST.ref_number_1 LICENSE_NUMBER_SBL,
               TE.LICENSE_NUMBER,
               ORG.integration_id CUSTOMER_ID_SBL,
               TE.CUSTOMER_ID,
               ORDITMO.service_start_dt NEXT_CHARGE_DATE_SBL,
               TE.NEXT_CHARGE_DATE,
               INV.paymt_meth_cd PAYMENT_METHOD_TYPE_SBL,
               TE.PAYMENT_METHOD_TYPE,
               INV.crdt_crd_appr_cd CREDIT_CARD_TYPE_SBL,
               TE.CREDIT_CARD_TYPE,
               substr(INV.cc_number,-4) CREDIT_CARD_NUMBER_SBL,
               TE.CREDIT_CARD_NUMBER,
               INV.CRDT_CRD_EXP_MO_CD || '/' || INV.CRDT_CRD_EXP_YR_CD
                  CREDIT_CARD_EXPIRATION_SBL,
                  TE.CREDIT_CARD_EXPIRATION,
               ASTX.attrib_04 ENTITLEMENT_OFFERING_CODE_SBL,
               TE.ENTITLEMENT_OFFERING_CODE,
               CON.email_addr CONTACT_EMAIL_SBL,
               TE.CONTACT_EMAIL,
               AST.status_cd ENTITLEMENT_STATE_SBL,
               TE.ENTITLEMENT_STATE,
              CON.FST_NAME || ' ' || CON.LAST_NAME CONTACT_NAME_SBL,
              TE.CONTACT_NAME,
                  ADDR.zipcode BILLING_ZIP_CODE_SBL,
                  TE.BILLING_ZIP_CODE,
               spi.part_num EC_ASSET_ITEM_NUM_SBL,
               TE.EC_ASSET_ITEM_NUM,
               saxa.char_val EC_EDITION_TYPE_SBL,
               TE.EC_EDITION_TYPE,
               saxa1.char_val EC_NUM_EMPL_TYPE_SBL,
               TE.EC_NUM_EMPL_TYPE
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
                siebel.s_prod_int@SIEBEL_PROD spi,
               siebel.s_asset_xa@SIEBEL_PROD saxa,
               siebel.s_asset_xa@SIEBEL_PROD saxa1,
               temp_entitle te
         WHERE     ORD.ROW_ID = ORDX.PAR_ROW_ID
               AND ORD.ROW_ID = ORDITM.ORDER_ID
               AND ORDITM.ASSET_INTEG_ID = AST.INTEGRATION_ID
               AND ORDITM.ROW_ID = ORDITMO.PAR_ROW_ID
               AND AST.owner_con_id = CON.PAR_ROW_ID
               AND AST.BILL_PROFILE_ID = INV.ROW_ID
               AND AST.ROW_ID = ASTX.PAR_ROW_ID
               AND ORG.ROW_ID = AST.BILL_ACCNT_ID
               AND ORG.PR_BL_ADDR_ID = ADDR.ROW_ID
               AND AST.ref_number_1 = te.LICENSE_NUMBER
             and   ASTX.attrib_04 = TE.ENTITLEMENT_OFFERING_CODE
             and  ORD.order_num=TE.ORDER_NUMBER  
                           AND ast.prod_id = spi.row_id
               AND ast.row_id = saxa.asset_id
               AND ast.row_id = saxa1.asset_id
               AND saxa.attr_name = 'Edition'
               AND saxa1.attr_name = 'Number of Employees'
               and ( ORDX.attrib_37 !=TE.ORDER_SOURCE OR 
                              --     ORDITMO.service_start_dt !=TE.NEXT_CHARGE_DATE OR 
               INV.paymt_meth_cd !=TE.PAYMENT_METHOD_TYPE OR
               INV.crdt_crd_appr_cd !=TE.CREDIT_CARD_TYPE OR 
               substr(INV.cc_number,-4) !=TE.CREDIT_CARD_NUMBER OR 
                  ORG.integration_id! =TE.CUSTOMER_ID or
               INV.CRDT_CRD_EXP_MO_CD || '/' || INV.CRDT_CRD_EXP_YR_CD!=TE.CREDIT_CARD_EXPIRATION OR
                             CON.email_addr!=TE.CONTACT_EMAIL OR
               AST.status_cd !=TE.ENTITLEMENT_STATE OR 
              CON.FST_NAME || ' ' || CON.LAST_NAME!=TE.CONTACT_NAME OR
                  ADDR.zipcode !=TE.BILLING_ZIP_CODE OR
               spi.part_num !=TE.EC_ASSET_ITEM_NUM OR 
               saxa.char_val !=TE.EC_EDITION_TYPE OR
               saxa1.char_val !=TE.EC_NUM_EMPL_TYPE);