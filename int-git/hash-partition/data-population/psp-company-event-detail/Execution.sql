DECLARE

    RECORD_COUNT       INTEGER   := 0;
    MAX_RECORDS        INTEGER   := 1000;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 100;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2008-10-06', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2008-10-12', 'yyyy-mm-dd');
    PARTITION_END_DATE DATE := START_DATE + 1;

BEGIN
    SELECT count(*) INTO RECORD_COUNT from PSPADM.PSP_COMPANY_EVENT_DETAIL WHERE COMPANY_FK IS NULL;

    DBMS_OUTPUT.PUT_LINE('Found ' || RECORD_COUNT || ' Records');

    WHILE (START_DATE < END_DATE AND RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)

        LOOP
            DBMS_OUTPUT.PUT_LINE('OUTER LOOP');
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.

            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)

                LOOP
                    BEGIN
                        DBMS_OUTPUT.PUT_LINE('INNER LOOP');

                        MERGE INTO PSPADM.PSP_COMPANY_EVENT_DETAIL ced
                        using (SELECT cedi.COMPANY_EVENT_DETAIL_SEQ, cei.COMPANY_FK
                               FROM PSPADM.PSP_COMPANY_EVENT_DETAIL cedi,
                                    PSPADM.PSP_COMPANY_EVENT cei
                               WHERE cei.EVENT_TIME_STAMP between START_DATE and PARTITION_END_DATE
                                 AND cedi.COMPANY_EVENT_FK = cei.COMPANY_EVENT_SEQ
                                 AND cedi.COMPANY_FK is null
                                 and ROWNUM <= MAX_FETCH_RECORDS) ce
                        on (ced.COMPANY_EVENT_DETAIL_SEQ = ce.COMPANY_EVENT_DETAIL_SEQ)
                        when matched then
                            update set ced.COMPANY_FK = ce.COMPANY_FK;
                        BATCH_COUNT := SQL%ROWCOUNT;
                        COMMIT;

                        PROCESSED_RECORDS := PROCESSED_RECORDS + BATCH_COUNT;

                        DBMS_OUTPUT.PUT_LINE('Processed ' || PROCESSED_RECORDS || ' records successfully');

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