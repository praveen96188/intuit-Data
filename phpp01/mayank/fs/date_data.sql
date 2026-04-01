set echo on feedback on timing on

ALTER SESSION SET NLS_DATE_FORMAT='dd-mon-yyyy hh24:mi:ss';

spool date_data
select /*+ PARALLEL(16) */ min(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as min_mod_date, 
		max(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as max_mod_date 
from pspadm.PSP_PAYROLL_RUN 
where to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') >= to_date('28-AUG-2018 22:05:00','dd-MON-yyyy hh24:mi:ss') 
	and to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') <= to_date('28-AUG-2021 22:05:00','dd-MON-yyyy hh24:mi:ss');

select /*+ PARALLEL(16) */ min(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as min_mod_date, 
		max(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as max_mod_date 
from pspadm.PSP_FINANCIAL_TRANSACTION 
where to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') >= to_date('28-AUG-2018 22:05:00','dd-MON-yyyy hh24:mi:ss') 
	and to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') <= to_date('28-AUG-2021 22:05:00','dd-MON-yyyy hh24:mi:ss');

select /*+ PARALLEL(16) */ min(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as min_mod_date, 
		max(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as max_mod_date 
from pspadm.PSP_MONEY_MOVEMENT_TRANSACTION 
where to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') >= to_date('28-AUG-2018 22:05:00','dd-MON-yyyy hh24:mi:ss') 
	and to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') <= to_date('28-AUG-2021 22:05:00','dd-MON-yyyy hh24:mi:ss');

select /*+ PARALLEL(16) */ min(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as min_mod_date, 
		max(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as max_mod_date 
from pspadm.PSP_ENTRY_DETAIL_RECORD 
where to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') >= to_date('28-AUG-2018 22:05:00','dd-MON-yyyy hh24:mi:ss') 
	and to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') <= to_date('28-AUG-2021 22:05:00','dd-MON-yyyy hh24:mi:ss');

select /*+ PARALLEL(16) */ min(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as min_mod_date, 
		max(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as max_mod_date 
from pspadm.PSP_PAYCHECK 
where to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') >= to_date('28-AUG-2018 22:05:00','dd-MON-yyyy hh24:mi:ss') 
	and to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') <= to_date('28-AUG-2021 22:05:00','dd-MON-yyyy hh24:mi:ss');

select /*+ PARALLEL(16) */ min(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as min_mod_date, 
		max(to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss')) as max_mod_date 
from pspadm.PSP_PAYCHECK_SPLIT 
where to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') >= to_date('28-AUG-2018 22:05:00','dd-MON-yyyy hh24:mi:ss') 
	and to_date(to_char(modified_date,'dd-MON-yyyy hh24:mi:ss'),'dd-MON-yyyy hh24:mi:ss') <= to_date('28-AUG-2021 22:05:00','dd-MON-yyyy hh24:mi:ss');

spool off

