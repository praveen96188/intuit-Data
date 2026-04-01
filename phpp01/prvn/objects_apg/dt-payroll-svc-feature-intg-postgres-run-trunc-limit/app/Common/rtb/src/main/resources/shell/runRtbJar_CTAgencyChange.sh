#!/bin/bash

. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=CTAgencyChange"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.ems.payroll.psp.CTAgencyChange $1 $2 $3 > $BE_LOG/CTAgencyChange.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?