create table sbg_vdba.spm_sql_id_stats as
select 'C01' cluster_id,
       sql_id,
       plan_hash_value ,
       sql_profile,
       sql_plan_baseline,
       executions,
       rows_processed avg_rows_returned,
       round(elapsed_time/1000/executions/1000,4) elapsed_time_per_exec ,
       round(disk_reads/executions) avg_pio,
       round (buffer_gets / executions) avg_lio,
       round (cpu_time / executions / 1000/1000,4) avg_cpu_time_per_exec,
       round(concurrency_wait_time/1000/executions/1000,4) concurrency_wait_time,
       round(user_io_wait_time/1000/executions/1000,4) user_io_wait_time,
       command_type,
       last_active_time,
       sysdate create_date
  from gv$sql
  where 1=2 ;

-- 2,3,6,7, 189 (I, S, U, D,UPSERT)

alter table sbg_vdba.spm_sql_id_stats modify (cluster_id varchar2(10));
alter table sbg_vdba.spm_sql_id_stats add (run_id number);
alter table sbg_vdba.spm_sql_id_stats add (sql_id_handle varchar2(512));
create index sbg_vdba.spm_sql_id_stats_sql_id_run_id on spm_sql_id_stats(sql_id, run_id);
create table sbg_vdba.spm_qbo_clusters
             (
             cluster_id varchar2(10),
             cluster_sid_qcy varchar2(10),
             cluster_sid_las varchar2(10),
             cluster_type varchar2(10));
alter table sbg_vdba.spm_qbo_clusters add (cluster_sid_uw2 varchar2(10));
alter table sbg_vdba.spm_qbo_clusters add (cluster_ip_qcy varchar2(30),
             cluster_ip_las varchar2(30));
delete sbg_vdba.spm_qbo_clusters;
--insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('PPD','pspqpd01','psplpd01', '?');
--insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('PPF','pspqpf01','psplpf01', '?');

insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C1','qboqpd01','qbolpd01', 'qbopp001');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C2','qboqpd02','qbolpd02', 'qbopp002');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C3','qboqpd03','qbolpd03', 'qbopp003');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C4','qboqpd04','qbolpd04', 'qbopp004');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C5','qboqpd05','qbolpd05', 'qbopp005');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C6','qboqpd06','qbolpd06', 'qbopp006');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C7','qboqpd07','qbolpd07', 'qbopp007');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C8','qboqpd08','qbolpd08', 'qbopp008');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C9','qboqpd09','qbolpd09', 'qbopp009');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C10','qboqpd10','qbolpd10', 'qbopp010');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C19','qboqpd19','qbolpd19', 'qbopp019');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C11','qboqpd11','qbolpd11', 'qbopp011');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C12','qboqpd12','qbolpd12', 'qbopp012');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C13','qboqpd13','qbolpd13', 'qbopp013');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C14','qboqpd14','qbolpd14', 'qbopp014');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C15','qboqpd15','qbolpd15', 'qbopp015');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C16','qboqpd16','qbolpd16', 'qbopp016');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C17','qboqpd17','qbolpd17', 'qbopp017');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C18','qboqpd18','qbolpd18', 'qbopp018');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C20','qboqpd20','qbolpd20', 'qbopp020');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C21','qboqpd21','qbolpd21', 'qbopp021');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C22','qboqpd22','qbolpd22', 'qbopp022');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C23','qboqpd23','qbolpd23', 'qbopp023');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C24','qboqpd24','qbolpd24', 'qbopp024');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C25','qboqpd25','qbolpd25', 'qbopp025');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C26','qboqpd26','qbolpd26', 'qbopp026');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C27','qboqpd27','qbolpd27', 'qbopp027');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C28','qboqpd28','qbolpd28', 'qbopp028');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C29','qboqpd29','qbolpd29', 'qbopp029');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C30','qboqpd30','qbolpd30', 'qbopp030');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C31','qboqpd31','qbolpd31', 'qbopp031');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C32','qboqpd32','qbolpd32', 'qbopp032');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C33','qboqpd33','qbolpd33', 'qbopp033');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C34','qboqpd34','qbolpd34', 'qbopp034');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C35','qboqpd35','qbolpd35', 'qbopp035');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C36','qboqpd36','qbolpd36', 'qbopp036');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C37','qboqpd37','qbolpd37', 'qbopp037');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C38','qboqpd38','qbolpd38', 'qbopp038');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C39','qboqpd39','qbolpd39', 'qbopp039');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C40','qboqpd40','qbolpd40', 'qbopp040');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C41','qboqpd41','qbolpd41', 'qbopp041');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C42','qboqpd42','qbolpd42', 'qbopp042');
delete sbg_vdba.spm_qbo_clusters where cluster_id='C43';
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C43','qboqpd43','qbolpd43', 'qbopp043');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C70','qboqpd70','qbolpd70', 'qbopp070');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C71','qboqpd71','qbolpd71', 'qbopp071');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C72','qboqpd72','qbolpd72', 'qbopp072');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C73','qboqpd73','qbolpd73', 'qbopp073');
insert into sbg_vdba.spm_qbo_clusters (cluster_id,cluster_sid_qcy,cluster_sid_las,cluster_sid_uw2) values ('C60',null,null, 'qbopp060');

