CREATE OR REPLACE PROCEDURE PRC_OFFLOAD_UPDATE_MMT(
    p_user_id IN VARCHAR, -- For audit purposes
    p_app_server_date IN TIMESTAMP, -- UTC Date
    p_offload_batch_id IN VARCHAR, -- psp_offload_batch.offload_batch_seq
    p_offload_date IN TIMESTAMP, -- UTC Date
    p_file_type IN VARCHAR -- DD or Tax
)
    LANGUAGE plpgsql AS
$$
DECLARE

    -- these two variables are used in all SQL statements to populate date fields,
    -- the UTC date is used to populate SPCF audit fields created_date and modified_date
    v_psp_date   TIMESTAMP; -- current system date and time adjusted by PSPDate offset
    v_utc_date   TIMESTAMP; -- current system UTC date and time

    v_RETURN_CD  text; -- return code variable for logging
    v_ERROR_DESC VARCHAR(100);-- error desc variable for logging
    mmt_list     seq_company_fk_record[];
BEGIN

     SELECT timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)) INTO v_psp_date;

    v_utc_date := p_app_server_date;

      CALL PRC_SET_PSP_EVENT_LOG (
        v_RETURN_CD,
        v_ERROR_DESC,
       'N/A',                          -- p_CompanyId          IN   VARCHAR2,
        null,                         -- p_TypeCd             IN   VARCHAR2,
       'PROD',                         -- p_DomainName         IN   VARCHAR2,
       'PSP',                          --p_ArchName           IN   VARCHAR2,
       'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
       'N/A',                          -- p_HostName           IN   VARCHAR2,
       'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
       'PSP_MONEY_MOVEMENT_TRANSACTION',   --  p_ObjectName         IN   VARCHAR2,
       'N/A',                          --p_UserName           IN   VARCHAR2,
        to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM TODO Replace CURRENT_TIMESTAMP with FN_GET_PSP_TIMESTAMP
       'Updating MONEY_MOVEMENT_TRANSACTION');

    --
-- Set MMTxns status to Executed
--
    SELECT ARRAY_AGG(row (mmt.money_movement_transaction_seq, mmt.company_fk)::seq_company_fk_record)
    INTO mmt_list
    FROM psp_money_movement_transaction mmt
    WHERE mmt.offload_batch_fk = p_offload_batch_id
      AND mmt.initiation_date = p_offload_date
      AND mmt.mm_transaction_amount >= 0;

    IF p_file_type = 'Tax' THEN
        /*+ IndexScan(mmt PSP_MONEY_MOVEMENT_TRANSAC_FK3)*/
        UPDATE psp_money_movement_transaction mmt
        SET status             = 'Executed',
            VERSION            = VERSION + 1,
            modifier_id        = p_user_id,
            tax_payment_status = 'SentToAgency',
            modified_date      = v_utc_date
        from unnest(mmt_list) as mmt_tmp
        where (mmt.company_fk, mmt.money_movement_transaction_seq) = (mmt_tmp.company_fk, mmt_tmp.seq);
    ELSE
        /*+ IndexScan(mmt PSP_MONEY_MOVEMENT_TRANSAC_FK3)*/
        UPDATE psp_money_movement_transaction mmt
        SET status        = 'Executed',
            VERSION       = VERSION + 1,
            modifier_id   = p_user_id,
            modified_date = v_utc_date
        from unnest(mmt_list) as mmt_tmp
        where (mmt.company_fk, mmt.money_movement_transaction_seq) = (mmt_tmp.company_fk, mmt_tmp.seq);
    END IF;

     mmt_list = NULL;

END;
$$;
