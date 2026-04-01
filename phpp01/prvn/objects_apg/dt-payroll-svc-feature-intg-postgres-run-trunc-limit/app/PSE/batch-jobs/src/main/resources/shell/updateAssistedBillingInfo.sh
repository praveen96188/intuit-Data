#!/bin/bash
. /apps/batch/flux/shell/setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=migrate-assistedBillingInfo -DshowUpdated=true"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH com.intuit.sbd.payroll.psp.migration.AssistedBillingInformationUpdater $1 $2 $3 $4 $5 $6 $7 $8 $9 $10 $11 $12 > $BE_LOG/migrateAssistedBillingInfo.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
