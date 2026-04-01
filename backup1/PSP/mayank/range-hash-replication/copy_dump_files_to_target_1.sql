spool copy_dump_files_to_target_1
set echo on
set feed on
set time on
set timi on

BEGIN
  FOR i IN 1..5 LOOP
    DBMS_FILE_TRANSFER.PUT_FILE(
      source_directory_object       => 'DATA_PUMP_DIR',
      source_file_name              => 'exp_pspadm_data_' || lpad(to_char(i), 2, '0') || '.dmp',
      destination_directory_object  => 'DATA_PUMP_DIR',
      destination_file_name         => 'exp_pspadm_data_' || lpad(to_char(i), 2, '0') || '.dmp',
      destination_database          => 'to_target'
    );
  END LOOP;
END;
/

