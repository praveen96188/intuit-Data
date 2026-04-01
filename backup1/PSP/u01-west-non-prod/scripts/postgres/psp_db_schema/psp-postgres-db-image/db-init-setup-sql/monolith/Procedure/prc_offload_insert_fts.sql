CREATE OR REPLACE PROCEDURE PRC_OFFLOAD_INSERT_FTS(
    p_user_id IN VARCHAR, -- For audit purposes
    p_app_server_date IN TIMESTAMP, -- UTC Date
    p_offload_batch_id IN VARCHAR, -- psp_offload_batch.offload_batch_seq
    p_offload_date IN TIMESTAMP -- UTC Date
)
    LANGUAGE plpgsql AS
$$
DECLARE

    -- these two variables are used in all SQL statements to populate date fields,
    -- the UTC date is used to populate SPCF audit fields created_date and modified_date

    v_psp_date TIMESTAMP; -- current system date and time adjusted by PSPDate offset
    v_utc_date TIMESTAMP; -- current system UTC date and time

    --  SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;
    ft_list    ft_record[];


BEGIN

    -- DATE LOGIC TO BE REVIEWED ********
-- I think we should use p_offload_date instead of v_psp_date ..

SELECT timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() as timestamptz)) INTO v_psp_date;

v_utc_date := p_app_server_date;

    -- Create Executed Transaction States for all transactions in the Offload Batch

SELECT ARRAY_AGG(row (FT.FINANCIAL_TRANSACTION_SEQ, ft.company_fk, ft.transaction_type_fk)::ft_record)
INTO ft_list
FROM PSP_FINANCIAL_TRANSACTION FT,
     PSP_MONEY_MOVEMENT_TRANSACTION MMT
WHERE MMT.OFFLOAD_BATCH_FK = p_offload_batch_id
  AND MMT.MONEY_MOVEMENT_TRANSACTION_SEQ = FT.MONEY_MOVEMENT_TRANSACTION_FK
  AND MMT.COMPANY_FK = FT.COMPANY_FK
  AND FT.CURRENT_TRANSACTION_STATE_FK = 'Executed'
  AND MMT.initiation_date = p_offload_date
  AND FT.SETTLEMENT_DATE >= MMT.INITIATION_DATE
  AND date_trunc('day', FT.SETTLEMENT_DATE) >= date_trunc('day', MMT.INITIATION_DATE);


INSERT INTO PSP_FINANCIAL_TRANS_STATE
(FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
 CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
 REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
 GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
 TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
    (
        SELECT gen_random_uuid(),
               0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1,
               date_trunc('day', timezone('PDT', v_psp_date)) + INTERVAL '12 HOURS',
            NULL, NULL, ftl.ft_seq, 'Executed', NULL, ftl.ft_company_fk, ftl.ft_transaction_type_fk
        from unnest(ft_list) as ftl);
ft_list = NULL;
END;
$$;