CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_EVENTS
   (
    p_calling_procedure         IN VARCHAR2,
	p_user_id           		IN VARCHAR2,  -- for audit purposes
    p_app_server_date			IN TIMESTAMP, -- UTC Date
    p_payment_file_seq	  		IN VARCHAR2,  -- primary key of payment file
    p_payment_initiation_date   IN TIMESTAMP,
    p_tax_payment_status_1      IN VARCHAR2,
    p_tax_payment_status_2      IN VARChAR2 := NULL
   )
IS
  v_sql_stmt VARCHAR2(32767);

  -- unused
  v_return_cd number; -- return code variable for logging
  v_error_desc varchar2(100);-- error desc variable for logging
BEGIN
  dbms_output.put_line('company event started - ' || to_char(systimestamp, 'hh24:mi:ss'));

  PRC_SET_PSP_EVENT_LOG (
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
               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
              'Inserting COMPANY_EVENT');

  INSERT INTO psp_company_event
          (COMPANY_EVENT_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, EVENT_TIME_STAMP, STATUS_EFFECTIVE_DATE,
           STATUS_CD, EVENT_TYPE_CD, EVENT_TOKEN, SOURCE_ID, NOTE_LAST_UPDATED_DATE,
           COMPANY_FK)
    SELECT
        FN_FORMAT_SYSGUID(sys_guid()), 0, p_user_id, p_app_server_date, p_user_id, p_app_server_date, -1, p_app_server_date, p_app_server_date,
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

  PRC_SET_PSP_EVENT_LOG (
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
               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
              'Inserting COMPANY_EVENT_DETAIL');

  v_sql_stmt:='INSERT ALL
    INTO psp_company_event_detail (COMPANY_EVENT_DETAIL_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID,
         VALUE, EVENT_DETAIL_TYPE_CD, EVENT_DETAIL_SUBTYPE, COMPANY_EVENT_FK, COMPANY_FK)
    VALUES (FN_FORMAT_SYSGUID(sys_guid()), 0, :b1, :b2, :b3, :b4, -1,
        money_movement_transaction_seq, ''MoneyMovementTransactionId'', null, company_event_seq, company_fk)
    INTO psp_company_event_detail (COMPANY_EVENT_DETAIL_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID,
         VALUE, EVENT_DETAIL_TYPE_CD, EVENT_DETAIL_SUBTYPE, COMPANY_EVENT_FK, COMPANY_FK)
    VALUES (FN_FORMAT_SYSGUID(sys_guid()), 0, :b5, :b6, :b7, :b8, -1,
        ''The tax payment status for '' || payment_template_fk || '' due on '' || to_char(payment_due_date, ''MM/DD/YYYY'') || '' via '' || money_movement_payment_method || '' has changed to '' || status_cd, ''GenericEventDetail'', null, company_event_seq, company_fk)
    SELECT
        mmt.money_movement_transaction_seq, mmt.payment_template_fk, mmt.money_movement_payment_method, efpd.status_cd, efpd.payment_due_date, ce.company_event_seq, mmt.company_fk
    FROM
        psp_eftps_payment_detail efpd
        JOIN psp_money_movement_transaction mmt on mmt.money_movement_transaction_seq = efpd.money_movement_transaction_fk and mmt.company_fk = efpd.company_fk
        JOIN psp_company_event ce on ce.company_fk = mmt.company_fk
    WHERE
        efpd.parent_file_fk = :b9
        AND mmt.initiation_date = :b10
        AND mmt.status = ''Executed''
        AND ce.event_type_cd = ''TaxPaymentStatusChanged''
        AND ce.source_id = mmt.money_movement_transaction_seq
        AND ce.event_time_stamp >= :b11
        AND ce.created_date = :b12';

  execute immediate v_sql_stmt
  using p_user_id, p_app_server_date, p_user_id, p_app_server_date,
        p_user_id, p_app_server_date, p_user_id, p_app_server_date,
        p_payment_file_seq,
        p_payment_initiation_date,
        p_app_server_date, p_app_server_date;

    dbms_output.put_line('company event inserts finished ' || to_char(systimestamp, 'hh24:mi:ss'));
END PRC_EFTPS_PAYMENTS_EVENTS;
/
