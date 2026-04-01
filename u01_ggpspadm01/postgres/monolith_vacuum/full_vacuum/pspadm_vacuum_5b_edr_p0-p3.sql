\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p0;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p1;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p2;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p3;

