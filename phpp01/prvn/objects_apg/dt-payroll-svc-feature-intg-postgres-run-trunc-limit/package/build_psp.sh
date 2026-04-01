#!/bin/sh
db_build=$1
project_version=$2
pom_version=$3

#db_build=false
#project_version="3.3.0-SNAPSHOT"
#BUILD_NUMBER=11



cd app/

echo "============== START::Manually downloading asm-attrs jars=============="
curl https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Releases/localRepo/asm/asm-attrs/1.5.3/asm-attrs-1.5.3.jar  --output  /tmp/asm-attrs-1.5.3.jar
echo "Manually installing asm-attrs jars..."
mvn install:install-file -Dfile=/tmp/asm-attrs-1.5.3.jar -DgroupId=asm -DartifactId=asm-attrs -Dversion=1.5.3 -Dpackaging=jar
ls -ltr /root/.m2/repository/asm/asm-attrs/1.5.3

echo "============== END::Manually downloading asm-attrs jars=============="


echo "============== START::DB Connect when db build is enabled =============="

echo "db_build parameter:: $db_build"
if [ "$db_build" == "No" ]
then
  touch ./PSE/domain/skip-install-db.txt
  touch ./PSE/domain-secondary/skip-install-db.txt
else
  echo "Trying to Connect  to DB"
  c=1
  echo "exit" | sqlplus -L PSP_LOCAL/PSP_LOCAL@XE | grep Connected > /dev/null
  while [[ ("$?" -ne "0")  && ("$c" -le "20") ]]
  do
    sleep 5
    echo "ReTrying to connect to DB...$c"
    let c=c+1
    echo "exit" | sqlplus -L PSP_LOCAL/PSP_LOCAL@XE | grep Connected > /dev/null
  done
  echo "Successfully Connected to DB after $c attemps"
fi
echo "============== END::DB Connect when db build is enabled =============="




echo "============== START::Local POM update =============="
pwd

mvn versions:set -DnewVersion=${project_version}  -s ./jenkins/settings.xml
mvn versions:commit

sed -i -e "s/${pom_version}/${project_version}/g" ${WORKSPACE}/app/pom.xml

echo "============== END::Local POM update =============="




echo "============== START::Build PSP and PUSH to nexus =============="

nexusUrl="https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Releases/";

SUB="SNAPSHOT"
case $project_version in

  *"$SUB"*)
    echo "SNAPSHOT Matched"
    nexusUrl="https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Snapshots/"
    ;;
esac

echo "Nexus url is - ${nexusUrl} and version to be pushed to nexus ${project_version}"
pwd

echo "mvn -B -DskipTests -s ./jenkins/settings.xml clean deploy"
mvn -B -DskipTests -s ./jenkins/settings.xml clean deploy


echo "Deploying psp-dbinstall-${project_version}.zip to nexus - ${nexusUrl}"
mvn deploy:deploy-file \
    -s ./jenkins/settings.xml \
    -DrepositoryId=scm.dev.snap.repo \
    -Denvironment=build \
    -Pnexus-deploy \
    -Durl=${nexusUrl} \
    -DgroupId=com.intuit.sbg.psp \
    -DartifactId=psp-dbinstall \
    -Dversion=${project_version} \
    -Dpackaging=zip \
    -Dfile=PSE/domain/target/psp-dbinstall-${project_version}.zip

echo "Deploying psp-dbinstall-secondary-${project_version}.zip to nexus - ${nexusUrl}"
mvn deploy:deploy-file \
    -s ./jenkins/settings.xml \
    -DrepositoryId=scm.dev.snap.repo \
    -Denvironment=build \
    -Pnexus-deploy \
    -Durl=${nexusUrl} \
    -DgroupId=com.intuit.sbg.psp \
    -DartifactId=psp-dbinstall-secondary \
    -Dversion=${project_version} \
    -Dpackaging=zip \
    -Dfile=PSE/domain-secondary/target/psp-dbinstall-secondary-${project_version}.zip

echo "Deploying psp-configuration-${project_version}.zip to nexus - ${nexusUrl}"
mvn deploy:deploy-file \
    -Denvironment=build -s ./jenkins/settings.xml -Pnexus-deploy \
    -Durl=${nexusUrl} \
    -DrepositoryId=scm.dev.snap.repo \
    -DgroupId=com.intuit.sbg.psp \
    -DartifactId=psp-configuration \
    -Dversion=${project_version} \
    -Dpackaging=zip \
    -Dfile=PSE/configuration/target/psp-configuration-${project_version}.zip

echo "==============B =============="