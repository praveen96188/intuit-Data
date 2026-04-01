set echo on timing on
set serveroutput on
spool upd_emp_data_19Nov.log

DECLARE
    RECORD_COUNT      INTEGER := 0;
    MAX_RECORDS       INTEGER := 250000;
    PROCESSED_RECORDS INTEGER := 0;
    MAX_FETCH_RECORDS INTEGER := 25000;
BEGIN
    SELECT count(*) INTO RECORD_COUNT FROM PSPADM.PSP_EMPLOYEE WHERE PUBLISH_STATUS is not null AND PUBLISH_STATUS not like '_0%';

    DBMS_OUTPUT.PUT_LINE('Found ' || RECORD_COUNT || ' Records');

    WHILE RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS
        LOOP
            UPDATE PSPADM.PSP_EMPLOYEE
            SET PUBLISH_STATUS = (substr(PUBLISH_STATUS, 1, 1) || '0' || substr(PUBLISH_STATUS, 3))
            WHERE employee_seq IN (SELECT employee_seq
                                  FROM PSPADM.PSP_EMPLOYEE
                                  WHERE PUBLISH_STATUS is not null AND PUBLISH_STATUS not like '_0%' AND ROWNUM <= MAX_FETCH_RECORDS);
            COMMIT;
            PROCESSED_RECORDS := PROCESSED_RECORDS + MAX_FETCH_RECORDS;
            DBMS_OUTPUT.PUT_LINE('Processed ' || PROCESSED_RECORDS || ' records successfully');
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error updating the record');
END;
/

spool off