update sbg_vdba.spm_qbo_clusters set cluster_sid_uw2='qbopp0'||lpad(to_number( substr(cluster_id,2)),2,'0') where cluster_sid_uw2 is null;
-- Update IP only in AWS

CREATE SEQUENCE sbg_vdba.spm_sql_id_stats_seq
  MINVALUE 1000000000
  START WITH 1000000000
  INCREMENT BY 1
  CACHE 100;
--**************************************************************
--* Package: QBO_SPM V1
--* Created: 06/22/16 sbg_vdba
--**************************************************************
CREATE OR REPLACE PACKAGE sbg_vdba.QBO_SPM
AUTHID CURRENT_USER
IS
    -- This package defines procedures and functions to be used to collect
    -- SQL performance stats and Sql Plan Managment across QBO clusters
    PROCEDURE create_dbl;

    PROCEDURE create_dbl(p_cluster_id IN varchar2);

    FUNCTION find_primary_dc(p_cluster_id IN VARCHAR2) RETURN VARCHAR2;

    FUNCTION find_primary_dc_aws(p_cluster_id IN VARCHAR2) RETURN VARCHAR2;

    PROCEDURE sql_id_stats(p_sql_id IN varchar2, p_sql_handle IN varchar2 DEFAULT 'N');

    PROCEDURE export_plan(p_sql_id varchar2);

    PROCEDURE import_plan(p_sql_id varchar2,
                          p_plan_hash_value number,
                          p_source_dbl_name varchar2);
