CREATE OR REPLACE PROCEDURE prc_offload (
  p_offload_batch_id   IN      VARCHAR,                       -- psp_offload_batch.offload_batch_seq
  p_offload_date       IN       TIMESTAMP,                      -- UTC Date
  p_file_type          IN       VARCHAR,                          -- DD or Tax
  p_user_id            IN       VARCHAR,                          -- For audit purposes
  p_app_server_date    IN       TIMESTAMP                    -- UTC Date
)
LANGUAGE plpgsql AS
$$
DECLARE
  -- 11/20/09 added new hints
  -- these two variables are used in all SQL statements to populate date fields,
  -- the UTC date is used to populate SPCF audit fields created_date and modified_date
  v_psp_date   TIMESTAMP;                                         -- current system date and time adjusted by PSPDate offset
  v_utc_date   TIMESTAMP;                                         -- current system UTC date and time

  v_RETURN_CD text; -- return code variable for logging
  v_ERROR_DESC VARCHAR(100);-- error desc variable for logging
BEGIN

    -- In oracle the query is SYS_EXTRACT_UTC (to_timestamp_tz(concat(fn_get_psp_timestamp(0), ' US/Pacific'))). In postgres
    -- we dont have to change to US/Pacific as postgres is DST aware
    SELECT (fn_get_psp_timestamp(0)::timestamptz) AT TIME ZONE 'UTC'
           INTO v_psp_date;
    v_utc_date := p_app_server_date;

    UPDATE PSP_OFFLOAD_BATCH
    SET STATUS_CD = 'Completed',
        STATUS_EFFECIVE_DATE = v_psp_date,
        VERSION = VERSION + 1,
        MODIFIER_ID = p_user_id,
        MODIFIED_DATE = v_utc_date
    WHERE OFFLOAD_BATCH_SEQ = p_offload_batch_id;

    IF p_file_type = 'DD'
    THEN
      CALL prc_update_nacha_file_trace_number (v_RETURN_CD, v_ERROR_DESC, p_offload_batch_id, p_offload_date, p_user_id, v_utc_date, 'CCD', v_psp_date);
      CALL prc_update_nacha_file_trace_number (v_RETURN_CD, v_ERROR_DESC, p_offload_batch_id, p_offload_date, p_user_id, v_utc_date, 'PPD', v_psp_date);
    END IF;-- 'DD' File Type

    IF p_file_type = 'Tax'
    THEN
      CALL prc_update_nacha_file_trace_number (v_RETURN_CD, v_ERROR_DESC, p_offload_batch_id, p_offload_date, p_user_id, v_utc_date, 'CCDPlus', v_psp_date);
    END IF; -- 'Tax' File Type

END;
$$;