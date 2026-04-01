set search_path=pspadm;
\timing
call PRC_DELETE_LEDGER_BALANCE_DATA(date '2023-04-03', date '2023-04-10');
