--VALIDATE COMPANY INFO
SELECT /*+ DRIVING_SITE (DOC ) DRIVING_SITE(ENT) DRIVING_SITE(ENT_X) DRIVING_SITE(PRI ) DRIVING_SITE(ADDR )*/
      DECODE (
          pri.subtype_cd,
          TC.PRICE_TYPE, NULL,
          ' PRICE_TYPE ' || TC.PRICE_TYPE || 'CHANGED TO ' || pri.subtype_cd)
       || DECODE (
             TO_DATE (
                TO_CHAR (TC.AGREE_INFO_AGREE_CREATE_DATE,
                         'MM/DD/YYYY HH12:MI:SS'),
                'MM/DD/YYYY HH12:MI:SS'),
             TO_DATE (TO_CHAR (DOC.CREATED, 'MM/DD/YYYY HH12:MI:SS'),
                      'MM/DD/YYYY HH12:MI:SS'), NULL,
                ' AGREE_INFO_AGREE_CREATE_DATE '
             || TC.AGREE_INFO_AGREE_CREATE_DATE
             || 'CHANGED TO '
             || doc.created)
       || DECODE (
             doc.name,
             TC.AGREE_INFO_NAME, NULL,
                '  AGREE_INFO_NAME  '
             || TC.AGREE_INFO_NAME
             || 'CHANGED TO '
             || doc.name
             || ' ON '
             || DOC.LAST_UPD)
       || DECODE (
             doc.x_service_type,
             TC.AGREE_INFO_SERVICE_TYPE, NULL,
                '  AGREE_INFO_SERVICE_TYPE '
             || TC.AGREE_INFO_SERVICE_TYPE
             || 'CHANGED TO '
             || doc.x_service_type
             || ' ON '
             || DOC.LAST_UPD)
       || DECODE (
             doc.x_sub_num_stage,
             TC.AGREE_INFO_SUB_NBR, NULL,
                ' AGREE_INFO_SUB_NBR '
             || TC.AGREE_INFO_SUB_NBR
             || 'CHANGED TO '
             || doc.x_sub_num_stage
             || ' ON '
             || DOC.LAST_UPD)
       || DECODE (
             doc.row_id,
             TC.AGREE_INFO_SOURCE_ID, NULL,
                '  AGREE_INFO_SOURCE_ID '
             || TC.AGREE_INFO_SOURCE_ID
             || 'CHANGED TO '
             || doc.row_id
             || ' ON '
             || DOC.LAST_UPD)
       || DECODE (
             doc.x_subtype,
             TC.AGREE_INFO_AGREE_SUB_TYPE, NULL,
                '  AGREE_INFO_AGREE_SUB_TYPE '
             || TC.AGREE_INFO_AGREE_SUB_TYPE
             || 'CHANGED TO '
             || doc.x_subtype
             || ' ON '
             || DOC.LAST_UPD)
       || DECODE (
             ent_x.name,
             TC.FED_TAX_ID, NULL,
                ' FED_TAX_ID  '
             || TC.FED_TAX_ID
             || ' CHANGED TO '
             || ent_x.name
             || ' ON '
             || ENT_X.LAST_UPD)
    || DECODE (
             ent_x.x_app_ver_lic,
             TC.QB_INFO_APP_VERSION, NULL,
                ' QB_INFO_APP_VERSION  '
             || TC.QB_INFO_APP_VERSION
             || ' CHANGED TO '
             || ent_x.x_app_ver_lic
             || ' ON '
             || ENT_X.LAST_UPD) 
       || DECODE (
             ent_x.x_attrib_24,
             TC.QB_INFO_LICENSE_NUMBER, NULL,
                ' QB_INFO_LICENSE_NUMBER '
             || TC.QB_INFO_LICENSE_NUMBER
             || ' CHANGED TO '
             || ent_x.x_attrib_24
             || ' ON '
             || ENT_X.LAST_UPD) 
       || DECODE (
             ent_x.x_attrib_02,
             TC.AGREE_INFO_SERVICE_KEY, NULL,
                ' AGREE_INFO_SERVICE_KEY '
             || TC.AGREE_INFO_SERVICE_KEY
             || ' CHANGED TO '
             || ent_x.x_attrib_02
             || ' ON '
             || ENT_X.LAST_UPD)
       || DECODE (
             addr.addr,
             TC.COMP_ADDRESS_LINE1, NULL,
                ' COMP_ADDRESS_LINE1 '
             || TC.COMP_ADDRESS_LINE1
             || ' CHANGED TO '
             || addr.addr
             || ' ON '
             || ADDR.LAST_UPD)
       || DECODE (
             addr.addr_line_2,
             TC.COMP_ADDRESS_LINE2, NULL,
                ' COMP_ADDRESS_LINE2 '
             || TC.COMP_ADDRESS_LINE2
             || ' CHANGED TO '
             || addr.addr_line_2
             || ' ON '
             || ADDR.LAST_UPD)
       || DECODE (
             addr.city,
             TC.COMP_CITY, NULL,
                ' COMP_CITY '
             || TC.COMP_CITY
             || ' CHANGED TO '
             || addr.city
             || ' ON '
             || ADDR.LAST_UPD)
       || DECODE (
             addr.country,
             TC.COMP_COUNTRY, NULL,
                ' COMP_COUNTRY '
             || TC.COMP_COUNTRY
             || ' CHANGED TO '
             || addr.country
             || ' ON '
             || ADDR.LAST_UPD)
       || DECODE (
             addr.state,
             TC.COMP_STATE, NULL,
                ' COMP_STATE '
             || TC.COMP_STATE
             || ' CHANGED TO '
             || addr.state
             || ' ON '
             || ADDR.LAST_UPD)
       || DECODE (
             addr.zipcode,
             TC.COMP_ZIP_CODE, NULL,
                ' COMP_ZIP_CODE '
             || TC.COMP_ZIP_CODE
             || ' CHANGED TO '
             || addr.zipcode
             || ' ON '
             || ADDR.LAST_UPD)
       || ' FOR AGREEMENT# '
       || TC.AGREE_INFO_SUB_NBR
       || ' AND EIN # '
       || TC.FED_TAX_ID
  FROM SIEBEL.S_DOC_AGREE@CRIS_PROD DOC,
  SIEBEL.S_ORG_EXT@CRIS_PROD ORG,
       SIEBEL.S_ENTLMNT@CRIS_PROD ENT,
       SIEBEL.CX_ENTLMNT_XM@CRIS_PROD ENT_X,
       SIEBEL.S_PRI_LST@CRIS_PROD PRI,
       SIEBEL.S_ADDR_ORG@cris_PROD ADDR,
       TEMP_COMPANY TC
 WHERE     DOC.ROW_ID = ENT.PAR_AGREE_ID
       AND ENT.ROW_ID = ENT_X.PAR_ROW_ID
       AND PRI.ROW_ID = DOC.PRI_LST_ID
       AND DOC.TARGET_OU_ID=ORG.ROW_ID
  --     AND DOC.BILL_TO_ADDR_ID = ADDR.ROW_ID
  AND ORG.PR_BL_ADDR_ID=ADDR.ROW_ID 
       AND DOC.ROW_ID = TC.AGREE_INFO_SOURCE_ID
       AND ENT_X.NAME = TC.FED_TAX_ID
       AND tc.batch_id = &&p_batch_id
       AND (TO_DATE (
               TO_CHAR (TC.AGREE_INFO_AGREE_CREATE_DATE,
                        'MM/DD/YYYY HH12:MI:SS'),
               'MM/DD/YYYY HH12:MI:SS') !=
               TO_DATE (TO_CHAR (DOC.CREATED, 'MM/DD/YYYY HH12:MI:SS'),
                        'MM/DD/YYYY HH12:MI:SS')
            OR TC.AGREE_INFO_AGREE_SUB_TYPE != doc.x_subtype
            OR TC.AGREE_INFO_NAME != doc.name
            OR tc.FED_TAX_ID != ent_x.name
            OR tc.PRICE_TYPE != pri.subtype_cd
           OR tc.QB_INFO_APP_VERSION != ent_x.x_app_ver_lic
           OR tc.QB_INFO_LICENSE_NUMBER != ent_x.x_attrib_24
            OR tc.AGREE_INFO_NAME != doc.name
            OR tc.AGREE_INFO_SERVICE_TYPE != doc.x_service_type
            OR tc.AGREE_INFO_SUB_NBR != doc.x_sub_num_stage
            OR tc.AGREE_INFO_SERVICE_KEY != ent_x.x_attrib_02
            OR tc.COMP_ADDRESS_LINE2 != addr.addr_line_2
            OR tc.COMP_ADDRESS_LINE1 != addr.addr
            OR tc.COMP_CITY != addr.city
            OR tc.COMP_COUNTRY != addr.country
            OR tc.COMP_STATE != addr.state
            OR tc.COMP_ZIP_CODE != addr.zipcode);

           
