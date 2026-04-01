#!/bin/bash
#
#

. ./setenv.sh

$JAVA_HOME/java $JAVA_OPTS -cp $BE_CLASSPATH com.intuit.sbd.payroll.psp.tools.FixDupTaxIds >$BE_LOG/fixduptxid.out.`date +%Y%m%d%H%M%S` 2>&1

exit $?
