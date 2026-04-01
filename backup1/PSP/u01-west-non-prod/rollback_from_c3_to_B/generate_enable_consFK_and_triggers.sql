set lines 3000
set serverout on
begin
for i in (select constraint_name, table_name from dba_constraints where owner='PSPADM' and constraint_type='R' and status='DISABLED') LOOP
dbms_output.put_line ('alter table pspadm.'||i.table_name||' enable novalidate constraint '||i.constraint_name||';');
end loop;
end;
/


set lines 3000
set serverout on
begin
for i in (select trigger_name from dba_triggers where owner='PSPADM' and status='DISABLED' and trigger_name not in ('TR_UPD_COMPANY_EVNT_TIMESTAMP','TR_UPD_INTUIT_TAX_BANK_ACCT')) LOOP
dbms_output.put_line ('alter trigger pspadm.'||i.trigger_name||' enable;');
end loop;
end;
/

