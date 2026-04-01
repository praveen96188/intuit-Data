MERGE INTO PSPADM.PSP_EDI_PAYMENT_DETAIL c
    using (SELECT epd.EDI_PAYMENT_DETAIL_SEQ, mmt.COMPANY_FK
           FROM PSPADM.PSP_EDI_PAYMENT_DETAIL epd,
                PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION mmt
           WHERE mmt.INITIATION_DATE between TO_DATE('2012-01-20', 'yyyy-mm-dd') and TO_DATE('2012-09-20', 'yyyy-mm-dd')
             AND mmt.MONEY_MOVEMENT_TRANSACTION_SEQ= epd.MONEY_MOVEMENT_TRANSACTION_FK
             AND epd.COMPANY_FK is null
             and ROWNUM <= 5000) p
    on (c.EDI_PAYMENT_DETAIL_SEQ = p.EDI_PAYMENT_DETAIL_SEQ)
    when matched then
        update set c.COMPANY_FK = p.COMPANY_FK;
COMMIT;