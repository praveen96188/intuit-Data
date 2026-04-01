spool enable_triggers_on_B
set echo on feedback on timing on

-- Setting Tag 2 to disable DDL replication on C2
exec dbms_streams.set_tag (hextoraw(2));

alter trigger pspadm.PSP_COMPANY_PAYROLL_ITEM_AT enable;
alter trigger pspadm.PSP_CMPMTMPLT_PMTMTD_AT enable;
alter trigger pspadm.PSP_QUICKBOOKS_INFO_AT enable;
alter trigger pspadm.PSP_ADDRESS_AT enable;
alter trigger pspadm.PSP_ENTITY_CHANGE_AT enable;
alter trigger pspadm.PSP_COMPANY_BANK_ACCOUNT_AT enable;
alter trigger pspadm.PSP_BPCOMPANY_SERVICE_INFO_AT enable;
alter trigger pspadm.PSP_COMPANY_AT enable;
alter trigger pspadm.PSP_DDCOMPANY_SERVICE_INFO_AT enable;
alter trigger pspadm.PSP_EMPLOYEE_AT enable;
alter trigger pspadm.PSP_COMPANY_LAW_AT enable;
alter trigger pspadm.PSP_INDIVIDUAL_AT enable;
alter trigger pspadm.PSP_TAX_PENALTY_INTEREST_AT enable;
alter trigger pspadm.TR_UPD_COMPANY_EVENT_TIMESTAMP enable;
alter trigger pspadm.TR_UPD_DD_LIMITS enable;
alter trigger pspadm.PSP_COMPANY_AGENCY_AT enable;
alter trigger pspadm.PSP_CMPNYAGENCY_PMTTPLT_AT enable;
alter trigger pspadm.PSP_COMPANY_OFFER_AT enable;
alter trigger pspadm.PSP_TAX_CS_INFO_AT enable;
alter trigger pspadm.PSP_COMPANY_SERVICE_AT enable;
alter trigger pspadm.PSP_CONTACT_AT enable;
alter trigger pspadm.PSP_MONEY_MVMT_TRANS_AT enable;

spool off
