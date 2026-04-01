set -x 
#!/bin/bash
# Usage: monitor_ggserr_aws.sh aws or ihp> [db_name]
# Sample cron job entry:
#       */5 * * * * cd /u01/ogg/scripts; ./monitor_ggserr.sh aws pitpp001 1>/dev/null 2>&1

the_dist="SBGOPSIDCDBA@intuit.com"

. ~/.bash_profile

site=$1
dbname=$2

if [ "$site" == "ihp" ]; then
  . /l/${dbname}
  export https_proxy=http://qy1prdproxy01.pprod.ie.intuit.net:80
  profile=" --profile sbg-psp-ppd"
elif [ "$site" == "aws" ]; then
  . /l/orcl
  if [ "${dbname}" == "" ]; then
    dbname=${RDS_SID}
  fi
  profile=""
else
  echo "Wrong site"
  exit 1
fi

shdir=`pwd`
logdir=${shdir}/log

mkdir -p $logdir

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`
#AWS_DEFAULT_REGION=us-west-2

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:us-west-2:152430470825:gg-sbg-psp-prf-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:us-west-2:893547637742:gg-sbg-psp-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV"
  exit 1
fi

cd $GG_HOME

if [ ! -s ggserr.log ]
then
        exit
fi

light=`grep MANAGER ${shdir}/monitor_${dbname}.cfg | awk 'BEGIN{FS=","}{print $2}'`

if [ ${light} == "0" ]
then
        exit
fi

cat /dev/null > ${logdir}/ggsci_stat_${dbname}

./ggsci << EOF > ${logdir}/ggsci_${dbname}.out
info all
EOF

mgrstat=`cat ${logdir}/ggsci_${dbname}.out | grep MANAGER | awk 'BEGIN{OFS=""}{print $2}'`

if [ ${mgrstat} != "RUNNING" ]
then

        rpt_date=`ls -l $GG_HOME/dirrpt/MGR.rpt | awk '{print $6,$7,$8}'`
        let odate=`date -d "${rpt_date}" +%s`
        let ndate=`date +%s`
        let delta_min=($ndate - $odate)/60

        if [ $delta_min -lt 25 ]
        then
                the_subject="MANAGER is ${mgrstat} on ${the_host}"
                cat ${logdir}/ggsci_${dbname}.out | grep -i ^program > ${logdir}/ggsci_stat_${dbname}
                echo >> ${logdir}/ggsci_stat_${dbname}
                cat ${logdir}/ggsci_${dbname}.out | grep MANAGER >> ${logdir}/ggsci_stat_${dbname}
                echo >> ${logdir}/ggsci_stat_${dbname}
                echo "Server Name: ${the_host}" >> ${logdir}/ggsci_stat_${dbname}
                echo >> ${logdir}/ggsci_stat_${dbname}
                echo "Golden Gate Home: ${GG_HOME}"  >> ${logdir}/ggsci_stat_${dbname}
                echo >> ${logdir}/ggsci_stat_${dbname}
                echo "Source profile: ${sprofile}"  >> ${logdir}/ggsci_stat_${dbname}
                echo >> ${logdir}/ggsci_stat_${dbname}
                if [ "$site" == "ihp" ]; then
                  cat ${logdir}/ggsci_stat_${dbname} | mailx -s "${the_subject}" ${the_dist}
                else
                  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/ggsci_stat_${dbname}`"
                fi
        fi

fi

gg_array=(`cat ${logdir}/ggsci_${dbname}.out | egrep "^EXTRACT|^REPLICAT" | awk 'BEGIN{OFS=""}{print $1,"-",$2,"-",$3,"-",$4,"-",$5}'`)

for i in "${gg_array[@]}"
do
        gger=`echo ${i} | awk 'BEGIN{FS="-"}{print $1}'`
        ggs=`echo ${i} | awk 'BEGIN{FS="-"}{print $2}'`
        ggn=`echo ${i} | awk 'BEGIN{FS="-"}{print $3}'`
        ggl=`echo ${i} | awk 'BEGIN{FS="-"}{print $4}'`
        ggc=`echo ${i} | awk 'BEGIN{FS="-"}{print $5}'`

        light=`cat ${shdir}/monitor_${dbname}.cfg | awk -v entity=${ggn} 'BEGIN{FS=","}{if ( $1 == entity ) print $2}'`

        if [ ${light} != "0" ]
        then
                if [ ${ggs} == "RUNNING" ]
                then
                        if [ ${light} == "2" ]
                        then
                                lag=`echo ${ggl} | awk 'BEGIN{FS=":";secs=0}{secs=$1*3600+$2*60+$3}END{print secs}'`
                                cpt=`echo ${ggc} | awk 'BEGIN{FS=":";secs=0}{secs=$1*3600+$2*60+$3}END{print secs}'`
				let lag_min=$lag/60
				let cpt_min=$cpt/60
