#!/bin/sh
enableCocoon=$1
branchName=$2
dockerTagTimeStamp=docker.intuit.com/payroll/dtpayroll/dt-payroll-svc/service/dt-payroll-svc/pspmavensqlcocoon:ikslatest

echo "enableCocoon value::${enableCocoon}"
echo "BranchName::${branchName}"

if [ $enableCocoon == true ] && [[ $branchName != PR-* ]]
then
  echo "Inside Coocoon Docker Helper script"
  rm -rf /root/.m2/repository/com/intuit/sbg/psp

  #you might be wondering, why do we need temp dir, unfortunately, docker can only read from the sub dirs where docker command is run
  mkdir temp
  echo "Created temp dir"
  cp -r /root/.m2 temp/.

  docker build  --no-cache -t ${dockerTagTimeStamp} -f ./package/CocoonDockerfile .
  docker push ${dockerTagTimeStamp}
  echo "PSP M2 Docker DB pushed successfully"
  echo "cleaning temp"
  rm -rf temp/
else
 echo "Coocoon push disabled when it's PR OR enableCocoon is false"
fi