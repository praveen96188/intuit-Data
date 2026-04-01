set pages 0 lines 300 echo on timing on echo on feedback on trimspool on
spool tab_rowcnt
select /*+ parallel(8) */count(*) from pspadm.PSP_COMPANY_EVENT;
select /*+ parallel(8) */count(*) from pspadm.PSP_TAX;
select /*+ parallel(8) */count(*) from pspadm.PSP_DISBURSE_ADVICE_TAX_LIAB;
select /*+ parallel(8) */count(*) from pspadm.PSP_PAYCHECK_USAGE;
select /*+ parallel(8) */count(*) from pspadm.PSP_PSTUB_PAY_ITEM;
select /*+ parallel(8) */count(*) from pspadm.PSP_QBDT_PAYLINE_INFO;
select /*+ parallel(8) */count(*) from pspadm.PSP_COMPANY_EVENT_DETAIL;
select /*+ parallel(8) */count(*) from pspadm.PSP_COMPENSATION;
select /*+ parallel(8) */count(*) from pspadm.PSP_COMPANY_EVENT_EMAIL_PARAM;
select /*+ parallel(8) */count(*) from pspadm.PSP_QBDT_PAYCHECK_INFO;
select /*+ parallel(8) */count(*) from pspadm.PSP_ENTITY_UPDATE;
select /*+ parallel(8) */count(*) from pspadm.PSP_ENTITLEMENT_MESSAGE;
select /*+ parallel(8) */count(*) from pspadm.PSP_PROPERTY_AUDIT;

spool off
