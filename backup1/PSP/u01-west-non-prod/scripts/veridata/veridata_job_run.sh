#!/bin/sh

#==========================================================================================================================================================================================#
# 
#    ##Script Usage 
#
#    ## To Run The Full Job with Default Profile Settings, just pass the JOB_NAME as below
#       --  ## /bin/sh /u01/scripts/veridata/veridata_job_run.sh JOB_NAME
#             ## Example - 
#                #- /bin/sh /u01/scripts/veridata/veridata_job_run.sh TEST_IMPORT_JOB
#
#
#    ## To Run full JOB with overriding the default parallelism, pass the JOB_NAME and NO_OF_PARALLEL_THREADS as arguments
#       -- ## /bin/sh /u01/scripts/veridata/veridata_job_run.sh JOB_NAME NO_OF_PARALLEL_THREADS
#             ## Example - 
#                 #- /bin/sh /u01/scripts/veridata/veridata_job_run.sh TEST_IMPORT_JOB 15
#
#
#    ## To Run the specific compare pair with Default Profile Settings,we have to pass the JOB_NAME,GROUP_NAME and COMPARE_PAIR as arguments as below 
#       -- ## /bin/sh /u01/scripts/veridata/veridata_job_run.sh  JOB_NAME GROUP_NAME COMPARE_PAIR
#             ## Example - 
#                 #- /bin/sh /u01/scripts/veridata/veridata_job_run.sh TEST_IMPORT_JOB TEST_Delta_PREPROD_PSPE2EUW_Range_TO_PPSPHP01_Hash PSP_COMPANY_EVENT=PSP_COMPANY_EVENT
#
#==========================================================================================================================================================================================#


JOB_NAME=$1
GROUP_NAME=$2
COMPARE_PAIR=$3
ARGUMENT_COUNT=$#
day=`date +'%A'`
#date=$(date +"%Y-%m-%d")
date=$(date '+%F_%H-%M-%S')
START_TIME=$(date '+%F_%H:%M:%S')
the_host=`hostname`

## Log File Generated on Execution Time Basis
logfile=/u01/scripts/veridata/log/${JOB_NAME}_veridata_job_run_${date}.log
logfile_OOS=/u01/scripts/veridata/log/${JOB_NAME}_veridata_job_run_${date}_OOS.log

echo "." >> $logfile
echo "Compare Job Started at $START_TIME" >> $logfile
echo "." >> $logfile

. /l/orcl

export AWS_DEFAULT_REGION=`curl --noproxy "*" -s "http://169.254.169.254/latest/meta-data/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`
#AWS_DEFAULT_REGION=us-east-2

