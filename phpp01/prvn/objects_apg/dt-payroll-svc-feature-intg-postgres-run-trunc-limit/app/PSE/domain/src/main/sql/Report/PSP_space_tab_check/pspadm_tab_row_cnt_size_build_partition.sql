Set heading off
Set feedback off
Set pagesize 0
Set termout off
Set trimout on
Set trimspool on
Set recsep off
Set linesize 220
Column d noprint new_value date_
Column u noprint new_value owner_

Spool pspadm_tab_part_row_cnt_size.sql
select 'set heading on' from dual;
select 'set feedback off' from dual;
select 'col TABLE for a80' from dual;
select 'col ROWS for a30' from dual;
select 'col SIZE for a20' from dual;
select 'alter session set nls_date_format=''DD-MON-YY HH24:MI:SS'';' from dual;
select 'select sysdate from dual;' from dual;
Select 'Select '''||segment_name||':'||partition_name||''' "TABLE", ''ROWS: ''||count(*) "ROWS", '' MB: '||bytes/1024/1024||''' "SIZE" from '||owner||'.'||segment_name||' partition('||partition_name||');', to_char(sysdate, 'YYYYMMDDHH24MISS') d, owner u
from dba_segments
where owner = 'PSPADM'
and segment_type = 'TABLE PARTITION'
and segment_name in (
'PSP_PROPERTY_AUDIT'
, 'PSP_ENTRY_DETAIL_RECORD'
, 'PSP_FINANCIAL_TRANS_STATE'
, 'PSP_LEDGER_BALANCE'
, 'PSP_FINANCIAL_TRANSACTION'
, 'PSP_MONEY_MOVEMENT_TRANSACTION'
, 'PSP_PAYCHECK_SPLIT'
, 'PSP_PAYCHECK'
, 'PSP_COMPANY_EVENT_DETAIL'
, 'PSP_COMPANY_EVENT'
, 'PSP_INDIVIDUAL'
, 'PSP_BANK_ACCOUNT'
, 'PSP_EMPLOYEE_BANK_ACCOUNT'
, 'PSP_EMPLOYEE'
, 'PSP_SOURCE_SYSTEM_TRANSMISSION'
, 'PSP_PAYROLL_RUN'
, 'PSP_TRANSMISSION_PAYROLL_RUN'
, 'PSP_BILLING_DETAIL'
, 'PSP_COMPANY_SERVICE_BANK_ACCT'
, 'PSP_TRANSACTION_RESPONSE'
, 'PSP_ADDRESS'
, 'PSP_CONTACT'
, 'PSP_COMPANY'
, 'PSP_COMPANY_SERVICE'
, 'PSP_DDCOMPANY_SERVICE_INFO'
, 'PSP_COMPANY_BANK_ACCOUNT'
, 'PSP_BATCH_JOB_AUDIT_LOG'
, 'PSP_COMPANY_PIN'
, 'PSP_ON_HOLD_REASON'
, 'PSP_TRANSACTION_OFFLOAD_BATCH'
, 'PSP_PAYROLL_FRAUD_BATCH'
, 'SPCFPROPERTYDEFINITION'
, 'PSP_AUTHROLE_OPERATION_ASSOC'
, 'PSP_SVCSTAT_SYSCAP_ASSOC'
, 'PSP_POSTING_RULE'
, 'PSP_SERV_STAT_TXN_SKU_TYPE'
, 'PSP_AUTH_USER'
, 'PSP_ROLE_SUB_STATUS'
, 'PSP_FINANCIAL_TXN_ACTION'
, 'PSP_EVENT_DETAIL_TYPE'
, 'PSP_OFFLOAD_BATCH'
)
order by segment_name, partition_name
/
Select 'Select '''||segment_name||':'||partition_name||''' "TABLE", ''ROWS: ''||count(*) "ROWS", '' MB: '||bytes/1024/1024||''' "SIZE" from '||owner||'.'||segment_name||' subpartition('||partition_name||');', to_char(sysdate, 'YYYYMMDDHH24MISS') d, owner u
from dba_segments
where owner = 'PSPADM'
and segment_type = 'TABLE SUBPARTITION'
and segment_name = 'PSP_SOURCE_SYSTEM_TRANSMISSION'
order by partition_name
/

Spool off
set heading on
set feedback on

--Spool count_&owner_._&date_\.log
Spool &owner_._tab_part_row_cnt_size_&date_..log
@@pspadm_tab_part_row_cnt_size.sql
Spool off

