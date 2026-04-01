SET PAGESIZE 50000
SET MARKUP HTML ON TABLE "class=detail cellspacing=0" ENTMAP OFF
SPOOL output.log
SET SERVEROUTPUT ON;



DECLARE
    BATCH_COUNT        INTEGER   := 1;
    PROCESSED_COUNT          INTEGER   :=15000000;
    MAX_RECORDS        INTEGER   := 20000000;
BEGIN

    WHILE (BATCH_COUNT > 0 and PROCESSED_COUNT < MAX_RECORDS)

        LOOP
            DBMS_OUTPUT.PUT_LINE('OUTER LOOP');
            MERGE INTO PSPADM.PSP_WC_PAYCHECK c
            using (SELECT ci.WC_PAYCHECK_SEQ, pi.COMPANY_FK
                    FROM PSPADM.PSP_WC_PAYCHECK ci,
                    PSPADM.PSP_PAYCHECK pi
                    WHERE pi.PAYCHECK_SEQ = ci.PAYCHECK_FK
                    AND ci.WC_PAYCHECK_SEQ in (
                                select wcii.WC_PAYCHECK_SEQ
                                from PSPADM.PSP_WC_PAYCHECK wcii
                                where wcii.COMPANY_FK is null
                                and ROWNUM <= 5000
                                )
                    ) p
            on (c.WC_PAYCHECK_SEQ = p.WC_PAYCHECK_SEQ)
            when matched then
            update set c.COMPANY_FK = p.COMPANY_FK;
            
            BATCH_COUNT := SQL%ROWCOUNT;
            PROCESSED_COUNT := PROCESSED_COUNT+BATCH_COUNT;

            update SKUMAR71.TEMP_DATA_POPULATION
            set Total_processed =PROCESSED_COUNT
            where table_name ='PSP_WC_PAYCHECK';
            
            COMMIT;
        END LOOP;
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error in Procedure');
END;


 /
SPOOL OFF
quit;


