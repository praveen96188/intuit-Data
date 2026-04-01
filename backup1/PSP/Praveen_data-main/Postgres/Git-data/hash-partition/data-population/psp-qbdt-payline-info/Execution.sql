--execution compensation query
DECLARE
    RECORD_COUNT       INTEGER   := 17888851;
    MAX_RECORDS        INTEGER   := 17888851;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2010-11-19', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2011-07-02', 'yyyy-mm-dd');
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

                        MERGE INTO PSPADM.PSP_QBDT_PAYLINE_INFO qpi
                        using (SELECT qpii.QBDT_PAYLINE_INFO_SEQ, pci.COMPANY_FK
                               FROM PSPADM.PSP_QBDT_PAYLINE_INFO qpii,
                                    PSPADM.PSP_COMPENSATION ci,
                                    PSPADM.PSP_PAYCHECK pci
                               WHERE pci.CREATED_DATE between START_DATE and PARTITION_END_DATE
                                 AND qpii.COMPENSATION_FK = ci.COMPENSATION_SEQ
                                 AND ci.PAYCHECK_FK = pci.PAYCHECK_SEQ
                                 AND qpii.COMPANY_FK is null
                                 AND qpii.COMPENSATION_FK is not null
                                 and ROWNUM <= MAX_FETCH_RECORDS) pc
                        on (qpi.QBDT_PAYLINE_INFO_SEQ = pc.QBDT_PAYLINE_INFO_SEQ)
                        when matched then
                            update set qpi.COMPANY_FK = pc.COMPANY_FK;
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

--execution deduction
DECLARE
    RECORD_COUNT       INTEGER   := 13184888;
    MAX_RECORDS        INTEGER   := 13184888;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2010-11-28', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2011-07-02', 'yyyy-mm-dd');
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

                        MERGE INTO PSPADM.PSP_QBDT_PAYLINE_INFO qpi
                        using (SELECT qpii.QBDT_PAYLINE_INFO_SEQ, pci.COMPANY_FK
                               FROM PSPADM.PSP_QBDT_PAYLINE_INFO qpii,
                                    PSPADM.PSP_DEDUCTION ci,
                                    PSPADM.PSP_PAYCHECK pci
                               WHERE pci.CREATED_DATE between START_DATE and PARTITION_END_DATE
                                 AND qpii.DEDUCTION_FK = ci.DEDUCTION_SEQ
                                 AND ci.PAYCHECK_FK = pci.PAYCHECK_SEQ
                                 AND qpii.COMPANY_FK is null
                                 AND qpii.DEDUCTION_FK is not null
                                 and ROWNUM <= MAX_FETCH_RECORDS) pc
                        on (qpi.QBDT_PAYLINE_INFO_SEQ = pc.QBDT_PAYLINE_INFO_SEQ)
                        when matched then
                            update set qpi.COMPANY_FK = pc.COMPANY_FK;
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

--execution contribution
DECLARE
    RECORD_COUNT       INTEGER   := 3120232;
    MAX_RECORDS        INTEGER   := 3120232;
    PROCESSED_RECORDS  INTEGER   := 0;
    ERROR_RECORDS      INTEGER   := 0;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 0;
    START_DATE         DATE := TO_DATE('2010-11-28', 'yyyy-mm-dd');
    END_DATE           DATE := TO_DATE('2011-07-02', 'yyyy-mm-dd');
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

                        MERGE INTO PSPADM.PSP_QBDT_PAYLINE_INFO qpi
                        using (SELECT qpii.QBDT_PAYLINE_INFO_SEQ, pci.COMPANY_FK
                               FROM PSPADM.PSP_QBDT_PAYLINE_INFO qpii,
                                    PSPADM.PSP_EMPLOYER_CONTRIBUTION ci,
                                    PSPADM.PSP_PAYCHECK pci
                               WHERE pci.CREATED_DATE between START_DATE and END_DATE
                                 AND qpii.EMPLOYER_CONTRIBUTION_FK = ci.EMPLOYER_CONTRIBUTION_SEQ
                                 AND ci.PAYCHECK_FK = pci.PAYCHECK_SEQ
                                 AND qpii.COMPANY_FK is null
                                 and ROWNUM <= MAX_FETCH_RECORDS) pc
                        on (qpi.QBDT_PAYLINE_INFO_SEQ = pc.QBDT_PAYLINE_INFO_SEQ)
                        when matched then
                            update set qpi.COMPANY_FK = pc.COMPANY_FK;
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