if [ "${VPC_ENV}" == "ppd" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:152430470825:gg-sbg-psp-${VPC_ENV}-a-intuit-com"
elif [ "${VPC_ENV}" == "prod" ]; then
  topic_arn="arn:aws:sns:${AWS_DEFAULT_REGION}:893547637742:gg-sbg-psp-${VPC_ENV}-a-intuit-com"
else
  echo "Wrong VPC_ENV"
  exit 1
fi


## Function to Check The Arguments If  Valid 
ARGUMENT_CHECK ()
{ 
 if [[ "$ARGUMENT_COUNT" -lt 1 || "$ARGUMENT_COUNT" -gt 3 ]]
  then
   echo "."
   echo "."
   echo "."
   echo " ERROR : Either No Arguments are supplied OR Only JOBNAME and GROUPNAME is provided, please provide Compare Pair Name as well with the Group name. \n 
          JobName and GroupName doesnt work, Compare pair has to be provided with the GroupName when used. \n
          '\n
          '\n
          '\n
          ***  PLEASE CHECK THE SCRIPT USAGE COMMENTS IN SCRIPT HEADER   *** "  >> $logfile
  exit 1
 fi
}

## Function to Check The Final JOB Status After Execution
JOB_FINAL_STATUS ()
{
    if [ "$ARGUMENT_COUNT" -eq 1 ]; 
       then
        echo 'COMPARE JOB FINAL STATUS - JOB: "'$JOB_NAME'" IS COMPLETED SUCCESSFULLY !!!' >> $logfile
     elif [ "$ARGUMENT_COUNT" -eq 2 ];
       then
        echo 'COMPARE JOB FINAL STATUS - JOB:  "'$JOB_NAME'" IS COMPLETED SUCCESSFULLY !!!' >> $logfile
     else [ "$ARGUMENT_COUNT" -eq 3 ]
        echo 'COMPARE JOB FINAL STATUS - JOB: "'$JOB_NAME'" --> GROUP:"'$GROUP_NAME'" --> COMPARE_PAIR:"'$COMPARE_PAIR'" IS COMPLETED SUCCESSFULLY !!!' >> $logfile
     fi
}

## Function to PRINT the Number Of Arguments In the Logfile
ARGUMENT_COUNT_PRINT ()
{ 
 if [ "$ARGUMENT_COUNT" -eq 1 ]
  then
    echo "." >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
    echo "Number of Arguments Passed are $ARGUMENT_COUNT - JOB: $JOB_NAME " >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
elif  [ "$ARGUMENT_COUNT" -eq 2 ]
  then
    echo "." >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
    echo "Number of Arguments Passed are $ARGUMENT_COUNT - JOB: $JOB_NAME, PARALLEL: $GROUP_NAME " >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
else 
    echo "." >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
    echo "Number of Arguments Passed are $ARGUMENT_COUNT - JOB: $JOB_NAME, GROUP: $GROUP_NAME, COMPARE_PAIR: $COMPARE_PAIR " >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
    echo "." >> $logfile
 fi
}

EMAIL_SUCCESS ()    
{
 the_subject="SUCCESS: Veridata JOB Completed On ${the_host}"
 /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
}  

EMAIL_FAILURE ()
{
if [[ "$ERRORS" != 0 && "$CANCELLED" != 0 && "$OUT_OF_SYNC" != "0" ]]; then
     the_subject="FAILED: Veridata JOB has ERRORS,OOS & CANCELLED jobs on ${the_host}"
     /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
elif [[ ${ERRORS} != "0" && ${OUT_OF_SYNC} != "0" ]]; then
      the_subject="FAILED: Veridata JOB Failed With ERRORS and OUT_OF_SYNC on ${the_host}"
      /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
elif [[ ${ERRORS} != "0" && ${CANCELLED} != "0" ]]; then
      the_subject="FAILED: Veridata JOB has ERRORS And Jobs Are CANCELLED on ${the_host}"
      /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
elif [[ ${CANCELLED} != "0" && ${OUT_OF_SYNC} != "O" ]]; then
      the_subject="FAILED: Veridata JOB has OUT_OF_SYNC And Jobs Are CANCELLED on ${the_host}"
      /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
elif [ ${ERRORS} != "0" ]; then
      the_subject="FAILED: Veridata JOB has Failed With ERRORS on ${the_host}"
      /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
elif [ ${CANCELLED} != "0" ]; then
      the_subject="FAILED: Veridata JOB has Failed With Cancelled Compare Pairs on  ${the_host}"
      /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
elif [ ${OUT_OF_SYNC} != "0" ]; then
      the_subject="FAILED : Veridata JOB has Failed with OUT_OF_SYNC on ${the_host}"
      /usr/bin/aws sns publish --topic-arn ${topic_arn} --subject "${the_subject}" --message "`cat ${logfile}`"
fi
}

## Calling Function ARGUMENT_CHECK To Check The Arguments Are Appropriate
ARGUMENT_CHECK

## Calling Function ARGUMENT_PRINT_COUNT to print the arguments in logfile
ARGUMENT_COUNT_PRINT

## Execute Actual Job 
/bin/sh /u01/scripts/veridata/veridata_job_parallel.sh $1 $2 $3  >> $logfile

COMPLETION_TIME=$(date '+%F_%H:%M:%S')

ERRORS=$(awk -F ': ' '{print $3}' <<< `grep -w "OGGV-20027" $logfile`)
CANCELLED=$(awk -F ': ' '{print $3}' <<< `grep -w "OGGV-20029" $logfile`)
OUT_OF_SYNC=$(awk -F ': ' '{print $3}' <<< `grep -w "OGGV-20068" $logfile`)
SYNC_STATUS=$(awk -F ': ' '{print $3}' <<< `grep -w "OGGV-20039" $logfile`)
Job_Report_Filename=$(awk -F ': ' '{print $3}' <<< `grep -w "OGGV-20030" $logfile`)
Compare_Pair_Report_Filename=$(awk -F ': ' '{print $3}' <<< `grep -w "OGGV-20030" $logfile`)


## Check the Job Status and Errors If Reported Any
if [[ "$ERRORS" == 0 && "$CANCELLED" == 0 && "$OUT_OF_SYNC" == 0 && "$SYNC_STATUS" == 'In-Sync' ]];
    then
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           JOB_FINAL_STATUS
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "Compare Job Ended at $COMPLETION_TIME" >> $logfile
           echo "." >> $logfile
           EMAIL_SUCCESS
    elif [ "${OUT_OF_SYNC}" != "0" ]; then
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "Waiting for IN-FLIGHT Transactions If Any to Catch-up" >> $logfile 
           sleep 300
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "Executing the Only the OOS from Previous OUT-OF-SYNC reported">> $logfile 
           /bin/sh /u01/scripts/veridata/veridata_job_parallel.sh $1 -rR  >> $logfile_OOS
           cat $logfile_OOS >> $logfile
           OUT_OF_SYNC_NEW=$(awk -F ': ' '{print $3}' <<< `grep -w "OGGV-20068" $logfile_OOS`)
              if [ ${OUT_OF_SYNC_NEW} != "0" ]; then
                echo "." >> $logfile
                echo "." >> $logfile
                echo "." >> $logfile
                echo "." >> $logfile
                echo 'COMPARE JOB FINAL STATUS "'$JOB_NAME'" HAS COMPARE PAIRS OUT-OF-SYNC..PLEASE GO THROUGH THE LOGS AND VERIFY'  >> $logfile
                echo "." >> $logfile
                echo "." >> $logfile
                echo "." >> $logfile
                echo "." >> $logfile
                echo "Compare Job Ended at $COMPLETION_TIME" >> $logfile
                echo "." >> $logfile
                EMAIL_FAILURE
              else 
                EMAIL_SUCCESS
             fi      
    else
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo 'COMPARE JOB FINAL STATUS "'$JOB_NAME'" HAS COMPARE PAIRS HAS CANCELLED OR HAS REPORTED ERRORS..PLEASE GO THROUGH THE LOGS AND VERIFY'  >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "." >> $logfile
           echo "Compare Job Ended at $COMPLETION_TIME" >> $logfile
           echo "." >> $logfile
           EMAIL_FAILURE
fi

exit
