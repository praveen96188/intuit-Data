set echo on timing on
set serveroutput on
spool upd_comp_data_03Feb_1.log

DECLARE
    RECORD_COUNT      INTEGER := 0;
    MAX_RECORDS       INTEGER := 285200;
    PROCESSED_RECORDS INTEGER := 0;
    MAX_FETCH_RECORDS INTEGER := 285100;
BEGIN
    SELECT count(*) INTO RECORD_COUNT FROM PSPADM.PSP_COMPANY pc
    where pc.COMPANY_SEQ in (select company_seq
                             from PSPADM.PSP_COMPANY pc
                                      join MCHOUBEY.tmp_custid on pc.COMPANY_SEQ = tmp_custid.entity_Id
                             where not regexp_like(pc.PUBLISH_STATUS, '...3............'));

    DBMS_OUTPUT.PUT_LINE('Found ' || RECORD_COUNT || ' Records');

    WHILE RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS
        LOOP
            UPDATE PSPADM.PSP_COMPANY
            SET PUBLISH_STATUS = (substr(PUBLISH_STATUS,1,3) || '3' || substr(PUBLISH_STATUS, 5))
            WHERE company_seq IN (select COMPANY_SEQ
                                  from (select company_seq
                                        from PSPADM.PSP_COMPANY pc
                                                 join MCHOUBEY.tmp_custid on pc.COMPANY_SEQ = tmp_custid.entity_Id
                                        where not regexp_like(pc.PUBLISH_STATUS, '...3............'))
                                  where ROWNUM < MAX_FETCH_RECORDS);
            COMMIT;
            PROCESSED_RECORDS := PROCESSED_RECORDS + MAX_FETCH_RECORDS;
            DBMS_OUTPUT.PUT_LINE('Processed ' || PROCESSED_RECORDS || ' records successfully');
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error updating the record');
END;
/

spool off
