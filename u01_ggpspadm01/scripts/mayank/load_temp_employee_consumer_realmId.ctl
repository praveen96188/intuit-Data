load data
infile '/u01/scripts/mayank/temp_employee_consumer_realmId.csv'
into table mchoubey.temp_employee_consumer_realmId
fields terminated by ','
(employee_seq,
consumer_realm_id,
is_in_ems)

