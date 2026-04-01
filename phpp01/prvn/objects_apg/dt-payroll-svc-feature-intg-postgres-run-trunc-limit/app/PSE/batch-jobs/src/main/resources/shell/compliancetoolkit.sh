#!/bin/bash
# Start file for the Compliance Toolkit
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=compliancetoolkit"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.tools.ComplianceToolkit $1 $2 $3 $4 >$BE_LOG/compliancetoolkit.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
