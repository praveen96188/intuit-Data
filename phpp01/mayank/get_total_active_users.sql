set markup csv on
set pages 0

spool get_total_active_users.csv

SELECT
/*+ PARALLEL(8) */
       c.SOURCE_COMPANY_ID
FROM PSPADM.PSP_COMPANY C,
     PSPADM.PSP_ENTITLEMENT E,
     PSPADM.PSP_ENTITLEMENT_UNIT EU,
     PSPADM.PSP_ENTITLEMENT_CODE EC
WHERE C.COMPANY_SEQ = EU.COMPANY_FK
  AND E.ENTITLEMENT_SEQ = EU.ENTITLEMENT_FK
  AND E.ENTITLEMENT_CODE_FK = EC.ENTITLEMENT_CODE_SEQ
  AND E.ENTITLEMENT_STATE = 'Enabled'
  AND EU.ENTITLEMENT_UNIT_STATUS IN
      ('ActivationHold', 'Activated', 'PendingActivation', 'PendingReactivation', 'ErrorActivating');

spool off      

