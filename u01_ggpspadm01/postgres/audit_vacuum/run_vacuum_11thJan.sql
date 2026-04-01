\timing

vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m112023_from_ews;
SELECT pg_sleep(60);
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m112023_from_psp;
SELECT pg_sleep(60);
vacuum (analyze, verbose) pg_attribute;
vacuum (analyze, verbose) pg_depend;
vacuum (analyze, verbose) pg_class;
vacuum (analyze, verbose) pg_type;
vacuum (analyze, verbose) pg_statistic;
vacuum (analyze, verbose) pg_index;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_03;
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m122023_from_ews;
SELECT pg_sleep(60);
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m122023_from_qbdt;
SELECT pg_sleep(60);
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m122023_from_psp;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_04;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_05;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_06;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_07;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_08;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_09;
vacuum (analyze, verbose) perfstat.pg_stat_activity_sample_p2023_11_10;
