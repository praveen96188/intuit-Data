#!/bin/env bash
# Usage: run_on_a_cluster.sh c70 
cd /u01/scripts/mayank/tmp
#rm run_on_a_cluster.log
cluster=$1
source /l/orcl

echo "Running on $cluster" >> run_on_a_cluster.log 
sqlplus -s mchoubey/'Temp#123'@$cluster @run_on_a_cluster_with_hint.sql >run_on_a_cluster_with_hint.log

#cat run_on_a_cluster.log

