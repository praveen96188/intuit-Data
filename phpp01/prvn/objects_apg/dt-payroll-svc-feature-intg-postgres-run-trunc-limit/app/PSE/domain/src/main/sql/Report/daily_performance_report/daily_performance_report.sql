set linesize 205;
set pagesize 100;
col "Pct" justify right;
col Type format a33;
col Average format 9999.99;
col Total format 99999;

spool daily_performance_report.log
select
                to_char(time_pacific, 'MM/DD/YYYY HH12 AM') as "Time"
                ,transmission_type as "Type"
                ,"< 1 min", to_char(("< 1 min" / "TOTAL") * 100, '999.99') || '%' as "Pct"
                ,"1 to 2 mins" as "1-2 mins", to_char(("1 to 2 mins" / "TOTAL") * 100, '999.99') || '%' as "Pct"
                ,"2 to 3 mins" as "2-3 mins", to_char(("2 to 3 mins" / "TOTAL") * 100, '999.99') || '%' as "Pct"
                ,"3 to 4 mins" as "3-4 mins", to_char(("3 to 4 mins" / "TOTAL") * 100, '999.99') || '%' as "Pct"
                ,"4 to 5 mins" as "4-5 mins", to_char(("4 to 5 mins" / "TOTAL") * 100, '999.99') || '%' as "Pct"
                ,"> 5 mins", to_char(("> 5 mins" / "TOTAL") * 100, '999.99') || '%' as "Pct"
                , "TOTAL" as "Total"
                , "AVERAGE" as "Average"
                , "STD DEV" as "Std Dev"
from
                pspadm.psp_perf_sst
where
                time_pacific >= systimestamp - 6/24
order by
                time_pacific, transmission_type
/
spool off
