load data
infile 'data_file.txt'
into table MCHOUBEY.PSP_TMP_CLOB
FIELDS TERMINATED BY ',' TRAILING NULLCOLS 
(s_no,
file_name CHAR(100),
request_document LOBFILE(file_name) TERMINATED BY EOF)
