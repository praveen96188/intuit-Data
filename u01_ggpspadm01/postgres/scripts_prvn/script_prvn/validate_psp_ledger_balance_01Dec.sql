set search_path to pspadm;
\timing ;
do $$
    declare
        fts_list  fts_record[];
        src_list  fts_record[];
        ilb_list  fts_record[];
        src_length integer;
        ilb_length integer;
        i integer;
        v_ledger_update_date timestamp;
    begin
        SELECT date_trunc('day', date'2023-11-28') + INTERVAL '12 HOURS' INTO v_ledger_update_date;
        RAISE NOTICE 'Validate PSP_LEDGER_BALANCE  - Started at %' , to_char(clock_timestamp(), 'hh24:mi:ss');

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

        RAISE NOTICE 'fts_list Size - %', array_length(fts_list, 1);

        with temp_src as ( SELECT ftsl.fts_company_fk company_fk, ftsl.newbal_date newbal_date, ftsl.amount amount,
                                  ftsl.ledger_acc_fk ledger_account_fk, ftsl.reporting_type reporting_type from unnest(fts_list) as ftsl)
        SELECT ARRAY_AGG(row (company_fk,newbal_date, amount, ledger_account_fk, reporting_type)::fts_record) into src_list
        from (select company_fk company_fk, newbal_date newbal_date, COALESCE((select ilb.balance_amount from psp_ledger_balance ilb
                                                                          where ILB.COMPANY_FK = tsrc.company_fk
                                                                            and ILB.LEDGER_ACCOUNT_FK = tsrc.ledger_account_fk
                                                                            and date(ILB.BALANCE_DATE) = (select date(max(date(olb.balance_date))) from psp_ledger_balance olb
                                                                                                          where OLB.COMPANY_FK = tsrc.COMPANY_FK
                                                                                                            and OLB.LEDGER_ACCOUNT_FK = tsrc.ledger_account_fk)), 0) + tsrc.amount amount,
                     ledger_account_fk ledger_account_fk, reporting_type reporting_type from temp_src as tsrc) as ftsrc;

        src_length =  array_length(src_list, 1);
        RAISE NOTICE 'src_list Size - %', src_length;

        with temp_ilb as ( SELECT ftsl.fts_company_fk company_fk, ftsl.newbal_date newbal_date, ftsl.amount amount,
                                  ftsl.ledger_acc_fk ledger_account_fk, ftsl.reporting_type reporting_type from unnest(fts_list) as ftsl)
        SELECT ARRAY_AGG(row (company_fk,newbal_date, amount, ledger_account_fk, reporting_type)::fts_record) into ilb_list
        from (select company_fk company_fk, newbal_date newbal_date, COALESCE((select ilb.balance_amount from psp_ledger_balance ilb
                                                                                                 where ILB.COMPANY_FK = tilb.company_fk
                                                                                                   and ILB.LEDGER_ACCOUNT_FK = tilb.ledger_account_fk
                                                                                                   and date(ILB.BALANCE_DATE) = (select date(max(date(olb.balance_date))) from psp_ledger_balance olb
                                                                                                                                 where OLB.COMPANY_FK = ilb.COMPANY_FK
                                                                                                                                   and OLB.LEDGER_ACCOUNT_FK = ilb.ledger_account_fk)), 0) + tilb.amount amount,
                     ledger_account_fk ledger_account_fk, reporting_type reporting_type from temp_ilb as tilb) as ftsrc;

        ilb_length = array_length(ilb_list, 1);
        RAISE NOTICE 'ilb_list Size - %', ilb_length;

        IF ilb_length <> src_length THEN
            RAISE NOTICE 'Lengths are not same';
        END IF;

        FOR i IN 1..ilb_length LOOP
                IF ilb_list[i] IS DISTINCT FROM src_list[i] THEN
                    RAISE NOTICE 'Items mismatched - ilb - % , src - %', ilb_list[i], src_list[i];
                END IF;
         END LOOP;

        RAISE NOTICE 'Validation completed';

        ilb_list = null;
        src_list = null;
        fts_list = null;

    end;
$$;

