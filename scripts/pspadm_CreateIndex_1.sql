set search_path=pspadm;
create index concurrently idx_emp_qtr_totals_year on pspadm.psp_employee_law_qtr_totals (year);

create index concurrently idx_emp_w2_totals_year on pspadm.psp_employee_w2_totals (year);
