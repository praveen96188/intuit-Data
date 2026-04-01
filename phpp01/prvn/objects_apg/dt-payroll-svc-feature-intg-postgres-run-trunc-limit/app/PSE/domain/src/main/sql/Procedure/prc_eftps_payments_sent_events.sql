CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_SENT_EVENTS
   (
	p_user_id           		IN VARCHAR2,  -- for audit purposes
    p_app_server_date			IN TIMESTAMP, -- UTC Date
    p_payment_file_id	  		IN NUMBER,    -- aka file control number
    p_tax_payment_status        IN VARCHAR2
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

  PRC_EFTPS_PAYMENTS_EVENTS('PRC_EFTPS_PAYMENTS_SENT', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, p_tax_payment_status);

END PRC_EFTPS_PAYMENTS_SENT_EVENTS;
/
