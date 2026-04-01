spool testcase 
set echo on
set feed on

alter session set current_schema=qbo_data;

drop trigger qbo_data.testcase_update;

create or replace trigger qbo_data.testcase_update
  before update
  on qbo_data.testcase
  for each row
begin
  :new.rpt_data := null;
end;
/

UPDATE testcase MemorizedReports SET rpt_data=EMPTY_CLOB(), attach_as_excel = 1 , email_address = 'reports_automation_iamtestpass@mailinator.com' ,email_cc = 'reports_automation_iamtestpass@mailinator.com' , email_subject = 'Financial reports for Test Company_US' , email_text = 'Hello,nnAttached is the set of financial reports for Test Company_US_PS. nnRegards,nReportUser N' , edit_sequence = 1 , last_modify_date=CURRENT_DATE WHERE company_id = 56813255 and mem_rpt_id = 1;

UPDATE testcase MemorizedReports SET rpt_data=EMPTY_CLOB(), attach_as_excel = 1 , email_address = 'reports_automation_iamtestpass@mailinator.com' ,email_cc = 'reports_automation_iamtestpass@mailinator.com' , email_subject = 'Financial reports for Test Company_US' , email_text = 'Hello,nnAttached is the set of financial reports for Test Company_US_PS. nnRegards,nReportUser N' , edit_sequence = 1 , last_modify_date=CURRENT_DATE WHERE company_id = 56813255 and mem_rpt_id = 1;

rollback;

spool off

