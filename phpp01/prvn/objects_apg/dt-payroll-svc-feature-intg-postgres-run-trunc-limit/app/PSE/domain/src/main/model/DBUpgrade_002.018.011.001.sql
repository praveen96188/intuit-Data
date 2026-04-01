--------------------------------------------------------------------------
-- Play this script in PREVIOUS_PSP_LOCAL@XE to make it look like PSP_LOCAL@XE
--
-- Please review the script before using it to make sure it won't
-- cause any unacceptable data loss.
--
-- PREVIOUS_PSP_LOCAL@XE Schema Extracted by User PREVIOUS_PSP_LOCAL 
-- PSP_LOCAL@XE Schema Extracted by User PSP_LOCAL 


-- Rename To be executed manually

Prompt Table PSP_SAVED_REPORTS;
CREATE TABLE PSP_SAVED_REPORTS
(
  SAVED_REPORTS_SEQ  VARCHAR2(255 CHAR)         NOT NULL,
  VERSION            NUMBER(19)                 NOT NULL,
  CREATOR_ID         VARCHAR2(30 CHAR),
  CREATED_DATE       TIMESTAMP(6)               NOT NULL,
  MODIFIER_ID        VARCHAR2(30 CHAR),
  MODIFIED_DATE      TIMESTAMP(6)               NOT NULL,
  REALM_ID           NUMBER(19)                 DEFAULT -1                    NOT NULL,
  REPORT_ID          VARCHAR2(256 CHAR),
  DISPLAY_NAME       VARCHAR2(256 CHAR),
  INPUT_PARAM        VARCHAR2(4000 CHAR),
  QUERY              CLOB,
  DESCRIPTION        VARCHAR2(512 CHAR)
)
NOPARALLEL;

ALTER TABLE PSP_SAVED_REPORTS
 ADD PRIMARY KEY
  (SAVED_REPORTS_SEQ, REALM_ID)
  USING INDEX;


--Renaming the columns
alter table PSP_COMPANY rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_ACHENROLLMENT_DETAIL rename column F_E_I_N  to F_E_I_N_PT;

alter table PSP_ACHENROLLMENT_DETAIL rename column AGENCY_ID  to AGENCY_ID_PT;

alter table PSP_BANK_ACCOUNT rename column ACCOUNT_NUMBER to ACCOUNT_NUMBER_PT;

alter table PSP_COMPANYAGENCY_PMTTEMPLATE rename column AGENCY_TAXPAYER_ID to AGENCY_TAXPAYER_ID_PT;

alter table PSP_COMPANY_CONSENT rename column FEIN to FEIN_PT;

alter table PSP_COMP_PMT_TEMPLATE_AGENCYID rename column AGENCY_TAXPAYER_ID to AGENCY_TAXPAYER_ID_PT;

alter table PSP_DEPOSIT_FREQUENCY_FILE_REC rename column E_I_N to E_I_N_PT;

alter table PSP_EDI_PAYMENT_DETAIL rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_EFTPS_ENROLLMENT_DETAIL rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_EFTPS_PAYMENT_DETAIL rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_ENTITLEMENT_UNIT rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_ENTITY_CHANGE rename column OLD_EIN to OLD_E_I_N_PT;

alter table PSP_ENTITY_CHANGE rename column NEW_EIN to NEW_E_I_N_PT;

alter table PSP_ENTRY_DETAIL_RECORD rename column RECORD_DATA to RECORD_DATA_PT;

alter table PSP_ENTRY_DETAIL_RECORD rename column TXP_RECORD_DATA to TXP_RECORD_DATA_PT;

alter table PSP_FRAUD_BANK_ACCOUNT rename column ACCOUNT_NUMBER to ACCOUNT_NUMBER_PT;

alter table PSP_FRAUD_COMPANY rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_FRAUD_EVENT rename column COMPANY_EIN to COMPANY_E_I_N_PT;

alter table PSP_FSET_FILING_DETAIL rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_FSET_FILING_DETAIL rename column AGENCY_ID to AGENCY_ID_PT;

alter table PSP_PAYEE rename column TAX_ID to TAX_ID_PT;

alter table PSP_PAYEE rename column ACCOUNT_NUMBER to ACCOUNT_NUMBER_PT;

alter table PSP_PSTUB_STATE_TAX_INFO rename column AGENCY_ID to AGENCY_ID_PT;

alter table PSP_QBDT_PAYROLL_ITEM_INFO rename column AGENCY_ID to AGENCY_ID_PT;

alter table PSP_RAFENROLLMENT_DETAIL rename column FED_TAXID to FED_TAXID_PT;

alter table PSP_REPORTING_AGENT rename column FED_ID to FED_ID_PT;

alter table PSP_REPORTING_AGENT rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_TP401K_SIGNUP_QUEUE rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_CONTACT rename column SOCIAL_SECURITY_NUMBER to SOCIAL_SECURITY_NUMBER_PT;

alter table PSP_CONTACT rename column DATE_OF_BIRTH to DATE_OF_BIRTH_PT;

alter table PSP_EMPLOYEE rename column BIRTH_DATE to BIRTH_DATE_PT;

alter table PSP_EMPLOYEE rename column TAX_ID to TAX_ID_PT;

alter table PSP_MONEY_MOVEMENT_TRANSACTION rename column AGENCY_TAXPAYER_ID to AGENCY_TAXPAYER_ID_PT;

alter table PSP_TAX_CREDITS9061 rename column FED_TAX_ID to FED_TAX_ID_PT;

alter table PSP_TAX_CREDITS9061 rename column S_S_N to S_S_N_PT;

PROMPT finished DBUpgrade_002.018.011.001.sql