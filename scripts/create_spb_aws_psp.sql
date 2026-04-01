spool /u01/scripts/LOG/&5
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
  --
BEGIN
  --
  dbms_output.disable;
  v_sqlid     :='&1';  
  v_planhash  :='&2';
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
    v_source_cluster_dbl:= 'SPM_'||'&3'||ops_user.qbo_spm.find_primary_dc('&3');
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
    v_target_cluster_dbl:= 'SPM_'||'&4'||ops_user.qbo_spm.find_primary_dc('&4');
    dbms_output.put_line('Not AWS:'||v_is_aws);      
  END IF;
  dbms_output.enable;
  dbms_output.put_line('Source DBL:'||v_source_cluster_dbl);  
  dbms_output.put_line('Target DBL:'||v_target_cluster_dbl);
  -- export sql plan
  dbms_output.put_line('Exporting sql plan for sqlid ['||v_sqlid||']');  
  execute immediate 'BEGIN ops_user.qbo_spm.export_plan@'||v_source_cluster_dbl||'(:sqlid); END;' using in out v_sqlid;  
  -- import plan
  dbms_output.put_line('SQL plan export complated!');
  dbms_output.put_line('Importing sql plan for sqlid ['||v_sqlid||']');
  -- calling this to create DBL
  execute immediate 'BEGIN ops_user.qbo_spm.import_plan@'||v_target_cluster_dbl||'(:sqlid,:planhash,:sourcedbl); END;' using v_sqlid, v_planhash, v_source_cluster_dbl;   
  dbms_output.put_line('SQL plan import complated!');
  -- print success
  dbms_output.put_line('Export/Import of sql plan completed for sqlid ['||v_sqlid||'] plan hash ['||v_planhash||']' );
END;
/
spool off
exit

