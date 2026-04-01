\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p12;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p13;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p14;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p15;

