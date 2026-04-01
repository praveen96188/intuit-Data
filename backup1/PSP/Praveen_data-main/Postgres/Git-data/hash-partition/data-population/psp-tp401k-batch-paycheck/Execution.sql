DECLARE
    MAX_FETCH_RECORDS  INTEGER   := 5000;
    BATCH_COUNT        INTEGER   := 1;
BEGIN

    WHILE (BATCH_COUNT > 0)
        LOOP
            MERGE INTO PSPADM.PSP_TP401K_BATCH_PAYCHECK c
            using (SELECT /*+PARALLEL(8) */ci.TP401K_BATCH_PAYCHECK_SEQ, pi.COMPANY_FK
                   FROM PSPADM.PSP_TP401K_BATCH_PAYCHECK ci,
                        PSPADM.PSP_PAYCHECK pi
                   WHERE pi.CREATED_DATE between TO_DATE('2010-01-13', 'yyyy-mm-dd') and TO_DATE('2023-02-25', 'yyyy-mm-dd')
                     AND pi.PAYCHECK_SEQ = ci.PAYCHECK_FK
                     AND ci.COMPANY_FK is null
                     and ROWNUM <= MAX_FETCH_RECORDS) p
            on (c.TP401K_BATCH_PAYCHECK_SEQ = p.TP401K_BATCH_PAYCHECK_SEQ)
            when matched then
                update set c.COMPANY_FK = p.COMPANY_FK;
            BATCH_COUNT := SQL%ROWCOUNT;
            COMMIT;
        end loop;
END;