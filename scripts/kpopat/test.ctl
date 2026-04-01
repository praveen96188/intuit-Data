load data
infile '/home/oracle/AWS/qbopp034/scripts/kpopat/test.csv'
into table KPOPAT.test
fields terminated by ','
(co_id, dt "to_date(:dt,'DD-MON-YY')")
