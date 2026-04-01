DECLARE
    RECORD_COUNT        INTEGER   := 1;
    MAX_RECORDS         INTEGER   := 1000000;
    PROCESSED_RECORDS   INTEGER   := 0;
    ERROR_RECORDS       INTEGER   := 0;
    MAX_FETCH_RECORDS   INTEGER   := 5000;
    BATCH_COUNT         INTEGER   := 0;
    INTERVAL            INTEGER   := 3;
    START_INDEX         INTEGER   := 30000;
    END_INDEX           INTEGER   := 50000;
    PARTITION_END_INDEX INTEGER   := START_INDEX + INTERVAL;

BEGIN

    WHILE (START_INDEX < END_INDEX AND PROCESSED_RECORDS < MAX_RECORDS)

        LOOP
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.

            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)

                LOOP
                    BEGIN

                        MERGE INTO PSPADM.PSP_DISBURSE_ADVICE_TAX_LIAB ppio
                        using (SELECT ppi.DISBURSE_ADVICE_TAX_LIAB_SEQ, p.COMPANY_FK
                               FROM PSPADM.PSP_DISBURSE_ADVICE_TAX_LIAB ppi,
                                    PSPADM.PSP_DISBURSE_ADVICE p
                               WHERE ppi.DISBURSE_ADVICE_FK = p.DISBURSE_ADVICE_SEQ
                                 and ppi.COMPANY_FK is null
                                 and p.COMPANY_FK in (
                                   select cd.COMPANY_FK
                                   from PSPADM.temp_company_disburse cd
                                   where cd.ID between START_INDEX AND PARTITION_END_INDEX)
                                 and ROWNUM <= MAX_FETCH_RECORDS) ppii
                        ON (ppio.DISBURSE_ADVICE_TAX_LIAB_SEQ = ppii.DISBURSE_ADVICE_TAX_LIAB_SEQ)
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
            START_INDEX := START_INDEX + INTERVAL;
            PARTITION_END_INDEX := START_INDEX + INTERVAL;
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error in Procedure');
END;