#!/bin/bash
# Start file for batch jobs
#
# Description: This script will start the passed batch job process.
#
. ./setenv.sh

LOG_FILE_NAME=$(date +%Y%m%d%H%M%S)

# Flux Manager
export JAVA_HOME=/usr/java/jdk1.6.0_121/bin

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $RTB_CLASSPATH com.intuit.ems.payroll.psp.jss.monitoring.FluxManager $1 $2 $3 $4 $5 $6 $7 $8 $9 >$RTB_LOG/batchjobmanager.out.$LOG_FILE_NAME 2>&1

# JSS Manager
export JAVA_HOME=/usr/java/jdk1.7.0_111/bin

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $RTB_CLASSPATH com.intuit.ems.payroll.psp.jss.monitoring.JSSManager $1 $2 $3 $4 $5 $6 $7 $8 $9 >>$RTB_LOG/batchjobmanager.out.$LOG_FILE_NAME 2>&1

exit $?