\timing
set search_path to ibobadm;
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m092014_from_cris;
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m042018_from_ews; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m052018_from_cris;
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m062018_from_ews; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m072018_from_cris;
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m082018_from_dflt;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2022_10_12;
vacuum (analyze, verbose) perfstat.pg_stat_statements_hist_p2022_10_12;
vacuum (analyze, verbose) perfstat.pg_stat_all_indexes_hist_p2022_10_12;
vacuum (analyze, verbose) perfstat.dba_plans_hist_p2022_10_12         ;
vacuum (analyze, verbose) perfstat.pg_stat_all_tables_hist_p2022_10_12;
vacuum (analyze, verbose) perfstat.pg_stat_activity_hist_p2022_10_12  ;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2022_10_13;
vacuum (analyze, verbose) perfstat.pg_stat_activity_hist_p2022_10_13  ;
vacuum (analyze, verbose) perfstat.dba_plans_hist_p2022_10_13         ;
vacuum (analyze, verbose) perfstat.pg_stat_all_indexes_hist_p2022_10_13;
vacuum (analyze, verbose) perfstat.pg_stat_all_tables_hist_p2022_10_13;
vacuum (analyze, verbose) perfstat.pg_stat_statements_hist_p2022_10_13;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2022_10_14;
vacuum (analyze, verbose) perfstat.pg_stat_activity_hist_p2022_10_14  ;
vacuum (analyze, verbose) perfstat.pg_stat_statements_hist_p2022_10_14;
vacuum (analyze, verbose) perfstat.pg_stat_all_indexes_hist_p2022_10_14; 
vacuum (analyze, verbose) perfstat.dba_plans_hist_p2022_10_14         ;
vacuum (analyze, verbose) perfstat.pg_stat_all_tables_hist_p2022_10_14;
 
