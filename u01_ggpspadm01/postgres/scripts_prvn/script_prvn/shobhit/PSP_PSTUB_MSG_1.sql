SET PAGESIZE 50000
SET MARKUP HTML ON TABLE "class=detail cellspacing=0" ENTMAP OFF
SPOOL output.log
SET SERVEROUTPUT ON;
SET TIMING ON;

DECLARE
    RECORD_COUNT       INTEGER   := 2794030097;
    MAX_RECORDS        INTEGER   := 200000000;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2020-11-01', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2023-03-06', 'yyyy-mm-dd');
    PARTITION_START_DATE DATE := END_DATE - 1/96;
BEGIN

    DBMS_OUTPUT.PUT_LINE('Found ' || RECORD_COUNT || ' Records');

    WHILE (START_DATE < END_DATE AND RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)

        LOOP
            DBMS_OUTPUT.PUT_LINE('OUTER LOOP');
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.

            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)

                LOOP
                    BEGIN
                        DBMS_OUTPUT.PUT_LINE('INNER LOOP');

                        MERGE INTO PSPADM.PSP_PSTUB_MSG ppio
                        using (SELECT ppi.PSTUB_MSG_SEQ, p.COMPANY_FK
                               FROM PSPADM.PSP_PSTUB_MSG ppi,
                                    PSPADM.PSP_PAYSTUB ps,
                                    PSPADM.PSP_PAYCHECK p
                               WHERE p.created_date BETWEEN PARTITION_START_DATE and END_DATE
                                 and ps.PAYCHECK_FK = p.PAYCHECK_SEQ
                                 and ppi.PAYSTUB_FK = ps.PAYSTUB_SEQ
                                 and ppi.COMPANY_FK is null
                                 and ROWNUM <= MAX_FETCH_RECORDS) ppii
                        ON (ppio.PSTUB_MSG_SEQ = ppii.PSTUB_MSG_SEQ)
                        when matched then
                            update set ppio.COMPANY_FK = ppii.COMPANY_FK;

                        BATCH_COUNT := SQL%ROWCOUNT;
                        COMMIT;

                        PROCESSED_RECORDS := PROCESSED_RECORDS + BATCH_COUNT;

                        DBMS_OUTPUT.PUT_LINE('Processed ' || PROCESSED_RECORDS || ' records successfully');
                        
                        UPDATE SKUMAR71.TEMP_DATA_POPULATION T1
                        SET T1.Total_processed = PROCESSED_RECORDS
                        WHERE T1.table_name ='PSP_PSTUB_MSG_1';
                        COMMIT;

                        if BATCH_COUNT < MAX_FETCH_RECORDS then
                            exit;
                        end if;

                    EXCEPTION
                        WHEN OTHERS THEN
                            ERROR_RECORDS := ERROR_RECORDS + BATCH_COUNT;
                            DBMS_OUTPUT.PUT_LINE('Failed ' || ERROR_RECORDS || ' records');
                    END;
                END LOOP;
            END_DATE := END_DATE - 1/96;
            PARTITION_START_DATE := END_DATE - 1/96;
            DBMS_OUTPUT.PUT_LINE('PARTITION-START-DATE : ' || TO_DATE(PARTITION_START_DATE, 'yyyy-mm-dd') || ' END-DATE : ' || TO_DATE(END_DATE, 'yyyy-mm-dd'));
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error in Procedure');
END;

 /
SPOOL OFF
quit;

