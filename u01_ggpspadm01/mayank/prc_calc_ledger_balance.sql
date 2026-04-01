CREATE OR REPLACE PROCEDURE PRC_CALCULATE_LEDGER_BALANCE(
    p_ledger_update_date DATE DEFAULT now() - INTERVAL '1 DAYS'
)
    LANGUAGE plpgsql AS
$$
DECLARE
    -- the UTC date is used to populate SPCF audit fields created_date and modified_date
v_utc_date           timestamp; -- current system UTC date and time

    -- the time-corrected ledger_update_date is used throughout the code instead of p_ledger_update_date
    v_ledger_update_date timestamp;
    total_rows           NUMERIC;
    fts_list             fts_record[];
BEGIN
SELECT timezone('UTC', CURRENT_TIMESTAMP) INTO v_utc_date;

-- p_ledger_update_date is PDT, but must also have the time component in a way
-- that truncate(p_ledger_update_date) = truncate(p_ledger_update_date as UTC)
SELECT date_trunc('day', p_ledger_update_date) + INTERVAL '12 HOURS' INTO v_ledger_update_date; -- this sets the time to 12:00:00 PM

RAISE NOTICE 'Merging PSP_LEDGER_BALANCE, starting merge  - % - Started at %' , to_char(v_ledger_update_date, 'yyyy-mm-dd'), to_char(clock_timestamp(), 'hh24:mi:ss');

SELECT ARRAY_AGG(row (company_fk,newbal_date, amount, ledger_account_fk, reporting_type)::fts_record)
into fts_list
from (
         SELECT fts.company_fk,
                date(fts.transaction_state_eff_date) as newbal_date,
                SUM((select ft.financial_transaction_amount from psp_financial_transaction ft where ft.financial_transaction_seq = fts.financial_transaction_fk and ft.company_fk = fts.company_fk)
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
           AND pc.company_seq = fts.company_fk
           AND pc.company_seq = PCO.COMPANY_FK
           AND PCO.OFFERING_FK = PO.OFFERING_SEQ
           --AND PO.REPORTING_TYPE IN ('DirectDeposit','Tax')
           AND PO.SERVICE_CODE = 'DirectDeposit'
           AND date(fts.transaction_state_eff_date) = date(v_ledger_update_date)
           AND fts.transaction_state_eff_date BETWEEN v_ledger_update_date - INTERVAL '1 DAYS' AND v_ledger_update_date + INTERVAL '1 DAYS'
         GROUP BY fts.company_fk,
             date(fts.transaction_state_eff_date),
             pr.ledger_account_fk,
             po.reporting_type) as ft;

MERGE INTO psp_ledger_balance tgt
    USING (
        SELECT ftsl.fts_company_fk company_fk, ftsl.newbal_date newbal_date, ftsl.amount amount,
               ftsl.ledger_acc_fk ledger_account_fk, ftsl.reporting_type reporting_type from unnest(fts_list) as ftsl
    ) src
    ON ( tgt.company_fk = src.company_fk
        AND TGT.reporting_type =src.reporting_type
        AND tgt.ledger_account_fk = src.ledger_account_fk
        AND date(tgt.balance_date) = date(src.newbal_date))
    WHEN MATCHED THEN
        UPDATE
            SET
                balance_amount = balance_amount + src.amount,
                modified_date = v_utc_date,
                version = version + 1,
                modifier_id = 'LEDGERBALANCEBATCHJOBMERGEUPD'
    WHEN NOT MATCHED THEN
        INSERT
            (ledger_balance_seq, VERSION, creator_id, created_date,
             modified_date, realm_id, balance_amount, balance_date,
             ledger_account_fk, company_fk, reporting_type)
            VALUES (
                    gen_random_uuid(),
                    1, 'LEDGERBALANCEBATCHJOBMERGEINS', v_utc_date,
                    v_utc_date, -1,
                    COALESCE((select ilb.balance_amount from psp_ledger_balance ilb
                                where ILB.COMPANY_FK = src.company_fk
                                and ILB.LEDGER_ACCOUNT_FK = src.ledger_account_fk
                                    and date(ILB.BALANCE_DATE) = (select date(max(date(olb.balance_date))) from psp_ledger_balance olb
                                        where OLB.COMPANY_FK = ilb.COMPANY_FK
                                        and OLB.LEDGER_ACCOUNT_FK = ilb.ledger_account_fk)), 0) + src.amount,
                    src.newbal_date, src.ledger_account_fk,
                    src.company_fk, src.reporting_type);

fts_list = NULL;

RAISE NOTICE 'Finished merging PSP_LEDGER_BALANCE  - %' , to_char(clock_timestamp(), 'hh24:mi:ss');

END;

$$

