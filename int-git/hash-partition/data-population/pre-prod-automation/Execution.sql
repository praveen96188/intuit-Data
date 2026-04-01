MERGE INTO PSPADM.PSP_EVENT_AS400_SYNC ceep
    using (SELECT /*+PARALLEL(16) */ceepi.EVENT_AS400_SYNC_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_EVENT_AS400_SYNC ceepi,
                PSPADM.PSP_COMPANY_EVENT cei
           WHERE ceepi.COMPANY_EVENT_FK = cei.COMPANY_EVENT_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.EVENT_AS400_SYNC_SEQ = ce.EVENT_AS400_SYNC_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_TAX_PAYMENT_ON_HOLD_REASON ceep
    using (SELECT /*+PARALLEL(16) */ceepi.TAX_PAYMENT_ON_HOLD_REASON_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_TAX_PAYMENT_ON_HOLD_REASON ceepi,
                PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION cei
           WHERE ceepi.MONEY_MOVEMENT_TRANSACTION_FK = cei.MONEY_MOVEMENT_TRANSACTION_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.TAX_PAYMENT_ON_HOLD_REASON_SEQ = ce.TAX_PAYMENT_ON_HOLD_REASON_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_PAYSTUB ceep
    using (SELECT /*+PARALLEL(16) */ceepi.PAYSTUB_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_PAYSTUB ceepi,
                PSPADM.PSP_PAYCHECK cei
           WHERE ceepi.PAYCHECK_FK = cei.PAYCHECK_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.PAYSTUB_SEQ = ce.PAYSTUB_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_COMPANY_EVENT_EMAIL ceep
    using (SELECT /*+PARALLEL(16) */ceepi.COMPANY_EVENT_EMAIL_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_COMPANY_EVENT_EMAIL ceepi,
                PSPADM.PSP_COMPANY_EVENT cei
           WHERE ceepi.COMPANY_EVENT_FK = cei.COMPANY_EVENT_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.COMPANY_EVENT_EMAIL_SEQ = ce.COMPANY_EVENT_EMAIL_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_WC_PAYCHECK ceep
    using (SELECT /*+PARALLEL(16) */ceepi.WC_PAYCHECK_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_WC_PAYCHECK ceepi,
                PSPADM.PSP_PAYCHECK cei
           WHERE ceepi.PAYCHECK_FK = cei.PAYCHECK_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.WC_PAYCHECK_SEQ = ce.WC_PAYCHECK_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_TP401K_PAYCHECK ceep
    using (SELECT /*+PARALLEL(16) */ceepi.TP401K_PAYCHECK_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_TP401K_PAYCHECK ceepi,
                PSPADM.PSP_PAYCHECK cei
           WHERE ceepi.PAYCHECK_FK = cei.PAYCHECK_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.TP401K_PAYCHECK_SEQ = ce.TP401K_PAYCHECK_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_TP401K_BATCH_PAYCHECK ceep
    using (SELECT /*+PARALLEL(16) */ceepi.TP401K_BATCH_PAYCHECK_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_TP401K_BATCH_PAYCHECK ceepi,
                PSPADM.PSP_PAYCHECK cei
           WHERE ceepi.PAYCHECK_FK = cei.PAYCHECK_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.TP401K_BATCH_PAYCHECK_SEQ = ce.TP401K_BATCH_PAYCHECK_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_EDI_PAYMENT_DETAIL ceep
    using (SELECT /*+PARALLEL(16) */ceepi.EDI_PAYMENT_DETAIL_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_EDI_PAYMENT_DETAIL ceepi,
                PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION cei
           WHERE ceepi.MONEY_MOVEMENT_TRANSACTION_FK = cei.MONEY_MOVEMENT_TRANSACTION_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.EDI_PAYMENT_DETAIL_SEQ = ce.EDI_PAYMENT_DETAIL_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_EFTPS_PAYMENT_DETAIL ceep
    using (SELECT /*+PARALLEL(16) */ceepi.EFTPS_PAYMENT_DETAIL_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_EFTPS_PAYMENT_DETAIL ceepi,
                PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION cei
           WHERE ceepi.MONEY_MOVEMENT_TRANSACTION_FK = cei.MONEY_MOVEMENT_TRANSACTION_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.EFTPS_PAYMENT_DETAIL_SEQ = ce.EFTPS_PAYMENT_DETAIL_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_VOIDED_CHECK ceep
    using (SELECT /*+PARALLEL(16) */ceepi.VOIDED_CHECK_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_VOIDED_CHECK ceepi,
                PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION cei
           WHERE ceepi.MONEY_MOVEMENT_TRANSACTION_FK = cei.MONEY_MOVEMENT_TRANSACTION_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.VOIDED_CHECK_SEQ = ce.VOIDED_CHECK_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_FSET_FILING_DETAIL ceep
    using (SELECT /*+PARALLEL(16) */ceepi.FSET_FILING_DETAIL_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_FSET_FILING_DETAIL ceepi,
                PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION cei
           WHERE ceepi.MONEY_MOVEMENT_TRANSACTION_FK = cei.MONEY_MOVEMENT_TRANSACTION_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.FSET_FILING_DETAIL_SEQ = ce.FSET_FILING_DETAIL_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_PAYMENT_BATCH_ASSOC ceep
    using (SELECT /*+PARALLEL(16) */ceepi.PAYMENT_BATCH_ASSOC_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_PAYMENT_BATCH_ASSOC ceepi,
                PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION cei
           WHERE ceepi.MONEY_MOVEMENT_TRANSACTION_FK = cei.MONEY_MOVEMENT_TRANSACTION_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.PAYMENT_BATCH_ASSOC_SEQ = ce.PAYMENT_BATCH_ASSOC_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_TRANSACTION_OFFLOAD_BATCH ceep
    using (SELECT /*+PARALLEL(16) */ceepi.TRANSACTION_OFFLOAD_BATCH_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_TRANSACTION_OFFLOAD_BATCH ceepi,
                PSPADM.PSP_FINANCIAL_TRANSACTION cei
           WHERE ceepi.FINANCIAL_TRANSACTION_FK = cei.FINANCIAL_TRANSACTION_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.TRANSACTION_OFFLOAD_BATCH_SEQ = ce.TRANSACTION_OFFLOAD_BATCH_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_PAYCHECK_USAGE_HIST ceep
    using (SELECT /*+PARALLEL(16) */ceepi.PAYCHECK_USAGE_HIST_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_PAYCHECK_USAGE_HIST ceepi,
                PSPADM.PSP_PAYCHECK_USAGE cei
           WHERE ceepi.PAYCHECK_USAGE_FK = cei.PAYCHECK_USAGE_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.PAYCHECK_USAGE_HIST_SEQ = ce.PAYCHECK_USAGE_HIST_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_DISBURSE_ADVICE_TAX_LIAB ceep
    using (SELECT /*+PARALLEL(16) */ceepi.DISBURSE_ADVICE_TAX_LIAB_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_DISBURSE_ADVICE_TAX_LIAB ceepi,
                PSPADM.PSP_DISBURSE_ADVICE cei
           WHERE ceepi.DISBURSE_ADVICE_FK = cei.DISBURSE_ADVICE_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.DISBURSE_ADVICE_TAX_LIAB_SEQ = ce.DISBURSE_ADVICE_TAX_LIAB_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_PSTUB_DDITEM ceep
    using (SELECT /*+PARALLEL(16) */ceepi.PSTUB_DDITEM_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_PSTUB_DDITEM ceepi,
                PSPADM.PSP_PAYSTUB cei
           WHERE ceepi.PAYSTUB_FK = cei.PAYSTUB_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.PSTUB_DDITEM_SEQ = ce.PSTUB_DDITEM_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_PSTUB_MSG ceep
    using (SELECT /*+PARALLEL(16) */ceepi.PSTUB_MSG_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_PSTUB_MSG ceepi,
                PSPADM.PSP_PAYSTUB cei
           WHERE ceepi.PAYSTUB_FK = cei.PAYSTUB_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.PSTUB_MSG_SEQ = ce.PSTUB_MSG_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

MERGE INTO PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM ceep
    using (SELECT /*+PARALLEL(16) */ceepi.PSTUB_PAID_TIMEOFF_ITEM_SEQ, cei.COMPANY_FK
           FROM PSPADM.PSP_PSTUB_PAID_TIMEOFF_ITEM ceepi,
                PSPADM.PSP_PAYSTUB cei
           WHERE ceepi.PAYSTUB_FK = cei.PAYSTUB_SEQ
             AND ceepi.COMPANY_FK is null) ce
    on (ceep.PSTUB_PAID_TIMEOFF_ITEM_SEQ = ce.PSTUB_PAID_TIMEOFF_ITEM_SEQ)
    when matched then
        update set ceep.COMPANY_FK = ce.COMPANY_FK;
COMMIT;

