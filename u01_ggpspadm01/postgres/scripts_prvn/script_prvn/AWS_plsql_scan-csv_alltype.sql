set feedback off
set verify off
set serveroutput on

prompt Enter Schema to be analyzed
accept schema

spool AWS_plsql_scan_results-&schema-alltype.csv


create table oscgviewtmp (owner varchar2(128), vtype varchar2(50), view_name varchar2(128), text clob);
insert into oscgviewtmp select owner, 'VIEW',view_name, to_lob(text) AS text from all_views where owner=upper('&schema');
insert into oscgviewtmp select owner, 'MVIEW', mview_name, to_lob(query) as text from all_mviews where owner=upper('&schema');

declare
	
        v_testissue integer;
        v_issueexist integer;
        v_issue varchar2(50);
        type objtyp_array_t is table of varchar2(30);
        objtyp_array objtyp_array_t := objtyp_array_t('PROCEDURE','FUNCTION','TRIGGER','PACKAGE BODY','TYPE BODY');
	counter integer;
        type viewtyp_array_t is table of varchar2(30);
	viewtyp_array viewtyp_array_t := viewtyp_array_t('VIEW','MVIEW');
	v_viewcount integer;
 
begin 
--BEGIN VIEW DETAIL

select count(*) into v_viewcount from oscgviewtmp where owner=upper('&schema');

v_testissue:=0;
IF v_viewcount != 0 then


