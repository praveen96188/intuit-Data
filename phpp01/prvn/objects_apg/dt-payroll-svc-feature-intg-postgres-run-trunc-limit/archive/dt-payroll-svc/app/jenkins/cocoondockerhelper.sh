#!/bin/sh
dockerTagTimeStamp=$1

echo "Inside Coocoon Docker Helper script"
echo "Arguments passed - dockerTagTimeStamp=${dockerTagTimeStamp}"
rm -rf /root/.m2/repository/com/intuit/sbg/psp

#you might be wondering, why do we need temp dir, unfortunately, docker can only read from the sub dirs where docker command is run
mkdir temp
echo "Created temp dir"
cp -r /root/.m2 temp/.

docker build  --no-cache -t ${dockerTagTimeStamp} -f docker/CocoonDockerfile .
docker push ${dockerTagTimeStamp}
echo "PSP M2 Docker DB pushed successfully"
echo "cleaning temp"
rm -rf temp/

