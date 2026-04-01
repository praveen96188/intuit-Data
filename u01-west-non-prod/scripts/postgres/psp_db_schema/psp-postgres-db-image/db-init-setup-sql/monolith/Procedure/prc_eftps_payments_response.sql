CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_RESPONSE
    (
    IN p_user_id                varchar, -- for audit purposes
    IN p_app_server_date        timestamp, -- UTC Date
    IN p_response_file_id       numeric, -- aka file control number
    IN p_complete_fin_txns      numeric = 0
    )
    LANGUAGE plpgsql AS
    $$
    DECLARE
        v_payment_initiation_date timestamp;
        v_payment_file_seq        varchar(36);
        v_error_context           text;
        v_error_message           text;
        v_error_sqlState          text;
        v_error_detail            text;
        v_error_hint              text;
        -- unused
        v_return_cd               text; -- return code variable for logging
        v_error_desc              varchar(100);-- error desc variable for logging
    BEGIN
        -- find the payment file key to simplify payment detail join
        -- find the initiation date to force all MMT searches onto correct partition
        BEGIN
            SELECT mmt.initiation_date,
               ef_payment.eftps_file_seq
            INTO
               v_payment_initiation_date, v_payment_file_seq
            FROM psp_eftps_file ef_response
                 JOIN psp_eftps_payment_detail efpd on efpd.response_file_fk = ef_response.eftps_file_seq
                 JOIN psp_eftps_file ef_payment on ef_payment.eftps_file_seq = efpd.parent_file_fk
                 JOIN psp_money_movement_transaction mmt
                      on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                          AND mmt.company_fk = efpd.company_fk
                 JOIN PSP_EDI_TAX_FILE etf on ETF.EDI_TAX_FILE_SEQ = ef_response.EFTPS_FILE_SEQ
            WHERE etf.file_id = p_response_file_id
            limit 1;

            EXCEPTION
            WHEN TOO_MANY_ROWS
                THEN RAISE EXCEPTION 'ERROR: MULTIPLE RESPONSE FILES FOUND FOR FILE_ID: %', p_response_file_id
                    USING ERRCODE = 'P0003';
                WHEN NO_DATA_FOUND -- AS400 files
                THEN RETURN;
                WHEN OTHERS THEN
                    -- Output desired error message
                    RAISE NOTICE 'P0000: unexpected error -- error stack follows';
                    -- Output actual line number of error source
                    GET STACKED DIAGNOSTICS
                        v_error_context = PG_EXCEPTION_CONTEXT,
                        v_error_sqlState = RETURNED_SQLSTATE,
                        v_error_message = MESSAGE_TEXT,
                        v_error_detail = PG_EXCEPTION_DETAIL,
                        v_error_hint = PG_EXCEPTION_HINT;
                    RAISE NOTICE 'PRC_EFTPS_PAYMENTS_RESPONSE ERROR context=% sqlState=% message=% detail=% hint=%', v_error_context, v_error_sqlState, v_error_message, v_error_detail, v_error_hint;
                    END;
        CALL PRC_EFTPS_PAYMENTS_MMT_STATUS('PRC_EFTPS_PAYMENTS_RESPONSE', p_user_id, p_app_server_date, v_payment_file_seq,
                                       v_payment_initiation_date);
        CALL PRC_SET_PSP_EVENT_LOG(
            v_RETURN_CD,
            v_ERROR_DESC,
            'N/A', -- p_CompanyId          IN   VARCHAR2,
            null, -- p_TypeCd             IN   VARCHAR2,
            'PROD', -- p_DomainName         IN   VARCHAR2,
            'PSP', -- p_ArchName           IN   VARCHAR2,
            'PRC_EFTPS_PAYMENTS_RESPONSE', -- p_CompName           IN   VARCHAR2,
            'N/A', -- p_HostName           IN   VARCHAR2,
            'EftpsPayment', -- Application_name     IN   VARCHAR2,
            'PSP_FINANCIAL_TRANS_STATE', -- p_ObjectName         IN   VARCHAR2,
            'N/A', -- p_UserName           IN   VARCHAR2,
            to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
            'Inserting FINANCIAL_TRANS_STATE');

        -- update FTS
        INSERT INTO PSP_FINANCIAL_TRANS_STATE
            (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
             CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
             REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
             GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
             TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
             (SELECT gen_random_uuid(), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
                     ((p_app_server_date AT TIME ZONE 'GMT') AT TIME ZONE 'PDT'),
                     NULL, NULL, ft.financial_transaction_seq,
                     case when efpd.status_cd = 'AcknowledgedByAgency' then 'Completed'
                          when efpd.status_cd = 'RejectedByAgency' then 'Returned' end,
                     NULL, ft.company_fk, ft.transaction_type_fk
             FROM psp_eftps_payment_detail efpd
                 JOIN psp_money_movement_transaction mmt
                     on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                         AND mmt.company_fk = efpd.company_fk
                 JOIN psp_financial_transaction ft
                     on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
                         AND ft.company_fk = mmt.company_fk
              WHERE efpd.parent_file_fk = v_payment_file_seq
               AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
               AND mmt.status = 'Executed'
               AND (
                   (mmt.money_movement_payment_method = 'EFTPS' AND mmt.tax_payment_status in
                                                                    (case when p_complete_fin_txns = 1 then 'AcknowledgedByAgency' else '<same-day>' end,
                                                                     'RejectedByAgency'))
                       OR (mmt.money_movement_payment_method = 'EFTPSDirectDebit' AND
                           mmt.tax_payment_status = 'RejectedByAgency')
                   )
               AND mmt.initiation_date = v_payment_initiation_date
               AND ft.current_transaction_state_fk = 'Executed'
               AND date_trunc('day',ft.settlement_date) >= date_trunc('day',mmt.initiation_date)
               AND ft.settlement_date >= mmt.initiation_date
                 );
        CALL PRC_SET_PSP_EVENT_LOG(
            v_RETURN_CD,
            v_ERROR_DESC,
            'N/A', -- p_CompanyId          IN   VARCHAR2,
            null, -- p_TypeCd             IN   VARCHAR2,
            'PROD', -- p_DomainName         IN   VARCHAR2,
            'PSP', -- p_ArchName           IN   VARCHAR2,
            'PRC_EFTPS_PAYMENTS_RESPONSE', -- p_CompName           IN   VARCHAR2,
            'N/A', -- p_HostName           IN   VARCHAR2,
            'EftpsPayment', -- Application_name     IN   VARCHAR2,
            'PSP_FINANCIAL_TRANSACTION', -- p_ObjectName         IN   VARCHAR2,
            'N/A', -- p_UserName           IN   VARCHAR2,
            to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
            'Updating FINANCIAL_TRANSACTION');

        UPDATE psp_financial_transaction ft
        SET (current_transaction_state_fk, modifier_id, modified_date) = (
        SELECT (case when efpd.status_cd = 'AcknowledgedByAgency' then 'Completed'
            when efpd.status_cd = 'RejectedByAgency' then 'Returned' end),
               p_user_id,
               p_app_server_date
        FROM psp_eftps_payment_detail efpd
            JOIN psp_money_movement_transaction mmt
                on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                    AND mmt.company_fk = efpd.company_fk
        WHERE efpd.parent_file_fk = v_payment_file_seq
          AND ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
          AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
          AND mmt.money_movement_payment_method in ('EFTPS', 'EFTPSDirectDebit')
          AND mmt.status = 'Executed'
          AND mmt.tax_payment_status in ('AcknowledgedByAgency', 'RejectedByAgency')
          AND mmt.initiation_date = v_payment_initiation_date
            ),
            version = version + 1
        WHERE date_trunc('day',settlement_date) >= date_trunc('day',v_payment_initiation_date)
          AND settlement_date >= date_trunc('day',v_payment_initiation_date)
          AND current_transaction_state_fk = 'Executed'
          AND (money_movement_transaction_fk, ft.company_fk) in (
              SELECT money_movement_transaction_fk, efpd.company_fk
              FROM psp_eftps_payment_detail efpd
                  JOIN psp_money_movement_transaction mmt
                      on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
                          AND mmt.company_fk = efpd.company_fk
              WHERE efpd.parent_file_fk = v_payment_file_seq
                AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
                AND mmt.status = 'Executed'
                AND (
                    (mmt.tax_payment_status in
                     (case when p_complete_fin_txns = 1 then 'AcknowledgedByAgency' else '<same-day>' end, 'RejectedByAgency') AND
                     mmt.money_movement_payment_method = 'EFTPS')
                        OR (efpd.status_cd = 'RejectedByAgency' AND mmt.money_movement_payment_method = 'EFTPSDirectDebit')
                    )
                AND mmt.initiation_date = v_payment_initiation_date
              );

        -- company events
        CALL PRC_EFTPS_PAYMENTS_EVENTS('PRC_EFTPS_PAYMENTS_RESPONSE', p_user_id, p_app_server_date, v_payment_file_seq,
                                   v_payment_initiation_date, 'AcknowledgedByAgency', 'RejectedByAgency');
    END;
    $$
