-------------PSP_EDI_PAYMENT_DETAIL--

declare
    rec_count number;
begin
    SELECT count(*) into rec_count
    FROM PSPADM.PSP_EDI_PAYMENT_DETAIL epd,
         PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION mmt
    WHERE mmt.MONEY_MOVEMENT_TRANSACTION_SEQ= epd.MONEY_MOVEMENT_TRANSACTION_FK
      AND epd.COMPANY_FK !=  mmt.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_EDI_PAYMENT_DETAIL');
    end if;

    dbms_output.put_line('validation End');
end;
/

