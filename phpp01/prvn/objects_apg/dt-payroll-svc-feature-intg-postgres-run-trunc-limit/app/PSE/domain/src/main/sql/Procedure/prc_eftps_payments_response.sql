CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_RESPONSE
(
	p_user_id           		IN VARCHAR2,	-- for audit purposes
    p_app_server_date			IN TIMESTAMP, 	-- UTC Date
    p_response_file_id	  		IN NUMBER,      -- aka file control number
    p_complete_fin_txns         IN NUMBER := 0
)
IS
	v_payment_initiation_date TIMESTAMP;
	v_payment_file_seq VARCHAR2(36);

  -- unused
  v_return_cd number; -- return code variable for logging
  v_error_desc varchar2(100);-- error desc variable for logging
BEGIN
  -- find the payment file key to simplify payment detail join
  -- find the initiation date to force all MMT searches onto correct partition
  BEGIN
    SELECT
      mmt.initiation_date, ef_payment.eftps_file_seq
    INTO
      v_payment_initiation_date, v_payment_file_seq
    FROM
      psp_eftps_file ef_response
      JOIN psp_eftps_payment_detail efpd on efpd.response_file_fk = ef_response.eftps_file_seq
      JOIN psp_eftps_file ef_payment on ef_payment.eftps_file_seq = efpd.parent_file_fk
      JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
          AND mmt.company_fk = efpd.company_fk
      JOIN PSP_EDI_TAX_FILE etf on ETF.EDI_TAX_FILE_SEQ = ef_response.EFTPS_FILE_SEQ
    WHERE
      etf.file_id = p_response_file_id
      AND rownum = 1;
    EXCEPTION
      WHEN TOO_MANY_ROWS
        THEN RAISE_APPLICATION_ERROR(-20052, 'ERROR: MULTIPLE RESPONSE FILES FOUND FOR FILE_ID: ' || p_response_file_id);
      WHEN NO_DATA_FOUND -- AS400 files
         THEN RETURN;
      WHEN OTHERS THEN
        -- Output desired error message
        dbms_output.put_line('-20999: unexpected error -- error stack follows');
        -- Output actual line number of error source
        dbms_output.put(dbms_utility.format_error_backtrace);
        -- Output the actual error number and message
        dbms_output.put_line(dbms_utility.format_error_stack);

  END;

  PRC_EFTPS_PAYMENTS_MMT_STATUS('PRC_EFTPS_PAYMENTS_RESPONSE', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date);

  PRC_SET_PSP_EVENT_LOG (
               v_RETURN_CD,
               v_ERROR_DESC,
               'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                null,                           -- p_TypeCd             IN   VARCHAR2,
               'PROD',                          -- p_DomainName         IN   VARCHAR2,
               'PSP',                           -- p_ArchName           IN   VARCHAR2,
               'PRC_EFTPS_PAYMENTS_RESPONSE',   -- p_CompName           IN   VARCHAR2,
               'N/A',                           -- p_HostName           IN   VARCHAR2,
               'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
               'PSP_FINANCIAL_TRANS_STATE',     -- p_ObjectName         IN   VARCHAR2,
               'N/A',                           -- p_UserName           IN   VARCHAR2,
               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
              'Inserting FINANCIAL_TRANS_STATE');

  -- update FTS
  INSERT INTO PSP_FINANCIAL_TRANS_STATE
   (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
     CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
     REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
     GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
     TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
   (SELECT
      FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
      new_time(p_app_server_date,'GMT','PDT'), NULL, NULL, ft.financial_transaction_seq, DECODE(efpd.status_cd, 'AcknowledgedByAgency', 'Completed', 'RejectedByAgency', 'Returned'), NULL, ft.company_fk, ft.transaction_type_fk
   FROM
      psp_eftps_payment_detail efpd
  	  JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
          AND mmt.company_fk = efpd.company_fk
      JOIN psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
          AND ft.company_fk = mmt.company_fk
   WHERE
      efpd.parent_file_fk = v_payment_file_seq
      AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
      AND mmt.status = 'Executed'
      AND (
        (mmt.money_movement_payment_method = 'EFTPS' AND mmt.tax_payment_status in (DECODE(p_complete_fin_txns, 1, 'AcknowledgedByAgency', '<same-day>'), 'RejectedByAgency'))
        OR (mmt.money_movement_payment_method = 'EFTPSDirectDebit' AND mmt.tax_payment_status = 'RejectedByAgency')
      )
      AND mmt.initiation_date = v_payment_initiation_date
      AND ft.current_transaction_state_fk = 'Executed'
      AND trunc(ft.settlement_date) >= trunc(mmt.initiation_date)
      AND ft.settlement_date >= mmt.initiation_date
   );

  PRC_SET_PSP_EVENT_LOG (
               v_RETURN_CD,
               v_ERROR_DESC,
               'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                null,                           -- p_TypeCd             IN   VARCHAR2,
               'PROD',                          -- p_DomainName         IN   VARCHAR2,
               'PSP',                           -- p_ArchName           IN   VARCHAR2,
               'PRC_EFTPS_PAYMENTS_RESPONSE',   -- p_CompName           IN   VARCHAR2,
               'N/A',                           -- p_HostName           IN   VARCHAR2,
               'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
               'PSP_FINANCIAL_TRANSACTION',     -- p_ObjectName         IN   VARCHAR2,
               'N/A',                           -- p_UserName           IN   VARCHAR2,
               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
              'Updating FINANCIAL_TRANSACTION');

  UPDATE psp_financial_transaction ft
  SET (current_transaction_state_fk, modifier_id, modified_date) = (
    SELECT
      DECODE(efpd.status_cd, 'AcknowledgedByAgency', 'Completed', 'RejectedByAgency', 'Returned'), p_user_id, p_app_server_date
    FROM
      psp_eftps_payment_detail efpd
  	  JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
          AND mmt.company_fk = efpd.company_fk
    WHERE
      efpd.parent_file_fk = v_payment_file_seq
      AND ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
      AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
      AND mmt.money_movement_payment_method in ('EFTPS','EFTPSDirectDebit')
      AND mmt.status = 'Executed'
      AND mmt.tax_payment_status in ('AcknowledgedByAgency', 'RejectedByAgency')
      AND mmt.initiation_date = v_payment_initiation_date
    ), ft.version = ft.version + 1
  WHERE
    trunc(settlement_date) >= trunc(v_payment_initiation_date)
    AND settlement_date >= trunc(v_payment_initiation_date)
    AND current_transaction_state_fk = 'Executed'
    AND (money_movement_transaction_fk, ft.company_fk) in (
      SELECT
        money_movement_transaction_fk, efpd.company_fk
      FROM
        psp_eftps_payment_detail efpd
        JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
            AND mmt.company_fk = efpd.company_fk
      WHERE
        efpd.parent_file_fk = v_payment_file_seq
        AND efpd.status_cd in ('AcknowledgedByAgency', 'RejectedByAgency')
        AND mmt.status = 'Executed'
        AND (
          (mmt.tax_payment_status in (DECODE(p_complete_fin_txns, 1, 'AcknowledgedByAgency', '<same-day>'), 'RejectedByAgency') AND mmt.money_movement_payment_method = 'EFTPS')
          OR (efpd.status_cd = 'RejectedByAgency' AND mmt.money_movement_payment_method = 'EFTPSDirectDebit')
        )
        AND mmt.initiation_date = v_payment_initiation_date
    );

  -- company events
  PRC_EFTPS_PAYMENTS_EVENTS('PRC_EFTPS_PAYMENTS_RESPONSE', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, 'AcknowledgedByAgency','RejectedByAgency');

END PRC_EFTPS_PAYMENTS_RESPONSE;
/
