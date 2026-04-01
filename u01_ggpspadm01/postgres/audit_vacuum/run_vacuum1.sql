\timing
set search_path to ibobadm;
vacuum (analyze, verbose) psp_source_system_transmission_m102022_from_qbdt;
vacuum (analyze, verbose) psp_source_system_transmission_m092022_from_qbdt;
vacuum (analyze, verbose) psp_source_system_transmission_m102022_from_ews ;
vacuum (analyze, verbose) psp_source_system_transmission_m092022_from_ews ;
vacuum (analyze, verbose) psp_source_system_transmission_m102022_from_psp ;
vacuum (analyze, verbose) psp_source_system_transmission_m092022_from_psp ;
vacuum (analyze, verbose) psp_hcm401k_employee_deduction;
vacuum (analyze, verbose) gg_heartbeat;

