#!/bin/ksh
. /l/pspprod1

#
# report_long_running_queries.sh
#
# Generate a report once per hour containing
# sql_id, elapsed time, wait times and query text
# for queries running longer than 60 seconds
#
# Reported times computed from start time of each
# reported query
#
# ddf Wed Apr 27 08:14:06 Pacific Daylight Time 2011
#

#
# Determine if database and/or login credentials
# have been passed to the script
#

if [ $# -eq 3 ]      # Login and DB passed
then
	LOGIN="$1/$2"
	DB=$3
elif [ $# -eq 2 ]	# Login passed
then
	LOGIN="$1/$2"
	DB="$ORACLE_SID"
elif [ $# -eq 1 ]	# DB passed
then
	LOGIN="system/<some password here>"
	DB=$1
else			# Nothing passed
	LOGIN="system/<some password here>"
	DB="$ORACLE_SID"
fi

#
# Set recipient list
#

MAILLIST="allen_chaves@intuit.com raffi_norian@intuit.com david_fitzjarrell@intuit.com ken_paul@intuit.com"

#
# Find sqlplus
#

SQL=`which sqlplus 2>/dev/null`

if [ "$SQL" == "" ]
then
	echo "Cannot find sqlplus"
	echo "Check that ORACLE_HOME and PATH are correctly set"
	echo "Terminating"
	exit 4
fi

#
# Setup directories
#

export SCRIPT_DIR="/scripts/oracle"
export LOG_DIR="/scripts/oracle"

#
# Execute the query
#
cd ${SCRIPT_DIR}
##sqlplus -L -s $LOGIN@$DB <<EOF >>/dev/null
sqlplus  -s /nolog <<EOF >>/dev/null
connect / as sysdba
whenever sqlerror exit sql.sqlcode
set serveroutput on size 1000000 linesize 200 trimspool on feedback off

spool long_query_time.rpt

--
-- Elapsed time report for long-running queries
--
-- ddf Fri Apr 22 15:16:34 Pacific Daylight Time 2011
--

declare
        --
        -- Necessary variables
        --
        v_e_snapid dba_hist_snapshot.snap_id%type;
        v_curr_elapsed  dba_hist_sqlstat.elapsed_time_delta%type;
        v_curr_io  dba_hist_sqlstat.elapsed_time_delta%type;
        v_curr_ap  dba_hist_sqlstat.elapsed_time_delta%type;
        v_curr_cl  dba_hist_sqlstat.elapsed_time_delta%type;
        v_curr_cc  dba_hist_sqlstat.elapsed_time_delta%type;
        v_curr_exec  dba_hist_sqlstat.executions_delta%type;
        v_imm_elapsed  dba_hist_sqlstat.executions_delta%type;
        v_sqltxt  varchar2(32767);
        v_txt_ind number:=1;
        v_qry_ind number:=0;
        v_elap_tmp number:=0;

        --
        -- Cursor to get required data
        --
        cursor get_start_snap is
        select distinct a.sql_id, h.snap_id, a.inst_id
        from dba_hist_snapshot h, gv\$sqlarea a
        where to_timestamp(a.last_load_time) between h.begin_interval_time and h.end_interval_time
        and a.inst_id = h.instance_number
        and a.parsing_schema_name like 'PSP%';

begin

        --
        -- Output report header
        --
        dbms_output.put_line('SQL_ID        INST ELAPSED  IO WAIT  CC WAIT  AP WAIT  CL WAIT ');
        dbms_output.put_line('============= ==== ======== ======== ======== ======== ========');
        --
        -- Start processing
        --
        for snaprec in get_start_snap loop

                --
                -- Get maximum snap id for the given SQL_ID
                --
                begin
                select max(snap_id) maxsnap
                into v_e_snapid
                from dba_hist_sqlstat
                where sql_id = snaprec.sql_id
                and instance_number = snaprec.inst_id;
                exception
                when others then
                if SQLCODE=-1422 then
                        v_e_snapid := snaprec.snap_id;
                end if;
                end;

                --
                -- Return sum of elapsed deltas and execution deltas
                -- so an accurate elapsed delta can be computed
                --
                -- Also include sums for all of the reported wait metrics
                --
                begin
                select sum(elapsed_time_delta)/1000000 sum_elap, sum(executions_delta) sum_exec,
                       sum(iowait_delta)/1000000 sum_io, sum(ccwait_delta)/1000000 sum_cc,
                       sum(clwait_delta)/1000000 sum_cl, sum(apwait_delta)/1000000 sum_ap
                into v_curr_elapsed, v_curr_exec, v_curr_io, v_curr_cc, v_curr_cl, v_curr_ap
                from dba_hist_sqlstat
                where snap_id between snaprec.snap_id and v_e_snapid
                and sql_id = snaprec.sql_id
                and instance_number = snaprec.inst_id;
                exception
                when others then
                if SQLCODE=-1422 then
                        v_curr_elapsed:=1;
                        v_curr_exec:=1;
                end if;
                end;

                --
                -- Return the current window's elapsed time per execution
                -- for the given sql_id to be used as a trigger for output generation
                --
                begin
                select round(((elapsed_time_delta/decode(executions_delta,0,1,executions_delta))/1000000),2) immelap
                into v_imm_elapsed
                from dba_hist_sqlstat
                where snap_id = v_e_snapid
                and sql_id = snaprec.sql_id
                and instance_number = snaprec.inst_id;
                exception
                when no_data_found then
                        v_imm_elapsed:=1;
                when others then

                --
                -- Due to RAC some data can be 'duplicated'
                -- These rows are ignored
                --
                if SQLCODE=-1422 then
                        v_imm_elapsed:=1;
                end if;
                end;

                --
                -- Set current executions to 1 for
                -- a first-time execution for a query
                --
                -- Oracle does not increment the EXECUTIONS counter
                -- until a successful execution has completed
                --
                if v_curr_exec = 0 then
                        v_curr_exec := 1;
                end if;

                --
                -- Get the associated sql text if it exists
                --
                -- Set another display indicator if no text available
                --
                begin
                        select sql_text
                        into v_sqltxt
                        from dba_hist_sqltext
                        where sql_id = snaprec.sql_id;
                exception
                when no_data_found then
                        v_txt_ind := 0;
                when others then
                if SQLCODE=-1422 then
                        v_txt_ind := 0;
                end if;
                end;

                --
                -- Get current execution time for the query from gv$sql_plan, if possible
                --
                begin
                select nvl(round((sysdate - last_load_time)*86400,2),0)
                into v_elap_tmp
                from gv\$sqlarea
                where sql_id = snaprec.sql_id
                and inst_id = snaprec.inst_id;
                exception
                when no_data_found then
                        v_elap_tmp := 0;
                end;

                --
                -- Check all required indicators
                --
                -- If all conditions are met then display the row
                --
                if v_imm_elapsed >= 60 and v_txt_ind = 1 and nvl(v_curr_elapsed/v_curr_exec, 0) > 60 then
                        if v_elap_tmp < (v_curr_elapsed/v_curr_exec) and v_elap_tmp > 0 then
                        dbms_output.put_line(snaprec.sql_id||'  '||lpad(snaprec.inst_id,3)||' '||lpad(v_elap_tmp,8)||' '||lpad(round((v_curr_io/(v_curr_exec+1)), 2),8)||' '||lpad(round((v_curr_cc/(v_curr_exec+1)),2),8)||' '||lpad(round((v_curr_ap/(v_curr_exec+1)),2),8)||' '||lpad(round((v_curr_cl/(v_curr_exec+1)),2),8));
                        else
                        dbms_output.put_line(snaprec.sql_id||'  '||lpad(snaprec.inst_id,3)||' '||lpad(round((v_curr_elapsed/v_curr_exec),2),8)||' '||lpad(round((v_curr_io/v_curr_exec), 2),8)||' '||lpad(round((v_curr_cc/v_curr_exec),2),8)||' '||lpad(round((v_curr_ap/v_curr_exec),2),8)||' '||lpad(round((v_curr_cl/v_curr_exec),2),8));
                        end if;
                        dbms_output.put_line('SQL Text: '||v_sqltxt||chr(10));
                        v_qry_ind := 1;
                elsif v_txt_ind = 0 then
                        v_txt_ind := 1;
                end if;

        end loop;

        --
        -- If no data is returned don't display the  header
        --
        if v_qry_ind = 0 then
                dbms_output.disable;
        end if;

exception
        --
        -- NO_DATA_FOUND so just exit quietly
        --
        when no_data_found then
                null;
                dbms_output.disable;
        --
        -- Some other dastardly error occurred
        --
        -- Display what happened and where
        --
        when others then
                -- Output actual line number of error source
                dbms_output.put(dbms_utility.format_error_backtrace);
                -- Output the actual error number and message
                dbms_output.put_line(dbms_utility.format_error_stack);

end;
/

spool off
EOF

#
# Check exit status of sqlplus
#

status=$?

#
# Take appropriate action
#

if [ $status -ne 0 ]
then
	#
	# sqlplus failed, notify user and exit
	#

	echo "SQL*Plus failed to run"
	echo "Check login, password and database for errors"
	echo "Usage case 1: `basename $0` username password database"
	echo "Usage case 2: `basename $0` username password"
	echo "Usage case 3: `basename $0` database"
	exit 9
else
	#
	# Check that the spool file has text
	#
	# If so send it to the recipient list
	#

	if [ -s long_query_time.rpt ]
	then
		mailx -s "Query duration report -- `date`" $MAILLIST < long_query_time.rpt
		exit 0
	fi
fi


