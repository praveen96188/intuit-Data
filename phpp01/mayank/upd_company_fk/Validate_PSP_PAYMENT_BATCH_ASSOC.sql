------- PSP_PAYMENT_BATCH_ASSOC--
declare
    rec_count number;
begin
    SELECT count(*) into rec_count
    FROM PSPADM.PSP_PAYMENT_BATCH_ASSOC pba,
         PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION mmt
    WHERE mmt.MONEY_MOVEMENT_TRANSACTION_SEQ= pba.MONEY_MOVEMENT_TRANSACTION_FK
      AND pba.COMPANY_FK != mmt.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_PAYMENT_BATCH_ASSOC');
    end if;

    dbms_output.put_line('validation End');
end;
/

