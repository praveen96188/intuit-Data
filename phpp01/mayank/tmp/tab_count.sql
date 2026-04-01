set timing on echo on feedback on
spool tab_count
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_COMPANY_EVENT;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_TAX;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_DISBURSE_ADVICE_TAX_LIAB;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_PAYCHECK_USAGE;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_PSTUB_PAY_ITEM;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_QBDT_PAYLINE_INFO;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_COMPANY_EVENT_DETAIL;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_COMPENSATION;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_COMPANY_EVENT_EMAIL_PARAM;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_QBDT_PAYCHECK_INFO;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_COMPANY_EVENT;
Select /*+ parallel(a,8) */count(*) from pspadm.PSP_TAX_ACCOUNT_AUDIT;
spool off

