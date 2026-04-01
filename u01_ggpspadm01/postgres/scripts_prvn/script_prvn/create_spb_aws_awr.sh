#!/bin/bash
# Usage /ops/bin/db/create_spb_aws.sh $SQL_ID $SQL_PLAN_HASH_VLAUE $SOURCE_CLUSTER $TARGET_CLUSTER
if [ $# -ne 6 ]; then
  echo "Usage: create_spb_aws.sh <sql id> <plan hash> <source cluster> <target cluster> <begin awr snap> <end awr snap>" 
  echo "      where <sql id> is sql_id like fstn4vqmgvtb5"
  echo "            <plan hash> is sql id plan hash value like 2792059021"  
  echo "            <source cluster> source cluster from which you want to copy good plan like C92"
  echo "            <target cluster> target cluster to which you want to copy good plan like C92-UW2"
  echo "            <begin awr snap> begin awr snap"
  echo "            <end awr snap> end awr snap"
  echo "Example: create_spb_aws.sh fstn4vqmgvtb5 2792059021 C92 C92-UW2 123 124" 
  exit 2
fi
script_dir=/u01/scripts
log_dir=/u01/scripts/LOG
cd $script_dir

SQL_ID=$1
SQL_PLAN_HASH_VLAUE=$2
SOURCE_CLUSTER=$3
TARGET_CLUSTER=$4
BEGIN_SNAP=$5
END_SNAP=$6

log_file=create_spb_aws_${SQL_ID}_${SQL_PLAN_HASH_VLAUE}_${SOURCE_CLUSTER}_${TARGET_CLUSTER}.lst
echo ""
echo "Copying plan for sql_id $SQL_ID from cluster# $SOURCE_CLUSTER to cluster# $TARGET_CLUSTER for plan hash value $SQL_PLAN_HASH_VLAUE"
echo ""
# env file
source /l/orcl
sqlplus -s sbg_vdba/"U4HiW#f1bQ)x"@pspuwp01 @create_spb_aws_awr.sql $SQL_ID $SQL_PLAN_HASH_VLAUE $SOURCE_CLUSTER $TARGET_CLUSTER $log_file $BEGIN_SNAP $END_SNAP

if [ `egrep "Usage 1: sqlplus|ORA-" $log_dir/$log_file | grep -v "ORA-14552"|wc -l` -gt 0 ]; then
  echo "PERF-PSP: Failed to copy plan for $SQL_ID from $SOURCE_CLUSTER to $TARGET_CLUSTER for plan hash $SQL_PLAN_HASH_VLAUE "
else
  echo "PERF-PSP: Successfully copied plan for $SQL_ID from $SOURCE_CLUSTER to $TARGET_CLUSTER for plan hash $SQL_PLAN_HASH_VLAUE "
fi
