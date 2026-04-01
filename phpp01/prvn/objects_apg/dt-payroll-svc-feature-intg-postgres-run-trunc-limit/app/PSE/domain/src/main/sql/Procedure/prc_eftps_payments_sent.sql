CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_SENT
   (
	p_user_id           		IN VARCHAR2,  -- for audit purposes
    p_app_server_date			IN TIMESTAMP, -- UTC Date
    p_payment_file_id	  		IN NUMBER     -- aka file control number
   )
IS
  v_payment_file_seq VARCHAR2(36);
  v_payment_initiation_date TIMESTAMP;

  -- unused
  v_return_cd number; -- return code variable for logging
  v_error_desc varchar2(100);-- error desc variable for logging
BEGIN
  BEGIN
    SELECT
      eftps_file_seq, payment_initiation_date
    INTO
      v_payment_file_seq, v_payment_initiation_date
    FROM
      psp_eftps_file ef
      JOIN psp_eftps_payment_detail epd on epd.parent_file_fk = ef.eftps_file_seq
      JOIN PSP_EDI_TAX_FILE etf on ETF.EDI_TAX_FILE_SEQ = EF.EFTPS_FILE_SEQ
    WHERE
      file_id = p_payment_file_id
      AND rownum = 1;
    EXCEPTION
      WHEN TOO_MANY_ROWS
        THEN RAISE_APPLICATION_ERROR(-20052, 'ERROR: MULTIPLE FILES FOUND FOR FILE_ID: ' || p_payment_file_id);
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

  PRC_SET_PSP_EVENT_LOG (
               v_RETURN_CD,
               v_ERROR_DESC,
               'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                null,                           -- p_TypeCd             IN   VARCHAR2,
               'PROD',                          -- p_DomainName         IN   VARCHAR2,
               'PSP',                           --p_ArchName            IN   VARCHAR2,
               'PRC_EFTPS_PAYMENTS_SENT',       -- p_CompName           IN   VARCHAR2,
               'N/A',                           -- p_HostName           IN   VARCHAR2,
               'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
               'PSP_FINANCIAL_TRANSACTION',     --  p_ObjectName        IN   VARCHAR2,
               'N/A',                           --p_UserName            IN   VARCHAR2,
               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
              'Updating FINANCIAL_TRANSACTION');


  -- update current financial transaction state for all transactions in eftps
  -- the actual current_transaction_state_fk value (Executed, Returned, etc.) should be matched with what happened in Transaction States above
  UPDATE psp_financial_transaction
  SET current_transaction_state_fk = 'Executed', modifier_id = p_user_id, modified_date = p_app_server_date, VERSION = VERSION + 1
  WHERE
  trunc(settlement_date) >= trunc(p_app_server_date)
  AND settlement_date >= p_app_server_date
  AND current_transaction_state_fk = 'Created'
  AND (money_movement_transaction_fk, company_fk) in (
      SELECT money_movement_transaction_seq, mmt.company_fk
      FROM psp_eftps_file ef
         JOIN psp_eftps_payment_detail efpd on efpd.parent_file_fk = ef.eftps_file_seq
         JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
          AND mmt.company_fk = efpd.company_fk
         JOIN PSP_EDI_TAX_FILE etf on ETF.EDI_TAX_FILE_SEQ = EF.EFTPS_FILE_SEQ
      WHERE etf.file_id = p_payment_file_id
      AND mmt.status = 'Executed'
      AND mmt.tax_payment_status = 'SentToAgency'
      AND mmt.initiation_date = v_payment_initiation_date
  );

  dbms_output.put_line('financial transaction update finished - ' || to_char(systimestamp, 'hh24:mi:ss'));

  PRC_SET_PSP_EVENT_LOG (
               v_RETURN_CD,
               v_ERROR_DESC,
               'N/A',                           -- p_CompanyId          IN   VARCHAR2,
                null,                           -- p_TypeCd             IN   VARCHAR2,
               'PROD',                          -- p_DomainName         IN   VARCHAR2,
               'PSP',                           --p_ArchName            IN   VARCHAR2,
               'PRC_EFTPS_PAYMENTS_SENT',       -- p_CompName           IN   VARCHAR2,
               'N/A',                           -- p_HostName           IN   VARCHAR2,
               'EftpsPayment',                  -- Application_name     IN   VARCHAR2,
               'PSP_FINANCIAL_TRANS_STATE',     --  p_ObjectName        IN   VARCHAR2,
               'N/A',                           --p_UserName            IN   VARCHAR2,
               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
              'Insert FINANCIAL_TRANS_STATE');

  -- create Transaction States for all transactions in eftps file
  INSERT INTO PSP_FINANCIAL_TRANS_STATE
   (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
     CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
     REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
     GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
     TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK) 
     (SELECT FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
             new_time(p_app_server_date,'GMT','PDT'), NULL, NULL, FT.FINANCIAL_TRANSACTION_SEQ, 'Executed', NULL, ft.company_fk, ft.transaction_type_fk
        FROM psp_eftps_file ef
         JOIN psp_eftps_payment_detail efpd on efpd.parent_file_fk = ef.eftps_file_seq
         JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
            AND mmt.company_fk = efpd.company_fk
         JOIN psp_financial_transaction ft on ft.money_movement_transaction_fk = mmt.money_movement_transaction_seq
            AND ft.company_fk = mmt.company_fk
         JOIN PSP_EDI_TAX_FILE etf on ETF.EDI_TAX_FILE_SEQ = EF.EFTPS_FILE_SEQ
        WHERE etf.file_id = p_payment_file_id
        AND mmt.status = 'Executed'
        AND mmt.tax_payment_status = 'SentToAgency'
        AND mmt.initiation_date = v_payment_initiation_date
        AND ft.current_transaction_state_fk = 'Executed'
        AND trunc(ft.settlement_date) >= trunc(mmt.initiation_date)
        AND ft.settlement_date >= mmt.initiation_date);

  dbms_output.put_line('financial transaction state insert finished - ' || to_char(systimestamp, 'hh24:mi:ss'));

  UPDATE PSP_EDI_TAX_FILE
  SET
    STATUS_CD = 'PendingTransmission',
    MODIFIED_DATE = SYS_EXTRACT_UTC(SYSTIMESTAMP),
    MODIFIER_ID = p_user_id,
    VERSION = VERSION + 1
  WHERE SYSTEM_OWNER = 'PSP'
  AND FILE_ID = p_payment_file_id;

END PRC_EFTPS_PAYMENTS_SENT;
/