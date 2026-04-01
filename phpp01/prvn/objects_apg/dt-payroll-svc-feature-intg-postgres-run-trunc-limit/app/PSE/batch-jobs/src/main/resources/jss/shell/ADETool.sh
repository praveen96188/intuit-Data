#!/bin/bash
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=ADETool"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.adapters.ade.tools.ADETool $1 $2 $3 $4 $5 $6 $7 $8 $9 > $BE_LOG/ADETool.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?