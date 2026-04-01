#!/bin/bash
# Start file for the ACH Offload batch process
#
# Maintained by: Rob Histing / Jim Newell / Ken Paul
#
# Description: This script will start the ACH Offload batch process.
#
#
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=achoffload"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions $1 $2 >$BE_LOG/achoffload.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
