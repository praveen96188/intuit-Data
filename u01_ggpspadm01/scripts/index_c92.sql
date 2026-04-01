spool index_c92.log
set timing on
exec  dbms_application_info.set_module('Creating Index txdetails_source_txn', null)
select sum(bytes)/1024/1024/1024 from dba_segments where segment_name=upper('TxDetails_1');
CREATE INDEX QBO_DATA.txdetails_source_txn ON QBO_DATA.TxDetails_1 (company_id, source_txn_id, source_txn_sequence) local online parallel 8;
alter index qbo_data.txdetails_source_txn noparallel;
select sum(bytes)/1024/1024/1024 from dba_segments where segment_name=upper('txdetails_source_txn');
spool off
exit
