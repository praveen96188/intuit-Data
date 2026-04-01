col index_name for a30
col index_size for 9999
col col_list for a150
select segment_name index_name, 
       sum(bytes)/1024/1024/1024 index_size, 
       (select listagg(column_name, ',') within group (order by column_position) col_list 
          from dba_ind_columns y 
         where x.segment_name = y.index_name)  col_list,
        max(clustering_factor) clustering_factor,
       max(clustering_factor)/(select sum(blocks) from dba_tables where table_name='&table_name') num_block_visits
  from dba_segments x,
       dba_indexes y
 where segment_name in (select index_name from dba_indexes where table_name=upper('&table_name') and owner='QBO_DATA')
  and x.segment_name=y.index_name
 group by segment_name
 order by 2 desc
/
