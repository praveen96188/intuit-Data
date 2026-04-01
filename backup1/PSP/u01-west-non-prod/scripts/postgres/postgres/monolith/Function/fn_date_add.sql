CREATE OR REPLACE FUNCTION FN_DATE_ADD(date_val TIMESTAMP, int_val INTEGER, add_type VARCHAR(10))
    RETURNS TIMESTAMP
AS $$
BEGIN
    IF add_type = 'day' THEN
        RETURN date_val + int_val * INTERVAL '1' day;
    ELSIF add_type = 'month' THEN
        RETURN date_val + int_val * INTERVAL '1' month;
    ELSIF add_type = 'year' THEN
        RETURN date_val + int_val * INTERVAL '1' year;
ELSE
        RETURN NULL;
END IF;
END;
$$ LANGUAGE plpgsql;