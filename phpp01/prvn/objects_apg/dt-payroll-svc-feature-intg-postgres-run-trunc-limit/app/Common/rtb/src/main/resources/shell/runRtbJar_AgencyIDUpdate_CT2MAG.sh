#!/bin/bash

. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=AgencyIDUpdate_CT2MAG"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.ems.payroll.psp.AgencyIDUpdate_CT2MAG > $BE_LOG/AgencyIDUpdate_CT2MAG.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?