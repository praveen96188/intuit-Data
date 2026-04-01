CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_RETURN
(
    p_user_id                   IN VARCHAR2,    -- for audit purposes
    p_app_server_date           IN TIMESTAMP,   -- UTC Date
    p_return_file_id            IN NUMBER       -- aka file control number
)
IS
    v_payment_initiation_date TIMESTAMP;
    v_payment_file_seq VARCHAR2(36);

    -- unused
    v_return_cd number; -- return code variable for logging
    v_error_desc varchar2(100);-- error desc variable for logging

    CURSOR payment_file_cursor IS
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
              epd.parent_file_fk = v_payment_file_seq
              AND ROWNUM = 1;
          EXCEPTION
              WHEN no_data_found  THEN -- as400 files
                  dbms_output.put_line('-20999: no data found when looking up initiation date for payment file: ' || v_payment_file_seq || ' -- ');
                  dbms_output.put(dbms_utility.format_error_backtrace);
                  dbms_output.put_line(dbms_utility.format_error_stack);
                  RAISE;
              WHEN OTHERS THEN
                  dbms_output.put_line('-20999: unexpected error looking up initiation date for payment file: ' || v_payment_file_seq || ' -- ');
                  dbms_output.put(dbms_utility.format_error_backtrace);
                  dbms_output.put_line(dbms_utility.format_error_stack);
                  RAISE;
        END;


        PRC_EFTPS_PAYMENTS_MMT_STATUS('PRC_EFTPS_PAYMENTS_RETURN', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date);

        PRC_SET_PSP_EVENT_LOG (
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
           to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
           'Insert FINANCIAL_TRANS_STATE');

        -- update FTs
        INSERT INTO PSP_FINANCIAL_TRANS_STATE
         (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
             CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
             REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
             GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
             TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
         (SELECT
                FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
                new_time(p_app_server_date,'GMT','PDT'), NULL, NULL, ft.financial_transaction_seq, 'Returned', NULL, ft.company_fk, ft.transaction_type_fk
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
                AND trunc(ft.settlement_date) >= trunc(mmt.initiation_date)
                AND ft.settlement_date >= mmt.initiation_date
        );

        PRC_SET_PSP_EVENT_LOG (
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
           to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
           'Updating FINANCIAL_TRANSACTION');

        UPDATE psp_financial_transaction
        SET current_transaction_state_fk = 'Returned', modifier_id = p_user_id, modified_date = p_app_server_date, version = version + 1
        WHERE
            trunc(settlement_date) >= trunc(v_payment_initiation_date)
            AND settlement_date >= trunc(v_payment_initiation_date)
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

        PRC_EFTPS_PAYMENTS_EVENTS('PRC_EFTPS_PAYMENTS_RETURN', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, 'ReturnedTaxPaid, ReturnedTaxNotPaid');

    END LOOP;

END PRC_EFTPS_PAYMENTS_RETURN;
/
