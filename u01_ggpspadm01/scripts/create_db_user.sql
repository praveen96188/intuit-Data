CREATE OR REPLACE FUNCTION pspadm.create_db_user  (in lv_uid VARCHAR(100),
                                                   in lv_password VARCHAR(100),
                                                   out lv_request_status VARCHAR(100),
                                                   out lv_error_code VARCHAR(100))
    LANGUAGE plpgsql
AS $$

/* ------- Variable Declarations ------------- */
DECLARE

l_user_check    int := 0;
    l_psp_check     int :=0;

BEGIN

Select regexp_count(lv_uid, 'psp') into l_psp_check;

If l_psp_check > 0 THEN

        RAISE EXCEPTION 'String contains restricted Keyword PSP'
            USING ERRCODE = '22023'
                , DETAIL = 'Please check the user id provided.'
                , HINT = 'Please remove PSP from the user id';

ELSE

SELECT
    count(*)
INTO l_user_check
FROM
    pg_user
WHERE
        usename = lv_uid;

IF l_user_check >0 THEN
            lv_request_status := 'Failure';
            lv_error_code := 'EIIS-13001';
ELSE
            EXECUTE 'create user ' || lv_uid || ' with password ''' || lv_password ||'''';
EXECUTE 'grant pspadm_readonly_role to ' || lv_uid;
lv_request_status := 'Success';
            lv_error_code := NULL;
END IF;
END IF;

EXCEPTION
    WHEN others THEN
        lv_request_status := 'Failure';
        lv_error_code := 'EIIS-13009';
        raise exception '%',sqlerrm;
END;
$$
;

