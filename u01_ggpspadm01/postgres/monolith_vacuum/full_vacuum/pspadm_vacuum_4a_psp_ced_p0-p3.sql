\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p0;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p1;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p2;
vacuum (full, analyze, verbose) pspadm.psp_company_event_detail_p3;

