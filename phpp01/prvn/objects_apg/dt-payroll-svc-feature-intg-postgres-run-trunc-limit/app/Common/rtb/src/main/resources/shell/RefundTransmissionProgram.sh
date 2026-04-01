#!/bin/bash
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=RefundTransmissionProgram"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath /apps/batch/flux/lib:/RTB.jar:$BE_CLASSPATH com.intuit.ems.payroll.psp.RefundTransmissionProgram $1 $2 $3 $4 $5 $6 $7 $8 $9 > $BE_LOG/RefundTransmissionProgram.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
