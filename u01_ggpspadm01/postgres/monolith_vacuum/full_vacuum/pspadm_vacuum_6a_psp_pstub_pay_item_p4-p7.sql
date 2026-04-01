\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p4;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p5;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p6;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p7;

