#!/bin/sh

export JAVA_HOME=/u01/veridata/jdk1.8/jdk1.8.0_131/
export JRE_HOME=/u01/veridata/jdk1.8/jdk1.8.0_131/
export PATH=$JAVA_HOME/bin:$PATH
export PATH=$JRE_HOME/bin:$PATH
JOB_NAME=$1
ARG_CNT=$#
OOS="-rR"

## Change the path to the script location.
cd /u01/veridata/wls/user_projects/domains/base_domain/veridata/bin/


## Job Execution based on the Arguments Passed

if [ "$ARG_CNT" -eq 1 ] ; then
      ./vericom.sh -wluserAlias vduser -j $1
elif [ "$ARG_CNT" -eq 2 ] ; then
       if [[ "$2" =~ ^[0-9]+$ ]] ;then
         ./vericom.sh -wluserAlias vduser -j $1 -rN $2
       elif [[ "$2" == "$OOS" ]] ;then
         ./vericom.sh -wluserAlias vduser -j $1 -rR
       fi
else [ "$ARG_CNT" -eq 3 ] ;
       ./vericom.sh -wluserAlias vduser -j $1 -g $2 -c $3
fi

## Job Status Check
if [ $? == 0 ];
 then
   echo "."
   echo "."
   echo "."
   echo "$JOB_NAME is Sucessfully Completed !!!"
   echo "."
   echo "."
else  
   echo "."
   echo "."
   echo "$JOB_NAME has Out-Of-Sync compare pairs or has Failed..Please Re-run and Check the status"
   echo "."
   echo "."
fi

exit
