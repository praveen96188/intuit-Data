-------------PSP_TAX_PAYMENT_ON_HOLD_REASON--
declare
    rec_count number;
begin
    SELECT count(*) into rec_count
    FROM PSPADM.PSP_TAX_PAYMENT_ON_HOLD_REASON ceei,
         PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION cei
    WHERE ceei.MONEY_MOVEMENT_TRANSACTION_FK = cei.MONEY_MOVEMENT_TRANSACTION_SEQ
      AND ceei.COMPANY_FK != cei.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_TAX_PAYMENT_ON_HOLD_REASON');
    end if;

    dbms_output.put_line('validation End');
end;
/

