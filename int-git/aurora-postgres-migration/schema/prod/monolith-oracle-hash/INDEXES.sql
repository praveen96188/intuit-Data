--------------------------------------------------------
--  DDL for Index AS400_DROPME_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."AS400_DROPME_IDX1" ON "PSPADM"."AS400_DROPME" ("PREF_USERID") ;
--------------------------------------------------------
--  DDL for Index BACKUP_SCHEMA_STATS_090911
--------------------------------------------------------

  CREATE INDEX "PSPADM"."BACKUP_SCHEMA_STATS_090911" ON "PSPADM"."BACKUP_SCHEMA_STATS_090911" ("STATID", "TYPE", "C5", "C1", "C2", "C3", "C4", "VERSION") ;
  GRANT DELETE ON "PSPADM"."BACKUP_SCHEMA_STATS_090911" TO "PSPAPP_ROLE";
  GRANT INSERT ON "PSPADM"."BACKUP_SCHEMA_STATS_090911" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_SCHEMA_STATS_090911" TO "PSPAPP_ROLE";
  GRANT UPDATE ON "PSPADM"."BACKUP_SCHEMA_STATS_090911" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_SCHEMA_STATS_090911" TO "PSPREAD_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_SCHEMA_STATS_090911" TO "PSPADM_RO_AWS";
--------------------------------------------------------
--  DDL for Index BACKUP_STATS
--------------------------------------------------------

  CREATE INDEX "PSPADM"."BACKUP_STATS" ON "PSPADM"."BACKUP_STATS" ("STATID", "TYPE", "C5", "C1", "C2", "C3", "C4", "VERSION") ;
  GRANT SELECT ON "PSPADM"."BACKUP_STATS" TO "PSPREAD_ROLE";
  GRANT UPDATE ON "PSPADM"."BACKUP_STATS" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_STATS" TO "PSPAPP_ROLE";
  GRANT INSERT ON "PSPADM"."BACKUP_STATS" TO "PSPAPP_ROLE";
  GRANT DELETE ON "PSPADM"."BACKUP_STATS" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_STATS" TO "PSPADM_RO_AWS";
--------------------------------------------------------
--  DDL for Index BACKUP_STATS_032911
--------------------------------------------------------

  CREATE INDEX "PSPADM"."BACKUP_STATS_032911" ON "PSPADM"."BACKUP_STATS_032911" ("STATID", "TYPE", "C5", "C1", "C2", "C3", "C4", "VERSION") ;
  GRANT SELECT ON "PSPADM"."BACKUP_STATS_032911" TO "PSPREAD_ROLE";
  GRANT UPDATE ON "PSPADM"."BACKUP_STATS_032911" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_STATS_032911" TO "PSPAPP_ROLE";
  GRANT INSERT ON "PSPADM"."BACKUP_STATS_032911" TO "PSPAPP_ROLE";
  GRANT DELETE ON "PSPADM"."BACKUP_STATS_032911" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_STATS_032911" TO "PSPADM_RO_AWS";
--------------------------------------------------------
--  DDL for Index BACKUP_TAB_STATS_090911
--------------------------------------------------------

  CREATE INDEX "PSPADM"."BACKUP_TAB_STATS_090911" ON "PSPADM"."BACKUP_TAB_STATS_090911" ("STATID", "TYPE", "C5", "C1", "C2", "C3", "C4", "VERSION") ;
  GRANT DELETE ON "PSPADM"."BACKUP_TAB_STATS_090911" TO "PSPAPP_ROLE";
  GRANT INSERT ON "PSPADM"."BACKUP_TAB_STATS_090911" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_TAB_STATS_090911" TO "PSPAPP_ROLE";
  GRANT UPDATE ON "PSPADM"."BACKUP_TAB_STATS_090911" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_TAB_STATS_090911" TO "PSPREAD_ROLE";
  GRANT SELECT ON "PSPADM"."BACKUP_TAB_STATS_090911" TO "PSPADM_RO_AWS";
