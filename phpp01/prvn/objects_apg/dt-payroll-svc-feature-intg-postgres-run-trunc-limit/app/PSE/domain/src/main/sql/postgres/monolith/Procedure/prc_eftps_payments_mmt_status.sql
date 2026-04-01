CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_MMT_STATUS
   (
       IN p_calling_procedure           varchar,
       IN p_user_id           		    varchar,  -- for audit purposes
       IN p_app_server_date			    timestamp, -- UTC Date
       IN p_payment_file_seq	  		varchar,  -- primary key of payment file
       IN p_payment_initiation_date     timestamp
   )
   LANGUAGE plpgsql AS
    $$
    DECLARE
        -- unused
        v_return_cd text; -- return code variable for logging
        v_error_desc varchar(100); -- error desc variable for logging
    BEGIN
        RAISE NOTICE 'update mmt status started  - %', to_char(clock_timestamp(), 'hh24:mi:ss');

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
                'PSP_MONEY_MOVEMENT_TRANSACTION',--  p_ObjectName        IN   VARCHAR2,
                'N/A',                           --p_UserName            IN   VARCHAR2,
                to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                'Updating MONEY_MOVEMENT_TRANSACTION');

        -- For each MMT we are going to update the tax_payment_status on below,
        -- recalculate the ATF payments passing in the current and new payment status.
        CALL prc_recalc_atf_payments_eftps(p_calling_procedure, p_user_id, p_app_server_date, p_payment_file_seq, p_payment_initiation_date);

        -- update MMT TaxPaymentStatus to mirror EftpsPaymentDetail status updated by batch job, set status effective date
        UPDATE psp_money_movement_transaction mmt
        SET (modifier_id, modified_date, tax_payment_status, tax_pmtstatus_effectivedate) = (
            SELECT
                p_user_id, p_app_server_date, efpd.status_cd, p_app_server_date
            FROM
                psp_eftps_payment_detail efpd
            WHERE
                    efpd.parent_file_fk = p_payment_file_seq
              AND efpd.money_movement_transaction_fk = mmt.money_movement_transaction_seq
              AND mmt.initiation_date = p_payment_initiation_date
              AND efpd.company_fk = mmt.company_fk
        ), version = version + 1
        WHERE
                mmt.initiation_date = p_payment_initiation_date
          AND (mmt.money_movement_transaction_seq, mmt.company_fk) in (
            SELECT
                money_movement_transaction_fk, company_fk
            FROM
                psp_eftps_payment_detail efpd
            WHERE
                    efpd.parent_file_fk = p_payment_file_seq
        );

        RAISE NOTICE 'update mmt status finished  - %', to_char(clock_timestamp(), 'hh24:mi:ss');

    end;
    $$