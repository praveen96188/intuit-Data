-- daily data loader
-- updates perf table by looking at all SSTs recorded in hour after latest entry in PSP_PERF_SST up through the current hour - 1
-- i.e. if the last entry is 2011-07-19 13:00 and the current time is 2011-07-19 19:00, then data gathered will be for created date between 2011-07-19 13:00 and 2011-07-19 18:59:59 
spool daily_data_loader.log
INSERT INTO pspadm.PSP_PERF_SST SELECT
  to_timestamp(to_char(new_time(to_timestamp(to_char(sst.created_date, 'YYYY-MM-DD HH24'),'YYYY-MM-DD HH24'),'GMT','PDT'), 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS') as TIME_PACIFIC,                
  type,     
  count(case when (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) < '+00 00:01:00.000000' then 1 else null end) as "< 1 min",
  count(case when (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) >= '+00 00:01:00.000000' and (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) < '+00 00:02:00.000000' then 1 else null end) as "1 to 2 mins",
  count(case when (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) >= '+00 00:02:00.000000' and (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) < '+00 00:03:00.000000' then 1 else null end) as "2 to 3 mins",
  count(case when (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) >= '+00 00:03:00.000000' and (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) < '+00 00:04:00.000000' then 1 else null end) as "3 to 4 mins",                                   
  count(case when (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) >= '+00 00:04:00.000000' and (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) < '+00 00:05:00.000000' then 1 else null end) as "4 to 5 mins",
  count(case when (FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) >= '+00 00:05:00.000000' then 1 else null end) as "> 5 mins",
  count(*) as Total,
  coalesce(avg(extract(second from FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) + extract(minute from FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) * 60 + extract(hour from FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) * 3600),0) as Average,
  coalesce(stddev(extract(second from FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) + extract(minute from FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) * 60 + extract(hour from FINALIZE_DATE_TIME - INITIALIZE_DATE_TIME) * 3600),0) as "Std Dev"
FROM
  pspadm.psp_source_system_transmission sst
WHERE
  sst.created_Date >= new_time((select max(time_pacific) + 1/24 from pspadm.psp_perf_sst), 'PDT', 'GMT')
  and sst.created_Date <  to_timestamp(to_char(new_time(systimestamp,'PDT','GMT'), 'YYYY-MM-DD ') || extract(hour from systimestamp) || ':00:00', 'YYYY-MM-DD HH24:MI:SS')
  and from_source_system = 'QBDT'
group by
  type,
  to_timestamp(to_char(new_time(to_timestamp(to_char(sst.created_date, 'YYYY-MM-DD HH24'),'YYYY-MM-DD HH24'),'GMT','PDT'), 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'),
  to_char(new_time(sst.created_date, 'GMT', 'PDT'), 'MM/DD/YYYY HH24 AM'),
  to_char(new_time(sst.created_date, 'GMT', 'PDT'), 'MM/DD/YYYY HH12 AM') order by
  to_timestamp(to_char(new_time(to_timestamp(to_char(sst.created_date, 'YYYY-MM-DD HH24'),'YYYY-MM-DD HH24'),'GMT','PDT'), 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS'),
  type;
spool off
