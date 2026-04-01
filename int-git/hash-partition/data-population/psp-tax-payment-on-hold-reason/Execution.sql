DECLARE
    RECORD_COUNT       INTEGER   := 2688322;
    MAX_RECORDS        INTEGER   := 1000000;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2011-03-01', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2024-03-01', 'yyyy-mm-dd');
    PARTITION_END_DATE DATE := START_DATE + INTERVAL '1' MONTH;
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

                        MERGE INTO PSPADM.PSP_TAX_PAYMENT_ON_HOLD_REASON c
                        using (SELECT /*+ parallel(8) */ci.TAX_PAYMENT_ON_HOLD_REASON_SEQ, pi.COMPANY_FK
                               FROM PSPADM.PSP_TAX_PAYMENT_ON_HOLD_REASON ci,
                                    PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION pi
                               WHERE pi.INITIATION_DATE between START_DATE and PARTITION_END_DATE
                                 AND pi.MONEY_MOVEMENT_TRANSACTION_SEQ = ci.MONEY_MOVEMENT_TRANSACTION_FK
                                 AND ci.COMPANY_FK is null
                                 and ROWNUM <= MAX_FETCH_RECORDS) p
                        on (c.TAX_PAYMENT_ON_HOLD_REASON_SEQ = p.TAX_PAYMENT_ON_HOLD_REASON_SEQ)
                        when matched then
                            update set c.COMPANY_FK = p.COMPANY_FK;
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
            START_DATE := START_DATE + INTERVAL '1' MONTH;
            PARTITION_END_DATE := START_DATE + INTERVAL '1' MONTH;
            DBMS_OUTPUT.PUT_LINE('START-DATE : ' || TO_DATE(START_DATE, 'yyyy-mm-dd') || ' PARTITION-END-DATE : ' || TO_DATE(PARTITION_END_DATE, 'yyyy-mm-dd'));
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error in Procedure');
END;