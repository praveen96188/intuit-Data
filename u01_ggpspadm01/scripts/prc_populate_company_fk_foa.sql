CREATE OR REPLACE PROCEDURE mchoubey.PRC_POPULATE_COMPANY_FK_FOA(
    START_DATE timestamp, END_DATE timestamp
)
    LANGUAGE plpgsql AS
$$
DECLARE
    PROCESSED_RECORDS  INTEGER   := 0;
    BATCH_COUNT        INTEGER   := 0;
    PARTITION_END_DATE TIMESTAMP := START_DATE + interval '1 week';
    start_time         timestamp;
    end_time           timestamp;
    elapsed_interval   INTEGER;
    MAX_FETCH_RECORDS  INTEGER   := 5000;
BEGIN
    WHILE (START_DATE < END_DATE)
        LOOP
            BATCH_COUNT := 1;
            WHILE (BATCH_COUNT > 0)
                LOOP
                    BEGIN
                        select CURRENT_TIMESTAMP into start_time;
                        MERGE INTO pspadm.psp_fintxn_onholdreason_assoc foa
                        using (select distinct ohr.company_fk, on_hold_reason_seq
                               from pspadm.psp_on_hold_reason ohr,
                                    pspadm.psp_fintxn_onholdreason_assoc assoc
                               where ohr.on_hold_reason_seq = assoc.on_hold_reason_fk
                                 and ohr.created_date between START_DATE and PARTITION_END_DATE
                                 and assoc.company_fk is null
                               limit MAX_FETCH_RECORDS) ce
                        on (foa.on_hold_reason_fk = ce.on_hold_reason_seq)
                        when matched then
                            update set COMPANY_FK = ce.COMPANY_FK;
                        GET DIAGNOSTICS BATCH_COUNT = ROW_COUNT;
                        PROCESSED_RECORDS = PROCESSED_RECORDS + BATCH_COUNT
                            COMMIT;
                        select CURRENT_TIMESTAMP into end_time;
                        elapsed_interval := EXTRACT(EPOCH FROM (end_time - start_time));
                        RAISE NOTICE 'UpdatedRecords=%', BATCH_COUNT;
                        RAISE NOTICE 'Processed % records successfully in % for start_date % and end_date %', BATCH_COUNT, elapsed_interval, START_DATE, PARTITION_END_DATE;
                    end;
                end loop;

            START_DATE := START_DATE + interval '1 week';
            PARTITION_END_DATE := START_DATE + interval '1 week';
        END LOOP;
        RAISE NOTICE 'Total records %', PROCESSED_RECORDS;
END;
$$;
