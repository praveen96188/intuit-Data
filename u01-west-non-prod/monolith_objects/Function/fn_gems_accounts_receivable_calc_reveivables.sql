CREATE OR REPLACE FUNCTION fn_gems_accounts_receivable_calc_reveivables (
    p_batch_id                  IN numeric,
    p_psp_date                  IN TIMESTAMP,
    p_lookback_days             IN numeric,
    p_gems_upload_batch_key     IN VARCHAR (100)
) RETURNS REFCURSOR
    LANGUAGE plpgsql AS
$$
DECLARE
    p_cur_gems_daily_upload     REFCURSOR; -- result set to return to client
BEGIN
    OPEN p_cur_gems_daily_upload FOR
        SELECT   p_batch_id batch_id, ft.sku,
                 SUM (
                         (case when la.ledger_account_type = 'Income' then

                                       ft.sku_quantity*(case when pr.credit_debit_ind = 'C' then (case when la.balance_calculation_rule = 'CreditAddsToBalance' then 1 else -1 end)
                                                             when pr.credit_debit_ind = 'D' then (case when la.balance_calculation_rule = 'DebitAddsToBalance' then 1 else -1 end) end)
                               else 0 end)
                     ) sku_quantity,
                 SUM (
                         (case when la.ledger_account_type = 'Income' then

                                       ft.financial_transaction_amount*(case when pr.credit_debit_ind = 'C' then (case when la.balance_calculation_rule = 'CreditAddsToBalance' then 1 else -1 end)
                                                                             when pr.credit_debit_ind = 'D' then (case when la.balance_calculation_rule = 'DebitAddsToBalance' then 1 else -1 end) end)
                               else 0 end)
                     ) income_amt,
                 SUM
                     (
                         (case when la.ledger_account_type = 'SUTax' then

                                       ft.financial_transaction_amount*(case when pr.credit_debit_ind = 'C' then (case when la.balance_calculation_rule = 'CreditAddsToBalance' then 1 else -1 end)
                                                                             when pr.credit_debit_ind = 'D' then (case when la.balance_calculation_rule = 'DebitAddsToBalance' then 1 else -1 end) end)
                               else 0 end)
                     ) tax_amt
        FROM psp_financial_transaction ft,
             psp_financial_trans_state fts,
             psp_posting_rule pr,
             psp_ledger_account la
        WHERE ft.financial_transaction_seq = fts.financial_transaction_fk
                  AND ft.company_fk = fts.company_fk
                  AND fts.transaction_state_eff_date >= date(p_psp_date) - MAKE_INTERVAL(DAYS => p_lookback_days::integer)
                  --  AND ft.current_transaction_state_fk = fts.transaction_state_fk
                  AND fts.transaction_state_fk = pr.transaction_state_fk
                  AND fts.gems_upload_batch_fk = p_gems_upload_batch_key
                  AND pr.ledger_account_fk = la.ledger_account_cd
                  AND pr.transaction_type_fk = ft.transaction_type_fk
                  AND ft.sku IS NOT NULL
                  AND la.reporting_frequency = 'Daily'
                  AND la.ledger_account_type IN ('Income', 'SUTax')
        GROUP BY ft.sku
        ORDER BY ft.sku;
    RETURN p_cur_gems_daily_upload;
END;
$$ ;