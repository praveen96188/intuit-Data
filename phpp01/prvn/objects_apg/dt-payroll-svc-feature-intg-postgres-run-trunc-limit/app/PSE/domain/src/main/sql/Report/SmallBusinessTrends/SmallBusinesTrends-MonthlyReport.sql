TTITLE CENTER 'Small Business Trending - Month over Previous Month Comparison' SKIP 1 -
       CENTER _DATE SKIP 3

set linesize 200
set heading on
col t1_start_date format a14 head 'T1 Start Date' justify left
col t2_start_date format a14 head 'T2 Start Date' justify left
col companies format 999,999 head 'Companies' justify right
col paid_more_ee format 999,9999 head 'Paid More EEs' justify right
col paid_fewer_ee format 9999,9999 head 'Paid Fewer EEs' justify right
col same format 999,9999 head 'Paid Same Number of EEs' justify right
col total_ee_added format 9999,999 head 'Total EEs Added' justify right
col total_ee_removed format 999,999 head 'Total EEs Lost' justify right
col net_ee format 9999,999 head 'Net EEs' justify right
col t1_total_net_pay format 999,999,999 head 'T1 Total Net Pay' justify right
col t2_total_net_pay format 999,999,999 head 'T2 Total Net Pay' justify right
col net_pay_diff format 9999,999,999 head 'Net Pay Delta' justify right

      
-- month over previous month
select 
  to_char(trunc(add_months(systimestamp, -2), 'MM'), 'YYYY-MM') as t1_start_date,
  to_char(trunc(add_months(systimestamp, -1), 'MM'), 'YYYY-MM') as t2_start_date,
  to_char(count(*), '999,999') as companies,
  to_char(sum(case when t2.employee_count > t1.employee_count then 1 else 0 end), '999,999') as paid_more_ee,
  to_char(sum(case when t1.employee_count > t2.employee_count then 1 else 0 end), '999,999') as paid_fewer_ee,
  to_char(sum(case when t1.employee_count = t2.employee_count then 1 else 0 end), '999,999') as same,
  to_char(sum(case when t2.employee_count > t1.employee_count then t2.employee_count - t1.employee_count else 0 end), '999,999') as total_ee_added,
  to_char(sum(case when t1.employee_count > t2.employee_count then t1.employee_count - t2.employee_count else 0 end), '999,999') as total_ee_removed,
  to_char(sum(t2.employee_count) - sum(t1.employee_count), '999,999') as net_ee,  
  to_char(sum(t1.total_net_pay), '999,999,999,999.99') as t1_total_net_pay,
  to_char(sum(t2.total_net_pay), '999,999,999,999.99') as t2_total_net_pay,
  to_char(sum(t2.total_net_pay) - sum(t1.total_net_pay), '999,999,999,999.99') as net_pay_diff
from psp_rpt_paid_employees t1
  inner join psp_rpt_paid_employees t2 on t2.company_seq = t1.company_seq
and t1.start_date = (select trunc(add_months(systimestamp, -2), 'MM') from dual)
and t2.start_date = (select trunc(add_months(systimestamp, -1), 'MM') from dual);


TTITLE CENTER 'Small Business Trending - Month over Previous Year Month Comparison' SKIP 1 -
       CENTER _DATE SKIP 3

      
-- month over previous year month
select 
  to_char(trunc(add_months(systimestamp, -13), 'MM'), 'YYYY-MM') as t1_start_date,
  to_char(trunc(add_months(systimestamp, -1), 'MM'), 'YYYY-MM') as t2_start_date,
  to_char(count(*), '999,999') as companies,
  to_char(sum(case when t2.employee_count > t1.employee_count then 1 else 0 end), '999,999') as paid_more_ee,
  to_char(sum(case when t1.employee_count > t2.employee_count then 1 else 0 end), '999,999') as paid_fewer_ee,
  to_char(sum(case when t1.employee_count = t2.employee_count then 1 else 0 end), '999,999') as same,
  to_char(sum(case when t2.employee_count > t1.employee_count then t2.employee_count - t1.employee_count else 0 end), '999,999') as total_ee_added,
  to_char(sum(case when t1.employee_count > t2.employee_count then t1.employee_count - t2.employee_count else 0 end), '999,999') as total_ee_removed,
  to_char(sum(t2.employee_count) - sum(t1.employee_count), '999,999') as net_ee,  
  to_char(sum(t1.total_net_pay), '999,999,999,999.99') as t1_total_net_pay,
  to_char(sum(t2.total_net_pay), '999,999,999,999.99') as t2_total_net_pay,
  to_char(sum(t2.total_net_pay) - sum(t1.total_net_pay), '999,999,999,999.99') as net_pay_diff
from psp_rpt_paid_employees t1
  inner join psp_rpt_paid_employees t2 on t2.company_seq = t1.company_seq
and t1.start_date = (select trunc(add_months(systimestamp, -13), 'MM') from dual)
and t2.start_date = (select trunc(add_months(systimestamp, -1), 'MM') from dual);
