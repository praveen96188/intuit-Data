CREATE OR REPLACE PROCEDURE PRC_OFFLOAD_UPDATE_FT
   (
    p_user_id           IN VARCHAR2,  -- For audit purposes
    p_app_server_date   IN TIMESTAMP, -- UTC Date
    p_offload_batch_id  IN VARCHAR2,  -- psp_offload_batch.offload_batch_seq
    p_offload_date      IN TIMESTAMP  -- UTC Date
   )
IS

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
          'PSP_FINANCIAL_TRANSACTION',   --  p_ObjectName         IN   VARCHAR2,
          'N/A',                          --p_UserName           IN   VARCHAR2,
          to_char(SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP),'YYYY-MM-DD"T"HH24:MI:SS'),
          'Updating FINANCIAL_TRANSACTION');

      -- Set the status of Financial Transactions to Executed for all transactions in the Offload Batch
      UPDATE
           psp_financial_transaction ft0
         SET current_transaction_state_fk = 'Executed',
             VERSION = VERSION + 1,
             modifier_id = p_user_id,
             modified_date = v_utc_date
       WHERE ft0.current_transaction_state_fk = 'Created'
         AND ft0.settlement_date >= p_offload_date
         AND EXISTS (
                SELECT
                    'T'
                  FROM psp_money_movement_transaction mmt1
                 WHERE mmt1.money_movement_transaction_seq = ft0.money_movement_transaction_fk
                   AND mmt1.company_fk = ft0.company_fk
                   AND mmt1.offload_batch_fk = p_offload_batch_id
                   AND mmt1.initiation_date = p_offload_date
                   AND mmt1.mm_transaction_amount>=0);



END PRC_OFFLOAD_UPDATE_FT;
/
