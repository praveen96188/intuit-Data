--based on created date
CREATE OR REPLACE PROCEDURE mchoubey.PRC_FIX_ENTITY_UPDATE(
START_DATE timestamp, END_DATE timestamp, partition_window interval
)
    LANGUAGE plpgsql AS
$$
DECLARE
    
    PARTITION_END_DATE TIMESTAMP := START_DATE + partition_window ;
    BATCH_COUNT        INTEGER   := 0;
    PROCESSED_RECORDS  INTEGER   := 0;
    RECORD_COUNT       INTEGER   := 246117372;
    MAX_RECORDS        INTEGER   := 100000000;
    COMMIT_SIZE        INTEGER   := 5000;
    start_time         timestamp;
    end_time           timestamp;
    elapsed_interval   INTEGER;
    totalrowsAffected INTEGER :=0;

BEGIN
    WHILE (START_DATE < END_DATE AND RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)
        LOOP
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.
            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS )
                LOOP
                    select CURRENT_TIMESTAMP into start_time;
                    UPDATE ibobadm.psp_qbdt_request_info B
                    SET company_fk = A.company_id
                    FROM (
                             SELECT source_system_transmission_seq, company_id
                             FROM ibobadm.psp_source_system_transmission ai, ibobadm.psp_qbdt_request_info bi
                             WHERE ai.CREATED_DATE between START_DATE AND PARTITION_END_DATE
                               AND ai.source_system_transmission_seq = bi.source_system_transmission_fk
                               AND ai.from_source_system = 'QBDT'
                               AND ai.type IN ('PayrollSubmission', 'UsageSend', 'BalanceFile')
                               AND bi.company_fk IS NULL
                             LIMIT COMMIT_SIZE
                         ) AS A
                    WHERE B.source_system_transmission_fk = A.source_system_transmission_seq
                      AND B.company_fk IS NULL;

                    GET DIAGNOSTICS BATCH_COUNT = ROW_COUNT;
                    COMMIT;
                   

                    select CURRENT_TIMESTAMP into end_time;
                    elapsed_interval := EXTRACT(EPOCH FROM (end_time - start_time));
                    RAISE NOTICE 'Processed % records successfully in % for date % to %', BATCH_COUNT, elapsed_interval, START_DATE, PARTITION_END_DATE;
                     totalrowsAffected := (totalrowsAffected + BATCH_COUNT); 
                     IF BATCH_COUNT < COMMIT_SIZE THEN
                         EXIT;
                        END IF;
                    PROCESSED_RECORDS = PROCESSED_RECORDS + BATCH_COUNT;

                END LOOP;

            START_DATE := START_DATE + partition_window;
            PARTITION_END_DATE := START_DATE + partition_window;
        END LOOP;
        RAISE NOTICE 'Total Updated rows %', totalrowsAffected;
END;
$$;



--data
CREATE OR REPLACE PROCEDURE PRC_ADD_COMPANY(
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




