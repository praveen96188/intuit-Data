CREATE OR REPLACE FUNCTION FN_GET_LAST_DAY_OF_QUARTER(p_input TIMESTAMP) returns TIMESTAMP
AS $$
DECLARE
  ts TIMESTAMP;
  v_tz_offset varchar;
BEGIN
  SELECT SYSTEM_PARAMETER_VALUE
    INTO v_tz_offset
    FROM PSP_SYSTEM_PARAMETER
       WHERE SYSTEM_PARAMETER_CD='PSP_DATE_TIMEZONE_OFFSET';

    -- Find the last day of the quarter for the given date and subtract the PSP TZ offset.
    SELECT timezone('UTC', (cast(date_trunc('QUARTER', p_input) AS timestamptz) + interval '3 month') - interval '1 days')
    INTO ts;

   RETURN ts;
END;
$$
  LANGUAGE plpgsql;

--select FN_GET_LAST_DAY_OF_QUARTER(current_timestamp::timestamp without time zone);



