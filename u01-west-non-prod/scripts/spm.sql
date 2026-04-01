create or replace PACKAGE BODY            "QBO_SPM"
IS
---------------------------------------------------------------------------------------
-- PROCEDURE: create_dbl
---------------------------------------------------------------------------------------
PROCEDURE create_dbl as
    --
    v_tns varchar2(512);
    v_host varchar2(128);
    --
	BEGIN

		FOR i IN (select * from sbg_vdba.spm_qbo_clusters) LOOP

			-- create DBL in QCY
			begin
			    if i.cluster_ip_qcy is not null then
		    v_host := i.cluster_ip_qcy;
		else
		    v_host := 'ora'||i.cluster_sid_qcy||'.qcyf01.ie.intuit.net';
		end if;
				v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||v_host||')(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_qcy||')))';
				execute immediate 'create public database link SPM_'||i.cluster_id||'_QCY using '''||v_tns||''' ';
			exception
			     when others then
				  null;
					  Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' QCY DBL (All Clusters)' || chr(10) || sqlerrm);
			end;
			-- create DBL in LAS
			begin
			    if i.cluster_ip_las is not null then
		    v_host := i.cluster_ip_las;
		else
		    v_host := 'ora'||i.cluster_sid_las||'.lasf01.ie.intuit.net';
		end if;
				v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||v_host||')(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_las||')))';
				execute immediate 'create public database link SPM_'||i.cluster_id||'_LAS using '''||v_tns||''' ';
			exception
			     when others then
				  null;
					  Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' LAS DBL (All Clusters)' || chr(10) || sqlerrm);
			end;
			-- create DBL in UW2
			begin
				v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||i.cluster_sid_uw2||'.sbg-psp-ppd.a.intuit.com)(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_uw2||')))';
				execute immediate 'create public database link SPM_'||i.cluster_id||'_UW2 using '''||v_tns||''' ';
			exception
			     when others then
				  null;
					  Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' UW2 DBL (All Clusters)' || chr(10) || sqlerrm);
			end;

		END LOOP;
	END create_dbl;
---------------------------------------------------------------------------------------
-- PROCEDURE: create_dbl
---------------------------------------------------------------------------------------
PROCEDURE create_dbl(p_cluster_id varchar2)  AS
    --
    v_tns varchar2(512);
    v_host varchar2(128);
    --
	BEGIN
		FOR i IN (select * from sbg_vdba.spm_qbo_clusters where cluster_id=p_cluster_id) LOOP
			-- create DBL in QCY
			IF i.cluster_sid_las is not null THEN
			begin
			    if i.cluster_ip_qcy is not null then
		    v_host := i.cluster_ip_qcy;
		else
		    v_host := 'ora'||i.cluster_sid_qcy||'.qcyf01.ie.intuit.net';
		end if;
				v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||v_host||')(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_qcy||')))';
				execute immediate 'create public database link SPM_'||i.cluster_id||'_QCY using '''||v_tns||''' ';
			exception
			     when others then
				  null;
					  Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' QCY DBL (Single Cluster)' || chr(10) || sqlerrm);
			end;
			END IF;
			-- create DBL in LAS
			IF i.cluster_sid_qcy is not null THEN
			begin
			    if i.cluster_ip_las is not null then
		    v_host := i.cluster_ip_las;
		else
		    v_host := 'ora'||i.cluster_sid_las||'.lasf01.ie.intuit.net';
		end if;
				v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||v_host||')(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_las||')))';
				execute immediate 'create public database link SPM_'||i.cluster_id||'_LAS using '''||v_tns||''' ';
			exception
			     when others then
				  null;
					  Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' LAS DBL (Single Cluster)' || chr(10) || sqlerrm);
			end;
			END IF;
			-- create DBL in UW2
			begin
			  if i.cluster_sid_uw2 is not null then
				v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||i.cluster_sid_uw2||'.sbg-psp-ppd.a.intuit.com)(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_uw2||')))';
				execute immediate 'create public database link SPM_'||i.cluster_id||'_UW2 using '''||v_tns||''' ';
			  end if;
			exception
			     when others then
				  null;
					  Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' UW2 DBL (Single Cluster)' || chr(10) || sqlerrm);
			end;
		END LOOP;
	END create_dbl;
---------------------------------------------------------------------------------------
-- FUNCTION: find_primary_dc
---------------------------------------------------------------------------------------
FUNCTION find_primary_dc(p_cluster_id IN VARCHAR2)
  RETURN VARCHAR2 IS
	v_primary_dc  varchar2(10):='_QCY';
	-- find out primary dc
	BEGIN
	    create_dbl(p_cluster_id);
		execute immediate 'select decode(OPEN_MODE,''READ WRITE'',''_QCY'',''_LAS'')
							 from v$database@'||'SPM_'||p_cluster_id||'_QCY' into v_primary_dc;
		dbms_output.put_line('With help of SQL'||v_primary_dc);
		RETURN v_primary_dc;
	exception
	     when others then
		  v_primary_dc :='_LAS';
		      dbms_output.put_line('Due to error primay DC '||v_primary_dc||' Error '|| chr(10) || sqlerrm);
		  RETURN v_primary_dc;
	END find_primary_dc;
---------------------------------------------------------------------------------------
-- FUNCTION: find_primary_dc_aws
---------------------------------------------------------------------------------------
FUNCTION find_primary_dc_aws(p_cluster_id IN VARCHAR2)
  RETURN VARCHAR2 IS
	v_primary_dc  varchar2(10):='_QCY';
	v_count       number :=0;
	-- find out primary dc
	BEGIN
		-- check connections on AWS
	    create_dbl(p_cluster_id);
		execute immediate 'select count(1)
				     from v$session@'||'SPM_'||p_cluster_id||'_UW2'||' where username like ''QBO%'' or username like ''%UGT%''' into v_count;
		IF v_count > 0 THEN
		   v_primary_dc :='_UW2';
		   RETURN v_primary_dc;
	ELSE
		   execute immediate 'select decode(OPEN_MODE,''READ WRITE'',''_QCY'',''_LAS'')
							     from v$database@'||'SPM_'||p_cluster_id||'_QCY' into v_primary_dc;
		   dbms_output.put_line('With help of SQL'||v_primary_dc);
		   RETURN v_primary_dc;
		END IF;
	EXCEPTION
	     WHEN others THEN
		  v_primary_dc :='_LAS';
		      dbms_output.put_line('Due to error primay DC '||v_primary_dc||' Error '|| chr(10) || sqlerrm);
		  RETURN v_primary_dc;
	END find_primary_dc_aws;
