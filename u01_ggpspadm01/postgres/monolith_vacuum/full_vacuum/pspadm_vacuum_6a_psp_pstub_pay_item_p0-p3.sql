\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p0;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p1;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p2;
vacuum (full, analyze, verbose) pspadm.psp_pstub_pay_item_p3;

