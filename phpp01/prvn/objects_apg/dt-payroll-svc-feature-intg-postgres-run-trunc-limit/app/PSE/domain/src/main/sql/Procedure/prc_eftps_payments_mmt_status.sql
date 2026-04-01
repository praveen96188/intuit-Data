CREATE OR REPLACE PROCEDURE PRC_EFTPS_PAYMENTS_MMT_STATUS
   (
    p_calling_procedure         IN VARCHAR2,
	p_user_id           		IN VARCHAR2,  -- for audit purposes
    p_app_server_date			IN TIMESTAMP, -- UTC Date
    p_payment_file_seq	  		IN VARCHAR2,  -- primary key of payment file
    p_payment_initiation_date   IN TIMESTAMP
   )
IS
  -- unused
  v_return_cd number; -- return code variable for logging
  v_error_desc varchar2(100);-- error desc variable for logging
BEGIN
  dbms_output.put_line('update mmt status started  - ' || to_char(systimestamp, 'hh24:mi:ss'));

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
               'PSP_MONEY_MOVEMENT_TRANSACTION',--  p_ObjectName        IN   VARCHAR2,
               'N/A',                           --p_UserName            IN   VARCHAR2,
               to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
              'Updating MONEY_MOVEMENT_TRANSACTION');

  -- For each MMT we are going to update the tax_payment_status on below,
  -- recalculate the ATF payments passing in the current and new payment status.
  prc_recalc_atf_payments_eftps(p_calling_procedure, p_user_id, p_app_server_date, p_payment_file_seq, p_payment_initiation_date);

  -- update MMT TaxPaymentStatus to mirror EftpsPaymentDetail status updated by batch job, set status effective date
  UPDATE psp_money_movement_transaction mmt
  SET (mmt.modifier_id, mmt.modified_date, mmt.tax_payment_status, mmt.tax_pmtstatus_effectivedate) = (
    SELECT
      p_user_id, p_app_server_date, efpd.status_cd, p_app_server_date
  	FROM
      psp_eftps_payment_detail efpd
  	WHERE
      efpd.parent_file_fk = p_payment_file_seq
      AND efpd.money_movement_transaction_fk = mmt.money_movement_transaction_seq
      AND efpd.company_fk = mmt.company_fk
      AND mmt.initiation_date = p_payment_initiation_date
   ), mmt.version = mmt.version + 1
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

  dbms_output.put_line('update mmt status finished  - ' || to_char(systimestamp, 'hh24:mi:ss'));

END PRC_EFTPS_PAYMENTS_MMT_STATUS;
/