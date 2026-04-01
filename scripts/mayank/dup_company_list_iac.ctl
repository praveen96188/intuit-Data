load data
infile '/home/oracle/AWS/qbopp034/scripts/kpopat/dup_company_list_iac.txt'
into table KPOPAT.dup_company_list_iac
fields terminated by ','
(company_id, cnt)
