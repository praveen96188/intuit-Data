. /l/orcl

scripts_loc=/u01/scripts/partitioning
log_dir=/u01/scripts/partitioning/logs
RDS_SID=$1
sys_password=`cat .p`

cd ${scripts_loc}

exc_sqls()
{

  for i in {3..7}
    do
      file=${i}_*
      sql=`echo ${file}`
      echo -e "\n ................executing ${sql}................" 
      log=${log_dir}/`echo ${file}|cut -d. -f1`.html
      step=`echo ${file}|cut -d. -f1`
      sqlplus /nolog <<EOF >${log}
      connect intuadmin/$sys_password@${RDS_SID}
      set lines 300
      set echo on timing on feedback on 
      set pagesize 500
      SET MARKUP HTML ON ENTMAP ON SPOOL ON PREFORMAT OFF ;
      @${sql}
      exit;
EOF
      if [ `grep -i ora- ${log}|wc -l` -eq 0 ] ; then
        echo -e "\n\e[32m${step} has been executed successfully !\e[0m"
      else
        echo -e "\n\e[31m${step} did not execute properly.Please check...\e[0m"
#        exit 1
      fi
done
}

exc_sqls

