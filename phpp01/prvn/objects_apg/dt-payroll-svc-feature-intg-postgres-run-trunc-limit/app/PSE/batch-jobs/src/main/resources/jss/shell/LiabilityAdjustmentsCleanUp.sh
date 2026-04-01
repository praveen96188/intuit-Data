#!/bin/bash
cd /apps/batch/jss/shell
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=payment-cleanup"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.LiabilityAdjustmentsCleanUp -commit=true $1 $2 $3 $4 $5 $6 $7 $8 $9 > $BE_LOG/LiabilityAdjustmentsCleanUp.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
