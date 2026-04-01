spool /u01/scripts/log/spm/&5
set lines 100
SET serverout on
SET VERIFY OFF
DECLARE
  --
  v_source_cluster_dbl   varchar2(30);
  v_target_cluster_dbl   varchar2(30);
  v_sqlid                varchar2(30);
  v_planhash             number;
  v_source_cluster_dc    varchar2(30);
  v_is_aws               varchar2(10);
  v_is_aws_s               varchar2(10);
  v_sqlsetstgtabn        varchar2(30);
  v_begin_snap           number;
  v_end_snap             number;
  --
BEGIN
  --
  dbms_output.disable;
  v_sqlid      :='&1';
  v_planhash   :='&2';
  v_begin_snap :=&6;
  v_end_snap   :=&7;
  --
  select substr('&3',instr('&3','-')+1)
    into v_is_aws_s
    from dual;
  IF v_is_aws_s='UW2' THEN
    select 'SPM_'||substr('&3',1,instr('&3','-')-1)||'_'||v_is_aws_s
      into v_source_cluster_dbl
      from dual;
    dbms_output.put_line('Is AWS:'||v_is_aws_s||'-'||v_source_cluster_dbl);
  ELSE
    v_source_cluster_dbl:= 'SPM_'||'&3'||sbg_vdba.qbo_spm.find_primary_dc('&3');
    dbms_output.put_line('Not AWS:'||v_is_aws_s);
  END IF;

  --
  select substr('&4',instr('&4','-')+1)
    into v_is_aws
    from dual;
  IF v_is_aws='UW2' THEN
    select 'SPM_'||substr('&4',1,instr('&4','-')-1)||'_'||v_is_aws
      into v_target_cluster_dbl
      from dual;
    dbms_output.put_line('Is AWS:'||v_is_aws||'-'||v_target_cluster_dbl);
  ELSE
    v_target_cluster_dbl:= 'SPM_'||'&4'||sbg_vdba.qbo_spm.find_primary_dc('&4');
    dbms_output.put_line('Not AWS:'||v_is_aws);
  END IF;
  dbms_output.enable;
  dbms_output.put_line('Source DBL:'||v_source_cluster_dbl);
  dbms_output.put_line('Target DBL:'||v_target_cluster_dbl);
  -- export sql plan
  dbms_output.put_line('Exporting sql plan for sqlid ['||v_sqlid||']');
  dbms_output.put_line('BEGIN sbg_vdba.qbo_spm.export_plan_awr@'||v_source_cluster_dbl||'(:sql_id,:begin_snap,:end_snap); END;');  
  execute immediate 'BEGIN sbg_vdba.qbo_spm.export_plan_awr@'||v_source_cluster_dbl||'(:sql_id,:begin_snap,:end_snap); END;' using in out v_sqlid, v_begin_snap, v_end_snap;
  -- import plan
  dbms_output.put_line('SQL plan export complated!');
  dbms_output.put_line('Importing sql plan for sqlid ['||v_sqlid||']');
  --check whether to import same or different db
  IF v_source_cluster_dbl=v_target_cluster_dbl THEN
    execute immediate 'BEGIN sbg_vdba.qbo_spm.import_plan@'||v_target_cluster_dbl||'(:sqlid,:planhash,:sourcedbl); END;' using v_sqlid, v_planhash, v_source_cluster_dbl;
    dbms_output.put_line('SQL plan import complated!');
  ELSE
    execute immediate 'BEGIN sbg_vdba.qbo_spm.import_plan_across_db@'||v_target_cluster_dbl||'(:sqlid,:planhash,:sourcedbl); END;' using v_sqlid, v_planhash, v_source_cluster_dbl;
    dbms_output.put_line('SQL plan import complated!');
  END IF;
  -- print success
  dbms_output.put_line('Export/Import of sql plan completed for sqlid ['||v_sqlid||'] plan hash ['||v_planhash||']' );
END;
/
spool off
exit

