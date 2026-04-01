spool disable_triggers_on_B
set echo on feedback on timing on

alter trigger pspadm.PSP_COMPANY_PAYROLL_ITEM_AT disable;
alter trigger pspadm.PSP_CMPMTMPLT_PMTMTD_AT disable;
alter trigger pspadm.PSP_QUICKBOOKS_INFO_AT disable;
alter trigger pspadm.PSP_ADDRESS_AT disable;
alter trigger pspadm.PSP_ENTITY_CHANGE_AT disable;
alter trigger pspadm.PSP_COMPANY_BANK_ACCOUNT_AT disable;
alter trigger pspadm.PSP_BPCOMPANY_SERVICE_INFO_AT disable;
alter trigger pspadm.PSP_COMPANY_AT disable;
alter trigger pspadm.PSP_DDCOMPANY_SERVICE_INFO_AT disable;
alter trigger pspadm.PSP_EMPLOYEE_AT disable;
alter trigger pspadm.PSP_COMPANY_LAW_AT disable;
alter trigger pspadm.PSP_INDIVIDUAL_AT disable;
alter trigger pspadm.PSP_TAX_PENALTY_INTEREST_AT disable;
alter trigger pspadm.TR_UPD_COMPANY_EVENT_TIMESTAMP disable;
alter trigger pspadm.TR_UPD_DD_LIMITS disable;
alter trigger pspadm.PSP_COMPANY_AGENCY_AT disable;
alter trigger pspadm.PSP_CMPNYAGENCY_PMTTPLT_AT disable;
alter trigger pspadm.PSP_COMPANY_OFFER_AT disable;
alter trigger pspadm.PSP_TAX_CS_INFO_AT disable;
alter trigger pspadm.PSP_COMPANY_SERVICE_AT disable;
alter trigger pspadm.PSP_CONTACT_AT disable;
alter trigger pspadm.PSP_MONEY_MVMT_TRANS_AT disable;

spool off
