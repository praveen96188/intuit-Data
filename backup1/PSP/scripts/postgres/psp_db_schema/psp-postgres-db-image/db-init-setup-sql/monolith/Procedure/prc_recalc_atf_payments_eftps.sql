CREATE OR REPLACE PROCEDURE prc_recalc_atf_payments_eftps
    (
    IN p_calling_procedure          varchar,
	  IN p_user_id           		    varchar,  -- for audit purposes
    IN p_app_server_date			timestamp, -- UTC Date
    IN p_payment_file_seq	  		varchar,  -- primary key of payment file
    IN p_payment_initiation_date    timestamp
    )
    LANGUAGE plpgsql AS
    $$
    DECLARE
        -- unused
        v_return_cd text; -- return code variable for logging
        v_error_desc varchar(100);-- error desc variable for logging
    BEGIN
        CALL PRC_SET_PSP_EVENT_LOG (
            v_RETURN_CD,
            v_ERROR_DESC,
            'N/A',                           -- p_CompanyId          IN   VARCHAR2,
            null,                           -- p_TypeCd             IN   VARCHAR2,
            'PROD',                          -- p_DomainName         IN   VARCHAR2,
            'PSP',                           --p_ArchName            IN   VARCHAR2,
            p_calling_procedure,             -- p_CompName           IN   VARCHAR2,
            'N/A',                           -- p_HostName           IN   VARCHAR2,
            'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
            'PSP_ATFPAYMENTS_TO_PROCESS',    --  p_ObjectName        IN   VARCHAR2,
            'N/A',                           --p_UserName            IN   VARCHAR2,
            to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
            'Updating PSP_ATFPAYMENTS_TO_PROCESS');

        -- remove existing ATF payment information associated w/MMTs
        DELETE from PSP_ATFPAYMENTS_TO_PROCESS
        WHERE (money_movement_transaction_fk, company_fk) in (
            select
                money_movement_transaction_fk, company_fk
            from
                psp_eftps_payment_detail epd
            where
                    epd.parent_file_fk = p_payment_file_seq
              and epd.status_cd in ('AcknowledgedByAgency', 'ReturnedTaxPaid', 'RejectedByAgency', 'ReturnedTaxNotPaid')
        );


        -- insert acknowledged payments by law
        insert into PSP_ATFPAYMENTS_TO_PROCESS (atfpayments_to_process_seq, version, creator_id, created_date, modifier_id, modified_date,
                                                realm_id, payment_date, quarter_end_date, law_fk, money_movement_transaction_fk, company_fk, amount)
        select
               gen_random_uuid(),
               1, p_user_id, p_app_server_date, p_user_id, p_app_server_date,
            -1, timezone('UTC', to_timestamp(settlement_date)), quarter_end, law_fk, money_movement_transaction_seq, company_fk, amount
        from (
                 select
                     mmt.company_fk, mmt.money_movement_transaction_seq, FN_GET_LAST_DAY_OF_QUARTER(mmt.payment_period_end) as quarter_end, ft.law_fk,
                        percentile_cont(0.5) within group (ORDER by cast(extract(epoch from ft.settlement_date) as integer)) as settlement_date,
                     sum(
                             case
                                 when current_transaction_state_fk in ('Cancelled','Voided') then 0
                                 when transaction_type_fk in ('AgencyPostBALFHPDETaxPayment', 'AgencyPostBALFHPDETaxRefund') then 0
                                 when transaction_type_fk in ('AgencyTaxDebit', 'AgencyDirectDebit', 'AgencyHPDETaxRefund', 'AgencyTaxOverpaymentApplied') then (ft.financial_transaction_amount * -1)
                                 when transaction_type_fk in ('AgencyTaxCredit', 'AgencyDirectCredit', 'AgencyHPDETaxPayment') then ft.financial_transaction_amount
                                 else 0
                                 end
                         ) as amount
                 from
                     psp_eftps_payment_detail epd
                         join psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = epd.money_movement_transaction_fk
                            and mmt.company_fk = epd.company_fk
                        join psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                            and ft.company_fk = mmt.company_fk
                 where
                         epd.parent_file_fk = p_payment_file_seq
                   and epd.status_cd in ('AcknowledgedByAgency', 'ReturnedTaxPaid')
                   and mmt.initiation_date = p_payment_initiation_date
                   and mmt.money_movement_payment_method in ('EFTPS', 'EFTPSDirectDebit')
                   and ft.settlement_date >= p_payment_initiation_date
                   and ft.law_fk is not null
                 group by
                     mmt.company_fk,
                     mmt.money_movement_transaction_seq,
                     FN_GET_LAST_DAY_OF_QUARTER(mmt.payment_period_end),
                     ft.law_fk) as emf;


        -- rejected payments
        -- raffi: combine w/above?  case when epd.status_cd = 'RejectedByAgency' then 0
        insert into PSP_ATFPAYMENTS_TO_PROCESS (atfpayments_to_process_seq, version, creator_id, created_date, modifier_id, modified_date,
                                                realm_id, payment_date, quarter_end_date, law_fk, money_movement_transaction_fk, company_fk, amount)
        select
               gen_random_uuid(),
               1, p_user_id, p_app_server_date, p_user_id, p_app_server_date,
            -1, timezone('UTC', to_timestamp(settlement_date)), quarter_end, law_fk, money_movement_transaction_seq, company_fk, amount
        from (
                 select
                     mmt.company_fk, mmt.money_movement_transaction_seq, FN_GET_LAST_DAY_OF_QUARTER(mmt.payment_period_end) as quarter_end, ft.law_fk,
                     percentile_cont(0.5) within group (ORDER by cast(extract(epoch from ft.settlement_date) as integer)) as settlement_date,
                     0 as amount
                 from
                     psp_eftps_payment_detail epd
                         join psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = epd.money_movement_transaction_fk
                            and mmt.company_fk = epd.company_fk
                         join psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                            and ft.company_fk = mmt.company_fk
                 where
                         epd.parent_file_fk = p_payment_file_seq
                   and epd.status_cd in ('RejectedByAgency', 'ReturnedTaxNotPaid')
                   and mmt.initiation_date = p_payment_initiation_date
                   and mmt.money_movement_payment_method in ('EFTPS', 'EFTPSDirectDebit')
                   and ft.settlement_date >= p_payment_initiation_date
                   and ft.law_fk is not null
                 group by
                     mmt.company_fk,
                     mmt.money_movement_transaction_seq,
                     FN_GET_LAST_DAY_OF_QUARTER(mmt.payment_period_end),
                     ft.law_fk) as e;
    END;
    $$