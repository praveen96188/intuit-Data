DECLARE
    RECORD_COUNT       INTEGER   := 2688322;
    MAX_RECORDS        INTEGER   := 2688322;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2013-07-02', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2022-03-01', 'yyyy-mm-dd');
    PARTITION_END_DATE DATE := START_DATE + 1;
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

                        MERGE INTO PSPADM.PSP_PAYSTUB c
                        using (SELECT ci.PAYSTUB_SEQ, pi.COMPANY_FK
                               FROM PSPADM.PSP_PAYSTUB ci,
                                    PSPADM.PSP_PAYCHECK pi
                               WHERE pi.CREATED_DATE between START_DATE and PARTITION_END_DATE
                                 AND pi.PAYCHECK_SEQ = ci.PAYCHECK_FK
                                 AND ci.COMPANY_FK is null
                                 and ROWNUM <= MAX_FETCH_RECORDS) p
                        on (c.PAYSTUB_SEQ = p.PAYSTUB_SEQ)
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
            START_DATE := START_DATE + 1;
            PARTITION_END_DATE := START_DATE + 1;
            DBMS_OUTPUT.PUT_LINE('START-DATE : ' || TO_DATE(START_DATE, 'yyyy-mm-dd') || ' PARTITION-END-DATE : ' || TO_DATE(PARTITION_END_DATE, 'yyyy-mm-dd'));
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error in Procedure');
END;