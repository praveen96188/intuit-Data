CREATE OR REPLACE PROCEDURE prc_achtransactionprocessor
    (
        IN  p_source_system_cd              varchar,
        IN  p_user_id                       varchar,               -- For audit purposes
        IN  p_processing_date               timestamp,              -- UTC Date
        IN  p_application_server_date       timestamp,
        OUT p_number_of_records             int
    )
    LANGUAGE plpgsql AS
    $$
    DECLARE
        v_psp_date timestamp;

    BEGIN

        SELECT timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz))
        INTO v_psp_date;

        -- PSRV001109: Changed the way we're selecting financial transctions for state change.
        -- Previously: We were selecting by ft.settlement_date <= p_processing_date, which was causing full table/partition scans that took hours to complete.
        -- Currently:  We are now selecting by [ft.settlement_date between (p_processing_date - 45) and p_processing_date], i.e a 45 day look-back window.
        -- KP

        -- Create Completed Transaction States for all transactions in the Offload Batch
        INSERT INTO psp_financial_trans_state
        (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
         CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
         REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
         GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
         TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
            (SELECT
                    gen_random_uuid(),
                    0, p_user_id, p_application_server_date,
                    p_user_id, p_application_server_date, -1, date_trunc('day',v_psp_date) + INTERVAL '12 HOURS', NULL,
                    NULL, ft.financial_transaction_seq, 'Completed', NULL, ft.company_fk, ft.transaction_type_fk
             FROM psp_financial_transaction ft
                      INNER JOIN psp_company c ON c.company_seq = ft.company_fk
             WHERE c.source_system_cd = p_source_system_cd
               and ft.current_transaction_state_fk='Executed'
               and ft.settlement_type_cd in('ACH', 'ApplyForward', 'EFTPSDirectDebit','EDI')
               and ft.settlement_date between (p_processing_date - INTERVAL '45 DAYS') and p_processing_date
               and ft.transaction_type_fk IN (
                                              'EmployeeDdCredit',
                                              'ServiceSalesAndUseTax',
                                              'EmployerTaxReturnedCredit',
                                              'EmployerTaxRefundCredit',
                                              'EmployerTaxReturnedRefundCredit',
                                              'EmployerTaxCreditReturnedTransfer',
                                              'EmployerTaxCreditApplied',
                                              'EmployerTaxOverpaymentApplied',
                                              'AgencyTaxOverpayment',
                                              'EmployerTaxDirectDebit',
                                              'EmployerTaxDirectOverpaymentApplied',
                                              'AgencyDirectOverpayment',
                                              'AgencyDirectDebit',
                                              'AgencyDirectCredit',
                                              'AgencyTaxRecredit',
                                              'AgencyTaxRedebit',
                                              'AgencyTaxCredit',
                                              'AgencyTaxDebit',
                                              'AgencyTaxOverpaymentApplied')
            );

        -- Set the status of Financial Transactions to Completed for all transactions in the Offload Batch
        UPDATE psp_financial_transaction ft
        SET current_transaction_state_fk = 'Completed',
            VERSION = VERSION + 1,
            modifier_id = p_user_id,
            modified_date = p_application_server_date
        WHERE ft.current_transaction_state_fk='Executed'
          and ft.settlement_type_cd in ('ACH', 'ApplyForward', 'EFTPSDirectDebit','EDI')
          and ft.settlement_date between (p_processing_date - INTERVAL '45 DAYS') and p_processing_date
          and ft.transaction_type_fk IN (
                                         'EmployeeDdCredit',
                                         'ServiceSalesAndUseTax',
                                         'EmployerTaxReturnedCredit',
                                         'EmployerTaxRefundCredit',
                                         'EmployerTaxReturnedRefundCredit',
                                         'EmployerTaxCreditReturnedTransfer',
                                         'EmployerTaxCreditApplied',
                                         'EmployerTaxOverpaymentApplied',
                                         'AgencyTaxOverpayment',
                                         'EmployerTaxDirectDebit',
                                         'EmployerTaxDirectOverpaymentApplied',
                                         'AgencyDirectOverpayment',
                                         'AgencyDirectDebit',
                                         'AgencyDirectCredit',
                                         'AgencyTaxRecredit',
                                         'AgencyTaxRedebit',
                                         'AgencyTaxCredit',
                                         'AgencyTaxDebit',
                                         'AgencyTaxOverpaymentApplied')
          and EXISTS
            (
                SELECT 'T'
                FROM  PSP_COMPANY c
                WHERE  c.company_seq = ft.company_fk
                  and c.source_system_cd = p_source_system_cd
            );

        GET DIAGNOSTICS p_number_of_records := ROW_COUNT;
    END;
    $$