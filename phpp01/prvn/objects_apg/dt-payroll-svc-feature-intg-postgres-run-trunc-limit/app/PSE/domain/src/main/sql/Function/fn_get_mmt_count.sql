CREATE OR REPLACE FUNCTION FN_GET_MMT_COUNT (p_date DATE) RETURN NUMBER
AS

/******************************************************************************
   PURPOSE: Retrun transaction count prior to the offload. This is also help caching

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09.14.2009  Tushar           Created
******************************************************************************/

    Nbr_MMT_RECORDS number(15);
BEGIN

        SELECT 
        	COUNT (*) INTO Nbr_MMT_RECORDS
          FROM psp_money_movement_transaction mmt
               INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = mmt.OFFLOAD_BATCH_FK
         WHERE
           ob.STATUS_CD =  'InProcess'
           AND OB.OFFLOAD_GROUP_FK = (select OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' AND ROWNUM < 2)
           AND OB.OFFLOAD_DATE = SYS_EXTRACT_UTC (TO_TIMESTAMP (p_date))
           AND mmt.initiation_date = SYS_EXTRACT_UTC (TO_TIMESTAMP (p_date))
           AND mmt.money_movement_payment_method = 'ACHDirectDeposit';

   RETURN     Nbr_MMT_RECORDS;

END FN_GET_MMT_COUNT;
/