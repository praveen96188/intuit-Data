CREATE OR REPLACE PROCEDURE process_psp_employee_w2(target_year integer)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Declare local variables
    DECLARE
        counter             integer   := 1;
        p_number_of_records integer   := 1;
        max_records         integer;
        start_time          timestamp;
        end_time            timestamp;
        batch_size          integer   := 5000;
        rows_processed      integer   := 0;

    BEGIN
        -- Get the total number of records to be processed
        SELECT COUNT(*) INTO max_records 
        FROM pspadm.psp_employee_w2_totals 
        WHERE year = target_year;

        -- Notify the total number of records to be processed
        RAISE NOTICE 'Total records to be processed: %', max_records;

        -- Loop until all records are processed
        WHILE (max_records > 0 AND p_number_of_records > 0) LOOP
            start_time := current_timestamp;

            -- Fetch batch of records and delete them
            WITH batch_records AS (
                SELECT employee_w2_totals_seq 
                FROM pspadm.psp_employee_w2_totals 
                WHERE year = target_year 
                LIMIT batch_size
            )
            DELETE FROM pspadm.psp_employee_w2_totals
            WHERE employee_w2_totals_seq IN (SELECT employee_w2_totals_seq FROM batch_records);

            -- Commit after each batch
            COMMIT;

            end_time := current_timestamp;

            -- Get the number of affected rows
            GET DIAGNOSTICS p_number_of_records := ROW_COUNT;

            -- Notify progress
            RAISE NOTICE 'Batch count: %', counter;
            rows_processed := p_number_of_records;
            max_records := max_records - rows_processed;

            RAISE NOTICE 'Rows processed: %', rows_processed;
            RAISE NOTICE 'Rows remaining: %', max_records;
            RAISE NOTICE 'Elapsed time: %', end_time - start_time;

            -- Increment counter
            counter := counter + 1;
        END LOOP;
    END;
END;
$$;
