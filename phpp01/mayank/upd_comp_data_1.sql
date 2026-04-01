set echo on timing on
set serveroutput on
spool upd_comp_data_1.log
DECLARE
    RECORD_COUNT      INTEGER := 0;
    MAX_RECORDS       INTEGER := 500000;
    PROCESSED_RECORDS INTEGER := 0;
    MAX_FETCH_RECORDS INTEGER := 25000;
BEGIN
    SELECT count(*) INTO RECORD_COUNT FROM PSPADM.PSP_COMPANY WHERE PUBLISH_STATUS <> '0000000000000000';

    DBMS_OUTPUT.PUT_LINE('Found ' || RECORD_COUNT || ' Records');

    WHILE RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS
        LOOP
            UPDATE PSPADM.PSP_COMPANY
            SET PUBLISH_STATUS = '0000000000000000'
            WHERE company_seq IN (SELECT company_seq
                                  FROM PSPADM.PSP_COMPANY
                                  WHERE PUBLISH_STATUS <> '0000000000000000' AND ROWNUM <= MAX_FETCH_RECORDS);
            COMMIT;
            PROCESSED_RECORDS := PROCESSED_RECORDS + MAX_FETCH_RECORDS;
            DBMS_OUTPUT.PUT_LINE('Processed ' || PROCESSED_RECORDS || ' records successfully');
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error updating the record');
END;
/

spool off

