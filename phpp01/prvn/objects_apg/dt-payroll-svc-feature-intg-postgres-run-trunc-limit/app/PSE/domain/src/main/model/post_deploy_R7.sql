UPDATE PSP_COMPANY_SERVICE
   SET STATUS_CD = 'Cancelled',
       STATUS_EFFECTIVE_DATE = SYS_EXTRACT_UTC (systimestamp),
       MODIFIED_DATE = SYS_EXTRACT_UTC (systimestamp),
       MODIFIER_ID = 'PSRV003453'
 WHERE SERVICE_FK = 'CheckDistribution' AND STATUS_CD <> 'Cancelled';
 
commit;