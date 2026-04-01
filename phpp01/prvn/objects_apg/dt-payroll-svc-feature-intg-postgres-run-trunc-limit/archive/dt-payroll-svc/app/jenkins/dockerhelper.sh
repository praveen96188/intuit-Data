#!/bin/sh
version=$1
pspDBImage=$2
dockerTagTimeStamp=$3

echo "Inside Docker Helper script"
echo "Argumetns passed - version=${version}, pspDBImage=${pspDBImage}, dockerTagTimeStamp=${dockerTagTimeStamp}"

nexusUrl="https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Releases/"

SUB="SNAPSHOT"
case $version in

*"$SUB"*)
  echo "SNAPSHOT Matched"
  nexusUrl="https://artifact.intuit.com/artifactory/SBG.Payments.Intuit-Snapshots/"
  ;;
esac
echo "Nexus url for Docker passed - ${nexusUrl}"
docker build --build-arg PROJECT_VERSION=${version} --build-arg NEXUS_URL=${nexusUrl} --no-cache -t ${pspDBImage} -f docker/Dockerfile .
docker tag ${pspDBImage} ${dockerTagTimeStamp}
docker push ${dockerTagTimeStamp}
echo "PSP Docker DB pushed successfully"
