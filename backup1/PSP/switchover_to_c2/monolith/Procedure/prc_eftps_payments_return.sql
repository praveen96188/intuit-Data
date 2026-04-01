CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_RETURN
    (
    p_user_id                   IN varchar,    -- for audit purposes
    p_app_server_date           IN timestamp,   -- UTC Date
    p_return_file_id            IN numeric       -- aka file control number
    )
    LANGUAGE plpgsql AS
    $$
    DECLARE
        v_payment_initiation_date timestamp;
        v_payment_file_seq varchar(36);
        -- unused
        v_return_cd text; -- return code variable for logging
        v_error_desc varchar(100);-- error desc variable for logging

        v_error_context           text;
        v_error_message           text;
        v_error_sqlState          text;
        v_error_detail            text;
        v_error_hint              text;

        payment_file_cursor CURSOR FOR
        -- find the payment file key to simplify payment detail join
        SELECT DISTINCT parent_file_fk
        FROM psp_eftps_payment_detail
        WHERE return_file_fk = (
            SELECT edi_tax_file_seq
            FROM psp_edi_tax_file ef
            WHERE ef.file_id = p_return_file_id
            );
    BEGIN
        FOR payment_detail_rec IN payment_file_cursor
            LOOP
            v_payment_file_seq := payment_detail_rec.parent_file_fk;

            BEGIN
                -- find the initiation date to force all MMT searches onto correct partition
                SELECT initiation_date
                INTO v_payment_initiation_date
                FROM psp_eftps_payment_detail epd
                    JOIN psp_money_movement_transaction mmt ON mmt.money_movement_transaction_seq = epd.money_movement_transaction_fk
                    AND mmt.company_fk = epd.company_fk
                WHERE
                      epd.parent_file_fk = v_payment_file_seq limit 1;
                EXCEPTION
                WHEN no_data_found  THEN -- as400 files
                    RAISE NOTICE 'P0000: no data found when looking up initiation date for payment file: % --', v_payment_file_seq;
                    GET STACKED DIAGNOSTICS
                        v_error_context = PG_EXCEPTION_CONTEXT,
                        v_error_sqlState = RETURNED_SQLSTATE,
                        v_error_message = MESSAGE_TEXT,
                        v_error_detail = PG_EXCEPTION_DETAIL,
                        v_error_hint = PG_EXCEPTION_HINT;
                    RAISE NOTICE 'context - %', v_error_context;
                    RAISE NOTICE 'sqlState - %', v_error_sqlState;
                    RAISE NOTICE 'message - %', v_error_message;
                    RAISE NOTICE 'detail - %', v_error_detail;
                    RAISE NOTICE 'hint - %', v_error_hint;
                  RAISE;
              WHEN OTHERS THEN
                  RAISE NOTICE 'P0000: unexpected error looking up initiation date for payment file: % --', v_payment_file_seq;
                  GET STACKED DIAGNOSTICS
                      v_error_context = PG_EXCEPTION_CONTEXT,
                      v_error_sqlState = RETURNED_SQLSTATE,
                      v_error_message = MESSAGE_TEXT,
                      v_error_detail = PG_EXCEPTION_DETAIL,
                      v_error_hint = PG_EXCEPTION_HINT;
                  RAISE NOTICE 'context - %', v_error_context;
                  RAISE NOTICE 'sqlState - %', v_error_sqlState;
                  RAISE NOTICE 'message - %', v_error_message;
                  RAISE NOTICE 'detail - %', v_error_detail;
                  RAISE NOTICE 'hint - %', v_error_hint;
                  END;
            CALL PRC_EFTPS_PAYMENTS_MMT_STATUS('PRC_EFTPS_PAYMENTS_RETURN', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date);

            CALL PRC_SET_PSP_EVENT_LOG (
                v_RETURN_CD,
                v_ERROR_DESC,
                'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                null,                           -- p_TypeCd             IN   VARCHAR2,
                'PROD',                          -- p_DomainName         IN   VARCHAR2,
                'PSP',                           --p_ArchName            IN   VARCHAR2,
                'PRC_EFTPS_PAYMENTS_RETURN',     -- p_CompName           IN   VARCHAR2,
                'N/A',                           -- p_HostName           IN   VARCHAR2,
                'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                'PSP_FINANCIAL_TRANS_STATE',     --  p_ObjectName        IN   VARCHAR2,
                'N/A',                           --p_UserName            IN   VARCHAR2,
                to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                'Insert FINANCIAL_TRANS_STATE'
                );

            -- update FTs
            INSERT /*+ APPEND */ INTO PSP_FINANCIAL_TRANS_STATE
                (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
                 CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
                 REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
                 GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
                 TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
                 (SELECT
                         gen_random_uuid(),
                         0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
                         ((p_app_server_date AT TIME ZONE 'GMT') AT TIME ZONE 'PDT'), NULL, NULL, ft.financial_transaction_seq, 'Returned', NULL, ft.company_fk, ft.transaction_type_fk
                 FROM
                      psp_eftps_payment_detail efpd
                          JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                          AND mmt.company_fk = efpd.company_fk
                          JOIN psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                          AND mmt.company_fk = ft.company_fk
                 WHERE
                       efpd.parent_file_fk = v_payment_file_seq
                   AND efpd.status_cd in ('ReturnedTaxNotPaid')
                   AND mmt.money_movement_payment_method in ('EFTPS', 'EFTPSDirectDebit')
                   AND mmt.status = 'Executed'
                   AND mmt.tax_payment_status in ('ReturnedTaxNotPaid')
                   AND mmt.initiation_date = v_payment_initiation_date
                   AND ft.current_transaction_state_fk in ('Executed','Completed')
                   AND date_trunc('day',ft.settlement_date) >= date_trunc('day',mmt.initiation_date)
                   AND ft.settlement_date >= mmt.initiation_date
                     );

            CALL PRC_SET_PSP_EVENT_LOG (
                v_RETURN_CD,
                v_ERROR_DESC,
                'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                null,                           -- p_TypeCd             IN   VARCHAR2,
                'PROD',                          -- p_DomainName         IN   VARCHAR2,
                'PSP',                           --p_ArchName            IN   VARCHAR2,
                'PRC_EFTPS_PAYMENTS_RETURN',     -- p_CompName           IN   VARCHAR2,
                'N/A',                           -- p_HostName           IN   VARCHAR2,
                'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
                'PSP_FINANCIAL_TRANSACTION',     --  p_ObjectName        IN   VARCHAR2,
                'N/A',                           --p_UserName            IN   VARCHAR2,
                to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
                'Updating FINANCIAL_TRANSACTION'
                );

            UPDATE psp_financial_transaction
            SET current_transaction_state_fk = 'Returned', modifier_id = p_user_id, modified_date = p_app_server_date, version = version + 1
            WHERE
              date_trunc('day',settlement_date) >= date_trunc('day',v_payment_initiation_date)
              AND settlement_date >= date_trunc('day',v_payment_initiation_date)
              AND current_transaction_state_fk in ('Executed','Completed')
              AND (money_movement_transaction_fk, company_fk) in (
                  SELECT
                         money_movement_transaction_fk, efpd.company_fk
                  FROM
                       psp_eftps_payment_detail efpd
                           JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                           AND mmt.company_fk = efpd.company_fk
                  WHERE
                        efpd.parent_file_fk = v_payment_file_seq
                    AND efpd.status_cd in ('ReturnedTaxNotPaid')
                    AND mmt.money_movement_payment_method in ('EFTPS', 'EFTPSDirectDebit')
                    AND mmt.status = 'Executed'
                    AND mmt.tax_payment_status in ('ReturnedTaxNotPaid')
                    AND mmt.initiation_date = v_payment_initiation_date
                  );

            CALL PRC_EFTPS_PAYMENTS_EVENTS('PRC_EFTPS_PAYMENTS_RETURN', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, 'ReturnedTaxPaid', 'ReturnedTaxNotPaid');

            END LOOP;
    END;
    $$
