CREATE OR REPLACE FUNCTION fn_get_ledger_balance (
   p_company   VARCHAR,
   p_ledger    VARCHAR
)
   RETURNS NUMERIC
AS $$
/******************************************************************************
   PURPOSE: Return current ledger balance calculated from the most recent psp_ledger_balance and
   any financial transaction states that have been made since then (i.e. today)
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        05.04.2010  David            Created
   1.1        09.07.2010  David/Tushar     Removed trunc in second query since
                                           it was pulling too many partitions
   1.2        09.20.2010  David/Tushar     Changed second part to use max date from all
                                           ledger accounts since it was using all
                                           partitions when there was no data in balance table
******************************************************************************/
DECLARE
   v_lb_date       TIMESTAMP;
   v_lb_balance    NUMERIC;
   v_fts_balance   NUMERIC;
BEGIN
  BEGIN
      SELECT /*+ IndexScan(lb2 psp_ledger_balance_u2) */ balance_date, balance_amount
        INTO v_lb_date, v_lb_balance
        FROM psp_ledger_balance lb
      WHERE lb.company_fk = p_company
         AND lb.ledger_account_fk = p_ledger
         AND date(lb.balance_date) =
                (SELECT date(MAX (lb2.balance_date))
                   FROM psp_ledger_balance lb2
                  WHERE lb2.company_fk = p_company
                    AND lb2.ledger_account_fk = p_ledger);
  END;


  SELECT SUM (  ft.financial_transaction_amount
               * (CASE WHEN pr.credit_debit_ind='C' then (CASE WHEN la.balance_calculation_rule='CreditAddsToBalance' then 1 else -1 end)
                      WHEN pr.credit_debit_ind='D' then (CASE WHEN la.balance_calculation_rule='DebitAddsToBalance' then 1 else -1 end)
                      end)
              )
     INTO v_fts_balance
     FROM psp_financial_trans_state fts,
          psp_financial_transaction ft,
          psp_posting_rule pr,
          psp_ledger_account la
    WHERE fts.financial_transaction_fk = ft.financial_transaction_seq
      AND pr.transaction_state_fk = fts.transaction_state_fk
      AND pr.transaction_type_fk = ft.transaction_type_fk
      AND la.ledger_account_cd = pr.ledger_account_fk
      AND fts.transaction_state_eff_date > (select COALESCE(date_trunc('DAY', max(balance_date)) + interval '1 DAYS', to_date('01/01/1970', 'MM/DD/YYYY')) from psp_ledger_balance)
      AND fts.company_fk = p_company
      AND ft.company_fk = fts.company_fk
      AND pr.ledger_account_fk = p_ledger;

  RETURN (COALESCE (v_lb_balance, 0) + COALESCE (v_fts_balance, 0));
END;
$$
LANGUAGE plpgsql;

