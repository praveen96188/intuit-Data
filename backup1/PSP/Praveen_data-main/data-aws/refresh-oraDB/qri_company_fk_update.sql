CREATE OR REPLACE PROCEDURE prvn.PRC_FIX_ENTITY_UPDATE(
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

BEGIN
    WHILE (START_DATE < END_DATE AND RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)
        LOOP
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.
            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS )
                LOOP
                    select CURRENT_TIMESTAMP into start_time;
                    UPDATE prvn.qbdt_request_info_backup B
                    SET company_fk = A.company_id
                    FROM (
                             SELECT source_system_transmission_seq, company_id
                             FROM ibobadm.psp_source_system_transmission ai, prvn.qbdt_request_info_backup bi
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
                     IF BATCH_COUNT < COMMIT_SIZE THEN
                         EXIT;
                        END IF;
                    PROCESSED_RECORDS = PROCESSED_RECORDS + BATCH_COUNT;

                END LOOP;

            START_DATE := START_DATE + partition_window;
            PARTITION_END_DATE := START_DATE + partition_window;
        END LOOP;
END;
$$;


 call prvn.prc_fix_entity_update('2012-01-01 00:00:00', '2025-01-01 00:00:00', '1 year' );


UPDATE ibobadm_pds.psp_qbdt_request_info B
SET company_fk = A.company_id
FROM (
         SELECT /*+ Set(max_parallel_workers_per_gather 16)*/ source_system_transmission_seq, company_id
         FROM ibobadm_pds.psp_source_system_transmission ai, ibobadm_pds.psp_qbdt_request_info bi
         WHERE ai.CREATED_DATE between '2012-01-01 00:00:00' and '2025-01-01 00:00:00'
           and ai.source_system_transmission_seq = bi.source_system_transmission_fk
           AND ai.from_source_system = 'QBDT'
           and ai.type in ('PayrollSubmission', 'UsageSend','BalanceFile')
         and bi.company_fk is null
         LIMIT 10000
     ) AS A
WHERE B.source_system_transmission_fk = A.source_system_transmission_seq
  AND B.company_fk IS NULL;


 SELECT count(*)
         FROM ibobadm_pds.psp_source_system_transmission ai, ibobadm_pds.psp_qbdt_request_info bi
         WHERE  ai.source_system_transmission_seq = bi.source_system_transmission_fk
           AND ai.from_source_system = 'QBDT'
           and ai.type in ('PayrollSubmission', 'UsageSend','BalanceFile')
         and bi.company_fk is null;
