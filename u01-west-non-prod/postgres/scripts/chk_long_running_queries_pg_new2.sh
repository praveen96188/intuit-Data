. /l/orcl

cluster_postgres=$1
db_name=$2

logdir=./log
mkdir -p $logdir

# number of days to keep log files
DAYS_KEPT_LOG=14

EXTRACTED_DATA="extracted_data"

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`
pd_topic_arn="arn:aws:sns:us-west-2:893547637742:db-pspprod-slow-query"
stg_topic_arn="arn:aws:sns:us-west-2:893547637742:psp-staging-slack"
stg_mail_topic_arn="arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-warning-stagin-a-intuit-com"

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:DB-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:apg-db-sbg-psp-warning-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV" >> ${tmp_file}
  exit 1
fi

#cluster_instances= aws --region us-west-2 rds describe-db-clusters --db-cluster-identifier ${cluster_postgres} --query 'DBClusters[0].DBClusterMembers' | grep DBInstanceIdentifier | awk '{print $2}'|sed 's/"//'|sed 's/"//'
#echo ${cluster_instances}
for instance_name in `aws --region us-west-2 rds describe-db-clusters --db-cluster-identifier ${cluster_postgres} --query 'DBClusters[0].DBClusterMembers' | grep DBInstanceIdentifier | awk '{print $2}'|sed 's/"//'|sed 's/"//'`
do
     for instance_name1 in `echo ${instance_name} | awk '{print $1}'| rev | cut -c2- | rev`
   do
    ./run_postgres.sh ${instance_name1} $db_name chk_instance_db_mode.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}_chk_instance_db_mode.log
    if [ "`grep "WRITER" ${logdir}/${instance_name}_${db_name}_chk_instance_db_mode.log | wc -l`" -eq "1" ] ; then
        echo "${instance_name} database is a WRITER"
        ./run_postgres.sh ${instance_name1} $db_name chk_long_running_queries_pg.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log
        if [ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
            echo "good"
            # Read the log file and extract usename and query_duration_secs values
            awk '
            BEGIN { FS = "|" }
            {
                    if ($1 ~ /usename/) {
                    usename = $2; gsub(/^[ \t]*|[ \t]*$/, "", usename);
                    }
                    if ($1 ~ /query_duration_secs/) {
                    query_duration_secs = $2; gsub(/^[ \t]*|[ \t]*$/, "", query_duration_secs);
                    print usename, query_duration_secs
                    }
            }
            ' ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log > $EXTRACTED_DATA_${instance_name}
            # Process the extracted data
            while read usename query_duration_secs; do
                tr_query_duration_secs=$(printf "%.0f" $query_duration_secs)
                if [[ $tr_query_duration_secs -ge 300 ]]; then
                    if [[ $usename == "psp"*"app" || $usename == "psparead" ]]; then
                        the_subject="Long running query (> 5 Mins) - OLTP"
#                   elif [[ $usename != "pspbatch"*"user" || $usename != "psprjf" || $usename != "psp_payroll_dm" ]]; then
#                       the_subject="Long running query (> 5 Mins) - Individual User"
                    fi
                    if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
                        /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                    elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
                        /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                        /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                    fi
                elif [[ $tr_query_duration_secs -ge 3600 ]]; then
                    if [[ $usename == "pspbatch"*"user" || $usename == "psprjf" || $usename == "psp_payroll_dm" ]]; then
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
            done < $EXTRACTED_DATA_${instance_name}
        fi
    elif [ "`grep "READER" ${logdir}/${instance_name}_${db_name}_chk_instance_db_mode.log | wc -l`" -eq "1" ] ; then
        echo "${instance_name} database is a READER"
        ./run_postgres.sh ${instance_name1} $db_name chk_long_running_queries_pg.sql "-v dbname=${db_name}" > ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log
        if [ `cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log |grep -i "0 rows" |wc -l` -ne 1 ]; then
            echo "good"
            # Read the log file and extract usename and query_duration_secs values
            awk '
            BEGIN { FS = "|" }
            {
                    if ($1 ~ /usename/) {
                    usename = $2; gsub(/^[ \t]*|[ \t]*$/, "", usename);
                    }
                    if ($1 ~ /query_duration_secs/) {
                    query_duration_secs = $2; gsub(/^[ \t]*|[ \t]*$/, "", query_duration_secs);
                    print usename, query_duration_secs
                    }
            }
            ' ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log > $EXTRACTED_DATA_${instance_name}
            # Process the extracted data
            while read usename query_duration_secs; do
                tr_query_duration_secs=$(printf "%.0f" $query_duration_secs)
                if [[ $tr_query_duration_secs -ge 300 ]]; then
                    if [[ $usename == "psp"*"app" || $usename == "psp"*"read" ]]; then
                        the_subject="Long running query (> 5 Mins) - OLTP on reader instance:  ${instance_name}"
#                   elif [[ $usename != "pspbatch"*"user" || $usename != "psprjf" || $usename != "psp_payroll_dm" ]]; then
#                       the_subject="Long running query (> 5 Mins) - Individual User on reader instance:  ${instance_name}"
                    fi
                    if [ "${db_name}" == "pitparmo" ] || [ "${db_name}" == "parapgib" ]; then
                        /usr/bin/aws sns publish --topic-arn ${stg_mail_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                    elif [ "${db_name}" == "pspapg02" ] || [ "${db_name}" == "prodapgib" ]; then
                        /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                        /usr/bin/aws sns publish --topic-arn ${pd_topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${instance_name}_${db_name}_chk_long_running_queries.log`"
                    fi
                elif [[ $tr_query_duration_secs -ge 3600 ]]; then
                    if [[ $usename == "pspbatch"*"user" || $usename == "psprjf" || $usename == "psp_payroll_dm" ]]; then
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
            done < $EXTRACTED_DATA_${instance_name}
        fi
    fi
done
done
