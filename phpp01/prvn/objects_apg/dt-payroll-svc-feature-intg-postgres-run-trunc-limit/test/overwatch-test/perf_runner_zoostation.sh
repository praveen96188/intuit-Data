#!/bin/sh

if [ -z $GIT_REPO ]; then
    GIT_REPO=$1
fi
if [ -z $BUILD_URL ]; then
    BUILD_URL=$2
fi
if [ -z $ENV ]; then
    ENV=$3
fi

echo "*** perf Runner ***"
echo $GIT_REPO
echo "*** *** *** "
echo
echo ===================================================== Zoo Station Push - Start =====================================================
echo

JOB_NAME='PERF_CI'
TEST_GROUP='PERF_CI'
BUILD_NUMBER='NA'

MY_RUN_ID=`date +%s`

GRADE_THRESHOLD=2000
artifact_parent_location=https://artifact.intuit.com/nexus/content/repositories/SBG.QBO.Intuit-Releases/perf/runner/perf_runner

perf_runner_directory=`pwd`/gatling_lib
perf_runner_path=$perf_runner_directory/perf_runner.jar

sla_directory=`pwd`/src/test/resources/data/SLA

gatling_results_directory=`pwd`/gatling_results

gatling_output_directory=`ls $gatling_results_directory | sort -r | head -1`

echo "Checking for Perf Dir"
if [ ! -d $perf_runner_directory ]
then
	mkdir $perf_runner_directory
fi
echo $perf_runner_directory

echo "Checking for Perf Runner Path"
if [ ! -f $perf_runner_path ]
then
	latest_version=`curl -s $artifact_parent_location/maven-metadata.xml | grep release | sed 's/    <release>//g' | sed 's/<\/release>//g'`
	wget -O $perf_runner_path $artifact_parent_location/$latest_version/perf_runner-$latest_version-all.jar
	#curl -O $perf_runner_path $artifact_parent_location/$latest_version/perf_runner-$latest_version-all.jar
fi

if [ ! -f $gatling_results_directory/$gatling_output_directory/results/payload.json ]
then
	java -cp $perf_runner_path cruncher -r $gatling_results_directory/$gatling_output_directory -v gatling
fi

echo "DIR PATH"
echo $gatling_results_directory/$gatling_output_directory



echo java -cp $perf_runner_path emailAndEmit -j $JOB_NAME -b $BUILD_NUMBER -t $TEST_GROUP -i $MY_RUN_ID -o $gatling_results_directory/$gatling_output_directory -z -a SoOF1KnVklcOjvdyXUMtYiqONdU= -x $BUILD_URL -u $GIT_REPO -s SUCCESS

java -cp $perf_runner_path emailAndEmit -j $JOB_NAME -b $BUILD_NUMBER -t $TEST_GROUP -i $MY_RUN_ID -o $gatling_results_directory/$gatling_output_directory -z -a SoOF1KnVklcOjvdyXUMtYiqONdU= -x $BUILD_URL -n $ENV -u $GIT_REPO -s SUCCESS

echo
echo ===================================================== Zoo Station Push - End =====================================================
echo