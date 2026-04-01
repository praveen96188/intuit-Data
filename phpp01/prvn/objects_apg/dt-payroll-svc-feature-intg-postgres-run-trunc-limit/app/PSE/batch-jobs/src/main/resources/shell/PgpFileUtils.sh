#!/bin/bash
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=PgpFileUtils"

$JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath /apps/batch/flux/lib/psp-pgp-0.1.0.jar:$BE_CLASSPATH com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils $1 $2 $3 $4 $5 $6 $7 $8 $9 > $BE_LOG/PgpFileUtils.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
