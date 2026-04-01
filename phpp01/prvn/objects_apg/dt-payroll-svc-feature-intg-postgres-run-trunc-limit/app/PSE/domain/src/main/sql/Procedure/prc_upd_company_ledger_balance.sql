CREATE OR REPLACE PROCEDURE PRC_UPD_COMPANY_LEDGER_BALANCE
    (  p_company_fk varchar2 DEFAULT NULL )
IS

    v_utc_date TIMESTAMP; -- current system UTC date and time
    v_end_date   date :=  FN_GET_PSP_TIMESTAMP -1;
    v_ledger_balance_max_date date;

    CURSOR financial_trans_state_cursor IS
        select distinct (trunc(transaction_state_eff_date)) as transaction_state_eff_date  from PSP_FINANCIAL_TRANS_STATE where company_fk = p_company_fk
            and transaction_state_eff_date < v_end_date + 1 and trunc(transaction_state_eff_date)>(select trunc(nvl(max(balance_date),to_date('01-JAN-2005'))) from psp_ledger_balance lb where lb.company_fk=p_company_fk) order by trunc(transaction_state_eff_date);

BEGIN

    SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL;

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

    dbms_output.put_line( 'Started at ' || to_char(systimestamp, 'yyyy-mm-dd hh24:mi:ss'));
    FOR financial_trans_state_rec IN financial_trans_state_cursor
    LOOP
        INSERT INTO psp_ledger_balance  (ledger_balance_seq, VERSION, creator_id, created_date,
              modified_date, realm_id, balance_amount, balance_date,
              ledger_account_fk, company_fk,reporting_type)
                  (SELECT /*+ index(fts PSP_FINANCIALTRANSACTIONST_FK1) */ fn_format_sysguid (SYS_GUID ()), 1,'COMPANYLEDGERBALANCERECALCJOB', v_utc_date,
                    v_utc_date, -1, nvl((select ilb.balance_amount from psp_ledger_balance ilb where ILB.LEDGER_ACCOUNT_FK=pr.ledger_account_fk and
                    ILB.COMPANY_FK=fts.company_fk and ilb.reporting_type=po.reporting_type
                    and trunc(ILB.BALANCE_DATE) = (select max(trunc(olb.balance_date)) from psp_ledger_balance olb where
                                          OLB.COMPANY_FK=ILB.COMPANY_FK and OLB.LEDGER_ACCOUNT_FK=ilb.ledger_account_fk and olb.reporting_type=ilb.reporting_type )), 0)+ SUM (
                                          (select ft.financial_transaction_amount from psp_financial_transaction ft
                                            where ft.financial_transaction_seq = fts.financial_transaction_fk and ft.company_fk = fts.company_fk)
                        * DECODE (pr.credit_debit_ind,
                                  'C', DECODE (la.balance_calculation_rule,'CreditAddsToBalance', 1,-1),
                                  'D', DECODE (la.balance_calculation_rule,'DebitAddsToBalance', 1,-1)
                                 )
                       ) amount, TRUNC (fts.transaction_state_eff_date) newbal_date,
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
               AND TRUNC (fts.transaction_state_eff_date) = trunc(financial_trans_state_rec.transaction_state_eff_date)
               AND fts.transaction_state_eff_date BETWEEN  financial_trans_state_rec.transaction_state_eff_date  - 1 AND   financial_trans_state_rec.transaction_state_eff_date  + 1
               AND  fts.company_fk = p_company_fk
          GROUP BY fts.company_fk, TRUNC(fts.transaction_state_eff_date),
                   pr.ledger_account_fk,po.reporting_type);

    END LOOP;

    dbms_output.put_line( 'Finished at ' || to_char(systimestamp, 'yyyy-mm-dd hh24:mi:ss'));

END PRC_UPD_COMPANY_LEDGER_BALANCE;
/