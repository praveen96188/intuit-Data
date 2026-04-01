CREATE OR REPLACE PROCEDURE prc_gems_accounts_receivable_assoc_fin_txn_states (
    p_user_id           IN   VARCHAR, -- for audit purposes
    p_app_server_date   IN   TIMESTAMP, -- UTC Date
    p_gems_upload_batch_key   IN VARCHAR (100),
    p_psp_date                IN TIMESTAMP,
    p_lookback_days       IN    NUMERIC
)
    LANGUAGE plpgsql AS
$$
BEGIN
UPDATE psp_financial_trans_state fts
SET gems_upload_batch_fk = p_gems_upload_batch_key,
    VERSION = VERSION + 1,
    modifier_id = p_user_id,
    modified_date = p_app_server_date
WHERE fts.gems_upload_batch_fk IS NULL
  AND date_trunc('day',fts.transaction_state_eff_date) >= (date_trunc('day',p_psp_date)::date) - MAKE_INTERVAL(DAYS => p_lookback_days::integer)
      AND fts.transaction_state_eff_date >= (date_trunc('day',p_psp_date)::date) - MAKE_INTERVAL(DAYS => p_lookback_days::integer)
      AND fts.transaction_state_fk in ('Executed','Voided','Completed')
      AND EXISTS (
            SELECT 'T'
            FROM psp_financial_transaction ft, psp_posting_rule pr, psp_ledger_account la
            WHERE ft.financial_transaction_seq = fts.financial_transaction_fk
              AND ft.company_fk = fts.company_fk
              AND pr.transaction_state_fk = fts.transaction_state_fk
              AND pr.ledger_account_fk = la.ledger_account_cd
              AND pr.transaction_type_fk = ft.transaction_type_fk
              AND la.reporting_frequency = 'Daily'
              AND la.ledger_account_type IN ('Income', 'SUTax')
              AND ft.sku IS NOT NULL);
END;
$$;