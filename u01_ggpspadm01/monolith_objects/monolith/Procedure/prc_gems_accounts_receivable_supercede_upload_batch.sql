-- Simple procedure to supercede an existing gems upload batch
-- (set batch state to 'Superceded' and disassociate all FTS records)
-- (if p_gems_upload_batch_id == 0, then procedure takes no action)
CREATE OR REPLACE PROCEDURE prc_gems_accounts_receivable_supercede_upload_batch (
    p_old_upload_batch_id IN NUMERIC, -- existing gems upload batch to supercede (0 if none)
    p_user_id IN VARCHAR, -- for audit purposes
    p_app_server_date IN TIMESTAMP -- UTC Date
)
    LANGUAGE plpgsql AS
$$
DECLARE
    v_old_batch_key VARCHAR(100);
BEGIN
    IF p_old_upload_batch_id != 0
    THEN
        BEGIN
            SELECT gems_upload_batch_seq
            INTO v_old_batch_key
            FROM psp_gems_upload_batch
            WHERE batch_id = p_old_upload_batch_id;

            UPDATE psp_gems_upload_batch
            SET VERSION               = VERSION + 1,
                modifier_id           = p_user_id,
                modified_date         = p_app_server_date,
                upload_status         = 'Superceded',
                status_effective_date = p_app_server_date
            WHERE gems_upload_batch_seq = v_old_batch_key;

            UPDATE psp_financial_trans_state
            SET gems_upload_batch_fk = NULL
            WHERE gems_upload_batch_fk = v_old_batch_key;
        EXCEPTION
            -- potentional of select returning zero data, do nothing...
            WHEN NO_DATA_FOUND
                THEN
                    NULL;
        END;
    END IF;
END;
$$;
