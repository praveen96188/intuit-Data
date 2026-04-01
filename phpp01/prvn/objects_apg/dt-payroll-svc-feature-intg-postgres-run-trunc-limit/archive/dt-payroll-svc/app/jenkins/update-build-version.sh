#!/bin/bash
PROJECT_VERSION=$1

echo "Project version before version update"
echo ${PROJECT_VERSION}

# Check to see if PSP Project version has SNAPSHOT in it. If yes, then insert build number in between. If no, that means it is a PROD build. Use as is.
if [[ $PROJECT_VERSION == *SNAPSHOT ]]
then
    NEW_PROJECT_VERSION=${PROJECT_VERSION%SNAPSHOT}${BUILD_NUMBER}-SNAPSHOT
    NEW_PSP_PROJECT_VERSION=${PROJECT_VERSION%SNAPSHOT}${BUILD_NUMBER}-SNAPSHOT
else
    NEW_PROJECT_VERSION=$PROJECT_VERSION
    NEW_PSP_PROJECT_VERSION=$PROJECT_VERSION
fi

echo "Project version after version update"
echo ${NEW_PROJECT_VERSION}
echo ${NEW_PSP_PROJECT_VERSION}

cd app
echo "Updating project version..."
# Updating modified version in main project's pom.xml. We are modifying this version to include the build_number of the job in the middle of
# it to recognize and download it from within nexus(http://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Snapshots/com/intuit/sbg/psp/)
#Adding -DprocessAllModules as to update all modules regardless of parent child module as dt-payroll-svc-aggregator's pom.xml is not parent of app's pom.xml
mvn versions:set -DnewVersion=${NEW_PROJECT_VERSION} -s ./jenkins/settings.xml
mvn versions:commit

# Updating modified version in child project's pom.xml. We are modifying this version to include the build_number of the job in the middle of
# it to recognize and download it from within nexus(http://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Snapshots/com/intuit/sbg/psp/)
sed -i -e "s/${PROJECT_VERSION}/${NEW_PSP_PROJECT_VERSION}/g" ${WORKSPACE}/app/pom.xml