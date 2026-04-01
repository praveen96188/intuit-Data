CREATE OR REPLACE PROCEDURE PRC_UPD_COMPANY_LEDGER_BALANCE
    (
    p_company_fk varchar DEFAULT NULL
    )
    LANGUAGE plpgsql AS
    $$
    DECLARE
    v_utc_date TIMESTAMP; -- current system UTC date and time
    v_end_date   date :=  FN_GET_PSP_TIMESTAMP() - interval '1 days';
    v_ledger_balance_max_date date;

    financial_trans_state_cursor CURSOR FOR
        select /*+ IndexScan(lb psp_ledgerbalance_fk2) */ distinct date_trunc('day', transaction_state_eff_date) as transaction_state_eff_date
        from PSP_FINANCIAL_TRANS_STATE
        where company_fk = p_company_fk
          and transaction_state_eff_date < v_end_date + interval '1 days'
          and date_trunc('day', transaction_state_eff_date)>(
              select date_trunc('day', COALESCE(max(balance_date), to_date('01-JAN-2005', 'DD Mon YYYY')))
              from psp_ledger_balance lb
              where lb.company_fk=p_company_fk)
        order by date_trunc('day', transaction_state_eff_date);
    BEGIN
        SELECT timezone('UTC', CURRENT_TIMESTAMP) INTO v_utc_date;

        IF (p_company_fk IS NULL ) THEN
            return;
        END IF;

        -- get the max balance_date from psp_ledger_balance table calculate balances till that date
        SELECT max(balance_date)
        INTO v_ledger_balance_max_date
        FROM psp_ledger_balance;

        IF v_ledger_balance_max_date IS NOT NULL THEN
          v_end_date := v_ledger_balance_max_date;
        END IF;

        RAISE NOTICE 'Started at %' , to_char(clock_timestamp(), 'hh24:mi:ss');
        FOR financial_trans_state_rec IN financial_trans_state_cursor
            LOOP
              INSERT INTO psp_ledger_balance  (ledger_balance_seq, VERSION, creator_id, created_date,
                                                                modified_date, realm_id, balance_amount, balance_date,
                                                                ledger_account_fk, company_fk,reporting_type)
                    (SELECT gen_random_uuid(),
                            1,'COMPANYLEDGERBALANCERECALCJOB', v_utc_date,
                                                                             v_utc_date, -1, COALESCE((select ilb.balance_amount from psp_ledger_balance ilb where ILB.LEDGER_ACCOUNT_FK=pr.ledger_account_fk and
                                ILB.COMPANY_FK=fts.company_fk and ilb.reporting_type=po.reporting_type
                                                                                                                                                          and date(ILB.BALANCE_DATE) = (select max(date(olb.balance_date)) from psp_ledger_balance olb where
                                    OLB.COMPANY_FK=ILB.COMPANY_FK and OLB.LEDGER_ACCOUNT_FK=ilb.ledger_account_fk and olb.reporting_type=ilb.reporting_type )), 0)+ SUM (
                                                                                                         (select ft.financial_transaction_amount from psp_financial_transaction ft where ft.financial_transaction_seq = fts.financial_transaction_fk and ft.company_fk = fts.company_fk)
                                                                                                         * (CASE  pr.credit_debit_ind
                                                                                                              WHEN 'C' THEN
                                                                                                                CASE WHEN la.balance_calculation_rule = 'CreditAddsToBalance'
                                                                                                                       THEN 1 ELSE -1 END
                                                                                                              WHEN 'D' THEN
                                                                                                                CASE WHEN la.balance_calculation_rule = 'DebitAddsToBalance'
                                                                                                                       THEN 1 ELSE -1 END
                                                                                                            END)
                                                                                                 ) amount, date(fts.transaction_state_eff_date) newbal_date,
                                                                             pr.ledger_account_fk,
                                                                             fts.company_fk,po.reporting_type
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
                       AND date(fts.transaction_state_eff_date) = date(financial_trans_state_rec.transaction_state_eff_date)
                       AND fts.transaction_state_eff_date BETWEEN  financial_trans_state_rec.transaction_state_eff_date  - interval '1 days' AND   financial_trans_state_rec.transaction_state_eff_date  + interval '1 days'
                       AND  fts.company_fk = p_company_fk
                     GROUP BY fts.company_fk, date(fts.transaction_state_eff_date),
                              pr.ledger_account_fk,po.reporting_type);

            END LOOP;

        RAISE NOTICE 'Finished at %' , to_char(clock_timestamp(), 'hh24:mi:ss');
    end;
$$

