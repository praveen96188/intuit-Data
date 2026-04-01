/******************************************************************************
   UPDATED: 10.03.2008
   PURPOSE: Updates ledger balance for the passed date.
            If the date is null, it will use sysdate -1
   LOGIC: 1. Insert previous ledger balance for the company (with max date).
             insert only ledger account for companies that were updated. By
             updated means we have record in "financial_transaction_state"
          2. Update all the balances for the updated txns (via merge)
          3. For new ledger account for company, Insert data (via merge)
   ASSUMPTIONS:
          1. PSP_FINANCIAL_TRANS_STATE.transaction_state_eff_date is stored as UTC. However, it is stored in a way
             that trunc(transaction_state_eff_date) = trunc(transaction_state_eff_date as PDT)
             In other words, the time component is stored in a way that the UTC date is always the same as the PDT date
             (for instance, 01:00:00 PM)
          2. PSP_LEDGER_BALANCE.balance_date has the same characteristics (but this sproc is the one that guarantees
             that)
    TODO:
        1. Performance test in LT
        2. Functional test
   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        10.03.2008  Tushar           Created
   1.1        10.11.2008  Allen            Added UTC/PDT logic to dates
   1.2        06.02.2010  Allen            Changed the insert
   1.3        02.14.2012  Zack             Updated for performance improvement
   1.4        10.15.2012  Anand            Updated for reporting types
******************************************************************************/
CREATE OR REPLACE PROCEDURE PRC_CALCULATE_LEDGER_BALANCE
(
  p_ledger_update_date DATE DEFAULT now() - INTERVAL '1 DAYS'
)
  LANGUAGE plpgsql AS
$$
DECLARE
  -- the UTC date is used to populate SPCF audit fields created_date and modified_date
  v_utc_date timestamp; -- current system UTC date and time

  -- the time-corrected ledger_update_date is used throughout the code instead of p_ledger_update_date
  v_ledger_update_date timestamp;
  total_rows                NUMERIC;
BEGIN
  SELECT timezone('UTC', CURRENT_TIMESTAMP) INTO v_utc_date;

  -- p_ledger_update_date is PDT, but must also have the time component in a way
  -- that truncate(p_ledger_update_date) = truncate(p_ledger_update_date as UTC)
  SELECT date_trunc('day',p_ledger_update_date) + INTERVAL '12 HOURS' INTO v_ledger_update_date;  -- this sets the time to 12:00:00 PM

  RAISE NOTICE 'Merging PSP_LEDGER_BALANCE, starting merge  - % - Started at %' , to_char(v_ledger_update_date, 'yyyy-mm-dd'), to_char(clock_timestamp(), 'hh24:mi:ss');

       --TODO : Use merge query after upgrading postgres to version 15
       DECLARE

         ledger_balance_cursor CURSOR FOR

         SELECT  fts.company_fk as company_fk,
                  date_trunc('day',fts.transaction_state_eff_date) as newbal_date,
                  SUM (  (select ft.financial_transaction_amount from psp_financial_transaction ft where ft.financial_transaction_seq = fts.financial_transaction_fk)
                    * (
                           case when pr.credit_debit_ind = 'C' then (case when la.balance_calculation_rule = 'CreditAddsToBalance' then 1 else -1 end)
                                when pr.credit_debit_ind = 'D' then (case when la.balance_calculation_rule = 'DebitAddsToBalance' then 1 else -1 end) end
                           )
                    ) as amount,
                  pr.ledger_account_fk as ledger_account_fk,
                  po.reporting_type as reporting_type
          FROM psp_financial_trans_state fts,
               psp_posting_rule pr,
               psp_ledger_account la,
               psp_company pc,
               psp_company_offering pco,
               psp_offering po
          WHERE pr.transaction_state_fk = fts.transaction_state_fk
            AND pr.transaction_type_fk = fts.transaction_type_fk
            AND la.ledger_account_cd = pr.ledger_account_fk
            AND pc.company_seq=fts.company_fk
            AND pc.company_seq=PCO.COMPANY_FK
            AND PCO.OFFERING_FK=PO.OFFERING_SEQ
            --AND PO.REPORTING_TYPE IN ('DirectDeposit','Tax')
            AND PO.SERVICE_CODE='DirectDeposit'
            AND date_trunc('day',fts.transaction_state_eff_date) = date_trunc('day',v_ledger_update_date)
            AND fts.transaction_state_eff_date BETWEEN v_ledger_update_date - INTERVAL '1 DAYS' AND v_ledger_update_date + INTERVAL '1 DAYS'
          GROUP BY fts.company_fk,
                   date_trunc('day',fts.transaction_state_eff_date),
                   pr.ledger_account_fk,
                   po.reporting_type;

        BEGIN

            FOR cursor_rec IN ledger_balance_cursor
               LOOP
                 UPDATE psp_ledger_balance

                 SET balance_amount = balance_amount + cursor_rec.amount,
                     modified_date = v_utc_date,
                     version = version+1,
                     modifier_id='LEDGERBALANCEBATCHJOBMERGEUPD'
                 where
                     company_fk = cursor_rec.company_fk
                     AND reporting_type = cursor_rec.reporting_type
                     AND ledger_account_fk = cursor_rec.ledger_account_fk
                   AND date_trunc('day', balance_date) = date_trunc('day',cursor_rec.newbal_date);

                  GET DIAGNOSTICS total_rows := ROW_COUNT;

                   IF total_rows = 0 THEN

                     INSERT into psp_ledger_balance (ledger_balance_seq, VERSION, creator_id, created_date,
                             modified_date, realm_id, balance_amount, balance_date,
                             ledger_account_fk, company_fk, reporting_type)
                     VALUES (
                              gen_random_uuid(),
                              1, 'LEDGERBALANCEBATCHJOBMERGEINS', v_utc_date,
                              v_utc_date, -1,
                              COALESCE((select ilb.balance_amount from psp_ledger_balance ilb
                                        where ILB.LEDGER_ACCOUNT_FK=cursor_rec.ledger_account_fk
                                          and ILB.COMPANY_FK=cursor_rec.company_fk
                                          and date_trunc('day',ILB.BALANCE_DATE) = (select date_trunc('day',max(date_trunc('day',olb.balance_date))) from psp_ledger_balance olb
                                                                                    where OLB.COMPANY_FK=ILB.COMPANY_FK
                                                                                      and OLB.LEDGER_ACCOUNT_FK=ilb.ledger_account_fk )), 0) + cursor_rec.amount,
                              cursor_rec.newbal_date, cursor_rec.ledger_account_fk,
                              cursor_rec.company_fk, cursor_rec.reporting_type);
                   end if;

                END LOOP;

       END;
  RAISE NOTICE 'Finished merging PSP_LEDGER_BALANCE  - %' , to_char(clock_timestamp(), 'hh24:mi:ss');

END;

$$