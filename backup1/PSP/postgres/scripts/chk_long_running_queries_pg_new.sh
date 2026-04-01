. /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`
pd_topic_arn="arn:aws:sns:us-west-2:893547637742:db-pspprod-slow-query"
stg_topic_arn="arn:aws:sns:us-west-2:893547637742:psp-staging-slack"
stg_mail_topic_arn="arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-warning-stagin-a-intuit-com"

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

#cluster_instances= aws --region us-west-2 rds describe-db-clusters --db-cluster-identifier ${cluster_postgres} --query 'DBClusters[0].DBClusterMembers' | grep DBInstanceIdentifier | awk '{print $2}'|sed 's/"//'|sed 's/"//'
#echo ${cluster_instances}
for instance_name in `aws --region us-west-2 rds describe-db-clusters --db-cluster-identifier ${cluster_postgres} --query 'DBClusters[0].DBClusterMembers' | grep DBInstanceIdentifier | awk '{print $2}'|sed 's/"//'|sed 's/"//'  | rev | cut -c2- | rev`
do
   #for instance_name1 in `echo ${instance_name} | awk '{print $1}'| rev | cut -c2- | rev`
    #do
    ./run_postgres.sh ${instance_name} $db_name chk_instance_db_mode.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}_chk_instance_db_mode.log
    if [ "`grep "WRITER" ${logdir}/${instance_name}_${db_name}_chk_instance_db_mode.log | wc -l`" -eq "1" ] ; then
        echo "${instance_name} database is a WRITER"   
        ./run_postgres.sh ${instance_name} $db_name chk_long_running_queries_pg.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log
                if [ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
                        echo "good"
#                        if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep state|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'` == "idle in transaction" && `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'| xargs printf "%.*f\n" "0"` -ge 900 ]]; then
#                                the_subject="Query in idle in transaction state (>15 mins)"
#                                if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
#                                        /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
#                                elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
#                                        /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
#                                        /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
#                               fi
                        if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'| xargs printf "%.*f\n" "0"` -ge 300 ]]; then
                                if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'` == "psp"*"app" ]]; then
                                        the_subject="Long running query (> 5 Mins) - OLTP"
#                                else
#                                        the_subject="Long running query (> 5 Mins) - Individual User"
                                fi
                                if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
                                        /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
                                        /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                        /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                fi
                        elif [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F"." '{print $1}'|awk -F, '!seen[$1]++'| xargs printf "%.*f\n" "0"` -ge 3600 ]]; then
                                if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F, '!seen[$1]++'` == "pspbatch"*"user" ]]; then
                                        the_subject="Long running query (> 60 Mins) - OLAP"
                                else
                                        the_subject="Long running query (> 60 Mins) - Individual User"
                                fi
                                if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
                                        /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
                                        /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                        /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                fi
                        fi
                fi
    elif [ "`grep "READER" ${logdir}/${instance_name}_${db_name}_chk_instance_db_mode.log | wc -l`" -eq "1" ] ; then
        echo "${instance_name} database is a READER" 
        ./run_postgres.sh ${instance_name} $db_name chk_long_running_queries_pg.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log
            if [ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
                    echo "good"
#                    if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep state|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'` == "idle in transaction" && `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'| xargs printf "%.*f\n" "0"` -ge 900 ]]; then
#                            the_subject="Query in idle in transaction state (>15 mins) on reader instance:  ${instance_name}"
#                            if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
#                                    /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
#                            elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
#                                    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
#                                    /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
#                            fi
                    if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'| xargs printf "%.*f\n" "0"` -ge 300 ]]; then
                            if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'` == "psp"*"app" || `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'` == "psp"*"read" ]]; then
                                    the_subject="Long running query (> 5 Mins) - OLTP on reader instance:  ${instance_name}"
#                            else
#                                    the_subject="Long running query (> 5 Mins) - Individual User on reader instance:  ${instance_name}"
                            fi
                            if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
                                    /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                            elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
                                    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                    /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                            fi
                    elif [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep query_duration_secs|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F"." '{print $1}'|awk -F, '!seen[$1]++'| xargs printf "%.*f\n" "0"` -ge 3600 ]]; then
                            if [[ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F, '!seen[$1]++'` == "pspbatch"*"user" || `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F, '!seen[$1]++'` == "psprjf" || `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep usename|awk -F"|" '{print $2}'| awk '{ gsub(/^[ \t]+|[ \t]+$/, ""); print }'|awk -F, '!seen[$1]++'` == "psp_payroll_dm" ]]; then
                                    the_subject="Long running query (> 60 Mins) - OLAP on reader instance:  ${instance_name}"
                            else
                                    the_subject="Long running query (> 60 Mins) - Individual User on reader instance:  ${instance_name}"
                            fi
                            if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
                                    /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                            elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
                                    /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                                    /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                            fi
                    fi
            fi
    fi
#done
done

