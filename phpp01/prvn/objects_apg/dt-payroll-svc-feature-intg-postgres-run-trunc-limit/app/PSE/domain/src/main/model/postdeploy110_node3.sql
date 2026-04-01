--- THIS IS A POST DEPLOY SCRIPT.

select systimestamp as start_time_postdeploy_1 from dual;

Prompt Column IS_ARCHIVED;
ALTER TABLE PSP_COMPANY_PAYROLL_ITEM
 ADD (IS_ARCHIVED  NUMBER(1) DEFAULT 0);

Prompt Column IS_ARCHIVED;
ALTER TABLE PSP_EMPLOYEE
 ADD (IS_ARCHIVED  NUMBER(1) DEFAULT 0);

Prompt Column IS_ARCHIVED;
ALTER TABLE PSP_COMPANY_LAW
 ADD (IS_ARCHIVED  NUMBER(1) DEFAULT 0);




--ALTER TABLE PSP_EMPLOYEE add (QBDT_EMPLOYEE_INFO_FK varchar2(255) null);
--select count(*) from psp_employee where QBDT_EMPLOYEE_INFO_FK is not null;

merge into psp_qbdt_employee_info
using psp_employee on (psp_qbdt_employee_info.QBDT_EMPLOYEE_INFO_SEQ = psp_employee.QBDT_EMPLOYEE_INFO_FK)
when matched then update set employee_fk = employee_seq;

commit;



--select count(*) from psp_qbdt_employee_info where employee_fk is not null;

--ALTER TABLE PSP_COMPANY_PAYROLL_ITEM add (QBDT_PAYROLL_ITEM_INFO_FK varchar2(255) null);
--select count(*) from PSP_COMPANY_PAYROLL_ITEM where QBDT_PAYROLL_ITEM_INFO_FK is not null;

merge into psp_qbdt_payroll_item_info
using PSP_COMPANY_PAYROLL_ITEM on (psp_qbdt_payroll_item_info.QBDT_PAYROLL_ITEM_INFO_SEQ = PSP_COMPANY_PAYROLL_ITEM.QBDT_PAYROLL_ITEM_INFO_FK)
when matched then update set company_payroll_item_fk = company_payroll_item_seq;

commit;



--select count(*) from psp_qbdt_payroll_item_info where company_payroll_item_fk is not null;

--ALTER TABLE PSP_COMPANY_LAW add (QBDT_PAYROLL_ITEM_INFO_FK varchar2(255) null);
--select count(*) from PSP_COMPANY_LAW where QBDT_PAYROLL_ITEM_INFO_FK is not null;

merge into psp_qbdt_payroll_item_info
using PSP_COMPANY_LAW on (psp_qbdt_payroll_item_info.QBDT_PAYROLL_ITEM_INFO_SEQ = PSP_COMPANY_LAW.QBDT_PAYROLL_ITEM_INFO_FK)
when matched then update set company_law_fk = company_law_seq;

commit;





--select count(*) from psp_qbdt_payroll_item_info where company_law_fk is not null;




select systimestamp as end_time_postdeploy_1 from dual;