END QBO_SPM;
/
CREATE OR REPLACE PACKAGE BODY sbg_vdba.QBO_SPM
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
                execute immediate 'create public database link SPM_'||i.cluster_id||'_QCY connect to sbg_vdba IDENTIFIED  BY "SbgV96#7er#t" using '''||v_tns||''' ';
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
                execute immediate 'create public database link SPM_'||i.cluster_id||'_LAS connect to sbg_vdba IDENTIFIED  BY "SbgV96#7er#t" using '''||v_tns||''' ';
            exception
                 when others then
                      null;
                      Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' LAS DBL (All Clusters)' || chr(10) || sqlerrm);
            end;
            -- create DBL in UW2
            begin
                v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||i.cluster_sid_uw2||'.sbg-qbo-prod.a.intuit.com)(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_uw2||')))';
                execute immediate 'create public database link SPM_'||i.cluster_id||'_UW2 connect to sbg_vdba IDENTIFIED  BY "SbgV96#7er#t" using '''||v_tns||''' ';
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
                execute immediate 'create public database link SPM_'||i.cluster_id||'_QCY connect to sbg_vdba IDENTIFIED  BY "SbgV96#7er#t" using '''||v_tns||''' ';
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
                execute immediate 'create public database link SPM_'||i.cluster_id||'_LAS connect to sbg_vdba IDENTIFIED  BY "SbgV96#7er#t" using '''||v_tns||''' ';
            exception
                 when others then
                      null;
                      Dbms_Output.Put_Line('Error in creating '||i.cluster_id||' LAS DBL (Single Cluster)' || chr(10) || sqlerrm);
            end;
            END IF;
            -- create DBL in UW2
            begin
              if i.cluster_sid_uw2 is not null then
                v_tns :='(DESCRIPTION = (ADDRESS=(PROTOCOL=TCP) (HOST='||i.cluster_sid_uw2||'.sbg-qbo-prod.a.intuit.com)(PORT=1521)) (CONNECT_DATA=(service_name = '||i.cluster_sid_uw2||')))';
                execute immediate 'create public database link SPM_'||i.cluster_id||'_UW2 connect to sbg_vdba IDENTIFIED  BY "SbgV96#7er#t" using '''||v_tns||''' ';
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
-- PROCEDURE: import_plan
---------------------------------------------------------------------------------------
PROCEDURE import_plan(p_sql_id varchar2,
                      p_plan_hash_value number,
                      p_source_dbl_name varchar2) AS
    --
      v_plans_loaded                pls_integer;
      v_user                      varchar2(30);
    v_sqlset_stgtab_name        varchar2(30);
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
           ' where table_name like ''SPM_''||upper('''||p_sql_id||''')||''%'''  into v_sqlset_stgtab_name ;
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
-- updte qcy ip
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.99' where cluster_id='C1';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.13.101' where cluster_id='C2';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.13.100' where cluster_id='C3';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.68' where cluster_id='C4';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.12.222' where cluster_id='C5';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.13.99' where cluster_id='C6';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.12.223' where cluster_id='C7';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.12.225' where cluster_id='C8';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.12.226' where cluster_id='C9';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.12.227' where cluster_id='C10';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.5.220' where cluster_id='C11';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.5.221' where cluster_id='C12';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.5.222' where cluster_id='C13';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.240' where cluster_id='C14';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.242' where cluster_id='C15';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.244' where cluster_id='C16';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.245' where cluster_id='C17';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.246' where cluster_id='C18';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.12.224' where cluster_id='C19';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.66' where cluster_id='C20';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.78' where cluster_id='C21';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.111' where cluster_id='C22';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.49' where cluster_id='C23';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.140' where cluster_id='C24';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.79' where cluster_id='C25';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.97' where cluster_id='C26';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.148' where cluster_id='C27';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.108' where cluster_id='C28';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.47' where cluster_id='C29';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.98' where cluster_id='C30';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.209' where cluster_id='C31';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.109' where cluster_id='C32';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.149' where cluster_id='C33';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.137' where cluster_id='C34';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.138' where cluster_id='C35';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.110' where cluster_id='C36';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.139' where cluster_id='C37';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.52' where cluster_id='C38';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.48' where cluster_id='C39';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.67' where cluster_id='C40';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.13.102' where cluster_id='C41';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.13.103' where cluster_id='C42';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.13.104' where cluster_id='C43';
-- update las ip
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.111' where cluster_id=    'C1';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.8.108' where cluster_id=    'C2';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.8.107' where cluster_id=    'C3';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.105' where cluster_id=    'C4';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.7.171' where cluster_id=    'C5';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.8.106' where cluster_id=    'C6';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.7.172' where cluster_id=    'C7';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.7.174' where cluster_id=    'C8';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.7.175' where cluster_id=    'C9';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.7.176' where cluster_id=    'C10';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.2.44' where cluster_id=    'C11';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.2.45' where cluster_id=    'C12';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.2.46' where cluster_id=    'C13';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.176' where cluster_id=    'C14';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.177' where cluster_id=    'C15';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.178' where cluster_id=    'C16';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.179' where cluster_id=    'C17';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.180' where cluster_id=    'C18';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.7.173' where cluster_id=    'C19';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.181' where cluster_id=    'C20';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.106' where cluster_id=    'C21';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.115' where cluster_id=    'C22';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.79' where cluster_id=    'C23';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.119' where cluster_id=    'C24';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.107' where cluster_id=    'C25';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.109' where cluster_id=    'C26';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.120' where cluster_id=    'C27';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.112' where cluster_id=    'C28';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.59' where cluster_id=    'C29';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.110' where cluster_id=    'C30';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.108' where cluster_id=    'C31';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.113' where cluster_id=    'C32';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.121' where cluster_id=    'C33';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.116' where cluster_id=    'C34';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.117' where cluster_id=    'C35';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.114' where cluster_id=    'C36';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.118' where cluster_id=    'C37';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.97' where cluster_id=    'C38';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.78' where cluster_id=    'C39';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.0.99' where cluster_id=    'C40';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.8.109' where cluster_id=    'C41';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.8.110' where cluster_id=    'C42';
update sbg_vdba.spm_qbo_clusters set cluster_ip_las='10.161.8.111' where cluster_id=    'C43';

