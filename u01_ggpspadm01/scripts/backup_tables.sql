create table MCHOUBEY.PSP_BANK_ACCOUNT_bk as select /*+ parallel(8) */* from PSPADM.PSP_BANK_ACCOUNT;
create table MCHOUBEY.psp_offer_bk as select * from PSPADM.PSP_OFFER;
create table MCHOUBEY.psp_report_job_setup_bk as select * from PSPADM.PSP_REPORT_JOB_SETUP;
create table MCHOUBEY.psp_system_parameter_bk as select * from PSPADM.psp_system_parameter;
create table MCHOUBEY.psp_reporting_agent_bk as select * from PSPADM.psp_reporting_agent;

