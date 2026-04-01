SET PAGESIZE 50000
SET MARKUP HTML ON TABLE "class=detail cellspacing=0" ENTMAP OFF
SPOOL output.log
SET SERVEROUTPUT ON;

DECLARE
    RECORD_COUNT       INTEGER   := 527084;
    MAX_RECORDS        INTEGER   := 5000000;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 10000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2013-10-01', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2016-06-01', 'yyyy-mm-dd');
    PARTITION_END_DATE DATE := START_DATE + 5;
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

                        MERGE INTO PSPADM.PSP_EFTPS_PAYMENT_DETAIL c
                        using (SELECT pba.EFTPS_PAYMENT_DETAIL_SEQ, mmt.COMPANY_FK
                               FROM PSPADM.PSP_EFTPS_PAYMENT_DETAIL pba,
                                    PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION mmt
                               WHERE mmt.INITIATION_DATE between START_DATE and PARTITION_END_DATE
                                 AND mmt.MONEY_MOVEMENT_TRANSACTION_SEQ= pba.MONEY_MOVEMENT_TRANSACTION_FK
                                 AND pba.COMPANY_FK is null
                                 and ROWNUM <= MAX_FETCH_RECORDS) p
                        on (c.EFTPS_PAYMENT_DETAIL_SEQ = p.EFTPS_PAYMENT_DETAIL_SEQ)
                        when matched then
                            update set c.COMPANY_FK = p.COMPANY_FK;
                        BATCH_COUNT := SQL%ROWCOUNT;
                        COMMIT;

                        PROCESSED_RECORDS := PROCESSED_RECORDS + BATCH_COUNT;

                        DBMS_OUTPUT.PUT_LINE('Processed ' || PROCESSED_RECORDS || ' records successfully');
                        
                        UPDATE SKUMAR71.TEMP_DATA_POPULATION T1
                        SET T1.Total_processed = PROCESSED_RECORDS
                        WHERE T1.table_name ='PSP_EFTPS_PAYMENT_DETAIL_2';
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
            START_DATE := START_DATE + 5;
            PARTITION_END_DATE := START_DATE + 5;
            DBMS_OUTPUT.PUT_LINE('START-DATE : ' || TO_DATE(START_DATE, 'yyyy-mm-dd') || ' PARTITION-END-DATE : ' || TO_DATE(PARTITION_END_DATE, 'yyyy-mm-dd'));
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error in Procedure');
END;

 /
SPOOL OFF
quit;
