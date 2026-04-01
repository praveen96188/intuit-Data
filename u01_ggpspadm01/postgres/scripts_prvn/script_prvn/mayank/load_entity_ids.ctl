load data
infile '/u01/scripts/mayank/DTPayroll_RetryCustomers.csv'
into table mchoubey.tmp_custid
fields terminated by ','
(entity_id)
