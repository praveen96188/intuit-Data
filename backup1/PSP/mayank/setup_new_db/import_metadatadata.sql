spool import_metadata
set serveroutput on
set feed on
set time on
set timi on


DECLARE
  hdnl NUMBER;
BEGIN
  hdnl := DBMS_DATAPUMP.open( operation => 'IMPORT', job_mode => 'SCHEMA', job_name=>'IMPORT_METADATA');
  DBMS_DATAPUMP.ADD_FILE( handle => hdnl, filename => 'exp_pspadm_metadata.dmp', directory => 'DATA_PUMP_DIR', filetype => dbms_datapump.ku$_file_type_dump_file);
  DBMS_DATAPUMP.add_file( handle => hdnl, filename => 'imp_pspadm_metadata.log', directory => 'DATA_PUMP_DIR', filetype => dbms_datapump.ku$_file_type_log_file);
  DBMS_DATAPUMP.METADATA_FILTER(hdnl,'SCHEMA_EXPR','NOT IN (''ORDSYS'')');
  DBMS_DATAPUMP.METADATA_FILTER(hdnl, 'EXCLUDE_PATH_EXPR', 'IN (''INDEX'', ''STATISTICS'')');
  DBMS_DATAPUMP.start_job(hdnl);
  dbms_output.put_line('Import Handle: ' || hdnl);
  DBMS_DATAPUMP.detach(hdnl);
END;
/

spool off

exit


