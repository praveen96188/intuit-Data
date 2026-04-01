-- Main procedure that will be called from the batch job
-- 1. Supercede an existing gems upload batch (if any - 0 for none)
-- 2. Create new gems upload batch.
-- 3. Update all appropriate FTS records with new gems upload batch.
-- 4. Generate daily gems accounts receivable numbers.
CREATE OR REPLACE FUNCTION fn_gems_accounts_receivable_main (
    p_old_upload_batch_id       IN       numeric,        -- existing gems upload batch to supercede (0 if want new)
    p_user_id                   IN       VARCHAR,      -- for audit purposes
    p_app_server_date           IN       TIMESTAMP      -- UTC Date
) RETURNS REFCURSOR
    LANGUAGE plpgsql AS
$$
DECLARE
    p_cur_gems_daily_upload     REFCURSOR; -- result set to return to client
    g_gems_upload_batch_key     VARCHAR (100); -- new guid for gems upload batch
    g_batch_id                  NUMERIC;         -- new gems upload batch id
    g_psp_date                  TIMESTAMP;      -- current system date and time adjusted by PSPDate offset
    g_lookback_days             NUMERIC;         -- number of days back from g_psp_date to look for candidate financial transaction states
BEGIN
SELECT timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz))
INTO g_psp_date;

BEGIN
SELECT cast(COALESCE(SYSTEM_PARAMETER_VALUE, '5') as numeric)
INTO g_lookback_days
FROM PSP_SYSTEM_PARAMETER
WHERE SYSTEM_PARAMETER_CD = 'GEMS_ACCOUNTS_RECEIVABLE_LOOKBACK_DAYS';
EXCEPTION
        WHEN NO_DATA_FOUND
            THEN g_lookback_days := 5;
WHEN OTHERS
            THEN g_lookback_days := 5;
END;

SELECT gen_random_uuid()
INTO g_gems_upload_batch_key;

SELECT nextval('SEQ_GEMS_UPLOAD_BATCH_ID')
INTO g_batch_id;

CALL prc_gems_accounts_receivable_supercede_upload_batch (p_old_upload_batch_id, p_user_id, p_app_server_date);
CALL prc_gems_accounts_receivable_create_upload_batch (p_user_id, p_app_server_date, g_gems_upload_batch_key, g_batch_id, g_psp_date);
CALL prc_gems_accounts_receivable_assoc_fin_txn_states (p_user_id, p_app_server_date, g_gems_upload_batch_key, g_psp_date, g_lookback_days);
p_cur_gems_daily_upload:= fn_gems_accounts_receivable_calc_reveivables (g_batch_id, g_psp_date, g_lookback_days, g_gems_upload_batch_key);
RETURN p_cur_gems_daily_upload;
END;

$$;