\timing
set search_path to pspadm;
vacuum (full, analyze, verbose) pspadm.psp_tax_p0;
vacuum (full, analyze, verbose) pspadm.psp_tax_p1;
vacuum (full, analyze, verbose) pspadm.psp_tax_p2;
vacuum (full, analyze, verbose) pspadm.psp_tax_p3;

