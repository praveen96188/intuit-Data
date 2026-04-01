select segment_name index_name, sum(bytes)/1024/1024/1024 index_size
  from dba_segments
 where segment_name in (select index_name from dba_indexes where table_name='&table_name' and owner='QBO_DATA')
 group by segment_name
/
