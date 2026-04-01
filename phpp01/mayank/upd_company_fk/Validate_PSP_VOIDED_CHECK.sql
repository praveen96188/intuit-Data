----------PSP_VOIDED_CHECK--
declare
    rec_count number;
begin
    SELECT count(*) into rec_count
    FROM PSPADM.PSP_VOIDED_CHECK vc,
         PSPADM.PSP_MONEY_MOVEMENT_TRANSACTION mmt
    WHERE mmt.MONEY_MOVEMENT_TRANSACTION_SEQ= vc.MONEY_MOVEMENT_TRANSACTION_FK
      AND vc.COMPANY_FK != mmt.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_VOIDED_CHECK');
    end if;

    dbms_output.put_line('validation End');
end;
/

