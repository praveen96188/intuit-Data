#!/bin/sh
version=$1
SkipInstallDB=$2

echo "Version building is ${version}"
echo "Build with Full database - ${SkipInstallDB}"

if [ "${SkipInstallDB}" = "YES" ]; then
    echo "Skipping DB Install"
    touch app/PSE/domain/skip-install-db.txt
    touch app/PSE/domain-secondary/skip-install-db.txt
fi

nexusUrl="https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Releases/";
#nexusUrl="https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Snapshots/";

SUB="SNAPSHOT"
case $version in

  *"$SUB"*)
    echo "SNAPSHOT Matched"
    nexusUrl="https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Snapshots/"
    ;;
esac

echo "Nexus url is - ${nexusUrl}"

cd /tmp

mkdir -p /root/.m2/repository && cp -R localRepo /root/.m2/repository/

echo "Manually downloading asm-attrs jars..."
curl https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Releases/localRepo/asm/asm-attrs/1.5.3/asm-attrs-1.5.3.jar  --output  /tmp/asm-attrs-1.5.3.jar

echo "Manually installing asm-attrs jars..."
mvn install:install-file -Dfile=/tmp/asm-attrs-1.5.3.jar -DgroupId=asm -DartifactId=asm-attrs -Dversion=1.5.3 -Dpackaging=jar

ls -ltr /root/.m2/repository/asm/asm-attrs/1.5.3

cd -
pwd

echo "mvn -B -DskipTests -s ./jenkins/settings.xml clean deploy"
cd app
mvn -B -DskipTests -s ./jenkins/settings.xml clean deploy

echo "Deploying psp-dbinstall-${version}.zip to nexus - ${nexusUrl}"
mvn deploy:deploy-file \
    -s ./jenkins/settings.xml \
    -DrepositoryId=scm.dev.snap.repo \
    -Denvironment=build \
    -Pnexus-deploy \
    -Durl=${nexusUrl} \
    -DgroupId=com.intuit.sbg.psp \
    -DartifactId=psp-dbinstall \
    -Dversion=${version} \
    -Dpackaging=zip \
    -Dfile=PSE/domain/target/psp-dbinstall-${version}.zip

echo "Deploying psp-dbinstall-secondary-${version}.zip to nexus - ${nexusUrl}"
mvn deploy:deploy-file \
    -s ./jenkins/settings.xml \
    -DrepositoryId=scm.dev.snap.repo \
    -Denvironment=build \
    -Pnexus-deploy \
    -Durl=${nexusUrl} \
    -DgroupId=com.intuit.sbg.psp \
    -DartifactId=psp-dbinstall-secondary \
    -Dversion=${version} \
    -Dpackaging=zip \
    -Dfile=PSE/domain-secondary/target/psp-dbinstall-secondary-${version}.zip

echo "Deploying psp-configuration-${version}.zip to nexus - ${nexusUrl}"
mvn deploy:deploy-file \
    -Denvironment=build -s ./jenkins/settings.xml -Pnexus-deploy \
    -Durl=${nexusUrl} \
    -DrepositoryId=scm.dev.snap.repo \
    -DgroupId=com.intuit.sbg.psp \
    -DartifactId=psp-configuration \
    -Dversion=${version} \
    -Dpackaging=zip \
    -Dfile=PSE/configuration/target/psp-configuration-${version}.zip
