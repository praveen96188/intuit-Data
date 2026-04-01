CREATE OR REPLACE FUNCTION FN_GET_LAST_DAY_OF_QUARTER (p_input TIMESTAMP) RETURN TIMESTAMP
AS
  ts TIMESTAMP;
  v_tz_offset VARCHAR2 (100 Char);
BEGIN

   SELECT SYSTEM_PARAMETER_VALUE
    INTO v_tz_offset
    FROM PSP_SYSTEM_PARAMETER
       WHERE SYSTEM_PARAMETER_CD='PSP_DATE_TIMEZONE_OFFSET';

   -- Find the last day of the quarter for the given date and subtract the PSP TZ offset.
   SELECT sys_extract_utc(to_timestamp(add_months(trunc(p_input,'Q'),3) - 1) - NUMTODSINTERVAL(((TO_NUMBER(v_tz_offset) * 3600 * 1000))/1000, 'SECOND'))
    INTO ts
    FROM DUAL;

   RETURN ts;
END FN_GET_LAST_DAY_OF_QUARTER;
/