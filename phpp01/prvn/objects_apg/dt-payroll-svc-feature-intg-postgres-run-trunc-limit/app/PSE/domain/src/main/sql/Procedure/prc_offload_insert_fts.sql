CREATE OR REPLACE PROCEDURE PRC_OFFLOAD_INSERT_FTS
   (
    p_user_id           IN VARCHAR2,  -- For audit purposes
    p_app_server_date   IN TIMESTAMP, -- UTC Date
    p_offload_batch_id  IN VARCHAR2,  -- psp_offload_batch.offload_batch_seq
    p_offload_date      IN TIMESTAMP  -- UTC Date    
   )
IS
     TYPE ft_record IS RECORD (
            ft_seq PSP_FINANCIAL_TRANSACTION.FINANCIAL_TRANSACTION_SEQ%TYPE,
            ft_company_fk PSP_FINANCIAL_TRANSACTION.company_fk%TYPE,
            ft_transaction_type_fk PSP_FINANCIAL_TRANSACTION.transaction_type_fk%TYPE
        );

      TYPE ft_list_type IS TABLE OF ft_record;
      ft_list ft_list_type;
    -- these two variables are used in all SQL statements to populate date fields,
    -- the UTC date is used to populate SPCF audit fields created_date and modified_date

    v_psp_date TIMESTAMP; -- current system date and time adjusted by PSPDate offset
    v_utc_date TIMESTAMP; -- current system UTC date and time

  --  SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;



    BEGIN

-- DATE LOGIC TO BE REVIEWED ********
-- I think we should use p_offload_date instead of v_psp_date ..

    	SELECT SYS_EXTRACT_UTC(FN_GET_PSP_TIMESTAMP) INTO v_psp_date FROM DUAL;
        
        
       	v_utc_date := p_app_server_date;

       -- Create Executed Transaction States for all transactions in the Offload Batch

            SELECT/*+parallel(4)*/ FT.FINANCIAL_TRANSACTION_SEQ, ft.company_fk, ft.transaction_type_fk
                BULK COLLECT INTO ft_list
            FROM PSP_FINANCIAL_TRANSACTION FT,
                 PSP_MONEY_MOVEMENT_TRANSACTION MMT
            WHERE MMT.OFFLOAD_BATCH_FK = p_offload_batch_id
              AND MMT.MONEY_MOVEMENT_TRANSACTION_SEQ = FT.MONEY_MOVEMENT_TRANSACTION_FK
              AND MMT.COMPANY_FK = FT.COMPANY_FK
              AND FT.CURRENT_TRANSACTION_STATE_FK = 'Executed'
              AND MMT.initiation_date =  p_offload_date
              AND FT.SETTLEMENT_DATE >= MMT.INITIATION_DATE
              AND trunc(FT.SETTLEMENT_DATE) >= trunc(MMT.INITIATION_DATE);

            FORALL indx IN 1 .. ft_list.COUNT
                INSERT /*+APPEND */ INTO PSP_FINANCIAL_TRANS_STATE
                 (FINANCIAL_TRANS_STATE_SEQ, VERSION, CREATOR_ID,
                   CREATED_DATE, MODIFIER_ID, MODIFIED_DATE,
                   REALM_ID, TRANSACTION_STATE_EFF_DATE, INSERT_USER_ID,
                   GEMS_UPLOAD_BATCH_FK, FINANCIAL_TRANSACTION_FK, TRANSACTION_STATE_FK,
                   TRANSACTION_RESPONSE_FK, COMPANY_FK, TRANSACTION_TYPE_FK)
                   VALUES (FN_FORMAT_SYSGUID(SYS_GUID()), 0, p_user_id, v_utc_date, p_user_id, v_utc_date, -1,
                trunc(new_time(v_psp_date,'GMT','PDT')) + .5, NULL, NULL, ft_list(indx).ft_seq, 'Executed', NULL,  ft_list(indx).ft_company_fk,  ft_list(indx).ft_transaction_type_fk);

            ft_list.DELETE;

END PRC_OFFLOAD_INSERT_FTS;
/
