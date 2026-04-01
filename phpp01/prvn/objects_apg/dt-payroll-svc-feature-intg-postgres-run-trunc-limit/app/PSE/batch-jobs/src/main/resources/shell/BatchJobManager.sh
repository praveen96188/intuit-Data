#!/bin/bash
# Start file for the Missed Transactions batch process
#
# Maintained by: Rob Histing / Jim Newell / Ken Paul
#
# Description: This script will start the Missed Transactions batch process.
#
#
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=batchjobmanager"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager $1 $2 $3 $4 $5 $6 $7 $8 $9 >$BE_LOG/$1-batchjobmanager.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