commit;

create table sbg_vdba.qbo_cluster_top_sql as
select sql_id,
       plan_hash_value,
       sum(executions) EXEC,
       round (sum(buffer_gets)/sum(executions)) avg_LIO_per_exec,
       round(sum(disk_reads)/sum(executions)) avg_PIO_per_exec,
       round (sum(cpu_time)/sum(executions)/1000000,4) avg_cpu_tme_per_exec,
       round(sum(ELAPSED_TIME)/1000000/sum(EXECUTIONS),2) EL_PER_EXEC
  from v$sql
 where EXECUTIONS > 0
   and parsing_schema_name in ('QBO','QBO_DATA')
   and executions > 100
having round (sum(buffer_gets)/sum(executions)) > 100000
 group by sql_id,
          plan_hash_value  ;
alter table sbg_vdba.qbo_cluster_top_sql add (cluster_id varchar2(10), sql_handle varchar2(1024), load_date date, sample_date date);
create or replace procedure sbg_vdba.gen_qbo_cluster_top_sql (p_sample_date date default null, p_cluster_id varchar2 default null)
AUTHID CURRENT_USER
as

v_primary_dc  varchar2(10):='_QCY';

BEGIN
/*
create table sbg_vdba.qbo_cluster_top_sql as
select sql_id,
       plan_hash_value,
       sum(executions) EXEC,
       round (sum(buffer_gets)/sum(executions)) avg_LIO_per_exec,
       round(sum(disk_reads)/sum(executions)) avg_PIO_per_exec,
       round (sum(cpu_time)/sum(executions)/1000000,4) avg_cpu_tme_per_exec,
       round(sum(ELAPSED_TIME)/1000000/sum(EXECUTIONS),2) EL_PER_EXEC
  from v$sql
 where EXECUTIONS > 0
   and parsing_schema_name in ('QBO','QBO_DATA')
   and executions > 100
having round (sum(buffer_gets)/sum(executions)) > 100000
 group by sql_id,
          plan_hash_value  ;
alter table sbg_vdba.qbo_cluster_top_sql add (cluster_id varchar2(10), sql_handle varchar2(1024), load_date date, sample_date date);
*/
FOR i IN (select * from sbg_vdba.spm_qbo_clusters WHERE cluster_id=nvl(p_cluster_id, cluster_id) and CLUSTER_ID not in ('C60','C61')) LOOP

v_primary_dc :=qbo_spm.find_primary_dc_aws(i.cluster_id);

