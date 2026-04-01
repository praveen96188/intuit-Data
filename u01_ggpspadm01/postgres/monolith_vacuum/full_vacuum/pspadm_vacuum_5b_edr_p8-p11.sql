\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p8;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p9;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p10;
vacuum (full, analyze, verbose) pspadm.psp_entry_detail_record_p11;

