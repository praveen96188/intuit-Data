. ~/.bash_profile
. /l/orcl

log_dir=./log

mkdir -p $log_dir

dbname=$RDS_SID

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`

typeset -i interval
typeset -i interval_d
typeset -i samples
typeset -i oldcpu
typeset -i waits
typeset -i total_waits
typeset -i waited_time
typeset -i total_waited_time
typeset -i wt_per_sec
typeset -i wt_percent
typeset -i tot_lines
typeset -i del_lines

days_kept_log=14

log_file=${log_dir}/db_performance_${dbname}.tmp
log_file_hist=${log_dir}/db_performance_${dbname}_`date +\%Y\%m\%d`.log
log_file1=${log_dir}/db_performance_${dbname}.tmp1
log_file1a=${log_dir}/db_performance_${dbname}.tmp1a
log_file1b=${log_dir}/db_performance_${dbname}.tmp1b
log_file1c=${log_dir}/db_performance_${dbname}.tmp1c
log_file2=${log_dir}/db_performance_${dbname}.tmp2
log_file2a=${log_dir}/db_performance_${dbname}.tmp2a
log_file2b=${log_dir}/db_performance_${dbname}.tmp2b
log_file2c=${log_dir}/db_performance_${dbname}.tmp2c
log_file3=${log_dir}/db_performance_${dbname}.tmp3

if [[ -f ${log_file} ]]; then
  rm ${log_file}
fi

if [[ -f ${log_file3} ]]; then
  rm ${log_file3}
fi

if [ -f ${log_file2} ]; then
  cp -f ${log_file2} ${log_file1}
fi

encryption_password=`head -1 /dev/shm/goldengate/db/password_file`
encryption_key=`tail -1 /dev/shm/goldengate/db/password_file`
#password=`echo $encryption_password| openssl aes-256-cbc -a -d -salt -k $encryption_key`
password=`echo Salsa!23`
login_string=ops_user/${password}@${RDS_SID}

sqlplus -s << EOF
$login_string
spool ${log_file2}
   whenever oserror exit sql.oscode
    whenever sqlerror exit sql.sqlcode
    set echo off
    set feed off
    set pagesize 1000
    set head off
    set linesize 160
    set numwidth 40

    select 'T!' || to_char(to_number(to_char(sysdate, 'J'))*24*60*60 +
       to_number(to_char(sysdate, 'HH24')) * 60 * 60 +
       to_number(to_char(sysdate, 'MI')) * 60 +
       to_number(to_char(sysdate, 'SS')))
      from dual;

    column value format 9999999999999999
    select 'L!' || name || '!' || value
    from v\$sysstat
    where class <> 6;

    column value format 9999999999999999
    select 'W!' || event || '!' || total_waits || '!' || time_waited
      from v\$system_event
      where event not in (select name from v\$event_name where wait_class = 'Idle')
    order by 1;
spool off
exit
EOF

if (( `grep "ORA-" ${log_file2} |wc -l` > 0 )); then
  echo "ERROR: Oracle error is in the output file ${log_file2}" |tee -a ${log_file}
  grep "ORA-" ${log_file2} |tee -a ${log_file}
  exit 1
fi

total_waits=0
total_waited_time=0

start_time=`grep "^T!" ${log_file1} |cut -d! -f2`
end_time=`grep "^T!" ${log_file2} |cut -d! -f2`

echo "start_time=$start_time"
echo "end_time=$end_time"

interval=`awk -v x=${start_time} -v y=${end_time} 'BEGIN {printf "%.0f\n", y-x}'`

STNAME[1]="Redo size"
STVAL1[1]=`grep "^L!redo size!" ${log_file1} |cut -d! -f3`
STVAL2[1]=`grep "^L!redo size!" ${log_file2} |cut -d! -f3`
STNAME[2]="Logical reads"
STVAL1[2]=`grep "^L!session logical reads!" ${log_file1} |cut -d! -f3`
STVAL2[2]=`grep "^L!session logical reads!" ${log_file2} |cut -d! -f3`
STNAME[3]="Block changes"
STVAL1[3]=`grep "^L!db block changes!" ${log_file1} |cut -d! -f3`
STVAL2[3]=`grep "^L!db block changes!" ${log_file2} |cut -d! -f3`
STNAME[4]="Physical reads"
STVAL1[4]=`grep "^L!physical reads!" ${log_file1} |cut -d! -f3`
STVAL2[4]=`grep "^L!physical reads!" ${log_file2} |cut -d! -f3`
STNAME[5]="Physical writes"
STVAL1[5]=`grep "^L!physical writes!" ${log_file1} |cut -d! -f3`
STVAL2[5]=`grep "^L!physical writes!" ${log_file2} |cut -d! -f3`
STNAME[6]="User calls"
STVAL1[6]=`grep "^L!user calls!" ${log_file1} |cut -d! -f3`
STVAL2[6]=`grep "^L!user calls!" ${log_file2} |cut -d! -f3`
STNAME[7]="Parses"
STVAL1[7]=`grep "^L!parse count (total)!" ${log_file1} |cut -d! -f3`
STVAL2[7]=`grep "^L!parse count (total)!" ${log_file2} |cut -d! -f3`
STNAME[8]="Hard parses"
STVAL1[8]=`grep "^L!parse count (hard)!" ${log_file1} |cut -d! -f3`
STVAL2[8]=`grep "^L!parse count (hard)!" ${log_file2} |cut -d! -f3`
STNAME[9]="Sorts"
STVAL1D=`grep "^L!sorts (disk)!" ${log_file1} |cut -d! -f3`
STVAL2D=`grep "^L!sorts (disk)!" ${log_file2} |cut -d! -f3`
STVAL1M=`grep "^L!sorts (memory)!" ${log_file1} |cut -d! -f3`
STVAL2M=`grep "^L!sorts (memory)!" ${log_file2} |cut -d! -f3`
STVAL1[9]=`awk -v x=${STVAL1D} -v y=${STVAL1M} 'BEGIN {printf "%f\n", y+x}'`
STVAL2[9]=`awk -v x=${STVAL2D} -v y=${STVAL2M} 'BEGIN {printf "%f\n", y+x}'`
STNAME[10]="Logons"
STVAL1[10]=`grep "^L!logons cumulative!" ${log_file1} |cut -d! -f3`
STVAL2[10]=`grep "^L!logons cumulative!" ${log_file2} |cut -d! -f3`
STNAME[11]="Executes"
STVAL1[11]=`grep "^L!execute count!" ${log_file1} |cut -d! -f3`
STVAL2[11]=`grep "^L!execute count!" ${log_file2} |cut -d! -f3`
STNAME[12]="Transactions"
STVAL1C=`grep "^L!user commits!" ${log_file1} |cut -d! -f3`
STVAL2C=`grep "^L!user commits!" ${log_file2} |cut -d! -f3`
STVAL1R=`grep "^L!user rollbacks!" ${log_file1} |cut -d! -f3`
STVAL2R=`grep "^L!user rollbacks!" ${log_file2} |cut -d! -f3`
STVAL1[12]=`awk -v x=${STVAL1C} -v y=${STVAL1R} 'BEGIN {printf "%f\n", y+x}'`
STVAL2[12]=`awk -v x=${STVAL2C} -v y=${STVAL2R} 'BEGIN {printf "%f\n", y+x}'`

UROL=`awk -v x=${STVAL1R} -v y=${STVAL2R} 'BEGIN {printf "%f\n", y-x}'`
TRAN=`awk -v x=${STVAL1[12]} -v y=${STVAL2[12]} 'BEGIN {printf "%f\n", y-x}'`
if [[ ${TRAN} = "0.000000" ]]; then
  TRAN=1
fi

echo "TIME: `date +\%Y-%m-%dT\%H:%M:%S%z`" >> ${log_file_hist}

echo "" >> ${log_file_hist} 
echo "Load Profile" >> ${log_file_hist}
echo "~~~~~~~~~~~~                          Per Second      Per Transaction" >> ${log_file_hist}

index=1
index_end=${#STNAME[*]}
while (( index <= ${index_end} ))
do
  awk -v it=${interval} -v ct=${TRAN} -v nm="${STNAME[${index}]}" -v val1=${STVAL1[${index}]} -v val2=${STVAL2[${index}]} 'BEGIN {printf "%30s      %12.2f         %12.2f\n", nm, (val2-val1)/it, (val2-val1)/ct}' >> ${log_file_hist} 
  name=${STNAME[${index}]}
  value1=`awk -v it=${interval} -v ct=${TRAN} -v nm="${STNAME[${index}]}" -v val1=${STVAL1[${index}]} -v val2=${STVAL2[${index}]} 'BEGIN {printf "%12.2f", (val2-val1)/it}'`
  value2=`awk -v it=${interval} -v ct=${TRAN} -v nm="${STNAME[${index}]}" -v val1=${STVAL1[${index}]} -v val2=${STVAL2[${index}]} 'BEGIN {printf "%12.2f", (val2-val1)/ct}'`
  echo "name=$name; value1=$value1; value2=$value2"
  aws cloudwatch put-metric-data --metric-name ${dbname}-"'${name}'"-"'per second'" --namespace "DatabaseLoadProfile" --value $value1 --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
  aws cloudwatch put-metric-data --metric-name ${dbname}-"'${name}'"-"'per transaction'" --namespace "DatabaseLoadProfile" --value $value2 --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
  index=${index}+1
done

cat ${log_file1} |cut -d! -f1,2 > ${log_file1a}
cat ${log_file2} |cut -d! -f1,2 > ${log_file2a}
cp ${log_file1} ${log_file1b}
cp ${log_file2} ${log_file2b}

diff ${log_file1a} ${log_file2a} |egrep "<|>" |while read LINE
do
  EVENT=`echo $LINE |cut -d'!' -f2`
  grep -v "$EVENT" ${log_file1b} > ${log_file1c}
  grep -v "$EVENT" ${log_file2b} > ${log_file2c}
  mv ${log_file1c} ${log_file1b}
  mv ${log_file2c} ${log_file2b}
done

paste -d "\!"  ${log_file1b} ${log_file2b} |grep "^W!" |while read LINE
    do
      EVENT=`echo $LINE |cut -d'!' -f2`
      waits1=`echo $LINE |cut -d'!' -f3`
      waited_time1=`echo $LINE |cut -d! -f4`
      waits2=`echo $LINE |cut -d'!' -f7`
      waited_time2=`echo $LINE |cut -d! -f8`

      waits=`awk -v x=${waits1} -v y=${waits2} 'BEGIN {printf "%.0f\n", y-x}'`
      waited_time=`awk -v x=${waited_time1} -v y=${waited_time2} 'BEGIN {printf "%.0f\n", y-x}'`
      echo ${EVENT}!${waits}!${waited_time} >> ${log_file3}
    done
    total_waits=`awk 'BEGIN { FS = "!"; sum = 0 }; { sum += $2}; END { print sum }' ${log_file3}`
    total_waited_time=`awk 'BEGIN { FS = "!"; sum = 0}; { sum += $3}; END { print sum }' ${log_file3}`

    echo "Top 10 Wait Events" >> ${log_file_hist}
    echo "~~~~~~~~~~~~~~~~~~                                      Waits WTime(cs)     Avg  % Total" >> ${log_file_hist}
    echo "Event                                        Waits  Time (cs)   Per Sec Wait(ms) Wt Time" >> ${log_file_hist}
    echo "---------------------------------------- --------- ---------- --------- ------- -------" >> ${log_file_hist}

    if [[ -f ${log_file3} ]]; then
      if [[ ${total_waited_time} = 0 ]]; then
        sort -n -r -t! -k 3,3 -k 2,2 ${log_file3} |head -n 10 \
          | awk -F '!' -v tottm=${total_waited_time} -v it=${interval} \
   '{twps=$3/it; twpct=$3*100; printf "%39s!%10d!%10d!%9.2f!%7.2f\n", $1, $2, $3, twps, twpct}' |tee -a ${log_file}
      else
        sort -n -r -t! -k 3,3 ${log_file3} |head -n 10 \
          | awk -F '!' -v tottm=${total_waited_time} -v it=${interval} \
   '{twps=$3/it; twpct=$3*100; ws=$2; if (ws==0) {ws=1} awpw=10*$3/ws; twpct=twpct/tottm; printf "%39s!%10d!%10d!%9.2f!%7.2f!%7.2f\n", $1, $2, $3, twps, awpw, twpct}' |tee -a ${log_file}
      fi

      echo ${total_waits}!${total_waited_time} \
        | awk -F '!' -v it=${interval} '{ttwps=$2/it; printf "                                  TOTAL!%10d!%10d!%9.2f\n", $1, $2, ttwps}' |tee -a ${log_file}
    fi

#active_sessions=`tail -2 active_sessions.lst |head -1 |tr -s ' ' ' '`
#echo "active_sessions=$active_sessions"

cat ${log_file} >> ${log_file_hist}
cat ${log_file} |while read LINE
do
  EVENT=`echo $LINE |cut -d'!' -f1`
  waits=`echo $LINE |cut -d'!' -f2`
  wtime_psec=`echo $LINE |cut -d! -f4`
  wait_avg=`echo $LINE |cut -d! -f5`

#  echo "EVENT=$EVENT, waits=$waits, wtime_psec=$wtime_psec, wait_avg=$wait_avg"
  aws cloudwatch put-metric-data --metric-name ${dbname}-WAITS-"'${EVENT}'" --namespace "DatabaseWait" --value $waits --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
  aws cloudwatch put-metric-data --metric-name ${dbname}-WTIME-"'${EVENT}'" --namespace "DatabaseWait" --value $wtime_psec --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`

  if [ "$EVENT" != "TOTAL" ]; then
#    echo "aws cloudwatch put-metric-data --metric-name ${dbname}-WAITAVG-\"'${EVENT}'\" --namespace \"DatabaseWait\" --value $wait_avg --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`"
    aws cloudwatch put-metric-data --metric-name ${dbname}-WAITAVG-"'${EVENT}'" --namespace "DatabaseWait" --value $wait_avg --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
  fi
done

# Delete the old log files.
if [[ -L ${log_dir} ]]; then
  find ${log_dir} -follow -name "db_performance_${dbname}_*.log" -mtime +${days_kept_log} -exec rm {} \;
else
  find ${log_dir} -name "db_performance_${dbname}_*.log" -mtime +${days_kept_log} -exec rm {} \;
fi

