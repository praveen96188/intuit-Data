CREATE OR REPLACE PROCEDURE prc_recalculate_atf_payments
    (

    p_user_id                     IN varchar,  -- for audit purposes
    p_app_server_date             IN timestamp, -- UTC Date
    p_mmt_seq                     IN psp_money_movement_transaction.money_movement_transaction_seq%TYPE,
    p_payment_template_cd         IN psp_money_movement_transaction.payment_template_fk%TYPE,
    p_payment_method              IN psp_money_movement_transaction.money_movement_payment_method%TYPE,
    p_payment_status              IN psp_money_movement_transaction.tax_payment_status%TYPE,
    p_pay_period_end              IN psp_money_movement_transaction.payment_period_end%TYPE,
    p_initiation_date             IN psp_money_movement_transaction.initiation_date%TYPE,
    p_company_seq                 IN psp_money_movement_transaction.company_fk%TYPE,
    p_bulk_loading                IN BOOLEAN DEFAULT FALSE
    )
    LANGUAGE plpgsql AS
    $$
    DECLARE
        v_amount                      psp_atfpayments_to_process.amount%TYPE;
        v_settlement_date             TIMESTAMP;
        v_treat_as_zero               BOOLEAN;        -- Some method/status combinations will be treated as 0 regardless of FT amounts.
        lawRec                        psp_law;
        ftRec                         psp_financial_transaction;
    BEGIN
        IF p_payment_method in ('EFTPS', 'EFTPSDirectDebit', 'HPDE', 'HPDERefund', 'CheckPayment', 'ACHCredit', 'ACHDebit', 'EDI', 'SuperCheck') AND
           p_payment_status in ('AcknowledgedByAgency', 'ReturnedTaxPaid', 'RejectedByAgency', 'ReturnedTaxNotPaid', 'None') THEN

            -- If we are performing an initial data load, this is unnecessary.
            IF (p_bulk_loading = FALSE) THEN
                -- Delete any existing ATF Payment records for this MMT.
                DELETE from PSP_ATFPAYMENTS_TO_PROCESS
                WHERE company_fk = p_company_seq and money_movement_transaction_fk = p_mmt_seq;
            END IF;

            -- For each law that is part of this MMT's payment template.
            FOR lawRec IN ( select psp_law.law_id
                            from psp_law
                                     join psp_payment_template pt on pt.payment_template_cd = psp_law.payment_template_fk
                            where pt.payment_template_cd = p_payment_template_cd
                              and pt.support_start_date <= p_pay_period_end )
                LOOP
                    v_treat_as_zero := FALSE;
                    v_amount := 0.0;
                    v_settlement_date := null;

                    IF p_payment_status in ('RejectedByAgency', 'ReturnedTaxNotPaid') THEN
                        v_treat_as_zero := TRUE;
                    END IF;

                    DECLARE
                    ftRec psp_financial_transaction%rowtype;
                     BEGIN

                    FOR ftRec IN ( select ft.*
                                   from psp_financial_transaction ft
                                   where ft.company_fk = p_company_seq
                                     and FT.MONEY_MOVEMENT_TRANSACTION_FK = p_mmt_seq
                                     and ft.law_fk = lawRec.law_id
                                     and FT.SETTLEMENT_DATE >= p_initiation_date
                                     and date_trunc('day', FT.SETTLEMENT_DATE) >= date_trunc('day', p_initiation_date))
                        LOOP
                            -- We may be setting this to the same value multiple times...
                            v_settlement_date := ftRec.settlement_date;
                            -- Calculate this FT's impact on the total amount.
                            IF ftRec.current_transaction_state_fk IN ('Cancelled', 'Voided') THEN
                                v_treat_as_zero := TRUE;
                                exit;
                            ELSIF ftRec.TRANSACTION_TYPE_FK IN ('AgencyPostBALFHPDETaxPayment', 'AgencyPostBALFHPDETaxRefund') THEN
                                v_treat_as_zero := TRUE;
                                exit;
                                -- Determine if this is an addition or subtraction.
                            ELSIF ftRec.TRANSACTION_TYPE_FK IN ('AgencyTaxDebit', 'AgencyDirectDebit', 'AgencyHPDETaxRefund', 'AgencyTaxOverpaymentApplied') THEN
                                v_amount := v_amount - ftRec.FINANCIAL_TRANSACTION_AMOUNT;
                            ELSIF ftRec.TRANSACTION_TYPE_FK IN ('AgencyTaxCredit', 'AgencyDirectCredit', 'AgencyHPDETaxPayment') THEN
                                v_amount := v_amount + ftRec.FINANCIAL_TRANSACTION_AMOUNT;
                            END IF;

                        END LOOP;
                      END;
                    -- Only create an ATF record if we found at least 1 FT for this Law.
                    IF v_settlement_date IS NOT NULL THEN
                        -- If we've determined that the amount should be zero, override the calculated amount.
                        IF v_treat_as_zero THEN
                            v_amount := 0;
                        END IF;

                        -- Insert the record while converting the pay_period_end date to the equivalent quarter end date.
                        insert into PSP_ATFPAYMENTS_TO_PROCESS (atfpayments_to_process_seq, version, creator_id, created_date, modifier_id, modified_date,
                                                                realm_id, payment_date, quarter_end_date, law_fk, money_movement_transaction_fk, company_fk, amount)
                        values (
                                gen_random_uuid(),
                                1, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1, v_settlement_date,
                                FN_GET_LAST_DAY_OF_QUARTER(p_pay_period_end), lawRec.law_id, p_mmt_seq, p_company_seq, v_amount);

                    END IF;
                END LOOP;
        END IF;
    END;
    $$