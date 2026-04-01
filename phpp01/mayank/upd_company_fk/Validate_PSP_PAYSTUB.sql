-----------------PSP_PAYSTUB- 11m
declare
    rec_count number;
begin
    SELECT /*+Parallel(16) */ count(*) into rec_count
    FROM PSPADM.PSP_PAYSTUB ci,
         PSPADM.PSP_PAYCHECK pi
    WHERE pi.PAYCHECK_SEQ = ci.PAYCHECK_FK
      AND ci.COMPANY_FK != pi.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_PAYSTUB');
    end if;

    dbms_output.put_line('validation End');
end;
/

