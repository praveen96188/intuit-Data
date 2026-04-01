\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_tax_p4;
vacuum (full, analyze, verbose) pspadm.psp_tax_p5;
vacuum (full, analyze, verbose) pspadm.psp_tax_p6;
vacuum (full, analyze, verbose) pspadm.psp_tax_p7;

