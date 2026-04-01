\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p4;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p5;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p6;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p7;

