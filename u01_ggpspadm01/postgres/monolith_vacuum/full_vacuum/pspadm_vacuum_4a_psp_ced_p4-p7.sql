\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p4;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p5;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p6;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p7;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail;

