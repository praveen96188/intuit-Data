DECLARE
    RECORD_COUNT       INTEGER   := 2711819;
    MAX_RECORDS        INTEGER   := 3000000;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2010-11-23', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2011-07-23', 'yyyy-mm-dd');
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

                        MERGE INTO PSPADM.PSP_QBDT_TRANSACTION_INFO qtio
                        using (SELECT qti.QBDT_TRANSACTION_INFO_SEQ, pr.COMPANY_FK
                               FROM PSPADM.PSP_QBDT_TRANSACTION_INFO qti,
                                    PSPADM.PSP_LIABILITY_ADJUSTMENT la,
                                    PSPADM.PSP_PAYROLL_RUN pr
                               WHERE pr.paycheck_settlement_date between START_DATE and PARTITION_END_DATE
                                 and la.PAYROLL_RUN_FK = pr.PAYROLL_RUN_SEQ
                                 and qti.LIABILITY_ADJUSTMENT_FK = la.LIABILITY_ADJUSTMENT_SEQ
                                 and qti.COMPANY_FK is null
                                 and qti.liability_adjustment_fk is not null
                                 and ROWNUM <= MAX_FETCH_RECORDS) qtii
                        ON (qtio.QBDT_TRANSACTION_INFO_SEQ = qtii.QBDT_TRANSACTION_INFO_SEQ)
                        when matched then
                            update set qtio.COMPANY_FK = qtii.COMPANY_FK;

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
