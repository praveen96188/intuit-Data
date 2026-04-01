#!/bin/bash
. /apps/batch/jss/shell/setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=upload-ofx"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.tools.TestScpEdiFiles

