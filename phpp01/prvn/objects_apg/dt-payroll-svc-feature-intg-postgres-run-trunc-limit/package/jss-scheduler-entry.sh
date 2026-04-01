#!/bin/sh

artifact_directory="/app/artifacts"
version=""
configenv=$PSP_CONFIG_ENV

__log() {
    echo "$(date +'%FT%T,%3N%z') $APP_NAME-LOGGER - $PSP_CONFIG_ENV - $1";
}

set_version(){
  version=`cat /app/version.txt`
  __log "[stage=set_version] version : ${version}"
}


setup_artifacts() {
     __log "[stage=download_artifacts] Downloading JSS artifacts... Start";


    unzip -o ${artifact_directory}/batch-jobs-spcfdeploy.zip -d /apps/spcf/lib || exit 1;
    unzip -o -j ${artifact_directory}/psp-configuration.zip psp-configuration/psp-configuration-${version}-$PSP_CONFIG_ENV-batch-conf.zip -d /apps/batch/flux/etc/
    unzip -o /apps/batch/flux/etc/psp-configuration-${version}-$PSP_CONFIG_ENV-batch-conf.zip -d /apps/batch/flux/etc/

    rm /apps/batch/flux/etc/psp-configuration-${version}-$PSP_CONFIG_ENV-batch-conf.zip
    unzip -o ${artifact_directory}/psp-batchjobs-batchdeploy-jss.zip -d /apps/batch/jss/;

    mv /apps/batch/flux/etc/object-service-component-factory-conf.xml /apps/batch/flux/shell/ || exit 1;
    mv /apps/batch/flux/etc/spcf-meta-conf.xml /apps/batch/flux/shell/ || exit 1;

    rm -rdf /apps/batch/jss/etc;
    ln -s /apps/batch/flux/etc /apps/batch/jss/etc || exit 1;
    ln -s /apps/batch/flux/shell/spcf-meta-conf.xml /apps/batch/jss/shell/spcf-meta-conf.xml || exit 1;
    ln -s /apps/batch/flux/shell/object-service-component-factory-conf.xml /apps/batch/jss/shell/object-service-component-factory-conf.xml || exit 1;

    cd /apps/batch/flux/etc

#    dos2unix /apps/batch/jss/etc/* || exit 1;
    ln -s /apps/spcf/lib /apps/batch/jss/lib/spcf || exit 1;

#    dos2unix /apps/batch/jss/shell/* || exit 1;
    chmod 754 /apps/batch/jss/shell/*.sh || exit 1;

    __log "[stage=download_artifacts] Downloading JSS artifacts... Complete";
}

schedule_unschedule_jss_jobs() {
  __log "[stage=schedule_unschedule_jss_jobs] Schedule/UnSchedule Batch Jobs... Start";

    cd /apps/batch/jss/shell;

    if [ -e BatchJobManager.sh ]; then
      if [ "${SCHEDULE_OPERATION}" == "unschedule" ]; then
        __log "UnScheduling all batch jobs in JSS...Start";
          ./BatchJobManager.sh unschedule all;
        __log "UnScheduling all batch jobs in JSS...Complete";
      fi;
      if [ "${SCHEDULE_OPERATION}" == "schedule" ]; then
          __log "Scheduling all batch jobs in JSS...Start";
          ./BatchJobManager.sh schedule all;
          __log "Scheduling all batch jobs in JSS...Complete";
      fi;
    else
        __log "Error.....Cannot find BatchJobManager.sh!";
        exit 1;
    fi

  __log "[stage=schedule_unschedule_jss_jobs] Schedule/UnSchedule Batch Jobs... Complete";
}

run_reencryption_job() {
  __log "[stage=run_reencryption_job] Run Reencryption Batch Job... Start";
  __log "[stage=run_reencryption_job] Arguments="${INPUT_PARAM};

    cd /apps/batch/jss/shell;

    if [ -e BatchJobManager.sh ]; then
      if [ "${SCHEDULE_OPERATION}" == "re-encrypt" ]; then
          __log "Scheduling DataReencryption batch jobs in JSS...Start";
          ./ReencryptionJob.sh run DataReencryptionProcessor ${INPUT_PARAM}
          __log "Scheduling DataReencryption batch jobs in JSS...Complete";
      fi;
    else
        __log "Error.....Cannot find BatchJobManager.sh!";
        exit 1;
    fi

  __log "[stage=run_reencryption_job] Run Reencryption Batch Job... Complete";
}

print_schedule_logs() {
  if [ "${SCHEDULE_OPERATION}" != "re-encrypt" ]; then
    __log "[stage=print_schedule_logs] Printing Logs for JSS ${SCHEDULE_OPERATION} logs... Start"

    cd /apps/batch/flux/logs/
    cat $(ls -1t | head -1)
    __log "[stage=print_schedule_logs] Printing Logs for  JSS ${SCHEDULE_OPERATION} logs... Complete"
  fi
}

int_spring_activeprofile() {
  __log "[stage=int_spring_activeprofile] Adding spring activeprofile system property to JAVA OPTS in the file '$1'... START";
  echo '' >> $1
  echo JAVA_OPTS=\"\$JAVA_OPTS -Dspring.profiles.active=$configenv\" >> $1
  echo 'export JAVA_OPTS' >> $1

  __log "[stage=int_spring_activeprofile] Adding spring activeprofile system property to JAVA OPTS ... COMPLETED";
}

init_database_configuration() {
  __log "[stage=init_database_configuration] Adding database configuration to JAVA OPTS in the file '$1', Monolith_DB: '$MONOLITH_DB', Audit_DB: '$AUDIT_DB'... START";

  echo JAVA_OPTS=\"\$JAVA_OPTS -Ddatabase.monolith=$MONOLITH_DB\" >> $1
  echo JAVA_OPTS=\"\$JAVA_OPTS -Ddatabase.audit=$AUDIT_DB\" >> $1

  echo 'export JAVA_OPTS' >> $1

  __log "[stage=init_database_configuration] Adding database configuration to JAVA OPTS ... COMPLETED";
}

__log "Scheduling/UnScheduling JSS Jobs is Start";
set_version
setup_artifacts
int_spring_activeprofile "/apps/batch/jss/shell/setenv.sh"
init_database_configuration "/apps/batch/jss/shell/setenv.sh"
schedule_unschedule_jss_jobs
run_reencryption_job
print_schedule_logs
__log "Scheduling/UnScheduling JSS Jobs is Complete";

if [[ $APP_ENV == "ds2" ]] || [[ $APP_ENV == "stg" ]]
then
    __log "Parallel Env Killing Proxy Container";
    kill -9 $(pidof /usr/bin/envoy)
    __log "Parallel Env Killed Proxy Container";
fi
sleep 60