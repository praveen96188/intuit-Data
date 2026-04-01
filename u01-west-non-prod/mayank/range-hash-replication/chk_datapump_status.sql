/*
*
*  Author    : Vishal Gupta
*  Purpose   : Display datapump job status
*  Parameter : 1 - Job owner name , this could also be passed as OWNER.JOB_NAME
*              2 - Datapump Job Name
*
*  Revision History:
*  ===================
*  Date       Author        Description
*  ---------  ------------  ------------------------------------------------------------
*  12-FEB-16  Vishal Gupta  Added conditional display of days to duration field
*  23-SEP-14  Vishal Gupta  Added logic to auto-calculate max LPADING length for various outputs
*  14-FEB-13  Vishal Gupta  Added more details and some formating
*  17-Dec-12  Vishal Gupta  Modified to accept job owner and name separated by comma
*  26-Mar-12  Vishal Gupta  Created
*
*
*/

spool chk_datapump_status
set serveroutput on

VARIABLE owner VARCHAR2(30);
VARIABLE job_name VARCHAR2(30);

set term off
BEGIN
   :owner := upper('&&1');
   :job_name := upper('&&2');
   IF INSTR(:owner,'.') > 0 THEN
    :job_name := SUBSTR(:owner, INSTR(:owner,'.') + 1 ) ;
       :owner    := SUBSTR(:owner, 1 , INSTR(:owner,'.') - 1 ) ;
   END IF;
END;
/
set term on

DECLARE
  ind NUMBER;              -- Loop index
  h1 NUMBER;               -- Data Pump job handle
  percent_done NUMBER;     -- Percentage of job complete
  job_state VARCHAR2(30);  -- To keep track of job state
  sts ku$_Status;          -- The status object returned by get_status
  wip ku$_LogEntry;         -- WIP
  jd  ku$_JobDesc;          -- Job Description
  js  ku$_JobStatus;        -- The job status from get_status
  wsl ku$_WorkerStatusList; -- Worker status
  error ku$_LogEntry;

  max_completed_objects_length   NUMBER;
  max_completed_rows_length      NUMBER;
  max_completed_bytes_length      NUMBER;

  col_pad_len  NUMBER      := 50;
