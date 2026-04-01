\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p0;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p1;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p2;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p3;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p4;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p5;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p6;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction_p7;
vacuum (full, analyze, verbose) pspadm.psp_money_movement_transaction;

