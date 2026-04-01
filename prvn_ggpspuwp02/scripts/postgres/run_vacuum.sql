\timing
set search_path to ibobadm;                                                                                      
vacuum (analyze, verbose) psp_source_system_transmission_m032022_from_qbdt;
vacuum (analyze, verbose) psp_source_system_transmission_m012022_from_qbdt;
vacuum (analyze, verbose) psp_source_system_transmission_m022022_from_qbdt;
vacuum (analyze, verbose) psp_source_system_transmission_m012022_from_ews ;
vacuum (analyze, verbose) psp_source_system_transmission_m032022_from_ews ;
vacuum (analyze, verbose) psp_source_system_transmission_m022022_from_ews ;
vacuum (analyze, verbose) psp_source_system_transmission_m012022_from_psp ;
vacuum (analyze, verbose) psp_source_system_transmission_m032022_from_psp ;

