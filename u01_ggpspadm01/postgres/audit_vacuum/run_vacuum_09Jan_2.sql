\timing
set search_path to ibobadm;
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m022011_from_qbdt; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m032011_from_ews;  
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m032011_from_cris; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m022011_from_cris; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m022011_from_null; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m032011_from_null; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m032011_from_psp;  
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m022011_from_dflt; 
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m022011_from_psp;  
vacuum (analyze, verbose) ibobadm.psp_source_system_transmission_m032011_from_as400;

