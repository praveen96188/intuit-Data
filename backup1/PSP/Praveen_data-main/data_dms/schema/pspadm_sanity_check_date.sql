set lines 300 echo on timing on echo on feedback on trimspool on
spool psp_sanity_count


--Task 1

select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_LEDGER_BALANCE a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_ENTRY_DETAIL_RECORD a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_FINANCIAL_TRANS_STATE a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_FINANCIAL_TRANSACTION a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PAYCHECK a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PAYCHECK_SPLIT a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');

--Task 2
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_COMPANY_EVENT a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_COMPANY_EVENT_DETAIL a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_COMPANY_EVENT_EMAIL_PARAM a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');

--Task 3

select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_COMPENSATION a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PROPERTY_AUDIT a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_QBDT_PAYCHECK_INFO a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_QBDT_PAYLINE_INFO a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');

--Task 4

select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PSTUB_PAY_ITEM a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');

--Task 5

select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_TAX a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');

--Task 6

select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_ENTITLEMENT_MESSAGE a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PAYCHECK_USAGE a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_DISBURSE_ADVICE_TAX_LIAB a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_QBDT_TRANSACTION_INFO a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PAYSTUB a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_DEDUCTION a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_PSTUB_EMPLOYEE_INFO a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');

--Task 7

select /*+ parallel(a,4) */ count(*) from PSPADM.PSP_ENTITY_UPDATE a WHERE a.created_date between to_date('2023-01-01 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('2023-07-24 00:00:00','yyyy-mm-dd hh24:mi:ss');
