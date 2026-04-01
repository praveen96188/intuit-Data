SET PAGESIZE 40000
SET FEEDBACK OFF
SET NUM 30
set colsep ,
set headsep off
SET TERMOUT OFF
set pagesize 0
set trimspool on
set timing on
SPOOL file_name.csv
SELECT /*+ PARALLEL(16) */ EU.SERVICE_KEY,
                           (CASE
                                WHEN EU.ENTITLEMENT_UNIT_STATUS IN
                                     ('PendingActivation', 'PendingReactivation', 'Activated', 'ErrorActivating',
                                      'ActivationHold') THEN 'Active'
                                ELSE 'Inactive' END) STATUS
FROM PSPADM.PSP_ENTITLEMENT E,
     PSPADM.PSP_ENTITLEMENT_UNIT EU,
     PSPADM.PSP_ENTITLEMENT_CODE EC
WHERE E.ENTITLEMENT_SEQ = EU.ENTITLEMENT_FK
  AND E.ENTITLEMENT_CODE_FK = EC.ENTITLEMENT_CODE_SEQ
  AND EC.ASSET_ITEM_CD IN ('DIY', 'DIYDiskDelivery');

spool off
