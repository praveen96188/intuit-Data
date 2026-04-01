#!/bin/bash
# Start file for the Fraudulent Payrolls batch process
#
# Maintained by: Rob Histing / Jim Newell / Ken Paul
#
# Description: This script will start the Fraudulent Payrolls batch process.
#
#
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=fraudulentpayrolls"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls >$BE_LOG/fraudulentpayrolls.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
