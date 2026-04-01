\timing
set search_path to ibobadm;
vacuum (analyze, verbose) partman.part_config_sub;
vacuum (analyze, verbose) partman.custom_time_partitions;
vacuum (analyze, verbose) partman.part_config;
vacuum (analyze, verbose) partman.template_perfstat_snap;
vacuum (analyze, verbose) perfstat.snap_default;
vacuum (analyze, verbose) perfstat.pg_stat_statements_hist_default;
vacuum (analyze, verbose) partman.template_perfstat_pg_stat_statements_hist;
vacuum (analyze, verbose) partman.template_perfstat_pg_stat_activity_hist;
vacuum (analyze, verbose) perfstat.pg_stat_activity_hist_default;
vacuum (analyze, verbose) perfstat.pg_stat_database_hist_default;
vacuum (analyze, verbose) partman.template_perfstat_pg_stat_database_hist;
vacuum (analyze, verbose) partman.template_perfstat_pg_stat_all_tables_hist;
vacuum (analyze, verbose) perfstat.pg_stat_all_tables_hist_default;
vacuum (analyze, verbose) partman.template_perfstat_pg_stat_all_indexes_hist;
vacuum (analyze, verbose) perfstat.pg_stat_all_indexes_hist_default;
vacuum (analyze, verbose) perfstat.dba_plans_hist_default;
vacuum (analyze, verbose) partman.template_perfstat_dba_plans_hist;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_default;
vacuum (analyze, verbose) partman.template_perfstat_pg_stat_activity_sample;

