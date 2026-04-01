CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_EVENTS
(
  IN p_calling_procedure          varchar,
  IN p_user_id                    varchar,  -- for audit purposes
  IN p_app_server_date            timestamp, -- UTC Date
  IN p_payment_file_seq           varchar,  -- primary key of payment file
  IN p_payment_initiation_date    timestamp,
  IN p_tax_payment_status_1       varchar,
  IN p_tax_payment_status_2       varchar = NULL
)
  LANGUAGE plpgsql AS
$$
DECLARE
  v_sql_stmt varchar(32767);
  v_return_cd text; -- return code variable for logging
  v_error_desc varchar(100);-- error desc variable for logging
BEGIN
  RAISE NOTICE 'company event started - %', to_char(clock_timestamp(), 'hh24:mi:ss');

  CALL PRC_SET_PSP_EVENT_LOG (
      v_RETURN_CD,
      v_ERROR_DESC,
      'N/A',                           -- p_CompanyId          IN   VARCHAR2,
      null,                           -- p_TypeCd             IN   VARCHAR2,
      'PROD',                          -- p_DomainName         IN   VARCHAR2,
      'PSP',                           --p_ArchName           IN   VARCHAR2,
      p_calling_procedure,             -- p_CompName           IN   VARCHAR2,
      'N/A',                           -- p_HostName           IN   VARCHAR2,
      'EftpsPayment',                  -- Application_name           IN   VARCHAR2,
      'PSP_COMPANY_EVENT',             --  p_ObjectName         IN   VARCHAR2,
      'N/A',                           --p_UserName           IN   VARCHAR2,
      to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
      'Inserting COMPANY_EVENT');

  INSERT INTO psp_company_event
  (COMPANY_EVENT_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, EVENT_TIME_STAMP, STATUS_EFFECTIVE_DATE,
   STATUS_CD, EVENT_TYPE_CD, EVENT_TOKEN, SOURCE_ID, NOTE_LAST_UPDATED_DATE,
   COMPANY_FK)
  SELECT
    gen_random_uuid(),
    0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1, p_app_server_date, p_app_server_date,
    'Active', 'TaxPaymentStatusChanged', 0, mmt.money_movement_transaction_seq, null,
    mmt.company_fk
  FROM
    psp_eftps_payment_detail efpd
      JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
        AND mmt.company_fk = efpd.company_fk
  WHERE
      efpd.parent_file_fk = p_payment_file_seq
    AND mmt.initiation_date = p_payment_initiation_date
    AND mmt.status = 'Executed'
    AND mmt.tax_payment_status in (p_tax_payment_status_1, p_tax_payment_status_2);

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
      'PSP_COMPANY_EVENT_DETAIL',      --  p_ObjectName        IN   VARCHAR2,
      'N/A',                           --p_UserName            IN   VARCHAR2,
      to_char(timezone('UTC', cast(FN_GET_PSP_TIMESTAMP() AS timestamptz)),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
      'Inserting COMPANY_EVENT_DETAIL');

    DECLARE

      payments_events_cursor CURSOR FOR SELECT
        mmt.money_movement_transaction_seq, mmt.payment_template_fk, mmt.money_movement_payment_method, efpd.status_cd, efpd.payment_due_date, ce.company_event_seq, mmt.company_fk
        FROM
            psp_eftps_payment_detail efpd
            JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk
            JOIN psp_company_event ce on ce.company_fk = mmt.company_fk
        WHERE
            efpd.parent_file_fk = p_payment_file_seq
            AND mmt.initiation_date = p_payment_initiation_date
            AND mmt.status = 'Executed'
            AND ce.event_type_cd = 'TaxPaymentStatusChanged'
            AND ce.source_id = mmt.money_movement_transaction_seq
            AND ce.event_time_stamp >= p_app_server_date
            AND ce.created_date = p_app_server_date;

      BEGIN

      FOR cursor_rec IN payments_events_cursor
        LOOP

          INSERT INTO psp_company_event_detail (COMPANY_EVENT_DETAIL_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID,
                                   VALUE, EVENT_DETAIL_TYPE_CD, EVENT_DETAIL_SUBTYPE, COMPANY_EVENT_FK, COMPANY_FK)
              VALUES(gen_random_uuid(), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
                     cursor_rec.money_movement_transaction_seq, 'MoneyMovementTransactionId', null, cursor_rec.company_event_seq, cursor_rec.company_fk),
                    (gen_random_uuid(), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1,
                    ('The tax payment status for ' ||  cursor_rec.payment_template_fk || ' due on ' || to_char(cursor_rec.payment_due_date, 'MM/DD/YYYY') || ' via ' || cursor_rec.money_movement_payment_method || ' has changed to ' || cursor_rec.status_cd),
                     'GenericEventDetail', null, cursor_rec.company_event_seq, cursor_rec.company_fk);
        END LOOP;
      END;

  RAISE NOTICE 'company event inserts finished %' , to_char(clock_timestamp(), 'hh24:mi:ss');
END;
$$;