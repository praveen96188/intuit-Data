update PSP_SYSTEM_PARAMETER sps set system_parameter_value = 'false', modifier_id = 'disableOnDemandOffloadBatches' where SPS.SYSTEM_PARAMETER_CD = 'CREATE_NEW_OFFLOAD_BATCHES_ON_THE_FLY'
/
commit
/
exit
/