#!/bin/env bash
# Usage: run_on_a_cluster.sh c70 
cd /home/oracle/AWS/qbopp034/scripts/kpopat 
rm run_on_a_cluster.log
cluster=$1
source /l/orcl
export TNS_ADMIN=/home/oracle/AWS/qbopp034/scripts/kpopat

echo "Running on $cluster" >> run_on_a_cluster.log 
sqlplus -s kpopat/'Oracle#333'@$cluster @run_on_a_cluster.sql >>run_on_a_cluster.log

cat run_on_a_cluster.log
