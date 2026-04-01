spool chk_imp_file
set echo on
set feed on

alter session set nls_date_format = 'yyyy-mm-dd hh24:mi:ss';

col FILESIZE format 999,999,999,999
select * from table(RDSADMIN.RDS_FILE_UTIL.LISTDIR('DATA_PUMP_DIR')) order by 1;
spool off

