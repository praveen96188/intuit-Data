CREATE OR REPLACE FUNCTION FN_GET_MMT_COUNT (p_date TIMESTAMP) RETURNS INTEGER AS
$$
DECLARE
Nbr_MMT_RECORDS INTEGER;
BEGIN
SELECT COUNT(*) INTO Nbr_MMT_RECORDS
FROM psp_money_movement_transaction mmt
         INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = mmt.OFFLOAD_BATCH_FK
WHERE ob.STATUS_CD = 'InProcess'
  AND OB.OFFLOAD_GROUP_FK = (SELECT OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' LIMIT 1)
  AND OB.OFFLOAD_DATE = timezone('UTC', cast (p_date AS timestamptz))
  AND mmt.initiation_date = timezone('UTC', cast (p_date AS timestamptz))
  AND mmt.money_movement_payment_method = 'ACHDirectDeposit';

RETURN Nbr_MMT_RECORDS;
END;
$$ LANGUAGE plpgsql;