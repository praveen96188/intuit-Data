spool 01_export_data
set serveroutput on
set echo on
set feed on
DECLARE
  hdnl NUMBER;
BEGIN
  hdnl := DBMS_DATAPUMP.open( operation => 'EXPORT', job_mode => 'TABLE', job_name=>'EXPORT_DATA');
  DBMS_DATAPUMP.ADD_FILE( handle => hdnl, filename => 'ibob_tables_%U.dmp', directory => 'DATA_PUMP_DIR', filetype => dbms_datapump.ku$_file_type_dump_file);
  DBMS_DATAPUMP.add_file( handle => hdnl, filename => 'ibob_tables_export.log', directory => 'DATA_PUMP_DIR', filetype => dbms_datapump.ku$_file_type_log_file);
  DBMS_DATAPUMP.METADATA_FILTER(hdnl,'INCLUDE_NAME_EXPR','IN (''PSP_SOURCE_SYSTEM_TRANSMISSION'',''PSP_SAP_METHOD_CALL'',''PSP_QBDT_REQUEST_INFO'')');
  DBMS_DATAPUMP.set_parallel(hdnl, 4);
  DBMS_DATAPUMP.start_job(hdnl);
  dbms_output.put_line('Export Handle: ' || hdnl);
  DBMS_DATAPUMP.detach(hdnl);
END;
/

