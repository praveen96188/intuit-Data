\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p4;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p5;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p6;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p7;

