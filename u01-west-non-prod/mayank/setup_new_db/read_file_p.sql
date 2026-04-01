spool &1
set serveroutput on

DECLARE
  v_file                    UTL_FILE.FILE_TYPE;
  v_line                    VARCHAR2(32000);
BEGIN
  v_file       := UTL_FILE.FOPEN('DATA_PUMP_DIR', '&1', 'r');
  LOOP
   UTL_FILE.GET_LINE(v_file, v_line);
   DBMS_OUTPUT.PUT_LINE(v_line);
  END LOOP;
  UTL_FILE.FCLOSE(v_file);
EXCEPTION
when NO_DATA_FOUND then
DBMS_OUTPUT.PUT_LINE('File reading completed, but the IN Value hasn''t been recognized!');
END;
/
spool off