#                                echo "aws cloudwatch put-metric-data --metric-name ${dbname}-${ggn}-lag --namespace \"GoldengateLag\" --value $lag_min --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`"
                                aws $profile cloudwatch put-metric-data --metric-name ${dbname}-${ggn}-lag --namespace "GoldengateLag" --value $lag_min --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
                                aws $profile cloudwatch put-metric-data --metric-name ${dbname}-${ggn}-cpt --namespace "GoldengateLag" --value $cpt_min --timestamp `date +\%Y-%m-%dT\%H:%M:%S%z`
                                if [ ${lag} -gt 360 ] || [ ${cpt} -gt 360 ]
                                then
                                        the_subject="${gger} ${ggn} LAG/CHECKPOINT issue"
                                        cat ${logdir}/ggsci_${dbname}.out | grep -i ^program > ${logdir}/ggsci_stat_${dbname}
                                        echo >> ${logdir}/ggsci_stat_${dbname}
                                        cat ${logdir}/ggsci_${dbname}.out | grep ${ggn} >> ${logdir}/ggsci_stat_${dbname}
                                        echo >> ${logdir}/ggsci_stat_${dbname}
                                        echo "Server Name: ${the_host}" >> ${logdir}/ggsci_stat_${dbname}
                                        echo >> ${logdir}/ggsci_stat_${dbname}
                                        echo "Golden Gate Home: ${GG_HOME}"  >> ${logdir}/ggsci_stat_${dbname}
                                        echo >> ${logdir}/ggsci_stat_${dbname}
                                        echo "Source profile: ${sprofile}"  >> ${logdir}/ggsci_stat_${dbname}
                                        echo >> ${logdir}/ggsci_stat_${dbname}
                                        if [ "$site" == "ihp" ]; then
                                          cat ${logdir}/ggsci_stat_${dbname} | mailx -s "${the_subject}" ${the_dist}
                                        else
                                          /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/ggsci_stat_${dbname}`"
                                        fi
                                fi
                        fi
                else

                        if [ -s $GG_HOME/dirrpt/${ggn}.rpt ]
                        then
                                rpt_date=`ls -l $GG_HOME/dirrpt/${ggn}.rpt | awk '{print $6,$7,$8}'`
                                let odate=`date -d "${rpt_date}" +%s`
                                let ndate=`date +%s`
                                let delta_min=($ndate - $odate)/60
                        else
                                let delta_min=0
                        fi

                        if [ $delta_min -lt 25 ]
                        then
                                the_subject="${gger} ${ggn} is ${ggs}"
                                cat ${logdir}/ggsci_${dbname}.out | grep -i ^program > ${logdir}/ggsci_stat_${dbname}
                                echo >> ${logdir}/ggsci_stat_${dbname}
                                cat ${logdir}/ggsci_${dbname}.out | grep ${ggn} >> ${logdir}/ggsci_stat_${dbname}
                                echo >> ${logdir}/ggsci_stat_${dbname}
                                echo "Server Name: ${the_host}" >> ${logdir}/ggsci_stat_${dbname}
                                echo >> ${logdir}/ggsci_stat_${dbname}
                                echo "Golden Gate Home: ${GG_HOME}"  >> ${logdir}/ggsci_stat_${dbname}
                                echo >> ${logdir}/ggsci_stat_${dbname}
                                echo "Source profile: ${sprofile}"  >> ${logdir}/ggsci_stat_${dbname}
                                echo >> ${logdir}/ggsci_stat_${dbname}
                                if [ "$site" == "ihp" ]; then
                                  cat ${logdir}/ggsci_stat_${dbname} | mailx -s "${the_subject}" ${the_dist}
                                else
                                  /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/ggsci_stat_${dbname}`"
                                fi
                        fi
                fi

        fi
done



let err_count=`cat ggserr.log | grep "ERROR " | grep OGG- | wc -l`


if [ ! -f ${logdir}/error_save_${dbname} ]
then
        touch ${logdir}/error_save_${dbname}
fi

let last_count=`wc -l ${logdir}/error_save_${dbname} | awk '{print $1}'`

if [ ${err_count} -eq ${last_count} ]
then
        exit
fi

let delta=${err_count}-${last_count}

cat ggserr.log | grep "ERROR " | grep OGG- > ${logdir}/error_save_${dbname}

tail -${delta} ${logdir}/error_save_${dbname} > ${logdir}/error_rpt_${dbname}

for seed in `cat ${logdir}/ggsci_${dbname}.out | egrep "^EXTRACT|^REPLICAT" | awk '{print $3}'`
do

        light=`cat ${shdir}/monitor_${dbname}.cfg | awk -v entity=${seed} 'BEGIN{FS=","}{if ( $1 == entity ) print $2}'`

        if [ ${light} != "0" ]
        then
                lseed=`echo ${seed} | awk '{print tolower($1)}'`
                grep ${lseed} ${logdir}/error_rpt_${dbname} > ${logdir}/${lseed}_rpt
                if [ -s ${logdir}/${lseed}_rpt ]
                then
                        the_subject="${lseed} errors in ggserr.log"
                        echo >> ${logdir}/${lseed}_rpt
                        echo "Server Name: ${the_host}" >> ${logdir}/${lseed}_rpt
                        echo >> ${logdir}/${lseed}_rpt
                        echo "Golden Gate Home: ${GG_HOME}"  >> ${logdir}/${lseed}_rpt
                        echo >> ${logdir}/${lseed}_rpt
                        echo "Source profile: ${sprofile}"  >> ${logdir}/${lseed}_rpt
                        echo >> ${logdir}/${lseed}_rpt
                        if [ "$site" == "ihp" ]; then
                          cat ${logdir}/${lseed}_rpt | mailx -s "${the_subject}" ${the_dist}
                        else
                          /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logdir}/${lseed}_rpt`"
                        fi
                fi
        fi
done

