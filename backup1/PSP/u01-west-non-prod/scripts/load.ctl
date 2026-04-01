load data
infile '/u01/scripts/tmp_table_sys.csv'
into table mchoubey.tmp_custid
fields terminated by ','
(entity_id)
