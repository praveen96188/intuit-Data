CREATE OR REPLACE FUNCTION FN_GET_EDR_COUNT (p_date VARCHAR2) RETURN NUMBER
AS


/******************************************************************************
   PURPOSE: Retrun transaction count prior to the offload. This is also help caching

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        09.14.2009  Tushar           Created
   1.1          03.10.2010  Jeff Jones       Removed Sys_Extract_UTC function and
                                           changed input parm to VARCHAR2
******************************************************************************/

    Nbr_EDR_RECORDS number(15);
BEGIN

                SELECT
                        count(*) INTO     Nbr_EDR_RECORDS
                 FROM PSP_ENTRY_DETAIL_RECORD rec0
                      INNER JOIN PSP_NACHAFILE nf ON NF.NACHAFILE_SEQ = REC0.N_A_C_H_A_FILE_FK
                      INNER JOIN PSP_OFFLOAD_BATCH ob ON OB.OFFLOAD_BATCH_SEQ = NF.OFFLOAD_BATCH_FK
                WHERE
                    ob.STATUS_CD =  'InProcess'
                    AND OB.OFFLOAD_GROUP_FK = (select OFFLOAD_GROUP_SEQ FROM PSP_OFFLOAD_GROUP WHERE OFFLOAD_GROUP_CD = 'STD' AND ROWNUM < 2)
                    AND OB.OFFLOAD_DATE = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"')
                    AND rec0.initiation_date = TO_TIMESTAMP(p_date, 'YYYY-MM-DD"T"HH24:MI:SS.FF"Z"');
   RETURN     Nbr_EDR_RECORDS;
END FN_GET_EDR_COUNT;


