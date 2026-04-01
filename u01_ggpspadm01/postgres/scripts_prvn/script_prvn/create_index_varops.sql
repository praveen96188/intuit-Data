set search_path to pspadm;
create index concurrently psp_individual_varchar_patops_i1 on pspadm.psp_individual (lower(email) varchar_pattern_ops);
create index concurrently psp_company_varchar_patops_i5 on pspadm.psp_company (lower(legal_name) varchar_pattern_ops);
analyze (verbose) pspadm.psp_individual;
analyze (verbose) pspadm.psp_company;

