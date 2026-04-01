CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_SENT_EVENTS
   (
	p_user_id           		IN varchar,  -- for audit purposes
    p_app_server_date			IN timestamp, -- UTC Date
    p_payment_file_id	  		IN numeric,    -- aka file control number
    p_tax_payment_status        IN varchar
   )
    LANGUAGE plpgsql AS
    $$
    DECLARE
        v_payment_file_seq varchar(36);
        v_payment_initiation_date timestamp;

        -- unused
        v_return_cd numeric; -- return code variable for logging
        v_error_desc varchar(100);-- error desc variable for logging

        v_error_context           text;
        v_error_message           text;
        v_error_sqlState          text;
        v_error_detail            text;
        v_error_hint              text;
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
                    limit 1;
        EXCEPTION
            WHEN TOO_MANY_ROWS
                THEN RAISE EXCEPTION 'ERROR: MULTIPLE FILES FOUND FOR FILE_ID: %', p_payment_file_id
                    USING ERRCODE = 'P0003';
            WHEN NO_DATA_FOUND -- AS400 files
                THEN RETURN;
            WHEN OTHERS THEN
                RAISE NOTICE 'P0000: unexpected error -- error stack follows';
                GET STACKED DIAGNOSTICS
                    v_error_context = PG_EXCEPTION_CONTEXT,
                    v_error_sqlState = RETURNED_SQLSTATE,
                    v_error_message = MESSAGE_TEXT,
                    v_error_detail = PG_EXCEPTION_DETAIL,
                    v_error_hint = PG_EXCEPTION_HINT;
                RAISE NOTICE 'PRC_EFTPS_PAYMENTS_SENT_EVENTS ERROR context=% sqlState=% message=% detail=% hint=%', v_error_context, v_error_sqlState, v_error_message, v_error_detail, v_error_hint;
        END;

        CALL PRC_EFTPS_PAYMENTS_EVENTS('PRC_EFTPS_PAYMENTS_SENT', p_user_id, p_app_server_date, v_payment_file_seq, v_payment_initiation_date, p_tax_payment_status);
    END;
    $$