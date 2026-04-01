\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p8;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p9;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p10;
vacuum (full, analyze, verbose) pspadm.psp_financial_trans_state_p11;

