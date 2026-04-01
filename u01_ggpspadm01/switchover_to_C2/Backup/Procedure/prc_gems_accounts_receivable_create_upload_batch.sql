CREATE OR REPLACE PROCEDURE prc_gems_accounts_receivable_create_upload_batch (
    p_user_id                 IN   VARCHAR, -- for audit purposes
    p_app_server_date         IN   TIMESTAMP, -- UTC Date
    p_gems_upload_batch_key   IN VARCHAR (100),
    p_batch_id                IN NUMERIC,
    p_psp_date                IN TIMESTAMP
)
    LANGUAGE plpgsql AS
$$
BEGIN
INSERT INTO psp_gems_upload_batch
(gems_upload_batch_seq, VERSION, creator_id, created_date, modifier_id,
 modified_date, realm_id, batch_id, batch_type, upload_status,
 status_effective_date
)
VALUES (p_gems_upload_batch_key, 0, p_user_id, p_app_server_date, p_user_id,
        p_app_server_date, -1, p_batch_id, 'Daily', 'InProcess',
        p_psp_date
       );
END;
$$;