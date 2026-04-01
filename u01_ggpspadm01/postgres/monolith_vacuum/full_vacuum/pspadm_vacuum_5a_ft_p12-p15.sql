\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p12;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p13;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p14;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p15;

