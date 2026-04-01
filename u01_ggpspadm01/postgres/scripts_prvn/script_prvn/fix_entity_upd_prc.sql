CREATE OR REPLACE PROCEDURE mchoubey.PRC_FIX_ENTITY_UPDATE(
    START_DATE timestamp, END_DATE timestamp
)
    LANGUAGE plpgsql AS
$$
DECLARE
    BATCH_COUNT        INTEGER   := 0;
    PARTITION_END_DATE TIMESTAMP := START_DATE + interval '1 minute';
    start_time         timestamp;
    end_time           timestamp;
    elapsed_interval   INTEGER;

BEGIN
    WHILE (START_DATE < END_DATE)
        LOOP
            select CURRENT_TIMESTAMP into start_time;

            update psp_entity_update
            set status = 'Created'
            where modified_date between START_DATE and PARTITION_END_DATE
              and modifier_id = 'PubSub'
              and event_type = 'EntityCreate'
              and status = 'Published';

            GET DIAGNOSTICS BATCH_COUNT = ROW_COUNT;
            COMMIT;
            select CURRENT_TIMESTAMP into end_time;
            elapsed_interval := EXTRACT(EPOCH FROM (end_time - start_time));
            RAISE NOTICE 'Processed % records successfully in % for date %', BATCH_COUNT, elapsed_interval, START_DATE;
            START_DATE := START_DATE + interval '1 minute';
            PARTITION_END_DATE := START_DATE + interval '1 minute';
        END LOOP;
END;
$$

