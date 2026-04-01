set search_path=pspadm;
\timing
call PRC_RUN_LEDGER_BALANCE(date '2023-05-01', date '2023-05-08');

