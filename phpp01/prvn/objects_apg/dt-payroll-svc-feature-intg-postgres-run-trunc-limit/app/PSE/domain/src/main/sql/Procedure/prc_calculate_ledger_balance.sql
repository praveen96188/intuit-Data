CREATE OR REPLACE PROCEDURE PRC_CALCULATE_LEDGER_BALANCE
        ( p_ledger_update_date DATE DEFAULT sysdate-1)
IS

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

    -- the UTC date is used to populate SPCF audit fields created_date and modified_date
    v_utc_date TIMESTAMP; -- current system UTC date and time

    -- the time-corrected ledger_update_date is used throughout the code instead of p_ledger_update_date
    v_ledger_update_date TIMESTAMP;

    TYPE fts_record IS RECORD (
        fts_company_fk psp_financial_trans_state.company_fk%TYPE,
        newbal_date psp_financial_trans_state.transaction_state_eff_date%TYPE,
        amount number,
        ledger_acc_fk psp_posting_rule.ledger_account_fk%TYPE,
        reporting_type psp_offering.reporting_type%TYPE
    );

    TYPE fts_type IS TABLE OF fts_record;
    fts_list fts_type;

BEGIN

          SELECT SYS_EXTRACT_UTC(SYSTIMESTAMP) INTO v_utc_date FROM DUAL;

          -- p_ledger_update_date is PDT, but must also have the time component in a way
          -- that truncate(p_ledger_update_date) = truncate(p_ledger_update_date as UTC)
          SELECT trunc(p_ledger_update_date) + .5 INTO v_ledger_update_date  FROM DUAL;  -- this sets the time to 12:00:00 PM

          dbms_output.put_line('Merging PSP_LEDGER_BALANCE, starting merge  - ' || to_char(v_ledger_update_date, 'yyyy-mm-dd') || ' - Started at ' || to_char(systimestamp, 'hh24:mi:ss'));
        SELECT /*+ parallel(4) */ fts.company_fk,
          TRUNC (fts.transaction_state_eff_date),
          SUM (  (select ft.financial_transaction_amount from psp_financial_transaction ft
                  where ft.financial_transaction_seq = fts.financial_transaction_fk and ft.company_fk = fts.company_fk)
              * DECODE (pr.credit_debit_ind,
                        'C', DECODE (la.balance_calculation_rule,'CreditAddsToBalance', 1,-1),
                        'D', DECODE (la.balance_calculation_rule,'DebitAddsToBalance', 1,-1)
                     )
              ),
          pr.ledger_account_fk,
          po.reporting_type BULK COLLECT INTO fts_list
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
          AND PO.SERVICE_CODE='DirectDeposit'
          AND TRUNC (fts.transaction_state_eff_date) = trunc(v_ledger_update_date)
          AND fts.transaction_state_eff_date BETWEEN v_ledger_update_date - 1 AND v_ledger_update_date + 1
        GROUP BY fts.company_fk,
                 TRUNC (fts.transaction_state_eff_date),
                 pr.ledger_account_fk,
                 po.reporting_type;

         FORALL i IN 1..fts_list.count
            MERGE INTO psp_ledger_balance tgt
             USING (SELECT fts_list(i).fts_company_fk company_fk, fts_list(i).newbal_date newbal_date, fts_list(i).amount amount,
                        fts_list(i).ledger_acc_fk ledger_account_fk, fts_list(i).reporting_type reporting_type from dual) src
            ON ( tgt.company_fk = src.company_fk
             AND TGT.reporting_type =src.reporting_type
                 AND tgt.ledger_account_fk = src.ledger_account_fk
                 AND TRUNC (tgt.balance_date) = TRUNC (src.newbal_date))
             WHEN MATCHED THEN
                UPDATE
                    SET tgt.balance_amount = tgt.balance_amount + src.amount,
                    modified_date = v_utc_date,
                    version = version+1,
                    modifier_id='LEDGERBALANCEBATCHJOBMERGEUPD'
             WHEN NOT MATCHED THEN
                INSERT (tgt.ledger_balance_seq, VERSION, creator_id, created_date,
                modified_date, realm_id, balance_amount, balance_date,
                ledger_account_fk, company_fk, reporting_type)
                VALUES (fn_format_sysguid (SYS_GUID ()), 1, 'LEDGERBALANCEBATCHJOBMERGEINS', v_utc_date,
                    v_utc_date, -1,
                    NVL((select ilb.balance_amount from psp_ledger_balance ilb
                    where ILB.LEDGER_ACCOUNT_FK=src.ledger_account_fk
                    and ILB.COMPANY_FK=src.company_fk
                    and trunc(ILB.BALANCE_DATE) = (select trunc(max(trunc(olb.balance_date))) from psp_ledger_balance olb
                    where OLB.COMPANY_FK=ILB.COMPANY_FK
                    and OLB.LEDGER_ACCOUNT_FK=ilb.ledger_account_fk )), 0) + src.amount,
                    src.newbal_date, src.ledger_account_fk,
                    src.company_fk, src.reporting_type);

        dbms_output.put_line('Finished merging PSP_LEDGER_BALANCE  - ' || to_char(systimestamp, 'hh24:mi:ss'));

 COMMIT;
END PRC_CALCULATE_LEDGER_BALANCE;
/
