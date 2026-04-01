set echo on feedback on timing on
set serveroutput on

upd_company_fk/psp_pstub_pay_item_1

DECLARE
    RECORD_COUNT       INTEGER   := 2794030097;
    MAX_RECORDS        INTEGER   := 10000000;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 50000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2022-11-22', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2022-11-23', 'yyyy-mm-dd');
    PARTITION_START_DATE DATE := END_DATE - 1;
BEGIN

    DBMS_OUTPUT.PUT_LINE('Found ' || RECORD_COUNT || ' Records');

    WHILE (RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)

        LOOP
            DBMS_OUTPUT.PUT_LINE('OUTER LOOP');
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.

            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)

                LOOP
                    BEGIN
                        DBMS_OUTPUT.PUT_LINE('INNER LOOP');

                        MERGE INTO PSPADM.PSP_PSTUB_PAY_ITEM ppio
                        using (SELECT ppi.PSTUB_PAY_ITEM_SEQ, p.COMPANY_FK
                               FROM PSPADM.PSP_PSTUB_PAY_ITEM ppi,
                                    PSPADM.PSP_PAYSTUB ps,
                                    PSPADM.PSP_PAYCHECK p
                               WHERE p.created_date BETWEEN START_DATE and START_DATE + 1
                                 and ps.PAYCHECK_FK = p.PAYCHECK_SEQ
                                 and ppi.PAYSTUB_FK = ps.PAYSTUB_SEQ
                                 and ppi.COMPANY_FK is null
                                 and ROWNUM <= MAX_FETCH_RECORDS) ppii
                        ON (ppio.PSTUB_PAY_ITEM_SEQ = ppii.PSTUB_PAY_ITEM_SEQ)
                        when matched then
                            update set ppio.COMPANY_FK = ppii.COMPANY_FK;

                        BATCH_COUNT := SQL%ROWCOUNT;
                        COMMIT;

                        PROCESSED_RECORDS := PROCESSED_RECORDS + BATCH_COUNT;

                        DBMS_OUTPUT.PUT_LINE('Processed ' || PROCESSED_RECORDS || ' records successfully');

                        if BATCH_COUNT < MAX_FETCH_RECORDS then
                            exit;
                        end if;

                    EXCEPTION
                        WHEN OTHERS THEN
                            ERROR_RECORDS := ERROR_RECORDS + BATCH_COUNT;
                            DBMS_OUTPUT.PUT_LINE('Failed ' || ERROR_RECORDS || ' records');
                    END;
                END LOOP;
            exit;
            END_DATE := END_DATE - 1;
            PARTITION_START_DATE := END_DATE - 1;
            DBMS_OUTPUT.PUT_LINE('PARTITION-START-DATE : ' || TO_DATE(PARTITION_START_DATE, 'yyyy-mm-dd') || ' END-DATE : ' || TO_DATE(END_DATE, 'yyyy-mm-dd'));
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error in Procedure');
END;
/

spool off
