\timing
set search_path to ibobadm;

vacuum (analyze, verbose)  pg_statistic ;
vacuum (analyze, verbose)  pg_inherits ;
vacuum (analyze, verbose)  perfstat.pg_stat_activity_sample_p2023_02_27 ;
vacuum (analyze, verbose)  perfstat.pg_stat_all_tables_hist_p2023_02_27 ;
vacuum (analyze, verbose)  perfstat.dba_plans_hist_p2023_02_27  ;
vacuum (analyze, verbose)  perfstat.pg_stat_activity_hist_p2023_02_27;
SELECT pg_sleep(60);
vacuum (analyze, verbose)  perfstat.snap_p2023_02_27  ;
vacuum (analyze, verbose)  perfstat.pg_stat_statements_hist_p2023_02_27 ;
vacuum (analyze, verbose)  perfstat.pg_stat_database_hist_p2023_02_27  ;
vacuum (analyze, verbose)  perfstat.pg_stat_all_indexes_hist_p2023_02_27;
vacuum (analyze, verbose)  perfstat.pg_stat_activity_sample_p2023_02_28;
vacuum (analyze, verbose)  perfstat.pg_stat_activity_hist_p2023_02_28  ;
vacuum (analyze, verbose)  perfstat.snap_p2023_02_28 ;
SELECT pg_sleep(60);
vacuum (analyze, verbose)  perfstat.pg_stat_database_hist_p2023_02_28  ;
vacuum (analyze, verbose)  perfstat.pg_stat_statements_hist_p2023_02_28 ;
vacuum (analyze, verbose)  perfstat.pg_stat_all_tables_hist_p2023_02_28 ;
vacuum (analyze, verbose)  perfstat.pg_stat_all_indexes_hist_p2023_02_28;
vacuum (analyze, verbose)  perfstat.dba_plans_hist_p2023_02_28;
SELECT pg_sleep(60);
vacuum (analyze, verbose)  ibobadm.psp_source_system_transmission_m042023_from_qbdt ;
vacuum (analyze, verbose)  ibobadm.psp_source_system_transmission_m042023_from_ews;
vacuum (analyze, verbose)  ibobadm.psp_source_system_transmission_m042023_from_psp;
