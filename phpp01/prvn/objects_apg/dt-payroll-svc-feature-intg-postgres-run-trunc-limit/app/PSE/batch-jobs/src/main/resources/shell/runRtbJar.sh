#!/bin/sh


# Quick check of parameter count
if [ $# -ne 4 ] ; then
        echo "Incorect number of parameters.  Number passed in: $#"
        echo "  Usage:  ./runRtbJar.sh Release build mainClass filename"
        echo "  Release:        Release Identifier. eg, 2013R8"
        echo "  Build:          Build id. eg, 6"
        echo "  mainClass:      Class to execute. eg, com.intuit.sbd.payroll.psp.CutoverCleanup"
        echo "  filename:       Either a file to read or 'false' if there is no file"
        exit 1
fi


#Make sure user is flux
if [ "$USER" != "flux" ]; then
         echo "You must be flux user to run this script."
         exit 1
fi


#Setup the environment
LIB_DIR="/apps/batch/flux/shell"
ORS="http://oprdpbdws800.ie.intuit.net/ors/build/psp"
SAVED_JAR="rtb_$1_$2.jar"



# Get rtb.jar and rename it
echo "File to save: ${SAVED_JAR}"
echo "File to get:  ${ORS}$1/$2/RTB.jar"
wget --output-document=${SAVED_JAR} ${ORS}$1/$2/RTB.jar
if [ $? = "0" ] ; then
	echo "INFO: Successfully retrieved file"
else
	echo "FATAL: Could NOT download file: ${ORS}$1/$2/RTB.jar"
	exit 1
fi


# Set up the environment
. ./setenv.sh
export LOGGER_PROPS="-Deventlogger.environment=${env} -Deventlogger.architecture=PSP -Deventlogger.application=run_the_business"


# Determine if a file has been passed in and process accordingly
if [ $4 != "false" -a -e $4 ] ; then
	dos2unix $4
        while read line
        do
             echo $line
             $JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH:${LIB_DIR}/${SAVED_JAR} ${3} ${line} > $BE_LOG/${3}.out.`date +%Y%m%d%H%M%S` 2>&1
        done <$4
        
        exit $?


elif [ $4 == "false" ] ; then
        echo "There is no file to process - calling main class"
        $JAVA_HOME/java $JAVA_OPTS $LOGGER_PROPS -classpath $BE_CLASSPATH:${LIB_DIR}/${SAVED_JAR} ${3} > $BE_LOG/${3}.out.`date +%Y%m%d%H%M%S` 2>&1
	
	exit $?

elif [ ! -e $4 ] ; then
     echo "File Does Not Exist"
     exit 1

else
     echo "Unknown problem with fileName parameter: ${4} - Exiting"
     exit 1

fi
