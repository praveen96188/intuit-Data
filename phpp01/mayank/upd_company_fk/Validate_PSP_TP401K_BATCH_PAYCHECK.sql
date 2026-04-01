--------------PSP_TP401K_BATCH_PAYCHECK--
declare
    rec_count number;
begin
    SELECT count(*) into rec_count
    FROM PSPADM.PSP_TP401K_BATCH_PAYCHECK ci,
         PSPADM.PSP_PAYCHECK pi
    WHERE pi.PAYCHECK_SEQ = ci.PAYCHECK_FK
      AND ci.COMPANY_FK != pi.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_TP401K_BATCH_PAYCHECK');
    end if;

    dbms_output.put_line('validation End');
end;
/

