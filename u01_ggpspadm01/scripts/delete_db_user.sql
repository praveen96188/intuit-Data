CREATE OR REPLACE FUNCTION pspadm.delete_db_user  (in lv_uid VARCHAR(100),
                                                   out lv_request_status VARCHAR(100),
                                                   out lv_error_code VARCHAR(100))
    LANGUAGE plpgsql
AS $$

/* ------- Variable Declarations ------------- */
DECLARE

l_user_check    int := 0;

BEGIN

SELECT
    count(*)
INTO l_user_check
FROM
    pg_user
WHERE
        usename =lv_uid
  and usename not like '%psp%';

IF l_user_check >0 THEN
        EXECUTE 'drop user if exists ' || lv_uid;
        lv_request_status := 'Success';
        lv_error_code := NULL;
ELSE
        lv_request_status := 'Failure';
        lv_error_code := 'EIIS-13004';
END IF;

EXCEPTION
    WHEN others THEN
        raise exception '%',sqlerrm;
END;
$$
;
