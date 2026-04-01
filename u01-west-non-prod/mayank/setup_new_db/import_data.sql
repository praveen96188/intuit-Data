spool import_data
set serveroutput on

set echo on
set feed on

DECLARE
  hdnl NUMBER;
BEGIN
  hdnl := DBMS_DATAPUMP.open( operation => 'IMPORT', job_mode => 'SCHEMA', job_name=>'IMPORT_DATA');
  DBMS_DATAPUMP.ADD_FILE( handle => hdnl, filename => 'exp_pspadm_data_%U.dmp', directory => 'DATA_PUMP_DIR', filetype => dbms_datapump.ku$_file_type_dump_file);
  DBMS_DATAPUMP.add_file( handle => hdnl, filename => 'imp_pspadm_data.log', directory => 'DATA_PUMP_DIR', filetype => dbms_datapump.ku$_file_type_log_file);
  DBMS_DATAPUMP.set_parallel(hdnl, 4);
  DBMS_DATAPUMP.start_job(hdnl);
  dbms_output.put_line('Import Handle: ' || hdnl);
  DBMS_DATAPUMP.detach(hdnl);
END;
/

spool off
exit