v_issueexist :=0;
   for i in 1..viewtyp_array.count loop


	for view_rec in (select view_name from oscgviewtmp where owner=upper('&schema') and vtype=upper(viewtyp_array(i)) order by view_name)
	loop
	begin
		select 1,'DUAL' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and vtype=viewtyp_array(i) and upper(text) like '%DUAL%');
               if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			

			v_testissue:=1;
        	end if;
		exception
		when no_data_found then
			null;
	end;

	begin
		select 1,'(+)' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and vtype=viewtyp_array(i) and upper(text) like '%(+)%');
               if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			

			v_testissue:=1;
        	end if;
		exception
		when no_data_found then
			null;
	end;

	
	begin
		select 1,'ROWNUM' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%ROWNUM%');
               if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);

			v_testissue:=1;
        	end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
		select 1,'NEXTVAL' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%NEXTVAL%');
               if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);

			v_testissue:=1;
        	end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
		select 1,'HINT' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and text like '%*+%');
               if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);

			v_testissue:=1;
        	end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin

               select 1,'NVL' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%NVL%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
               select 1,'DECODE' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%DECODE%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;

	begin
               select 1,'BITAND' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%BITAND%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;

	begin
              select 1,'SYSDATE' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%SYSDATE%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;

	begin
             select 1,'USERENV' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%USERENV%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
                end if;
		exception
		when no_data_found then
			null;
	end;

	begin
            select 1,'V$ Table' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%V$%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;

	begin
            select 1,'MERGE' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%MERGE%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;

	begin
           select 1,'XML function' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%XML%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||viewtyp_array(i)||','||view_rec.view_name||','||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	
		begin
           select 1,'AS OF Flashback' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%AS OF TIMESTAMP%');
                if v_issueexist = 1 then
                        dbms_output.put_line(rpad(view_rec.view_name,35)||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
		begin
           select 1,'VERSIONS Flashback' into v_issueexist,v_issue from dual where exists (select 1 from oscgviewtmp where owner=upper('&schema') and view_name=view_rec.view_name and upper(text) like '%VERSIONS BETWEEN%');
                if v_issueexist = 1 then
                        dbms_output.put_line(rpad(view_rec.view_name,35)||v_issue);
			v_testissue:=1;
                end if;
		exception
		when no_data_found then
			null;
	end;	



    end loop;
end loop;
	

END IF;


--END VIEW DETAIL

  


--BEGIN PL/SQL types 
begin
   for i in 1..objtyp_array.count loop
   
        for c_rec in (select object_name as name from all_objects where owner=upper('&schema') and object_type=upper(objtyp_array(i)) ) 
	loop

	begin
	counter :=0;
		select 1,'ROWNUM' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i) and upper(text) like '%ROWNUM%');
                if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
        	end if;
	
	exception
		when no_data_found then
			null;

	end;
  	


	begin

               select 1,'NVL' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i) and upper(text) like '%NVL%');
                if v_issueexist = 1 then
                        
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
          exception
            when no_data_found then
                null;
	end;
       
	begin

               select 1,'(+)' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i) and upper(text) like '%(+)%');
                if v_issueexist = 1 then
                        
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
          exception
            when no_data_found then
                null;
	end;

	begin
		select 1,'DUAL' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i) and upper(text) like '%DUAL%');
                if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
        	end if;
		exception
		when no_data_found then
			null;
	end;

             
	

	
	begin
		select 1,'NEXTVAL' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i) and upper(text) like '%NEXTVAL%');
                if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
        	end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
		select 1,'HINT' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name  and type=objtyp_array(i) and text like '%/*+%');
                if v_issueexist = 1 then
			dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
        	end if;
		exception
		when no_data_found then
			null;
 end;
	




	begin
               select 1,'DECODE' into v_issueexist,v_issue from dual where exists ( select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DECODE%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
               select 1,'BITAND' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%BITAND%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	

	begin
              select 1,'SYSDATE' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%SYSDATE%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
 	end;
	

	begin
              select 1,'FROM_TZ' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%FROM_TZ%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
 end;

	begin
             select 1,'USERENV' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%USERENV%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	

	begin
            select 1,'V$ Table' into v_issueexist,v_issue from dual where exists ( select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%V$%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
 end;
	

	begin
            select 1,'MERGE' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%MERGE%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
 end;
	
	begin
           select 1,'XML function' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%XML%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
           select 1,'REFERENCING' into v_issueexist,v_issue from dual where exists ( select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%REFERENCING%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
           select 1,'COMMIT' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%COMMIT;%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	

	begin
           select 1,'ROLLBACK' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%ROLLBACK;%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	

	If objtyp_array(i) != 'PACKAGE BODY' then
	begin

         select 1,'Nested Procedure' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i) and upper(text) like '%PROCEDURE %' and line != 1);
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	
	end;
	end if;

	If objtyp_array(i) != 'PACKAGE BODY' then
	begin
	
         select 1,'Nested Function' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i) and upper(text) like '%FUNCTION %' and line != 1);
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;

	end;
	end if;

	
	begin
         select 1,'PRAGMA RESTRICT_REFERENCES' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%PRAGMA RESTRICT_REFERENCES%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;

	begin
         select 1,'PRAGMA EXCEPTION_INIT' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%PRAGMA EXCEPTION_INIT%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;	
	begin
         select 1,'PRAGMA AUTONOMOUS_TRANSACTION' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%PRAGMA AUTONOMOUS_TRANSACTION%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
         select 1,'SYS.ANYDATA' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%SYS.ANYDATA%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;

		exception
		when no_data_found then
			null;
	end;
	
	begin
         select 1,'SUBTYPE' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%SUBTYPE%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
        select 1,'PIPELINED' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%PIPELINED%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
        select 1,'RAISE_APPLICATION_ERROR' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%RAISE_APPLICATION_ERROR%' );
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
        select 1,'AS OF Flashback' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%AS OF TIMESTAMP%' );
                if v_issueexist = 1 then
                        dbms_output.put_line(rpad(c_rec.name,45)||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
        select 1,'VERSIONS Flashback' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%VERSIONS BETWEEN%' );
                if v_issueexist = 1 then
                        dbms_output.put_line(rpad(c_rec.name,45)||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
	begin
           select 1,'DBMS_OUTPUT package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_OUTPUT%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
        begin
           select 1,'DBMS_LOB package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_LOB%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_SQL package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_SQL%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	end;
	
        begin
           select 1,'DBMS_ALERT package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_ALERT%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_MVIEW package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_MVIEW%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_AQ package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_AQ%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_JOB package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_JOB%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_LOCK package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_LOCK%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_PIPE package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_PIPE%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_PROFILER package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_PROFILER%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_RANDOM package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_RANDOM%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_CRYPTO package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_CRYPTO%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_SESSION package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_SESSION%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_RLS package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_RLS%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_OBFUSCATION package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_OBFISCATION%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	 end;
	
        begin
           select 1,'DBMS_STATS package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_STATS%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_SPACE package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_SPACE%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'DBMS_TRANSACTION package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_TRANSACTION%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	end;
	
        begin
           select 1,'DBMS_UTILITY package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_UTILITY%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
  	end;
	
       begin
           select 1,'DBMS_APPLICATION_INFO package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_APPLICATION_INFO%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
  	end;

      begin
           select 1,'DBMS_XMLQUERY package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_XMLQUERY%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
  	end;


      begin
           select 1,'DBMS_XMLGEN package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_XMLGEN%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
  	end;

        begin
           select 1,'other DBMS package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%DBMS_%'
and upper(text) not like '%DBMS_OUTPUT%'
and upper(text) not like '%DBMS_SQL%'
and upper(text) not like '%DBMS_SESSION%'
and upper(text) not like '%DBMS_UTILITY%'
and upper(text) not like '%DBMS_RLS%'
and upper(text) not like '%DBMS_ALERT%'
and upper(text) not like '%DBMS_PIPE%'
and upper(text) not like '%DBMS_TRANSACTION%'
and upper(text) not like '%DBMS_CRYPTO%'
and upper(text) not like '%DBMS_OBFUSCATION%'
and upper(text) not like '%DBMS_SPACE%'
and upper(text) not like '%DBMS_STATS%'
and upper(text) not like '%DBMS_MVIEW%'
and upper(text) not like '%DBMS_LOB%'
and upper(text) not like '%DBMS_AQ%'
and upper(text) not like '%DBMS_JOB%'
and upper(text) not like '%DBMS_LOCK%'
and upper(text) not like '%DBMS_APPLICATION_INFO%'
and upper(text) not like '%DBMS_XMLQUERY%'
and upper(text) not like '%DBMS_XMLGEN%');

                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	end;
	
	begin
           select 1,'UTL_FILE package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%UTL_FILE%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
			v_testissue:=1;
			counter := counter +1;
                end if;
		exception
		when no_data_found then
			null;
	end;
	
        begin
           select 1,'UTL_RAW package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%UTL_RAW%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
	end;
	
        begin
           select 1,'UTL_HTTP package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%UTL_HTTP%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	end;
 
        begin
           select 1,'UTL_SMTP package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%UTL_SMTP%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	end;              
	

        begin
           select 1,'UTL_TCP package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%UTL_TCP%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	end;          

        begin
           select 1,'other UTL package' into v_issueexist,v_issue from dual where exists (select 1 from all_source where owner=upper('&schema') and name=c_rec.name and type=objtyp_array(i)  and upper(text) like '%UTL_%'
 and upper(text) not like '%UTL_FILE%' 
and upper(text) not like '%UTL_RAW%' 
and upper(text) not like '%UTL_HTTP%'
and upper(text) not like '%UTL_SMTP%'
and upper(text) not like '%UTL_TCP%');
                if v_issueexist = 1 then
                        dbms_output.put_line('&schema'||','||objtyp_array(i)||','||c_rec.name||','||v_issue);
                        v_testissue:=1;
			counter := counter +1;
                end if;
                exception
                when no_data_found then
                        null;
 	end;
        

	
	end loop;
 end loop;
end;
end;
/


drop table oscgviewtmp; 
spool off;

begin
dbms_output.put_line('  ');
dbms_output.put_line('  ');
dbms_output.put_line('******************************************************************************');
dbms_output.put_line('********************* MIGRATION ASSESSMENT COMPLETE **************************');
dbms_output.put_line('******** Please send output to your AWS representative for review ********');
dbms_output.put_line('******************************************************************************');
dbms_output.put_line('*********** Output file is : AWS_plsql_scan_results-&schema-alltype.csv ');
dbms_output.put_line('******************************************************************************');
end;
/

