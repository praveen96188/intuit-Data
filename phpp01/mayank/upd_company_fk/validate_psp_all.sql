set echo on timing on feedback on
set serveroutput on
spool  validate_psp_all
@Validate_PSP_COMPANY_EVENT_EMAIL.sql
@Validate_PSP_EDI_PAYMENT_DETAIL.sql
@Validate_PSP_EVENT_AS400_SYNC.sql
@Validate_PSP_FSET_FILING_DETAIL.sql
@Validate_PSP_PAYMENT_BATCH_ASSOC.sql
@Validate_PSP_PAYSTUB.sql
@Validate_PSP_TAX_PAYMENT_ON_HOLD_REASON.sql
@Validate_PSP_TP401K_BATCH_PAYCHECK.sql
@Validate_PSP_TP401K_PAYCHECK.sql
@Validate_PSP_VOIDED_CHECK.sql
spool off

