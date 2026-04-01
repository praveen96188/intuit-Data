\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p0;

