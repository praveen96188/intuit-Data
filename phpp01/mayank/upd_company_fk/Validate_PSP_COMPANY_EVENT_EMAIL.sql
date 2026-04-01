----------------PSP_COMPANY_EVENT_EMAIL--
declare
    rec_count number;
begin
    SELECT /*+Parallel(16) */ count(*) into rec_count
    FROM PSPADM.PSP_COMPANY_EVENT_EMAIL ceei,
         PSPADM.PSP_COMPANY_EVENT cei
    WHERE ceei.COMPANY_EVENT_FK = cei.COMPANY_EVENT_SEQ
      AND ceei.COMPANY_FK != cei.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_COMPANY_EVENT_EMAIL');
    end if;

    dbms_output.put_line('validation End');
end;
/

