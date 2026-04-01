\echo :AUTOCOMMIT

\set AUTOCOMMIT off

\echo :AUTOCOMMIT


select * from PSPADM.PSP_LAW where law_id='52'; --- 1 row and require_month_count is 0

select count(*)
from PSPADM.psp_atfpayrolls_to_process
where payroll_run_fk in (select payroll_run_seq
                         from PSPADM.PSP_PAYROLL_RUN
                         where company_fk in (select COMPANY_SEQ
                                              from (select COMPANY_SEQ, count(*)
                                                    from PSPADM.PSP_COMPANY c,
                                                         PSPADM.PSP_COMPANY_LAW cl,
                                                         PSPADM.PSP_COMPANY_AGENCY ca
                                                    where cl.LAW_FK in ('52')
                                                      and ca.COMPANY_AGENCY_SEQ = cl.COMPANY_AGENCY_FK
                                                      and ca.COMPANY_FK = c.COMPANY_SEQ
                                                    group by c.COMPANY_SEQ) as a)
                           and to_char(paycheck_date, 'YYYY Q') in
                               ('2023 4')); --- 5861 rows

select count(*)
from PSPADM.psp_emp_totals_payroll_run
where emp_totals_payroll_run_seq in (select etr.EMP_TOTALS_PAYROLL_RUN_SEQ
                                     from pspadm.PSP_EMP_TOTALS_PAYROLL_RUN etr
                                              join PSPADM.PSP_COMPANY c on c.company_seq = etr.company_fk
                                              join pspadm.psp_paycheck p
                                                   on etr.PAYROLL_RUN_FK = p.payroll_run_fk and etr.company_fk = p.company_fk
                                              join pspadm.psp_tax t
                                                   on p.paycheck_seq = t.paycheck_fk and p.company_fk = t.company_fk
                                     where etr.quarter_start_date > timestamp '2023-10-01'
                                       and etr.quarter_start_date < timestamp '2023-12-31' and t.law_fk = '52');  ---12248 rows


select count(*)
from PSPADM.psp_emp_totals_payroll_run
where emp_totals_payroll_run_seq in (select etr.EMP_TOTALS_PAYROLL_RUN_SEQ
                                     from pspadm.PSP_EMP_TOTALS_PAYROLL_RUN etr
                                              join PSPADM.PSP_COMPANY c on c.company_seq = etr.company_fk
                                              join pspadm.psp_paycheck p
                                                   on etr.PAYROLL_RUN_FK = p.payroll_run_fk and etr.company_fk = p.company_fk
                                              join pspadm.psp_tax t
                                                   on p.paycheck_seq = t.paycheck_fk and p.company_fk = t.company_fk
                                     where etr.quarter_start_date >= timestamp '2023-10-01 07:00:00'
  and etr.quarter_start_date < timestamp '2023-12-31 07:00:00' and t.law_fk = '52');

select count(*)
from PSPADM.psp_employee_law_qtr_totals
where law_fk = '52'
  and year = '2023'
  and quarter = '4'
  and (month_one_worked_indicator != 0 or month_two_worked_indicator != 0 or month_three_worked_indicator != 0);




UPDATE PSPADM.PSP_LAW set requires_month_counts='1' where law_id='52'; --- 1 row updated 

UPDATE PSPADM.psp_atfpayrolls_to_process p2p
set modifier_id='PSP-29197',
    modified_date=timezone('UTC', CURRENT_TIMESTAMP)
where p2p.PAYROLL_RUN_FK in (select payroll_run_seq
                             from PSPADM.PSP_PAYROLL_RUN
                             where company_fk in (select COMPANY_SEQ
                                                  from (select COMPANY_SEQ, count(*)
                                                        from PSPADM.PSP_COMPANY c,
                                                             PSPADM.PSP_COMPANY_LAW cl,
                                                             PSPADM.PSP_COMPANY_AGENCY ca
                                                        where cl.LAW_FK in ('52')
                                                          and ca.COMPANY_AGENCY_SEQ = cl.COMPANY_AGENCY_FK
                                                          and ca.COMPANY_FK = c.COMPANY_SEQ
                                                        group by c.COMPANY_SEQ) as a)
                               and to_char(paycheck_date, 'YYYY Q') in
                                   ('2023 4')); ---- 5861 rows




UPDATE PSPADM.psp_emp_totals_payroll_run
set status='Pending',
    modifier_id='PSP-29197',
    modified_date=timezone('UTC', CURRENT_TIMESTAMP)
where emp_totals_payroll_run_seq in (select etr.EMP_TOTALS_PAYROLL_RUN_SEQ
                                     from pspadm.PSP_EMP_TOTALS_PAYROLL_RUN etr
                                              join PSPADM.PSP_COMPANY c on c.company_seq = etr.company_fk
                                              join pspadm.psp_paycheck p
                                                   on etr.PAYROLL_RUN_FK = p.payroll_run_fk and etr.company_fk = p.company_fk
                                              join pspadm.psp_tax t
                                                   on p.paycheck_seq = t.paycheck_fk and p.company_fk = t.company_fk
                                     where etr.quarter_start_date > timestamp '2023-10-01' and etr.quarter_start_date < timestamp '2023-12-31'
                                       and t.law_fk = '52'); ---- 12248 rows