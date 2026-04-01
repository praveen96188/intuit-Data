#!/bin/bash
# Start file for the PSP Email Gateway
#
# Maintained by: Rob Histing / Jim Newell / Ken Paul
#
# Description: This script will start the PSP Email Gateway
#
#
#
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=emailgateway"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -cp $BE_LIB/jboss-xml-binding.jar:$BE_CLASSPATH com.intuit.sbd.payroll.psp.gateways.email.EmailGateway >$BE_LOG/emailgateway.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
