\o log/apply_exceptions_report_:dbname.txt
select to_char("ERROR_TIME", 'yyyy-mm-dd') as "INTERVAL", "TABLE_OWNER", "TABLE_NAME", substr("ERROR", 1, 100) as "ERROR", count(*) as "COUNT"
  from awsdms_control_table."awsdms_apply_exceptions"
  where "ERROR_TIME"  >= current_date -interval '2 days'
  group by to_char("ERROR_TIME", 'yyyy-mm-dd'), "TABLE_OWNER", "TABLE_NAME", substr("ERROR", 1, 100) 
  order by 1, 2, 3, 4;
\o


