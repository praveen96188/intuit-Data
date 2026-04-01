#!/bin/bash
# Start file for the Gems Upload batch process
#
# Maintained by: Rob Histing / Jim Newell / Ken Paul
#
# Description: This script will start the Gems Upload batch process.
#
#
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=gemsupload"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload.DailyGemsUploadBatchProcess $1 >$BE_LOG/gemsdailyupload.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
