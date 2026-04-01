--------PSP_EVENT_AS400_SYNC--
declare
    rec_count number;
begin
    SELECT count(*) into rec_count
    FROM PSPADM.PSP_EVENT_AS400_SYNC ceei,
         PSPADM.PSP_COMPANY_EVENT cei
    WHERE ceei.COMPANY_EVENT_FK = cei.COMPANY_EVENT_SEQ
      AND ceei.COMPANY_FK != cei.COMPANY_FK;

    if(rec_count = 0) then
        dbms_output.put_line('validated successful for PSP_EVENT_AS400_SYNC');
    end if;
    
    dbms_output.put_line('validation End');
end;
/

