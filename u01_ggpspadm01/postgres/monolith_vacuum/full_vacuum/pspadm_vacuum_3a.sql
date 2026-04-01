\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_qbdt_transaction_info_p0;
vacuum (full, analyze, verbose) pspadm.psp_qbdt_transaction_info_p1;
vacuum (full, analyze, verbose) pspadm.psp_qbdt_transaction_info_p2;
vacuum (full, analyze, verbose) pspadm.psp_qbdt_transaction_info_p3;
vacuum (full, analyze, verbose) pspadm.psp_qbdt_transaction_info;

