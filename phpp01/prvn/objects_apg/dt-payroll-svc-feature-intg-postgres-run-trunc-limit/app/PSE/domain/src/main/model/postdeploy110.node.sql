

-------this is a post deploy to make the following columns unused.
---We need to run this once all node post deploys are done.

select systimestamp from dual;

alter table psp_deduction   set unused column QBDT_PAYLINE_INFO_FK cascade constraints;
alter table psp_compensation   set unused column QBDT_PAYLINE_INFO_FK cascade constraints;
alter table psp_employer_contribution   set unused column QBDT_PAYLINE_INFO_FK cascade constraints;
alter table PSP_COMPANY_LAW set unused column QBDT_PAYROLL_ITEM_INFO_FK cascade constraints;

alter table psp_employee    set unused column QBDT_EMPLOYEE_INFO_FK cascade constraints;
alter table PSP_COMPANY_PAYROLL_ITEM set unused column QBDT_PAYROLL_ITEM_INFO_FK cascade constraints;

alter table PSP_PAYCHECK set unused column QBDT_PAYCHECK_INFO_FK cascade constraints;
alter table PSP_FINANCIAL_TRANSACTION  SET UNUSED COLUMN qbdt_transaction_info_fk cascade constraints;

select systimestamp from dual;