-- connect to primary site and get topn SQL
execute immediate ( '
insert into sbg_vdba.qbo_cluster_top_sql
       (sql_handle,
        sql_id,
        plan_hash_value,
        exec,
        avg_lio_per_exec,
        avg_pio_per_exec,
        avg_cpu_tme_per_exec,
        el_per_exec,
        cluster_id,
        load_date,
        sample_date)
select substr(sql_text, instr(sql_text,''/*'',1, 1), instr(sql_text,''*/'',1, 1)+1),
       x.sql_id,
       plan_hash_value,
       sum(executions_delta) EXEC,
       round(sum(buffer_gets_delta)/sum(executions_delta)) avg_LIO_per_exec,
       round(sum(disk_reads_delta)/sum(executions_delta)) avg_PIO_per_exec,
       round(sum(cpu_time_delta)/sum(executions_delta)/1000000,4) avg_cpu_tme_per_exec,
       round(sum(elapsed_time_delta)/1000000/sum(executions_delta),2) el_per_exec,
       '''||i.cluster_id||''',
       sysdate,
       '''||nvl(p_sample_date,trunc(sysdate-1))||'''
  from dba_hist_sqlstat@'||'SPM_'||i.cluster_id||v_primary_dc||'  x,
       (select sql_id, to_char(substr(sql_text,1,2048)) sql_text 
          from gv$sqlarea@'||'SPM_'||i.cluster_id||v_primary_dc||' ) y,
       dba_hist_snapshot@'||'SPM_'||i.cluster_id||v_primary_dc||' z
 where x.sql_id = y.sql_id
   and x.snap_id= z.snap_id
   and x.executions_delta > 0
   and x.parsing_schema_name like (''QBO%'')
   and trunc(begin_interval_time) = nvl('''||p_sample_date||''',trunc(sysdate-1))
-- having round (sum(buffer_gets_delta)/sum(executions_delta)) > 100000 -- IO
--   having round(sum(elapsed_time_delta)/1000000/sum(executions_delta),2) > 0.5 /* Elapsed time*/ and sum(executions_delta) > 100
 group by substr(sql_text, instr(sql_text,''/*'',1, 1), instr(sql_text,''*/'',1, 1)+1), x.sql_id,
          plan_hash_value
');
dbms_output.put_line('# of SQL collected from '||i.cluster_id||' :- '||sql%rowcount);
commit;
END LOOP;
END;
/
  CREATE TABLE sbg_vdba."QBO_CLUSTER_SQL_BASELINE"
   (    "CLUSTER_ID" VARCHAR2(10 CHAR),
    "SQL_ID" VARCHAR2(30 CHAR),
    "SQL_PLAN_BASELINE" VARCHAR2(120 CHAR),
    "CREATE_DATE" DATE,
    "SQL_T" VARCHAR2(1000 CHAR),
    "SQL_HANDLE" VARCHAR2(1024 CHAR)
   ) SEGMENT CREATION IMMEDIATE
 COMPRESS LOGGING
  TABLESPACE "USERS"
/
create or replace procedure sbg_vdba.gen_qbo_cluster_sql_baseline(p_cluster_id varchar2 default null)
AUTHID CURRENT_USER
as

v_primary_dc  varchar2(10):='_QCY';

BEGIN

FOR i IN (select * from sbg_vdba.spm_qbo_clusters WHERE cluster_id=nvl(p_cluster_id, cluster_id) and CLUSTER_ID not in ('C60','C61')) LOOP
--FOR i IN (select * from sbg_vdba.spm_qbo_clusters WHERE CLUSTER_TYPE='AWS') LOOP
v_primary_dc :=qbo_spm.find_primary_dc_aws(i.cluster_id);

-- connect to primary site and get topn SQL
execute immediate ( '
insert into sbg_vdba.qbo_cluster_sql_baseline
( cluster_id, sql_id, sql_plan_baseline, create_date, sql_handle)
 select  '''||i.cluster_id||''', sql_id, sql_plan_baseline, sysdate, sql_handle
  from (select distinct sql_id, sql_plan_baseline, substr(sql_text, instr(sql_text,''/*'',1, 1), instr(sql_text,''*/'',1, 1)+1) sql_handle from v$sql@'||'SPM_'||i.cluster_id||v_primary_dc||' a  where sql_plan_baseline is not null
       ) ');
dbms_output.put_line('# of SQL collected from '||i.cluster_id||' :- '||sql%rowcount);
commit;
END LOOP;
END;
/
create or replace procedure sbg_vdba.nightly_job_proc
AUTHID CURRENT_USER
as
begin
sbg_vdba.gen_qbo_cluster_sql_baseline;
sbg_vdba.gen_qbo_cluster_top_sql;
end;
/
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.15.56', cluster_ip_las='10.161.9.133' where cluster_id='C70';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.246', cluster_ip_las='10.161.7.175' where cluster_id='C71';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.66', cluster_ip_las='10.161.7.176' where cluster_id='C72';
update sbg_vdba.spm_qbo_clusters set cluster_ip_qcy='10.143.0.78', cluster_ip_las='10.161.2.44' where cluster_id='C73';
/*
exec dbms_scheduler.drop_job('SBG_VDBA_NIGHTLY_JOB');

BEGIN
dbms_scheduler.create_job(
job_name => 'SBG_VDBA.SBG_VDBA_NIGHTLY_JOB',
job_type => 'PLSQL_BLOCK',
job_action => 'SBG_VDBA.NIGHTLY_JOB_PROC;',
repeat_interval => 'FREQ=DAILY; BYHOUR=01; BYMINUTE=00;',
start_date => systimestamp at time zone 'America/Los_Angeles',
job_class => '"DEFAULT_JOB_CLASS"',
comments => 'SBG VDBA Nightly jobs',
auto_drop => FALSE,
enabled => TRUE);
END;
/
exec dbms_scheduler.run_job('SBG_VDBA.SBG_VDBA_NIGHTLY_JOB');
*/
create table sbg_vdba.qbo_cluster_capacity
      (cluster_id varchar2(15),
       load_date date,
       company_size_0kto1k number,
       company_size_1kto10k number,
       company_size_10kto20k number,
       company_size_20kto30k number,
       company_size_30kto40k number,
       company_size_40kto50k number,
       company_size_50kto60k number,
       company_size_60kto70k number,
       company_size_70kto80k number,
       company_size_80kto90k number,
       company_size_90kto100k number,
       company_size_L number,
       company_size_XL number,
       company_size_XXL number,
       company_count number);
 
create unique index sbg_vdba.qbo_cluster_capacity_pk on sbg_vdba.qbo_cluster_capacity
(cluster_id,load_date);
 
ALTER TABLE sbg_vdba.qbo_cluster_capacity ADD (
  CONSTRAINT qbo_cluster_capacity_pk
  PRIMARY KEY
  (cluster_id,load_date)
  USING INDEX sbg_vdba.qbo_cluster_capacity_pk);        
  alter table sbg_vdba.qbo_cluster_capacity add (COMPANY_ACTIVE NUMBER, CLUSTER_SIZE_GB NUMBER);

create or replace procedure sbg_vdba.gen_qbo_capacity (p_cluster_id varchar2 default null) as
 
v_primary_dc  varchar2(10):='_QCY';
 
BEGIN
FOR i IN (select * from sbg_vdba.spm_qbo_clusters WHERE cluster_id=nvl(p_cluster_id, cluster_id) and CLUSTER_ID not in ('C60','C61')) LOOP
 
v_primary_dc :=qbo_spm.find_primary_dc(i. cluster_id);
 
-- connect to primary site and get cluster capacity

execute immediate ( '
insert into sbg_vdba.qbo_cluster_capacity 
       (cluster_id,
       load_date,
       company_size_0kto1k,
       company_size_1kto10k,
       company_size_10kto20k,
       company_size_20kto30k,
       company_size_30kto40k,
       company_size_40kto50k,
       company_size_50kto60k,
       company_size_60kto70k,
       company_size_70kto80k,
       company_size_80kto90k,
       company_size_90kto100k,
       company_size_L,
       company_size_XL,
       company_size_XXL,
       company_count)
select '''||i.cluster_id||''',
       trunc(sysdate) load_date,
       SUM(case when company_type=''1k'' then 1 else 0 end) company_size_0kto1k,
       SUM(case when company_type=''10k'' then 1 else 0 end) company_size_1kto10k,
       SUM(case when company_type=''20k'' then 1 else 0 end) company_size_10kto20k,
       SUM(case when company_type=''30k'' then 1 else 0 end) company_size_20kto30k,
       SUM(case when company_type=''40k'' then 1 else 0 end) company_size_30kto40k,
       SUM(case when company_type=''50k'' then 1 else 0 end) company_size_40kto50k,
       SUM(case when company_type=''60k'' then 1 else 0 end) company_size_50kto60k,
       SUM(case when company_type=''70k'' then 1 else 0 end) company_size_60kto70k,
       SUM(case when company_type=''80k'' then 1 else 0 end) company_size_70kto80k,
       SUM(case when company_type=''90k'' then 1 else 0 end) company_size_80kto90k,
       SUM(case when company_type=''100k'' then 1 else 0 end) company_size_90kto100k,
       SUM(case when company_type=''L'' then 1 else 0 end) company_size_L,
       SUM(case when company_type=''XL'' then 1 else 0 end) company_size_XL,
       SUM(case when company_type=''XXL'' then 1 else 0 end) company_size_XXL,
       null
FROM   (SELECT company_id,
         (CASE
             WHEN z.company_size BETWEEN 1 AND 1000 THEN ''1k''
             WHEN z.company_size BETWEEN 1001 AND 10000 THEN ''10k''
             WHEN z.company_size BETWEEN 10001 AND 20000 THEN ''20k''
             WHEN z.company_size BETWEEN 20001 AND 30000 THEN ''30k''
             WHEN z.company_size BETWEEN 30001 AND 40000 THEN ''40k''
             WHEN z.company_size BETWEEN 40001 AND 50000 THEN ''50k''
             WHEN z.company_size BETWEEN 50001 AND 60000 THEN ''60k''
             WHEN z.company_size BETWEEN 60001 AND 70000 THEN ''70k''
             WHEN z.company_size BETWEEN 70001 AND 80000 THEN ''80k''
             WHEN z.company_size BETWEEN 80001 AND 90000 THEN ''90k''
             WHEN z.company_size BETWEEN 90001 AND 100000 THEN ''100k''            
             WHEN z.company_size BETWEEN 100001 AND 150000 THEN ''L''
             WHEN z.company_size BETWEEN 150001 AND 200000 THEN ''XL''
             WHEN z.company_size > 200000 THEN ''XXL''
          END)
            AS Company_type
    FROM (  SELECT /*+DRIVING_SITE(a) parallel(8) */ a.company_id, COUNT (*) AS company_size
              FROM qbo_data.txdetails_1@'||'SPM_'||i.cluster_id||v_primary_dc||' a, qbo.companies_1@'||'SPM_'||i.cluster_id||v_primary_dc||' b
             WHERE a.company_id = b.company_id
               AND b.company_status IN (0, 128)
          GROUP BY a.company_id) z) ');
         
execute immediate ( ' 
update sbg_vdba.qbo_cluster_capacity
   set company_active = (select count(1) from qbo.companies_1@'||'SPM_'||i.cluster_id||v_primary_dc||' WHERE company_status IN (0, 128))
 where cluster_id='''||i.cluster_id||'''
   and trunc(load_date)= trunc(sysdate) ');     

execute immediate ( ' 
update sbg_vdba.qbo_cluster_capacity
   set cluster_size_gb = (select round(sum(bytes)/1024/1024/1024) from dba_segments@'||'SPM_'||i.cluster_id||v_primary_dc||' )
 where cluster_id='''||i.cluster_id||'''
   and trunc(load_date)= trunc(sysdate) ');            
--dbms_output.put_line('# of SQL collected from '||i.cluster_id||' :- '||sql%rowcount);
commit;
END LOOP;
END;
/
/*
create or replace procedure sbg_vdba.gen_qbo_db_table_footprint (p_cluster_id varchar2 default null) as

v_primary_dc  varchar2(10):='_QCY';

BEGIN

create table sbg_vdba.db_table_footprint as
   select substr(segment_name,1,10) cluster_id,
          segment_name table_name,
          sum(bytes)/1024/1024/1024 tab_size_gb,
          sum(bytes)/1024/1024/1024 ind_size_gb,
          sum(bytes)/1024/1024/1024 lob_size_gb          
     from dba_segments s1
    where segment_type like '%TABLE%'
      and 1=2
    group by segment_name;

FOR i IN (select * from sbg_vdba.spm_qbo_clusters WHERE cluster_id=nvl(p_cluster_id, cluster_id) and CLUSTER_ID not in ('C60','C61')) LOOP

    v_primary_dc :=qbo_spm.find_primary_dc_aws(i.cluster_id);
    
    execute immediate ' insert into sbg_vdba.db_table_footprint 
                        select '''||i.cluster_id||''',
                               segment_name              table_name,
                               sum(bytes)/1024/1024/1024 tab_size_gb,
                               null ind_size_gb,
                               null lob_size_gb
                          from dba_segments@'||'SPM_'||i.cluster_id||v_primary_dc||' s1
                         where segment_type like ''%TABLE%''
                           and owner in (''QBO'',''QBO_DATA'')
                        having sum(bytes)/1024/1024/1024 > 10  
                         group by segment_name ';
    
    commit;
    for j in (select * from sbg_vdba.db_table_footprint WHERE cluster_id=i.cluster_id) loop
    execute immediate ' update sbg_vdba.db_table_footprint x
                           set ind_size_gb=(select sum(bytes)/1024/1024/1024
                                              from dba_segments@'||'SPM_'||i.cluster_id||v_primary_dc||' ind2
                                             where ind2.owner IN (''QBO'',''QBO_DATA'')
                                               and segment_name in (select index_name
                                                                      from dba_indexes@'||'SPM_'||i.cluster_id||v_primary_dc||' ind
                                                                     where ind.owner      = ind2.owner
                                                                       and ind.table_name = x.table_name
                                                                       and ind.table_name ='''||j.table_name||''' ))
                        where cluster_id = '''||i.cluster_id||''' 
                          and table_name ='''||j.table_name||''' ';
    commit;
    end loop;                                                                   
    for k in (select * from sbg_vdba.db_table_footprint WHERE cluster_id=i.cluster_id) loop
    execute immediate ' update sbg_vdba.db_table_footprint x
                           set lob_size_gb=(select sum(bytes)/1024/1024/1204
                                              from dba_segments@'||'SPM_'||i.cluster_id||v_primary_dc||' lob2
                                             where owner IN (''QBO'',''QBO_DATA'')
                                               and segment_name in (select segment_name 
                                                                      from dba_lobs@'||'SPM_'||i.cluster_id||v_primary_dc||' lob 
                                                                     where lob.owner = lob2.owner
                                                                       and lob.TABLE_NAME= x.table_name
                                                                       and lob.table_name ='''||k.table_name||''' ))
                        where cluster_id = '''||i.cluster_id||''' 
                          and table_name ='''||k.table_name||''' ';
    commit;
    end loop;
END LOOP;

END;
*/
exit
