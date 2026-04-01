#!/bin/bash
# Start file for the ACH Upload Manual Override
#
# Maintained by: Rob Histing / Mike Kinasz / Ken Paul
#
# Description: This script will send the ACH files to the alternate ach upload server and update the state
#              of the ACH Offload batch process to make it appear as if the files have been successfully
#              uploaded to the bank (this will allow the batch job to continue it's normal processing.)
#
# Important Note: Once this script has been successfully executed, it is the responsibility of Operations personnel
#                 (or another responsible agent) to maually upload the ACH files to the bank.
#
#
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=achuploadmanualoverride"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.tools.ach.AchUploadManualOverride $1 >$BE_LOG/achuploadmanualoverride.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
