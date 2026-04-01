-- regen missing day data
set serverout on size unlimited
declare
v_primary_dc varchar2(10);
p_cluster_id varchar2(10) :=null;
BEGIN
FOR i IN (select * from sbg_vdba.spm_qbo_clusters WHERE cluster_id=nvl(p_cluster_id, cluster_id) and CLUSTER_ID not in ('C60','C61')) LOOP
v_primary_dc :=sbg_vdba.qbo_spm.find_primary_dc_aws(i.cluster_id);
	-- connect to primary site and get topn SQL
	execute immediate ( '
	insert into sbg_vdba.db_clustertopsql
		   (sql_id,
			sql_name,
			plan_hash_value,
			sql_plan_baseline,
			exec,
			avg_lio_per_exec,
			avg_pio_per_exec,
			avg_cpu_tme_per_exec,
			avg_io_tme_per_exec,
			el_per_exec,
			rows_per_exec,
			cluster_id,
			load_date,
			sample_date,
			snap_id)
	select x.sql_id,
		   substr(sql_text, instr(sql_text,''/*'',1, 1), instr(sql_text,''*/'',1, 1)+1),
		   plan_hash_value,
		   null sql_plan_baseline,
		   sum(executions_delta) EXEC,
		   round(sum(buffer_gets_delta)/sum(executions_delta)) avg_LIO_per_exec,
		   round(sum(disk_reads_delta)/sum(executions_delta)) avg_PIO_per_exec,
		   round(sum(cpu_time_delta)/sum(executions_delta)/1000000,4) avg_cpu_tme_per_exec,
		   round(sum(iowait_delta)/sum(executions_delta)/1000000,4) avg_io_tme_per_exec,
		   round(sum(elapsed_time_delta)/1000000/sum(executions_delta),2) el_per_exec,
		   round(sum(rows_processed_delta)/decode(sum(executions_delta), 0, 1, sum(executions_delta))) rows_per_exec,
		   '''||i.cluster_id||''',
		   sysdate,
		   begin_interval_time,
		   x.snap_id
	  from dba_hist_sqlstat@'||'SPM_'||i.cluster_id||v_primary_dc||'  x,
		   (select sql_id, to_char(substr(sql_text,1,2048)) sql_text
			  from gv$sqlarea@'||'SPM_'||i.cluster_id||v_primary_dc||' ) y,
		   dba_hist_snapshot@'||'SPM_'||i.cluster_id||v_primary_dc||' z
	 where x.sql_id = y.sql_id
	   and x.snap_id= z.snap_id
	   and x.executions_delta > 0
	   and x.parsing_schema_name like (''QBO%'')
	   and x.snap_id IN (select snap_id from dba_hist_snapshot@'||'SPM_'||i.cluster_id||v_primary_dc||' where trunc(BEGIN_INTERVAL_TIME) between trunc(sysdate-6) and trunc(sysdate-2))
	 group by substr(sql_text, instr(sql_text,''/*'',1, 1), instr(sql_text,''*/'',1, 1)+1), x.sql_id,
			  plan_hash_value, begin_interval_time, x.snap_id');
	commit;		  
	dbms_output.put_line('# of SQL collected from '||i.cluster_id||' :- '||sql%rowcount);
END LOOP;
END;
/
-- fill hole for a given day/date-range
set serverout on size unlimited
declare
v_primary_dc varchar2(10);
p_cluster_id varchar2(10) :=null;
BEGIN
FOR i IN (select * from sbg_vdba.spm_qbo_clusters WHERE cluster_id=nvl(p_cluster_id, cluster_id) and CLUSTER_ID not in ('C60','C61')) LOOP
v_primary_dc :=sbg_vdba.qbo_spm.find_primary_dc_aws(i.cluster_id);
	-- connect to primary site and get topn SQL
	execute immediate ( '
	merge into sbg_vdba.db_clustertopsql a
using (
	select x.sql_id sql_id,
		   substr(sql_text, instr(sql_text,''/*'',1, 1), instr(sql_text,''*/'',1, 1)+1) sql_name,
		   plan_hash_value,
		   null sql_plan_baseline,
		   sum(executions_delta) EXEC,
		   round(sum(buffer_gets_delta)/sum(executions_delta)) avg_LIO_per_exec,
		   round(sum(disk_reads_delta)/sum(executions_delta)) avg_PIO_per_exec,
		   round(sum(cpu_time_delta)/sum(executions_delta)/1000000,4) avg_cpu_tme_per_exec,
		   round(sum(iowait_delta)/sum(executions_delta)/1000000,4) avg_io_tme_per_exec,
		   round(sum(elapsed_time_delta)/1000000/sum(executions_delta),2) el_per_exec,
		   round(sum(rows_processed_delta)/decode(sum(executions_delta), 0, 1, sum(executions_delta))) rows_per_exec,
		   '''||i.cluster_id||''' cluster_id,
		   sysdate load_date,
		   begin_interval_time sample_date,
		   x.snap_id snap_id
	  from dba_hist_sqlstat@'||'SPM_'||i.cluster_id||v_primary_dc||'  x,
		   (select sql_id, to_char(substr(sql_text,1,2048)) sql_text
			  from gv$sqlarea@'||'SPM_'||i.cluster_id||v_primary_dc||' ) y,
		   dba_hist_snapshot@'||'SPM_'||i.cluster_id||v_primary_dc||' z
	 where x.sql_id = y.sql_id
	   and x.snap_id= z.snap_id
	   and x.executions_delta > 0
	   and x.parsing_schema_name like (''QBO%'')
	   and x.snap_id IN (select snap_id from dba_hist_snapshot@'||'SPM_'||i.cluster_id||v_primary_dc||' where trunc(BEGIN_INTERVAL_TIME) between trunc(sysdate-1) and trunc(sysdate-1))
	 group by substr(sql_text, instr(sql_text,''/*'',1, 1), instr(sql_text,''*/'',1, 1)+1), x.sql_id,
			  plan_hash_value, begin_interval_time, x.snap_id) b
on (a.sql_id=b.sql_id and
a.cluster_id=b.cluster_id and
a.snap_id=b.snap_id and 
nvl(a.sql_name,''x'') = nvl(b.sql_name,''y'') and
a.plan_hash_value=b.plan_hash_value)
  WHEN MATCHED THEN
  update set a.load_date = a.load_date
  WHEN NOT MATCHED THEN			  
	insert (sql_id,
			sql_name,
			plan_hash_value,
			sql_plan_baseline,
			exec,
			avg_lio_per_exec,
			avg_pio_per_exec,
			avg_cpu_tme_per_exec,
			avg_io_tme_per_exec,
			el_per_exec,
			rows_per_exec,
			cluster_id,
			load_date,
			sample_date,
			snap_id)
	values (b.sql_id,
			b.sql_name,
			b.plan_hash_value,
			b.sql_plan_baseline,
			b.exec,
			b.avg_lio_per_exec,
			b.avg_pio_per_exec,
			b.avg_cpu_tme_per_exec,
			b.avg_io_tme_per_exec,
			b.el_per_exec,
			b.rows_per_exec,
			b.cluster_id,
			b.load_date,
			b.sample_date,
			b.snap_id) ');
	dbms_output.put_line('# of SQL collected from '||i.cluster_id||' :- '||sql%rowcount);
END LOOP;
END;
/
