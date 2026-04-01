\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p0;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p1;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p2;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p3;

