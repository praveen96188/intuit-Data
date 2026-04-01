  SELECT regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(s.relname,'_m\d+',''),'_from_psp',''),'_from_qbdt',''),'_from_ews',''),'_from_null',''),'_from_cris',''),'_
from_as400',''),'_from_dflt','') rname,
       sum(n_tup_ins) as inserts,
       sum(n_tup_upd) as updates,
       sum(n_tup_del) as deletes,
       sum(n_tup_hot_upd) as hot_updates,
       sum(n_live_tup) as live_tuples,
       sum(n_dead_tup) as dead_tuples,
       now()::date,
       ((coalesce(sum(n_tup_hot_upd),0)*100)/coalesce(sum(n_tup_upd),0)) ::int,
       (sum(c.relpages)*8192)/1024/1024/1024 AS total_bytes
FROM perfstat.pg_stat_all_tables_hist s, pg_class c where s.relname=c.relname and (n_tup_hot_upd > 0 or n_tup_upd > 0)
group by 1 order by 10;