---------------------------------------------------------------------------------------
-- PROCEDURE: sql_id_stats
---------------------------------------------------------------------------------------
PROCEDURE sql_id_stats(p_sql_id   IN varchar2, p_sql_handle IN varchar2 default 'N') AS

	v_primary_dc  varchar2(10):='_QCY';
	v_db_link  varchar2(30):=NULL;
	v_run_id number:=-1;

	BEGIN

		-- create_dbl(p_user_id,p_password) ;

		select sbg_vdba.spm_sql_id_stats_seq.nextval
		  into v_run_id
		from dual;

		FOR i IN (select * from sbg_vdba.spm_qbo_clusters where cluster_sid_qcy is not null and cluster_sid_las is not null) LOOP

			v_primary_dc :=find_primary_dc(i.cluster_id);

			BEGIN
				SELECT db_link
				  INTO v_db_link
				  FROM dba_db_links
				 WHERE db_link = 'SPM_'||i.cluster_id||v_primary_dc;

			EXCEPTION
				 WHEN no_data_found THEN
					  create_dbl(i.cluster_id) ;
				 WHEN others THEN
					  null;
					  Dbms_Output.Put_Line('Error in creating DBL ' || chr(10) || sqlerrm);
			End;
			-- connect to priamry site and get the sql stats
			dbms_output.put_line('SPM_'||i.cluster_id||v_primary_dc);
	    IF p_sql_handle = 'N' THEN
	    begin
			execute immediate( 'insert into sbg_vdba.spm_sql_id_stats
										(cluster_id,
										 sql_id,
										 plan_hash_value,
										 sql_profile,
										 sql_plan_baseline,
										 executions,
										 avg_rows_returned,
										 elapsed_time_per_exec,
										 avg_pio,
										 avg_lio,
										 avg_cpu_time_per_exec,
										 concurrency_wait_time,
										 user_io_wait_time,
										 command_type,
										 last_active_time,
										 create_date,
										 run_id,
										 sql_id_handle)
								 select '''||i.cluster_id||''',
										sql_id,
										plan_hash_value ,
										sql_profile,
										sql_plan_baseline,
										sum(executions),
										round(sum(rows_processed)/sum(executions)) avg_rows_returned,
										round(sum(elapsed_time)/1000/sum(executions)/1000,4) elapsed_time_per_exec ,
										round(sum(disk_reads)/sum(executions)) avg_pio,
										round (sum(buffer_gets) / sum(executions)) avg_lio,
										round (sum(cpu_time)/ sum(executions) / 1000/1000,4) avg_cpu_time_per_exec,
										round(sum(concurrency_wait_time)/sum(executions)/1000/1000,4) concurrency_wait_time,
										round(sum(user_io_wait_time)/sum(executions)/1000/1000,4) user_io_wait_time,
										command_type,
										max(last_active_time) last_active_time,
										sysdate create_date,'
										||v_run_id||','||''''||p_sql_id||''''||
								 ' from gv$sql@'||'SPM_'||i.cluster_id||v_primary_dc||
								' where sql_id='||''''||p_sql_id||''''||
								  ' and executions > 0 '||
								 ' group by '''||i.cluster_id||''',
									   sql_id,
									   plan_hash_value ,
									   sql_profile,
									   sql_plan_baseline,
									   command_type,
									   sysdate'
			   );
			exception
			when others then
			     null;
			end;
			dbms_output.put_line('# of SQL Collected From '||i.cluster_id||' For sql_id '||p_sql_id||' :- '||sql%rowcount);
			commit;
			ELSIF p_sql_handle = 'Y' THEN
						execute immediate( 'insert into sbg_vdba.spm_sql_id_stats
										(cluster_id,
										 sql_id,
										 plan_hash_value,
										 sql_profile,
										 sql_plan_baseline,
										 executions,
										 avg_rows_returned,
										 elapsed_time_per_exec,
										 avg_pio,
										 avg_lio,
										 avg_cpu_time_per_exec,
										 concurrency_wait_time,
										 user_io_wait_time,
										 command_type,
										 last_active_time,
										 create_date,
										 run_id,
										 sql_id_handle)
								 select '''||i.cluster_id||''',
										sql_id,
										plan_hash_value ,
										sql_profile,
										sql_plan_baseline,
										sum(executions),
										round(sum(rows_processed)/sum(executions)) avg_rows_returned,
										round(sum(elapsed_time)/1000/sum(executions)/1000,4) elapsed_time_per_exec ,
										round(sum(disk_reads)/sum(executions)) avg_pio,
										round (sum(buffer_gets) / sum(executions)) avg_lio,
										round (sum(cpu_time)/ sum(executions) / 1000/1000,4) avg_cpu_time_per_exec,
										round(sum(concurrency_wait_time)/sum(executions)/1000/1000,4) concurrency_wait_time,
										round(sum(user_io_wait_time)/sum(executions)/1000/1000,4) user_io_wait_time,
										command_type,
										max(last_active_time) last_active_time,
										sysdate create_date,'
										||v_run_id||','||''''||p_sql_id||''''||
								 ' from gv$sql@'||'SPM_'||i.cluster_id||v_primary_dc||
								' where sql_text like '||''''||p_sql_id||''''||
								  ' and executions > 0 '||
								 ' group by '''||i.cluster_id||''',
									   sql_id,
									   plan_hash_value ,
									   sql_profile,
									   sql_plan_baseline,
									   command_type,
									   sysdate'
			   );
			dbms_output.put_line('# of SQL Collected From '||i.cluster_id||' For sql_id' ||p_sql_id||' :- '||sql%rowcount);
			commit;
			END IF;
		END LOOP;
		-- Collecting data from AWS
		FOR i IN (select * from sbg_vdba.spm_qbo_clusters where cluster_sid_uw2 is not null and cluster_type='AWS') LOOP

			v_primary_dc :='_UW2';

			BEGIN
				SELECT db_link
				  INTO v_db_link
				  FROM dba_db_links
				 WHERE db_link = 'SPM_'||i.cluster_id||v_primary_dc;

			EXCEPTION
				 WHEN no_data_found THEN
					  create_dbl(i.cluster_id) ;
				 WHEN others THEN
					  null;
					  Dbms_Output.Put_Line('Error in creating DBL for UW2 ' || chr(10) || sqlerrm);
			End;
			-- connect to priamry site and get the sql stats
			dbms_output.put_line('SPM_'||i.cluster_id||v_primary_dc);

			execute immediate( 'insert into sbg_vdba.spm_sql_id_stats
										(cluster_id,
										 sql_id,
										 plan_hash_value,
										 sql_profile,
										 sql_plan_baseline,
										 executions,
										 avg_rows_returned,
										 elapsed_time_per_exec,
										 avg_pio,
										 avg_lio,
										 avg_cpu_time_per_exec,
										 concurrency_wait_time,
										 user_io_wait_time,
										 command_type,
										 last_active_time,
										 create_date,
										 run_id,
										 sql_id_handle)
								 select '''||i.cluster_id||'-UW2'||''',
										sql_id,
										plan_hash_value ,
										sql_profile,
										sql_plan_baseline,
										sum(executions),
										round(sum(rows_processed)/sum(executions)) avg_rows_returned,
										round(sum(elapsed_time)/1000/sum(executions)/1000,4) elapsed_time_per_exec ,
										round(sum(disk_reads)/sum(executions)) avg_pio,
										round (sum(buffer_gets) / sum(executions)) avg_lio,
										round (sum(cpu_time)/ sum(executions) / 1000/1000,4) avg_cpu_time_per_exec,
										round(sum(concurrency_wait_time)/sum(executions)/1000/1000,4) concurrency_wait_time,
										round(sum(user_io_wait_time)/sum(executions)/1000/1000,4) user_io_wait_time,
										command_type,
										max(last_active_time) last_active_time,
										sysdate create_date,'
										||v_run_id||','||''''||p_sql_id||''''||
								 ' from gv$sql@'||'SPM_'||i.cluster_id||v_primary_dc||
								' where sql_id='||''''||p_sql_id||''''||
								  ' and executions > 0 '||
								 ' group by '''||i.cluster_id||''',
									   sql_id,
									   plan_hash_value ,
									   sql_profile,
									   sql_plan_baseline,
									   command_type,
									   sysdate'
			   );
			dbms_output.put_line('# of SQL collected from UW2'||i.cluster_id||' :- '||sql%rowcount);
			commit;
		END LOOP;

	END sql_id_stats;
---------------------------------------------------------------------------------------
-- PROCEDURE: export_plan
---------------------------------------------------------------------------------------
PROCEDURE export_plan(p_sql_id varchar2) AS

	cur sys_refcursor;
	v_sqlset_stgtab_name varchar2(30);

	BEGIN

	BEGIN
			select 'SPM_'||upper(p_sql_id)||'_'||lpad(nvl(max(substr(table_name, instr(table_name,'_',1,2)+1)),0)+1,4,'0')
			  into v_sqlset_stgtab_name
			  from user_tables
			 where table_name like 'SPM_'||upper(p_sql_id)||'%';
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20501, 'Error in generating SQLSET/STGTBL name - '||v_sqlset_stgtab_name||' '|| SQLERRM);
	    END;

		BEGIN
			DBMS_SQLTUNE.CREATE_SQLSET (sqlset_name  => v_sqlset_stgtab_name,
						    description  => 'SPM');
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20502, 'Error in creating SQLSET ' || SQLERRM);
		END;

	    BEGIN
			OPEN cur FOR
			SELECT VALUE(P)
			  FROM table(DBMS_SQLTUNE.SELECT_CURSOR_CACHE(BASIC_FILTER=> 'sql_id='||CHR(39)||p_sql_id||CHR(39))) P;
			DBMS_SQLTUNE.LOAD_SQLSET(sqlset_name => v_sqlset_stgtab_name, populate_cursor => cur);
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20503, 'Error in loading SQLSET ' || SQLERRM);
	    END;
		-- ORA-30626: function/procedure parameters of remote object types are not supported
		-- PL/SQL: ORA-00906: missing left parenthesis
	    BEGIN
			DBMS_SQLTUNE.CREATE_STGTAB_SQLSET(table_name => v_sqlset_stgtab_name);
 	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20504, 'Error in creating STGTAB ' || SQLERRM);
	    END;
	    BEGIN
			DBMS_SQLTUNE.PACK_STGTAB_SQLSET(sqlset_name => v_sqlset_stgtab_name, staging_table_name => v_sqlset_stgtab_name);
 	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20505, 'Error in packing SQLSET to STGTAB ' || SQLERRM);
	    END;
	END export_plan;
---------------------------------------------------------------------------------------
-- PROCEDURE: export_plan_awr
---------------------------------------------------------------------------------------
PROCEDURE export_plan_awr(p_sql_id varchar2, p_begin_snap number,p_end_snap number) AS

	cur sys_refcursor;
	v_sqlset_stgtab_name varchar2(30);

	BEGIN

	BEGIN
			select 'SPM_'||upper(p_sql_id)||'_'||lpad(nvl(max(substr(table_name, instr(table_name,'_',1,2)+1)),0)+1,4,'0')
			  into v_sqlset_stgtab_name
			  from user_tables
			 where table_name like 'SPM_'||upper(p_sql_id)||'%';
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20701, 'Error in generating SQLSET/STGTBL name - '||v_sqlset_stgtab_name||' '|| SQLERRM);
	    END;

		BEGIN
			DBMS_SQLTUNE.CREATE_SQLSET (sqlset_name  => v_sqlset_stgtab_name,
						    description  => 'SPM');
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20702, 'Error in creating SQLSET ' || SQLERRM);
		END;

	    BEGIN
			OPEN cur FOR
			SELECT VALUE(P)
			FROM table(DBMS_SQLTUNE.SELECT_WORKLOAD_REPOSITORY(p_begin_snap, p_end_snap, 'sql_id='||CHR(39)||p_sql_id||CHR(39))) P;

			DBMS_SQLTUNE.LOAD_SQLSET(sqlset_name => v_sqlset_stgtab_name, populate_cursor => cur);

			/* OPEN cur FOR
			SELECT VALUE(P)
			  FROM table(DBMS_SQLTUNE.SELECT_CURSOR_CACHE(BASIC_FILTER=> 'sql_id='||CHR(39)||p_sql_id||CHR(39))) P;
			DBMS_SQLTUNE.LOAD_SQLSET(sqlset_name => v_sqlset_stgtab_name, populate_cursor => cur);*/

	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20703, 'Error in loading SQLSET ' || SQLERRM);
	    END;
		-- ORA-30626: function/procedure parameters of remote object types are not supported
		-- PL/SQL: ORA-00906: missing left parenthesis
	    BEGIN
			DBMS_SQLTUNE.CREATE_STGTAB_SQLSET(table_name => v_sqlset_stgtab_name);
 	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20704, 'Error in creating STGTAB ' || SQLERRM);
	    END;
	    BEGIN
			DBMS_SQLTUNE.PACK_STGTAB_SQLSET(sqlset_name => v_sqlset_stgtab_name, staging_table_name => v_sqlset_stgtab_name);
 	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20705, 'Error in packing SQLSET to STGTAB ' || SQLERRM);
	    END;
	END export_plan_awr;
---------------------------------------------------------------------------------------
-- PROCEDURE: import_plan
---------------------------------------------------------------------------------------
PROCEDURE import_plan(p_sql_id varchar2,
		      p_plan_hash_value number,
		      p_source_dbl_name varchar2) AS
    --
  	v_plans_loaded	 		   pls_integer;
  	v_user		 		   varchar2(30);
	v_sqlset_stgtab_name 	   varchar2(30);
	v_source_db_name_start_pos pls_integer;
	v_source_db_name_end_pos   pls_integer;
	--
	BEGIN
	    --
		v_user := sys_context('USERENV','SESSION_USER');
		-- create_dbl(substr(p_source_dbl_name,5,3));
		v_source_db_name_start_pos := instr(p_source_dbl_name,'_')+1;
		v_source_db_name_end_pos   := instr(p_source_dbl_name,'_',1,2);
		create_dbl(substr(p_source_dbl_name,v_source_db_name_start_pos,v_source_db_name_end_pos-v_source_db_name_start_pos));
	-- getting sqlset/stgtbl name
	BEGIN
			execute immediate 'select ''SPM_''||upper('''||p_sql_id||''')||''_''||lpad(nvl(max(substr(table_name, instr(table_name,''_'',1,2)+1)),0),4,''0'')'||
			 ' from user_tables'||'@'||p_source_dbl_name||
		   ' where table_name like ''SPM_''||upper('''||p_sql_id||''')||''%'''	into v_sqlset_stgtab_name ;
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20601, 'Error in getting SQLSET/STGTBL name - '||v_sqlset_stgtab_name||' '|| SQLERRM);
	    END;
		-- copy sqlset staging table
	 BEGIN
		execute immediate 'create table '||v_user||'.'||v_sqlset_stgtab_name||' as select * from '||v_sqlset_stgtab_name||'@'||p_source_dbl_name;
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20602, 'Error in copying sqlset STGTBL '||v_sqlset_stgtab_name||' '|| SQLERRM);
	    END;
		-- create sqlset
	BEGIN
			DBMS_SQLTUNE.CREATE_SQLSET (sqlset_name  => v_sqlset_stgtab_name,
										description  => 'SPM',
										SQLSET_OWNER => v_user);
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20603, 'Error in creating SQLSET ' || SQLERRM);
	    END;
		 -- unpack sqlset
	BEGIN
			DBMS_SQLTUNE.UNPACK_STGTAB_SQLSET(sqlset_name => v_sqlset_stgtab_name,
											  sqlset_owner => v_user,
											  replace => true,
											  staging_table_name => v_sqlset_stgtab_name,
											  staging_schema_owner => v_user);
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20604, 'Error in unpacking SQLSET ' || SQLERRM);
	    END;
		 -- create spb
	BEGIN
			v_plans_loaded :=
			DBMS_SPM.load_plans_from_sqlset(sqlset_owner => v_user,
											sqlset_name => v_sqlset_stgtab_name,
											basic_filter => 'sql_id='||CHR(39)||p_sql_id||CHR(39)||' and plan_hash_value='||CHR(39)||p_plan_hash_value||CHR(39),
											fixed => 'YES');
			DBMS_OUTPUT.put_line ('Plans Loaded: ' || v_plans_loaded);
	    EXCEPTION
			 WHEN OTHERS THEN
				  RAISE_APPLICATION_ERROR(-20605, 'Error in unpacking SQLSET ' || SQLERRM);
	    END;
	END import_plan;
END QBO_SPM;
/


