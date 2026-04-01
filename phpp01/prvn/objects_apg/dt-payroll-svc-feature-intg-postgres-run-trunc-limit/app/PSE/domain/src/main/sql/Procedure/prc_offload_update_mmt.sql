CREATE OR REPLACE PROCEDURE PRC_OFFLOAD_UPDATE_MMT
   (
    p_user_id           IN VARCHAR2,            -- For audit purposes
    p_app_server_date   IN TIMESTAMP,    -- UTC Date
    p_offload_batch_id  IN VARCHAR2,       -- psp_offload_batch.offload_batch_seq
    p_offload_date      IN TIMESTAMP ,      -- UTC Date
    p_file_type          IN       VARCHAR2     -- DD or Tax
   )
IS

    TYPE mmt_record IS RECORD (
        mmt_seq psp_money_movement_transaction.money_movement_transaction_seq%TYPE,
        mmt_company_fk psp_money_movement_transaction.company_fk%TYPE
    );
    TYPE mmt_list_type IS TABLE OF mmt_record;
    mmt_list mmt_list_type;
    -- these two variables are used in all SQL statements to populate date fields,
    -- the UTC date is used to populate SPCF audit fields created_date and modified_date
    v_psp_date TIMESTAMP; -- current system date and time adjusted by PSPDate offset
    v_utc_date TIMESTAMP; -- current system UTC date and time

   v_return_cd number; -- return code variable for logging
   v_error_desc varchar2(100);-- error desc variable for logging
BEGIN

     SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;
      v_utc_date := p_app_server_date;

      PRC_SET_PSP_EVENT_LOG (
        v_RETURN_CD,
        v_ERROR_DESC,
       'N/A',                          -- p_CompanyId          IN   VARCHAR2,
        null,                         -- p_TypeCd             IN   VARCHAR2,
       'PROD',                         -- p_DomainName         IN   VARCHAR2,
       'PSP',                          --p_ArchName           IN   VARCHAR2,
       'PRC_OFFLOAD',                  -- p_CompName           IN   VARCHAR2,
       'N/A',                          -- p_HostName           IN   VARCHAR2,
       'Offload Stored Proc',          -- Application_name           IN   VARCHAR2,
       'PSP_MONEY_MOVEMENT_TRANSACTION',   --  p_ObjectName         IN   VARCHAR2,
       'N/A',                          --p_UserName           IN   VARCHAR2,
       to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS') , -- p_MessageDTTM
       'Updating MONEY_MOVEMENT_TRANSACTION');

       --
       -- Set MMTxns status to Executed
       --

       SELECT/*+parallel(4) */ mmt.money_movement_transaction_seq, mmt.company_fk
            BULK COLLECT INTO mmt_list
       FROM psp_money_movement_transaction mmt
       WHERE mmt.offload_batch_fk = p_offload_batch_id
          AND mmt.initiation_date = p_offload_date
          AND mmt.mm_transaction_amount>=0;

       IF p_file_type = 'Tax'  THEN

            FORALL indx IN 1 .. mmt_list.COUNT
                UPDATE
                    psp_money_movement_transaction mmt
                SET status = 'Executed',
                    VERSION = VERSION + 1,
                    modifier_id = p_user_id,
                    tax_payment_status = 'SentToAgency',
                    modified_date = v_utc_date
                WHERE mmt.company_fk = mmt_list(indx).mmt_company_fk
                        AND mmt.money_movement_transaction_seq = mmt_list(indx).mmt_seq;

       ELSE

           FORALL indx IN 1 .. mmt_list.COUNT
                UPDATE
                    psp_money_movement_transaction mmt
                SET
                    status = 'Executed',
                    VERSION = VERSION + 1,
                    modifier_id = p_user_id,
                    modified_date = v_utc_date
                WHERE mmt.company_fk = mmt_list(indx).mmt_company_fk
                  AND mmt.money_movement_transaction_seq = mmt_list(indx).mmt_seq;

       END IF;

       mmt_list.DELETE;


END PRC_OFFLOAD_UPDATE_MMT;
/
