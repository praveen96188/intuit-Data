set pages 0
select sum(bytes)/1024/1024/1024 from dba_segments where segment_name='CASHBASIS_1';
exit