BEGIN
      h1 := DBMS_DATAPUMP.attach(job_name  => :job_name
                           ,  job_owner => CASE :owner WHEN '' THEN USER ELSE :owner END
         );

      /* Fetch Datapump Job Status */
      dbms_datapump.get_status( handle    => h1
                              , mask      =>   dbms_datapump.ku$_status_wip
                                             + dbms_datapump.ku$_status_job_desc
                                             + dbms_datapump.ku$_status_job_status
                                             + dbms_datapump.ku$_status_job_error
                              , timeout   => 0
                              , job_state => job_state
                              , status    => sts);

      wip := sts.wip;
      jd  := sts.job_description;
      js  := sts.job_status;
      wsl := js.worker_status_list;
      error := sts.error;


     dbms_output.put_line(' ' );
     dbms_output.put_line('------------------------------------------' );
     dbms_output.put_line('Datapump Job Details' );
     dbms_output.put_line('------------------------------------------' );
     dbms_output.put_line(RPAD('Job Owner     : ' || jd.owner,col_pad_len)        || RPAD('Remote Link     : ' || jd.remote_link,col_pad_len)      );
     dbms_output.put_line(RPAD('Job Name      : ' || jd.job_name,col_pad_len)     || RPAD('Exp Start Time  : ' || TO_CHAR(jd.exp_start_time,'DD-Mon-YY hh24:mi:ss'),col_pad_len)  );
     dbms_output.put_line(RPAD('Operation     : ' || jd.operation,col_pad_len)    || RPAD('Exp Global Name : ' || jd.exp_global_name,col_pad_len)  );
     dbms_output.put_line(RPAD('Job Mode      : ' || jd.job_mode,col_pad_len)     || RPAD('Exp Platform    : ' || jd.exp_platform,col_pad_len)     );
     dbms_output.put_line(RPAD('Max Degree    : ' || jd.max_degree,col_pad_len)   || RPAD('Exp DB Version  : ' || jd.exp_db_version,col_pad_len)   );
     dbms_output.put_line(RPAD('Instance      : ' || jd.instance,col_pad_len)     || RPAD('Creator Privs   : ' || jd.creator_privs,col_pad_len)    );
     dbms_output.put_line(RPAD('Global Name   : ' || jd.global_name,col_pad_len)  || RPAD('SCN             : ' || jd.scn,col_pad_len)              );
     dbms_output.put_line(RPAD('Platform      : ' || jd.platform,col_pad_len)     );
     dbms_output.put_line(RPAD('DB Version    : ' || jd.db_version,col_pad_len)   );
     dbms_output.put_line(RPAD('Job StartTime : ' || TO_CHAR(jd.start_time,'DD-Mon-YY hh24:mi:ss'),col_pad_len)    );
     dbms_output.put_line(RPAD('Duration      : ' ||    CASE WHEN FLOOR(sysdate - jd.start_time) > 0 THEN  FLOOR(sysdate - jd.start_time) || 'd ' ELSE '' END
                                                     || FLOOR(MOD((sysdate - jd.start_time) , 1) * 24 ) || 'h '
                                                     || FLOOR(MOD((sysdate - jd.start_time)  * 24 , 1) * 60 )  || 'm '
                                                     || FLOOR(MOD((sysdate - jd.start_time)  * 24 * 60 , 1) * 60 )  || 's '
                                                     ,col_pad_len)    );
     dbms_output.put_line(RPAD('Termin Reason : ' || jd.term_reason,col_pad_len) );
     dbms_output.put_line('Logfile       : ' || jd.log_file );
     dbms_output.put_line('SQL File      : ' || jd.sql_file   );

     ind := jd.params.first;
     while ind is not null
     loop
         dbms_output.put_line('Parameter ' || ind || '   :'
                                           || ' Op - ' || jd.params(ind).param_op
                                           || ', ' || RPAD(jd.params(ind).param_name,25)
                                           || ' = ' || NVL(jd.params(ind).param_value_t ,jd.params(ind).param_value_n)
                              );
         ind := jd.params.next(ind);
     end loop;

     dbms_output.put_line('------------------------------------------' );
     dbms_output.put_line('Datapump Job Status' );
     dbms_output.put_line('------------------------------------------' );
     dbms_output.put_line(RPAD('State         : ' || js.state,col_pad_len )  || RPAD('Total Bytes     : ' || to_char(js.total_bytes,'999,999,999,999,999'),col_pad_len )      );
     dbms_output.put_line(RPAD('Degree        : ' || js.degree,col_pad_len ) || RPAD('Bytes Processed : ' || to_char(js.bytes_processed,'999,999,999,999,999'),col_pad_len )  );
     dbms_output.put_line(RPAD('% Done        : ' || js.percent_done || ' %   <----- *****',col_pad_len )  || RPAD('Restart Count   : ' || js.restart_count,col_pad_len ) );
     dbms_output.put_line(RPAD('Phase         : ' || js.phase,col_pad_len ) || RPAD('Error Count     : ' || js.error_count,col_pad_len ));

     ind := js.files.first;
     dbms_output.put_line('DumpFiles     : ' || SUBSTR(js.files(ind).file_name,1,CASE INSTR(js.files(ind).file_name,'/',-1)
                                                                                   WHEN 0 THEN INSTR(js.files(ind).file_name,'\',-1) - 1
                                                                                   ELSE INSTR(js.files(ind).file_name,'/',-1) -1
                                                                                   END
                                                        )
                          );
     while ind is not null
     loop
         IF jd.operation = 'EXPORT' THEN
            dbms_output.put_line('.             : '
                            || SUBSTR(js.files(ind).file_name,INSTR(js.files(ind).file_name,'/',-1)+1)
                            || ' ( Size - ' || LPAD(to_char(ROUND(js.files(ind).file_bytes_written/1024/1024),'999,999,999'),13) || ' MB )'
                            );
         END IF;
         IF jd.operation = 'IMPORT' THEN
            -- Odd file number
            IF MOD(ind,3) = 1 THEN
                dbms_output.put('.             : ' || SUBSTR(js.files(ind).file_name,INSTR(js.files(ind).file_name,'/',-1)+1) );
            END IF;
            IF MOD(ind,3) = 2 THEN
                dbms_output.put(',    '  || SUBSTR(js.files(ind).file_name,INSTR(js.files(ind).file_name,'/',-1)+1)   );
            END IF;
            IF MOD(ind,3) = 0 THEN
                dbms_output.put_line(',    '  ||SUBSTR(js.files(ind).file_name,INSTR(js.files(ind).file_name,'/',-1)+1)   );
            END IF;
         END IF;
         ind := js.files.next(ind);
     end loop;

     dbms_output.put_line(' ');
     dbms_output.put_line('------------------------------------------' );
     dbms_output.put_line('Datapump Worker Status' );
     dbms_output.put_line('------------------------------------------' );

     max_completed_objects_length := 1;
     max_completed_rows_length := 1;
     max_completed_bytes_length := 1;
     ind := wsl.first;
     while ind is not null
     loop
        IF  LENGTH(wsl(ind).completed_objects) > max_completed_objects_length THEN
            max_completed_objects_length := LENGTH(wsl(ind).completed_objects);
        END IF;
        IF  LENGTH(ROUND(wsl(ind).completed_rows/1000)) > max_completed_rows_length THEN
            max_completed_rows_length := LENGTH(ROUND(wsl(ind).completed_rows/1000));
        END IF;
        IF  LENGTH(ROUND(wsl(ind).completed_bytes/1024/1024)) > max_completed_bytes_length THEN
            max_completed_bytes_length := LENGTH(ROUND(wsl(ind).completed_bytes/1024/1024));
        END IF;
        ind := wsl.next(ind);
     end loop;

     ind := wsl.first;
     while ind is not null
     loop

     /*
     percent_done := 0;
         BEGIN
             SELECT *
             into percent_done
             FROM (select ROUND(NVL(l.sofar / l.totalwork,0) *100,   2) pct_done
              from dba_datapump_sessions d
                   LEFT OUTER JOIN gv$session s ON s.inst_id = d.inst_id  AND s.saddr   = d.saddr
                   LEFT OUTER JOIN gv$process p ON s.inst_id = p.inst_id  AND s.paddr   = p.addr
                   LEFT OUTER JOIN gv$session_longops l ON l.inst_id = s.inst_id   AND l.sid  = s.sid
             WHERE d.owner_name = jd.owner
                AND d.job_name = jd.job_name
                AND d.session_type = 'WORKER'
                AND s.program like '%' || TRIM(wsl(ind).process_name) || '%'
               ORDER BY l.start_Time desc)
             WHERE ROWNUM=1
             ;
         EXCEPTION
             WHEN Others THEN
                NULL;
         END;
       */
         dbms_output.put_line('Worker ' || LPAD(wsl(ind).worker_number,2) || ' :'
                                        || ' '   || wsl(ind).process_name -- || '(Inst:' || wsl(ind).instance_id || ')'
                                        || ' , ' || LPAD(wsl(ind).percent_done,2) || '% Done'
                                        --|| ' ,' || LPAD(percent_done,6) || '% Done'
                                        || ' , Deg '  || wsl(ind).degree
                                        || ' , '  || RPAD(wsl(ind).state,9)
                                        || ' , Completed Objects '  || LPAD(wsl(ind).completed_objects,max_completed_objects_length) || '/' ||  TRIM(wsl(ind).total_objects)
                                        || ' , '  || LPAD(TRIM(to_char(wsl(ind).completed_rows/1000,'999999999')) , max_completed_rows_length) || ' K rows'
                                        || ' , Size ' || LPAD(TRIM(to_char(wsl(ind).completed_bytes/1024/1024,'99999999')), max_completed_bytes_length) || ' MB'
                                        || ' , ' || wsl(ind).schema || '.' || wsl(ind).name
                                        || CASE WHEN wsl(ind).partition IS NULL THEN '' ELSE '(' || wsl(ind).partition || ')' END
                                        || ' , ' || wsl(ind).object_type
         );
         ind := wsl.next(ind);
     end loop;
     DBMS_DATAPUMP.detach(h1);
end;
/

UNDEFINE 1
UNDEFINE 2

spool off