--------------------------------------------------------
--  DDL for Index C_PSP_BATCH_JOB_STATUS1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."C_PSP_BATCH_JOB_STATUS1" ON "PSPADM"."PSP_BATCH_JOB_STATUS" ("JOB_TYPE") ;
--------------------------------------------------------
--  DDL for Index EMP_PREF_UNQ_INDX
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."EMP_PREF_UNQ_INDX" ON "PSPADM"."PSP_PSTUB_EMPLOYEE_PREFERENCE" ("APP_NAME", "PREFERENCE_NAME", "EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index FN_STATE_EFF_DATE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."FN_STATE_EFF_DATE" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" (TRUNC("TRANSACTION_STATE_EFF_DATE")) TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index IDX_ENTITYUPDATE_CRDATE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."IDX_ENTITYUPDATE_CRDATE" ON "PSPADM"."PSP_ENTITY_UPDATE" ("CREATED_DATE") TABLESPACE "PSP_IDX01" LOCAL
 (PARTITION "ENTITY_UPDATE_M102022" NOCOMPRESS , 
 PARTITION "ENTITY_UPDATE_M112022" NOCOMPRESS , 
 PARTITION "ENTITY_UPDATE_M122022" NOCOMPRESS , 
 PARTITION "ENTITY_UPDATE_M012023" NOCOMPRESS ) ;
--------------------------------------------------------
--  DDL for Index PAYCHECK_DDEMPFKCRDATE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PAYCHECK_DDEMPFKCRDATE" ON "PSPADM"."PSP_PAYCHECK" ("D_D_EMPLOYEE_FK", "CREATED_DATE") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PAYCHECK_SRCEMPFKCRDATE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PAYCHECK_SRCEMPFKCRDATE" ON "PSPADM"."PSP_PAYCHECK" ("SOURCE_EMPLOYEE_FK", "CREATED_DATE") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_ACHENROLLMENT_DETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ACHENROLLMENT_DETAIL_FK1" ON "PSPADM"."PSP_ACHENROLLMENT_DETAIL" ("A_C_H_ENROLLMENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ACHENROLLMENT_DETAIL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ACHENROLLMENT_DETAIL_FK2" ON "PSPADM"."PSP_ACHENROLLMENT_DETAIL" ("RESPONSE_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ACHENROLLMENT_DETAIL_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ACHENROLLMENT_DETAIL_FK3" ON "PSPADM"."PSP_ACHENROLLMENT_DETAIL" ("REQUEST_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ACHENROLLMENT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ACHENROLLMENT_FK2" ON "PSPADM"."PSP_ACHENROLLMENT" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ADDITIONAL_FILING_AMOU_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ADDITIONAL_FILING_AMOU_FK1" ON "PSPADM"."PSP_ADDITIONAL_FILING_AMOUNT" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ADE_LAW_MAP_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ADE_LAW_MAP_FK1" ON "PSPADM"."PSP_ADE_LAW_MAP" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ADE_LAW_MAP_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ADE_LAW_MAP_FK2" ON "PSPADM"."PSP_ADE_LAW_MAP" ("ADE_LAW_MAP_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_AGENCY_AGENCYIDENC_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_AGENCY_AGENCYIDENC_I1" ON "PSPADM"."PSP_AGENCY" ("AGENCY_ID_ENC") ;
--------------------------------------------------------
--  DDL for Index PSP_AGENCY_CHECK_BATCH_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_AGENCY_CHECK_BATCH_FK2" ON "PSPADM"."PSP_AGENCY_CHECK_BATCH" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_AGENCY_ID_REQUIREMENT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_AGENCY_ID_REQUIREMENT_FK2" ON "PSPADM"."PSP_AGENCY_ID_REQUIREMENT" ("PAYMENT_TEMPLATE_AGENCY_ID_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_AGENCY_RATE_REQUEST_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_AGENCY_RATE_REQUEST_FK1" ON "PSPADM"."PSP_AGENCY_RATE_REQUEST" ("AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ANNUALBILLINGITEM_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ANNUALBILLINGITEM_FK1" ON "PSPADM"."PSP_ANNUAL_BILLING_ITEM" ("ANNUAL_BILLING_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ANNUALBILLINGITEM_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ANNUALBILLINGITEM_FK2" ON "PSPADM"."PSP_ANNUAL_BILLING_ITEM" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ASSISTEDBUNDLEBILL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ASSISTEDBUNDLEBILL_FK1" ON "PSPADM"."PSP_ASSISTED_BUNDLE_BILL" ("ASST_BUNDLE_COMP_USAGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ASSTBUNDLEBILLDETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ASSTBUNDLEBILLDETAIL_FK1" ON "PSPADM"."PSP_ASST_BUNDLE_BILL_DETAIL" ("ASSISTED_BUNDLE_BILL_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ATFDATA_EXTRACT_FILE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ATFDATA_EXTRACT_FILE_FK1" ON "PSPADM"."PSP_ATFDATA_EXTRACT_FILE" ("A_T_F_DATA_EXTRACT_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ATFPAYMENTS_TO_PROCESS_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ATFPAYMENTS_TO_PROCESS_FK1" ON "PSPADM"."PSP_ATFPAYMENTS_TO_PROCESS" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ATFPAYMENTS_TO_PROCESS_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ATFPAYMENTS_TO_PROCESS_FK2" ON "PSPADM"."PSP_ATFPAYMENTS_TO_PROCESS" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ATFPAYMENTS_TO_PROCESS_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ATFPAYMENTS_TO_PROCESS_FK3" ON "PSPADM"."PSP_ATFPAYMENTS_TO_PROCESS" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ATFPAYROLLS_TO_PROCESS_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ATFPAYROLLS_TO_PROCESS_FK1" ON "PSPADM"."PSP_ATFPAYROLLS_TO_PROCESS" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ATFPAYROLLS_TO_PROCESS_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ATFPAYROLLS_TO_PROCESS_I1" ON "PSPADM"."PSP_ATFPAYROLLS_TO_PROCESS" ("MODIFIED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_AUTHROLE_OPT_FK_OPT
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_AUTHROLE_OPT_FK_OPT" ON "PSPADM"."PSP_AUTHROLE_OPERATION_ASSOC" ("AUTH_OPERATION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_AUTH_ROLE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_AUTH_ROLE_FK1" ON "PSPADM"."PSP_AUTH_ROLE" ("AUTH_DOMAIN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_AUTH_USER_CORPID
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_AUTH_USER_CORPID" ON "PSPADM"."PSP_AUTH_USER" ("CORP_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_BANK_ACCOUNT_ENC_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BANK_ACCOUNT_ENC_I1" ON "PSPADM"."PSP_BANK_ACCOUNT" ("ACCOUNT_NUMBER_ENC") ;
--------------------------------------------------------
--  DDL for Index PSP_BANK_ACCOUNT_MOODDATE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BANK_ACCOUNT_MOODDATE" ON "PSPADM"."PSP_BANK_ACCOUNT" ("MODIFIED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_BATCH_JOB_AUDIT_LOG_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BATCH_JOB_AUDIT_LOG_I1" ON "PSPADM"."PSP_BATCH_JOB_AUDIT_LOG" ("CREATED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_BATCH_JOB_AUDIT_LOG_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BATCH_JOB_AUDIT_LOG_IDX1" ON "PSPADM"."PSP_BATCH_JOB_AUDIT_LOG" ("JOB_NAMESPACE", "IS_VERIFIED") ;
--------------------------------------------------------
--  DDL for Index PSP_BATCH_JOB_PARAMETER_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BATCH_JOB_PARAMETER_FK1" ON "PSPADM"."PSP_BATCH_JOB_PARAMETER" ("BATCH_JOB_SETUP_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILLING_DETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BILLING_DETAIL_FK1" ON "PSPADM"."PSP_BILLING_DETAIL" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILLING_DETAIL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BILLING_DETAIL_FK2" ON "PSPADM"."PSP_BILLING_DETAIL" ("OFFERING_SVCCHG_PRICE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BILL_FK1" ON "PSPADM"."PSP_BILL" ("COMPANY_USAGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILL_PAYMENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BILL_PAYMENT_FK1" ON "PSPADM"."PSP_BILL_PAYMENT" ("PAYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILL_PAYMENT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BILL_PAYMENT_FK2" ON "PSPADM"."PSP_BILL_PAYMENT" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILL_PAYMENT_SPLIT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BILL_PAYMENT_SPLIT_FK1" ON "PSPADM"."PSP_BILL_PAYMENT_SPLIT" ("BILL_PAYMENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILL_PAYMENT_SPLIT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_BILL_PAYMENT_SPLIT_FK2" ON "PSPADM"."PSP_BILL_PAYMENT_SPLIT" ("PAYEE_BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_BILL_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_BILL_U1" ON "PSPADM"."PSP_BILL" ("COMPANY_USAGE_FK", "BILL_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_CHECKPRINTPAYCHECK_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_CHECKPRINTPAYCHECK_FK2" ON "PSPADM"."PSP_CHECK_PRINT_PAYCHECK" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_CHECK_PRINT_BATCH_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_CHECK_PRINT_BATCH_FK1" ON "PSPADM"."PSP_CHECK_PRINT_BATCH" ("RECON_PLUS_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_CHECK_PRINT_BATCH_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_CHECK_PRINT_BATCH_FK2" ON "PSPADM"."PSP_CHECK_PRINT_BATCH" ("POSITIVE_PAY_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_CHECK_PRINT_PAYCHECK_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_CHECK_PRINT_PAYCHECK_FK1" ON "PSPADM"."PSP_CHECK_PRINT_PAYCHECK" ("COMPANY_PAYCHECK_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_CHECK_PRINT_SIGNATURE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_CHECK_PRINT_SIGNATURE_FK1" ON "PSPADM"."PSP_CHECK_PRINT_SIGNATURE" ("SOURCESYS_PRINTEDCHK_INFO_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANYFILINGAMOUNT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANYFILINGAMOUNT_FK1" ON "PSPADM"."PSP_COMPANY_FILING_AMOUNT" ("COMPANY_AGENCY_PMT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANYNOTE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANYNOTE_FK1" ON "PSPADM"."PSP_COMPANY_NOTE" ("COMPANY_FK","COMPANY_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANYPAYMENTTEMPLATE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANYPAYMENTTEMPLATE_FK1" ON "PSPADM"."PSP_COMP_PMTTEMPLATE_PMTMETHOD" ("COMPANY_AGENCY_PMT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANYPAYMENTTEMPLATE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANYPAYMENTTEMPLATE_FK2" ON "PSPADM"."PSP_COMP_PMT_TEMPLATE_AGENCYID" ("COMPANY_AGENCY_PMT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_ADDITIONAL_INF_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_ADDITIONAL_INF_FK1" ON "PSPADM"."PSP_COMPANY_ADDITIONAL_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_ADDITIONAL_INF_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_ADDITIONAL_INF_FK2" ON "PSPADM"."PSP_COMPANY_ADDITIONAL_INFO" ("INDUSTRY_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_ADDITIONAL_INF_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_ADDITIONAL_INF_FK3" ON "PSPADM"."PSP_COMPANY_ADDITIONAL_INFO" ("OWNERSHIP_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_ADJUSTMENT_SUB_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_ADJUSTMENT_SUB_FK1" ON "PSPADM"."PSP_COMP_ADJUST_SUBMISSION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_ADJUSTMENT_SUB_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_ADJUSTMENT_SUB_FK3" ON "PSPADM"."PSP_COMP_ADJUST_SUBMISSION" ("VOID_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_ADJUSTMENT_SUB_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_ADJUSTMENT_SUB_FK4" ON "PSPADM"."PSP_COMP_ADJUST_SUBMISSION" ("ORIGINAL_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_AGENCY_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_AGENCY_FK1" ON "PSPADM"."PSP_COMPANY_AGENCY" ("AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_AGENCY_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_AGENCY_FK2" ON "PSPADM"."PSP_COMPANY_AGENCY" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_AGENCY_FORM_TE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_AGENCY_FORM_TE_FK1" ON "PSPADM"."PSP_COMPANYAGENCY_FRMTEMPLATE" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_AGENCY_FORM_TE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_AGENCY_FORM_TE_FK2" ON "PSPADM"."PSP_COMPANYAGENCY_FRMTEMPLATE" ("FORM_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_AGENCY_PAYMENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_AGENCY_PAYMENT_FK1" ON "PSPADM"."PSP_COMPANYAGENCY_PMTTEMPLATE" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_AGENCY_PAYMENT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_AGENCY_PAYMENT_FK2" ON "PSPADM"."PSP_COMPANYAGENCY_PMTTEMPLATE" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_BANK_ACCOUNT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_BANK_ACCOUNT_FK1" ON "PSPADM"."PSP_COMPANY_BANK_ACCOUNT" ("BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_BANK_ACCOUNT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_BANK_ACCOUNT_FK2" ON "PSPADM"."PSP_COMPANY_BANK_ACCOUNT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_BANK_ACCOUNT_U1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_BANK_ACCOUNT_U1" ON "PSPADM"."PSP_COMPANY_BANK_ACCOUNT" ("SOURCE_BANK_ACCOUNT_ID", "COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_DAILY_LIABILIT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_DAILY_LIABILIT_FK1" ON "PSPADM"."PSP_COMPANY_DAILY_LIABILITY" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_DAILY_LIABILIT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_DAILY_LIABILIT_FK2" ON "PSPADM"."PSP_COMPANY_DAILY_LIABILITY" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_DETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_DETAIL_FK1" ON "PSPADM"."PSP_COMPANY_EVENT_DETAIL" ("COMPANY_FK","COMPANY_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_DETAIL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_DETAIL_FK2" ON "PSPADM"."PSP_COMPANY_EVENT_DETAIL" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_DETAIL_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_DETAIL_I2" ON "PSPADM"."PSP_COMPANY_EVENT_DETAIL" ("EVENT_DETAIL_TYPE_CD", "COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_EMAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_EMAIL_FK1" ON "PSPADM"."PSP_COMPANY_EVENT_EMAIL" ("COMPANY_FK","COMPANY_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_EMAIL_PA_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_EMAIL_PA_FK1" ON "PSPADM"."PSP_COMPANY_EVENT_EMAIL_PARAM" ("COMPANY_EVENT_EMAIL_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_EMAIL_PA_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_EMAIL_PA_FK2" ON "PSPADM"."PSP_COMPANY_EVENT_EMAIL_PARAM" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_FK1" ON "PSPADM"."PSP_COMPANY_EVENT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_I1" ON "PSPADM"."PSP_COMPANY_EVENT" ("COMPANY_FK", "EVENT_TYPE_CD") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_I2" ON "PSPADM"."PSP_COMPANY_EVENT" ("EVENT_TIME_STAMP") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_EVENT_I3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_EVENT_I3" ON "PSPADM"."PSP_COMPANY_EVENT" ("COMPANY_FK", "EVENT_TIME_STAMP") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_FEDTAXIDENC_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_FEDTAXIDENC_I1" ON "PSPADM"."PSP_COMPANY" ("FED_TAX_ID_ENC") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_FK2" ON "PSPADM"."PSP_COMPANY" ("PAYROLL_FREQUENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_FK3" ON "PSPADM"."PSP_COMPANY" ("MAILING_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_FK4" ON "PSPADM"."PSP_COMPANY" ("LEGAL_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_FK5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_FK5" ON "PSPADM"."PSP_COMPANY" ("FUNDING_MODEL_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_FK7
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_FK7" ON "PSPADM"."PSP_COMPANY" ("ANNUAL_BILLING_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_FK8
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_FK8" ON "PSPADM"."PSP_COMPANY" ("OFFLOAD_GROUP_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_I1" ON "PSPADM"."PSP_COMPANY" (LOWER("SOURCE_COMPANY_ID")) ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_I5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_I5" ON "PSPADM"."PSP_COMPANY" (LOWER("LEGAL_NAME")) ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_I6
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_I6" ON "PSPADM"."PSP_COMPANY" ("COMPLIANCE_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_LAW_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_LAW_FK1" ON "PSPADM"."PSP_COMPANY_LAW" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_LAW_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_LAW_FK2" ON "PSPADM"."PSP_COMPANY_LAW" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_LAW_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_LAW_FK4" ON "PSPADM"."PSP_COMPANY_LAW" ("ADDITIONAL_COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_LAW_RATE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_LAW_RATE_FK1" ON "PSPADM"."PSP_COMPANY_LAW_RATE" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_NOTE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_NOTE_FK1" ON "PSPADM"."PSP_COMPANY_NOTE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_OFFERING_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_OFFERING_FK1" ON "PSPADM"."PSP_COMPANY_OFFERING" ("OFFERING_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_OFFERING_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_OFFERING_FK2" ON "PSPADM"."PSP_COMPANY_OFFERING" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_OFFER_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_OFFER_FK1" ON "PSPADM"."PSP_COMPANY_OFFER" ("OFFER_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_OFFER_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_OFFER_FK2" ON "PSPADM"."PSP_COMPANY_OFFER" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_PAYCHECK_BATCH_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_PAYCHECK_BATCH_FK2" ON "PSPADM"."PSP_COMPANY_PAYCHECK_BATCH" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_PAYROLL_ITEM_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_PAYROLL_ITEM_FK1" ON "PSPADM"."PSP_COMPANY_PAYROLL_ITEM" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_PAYROLL_ITEM_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_PAYROLL_ITEM_FK2" ON "PSPADM"."PSP_COMPANY_PAYROLL_ITEM" ("PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_PAYROLL_ITEM_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_PAYROLL_ITEM_FK4" ON "PSPADM"."PSP_COMPANY_PAYROLL_ITEM" ("ADDITIONAL_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_PIN_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_PIN_FK1" ON "PSPADM"."PSP_COMPANY_PIN" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_RATE_REQUEST_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_RATE_REQUEST_FK1" ON "PSPADM"."PSP_COMPANY_RATE_REQUEST" ("AGENCY_RATE_REQUEST_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_RATE_REQUEST_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_RATE_REQUEST_FK2" ON "PSPADM"."PSP_COMPANY_RATE_REQUEST" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_SERVICE_BANK_A_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_SERVICE_BANK_A_FK1" ON "PSPADM"."PSP_COMPANY_SERVICE_BANK_ACCT" ("COMPANY_SERVICE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_SERVICE_BANK_A_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_SERVICE_BANK_A_FK2" ON "PSPADM"."PSP_COMPANY_SERVICE_BANK_ACCT" ("COMPANY_BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_SERVICE_BANK_A_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_SERVICE_BANK_A_FK3" ON "PSPADM"."PSP_COMPANY_SERVICE_BANK_ACCT" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_SERVICE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_SERVICE_FK1" ON "PSPADM"."PSP_COMPANY_SERVICE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_SERVICE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_SERVICE_FK2" ON "PSPADM"."PSP_COMPANY_SERVICE" ("SERVICE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_SERVICE_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_SERVICE_FK3" ON "PSPADM"."PSP_COMPANY_SERVICE" ("FUNDING_MODEL_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_SERVICE_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_SERVICE_IDX1" ON "PSPADM"."PSP_COMPANY_SERVICE" ("STATUS_CD") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_TFSSUBMISSION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPANY_TFSSUBMISSION_FK1" ON "PSPADM"."PSP_COMPANY_TFSSUBMISSION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_COMPANY_U1" ON "PSPADM"."PSP_COMPANY" ("SOURCE_COMPANY_ID", "SOURCE_SYSTEM_CD") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPANY_USAGE_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_COMPANY_USAGE_U1" ON "PSPADM"."PSP_COMPANY_USAGE" ("SOURCE_COMPANY_ID", "SOURCE_SYSTEM_CD", "LICENSE_ID", "ENTITLEMENT_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPENSATION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPENSATION_FK1" ON "PSPADM"."PSP_COMPENSATION" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPENSATION_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPENSATION_FK2" ON "PSPADM"."PSP_COMPENSATION" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMPENSATION_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMPENSATION_FK4" ON "PSPADM"."PSP_COMPENSATION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_COMP_IAM_REALMID_IDX
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_COMP_IAM_REALMID_IDX" ON "PSPADM"."PSP_COMPANY" ("I_A_M_REALM_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_CONTACT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_CONTACT_FK2" ON "PSPADM"."PSP_CONTACT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DDCOMPANY_SERVICE_INFO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DDCOMPANY_SERVICE_INFO_FK2" ON "PSPADM"."PSP_DDCOMPANY_SERVICE_INFO" ("OFFLOAD_GROUP_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DEDUCTION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DEDUCTION_FK1" ON "PSPADM"."PSP_DEDUCTION" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DEDUCTION_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DEDUCTION_FK2" ON "PSPADM"."PSP_DEDUCTION" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DEDUCTION_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DEDUCTION_FK4" ON "PSPADM"."PSP_DEDUCTION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DEPOSITFREQUENCYFILERE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DEPOSITFREQUENCYFILERE_FK1" ON "PSPADM"."PSP_DEPOSIT_FREQUENCY_FILE_REC" ("DEPOSIT_FREQUENCY_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DEPOSIT_FREQUENCY_FILE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DEPOSIT_FREQUENCY_FILE_FK1" ON "PSPADM"."PSP_DEPOSIT_FREQUENCY_FILE_REC" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DICRFILE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DICRFILE_FK1" ON "PSPADM"."PSP_DICRFILE" ("N_A_C_H_A_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DISBURSE_ADVICE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DISBURSE_ADVICE_FK1" ON "PSPADM"."PSP_DISBURSE_ADVICE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DISBURSE_ADVICE_TAX_LI_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DISBURSE_ADVICE_TAX_LI_FK1" ON "PSPADM"."PSP_DISBURSE_ADVICE_TAX_LIAB" ("DISBURSE_ADVICE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DISBURSE_ADVICE_TAX_LI_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DISBURSE_ADVICE_TAX_LI_FK2" ON "PSPADM"."PSP_DISBURSE_ADVICE_TAX_LIAB" ("COMPANY_FK","TIPS_LIABILITY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_DISBURSE_ADVICE_TAX_LI_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_DISBURSE_ADVICE_TAX_LI_FK3" ON "PSPADM"."PSP_DISBURSE_ADVICE_TAX_LIAB" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EDI_PAYMENT_DETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EDI_PAYMENT_DETAIL_FK1" ON "PSPADM"."PSP_EDI_PAYMENT_DETAIL" ("PARENT_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EDI_PAYMENT_DETAIL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EDI_PAYMENT_DETAIL_FK2" ON "PSPADM"."PSP_EDI_PAYMENT_DETAIL" ("RESPONSE_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EDI_PAYMENT_DETAIL_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EDI_PAYMENT_DETAIL_FK3" ON "PSPADM"."PSP_EDI_PAYMENT_DETAIL" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFFECTIVE_DEPOSIT_FREQ_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFFECTIVE_DEPOSIT_FREQ_FK1" ON "PSPADM"."PSP_EFFECTIVE_DEPOSIT_FREQ" ("COMPANY_AGENCY_PMT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFFECTIVE_DEPOSIT_FREQ_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFFECTIVE_DEPOSIT_FREQ_FK2" ON "PSPADM"."PSP_EFFECTIVE_DEPOSIT_FREQ" ("PAYMENT_TEMPLATE_FREQUENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_ENROLLMENT_DETAI_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_ENROLLMENT_DETAI_FK1" ON "PSPADM"."PSP_EFTPS_ENROLLMENT_DETAIL" ("EFTPS_ENROLLMENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_ENROLLMENT_DETAI_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_ENROLLMENT_DETAI_FK2" ON "PSPADM"."PSP_EFTPS_ENROLLMENT_DETAIL" ("PARENT_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_ENROLLMENT_DETAI_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_ENROLLMENT_DETAI_FK3" ON "PSPADM"."PSP_EFTPS_ENROLLMENT_DETAIL" ("RESPONSE_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_ENROLLMENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_ENROLLMENT_FK1" ON "PSPADM"."PSP_EFTPS_ENROLLMENT" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_FILE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_FILE_FK1" ON "PSPADM"."PSP_EFTPS_FILE" ("ACK_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_FK1" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("PARENT_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_FK2" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("RETURN_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_FK3" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("RESPONSE_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_FK4" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_I1" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("PARENT_FILE_FK", "STATUS_CD", "MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_I2" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("TRANSACTION_SET_ID", "TRANSACTION_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_I3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_I3" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("EFT_TRANSACTION_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_EFTPS_PAYMENT_DETAIL_I4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL_I4" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("TRANSACTION_ID", "AGENCY_PAYMENT_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEEUSAGE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEEUSAGE_FK1" ON "PSPADM"."PSP_EMPLOYEE_USAGE" ("USAGE_PERIOD_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_ACCRUAL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_ACCRUAL_FK1" ON "PSPADM"."PSP_EMPLOYEE_ACCRUAL" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_BANK_ACCOUNT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT_FK1" ON "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT" ("BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_BANK_ACCOUNT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT_FK2" ON "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_BANK_ACCOUNT_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT_I1" ON "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT" ("MODIFIED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_BANK_ACCOUNT_U1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT_U1" ON "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT" ("SOURCE_BANK_ACCOUNT_ID", "EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_CUSTOM_FIELD_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_CUSTOM_FIELD_FK1" ON "PSPADM"."PSP_EMPLOYEE_CUSTOM_FIELD" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_FK2" ON "PSPADM"."PSP_EMPLOYEE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_LAW_QTR_TOTAL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTAL_FK1" ON "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTALS" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_LAW_QTR_TOTAL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTAL_FK2" ON "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTALS" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_LAW_QTR_TOTAL_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTAL_FK3" ON "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTALS" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_LAW_QTR_TOTAL_FK6
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTAL_FK6" ON "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTALS" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_PAYROLL_ITEM_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_PAYROLL_ITEM_FK1" ON "PSPADM"."PSP_EMPLOYEE_PAYROLL_ITEM" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_PAYROLL_ITEM_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_PAYROLL_ITEM_FK2" ON "PSPADM"."PSP_EMPLOYEE_PAYROLL_ITEM" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_PAYROLL_ITEM__FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_PAYROLL_ITEM__FK1" ON "PSPADM"."PSP_EE_PAYROLLITEM_QTRTOTALS" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_PAYROLL_ITEM__FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_PAYROLL_ITEM__FK2" ON "PSPADM"."PSP_EE_PAYROLLITEM_QTRTOTALS" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_TAXIDENC_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_TAXIDENC_I1" ON "PSPADM"."PSP_EMPLOYEE" ("TAX_ID_ENC") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_TAX_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_TAX_FK1" ON "PSPADM"."PSP_EMPLOYEE_TAX" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_TAX_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_TAX_FK2" ON "PSPADM"."PSP_EMPLOYEE_TAX" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_USAGE_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_EMPLOYEE_USAGE_U1" ON "PSPADM"."PSP_EMPLOYEE_USAGE" ("USAGE_PERIOD_FK", "SOURCE_EMPLOYEE_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_W2_TOTALS_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_W2_TOTALS_FK1" ON "PSPADM"."PSP_EMPLOYEE_W2_TOTALS" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_W2_TOTALS_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_W2_TOTALS_FK2" ON "PSPADM"."PSP_EMPLOYEE_W2_TOTALS" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_W2_TOTALS_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_W2_TOTALS_FK3" ON "PSPADM"."PSP_EMPLOYEE_W2_TOTALS" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_W2_TOTALS_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_W2_TOTALS_FK4" ON "PSPADM"."PSP_EMPLOYEE_W2_TOTALS" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYEE_WAGE_PLAN_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYEE_WAGE_PLAN_FK1" ON "PSPADM"."PSP_EMPLOYEE_WAGE_PLAN" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYERCONTRIBUTION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYERCONTRIBUTION_FK1" ON "PSPADM"."PSP_EMPLOYER_CONTRIBUTION" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYERCONTRIBUTION_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYERCONTRIBUTION_FK2" ON "PSPADM"."PSP_EMPLOYER_CONTRIBUTION" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYER_CONTRIBUTION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYER_CONTRIBUTION_FK1" ON "PSPADM"."PSP_EMPLOYER_CONTRIBUTION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMPLOYER_PREFERENCE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMPLOYER_PREFERENCE_FK1" ON "PSPADM"."PSP_EMPLOYER_PREFERENCE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMP_COMP_FK_SRC_EMP_ID
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMP_COMP_FK_SRC_EMP_ID" ON "PSPADM"."PSP_EMPLOYEE" ("COMPANY_FK", "SOURCE_EMPLOYEE_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_EMP_CONS_REALMID_IDX
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMP_CONS_REALMID_IDX" ON "PSPADM"."PSP_EMPLOYEE" ("CONSUMER_REALM_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_EMP_TOTALS_PAYROLL_RUN_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMP_TOTALS_PAYROLL_RUN_FK1" ON "PSPADM"."PSP_EMP_TOTALS_PAYROLL_RUN" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EMP_TOTALS_PAYROLL_RUN_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EMP_TOTALS_PAYROLL_RUN_FK2" ON "PSPADM"."PSP_EMP_TOTALS_PAYROLL_RUN" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ENMT_FEDTAXIDENC_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENMT_FEDTAXIDENC_I1" ON "PSPADM"."PSP_ENTITLEMENT_UNIT" ("FED_TAX_ID_ENC") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENTUNIT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENTUNIT_FK1" ON "PSPADM"."PSP_ENTITLEMENT_UNIT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_CODE_OFFER_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_CODE_OFFER_FK1" ON "PSPADM"."PSP_ENTITLEMENT_CODE_OFFERING" ("ENTITLEMENT_CODE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_CODE_OFFER_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_CODE_OFFER_FK2" ON "PSPADM"."PSP_ENTITLEMENT_CODE_OFFERING" ("OFFERING_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_FK1" ON "PSPADM"."PSP_ENTITLEMENT" ("ENTITLEMENT_CODE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_IDX2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_IDX2" ON "PSPADM"."PSP_ENTITLEMENT" ("CUSTOMER_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_IDX3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_IDX3" ON "PSPADM"."PSP_ENTITLEMENT" ("BILLING_REALM_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_MESSAGE_U1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_MESSAGE_U1" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ("TOKEN") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_MSG_LCNO_EOFCD
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_MSG_LCNO_EOFCD" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ("LICENSE_NUMBER", "ENTITLEMENT_OFFERING_CODE") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_MSG_MSGTS_ER
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_MSG_MSGTS_ER" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ("MESSAGE_TIMESTAMP", "EVENT_REASON") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_MSG_ORNO_LCNO
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_MSG_ORNO_LCNO" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ("ORDER_NUMBER", "LICENSE_NUMBER") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_SUBSNO
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_SUBSNO" ON "PSPADM"."PSP_ENTITLEMENT" ("SUBSCRIPTION_NUMBER") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_ENTITLEMENT_U1" ON "PSPADM"."PSP_ENTITLEMENT" ("LICENSE_NUMBER", "ENTITLEMENT_OFFERING_CODE") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_UNIT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_UNIT_FK1" ON "PSPADM"."PSP_ENTITLEMENT_UNIT" ("ENTITLEMENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_UNIT_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_UNIT_I1" ON "PSPADM"."PSP_ENTITLEMENT_UNIT" ("ENTITLEMENT_UNIT_STATUS", "CREATED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITLEMENT_UNIT_IDX2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITLEMENT_UNIT_IDX2" ON "PSPADM"."PSP_ENTITLEMENT_UNIT" ("SERVICE_KEY") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITYCHANGE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITYCHANGE_FK1" ON "PSPADM"."PSP_ENTITY_CHANGE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTITY_UPDATE_U1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTITY_UPDATE_U1" ON "PSPADM"."PSP_ENTITY_UPDATE_HIST" ("CREATED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_ENTRY_DETAIL_RECORD_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTRY_DETAIL_RECORD_FK1" ON "PSPADM"."PSP_ENTRY_DETAIL_RECORD" ("INTUIT_BANK_ACCOUNT_FK") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_ENTRY_DETAIL_RECORD_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTRY_DETAIL_RECORD_FK2" ON "PSPADM"."PSP_ENTRY_DETAIL_RECORD" ("N_A_C_H_A_FILE_FK", "INTUIT_BANK_ACCOUNT_FK") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_ENTRY_DETAIL_RECORD_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTRY_DETAIL_RECORD_FK3" ON "PSPADM"."PSP_ENTRY_DETAIL_RECORD" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_ENTRY_DETAIL_RECORD_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTRY_DETAIL_RECORD_FK4" ON "PSPADM"."PSP_ENTRY_DETAIL_RECORD" ("COMPANY_FK") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_ENTRY_DETAIL_RECORD_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTRY_DETAIL_RECORD_I1" ON "PSPADM"."PSP_ENTRY_DETAIL_RECORD" ("TRACE_NUMBER") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_ENTRY_DETAIL_RECORD_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ENTRY_DETAIL_RECORD_I2" ON "PSPADM"."PSP_ENTRY_DETAIL_RECORD" ("INITIATION_DATE", "N_A_C_H_A_FILE_TYPE") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_EVENT_AS400_SYNC_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EVENT_AS400_SYNC_FK1" ON "PSPADM"."PSP_EVENT_AS400_SYNC" ("COMPANY_FK","COMPANY_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EVTTP_SRCSYS_FK_EVTTP
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EVTTP_SRCSYS_FK_EVTTP" ON "PSPADM"."PSP_EVTTP_SRCSYS_ASSOC" ("INTERESTING_EVENT_TYPES_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_EVTTP_SRCSYS_FK_SRCSYS
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_EVTTP_SRCSYS_FK_SRCSYS" ON "PSPADM"."PSP_EVTTP_SRCSYS_ASSOC" ("SOURCE_SYSTEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FAILED_PAYROLL_RUN_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FAILED_PAYROLL_RUN_FK1" ON "PSPADM"."PSP_FAILED_PAYROLL_RUN" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FEE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FEE_FK1" ON "PSPADM"."PSP_FEE" ("SOURCE_SYSTEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FEE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FEE_FK2" ON "PSPADM"."PSP_FEE" ("TRANSACTION_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIALTRANSACTIONST_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIALTRANSACTIONST_FK1" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("COMPANY_FK", "TRANSACTION_TYPE_FK", "TRANSACTION_STATE_EFF_DATE") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIALTRANSACTIONST_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIALTRANSACTIONST_FK2" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("TRANSACTION_TYPE_FK", "TRANSACTION_STATE_EFF_DATE") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIALTRANSACTION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIALTRANSACTION_FK1" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("BILLING_DETAIL_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIALTRANSACTION_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIALTRANSACTION_FK2" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("COMPANY_FK","RELATABLE_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIALTRANSACTION_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIALTRANSACTION_FK3" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("COMPANY_FK","ORIGINAL_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK10
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK10" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK11
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK11" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("COMP_ADJUST_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK13
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK13" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("BILL_PAYMENT_SPLIT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK14
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK14" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("TAX_PENALTY_INTEREST_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK16
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK16" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK2" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("CREDIT_BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK3" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("DEBIT_BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK5" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK6
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK6" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("COMPANY_FK","PAYCHECK_SPLIT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_FK9
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_FK9" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("CURRENT_TRANSACTION_STATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_I3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_I3" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("TRANSACTION_TYPE_FK", "COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_I4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_I4" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("LAW_FK", "TRANSACTION_TYPE_FK", "COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION_I5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION_I5" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("COMPANY_FK", "CURRENT_TRANSACTION_STATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION__FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION__FK1" ON "PSPADM"."PSP_FINANCIAL_TXN_ACTION" ("ACTION_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION__FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION__FK2" ON "PSPADM"."PSP_FINANCIAL_TXN_ACTION" ("TRANSACTION_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION__FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION__FK3" ON "PSPADM"."PSP_FINANCIAL_TXN_ACTION" ("TRANSACTION_STATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION__FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION__FK4" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("GEMS_UPLOAD_BATCH_FK") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION__FK5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION__FK5" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("COMPANY_FK","FINANCIAL_TRANSACTION_FK") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION__FK6
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION__FK6" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("TRANSACTION_STATE_FK") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANSACTION__FK7
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANSACTION__FK7" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("TRANSACTION_RESPONSE_FK") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_FINANCIAL_TRANS_DL_1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FINANCIAL_TRANS_DL_1" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("CREATED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_FIN_TXN_HOLD_FK_FIN_TXN
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FIN_TXN_HOLD_FK_FIN_TXN" ON "PSPADM"."PSP_FINTXN_ONHOLDREASON_ASSOC" ("COMPANY_FK","FINANCIAL_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FIN_TXN_HOLD_FK_HOLD
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FIN_TXN_HOLD_FK_HOLD" ON "PSPADM"."PSP_FINTXN_ONHOLDREASON_ASSOC" ("ON_HOLD_REASON_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FORECASTDETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FORECASTDETAIL_FK1" ON "PSPADM"."PSP_FORECAST_DETAIL" ("FORECAST_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FORM_TEMPLATE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FORM_TEMPLATE_FK1" ON "PSPADM"."PSP_FORM_TEMPLATE" ("AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FORM_TEMPLATE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FORM_TEMPLATE_FK2" ON "PSPADM"."PSP_FORM_TEMPLATE" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUDADDRESS_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUDADDRESS_FK1" ON "PSPADM"."PSP_FRAUD_ADDRESS" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUDBANKACCOUNT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUDBANKACCOUNT_FK1" ON "PSPADM"."PSP_FRAUD_BANK_ACCOUNT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUDCOMPANY_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUDCOMPANY_FK1" ON "PSPADM"."PSP_FRAUD_COMPANY" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUDCONTACT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUDCONTACT_FK1" ON "PSPADM"."PSP_FRAUD_CONTACT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUDVALUE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUDVALUE_FK1" ON "PSPADM"."PSP_FRAUD_VALUE" ("FRAUD_RULE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUD_ACCOUNT_ENC_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUD_ACCOUNT_ENC_I1" ON "PSPADM"."PSP_FRAUD_BANK_ACCOUNT" ("ACCOUNT_NUMBER_ENC") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUD_EVENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUD_EVENT_FK1" ON "PSPADM"."PSP_FRAUD_EVENT" ("COMPANY_FK","COMPANY_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUD_EVENT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUD_EVENT_FK2" ON "PSPADM"."PSP_FRAUD_EVENT" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUD_EVENT_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUD_EVENT_FK3" ON "PSPADM"."PSP_FRAUD_EVENT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUD_EVENT_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUD_EVENT_FK4" ON "PSPADM"."PSP_FRAUD_EVENT" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FRAUD_EVENT_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FRAUD_EVENT_I1" ON "PSPADM"."PSP_FRAUD_EVENT" ("EVENT_STATUS_CD", "EVENT_TIME_STAMP") ;
--------------------------------------------------------
--  DDL for Index PSP_FSET_FILING_DETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FSET_FILING_DETAIL_FK1" ON "PSPADM"."PSP_FSET_FILING_DETAIL" ("RESPONSE_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FSET_FILING_DETAIL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FSET_FILING_DETAIL_FK2" ON "PSPADM"."PSP_FSET_FILING_DETAIL" ("PARENT_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_FSET_FILING_DETAIL_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_FSET_FILING_DETAIL_FK3" ON "PSPADM"."PSP_FSET_FILING_DETAIL" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_GEMS_LEDGER_POSTING_RU_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_GEMS_LEDGER_POSTING_RU_FK1" ON "PSPADM"."PSP_GEMS_LEDGER_POSTING_RULE" ("LEDGER_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_GEMS_MONTHLY_BALANCE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_GEMS_MONTHLY_BALANCE_FK1" ON "PSPADM"."PSP_GEMS_MONTHLY_BALANCE" ("GEMS_LEDGER_POSTING_RULE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_GEMS_MONTHLY_BALANCE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_GEMS_MONTHLY_BALANCE_FK2" ON "PSPADM"."PSP_GEMS_MONTHLY_BALANCE" ("GEMS_UPLOAD_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_HOURS_WORKED_EXCEPTION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_HOURS_WORKED_EXCEPTION_FK1" ON "PSPADM"."PSP_HOURS_WORKED_EXCEPTION" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_INDIVIDUAL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INDIVIDUAL_FK1" ON "PSPADM"."PSP_INDIVIDUAL" ("MAILING_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_INDIVIDUAL_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INDIVIDUAL_IDX1" ON "PSPADM"."PSP_INDIVIDUAL" (LOWER("FIRST_NAME"), LOWER("LAST_NAME")) ;
--------------------------------------------------------
--  DDL for Index PSP_INDIV_EMAIL_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INDIV_EMAIL_IDX1" ON "PSPADM"."PSP_INDIVIDUAL" ("EMAIL") ;
--------------------------------------------------------
--  DDL for Index PSP_INTUIT_BABATCH_TYPE_FI_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INTUIT_BABATCH_TYPE_FI_FK1" ON "PSPADM"."PSP_INTUIT_BA_BT_FT" ("INTUIT_BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_INTUIT_BANK_ACCOUNT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INTUIT_BANK_ACCOUNT_FK1" ON "PSPADM"."PSP_INTUIT_BANK_ACCOUNT" ("BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_INTUIT_BANK_ACCOUNT_TR_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INTUIT_BANK_ACCOUNT_TR_FK1" ON "PSPADM"."PSP_INTUIT_BANK_ACC_TXN_TYPE" ("INTUIT_BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_INTUIT_BANK_ACCOUNT_TR_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INTUIT_BANK_ACCOUNT_TR_FK2" ON "PSPADM"."PSP_INTUIT_BANK_ACC_TXN_TYPE" ("TRANSACTION_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_INTUIT_SHIPPER_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_INTUIT_SHIPPER_INFO_FK1" ON "PSPADM"."PSP_INTUIT_SHIPPER_INFO" ("SHIPPER_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LAW_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LAW_FK1" ON "PSPADM"."PSP_LAW" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LAW_RATE_RANGE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LAW_RATE_RANGE_FK1" ON "PSPADM"."PSP_LAW_RATE_RANGE" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LAW_RATE_VALUE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LAW_RATE_VALUE_FK1" ON "PSPADM"."PSP_LAW_RATE_VALUE" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGERBALANCE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGERBALANCE_FK1" ON "PSPADM"."PSP_LEDGER_BALANCE" ("LEDGER_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGERBALANCE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGERBALANCE_FK2" ON "PSPADM"."PSP_LEDGER_BALANCE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_ACCOUNT_ACTION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_ACCOUNT_ACTION_FK1" ON "PSPADM"."PSP_LEDGER_ACCOUNT_ACTION" ("ACTION_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_ACCOUNT_ACTION_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_ACCOUNT_ACTION_FK2" ON "PSPADM"."PSP_LEDGER_ACCOUNT_ACTION" ("LEDGER_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_BALANCE_DL_1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_BALANCE_DL_1" ON "PSPADM"."PSP_LEDGER_BALANCE" ("CREATED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_BALANCE_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_BALANCE_I1" ON "PSPADM"."PSP_LEDGER_BALANCE" ("BALANCE_DATE") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_BALANCE_U1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_BALANCE_U1" ON "PSPADM"."PSP_LEDGER_BALANCE" (TRUNC("BALANCE_DATE")) ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_BALANCE_U2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_BALANCE_U2" ON "PSPADM"."PSP_LEDGER_BALANCE" ("COMPANY_FK", "LEDGER_ACCOUNT_FK", TRUNC("BALANCE_DATE")) ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_OPERATION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_OPERATION_FK1" ON "PSPADM"."PSP_LEDGER_OPERATION" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LEDGER_OPERATION_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LEDGER_OPERATION_FK2" ON "PSPADM"."PSP_LEDGER_OPERATION" ("LEDGER_OPERATION_JOB_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_ADJUSTMENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_ADJUSTMENT_FK1" ON "PSPADM"."PSP_LIABILITY_ADJUSTMENT" ("COMP_ADJUST_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_ADJUSTMENT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_ADJUSTMENT_FK2" ON "PSPADM"."PSP_LIABILITY_ADJUSTMENT" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_ADJUSTMENT_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_ADJUSTMENT_FK3" ON "PSPADM"."PSP_LIABILITY_ADJUSTMENT" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_ADJUSTMENT_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_ADJUSTMENT_FK4" ON "PSPADM"."PSP_LIABILITY_ADJUSTMENT" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_ADJUSTMENT_FK6
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_ADJUSTMENT_FK6" ON "PSPADM"."PSP_LIABILITY_ADJUSTMENT" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_ADJUSTMENT_FK7
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_ADJUSTMENT_FK7" ON "PSPADM"."PSP_LIABILITY_ADJUSTMENT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_CHECK_BILLIN_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_CHECK_BILLIN_FK1" ON "PSPADM"."PSP_LIAB_CHECK_BILLING_ASSOC" ("BILLING_DETAIL_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_CHECK_BILLIN_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_CHECK_BILLIN_FK2" ON "PSPADM"."PSP_LIAB_CHECK_BILLING_ASSOC" ("LIABILITY_CHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_CHECK_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_CHECK_FK1" ON "PSPADM"."PSP_LIABILITY_CHECK" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_CHECK_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_CHECK_FK2" ON "PSPADM"."PSP_LIABILITY_CHECK" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_CHECK_LINE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_CHECK_LINE_FK1" ON "PSPADM"."PSP_LIABILITY_CHECK_LINE" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_CHECK_LINE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_CHECK_LINE_FK2" ON "PSPADM"."PSP_LIABILITY_CHECK_LINE" ("LIABILITY_CHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIABILITY_CHECK_LINE_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIABILITY_CHECK_LINE_FK4" ON "PSPADM"."PSP_LIABILITY_CHECK_LINE" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_LIMIT_VALUE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_LIMIT_VALUE_FK1" ON "PSPADM"."PSP_LIMIT_VALUE" ("LIMIT_RULE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_MM_TRANSACTION_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MM_TRANSACTION_I1" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("COMPANY_FK", "STATUS", "INITIATION_DATE", "DUE_DATE", "MONEY_MOVEMENT_PAYMENT_METHOD") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_MM_TRANSACTION_I10
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MM_TRANSACTION_I10" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("PAYMENT_TEMPLATE_FK", "TAX_PAYMENT_STATUS", "PAYMENT_PERIOD_END") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_MM_TRANSACTION_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MM_TRANSACTION_I2" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("INITIATION_DATE", "MONEY_MOVEMENT_PAYMENT_METHOD") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_MM_TRANSACTION_I3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MM_TRANSACTION_I3" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("TAX_PAYMENT_STATUS", "COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_MM_TRANSACTION_I4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MM_TRANSACTION_I4" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("PAYMENT_TEMPLATE_FK", "COMPANY_FK", "STATUS") ;
--------------------------------------------------------
--  DDL for Index PSP_MM_TRANSACTION_I5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MM_TRANSACTION_I5" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("TAX_PAYMENT_STATUS", "MONEY_MOVEMENT_PAYMENT_METHOD", "PAYMENT_PERIOD_END") TABLESPACE "PSP_IDX01" LOCAL ;
--------------------------------------------------------
--  DDL for Index PSP_MM_TRANSACTION_I9
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MM_TRANSACTION_I9" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("TAX_PAYMENT_STATUS", "STATUS", "PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_MONEYMOVEMENTTRANSACTI_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MONEYMOVEMENTTRANSACTI_FK1" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("COMPANY_FK","ORIGINAL_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_MONEY_MOVEMENT_TRANSAC_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MONEY_MOVEMENT_TRANSAC_FK2" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_MONEY_MOVEMENT_TRANSAC_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MONEY_MOVEMENT_TRANSAC_FK3" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("OFFLOAD_BATCH_FK") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_MONEY_MOVEMENT_TRANSAC_FK6
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_MONEY_MOVEMENT_TRANSAC_FK6" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("PAYMENT_FREQUENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_NACHAFILE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_NACHAFILE_FK1" ON "PSPADM"."PSP_NACHAFILE" ("OFFLOAD_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFERINGSERVICECHARGEP_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFERINGSERVICECHARGEP_FK1" ON "PSPADM"."PSP_SVCCHGPRICE" ("OFFERING_SERVICE_CHARGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFERING_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFERING_FK1" ON "PSPADM"."PSP_OFFERING" ("LIMIT_RULE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFERING_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFERING_FK2" ON "PSPADM"."PSP_OFFERING" ("FRAUD_RULE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFERING_SVCCHG_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFERING_SVCCHG_FK" ON "PSPADM"."PSP_OFFERING_SVCCHG" ("OFFERING_SVCCHG_GRP_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFERING_SVCCHG_GRP_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFERING_SVCCHG_GRP_FK" ON "PSPADM"."PSP_OFFERING_SVCCHG_GRP" ("OFFERING_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFER_PRICE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFER_PRICE_FK1" ON "PSPADM"."PSP_OFFER_PRICE" ("OFFER_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFER_SVCCHG_FK_OFFER
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFER_SVCCHG_FK_OFFER" ON "PSPADM"."PSP_OFFER_SVCCHG_ASSOC" ("OFFER_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFER_SVCCHG_FK_SVCCHG
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFER_SVCCHG_FK_SVCCHG" ON "PSPADM"."PSP_OFFER_SVCCHG_ASSOC" ("OFFERING_SERVICE_CHARGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_OFFLOAD_BATCH_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_OFFLOAD_BATCH_FK1" ON "PSPADM"."PSP_OFFLOAD_BATCH" ("OFFLOAD_GROUP_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ON_HOLD_REASON_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ON_HOLD_REASON_FK1" ON "PSPADM"."PSP_ON_HOLD_REASON" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECKUSAGE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECKUSAGE_FK1" ON "PSPADM"."PSP_PAYCHECK_USAGE" ("BILL_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECKUSAGE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECKUSAGE_FK2" ON "PSPADM"."PSP_PAYCHECK_USAGE" ("EMPLOYEE_USAGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_FK1" ON "PSPADM"."PSP_PAYCHECK" ("D_D_EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_FK2" ON "PSPADM"."PSP_PAYCHECK" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_FK3" ON "PSPADM"."PSP_PAYCHECK" ("COMP_ADJUST_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_FK4" ON "PSPADM"."PSP_PAYCHECK" ("SOURCE_EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_I1" ON "PSPADM"."PSP_PAYCHECK" ("CREATED_DATE") TABLESPACE "PSP_IDX01" LOCAL;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_I2" ON "PSPADM"."PSP_PAYCHECK" ("COMPANY_FK", "SOURCE_PAYCHECK_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_SPLIT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_SPLIT_FK1" ON "PSPADM"."PSP_PAYCHECK_SPLIT" ("EMPLOYEE_BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_SPLIT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_SPLIT_FK2" ON "PSPADM"."PSP_PAYCHECK_SPLIT" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_SPLIT_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_SPLIT_FK3" ON "PSPADM"."PSP_PAYCHECK_SPLIT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_SPLIT_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_PAYCHECK_SPLIT_U1" ON "PSPADM"."PSP_PAYCHECK_SPLIT" ("SOURCE_DD_TXN_ID", "PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_U1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_U1" ON "PSPADM"."PSP_PAYCHECK" ("SOURCE_PAYCHECK_ID", "PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_USAGE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_USAGE_FK1" ON "PSPADM"."PSP_PAYCHECK_USAGE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_USAGE_HIST_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_USAGE_HIST_FK1" ON "PSPADM"."PSP_PAYCHECK_USAGE_HIST" ("EMPLOYEE_USAGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_USAGE_HIST_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_USAGE_HIST_FK2" ON "PSPADM"."PSP_PAYCHECK_USAGE_HIST" ("COMPANY_FK","PAYCHECK_USAGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_USAGE_NU1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYCHECK_USAGE_NU1" ON "PSPADM"."PSP_PAYCHECK_USAGE" ("SOURCE_PAYCHECK_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYCHECK_USAGE_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_PAYCHECK_USAGE_U1" ON "PSPADM"."PSP_PAYCHECK_USAGE" ("EMPLOYEE_USAGE_FK", "SOURCE_PAYCHECK_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYEE_BANK_ACCOUNT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYEE_BANK_ACCOUNT_FK1" ON "PSPADM"."PSP_PAYEE_BANK_ACCOUNT" ("PAYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYEE_BANK_ACCOUNT_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYEE_BANK_ACCOUNT_FK2" ON "PSPADM"."PSP_PAYEE_BANK_ACCOUNT" ("BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYEE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYEE_FK1" ON "PSPADM"."PSP_PAYEE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYEE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYEE_FK2" ON "PSPADM"."PSP_PAYEE" ("MAILING_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENTMETHODREQUIREME_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENTMETHODREQUIREME_FK1" ON "PSPADM"."PSP_PAYMENT_METHOD_REQUIREMENT" ("PMT_TEMPLATE_PMT_METHOD_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_BATCH_ASSOC_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_BATCH_ASSOC_FK1" ON "PSPADM"."PSP_PAYMENT_BATCH_ASSOC" ("AGENCY_CHECK_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_BATCH_ASSOC_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_BATCH_ASSOC_FK2" ON "PSPADM"."PSP_PAYMENT_BATCH_ASSOC" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_AGENC_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_AGENC_FK1" ON "PSPADM"."PSP_PAYMENT_TEMPLATE_AGENCY_ID" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_BANK__FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_BANK__FK1" ON "PSPADM"."PSP_PMT_TEMPLATE_BANKACCOUNT" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_BANK__FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_BANK__FK2" ON "PSPADM"."PSP_PMT_TEMPLATE_BANKACCOUNT" ("BANK_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_CHECK_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_CHECK_FK1" ON "PSPADM"."PSP_PMTTEMPLATE_CHKINFO_ASSOC" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_CHECK_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_CHECK_FK2" ON "PSPADM"."PSP_PMTTEMPLATE_CHKINFO_ASSOC" ("PMTTEMPLATE_PRINTEDCHKINFO_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_FK1" ON "PSPADM"."PSP_PAYMENT_TEMPLATE" ("AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_FREQU_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_FREQU_FK1" ON "PSPADM"."PSP_PMT_TEMPLATE_FREQUENCY" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_PAYME_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_PAYME_FK1" ON "PSPADM"."PSP_PMT_TEMPLATE_PAYMENTMETHOD" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYMENT_TEMPLATE_PRINT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYMENT_TEMPLATE_PRINT_FK1" ON "PSPADM"."PSP_PMTTEMPLATE_PRINTEDCHKINFO" ("ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_ITEM_TAXABLE_T_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_ITEM_TAXABLE_T_FK1" ON "PSPADM"."PSP_PAYROLL_ITEM_TAXABLE_TO" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_ITEM_TAXABLE_T_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_ITEM_TAXABLE_T_FK2" ON "PSPADM"."PSP_PAYROLL_ITEM_TAXABLE_TO" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_RUN_ACTION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_RUN_ACTION_FK1" ON "PSPADM"."PSP_PAYROLL_RUN_ACTION" ("ACTION_EVENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_RUN_EECALCTOKEN
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_RUN_EECALCTOKEN" ON "PSPADM"."PSP_PAYROLL_RUN" ("E_E_CALCULATION_TOKEN") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_RUN_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_RUN_I1" ON "PSPADM"."PSP_PAYROLL_RUN" ("COMPANY_FK", "PAYCHECK_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_RUN_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_RUN_I2" ON "PSPADM"."PSP_PAYROLL_RUN" ("PAYROLL_RUN_STATUS") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_RUN_I3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_RUN_I3" ON "PSPADM"."PSP_PAYROLL_RUN" ("COMPANY_FK", "PAYCHECK_SETTLEMENT_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_RUN_I4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_RUN_I4" ON "PSPADM"."PSP_PAYROLL_RUN" ("USAGE_BILLING_TOKEN") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_RUN_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_PAYROLL_RUN_U1" ON "PSPADM"."PSP_PAYROLL_RUN" ("COMPANY_FK", "SOURCE_PAY_RUN_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYROLL_SUBTYPE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYROLL_SUBTYPE_FK1" ON "PSPADM"."PSP_PAYROLL_SUBTYPE" ("OFFERING_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYSTUB_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYSTUB_FK1" ON "PSPADM"."PSP_PAYSTUB" ("PSTUB_EMPLOYER_INFO_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYSTUB_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYSTUB_FK2" ON "PSPADM"."PSP_PAYSTUB" ("COMPANY_FK","PSTUB_EMPLOYEE_INFO_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAYSTUB_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAYSTUB_FK3" ON "PSPADM"."PSP_PAYSTUB" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAY_ITEM_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAY_ITEM_FK1" ON "PSPADM"."PSP_PAY_ITEM" ("LIABILITY_ADJUSTMENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PAY_ITEM_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PAY_ITEM_FK2" ON "PSPADM"."PSP_PAY_ITEM" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_POSTING_RULE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_POSTING_RULE_FK1" ON "PSPADM"."PSP_POSTING_RULE" ("LEDGER_ACCOUNT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_POSTING_RULE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_POSTING_RULE_FK2" ON "PSPADM"."PSP_POSTING_RULE" ("TRANSACTION_STATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_POSTING_RULE_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_POSTING_RULE_FK3" ON "PSPADM"."PSP_POSTING_RULE" ("TRANSACTION_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PRIOR_PAYMENT_SUBMISSI_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PRIOR_PAYMENT_SUBMISSI_FK1" ON "PSPADM"."PSP_PRIOR_PAYMENT_SUBMISSION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PROPERTYAUDIT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PROPERTYAUDIT_FK1" ON "PSPADM"."PSP_PROPERTY_AUDIT" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PR_PROCESED_BY_FRD_BTCHJOB
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PR_PROCESED_BY_FRD_BTCHJOB" ON "PSPADM"."PSP_PAYROLL_RUN" ("PROCESSED_BY_FRAUD_BATCH_JOB") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_DDITEM_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_DDITEM_FK1" ON "PSPADM"."PSP_PSTUB_DDITEM" ("COMPANY_FK","PAYSTUB_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_EMPLOYEE_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_EMPLOYEE_INFO_FK1" ON "PSPADM"."PSP_PSTUB_EMPLOYEE_INFO" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_EMPLOYEE_INFO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_EMPLOYEE_INFO_FK2" ON "PSPADM"."PSP_PSTUB_EMPLOYEE_INFO" ("PSTUB_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_EMPLOYEE_PREFERE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_EMPLOYEE_PREFERE_FK1" ON "PSPADM"."PSP_PSTUB_EMPLOYEE_PREFERENCE" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_EMPLOYER_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_EMPLOYER_INFO_FK1" ON "PSPADM"."PSP_PSTUB_EMPLOYER_INFO" ("PSTUB_ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_MSG_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_MSG_FK1" ON "PSPADM"."PSP_PSTUB_MSG" ("COMPANY_FK","PAYSTUB_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_PAID_TIMEOFF_ITE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_PAID_TIMEOFF_ITE_FK1" ON "PSPADM"."PSP_PSTUB_PAID_TIMEOFF_ITEM" ("COMPANY_FK","PAYSTUB_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_PAY_ITEM_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_PAY_ITEM_FK1" ON "PSPADM"."PSP_PSTUB_PAY_ITEM" ("COMPANY_FK","PAYSTUB_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_PAY_ITEM_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_PAY_ITEM_FK2" ON "PSPADM"."PSP_PSTUB_PAY_ITEM" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_PSTUB_STATE_TAX_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_PSTUB_STATE_TAX_INFO_FK1" ON "PSPADM"."PSP_PSTUB_STATE_TAX_INFO" ("PSTUB_EMPLOYER_INFO_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDTTRANSACTIONINFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDTTRANSACTIONINFO_FK1" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("PRIOR_PAYMENT_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_EMPLOYEE_INFO_COM_LST
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_EMPLOYEE_INFO_COM_LST" ON "PSPADM"."PSP_QBDT_EMPLOYEE_INFO" ("COMPANY_FK", "LIST_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_EMPLOYEE_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_EMPLOYEE_INFO_FK1" ON "PSPADM"."PSP_QBDT_EMPLOYEE_INFO" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_EMPLOYEE_INFO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_EMPLOYEE_INFO_FK2" ON "PSPADM"."PSP_QBDT_EMPLOYEE_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYCHECK_INFO_COM_LST
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYCHECK_INFO_COM_LST" ON "PSPADM"."PSP_QBDT_PAYCHECK_INFO" ("COMPANY_FK", "LIST_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYCHECK_INFO_CYVT_B1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYCHECK_INFO_CYVT_B1" ON "PSPADM"."PSP_QBDT_PAYCHECK_INFO" ("COMPANY_FK", "VOID_TOKEN") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYCHECK_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYCHECK_INFO_FK1" ON "PSPADM"."PSP_QBDT_PAYCHECK_INFO" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYCHECK_INFO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYCHECK_INFO_FK2" ON "PSPADM"."PSP_QBDT_PAYCHECK_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYLINE_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYLINE_INFO_FK1" ON "PSPADM"."PSP_QBDT_PAYLINE_INFO" ("EMPLOYER_CONTRIBUTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYLINE_INFO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYLINE_INFO_FK2" ON "PSPADM"."PSP_QBDT_PAYLINE_INFO" ("COMPANY_FK","COMPENSATION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYLINE_INFO_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYLINE_INFO_FK3" ON "PSPADM"."PSP_QBDT_PAYLINE_INFO" ("COMPANY_FK","DEDUCTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYLINE_INFO_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYLINE_INFO_FK4" ON "PSPADM"."PSP_QBDT_PAYLINE_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_ITEM_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_ITEM_INFO_FK1" ON "PSPADM"."PSP_QBDT_PAYROLL_ITEM_INFO" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_ITEM_INFO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_ITEM_INFO_FK2" ON "PSPADM"."PSP_QBDT_PAYROLL_ITEM_INFO" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_ITEM_INFO_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_ITEM_INFO_FK3" ON "PSPADM"."PSP_QBDT_PAYROLL_ITEM_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_TRANSACTI_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTI_FK1" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTION" ("COMP_ADJUST_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_TRANSACTI_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTI_FK2" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANS_LINE" ("QBDT_PAYROLL_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_TRANSACTI_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTI_FK3" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_TRANSLINE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_TRANSLINE_FK1" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANS_LINE" ("COMPANY_PAYROLL_ITEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_TRANS_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_TRANS_FK1" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTION" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PAYROLL_TRANS_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PAYROLL_TRANS_FK2" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTION" ("PRIOR_PAYMENT_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_PYLN_MD_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_PYLN_MD_IDX1" ON "PSPADM"."PSP_QBDT_PAYLINE_INFO" ("MODIFIED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_REQUEST_INFO_CRDT
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_REQUEST_INFO_CRDT" ON "PSPADM"."PSP_QBDT_REQUEST_INFO" ("CREATED_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_REQUEST_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_REQUEST_INFO_FK1" ON "PSPADM"."PSP_QBDT_REQUEST_INFO" ("SOURCE_SYSTEM_TRANSMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK1" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("LIABILITY_CHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK2" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("LIABILITY_CHECK_LINE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK3" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK4
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK4" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("COMPANY_FK","FINANCIAL_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK5" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("COMP_ADJUST_SUBMISSION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK6
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK6" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("LIABILITY_ADJUSTMENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK7
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK7" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK8
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK8" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("QBDT_PAYROLL_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_TRANSACTION_INFO_FK9
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_TRANSACTION_INFO_FK9" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("QBDT_PAYROLL_TRANS_LINE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_UNPROCESSED_REQUE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_UNPROCESSED_REQUE_FK1" ON "PSPADM"."PSP_QBDT_UNPROCESSED_REQUEST" ("SOURCE_SYSTEM_TRANSMISSION_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_UNPROCESSED_REQUE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_UNPROCESSED_REQUE_FK2" ON "PSPADM"."PSP_QBDT_UNPROCESSED_REQUEST" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_QBDT_UNPROCESSED_REQUE_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QBDT_UNPROCESSED_REQUE_I1" ON "PSPADM"."PSP_QBDT_UNPROCESSED_REQUEST" ("STATUS") ;
--------------------------------------------------------
--  DDL for Index PSP_QUICKBOOKS_I2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QUICKBOOKS_I2" ON "PSPADM"."PSP_QUICKBOOKS_INFO" ("LICENSE_NUMBER") ;
--------------------------------------------------------
--  DDL for Index PSP_QUICKBOOKS_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_QUICKBOOKS_INFO_FK1" ON "PSPADM"."PSP_QUICKBOOKS_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_RAFENROLLMENT_DETAIL_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_RAFENROLLMENT_DETAIL_FK1" ON "PSPADM"."PSP_RAFENROLLMENT_DETAIL" ("R_A_F_ENROLLMENT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_RAFENROLLMENT_DETAIL_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_RAFENROLLMENT_DETAIL_FK2" ON "PSPADM"."PSP_RAFENROLLMENT_DETAIL" ("ENROLLMENT_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_RAFENROLLMENT_DETAIL_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_RAFENROLLMENT_DETAIL_FK3" ON "PSPADM"."PSP_RAFENROLLMENT_DETAIL" ("DELETE_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_RAFENROLLMENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_RAFENROLLMENT_FK1" ON "PSPADM"."PSP_RAFENROLLMENT" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_RAFENROLLMENT_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_RAFENROLLMENT_I1" ON "PSPADM"."PSP_RAFENROLLMENT" ("STATUS") ;
--------------------------------------------------------
--  DDL for Index PSP_REPORTING_AGENT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_REPORTING_AGENT_FK1" ON "PSPADM"."PSP_REPORTING_AGENT" ("ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ROLE_OPERATION_FK_ROLE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ROLE_OPERATION_FK_ROLE" ON "PSPADM"."PSP_AUTHROLE_OPERATION_ASSOC" ("AUTH_ROLE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ROLE_SUB_STATUS_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ROLE_SUB_STATUS_FK1" ON "PSPADM"."PSP_ROLE_SUB_STATUS" ("AUTH_ROLE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_ROLE_SUB_STATUS_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_ROLE_SUB_STATUS_FK2" ON "PSPADM"."PSP_ROLE_SUB_STATUS" ("SERVICE_SUB_STATUS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SAP_METHOD_CALL_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SAP_METHOD_CALL_IDX1" ON "PSPADM"."PSP_SAP_METHOD_CALL_BKP" ("CREATED_DATE", "SERVICE_NAME", "METHOD_NAME") ;
--------------------------------------------------------
--  DDL for Index PSP_SAP_METH_CALL_IDX1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SAP_METH_CALL_IDX1" ON "PSPADM"."PSP_SAP_METHOD_CALL" ("CREATED_DATE", "SERVICE_NAME", "METHOD_NAME") ;
--------------------------------------------------------
--  DDL for Index PSP_SECOND_OFFLOAD_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SECOND_OFFLOAD_FK1" ON "PSPADM"."PSP_SECOND_OFFLOAD" ("OFFLOAD_GROUP_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SERVICE_SUB_STATUS_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SERVICE_SUB_STATUS_FK1" ON "PSPADM"."PSP_SERVICE_SUB_STATUS" ("SERVICE_STATUS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SERV_STAT_TXN_SKU_TYPE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SERV_STAT_TXN_SKU_TYPE_FK1" ON "PSPADM"."PSP_SERV_STAT_TXN_SKU_TYPE" ("SERVICE_SUB_STATUS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SERV_STAT_TXN_SKU_TYPE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SERV_STAT_TXN_SKU_TYPE_FK2" ON "PSPADM"."PSP_SERV_STAT_TXN_SKU_TYPE" ("TRANSACTION_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SMSMIGRATION_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SMSMIGRATION_FK1" ON "PSPADM"."PSP_SMSMIGRATION" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SOURCE_SYSTEM_LAW_ASSO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SOURCE_SYSTEM_LAW_ASSO_FK1" ON "PSPADM"."PSP_SOURCE_SYSTEM_LAW_ASSOC" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SOURCE_SYSTEM_LAW_ASSO_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SOURCE_SYSTEM_LAW_ASSO_FK2" ON "PSPADM"."PSP_SOURCE_SYSTEM_LAW_ASSOC" ("SOURCE_SYSTEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SOURCE_SYSTEM_PRINTED__FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SOURCE_SYSTEM_PRINTED__FK1" ON "PSPADM"."PSP_SOURCESYS_PRINTEDCHK_INFO" ("ADDRESS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_STATE_EDI_TAX_FILE_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_STATE_EDI_TAX_FILE_FK2" ON "PSPADM"."PSP_STATE_EDI_TAX_FILE" ("ACK_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_STRPT_TMPFREQ_FK_PMTTMPFRQ
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_STRPT_TMPFREQ_FK_PMTTMPFRQ" ON "PSPADM"."PSP_STATE_REPORT_ASSOC" ("PAYMENT_TEMPLATE_FREQUENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_STRPT_TMPFREQ_FK_STRPTOUT
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_STRPT_TMPFREQ_FK_STRPTOUT" ON "PSPADM"."PSP_STATE_REPORT_ASSOC" ("STATE_REPORT_OUTPUT_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SUICREDITS_JOB_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SUICREDITS_JOB_FK1" ON "PSPADM"."PSP_SUICREDITS_JOB" ("PAYMENT_TEMPLATE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SVCSTAT_CAP_FK_CAP
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SVCSTAT_CAP_FK_CAP" ON "PSPADM"."PSP_SVCSTAT_SYSCAP_ASSOC" ("SYSTEM_CAPABILITY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SVCSTAT_CAP_FK_SVCSTAT
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SVCSTAT_CAP_FK_SVCSTAT" ON "PSPADM"."PSP_SVCSTAT_SYSCAP_ASSOC" ("SERVICE_SUB_STATUS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SVCSTAT_SRCSYS_FK_SRCSYS
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SVCSTAT_SRCSYS_FK_SRCSYS" ON "PSPADM"."PSP_SVCSTAT_SRCSYS_ASSOC" ("SOURCE_SYSTEM_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SVCSTAT_SRCSYS_FK_SVCSTAT
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SVCSTAT_SRCSYS_FK_SVCSTAT" ON "PSPADM"."PSP_SVCSTAT_SRCSYS_ASSOC" ("SERVICE_SUB_STATUS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SVCSTAT_SVC_FK_SVC
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SVCSTAT_SVC_FK_SVC" ON "PSPADM"."PSP_SVCSTAT_SVC_ASSOC" ("SERVICE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SVCSTAT_SVC_FK_SVCSTAT
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_SVCSTAT_SVC_FK_SVCSTAT" ON "PSPADM"."PSP_SVCSTAT_SVC_ASSOC" ("SERVICE_SUB_STATUS_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_SYSTEM_PARAMETER_SPCODE_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_SYSTEM_PARAMETER_SPCODE_U1" ON "PSPADM"."PSP_SYSTEM_PARAMETER" ("SYSTEM_PARAMETER_CD") ;
--------------------------------------------------------
--  DDL for Index PSP_TAXPAYMENTONHOLDREASON_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAXPAYMENTONHOLDREASON_FK1" ON "PSPADM"."PSP_TAX_PAYMENT_ON_HOLD_REASON" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_ACC_AUD_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_ACC_AUD_I1" ON "PSPADM"."PSP_TAX_ACCOUNT_AUDIT" ("REPORTED") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_ACC_AUD_PK
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_TAX_ACC_AUD_PK" ON "PSPADM"."PSP_TAX_ACCOUNT_AUDIT" ("AUDIT_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_CREDITS9061_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_CREDITS9061_FK1" ON "PSPADM"."PSP_TAX_CREDITS9061" ("TAX_CREDITS_APPLICATION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_FK1" ON "PSPADM"."PSP_TAX" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_FK2" ON "PSPADM"."PSP_TAX" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_FK3" ON "PSPADM"."PSP_TAX" ("COMPANY_LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_FK5
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_FK5" ON "PSPADM"."PSP_TAX" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_PENALTY_INTEREST_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_PENALTY_INTEREST_FK1" ON "PSPADM"."PSP_TAX_PENALTY_INTEREST" ("COMPANY_AGENCY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_TABLE_MISC_DATA_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_TABLE_MISC_DATA_FK1" ON "PSPADM"."PSP_TAX_TABLE_MISC_DATA" ("EMPLOYEE_TAX_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TAX_TABLE_MISC_DATA_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TAX_TABLE_MISC_DATA_FK2" ON "PSPADM"."PSP_TAX_TABLE_MISC_DATA" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_THIRD_PARTY401K_BATCH__FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_THIRD_PARTY401K_BATCH__FK1" ON "PSPADM"."PSP_TP401K_BATCH_PAYCHECK" ("THIRD_PARTY401K_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_THIRD_PARTY401K_BATCH__FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_THIRD_PARTY401K_BATCH__FK2" ON "PSPADM"."PSP_TP401K_BATCH_PAYCHECK" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TP401K_BATCH_EE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TP401K_BATCH_EE_FK1" ON "PSPADM"."PSP_TP401K_BATCH_EMPLOYEE" ("THIRD_PARTY401K_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TP401K_BATCH_EE_FK2 
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TP401K_BATCH_EE_FK2 " ON "PSPADM"."PSP_TP401K_BATCH_EMPLOYEE" ("EMPLOYEE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TP401K_PCHK_PCHK_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TP401K_PCHK_PCHK_FK" ON "PSPADM"."PSP_TP401K_PAYCHECK" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TP401K_PCHK_STATE_PCHK_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TP401K_PCHK_STATE_PCHK_FK" ON "PSPADM"."PSP_TP401K_PAYCHECK_STATE" ("THIRD_PARTY401K_PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TP401K_PEND_STATE_PCHK_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TP401K_PEND_STATE_PCHK_FK" ON "PSPADM"."PSP_TP401K_PAYCHECK_PENDING" ("THIRD_PARTY401K_PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSACTION_OFFLOAD_BA_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSACTION_OFFLOAD_BA_FK1" ON "PSPADM"."PSP_TRANSACTION_OFFLOAD_BATCH" ("COMPANY_FK","FINANCIAL_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSACTION_OFFLOAD_BA_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSACTION_OFFLOAD_BA_FK2" ON "PSPADM"."PSP_TRANSACTION_OFFLOAD_BATCH" ("OFFLOAD_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSACTION_RESPONSE_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSACTION_RESPONSE_FK1" ON "PSPADM"."PSP_TRANSACTION_RESPONSE" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSACTION_RETURN_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSACTION_RETURN_FK1" ON "PSPADM"."PSP_TRANSACTION_RETURN" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSACTION_RETURN_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSACTION_RETURN_FK2" ON "PSPADM"."PSP_TRANSACTION_RETURN" ("RETURN_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSACTION_RETURN_I1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSACTION_RETURN_I1" ON "PSPADM"."PSP_TRANSACTION_RETURN" ("COMPANY_FK", "RETURN_STATUS_CD", "BANK_RETURN_CD") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSMISSIONPAYROLLRUN_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSMISSIONPAYROLLRUN_FK1" ON "PSPADM"."PSP_TRANSMISSION_PAYROLL_RUN" ("SOURCE_SYSTEM_TRANSMISSION_ID") ;
--------------------------------------------------------
--  DDL for Index PSP_TRANSMISSIONPAYROLLRUN_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TRANSMISSIONPAYROLLRUN_FK2" ON "PSPADM"."PSP_TRANSMISSION_PAYROLL_RUN" ("PAYROLL_RUN_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TXNTYPE_SVC_FK_SVC
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TXNTYPE_SVC_FK_SVC" ON "PSPADM"."PSP_TXNTYPE_SERVICE_ASSOC" ("SERVICE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_TXNTYPE_SVC_FK_TXNTYPE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_TXNTYPE_SVC_FK_TXNTYPE" ON "PSPADM"."PSP_TXNTYPE_SERVICE_ASSOC" ("TRANSACTION_TYPE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_USAGEPERIOD_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_USAGEPERIOD_FK1" ON "PSPADM"."PSP_USAGE_PERIOD" ("COMPANY_USAGE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_USAGE_PERIOD_U1
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."PSP_USAGE_PERIOD_U1" ON "PSPADM"."PSP_USAGE_PERIOD" ("COMPANY_USAGE_FK", "START_DATE", "END_DATE") ;
--------------------------------------------------------
--  DDL for Index PSP_USERROLE_FK_ROLE
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_USERROLE_FK_ROLE" ON "PSPADM"."PSP_AUTH_USER_AUTH_ROLE__ASSOC" ("AUTH_ROLE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_USERROLE_FK_USER
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_USERROLE_FK_USER" ON "PSPADM"."PSP_AUTH_USER_AUTH_ROLE__ASSOC" ("AUTH_USER_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_USERSETTING_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_USERSETTING_FK1" ON "PSPADM"."PSP_USER_SETTING" ("AUTH_USER_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_USER_SETTING_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_USER_SETTING_FK1" ON "PSPADM"."PSP_USER_SETTING" ("USER_PREFERENCE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_VMP_EMPLOYEE_INFO_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_VMP_EMPLOYEE_INFO_FK1" ON "PSPADM"."PSP_VMP_EMPLOYEE_INFO" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_VOIDED_CHECK_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_VOIDED_CHECK_FK1" ON "PSPADM"."PSP_VOIDED_CHECK" ("ACCOUNTING_REPORT_FILE_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_VOIDED_CHECK_FK2
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_VOIDED_CHECK_FK2" ON "PSPADM"."PSP_VOIDED_CHECK" ("COMPANY_FK","MONEY_MOVEMENT_TRANSACTION_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_VOIDED_CHECK_FK3
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_VOIDED_CHECK_FK3" ON "PSPADM"."PSP_VOIDED_CHECK" ("AGENCY_CHECK_BATCH_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_WAGE_LIMIT_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_WAGE_LIMIT_FK1" ON "PSPADM"."PSP_WAGE_LIMIT" ("LAW_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_WC_COMPANY_FK1
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_WC_COMPANY_FK1" ON "PSPADM"."PSP_WC_COMPANY" ("COMPANY_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_WC_PCHK_PCHK_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_WC_PCHK_PCHK_FK" ON "PSPADM"."PSP_WC_PAYCHECK" ("COMPANY_FK","PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_WC_PCHK_STATE_PCHK_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_WC_PCHK_STATE_PCHK_FK" ON "PSPADM"."PSP_WC_PAYCHECK_STATE" ("WORKERS_COMP_PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index PSP_WC_PEND_STATE_PCHK_FK
--------------------------------------------------------

  CREATE INDEX "PSPADM"."PSP_WC_PEND_STATE_PCHK_FK" ON "PSPADM"."PSP_WC_PAYCHECK_PENDING" ("WORKERS_COMP_PAYCHECK_FK") ;
--------------------------------------------------------
--  DDL for Index STATS_11DEC
--------------------------------------------------------

  CREATE INDEX "PSPADM"."STATS_11DEC" ON "PSPADM"."STATS_11DEC" ("STATID", "TYPE", "C5", "C1", "C2", "C3", "C4", "VERSION") ;
  GRANT DELETE ON "PSPADM"."STATS_11DEC" TO "PSPAPP_ROLE";
  GRANT INSERT ON "PSPADM"."STATS_11DEC" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."STATS_11DEC" TO "PSPAPP_ROLE";
  GRANT UPDATE ON "PSPADM"."STATS_11DEC" TO "PSPAPP_ROLE";
  GRANT SELECT ON "PSPADM"."STATS_11DEC" TO "PSPREAD_ROLE";
--------------------------------------------------------
--  DDL for Index STATUS_CD_IDX
--------------------------------------------------------

  CREATE INDEX "PSPADM"."STATUS_CD_IDX" ON "PSPADM"."PSP_COMPANY_EVENT_EMAIL" ("STATUS_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C00100240
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C00100240" ON "PSPADM"."PSP_SMSSYNC_FAILURE" ("SMSSYNC_FAILURE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C00105734
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C00105734" ON "PSPADM"."PSP_SMSMIGRATION" ("SMSMIGRATION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C00105738
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C00105738" ON "PSPADM"."PSP_OWNERSHIP_TYPE" ("OWNERSHIP_TYPE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C00129966
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C00129966" ON "PSPADM"."PSP_WC_COMPANY" ("WC_COMPANY_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C00130765
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C00130765" ON "PSPADM"."PSP_ARCHIVE_RECORD" ("ARCHIVE_RECORD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C00133801
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C00133801" ON "PSPADM"."PSP_ENTITY_UPDATE" ("ENTITY_UPDATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020423
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020423" ON "PSPADM"."PSP_TAX_CREDITS9061" ("TAX_CREDITS9061_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020424
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020424" ON "PSPADM"."PSP_COMPANY" ("COMPANY_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020430
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020430" ON "PSPADM"."PSP_BANK_ACCOUNT" ("BANK_ACCOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020457
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020457" ON "PSPADM"."PSP_AUTH_ROLE" ("AUTH_ROLE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020458
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020458" ON "PSPADM"."PSP_BATCH_JOB_AUDIT_LOG" ("BATCH_JOB_AUDIT_LOG_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020459
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020459" ON "PSPADM"."PSP_BILLING_DETAIL" ("BILLING_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020460
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020460" ON "PSPADM"."PSP_COMPANY_EVENT_DETAIL" ("COMPANY_EVENT_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020461
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020461" ON "PSPADM"."PSP_COMPANY_OFFER" ("COMPANY_OFFER_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020462
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020462" ON "PSPADM"."PSP_COMPANY_SERVICE_BANK_ACCT" ("COMPANY_SERVICE_BANK_ACCT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020463
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020463" ON "PSPADM"."PSP_DICRFILE" ("DICRFILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020464
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020464" ON "PSPADM"."PSP_FINANCIAL_TXN_ACTION" ("FINANCIAL_TXN_ACTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020465
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020465" ON "PSPADM"."PSP_GEMS_LEDGER_POSTING_RULE" ("GEMS_LEDGER_POSTING_RULE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020466
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020466" ON "PSPADM"."PSP_GEMS_MONTHLY_BALANCE" ("GEMS_MONTHLY_BALANCE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020467
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020467" ON "PSPADM"."PSP_GEMS_UPLOAD_BATCH" ("GEMS_UPLOAD_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020468
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020468" ON "PSPADM"."PSP_INTUIT_BA_BT_FT" ("INTUIT_BA_BT_FT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020469
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020469" ON "PSPADM"."PSP_LEDGER_ACCOUNT_ACTION" ("LEDGER_ACCOUNT_ACTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020470
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020470" ON "PSPADM"."PSP_NACHAFILE" ("NACHAFILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020471
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020471" ON "PSPADM"."PSP_OFFER" ("OFFER_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020472
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020472" ON "PSPADM"."PSP_OFFERING" ("OFFERING_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020473
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020473" ON "PSPADM"."PSP_OFFERING_SVCCHG" ("OFFERING_SVCCHG_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020474
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020474" ON "PSPADM"."PSP_OFFERING_SVCCHG_GRP" ("OFFERING_SVCCHG_GRP_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020475
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020475" ON "PSPADM"."PSP_TRANSMISSION_PAYROLL_RUN" ("TRANSMISSION_PAYROLL_RUN_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020476
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020476" ON "PSPADM"."PSP_ADDRESS" ("ADDRESS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020477
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020477" ON "PSPADM"."PSP_AUTH_USER" ("AUTH_USER_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020478
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020478" ON "PSPADM"."PSP_COMPANY_BANK_ACCOUNT" ("COMPANY_BANK_ACCOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020479
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020479" ON "PSPADM"."PSP_COMPANY_NOTE" ("COMPANY_NOTE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020480
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020480" ON "PSPADM"."PSP_COMPANY_SERVICE" ("COMPANY_SERVICE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020481
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020481" ON "PSPADM"."PSP_COMPENSATION" ("COMPENSATION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020482
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020482" ON "PSPADM"."PSP_CONTACT" ("CONTACT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020483
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020483" ON "PSPADM"."PSP_DDCOMPANY_SERVICE_INFO" ("DDCOMPANY_SERVICE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020484
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020484" ON "PSPADM"."PSP_DEDUCTION" ("DEDUCTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020485
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020485" ON "PSPADM"."PSP_EFFECTIVE_DEPOSIT_FREQ" ("EFFECTIVE_DEPOSIT_FREQ_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020486
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020486" ON "PSPADM"."PSP_EMPLOYEE" ("EMPLOYEE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020487
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020487" ON "PSPADM"."PSP_EMPLOYEE_BANK_ACCOUNT" ("EMPLOYEE_BANK_ACCOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020488
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020488" ON "PSPADM"."PSP_FEE" ("FEE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020489
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020489" ON "PSPADM"."PSP_INDIVIDUAL" ("INDIVIDUAL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020490
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020490" ON "PSPADM"."PSP_INTUIT_BANK_ACCOUNT" ("INTUIT_BANK_ACCOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020491
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020491" ON "PSPADM"."PSP_INTUIT_BANK_ACC_TXN_TYPE" ("INTUIT_BANK_ACC_TXN_TYPE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020492
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020492" ON "PSPADM"."PSP_OFFLOAD_BATCH" ("OFFLOAD_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020493
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020493" ON "PSPADM"."PSP_OFFLOAD_GROUP" ("OFFLOAD_GROUP_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020494
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020494" ON "PSPADM"."PSP_PAYROLL_RUN" ("PAYROLL_RUN_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020495
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020495" ON "PSPADM"."PSP_SOURCE_PAYROLL_PARAMETER" ("SOURCE_PAYROLL_PARAMETER_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020496
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020496" ON "PSPADM"."PSP_TAX" ("TAX_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020497
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020497" ON "PSPADM"."PSP_TRANSACTION_OFFLOAD_BATCH" ("TRANSACTION_OFFLOAD_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020498
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020498" ON "PSPADM"."PSP_TRANSACTION_RESPONSE" ("TRANSACTION_RESPONSE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020499
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020499" ON "PSPADM"."PSP_TRANSACTION_RETURN" ("TRANSACTION_RETURN_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020500
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020500" ON "PSPADM"."PSP_TRANSACTION_RETURN_BATCH" ("TRANSACTION_RETURN_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020501
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020501" ON "PSPADM"."PSP_ACTION_EVENT" ("CODE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020502
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020502" ON "PSPADM"."PSP_AUTH_DOMAIN" ("DOMAIN_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020503
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020503" ON "PSPADM"."PSP_AUTH_OPERATION" ("OPERATION_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020504
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020504" ON "PSPADM"."PSP_COLLECTION_STAGE" ("COLLECTION_STAGE_CODE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020505
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020505" ON "PSPADM"."PSP_EVENT_DETAIL_TYPE" ("EVENT_DETAIL_TYPE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020506
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020506" ON "PSPADM"."PSP_PAYROLL_FREQUENCY" ("PAYROLL_FREQ_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020507
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020507" ON "PSPADM"."PSP_POSTING_RULE" ("POSTING_RULE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020508
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020508" ON "PSPADM"."PSP_RETURN_REASON_DESC" ("REASON_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020509
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020509" ON "PSPADM"."PSP_SERVICE_STATUS" ("SERVICE_STATUS_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020510
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020510" ON "PSPADM"."PSP_SERVICE_SUB_STATUS" ("SERVICE_SUB_STATUS_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020511
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020511" ON "PSPADM"."PSP_SYSTEM_CAPABILITY" ("SYSTEM_CAPABILITY_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020512
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020512" ON "PSPADM"."PSP_BANK_HOLIDAY" ("BANK_HOLIDAY_DATE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020513
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020513" ON "PSPADM"."PSP_EVENT_TYPE" ("EVENT_TYPE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020514
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020514" ON "PSPADM"."PSP_FUNDING_MODEL" ("FUNDING_MODEL_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020515
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020515" ON "PSPADM"."PSP_LEDGER_ACCOUNT" ("LEDGER_ACCOUNT_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020516
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020516" ON "PSPADM"."PSP_SERVICE" ("SERVICE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020517
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020517" ON "PSPADM"."PSP_SOURCE_SYSTEM" ("SOURCE_SYSTEM_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020518
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020518" ON "PSPADM"."PSP_TRANSACTION_STATE" ("TRANSACTION_STATE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020519
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020519" ON "PSPADM"."PSP_TRANSACTION_TYPE" ("TRANSACTION_TYPE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020520
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020520" ON "PSPADM"."PSP_AUTHROLE_OPERATION_ASSOC" ("AUTH_ROLE_FK", "AUTH_OPERATION_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020521
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020521" ON "PSPADM"."PSP_OFFER_SVCCHG_ASSOC" ("OFFER_FK", "OFFERING_SERVICE_CHARGE_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020522
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020522" ON "PSPADM"."PSP_FINTXN_ONHOLDREASON_ASSOC" ("FINANCIAL_TRANSACTION_FK", "ON_HOLD_REASON_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020523
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020523" ON "PSPADM"."PSP_SVCSTAT_SYSCAP_ASSOC" ("SERVICE_SUB_STATUS_FK", "SYSTEM_CAPABILITY_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020524
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020524" ON "PSPADM"."PSP_SVCSTAT_SRCSYS_ASSOC" ("SERVICE_SUB_STATUS_FK", "SOURCE_SYSTEM_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020525
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020525" ON "PSPADM"."PSP_SVCSTAT_SVC_ASSOC" ("SERVICE_SUB_STATUS_FK", "SERVICE_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020526
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020526" ON "PSPADM"."PSP_EVTTP_SRCSYS_ASSOC" ("INTERESTING_EVENT_TYPES_FK", "SOURCE_SYSTEM_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020527
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020527" ON "PSPADM"."PSP_COMPANY_PIN" ("COMPANY_PIN_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020528
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020528" ON "PSPADM"."PSP_OFFER_PRICE" ("OFFER_PRICE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020529
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020529" ON "PSPADM"."PSP_BATCH_JOB_SETUP" ("JOB_TYPE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020534
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020534" ON "PSPADM"."Z_TEMP_DIY_RESET_COMPANY" ("SOURCE_COMPANY_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020536
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020536" ON "PSPADM"."PSP_APPLIED_DATABASE_PATCH" ("APPLIED_DATABASE_PATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020539
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020539" ON "PSPADM"."PSP_COMPANY_EVENT_EMAIL" ("COMPANY_EVENT_EMAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020540
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020540" ON "PSPADM"."PSP_COMPANY_EVENT_EMAIL_PARAM" ("COMPANY_EVENT_EMAIL_PARAM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020541
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020541" ON "PSPADM"."PSP_ACH_TRANSACTION_CODE" ("TRANSACTION_CODE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020542
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020542" ON "PSPADM"."PSP_FORECAST" ("FORECAST_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020543
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020543" ON "PSPADM"."PSP_EVENT_LOG" ("EVENT_LOG_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020544
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020544" ON "PSPADM"."PSP_FORECAST_DETAIL" ("FORECAST_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020545
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020545" ON "PSPADM"."PSP_TXNTYPE_SERVICE_ASSOC" ("TRANSACTION_TYPE_FK", "SERVICE_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020546
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020546" ON "PSPADM"."PSP_PAYMENT_TEMPLATE" ("PAYMENT_TEMPLATE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020547
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020547" ON "PSPADM"."PSP_LAW" ("LAW_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020548
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020548" ON "PSPADM"."PSP_AGENCY" ("AGENCY_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020549
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020549" ON "PSPADM"."PSP_REPORTING_AGENT" ("REPORTING_AGENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020550
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020550" ON "PSPADM"."PSP_RAFENROLLMENT" ("RAFENROLLMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020551
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020551" ON "PSPADM"."PSP_COMPANY_AGENCY" ("COMPANY_AGENCY_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020552
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020552" ON "PSPADM"."PSP_EFTPS_ENROLLMENT" ("EFTPS_ENROLLMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020553
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020553" ON "PSPADM"."PSP_ACHENROLLMENT" ("ACHENROLLMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020554
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020554" ON "PSPADM"."PSP_ATFDATA_EXTRACT_BATCH" ("ATFDATA_EXTRACT_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020555
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020555" ON "PSPADM"."PSP_FORM_TEMPLATE" ("FORM_TEMPLATE_CD") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020556
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020556" ON "PSPADM"."PSP_ATFDATA_EXTRACT_FILE" ("ATFDATA_EXTRACT_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020557
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020557" ON "PSPADM"."PSP_EMPLOYEE_WAGE_PLAN" ("EMPLOYEE_WAGE_PLAN_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020558
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020558" ON "PSPADM"."PSP_COMP_ADJUST_SUBMISSION" ("COMP_ADJUST_SUBMISSION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020559
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020559" ON "PSPADM"."PSP_TAX_PENALTY_INTEREST" ("TAX_PENALTY_INTEREST_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020560
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020560" ON "PSPADM"."PSP_LIABILITY_ADJUSTMENT" ("LIABILITY_ADJUSTMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020561
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020561" ON "PSPADM"."PSP_PMT_TEMPLATE_FREQUENCY" ("PAYMENT_TEMPLATE_FREQUENCY_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020562
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020562" ON "PSPADM"."PSP_COMPANY_LAW" ("COMPANY_LAW_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020563
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020563" ON "PSPADM"."PSP_COMPANYAGENCY_PMTTEMPLATE" ("COMPANYAGENCY_PMTTEMPLATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020564
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020564" ON "PSPADM"."PSP_USER_PREFERENCE" ("KEY") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020565
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020565" ON "PSPADM"."PSP_USER_SETTING" ("USER_SETTING_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020566
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020566" ON "PSPADM"."PSP_PAYEE" ("PAYEE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020567
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020567" ON "PSPADM"."PSP_TP401KCOMPANY_SERVICE_INFO" ("TP401KCOMPANY_SERVICE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020568
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020568" ON "PSPADM"."PSP_PAYEE_BANK_ACCOUNT" ("PAYEE_BANK_ACCOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020569
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020569" ON "PSPADM"."PSP_BILL_PAYMENT" ("BILL_PAYMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020570
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020570" ON "PSPADM"."PSP_BILL_PAYMENT_SPLIT" ("BILL_PAYMENT_SPLIT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020571
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020571" ON "PSPADM"."PSP_THIRD_PARTY401K_BATCH" ("THIRD_PARTY401K_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020572
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020572" ON "PSPADM"."PSP_PAYROLL_ITEM" ("PAYROLL_ITEM_CODE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020573
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020573" ON "PSPADM"."PSP_COMPANY_PAYROLL_ITEM" ("COMPANY_PAYROLL_ITEM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020574
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020574" ON "PSPADM"."PSP_TP401K_SIGNUP_QUEUE" ("TP401K_SIGNUP_QUEUE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020575
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020575" ON "PSPADM"."PSP_TP401K_SIGNUP_BATCH" ("TP401K_SIGNUP_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020576
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020576" ON "PSPADM"."PSP_INTUIT_SHIPPER_INFO" ("INTUIT_SHIPPER_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020577
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020577" ON "PSPADM"."PSP_CHECK_PRINT_SIGNATURE" ("CHECK_PRINT_SIGNATURE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020578
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020578" ON "PSPADM"."PSP_CHECK_PRINT_BATCH" ("CHECK_PRINT_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020579
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020579" ON "PSPADM"."PSP_EMPLOYER_CONTRIBUTION" ("EMPLOYER_CONTRIBUTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020580
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020580" ON "PSPADM"."PSP_TP401K_BATCH_PAYCHECK" ("TP401K_BATCH_PAYCHECK_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020581
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020581" ON "PSPADM"."PSP_CHECK_PRINT_PAYCHECK" ("CHECK_PRINT_PAYCHECK_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020582
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020582" ON "PSPADM"."PSP_CDCOMPANY_SERVICE_INFO" ("CDCOMPANY_SERVICE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020583
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020583" ON "PSPADM"."PSP_BPCOMPANY_SERVICE_INFO" ("BPCOMPANY_SERVICE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020584
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020584" ON "PSPADM"."PSP_TP401K_BATCH_EMPLOYEE" ("TP401K_BATCH_EMPLOYEE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020585
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020585" ON "PSPADM"."PSP_SYSTEM_PARAMETER" ("SYSTEM_PARAMETER_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020586
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020586" ON "PSPADM"."PSP_ON_HOLD_REASON" ("ON_HOLD_REASON_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020587
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020587" ON "PSPADM"."PSP_PROPERTY_AUDIT" ("PROPERTY_AUDIT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020588
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020588" ON "PSPADM"."PSP_PAYROLL_FRAUD_BATCH" ("PAYROLL_FRAUD_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020589
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020589" ON "PSPADM"."PSP_PAYROLL_RUN_ACTION" ("PAYROLL_RUN_ACTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020590
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020590" ON "PSPADM"."PSP_PAYROLL_SUBTYPE" ("PAYROLL_SUBTYPE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020591
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020591" ON "PSPADM"."PSP_ROLE_SUB_STATUS" ("ROLE_SUB_STATUS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020592
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020592" ON "PSPADM"."PSP_SECOND_OFFLOAD" ("SECOND_OFFLOAD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020593
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020593" ON "PSPADM"."PSP_SERV_STAT_TXN_SKU_TYPE" ("SERV_STAT_TXN_SKU_TYPE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020594
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020594" ON "PSPADM"."PSP_SVCCHGPRICE" ("SVCCHGPRICE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020595
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020595" ON "PSPADM"."PSP_COMPANY_OFFERING" ("COMPANY_OFFERING_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020596
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020596" ON "PSPADM"."PSP_FRAUD_BANK_ACCOUNT" ("FRAUD_BANK_ACCOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020598
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020598" ON "PSPADM"."PSP_TAX_CREDITS_APPLICATION" ("TAX_CREDITS_APPLICATION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020599
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020599" ON "PSPADM"."PSP_QBDT_TRANSACTION_INFO" ("QBDT_TRANSACTION_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020600
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020600" ON "PSPADM"."PSP_QBDT_PAYROLL_ITEM_INFO" ("QBDT_PAYROLL_ITEM_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020601
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020601" ON "PSPADM"."PSP_QBDT_PAYLINE_INFO" ("QBDT_PAYLINE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020602
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020602" ON "PSPADM"."PSP_QBDT_PAYCHECK_INFO" ("QBDT_PAYCHECK_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020603
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020603" ON "PSPADM"."PSP_QBDT_EMPLOYEE_INFO" ("QBDT_EMPLOYEE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020604
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020604" ON "PSPADM"."PSP_SOURCE_SYSTEM_LAW_ASSOC" ("SOURCE_SYSTEM_LAW_ASSOC_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020605
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020605" ON "PSPADM"."PSP_DEPOSIT_FREQUENCY" ("DEPOSIT_FREQUENCY_CODE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020606
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020606" ON "PSPADM"."PSP_QBDT_UNPROCESSED_REQUEST" ("QBDT_UNPROCESSED_REQUEST_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020607
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020607" ON "PSPADM"."PSP_LIABILITY_CHECK" ("LIABILITY_CHECK_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020608
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020608" ON "PSPADM"."PSP_EMPLOYEE_PAYROLL_ITEM" ("EMPLOYEE_PAYROLL_ITEM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020609
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020609" ON "PSPADM"."PSP_EMPLOYEE_CUSTOM_FIELD" ("EMPLOYEE_CUSTOM_FIELD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020610
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020610" ON "PSPADM"."PSP_EMPLOYEE_ACCRUAL" ("EMPLOYEE_ACCRUAL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020611
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020611" ON "PSPADM"."PSP_PAYROLL_ITEM_TAXABLE_TO" ("PAYROLL_ITEM_TAXABLE_TO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020612
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020612" ON "PSPADM"."PSP_LIABILITY_CHECK_LINE" ("LIABILITY_CHECK_LINE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020613
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020613" ON "PSPADM"."PSP_EMPLOYEE_TAX" ("EMPLOYEE_TAX_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020614
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020614" ON "PSPADM"."PSP_COMPANY_LAW_RATE" ("COMPANY_LAW_RATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020615
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020615" ON "PSPADM"."PSP_TAX_TABLE_MISC_DATA" ("TAX_TABLE_MISC_DATA_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020616
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020616" ON "PSPADM"."PSP_FRAUD_CONTACT" ("FRAUD_CONTACT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020617
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020617" ON "PSPADM"."PSP_FRAUD_COMPANY" ("FRAUD_COMPANY_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020618
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020618" ON "PSPADM"."PSP_FRAUD_ADDRESS" ("FRAUD_ADDRESS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020619
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020619" ON "PSPADM"."PSP_SQL_EXECUTION_LOG_ENTRY" ("SQL_EXECUTION_LOG_ENTRY_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020620
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020620" ON "PSPADM"."PSP_PAY_ITEM" ("PAY_ITEM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020621
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020621" ON "PSPADM"."PSP_EFTPS_FILE" ("EFTPS_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020622
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020622" ON "PSPADM"."PSP_EFTPS_ENROLLMENT_DETAIL" ("EFTPS_ENROLLMENT_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020623
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020623" ON "PSPADM"."PSP_EFTPS_PAYMENT_DETAIL" ("EFTPS_PAYMENT_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020624
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020624" ON "PSPADM"."PSP_RAFENROLLMENT_FILE" ("RAFENROLLMENT_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020625
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020625" ON "PSPADM"."PSP_COMPANYAGENCY_FRMTEMPLATE" ("COMPANYAGENCY_FRMTEMPLATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020626
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020626" ON "PSPADM"."PSP_RAFENROLLMENT_DETAIL" ("RAFENROLLMENT_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020627
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020627" ON "PSPADM"."PSP_TAX_PAYMENT_ON_HOLD_REASON" ("TAX_PAYMENT_ON_HOLD_REASON_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020628
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020628" ON "PSPADM"."PSP_ENTITY_CHANGE" ("ENTITY_CHANGE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020629
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020629" ON "PSPADM"."PSP_PRIOR_PAYMENT_SUBMISSION" ("PRIOR_PAYMENT_SUBMISSION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020630
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020630" ON "PSPADM"."PSP_RACOMPANY_SERVICE_INFO" ("RACOMPANY_SERVICE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020631
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020631" ON "PSPADM"."PSP_PMT_TEMPLATE_PAYMENTMETHOD" ("PMT_TEMPLATE_PAYMENTMETHOD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020632
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020632" ON "PSPADM"."PSP_PMT_TEMPLATE_BANKACCOUNT" ("PMT_TEMPLATE_BANKACCOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020633
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020633" ON "PSPADM"."PSP_BATCH_JOB_PARAMETER" ("ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020634
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020634" ON "PSPADM"."PSP_COMPANY_DAILY_LIABILITY" ("COMPANY_DAILY_LIABILITY_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020635
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020635" ON "PSPADM"."PSP_ATFPAYROLLS_TO_PROCESS" ("ATFPAYROLLS_TO_PROCESS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020636
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020636" ON "PSPADM"."PSP_FRAUD_EVENT" ("FRAUD_EVENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020637
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020637" ON "PSPADM"."PSP_PERF_SST" ("TIME_PACIFIC", "TRANSMISSION_TYPE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020638
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020638" ON "PSPADM"."PSP_TP401K_PAYCHECK" ("TP401K_PAYCHECK_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020639
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020639" ON "PSPADM"."PSP_TP401K_PAYCHECK_STATE" ("TP401K_PAYCHECK_STATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020640
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020640" ON "PSPADM"."PSP_TP401K_PAYCHECK_PENDING" ("TP401K_PAYCHECK_PENDING_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020641
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020641" ON "PSPADM"."PSP_SOURCESYS_PRINTEDCHK_INFO" ("SOURCESYS_PRINTEDCHK_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020642
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020642" ON "PSPADM"."PSP_PMTTEMPLATE_PRINTEDCHKINFO" ("PMTTEMPLATE_PRINTEDCHKINFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020643
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020643" ON "PSPADM"."PSP_COMPANY_PAYCHECK_BATCH" ("COMPANY_PAYCHECK_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020644
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020644" ON "PSPADM"."PSP_AGENCY_CHECK_BATCH" ("AGENCY_CHECK_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020645
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020645" ON "PSPADM"."PSP_PMTTEMPLATE_CHKINFO_ASSOC" ("PMTTEMPLATE_CHKINFO_ASSOC_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020646
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020646" ON "PSPADM"."PSP_PAYMENT_TEMPLATE_AGENCY_ID" ("PAYMENT_TEMPLATE_AGENCY_ID_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020647
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020647" ON "PSPADM"."PSP_PAYMENT_METHOD_REQUIREMENT" ("PAYMENT_METHOD_REQUIREMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020648
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020648" ON "PSPADM"."PSP_VOIDED_CHECK" ("VOIDED_CHECK_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020649
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020649" ON "PSPADM"."PSP_SYSTEM_REQUIREMENT" ("SYSTEM_REQUIREMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020650
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020650" ON "PSPADM"."PSP_MANUAL_REQUIREMENT" ("MANUAL_REQUIREMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020651
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020651" ON "PSPADM"."PSP_AGENCY_ID_REQUIREMENT" ("AGENCY_ID_REQUIREMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020652
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020652" ON "PSPADM"."PSP_PAYMENT_BATCH_ASSOC" ("PAYMENT_BATCH_ASSOC_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020653
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020653" ON "PSPADM"."PSP_COMP_PMTTEMPLATE_PMTMETHOD" ("COMP_PMTTEMPLATE_PMTMETHOD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020654
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020654" ON "PSPADM"."PSP_COMP_PMT_TEMPLATE_AGENCYID" ("COMP_PMT_TEMPLATE_AGENCYID_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020655
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020655" ON "PSPADM"."PSP_STATE_REPORT_OUTPUT" ("STATE_REPORT_OUTPUT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020656
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020656" ON "PSPADM"."PSP_STATE_REPORT_ASSOC" ("STATE_REPORT_ASSOC_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020657
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020657" ON "PSPADM"."PSP_ACCOUNTING_REPORT_FILE" ("ACCOUNTING_REPORT_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020658
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020658" ON "PSPADM"."PSP_PAYMENT_REQUIREMENT" ("PAYMENT_REQUIREMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020659
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020659" ON "PSPADM"."PSP_THRESHOLD_REQUIREMENT" ("THRESHOLD_REQUIREMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020660
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020660" ON "PSPADM"."PSP_DEPOSIT_FREQUENCY_REQ" ("DEPOSIT_FREQUENCY_REQ_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020661
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020661" ON "PSPADM"."PSP_SYSTEM_PAYMENT_REQUIREMENT" ("SYSTEM_PAYMENT_REQUIREMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020662
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020662" ON "PSPADM"."PSP_ENTITLEMENT" ("ENTITLEMENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020663
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020663" ON "PSPADM"."PSP_ENTITLEMENT_CODE" ("ENTITLEMENT_CODE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020664
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020664" ON "PSPADM"."PSP_TAX_COMPANY_SERVICE_INFO" ("TAX_COMPANY_SERVICE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020666
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020666" ON "PSPADM"."PSP_EDI_TAX_FILE" ("EDI_TAX_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020667
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020667" ON "PSPADM"."PSP_QUICKBOOKS_INFO" ("QUICKBOOKS_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020668
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020668" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANSACTION" ("QBDT_PAYROLL_TRANSACTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020669
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020669" ON "PSPADM"."PSP_QBDT_PAYROLL_TRANS_LINE" ("QBDT_PAYROLL_TRANS_LINE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020670
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020670" ON "PSPADM"."PSP_STATE_EDI_TAX_FILE" ("STATE_EDI_TAX_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020671
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020671" ON "PSPADM"."PSP_EDI_PAYMENT_DETAIL" ("EDI_PAYMENT_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020672
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020672" ON "PSPADM"."PSP_DELETED_RECORD" ("DELETED_RECORD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020673
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020673" ON "PSPADM"."PSP_ATFPAYMENTS_TO_PROCESS" ("ATFPAYMENTS_TO_PROCESS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020674
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020674" ON "PSPADM"."PSP_QBDT_REQUEST_INFO" ("QBDT_REQUEST_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020675
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020675" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ("ENTITLEMENT_MESSAGE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020676
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020676" ON "PSPADM"."PSP_ENTITLEMENT_CODE_OFFERING" ("ENTITLEMENT_CODE_OFFERING_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020677
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020677" ON "PSPADM"."PSP_ENTITLEMENT_UNIT" ("ENTITLEMENT_UNIT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020678
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020678" ON "PSPADM"."PSP_EVENT_AS400_SYNC" ("EVENT_AS400_SYNC_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020679
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020679" ON "PSPADM"."PSP_AUTH_USER_AUTH_ROLE__ASSOC" ("AUTH_USER_FK", "AUTH_ROLE_FK") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020680
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020680" ON "PSPADM"."PSP_WAGE_LIMIT" ("WAGE_LIMIT_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020681
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020681" ON "PSPADM"."PSP_LIMIT_RULE" ("LIMIT_RULE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020682
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020682" ON "PSPADM"."PSP_LIMIT_VALUE" ("LIMIT_VALUE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020683
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020683" ON "PSPADM"."PSP_LIAB_CHECK_BILLING_ASSOC" ("LIAB_CHECK_BILLING_ASSOC_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020684
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020684" ON "PSPADM"."PSP_DEPOSIT_FREQUENCY_FILE" ("DEPOSIT_FREQUENCY_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020685
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020685" ON "PSPADM"."PSP_DEPOSIT_FREQUENCY_FILE_REC" ("DEPOSIT_FREQUENCY_FILE_REC_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020686
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020686" ON "PSPADM"."PSP_COMPANY_USAGE" ("COMPANY_USAGE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020687
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020687" ON "PSPADM"."PSP_USAGE_PERIOD" ("USAGE_PERIOD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020688
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020688" ON "PSPADM"."PSP_BILL" ("BILL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020689
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020689" ON "PSPADM"."PSP_EMPLOYEE_USAGE" ("EMPLOYEE_USAGE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020690
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020690" ON "PSPADM"."PSP_FAILED_PAYROLL_RUN" ("FAILED_PAYROLL_RUN_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020691
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020691" ON "PSPADM"."PSP_PAYCHECK_USAGE" ("PAYCHECK_USAGE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020692
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020692" ON "PSPADM"."PSP_FRAUD_RULE" ("FRAUD_RULE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020693
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020693" ON "PSPADM"."PSP_FRAUD_VALUE" ("FRAUD_VALUE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020694
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020694" ON "PSPADM"."PSP_EMPLOYEE_LAW_QTR_TOTALS" ("EMPLOYEE_LAW_QTR_TOTALS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020695
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020695" ON "PSPADM"."PSP_EE_PAYROLLITEM_QTRTOTALS" ("EE_PAYROLLITEM_QTRTOTALS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020696
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020696" ON "PSPADM"."PSP_EMPLOYEE_W2_TOTALS" ("EMPLOYEE_W2_TOTALS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020697
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020697" ON "PSPADM"."PSP_ADDITIONAL_FILING_AMOUNT" ("NAME") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020698
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020698" ON "PSPADM"."PSP_COMPANY_FILING_AMOUNT" ("COMPANY_FILING_AMOUNT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020699
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020699" ON "PSPADM"."PSP_HOURS_WORKED_EXCEPTION" ("HOURS_WORKED_EXCEPTION_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020700
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020700" ON "PSPADM"."PSP_COMPANY_TFSSUBMISSION" ("COMPANY_TFSSUBMISSION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020701
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020701" ON "PSPADM"."PSP_FSET_FILE" ("FSET_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020702
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020702" ON "PSPADM"."PSP_FSET_FILING_DETAIL" ("FSET_FILING_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020703
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020703" ON "PSPADM"."PSP_SAP_METHOD_CALL" ("SAP_METHOD_CALL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020704
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020704" ON "PSPADM"."PSP_WC_PAYCHECK" ("WC_PAYCHECK_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020705
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020705" ON "PSPADM"."PSP_WC_PAYCHECK_STATE" ("WC_PAYCHECK_STATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020706
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020706" ON "PSPADM"."PSP_WC_PAYCHECK_PENDING" ("WC_PAYCHECK_PENDING_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020707
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020707" ON "PSPADM"."PSP_DISBURSE_ADVICE" ("DISBURSE_ADVICE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020708
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020708" ON "PSPADM"."PSP_DISBURSE_ADVICE_TAX_LIAB" ("DISBURSE_ADVICE_TAX_LIAB_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020709
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020709" ON "PSPADM"."PSP_LEDGER_OPERATION_JOB" ("LEDGER_OPERATION_JOB_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020710
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020710" ON "PSPADM"."PSP_LEDGER_OPERATION" ("LEDGER_OPERATION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020711
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020711" ON "PSPADM"."PSP_ANNUAL_BILLING_BATCH" ("ANNUAL_BILLING_BATCH_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020712
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020712" ON "PSPADM"."PSP_ANNUAL_BILLING_ITEM" ("ANNUAL_BILLING_ITEM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020713
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020713" ON "PSPADM"."PSP_AGENCY_RATE_REQUEST" ("AGENCY_RATE_REQUEST_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020714
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020714" ON "PSPADM"."PSP_COMPANY_RATE_REQUEST" ("COMPANY_RATE_REQUEST_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020715
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020715" ON "PSPADM"."PSP_ACHENROLLMENT_FILE" ("ACHENROLLMENT_FILE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020716
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020716" ON "PSPADM"."PSP_ACHENROLLMENT_DETAIL" ("ACHENROLLMENT_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020717
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020717" ON "PSPADM"."PSP_PSTUB_EMPLOYER_INFO" ("PSTUB_EMPLOYER_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020718
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020718" ON "PSPADM"."PSP_PSTUB_EMPLOYEE_INFO" ("PSTUB_EMPLOYEE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020719
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020719" ON "PSPADM"."PSP_PSTUB_EMPLOYEE_PREFERENCE" ("PSTUB_EMPLOYEE_PREFERENCE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020720
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020720" ON "PSPADM"."PSP_PSTUB_ADDRESS" ("PSTUB_ADDRESS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020721
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020721" ON "PSPADM"."PSP_PAYSTUB" ("PAYSTUB_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020722
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020722" ON "PSPADM"."PSP_PSTUB_DDITEM" ("PSTUB_DDITEM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020723
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020723" ON "PSPADM"."PSP_PSTUB_MSG" ("PSTUB_MSG_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020724
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020724" ON "PSPADM"."PSP_PSTUB_PAID_TIMEOFF_ITEM" ("PSTUB_PAID_TIMEOFF_ITEM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020725
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020725" ON "PSPADM"."PSP_PSTUB_PAY_ITEM" ("PSTUB_PAY_ITEM_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020726
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020726" ON "PSPADM"."PSP_LAW_RATE_RANGE" ("LAW_RATE_RANGE_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020727
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020727" ON "PSPADM"."PSP_LAW_RATE_VALUE" ("LAW_RATE_VALUE_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020728
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020728" ON "PSPADM"."PSP_RATE_LEDGER_OPERATION" ("RATE_LEDGER_OPERATION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020729
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020729" ON "PSPADM"."PSP_EMPLOYER_PREFERENCE" ("EMPLOYER_PREFERENCE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020730
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020730" ON "PSPADM"."PSP_ADE_LAW_MAP" ("ADE_LAW_MAP_ID") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020731
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020731" ON "PSPADM"."PSP_DEP_FREQ_LEDGER_OPERATION" ("DEP_FREQ_LEDGER_OPERATION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020732
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020732" ON "PSPADM"."PSP_COMPANY_EVENT" ("COMPANY_EVENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020733
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020733" ON "PSPADM"."PSP_EMP_TOTALS_PAYROLL_RUN" ("EMP_TOTALS_PAYROLL_RUN_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020734
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020734" ON "PSPADM"."PSP_SUICREDITS_JOB" ("SUICREDITS_JOB_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020735
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020735" ON "PSPADM"."PSP_BATCH_JOB_STATUS" ("BATCH_JOB_STATUS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020736
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020736" ON "PSPADM"."PSP_INDUSTRY_TYPE" ("INDUSTRY_TYPE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020737
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020737" ON "PSPADM"."PSP_COMPANY_ADDITIONAL_INFO" ("COMPANY_ADDITIONAL_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020738
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020738" ON "PSPADM"."PSP_IOPSYNC_COMPANY" ("IOPSYNC_COMPANY_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020739
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020739" ON "PSPADM"."PSP_PSTUB_STATE_TAX_INFO" ("PSTUB_STATE_TAX_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020740
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020740" ON "PSPADM"."PSP_COMPANY_CONSENT" ("COMPANY_CONSENT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020741
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020741" ON "PSPADM"."GG_HEARTBEAT" ("SOURCE") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020742
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020742" ON "PSPADM"."PSP_REPORT_JOB_SETUP" ("REPORT_NAME") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020743
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020743" ON "PSPADM"."PSP_VMP_EMPLOYEE_INFO" ("VMP_EMPLOYEE_INFO_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0020744
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0020744" ON "PSPADM"."PSP_SAVED_REPORTS" ("SAVED_REPORTS_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0032991
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0032991" ON "PSPADM"."PSP_PAYCHECK_USAGE_HIST" ("PAYCHECK_USAGE_HIST_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0055948
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0055948" ON "PSPADM"."PSP_RTBAUTOMATIONBACKUP" ("RTBAUTOMATIONBACKUP_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0058710
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0058710" ON "PSPADM"."PSP_ASST_BUNDLE_COMP_USAGE" ("ASST_BUNDLE_COMP_USAGE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0058718
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0058718" ON "PSPADM"."PSP_ASSISTED_BUNDLE_BILL" ("ASSISTED_BUNDLE_BILL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0058725
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0058725" ON "PSPADM"."PSP_ASST_BUNDLE_BILL_DETAIL" ("ASST_BUNDLE_BILL_DETAIL_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0060128
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0060128" ON "PSPADM"."PSP_MESSAGE_LOG" ("MESSAGE_LOG_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C0070166
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C0070166" ON "PSPADM"."PSP_ENTITY_UPDATE_HIST" ("ENTITY_UPDATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C008379
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C008379" ON "PSPADM"."PSP_ENTRY_DETAIL_RECORD" ("ENTRY_DETAIL_RECORD_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C008432
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C008432" ON "PSPADM"."PSP_LEDGER_BALANCE" ("LEDGER_BALANCE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C008675
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C008675" ON "PSPADM"."PSP_FINANCIAL_TRANSACTION" ("FINANCIAL_TRANSACTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C008683
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C008683" ON "PSPADM"."PSP_FINANCIAL_TRANS_STATE" ("FINANCIAL_TRANS_STATE_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C008717
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C008717" ON "PSPADM"."PSP_MONEY_MOVEMENT_TRANSACTION" ("MONEY_MOVEMENT_TRANSACTION_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C008737
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C008737" ON "PSPADM"."PSP_PAYCHECK" ("PAYCHECK_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_C008745
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_C008745" ON "PSPADM"."PSP_PAYCHECK_SPLIT" ("PAYCHECK_SPLIT_SEQ") ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124618C00011$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124618C00011$$" ON "PSPADM"."Z_REDEF_SRC_SYS_TRANSMISSION" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124618C00012$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124618C00012$$" ON "PSPADM"."Z_REDEF_SRC_SYS_TRANSMISSION" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124631C00008$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124631C00008$$" ON "PSPADM"."PSP_CHECK_PRINT_SIGNATURE" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124634C00008$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124634C00008$$" ON "PSPADM"."PSP_TAX_CREDITS_APPLICATION" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124634C00009$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124634C00009$$" ON "PSPADM"."PSP_TAX_CREDITS_APPLICATION" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124639C00009$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124639C00009$$" ON "PSPADM"."PSP_SQL_EXECUTION_LOG_ENTRY" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124642C00012$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124642C00012$$" ON "PSPADM"."PSP_SOURCESYS_PRINTEDCHK_INFO" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124642C00013$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124642C00013$$" ON "PSPADM"."PSP_SOURCESYS_PRINTEDCHK_INFO" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124704C00010$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124704C00010$$" ON "PSPADM"."PSP_STATE_REPORT_OUTPUT" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124890C00010$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124890C00010$$" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124890C00019$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124890C00019$$" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124890C00020$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124890C00020$$" ON "PSPADM"."PSP_ENTITLEMENT_MESSAGE" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124899C00012$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124899C00012$$" ON "PSPADM"."PSP_ACHENROLLMENT_FILE" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124899C00013$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124899C00013$$" ON "PSPADM"."PSP_ACHENROLLMENT_FILE" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124920C00012$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124920C00012$$" ON "PSPADM"."PSP_LEDGER_OPERATION_JOB" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124920C00013$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124920C00013$$" ON "PSPADM"."PSP_LEDGER_OPERATION_JOB" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000124938C00012$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000124938C00012$$" ON "PSPADM"."PSP_SUICREDITS_JOB" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000125047C00011$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000125047C00011$$" ON "PSPADM"."PSP_SAVED_REPORTS" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000126541C00008$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000126541C00008$$" ON "PSPADM"."PSP_TAX_CREDITS9061" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000126623C00030$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000126623C00030$$" ON "PSPADM"."STATS_11DEC" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000174992C00008$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000174992C00008$$" ON "PSPADM"."PSP_RTBAUTOMATIONBACKUP" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000181442C00008$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000181442C00008$$" ON "PSPADM"."PSP_MESSAGE_LOG" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000181442C00010$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000181442C00010$$" ON "PSPADM"."PSP_MESSAGE_LOG" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000194817C00010$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000194817C00010$$" ON "PSPADM"."PSP_ENTITY_UPDATE_HIST" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000259372C00010$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000259372C00010$$" ON "PSPADM"."PSP_SMSMIGRATION" ;
--------------------------------------------------------
--  DDL for Index SYS_IL0000342027C00010$$
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."SYS_IL0000342027C00010$$" ON "PSPADM"."PSP_ENTITY_UPDATE" (
   LOCAL
 (PARTITION "SYS_IL_P65179" NOCOMPRESS , 
 PARTITION "SYS_IL_P65180" NOCOMPRESS , 
 PARTITION "SYS_IL_P65181" NOCOMPRESS , 
 PARTITION "SYS_IL_P65182" NOCOMPRESS ) ;
--------------------------------------------------------
--  DDL for Index TPSQL_IDX
--------------------------------------------------------

  CREATE UNIQUE INDEX "PSPADM"."TPSQL_IDX" ON "PSPADM"."TOAD_PLAN_SQL" ("STATEMENT_ID") ;
