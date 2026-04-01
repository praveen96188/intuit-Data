\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p12;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p13;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p14;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p15;

