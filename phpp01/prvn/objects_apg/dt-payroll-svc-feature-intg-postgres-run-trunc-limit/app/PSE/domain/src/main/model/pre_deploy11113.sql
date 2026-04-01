-- Remove synonym for PSP_STATERPT_PMNTTEMPFRQ_ASSOC
declare
     v_syn user_synonyms.synonym_name%type:=null;
     sqlstr varchar2(100);
     cursor find_syns is
     select owner, synonym_name from all_synonyms where synonym_name = 'PSP_STATERPT_PMNTTEMPFRQ_ASSOC' and owner <> 'PSPREAD';
begin
     for rec in find_syns loop
               if rec.synonym_name is not null then
                  sqlstr:='drop synonym '||rec.owner||'.PSP_STATERPT_PMNTTEMPFRQ_ASSOC';
                                 dbms_output.put_line(rec.owner||' '||rec.synonym_name);
                  execute immediate sqlstr;
               end if;
     end loop;
exception
   when no_data_found then
           null;
end;
/
