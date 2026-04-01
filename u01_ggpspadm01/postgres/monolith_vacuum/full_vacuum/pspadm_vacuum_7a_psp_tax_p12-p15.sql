\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_tax_p12;
vacuum (full, analyze, verbose) pspadm.psp_tax_p13;
vacuum (full, analyze, verbose) pspadm.psp_tax_p14;
vacuum (full, analyze, verbose) pspadm.psp_tax_p15;

