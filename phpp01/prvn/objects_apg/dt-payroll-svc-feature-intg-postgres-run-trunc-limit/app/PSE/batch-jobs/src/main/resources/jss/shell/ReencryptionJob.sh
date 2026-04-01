#!/bin/bash
# Start file for batch jobs
#
# Description: This script will start the passed batch job process.
#
. ./setenv.sh

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.jss.JSSBatchJobManager $1 $2 $3 $4 $5 $6 $7 $8 $9

exit $?