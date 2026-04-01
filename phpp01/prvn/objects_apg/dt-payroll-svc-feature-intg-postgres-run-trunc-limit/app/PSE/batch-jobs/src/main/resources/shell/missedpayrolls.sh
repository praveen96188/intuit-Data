#!/bin/bash
# Start file for the Missed Payrolls batch process
#
# Maintained by: Rob Histing / Jim Newell / Ken Paul
#
# Description: This script will start the Missed Payrolls batch process.
#
#
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=missedpayrolls"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls $1 >$BE_LOG/missedpayrolls.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
