CREATE OR REPLACE FUNCTION FN_GET_PSP_TIMESTAMP(forTrigger IN NUMERIC DEFAULT 1) RETURNS TIMESTAMP
AS $$
DECLARE
ts TIMESTAMP;
v_offset VARCHAR (100);
-- v_tz_offset VARCHAR (100);
BEGIN
   SELECT SYSTEM_PARAMETER_VALUE
     INTO v_offset
	 FROM PSP_SYSTEM_PARAMETER
	WHERE SYSTEM_PARAMETER_CD='PSP_DATE_OFFSET';

--   commenting out the below part since Postgres is Daylight Savings aware and automatically adjusts the time
--   for Daylight Savings unlike Oracle.

--   v_tz_offset:='+00.00';
--
--   If (forTrigger=0) THEN
--        SELECT SYSTEM_PARAMETER_VALUE
-- 	INTO v_tz_offset
-- 	FROM PSP_SYSTEM_PARAMETER
--        WHERE SYSTEM_PARAMETER_CD='PSP_DATE_TIMEZONE_OFFSET';
--   END IF;

  -- SELECT CURRENT_TIMESTAMP + ((interval '1 seconds')*((cast(v_offset as decimal) + (cast(v_tz_offset as decimal) * 3600 * 1000))/1000))
  SELECT CURRENT_TIMESTAMP + ((interval '1 seconds')*(cast(v_offset as bigint)/1000))
  INTO ts;

   RETURN ts;
END;
$$
  LANGUAGE plpgsql;


