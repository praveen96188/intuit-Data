#!/bin/bash -ex
export PATH=$PATH:/usr/local/bin:/ops/bin

# export variables
for line in `cat /etc/intu_metadata/app.ini`
do
  export $line
done

# Prevent instance from terminating due to full root disk
sed -i -e 's/halt/SUSPEND/' /etc/audit/auditd.conf
sed -i -e 's/keep_logs/rotate/' /etc/audit/auditd.conf
service auditd restart 

# Locale
export LANG=C.UTF-8
export LC_ALL=en_US.UTF-8
sed -i.bak '1 s|UTC|America/Los_Angeles|' /etc/sysconfig/clock
ln -sf /usr/share/zoneinfo/America/Los_Angeles /etc/localtime

# Instance Metadata

INTU_METADATA=/etc/intu_metadata
mkdir -p $INTU_METADATA
chmod 755 $INTU_METADATA

export METADATA=http://169.254.169.254/latest/meta-data

export AWS_REGION=`curl --noproxy "*" -s "${METADATA}/placement/availability-zone" | awk '{print substr($0, 0, length($0) - 1)}'`
echo AWS_REGION=${AWS_REGION} >> ${INTU_METADATA}/app.ini

chmod 755 ${INTU_METADATA}/app.ini

export AWS_AZ=`curl --noproxy "*" -s "${METADATA}/placement/availability-zone" | sed -e 's/\\s.*//'`
echo AWS_AZ=${AWS_AZ} >> ${INTU_METADATA}/app.ini
az=`echo ${AWS_AZ} |rev |cut -c 1`

export TOKEN=$(curl --noproxy "*" -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600" -s)
export ACCOUNT_ID=$(curl --noproxy "*" -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/dynamic/instance-identity/document | /usr/local/bin/jq -r '.accountId')

echo ACCOUNT_ID=${ACCOUNT_ID} >> ${INTU_METADATA}/app.ini

export INSTANCE_ID=`curl --noproxy "*" -s "${METADATA}/instance-id" | sed -e 's/\\s.*//'`
echo INSTANCE_ID=${INSTANCE_ID} >> ${INTU_METADATA}/app.ini

# Get metadata
export INSTANCE_METADATA=`aws --region $AWS_REGION ec2 describe-instances --instance-ids $INSTANCE_ID`

export STACK_ID=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "aws:cloudformation:stack-id").Value'`
echo STACK_ID=$STACK_ID >> ${INTU_METADATA}/app.ini

export STACK_NAME=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "aws:cloudformation:stack-name").Value'`
echo STACK_NAME=$STACK_NAME >> ${INTU_METADATA}/app.ini

export LOGICAL_ID=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "aws:cloudformation:logical-id").Value'`

echo LOGICAL_ID=$LOGICAL_ID >> ${INTU_METADATA}/app.ini

export APP_NAME=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "app-name").Value'`
echo APP_NAME=$APP_NAME >> ${INTU_METADATA}/app.ini

export APP_ENV=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "app-env").Value'`
echo APP_ENV=$APP_ENV >> ${INTU_METADATA}/app.ini

export VPC_ENV=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "vpc-env").Value'`
echo VPC_ENV=$VPC_ENV >> ${INTU_METADATA}/app.ini

export GOLDENGATE_VOLUME_ID=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "goldengate-volume-id").Value'`
echo GOLDENGATE_VOLUME_ID=$GOLDENGATE_VOLUME_ID >>  ${INTU_METADATA}/app.ini

export NAME=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "Name").Value'`
echo NAME=$NAME >>  ${INTU_METADATA}/app.ini

export VHOSTNAME=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "hostname").Value'`
echo VHOSTNAME=$VHOSTNAME >> ${INTU_METADATA}/app.ini

export HOSTED_ZONE_ID=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "hosted-zone-id").Value'`
echo HOSTED_ZONE_ID=$HOSTED_ZONE_ID >> ${INTU_METADATA}/app.ini

export NOTIFICATION_SERVICE_ARN=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "notification-service-arn").Value'`
echo NOTIFICATION_SERVICE_ARN=$NOTIFICATION_SERVICE_ARN >> ${INTU_METADATA}/app.ini

export HA=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "ha").Value'`
echo HA=$HA >> ${INTU_METADATA}/app.ini

export KMS_ID=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "kms-id").Value'`
echo KMS_ID=$KMS_ID >> ${INTU_METADATA}/app.ini

export RDS_INSTANCE_NAME=`echo $INSTANCE_METADATA | jq -r '.Reservations[].Instances[].Tags[] | select (.Key == "rds-instance-name").Value'`
echo RDS_INSTANCE_NAME=$RDS_INSTANCE_NAME >> ${INTU_METADATA}/app.ini

# Set NTP
ntpd -q
service ntpd start
chkconfig ntpd on

#Network config
NAME_SERVER=`egrep -o '[0-9]+.[0-9]+.[0-9]+.[0-9]+' /etc/resolv.conf`
echo 'ALL: ALL' > /etc/hosts.allow
sed -i 's/PEERDNS=yes/PEERDNS=no/' /etc/sysconfig/network-scripts/ifcfg-eth0
LOCAL_IP=`curl --noproxy "*" -s "${METADATA}/local-ipv4"`

# Set hostname
export APP_ENV_PREFIX=`if [ "$APP_ENV" == "prod" ]; then echo "prd"; elif [ "$APP_ENV" == "perf" ]; then echo "prf"; elif [ "$APP_ENV" == "qa" ]; then echo "qal";else echo "$APP_ENV";  fi;`

export REGION_CODE=`echo $AWS_REGION | awk -F - '{print substr($1, 0, 1) substr($2, 0, 1) substr($3, 0, 1)}'`

export IPv4_ADDRESS=`curl --noproxy "*" -s "${METADATA}/local-hostname" | sed -e 's/\..*$//g' | gawk -F - '{ printf "%s.%s.%s.%s", $2,$3,$4,$5 }'`

LAST_2_OCTETS=`curl --noproxy "*" -s "${METADATA}/local-hostname" | sed -e 's/\..*$//g' | gawk -F - '{ printf "%03i%03i", $4,$5 }'`

HOSTNAME=gg${RDS_INSTANCE_NAME}${REGION_CODE}${ROLE_SUFFIX}${LAST_2_OCTETS}
sysctl kernel.hostname=${HOSTNAME}.${AWS_REGION}.sbg-${APP_NAME}-${VPC_ENV}.a.intuit.com

# Install dnsmasq
yum install dnsmasq -y -q

# Modify resolver
cp /etc/resolv.conf /etc/resolv.conf.dhcp
cat << EOF > /etc/resolv.conf
nameserver 127.0.0.1
search ${AWS_REGION}.compute.internal ${AWS_REGION}.sbg-${APP_NAME}-${VPC_ENV}.a.intuit.com
options timeout:2 attempts:1 rotate single-request-reopen
EOF

DNSMASQ_CONF_DIR=/etc/dnsmasq.d
cat << EOF > /etc/dnsmasq.conf
server=208.67.222.222
server=208.67.220.220
conf-dir=${DNSMASQ_CONF_DIR}
interface=lo
bind-interfaces
EOF

# Set IHP searches
cat << EOF > ${DNSMASQ_CONF_DIR}/ihp.conf
server=/intuit.net/10.180.192.18
server=/intuit.net/10.158.32.22
server=/ie.intuit.com/10.180.192.18
server=/ie.intuit.com/10.158.32.22
EOF

# Set non-IHP searches
# Used by old VPC
SEARCH_DOMAIN=${AWS_REGION}.compute.internal
cat << EOF > ${DNSMASQ_CONF_DIR}/${SEARCH_DOMAIN}.conf
server=/${SEARCH_DOMAIN}/${NAME_SERVER}
server=/amazonaws.com/${NAME_SERVER}
server=/10.in-addr.arpa/${NAME_SERVER}
server=/a.intuit.com/${NAME_SERVER}
EOF
# Used by new VPC
SEARCH_DOMAIN=${AWS_REGION}.vpc.internal
cat << EOF > ${DNSMASQ_CONF_DIR}/${SEARCH_DOMAIN}.conf
server=/${SEARCH_DOMAIN}/${NAME_SERVER}
server=/amazonaws.com/${NAME_SERVER}
server=/10.in-addr.arpa/${NAME_SERVER}
server=/a.intuit.com/${NAME_SERVER}
EOF

# Start dnsmasq daemon
chkconfig dnsmasq on
service dnsmasq restart

# Setup yum
sed -i 's/cds01/cds02/g' /etc/yum.repos.d/*.repo
cat > /etc/yum.repos.d/intu-packages-${AWS_REGION}.repo << EOS
[intu-packages-${AWS_REGION}]
name=Intuit Custom RPM Packages
baseurl=http://s3-${AWS_REGION}.amazonaws.com/intu-packages-${AWS_REGION}/rhel/6/x86_64
gpgcheck=0
enabled=1
EOS

# Download build artifacts
cd /var/tmp

# Install secrets-cli
aws --region ${AWS_REGION} --debug s3 cp s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/intuit/secrets-cli-2.4.0.0.rpm secrets-cli.rpm
yum install -y secrets-cli.rpm

# Take in Oracle for admin
if [ "$ROLE_SUFFIX" == "ada" ]; then 
  if [ "${APP_ENV}" == "prod" -o "${APP_ENV}" == "stg" ]; then ORACLE_SNAPSHOT=snap-ed87a1c3; else ORACLE_SNAPSHOT=snap-710d018b; fi
  VOLUME_ID=`aws --region $AWS_REGION ec2 create-volume --snapshot-id $ORACLE_SNAPSHOT --availability-zone $AWS_AZ | jq -r '.VolumeId'`
  sleep 60
  aws --region $AWS_REGION ec2 attach-volume --instance-id $INSTANCE_ID --device /dev/xvdo --volume-id $VOLUME_ID
fi

# Install SSM agent
aws --region ${AWS_REGION} --debug s3 cp s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/aws/amazon-ssm-agent.rpm /var/tmp/amazon-ssm-agent.rpm
yum install -y /var/tmp/amazon-ssm-agent.rpm

# Setup Chef solo
aws s3 --region ${AWS_REGION} cp s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/chef-12/chef.rpm chef.rpm
rpm -Uvh chef.rpm

mkdir -p -m 0700 /var/log/chef
ARTIFACT_KEY=goldengate_hub_ha_latest.tar

#Temporarily Download consul-template
mkdir -p /var/chef/cache
aws --region ${AWS_REGION} s3 cp s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/consul/consul-template_0.15.0_linux_amd64.zip /var/chef/cache/consul-template_0.15.0.zip
# Set Chef JSON attributes file

export PORT=9040

cat > /var/chef/attributes.json << EOF
{"application": {"db": "${RDS_INSTANCE_NAME}", "env": "${APP_ENV}", "env_prefix": "${APP_ENV_PREFIX}"}, "jk_port": "${PORT}"}
EOF
aws --region ${AWS_REGION} s3 cp s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/jq/jq-linux64 /usr/local/bin/jq
chmod +x /usr/local/bin/jq
/usr/local/bin/jq '.metadata.deployment.stack.id=env.STACK_ID' /var/chef/attributes.json > /tmp/attributes.json && mv /tmp/attributes.json /var/chef/attributes.json
/usr/local/bin/jq '.metadata.deployment.stack.name=env.STACK_NAME' /var/chef/attributes.json > /tmp/attributes.json && mv /tmp/attributes.json /var/chef/attributes.json
/usr/local/bin/jq '.metadata.deployment.stack.region=env.AWS_REGION' /var/chef/attributes.json > /tmp/attributes.json && mv /tmp/attributes.json /var/chef/attributes.json
/usr/local/bin/jq '.metadata.deployment.stack.region_code=env.REGION_CODE' /var/chef/attributes.json > /tmp/attributes.json && mv /tmp/attributes.json /var/chef/attributes.json
/usr/local/bin/jq '.metadata.deployment.stack.vpc.env=env.VPC_ENV' /var/chef/attributes.json > /tmp/attributes.json && mv /tmp/attributes.json /var/chef/attributes.json
/usr/local/bin/jq '.metadata.ec2.ipv4_address=env.IPv4_ADDRESS' /var/chef/attributes.json > /tmp/attributes.json && mv /tmp/attributes.json /var/chef/attributes.json
CONSUL_DC=sbg-${APP_NAME}-${VPC_ENV}
mkdir -p /etc/consul.d/
cat > /etc/consul.d/config-client.json << EOF
{"bind_addr": "$LOCAL_IP", "client_addr": "0.0.0.0", "node_name": "${HOSTNAME}", "log_level": "INFO",
"server": false, "rejoin_after_leave": true, "enable_syslog": true, "datacenter": "${CONSUL_DC}", "data_dir": "/var/consul", "retry_join": ["consul.${AWS_REGION}.compute.internal"] }
EOF

cat > /etc/consul.d/role.json << EOF
{"service": {"name": "${NAME}", "tags": ["${STACK_NAME}", "${ROLE_SUFFIX}", "${APP_ENV}"], "port": ${PORT} }}
EOF

# Download Golden Gate Chef artifact
cd /var/tmp

mkdir -p /var/chef/chef-repo

cd /var/chef/chef-repo
aws s3 cp s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/chef/${ARTIFACT_KEY} .
tar xvf ${ARTIFACT_KEY}

if [ "$HA" == "yes" ]; then
  # Attach elastic network interface
  sleep 60
  EniName=gg${RDS_INSTANCE_NAME}-${az}
  EniId=`aws --region ${AWS_REGION} ec2 describe-network-interfaces --filters "Name=tag:Name,Values=${EniName}" --query 'NetworkInterfaces[0].NetworkInterfaceId' |cut -d '"' -f2`

  aws --region ${AWS_REGION} ec2 attach-network-interface --instance-id $INSTANCE_ID --device-index 1 --network-interface-id ${EniId}
fi

# Attach GoldenGate EBS volume
aws --region ${AWS_REGION} ec2 describe-volumes --volume-ids ${GOLDENGATE_VOLUME_ID}  > /tmp/goldengate-volume.json
GoldenGateVolumeStatus=`cat /tmp/goldengate-volume.json | /usr/local/bin/jq -r '.Volumes[0].State'`
if [ "$GoldenGateVolumeStatus" == "available" ]; then 
  TOKEN=$(curl -X PUT -H "X-aws-ec2-metadata-token-ttl-seconds: 21600" -s "http://169.254.169.254/latest/api/token")
  PrivateIpAddress=$(curl -H "X-aws-ec2-metadata-token: $TOKEN" -s http://169.254.169.254/latest/meta-data/local-ipv4)
  if [ "$HA" == "yes" ]; then
    aws --region ${AWS_REGION} ec2 attach-volume --instance-id $INSTANCE_ID --device /dev/xvdm --volume-id ${GOLDENGATE_VOLUME_ID}
    MESSAGE="GG instance ${STACK_NAME} launched or rebooted: ec2 attach-volume --instance-id $INSTANCE_ID "
    MESSAGE="$MESSAGE --device /dev/xvdm --volume-id ${GOLDENGATE_VOLUME_ID}"
  else
    aws --region ${AWS_REGION} ec2 attach-volume --instance-id $INSTANCE_ID --device /dev/xvdn --volume-id ${GOLDENGATE_VOLUME_ID}
    MESSAGE="GG instance ${STACK_NAME} launched or rebooted: ec2 attach-volume --instance-id $INSTANCE_ID "
    MESSAGE="$MESSAGE --device /dev/xvdn --volume-id ${GOLDENGATE_VOLUME_ID}"
  fi
else
  MESSAGE="GG instance ${STACK_NAME} launched or rebooted: ec2 attach-volume --instance-id $INSTANCE_ID "
  MESSAGE="$MESSAGE BUT NO EBS VOLUME ATTACHMENT "
  MESSAGE=`echo $MESSAGE; cat /tmp/oracle-volume.json; echo ''; cat /tmp/goldengate-volume.json`
fi

# Send GoldenGate EBS Volume re-attachment notification
aws --region ${AWS_REGION} sns publish --topic-arn ${NOTIFICATION_SERVICE_ARN} --subject "STACK or REBOOT EVENT OCCURRED" --message "$MESSAGE"

# Find or define primary site
if [ `aws --region ${AWS_REGION} s3 ls s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/goldengate/ha/${APP_ENV}/gg${RDS_INSTANCE_NAME}.flag |wc -l` -gt 0 ]; then
  aws --region ${AWS_REGION} s3 cp s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/goldengate/ha/${APP_ENV}/gg${RDS_INSTANCE_NAME}.flag /tmp/gg${RDS_INSTANCE_NAME}.flag
else
  # Choose AZ a as primary site
  echo "a" > /tmp/gg${RDS_INSTANCE_NAME}.flag
  aws --region ${AWS_REGION} s3 cp /tmp/gg${RDS_INSTANCE_NAME}.flag s3://bits-sbg-${APP_NAME}-${VPC_ENV}-${AWS_REGION}/binaries/goldengate/ha/${APP_ENV}/gg${RDS_INSTANCE_NAME}.flag
fi

primary_site=`cat /tmp/gg${RDS_INSTANCE_NAME}.flag`
  
if [ "$HA" == "yes" ]; then
  # Reset VHOSTNAME
  OLD_VHOSTNAME=$VHOSTNAME
  VHOSTNAME_1=`echo $VHOSTNAME |cut -d'.' -f1|cut -d'-' -f1`
  VHOSTNAME_2=`echo $VHOSTNAME |cut -d'.' -f2-`
  PHOSTNAME="${VHOSTNAME_1}-${az}.${VHOSTNAME_2}" 
  if [ "${az}" != "${primary_site}" ]; then
    VHOSTNAME_1="${VHOSTNAME_1}-ha"
  fi
  export VHOSTNAME="${VHOSTNAME_1}.${VHOSTNAME_2}"
  sed -i "s/HOSTNAME=${OLD_VHOSTNAME}/HOSTNAME=${VHOSTNAME}/g" ${INTU_METADATA}/app.ini
else
  PHOSTNAME=${VHOSTNAME}
fi
hostname ${PHOSTNAME}
  
# Update /etc/hosts
ORIG_HOSTNAME=`curl --noproxy "*" -s "${METADATA}/local-hostname" | sed -e 's/\s.*//' | cut -d . -f 1`
PHOSTNAME_1=`echo ${PHOSTNAME} | cut -d . -f 1`
sed -i "s/${ORIG_HOSTNAME}.${AWS_REGION}.compute.internal/${PHOSTNAME}/g" /etc/hosts
sed -i "s/${ORIG_HOSTNAME}.${AWS_REGION}.vpc.internal/${PHOSTNAME}/g" /etc/hosts
sed -i "s/${ORIG_HOSTNAME}.internal/${PHOSTNAME}/g" /etc/hosts
sed -i "s/${ORIG_HOSTNAME}.vpc.internal/${PHOSTNAME}/g" /etc/hosts
sed -i "s/${ORIG_HOSTNAME}/${PHOSTNAME_1}/g" /etc/hosts

# Update Route53 Golden Gate record with new EC2 IP
IMDSV2_BASE_URL="http://169.254.169.254/latest/meta-data"
IMDSV2_TOKEN=$(curl -X PUT "$IMDSV2_BASE_URL/api/token" -H 'X-aws-ec2-metadata-token-ttl-seconds: 21600')
PrivateIpAddress=$(curl -H "X-aws-ec2-metadata-token: $IMDSV2_TOKEN" "$IMDSV2_BASE_URL/local-ipv4")

echo '{"Comment": "Route53 change",
"Changes": [{"Action": "UPSERT",
"ResourceRecordSet": {
"Name": "HOST_NAME",
"Type": "A", "TTL": 300,
"ResourceRecords": [{ "Value": "IP_ADDRESS" }]}}]}' > /tmp/recordset.json
sed -i "s/HOST_NAME/${VHOSTNAME}/g" /tmp/recordset.json
sed -i "s/IP_ADDRESS/$PrivateIpAddress/g" /tmp/recordset.json
cat /tmp/recordset.json
aws --region ${AWS_REGION} route53 change-resource-record-sets --hosted-zone-id ${HOSTED_ZONE_ID} --change-batch file:////tmp//recordset.json

if [ "$HA" == "yes" ]; then
  # Install and configure DRBD
  rpm -Uvh http://www.elrepo.org/elrepo-release-6-8.el6.elrepo.noarch.rpm
  yum update -y
  setenforce 0
  yum install -y kmod-drbd84

  lhost=gg${RDS_INSTANCE_NAME}-${az}
  if [ "${az}" == "a" ]; then
    rhost=gg${RDS_INSTANCE_NAME}-b
  else
    rhost=gg${RDS_INSTANCE_NAME}-a
  fi

  leni_ip=`aws --region ${AWS_REGION} ec2 describe-network-interfaces --filters "Name=tag:Name,Values=${lhost}" --query 'NetworkInterfaces[0].PrivateIpAddresses[0].PrivateIpAddress' |cut -d'"' -f2`
  reni_ip=`aws --region ${AWS_REGION} ec2 describe-network-interfaces --filters "Name=tag:Name,Values=${rhost}" --query 'NetworkInterfaces[0].PrivateIpAddresses[0].PrivateIpAddress' |cut -d'"' -f2`

  sed -i -e 's/net {$/net {   protocol C;/' /etc/drbd.d/global_common.conf

  gateway=`route |grep default |tr -s ' ' ' ' |cut -d ' ' -f2`
  netmask=`ifconfig eth0 |grep Mask |cut -d":" -f 4`
  eth0_ip=`ifconfig eth0 |grep Mask |cut -d":" -f 2 |cut -d ' ' -f1`

  echo "DEVICE=eth1" > /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "BOOTPROTO=static" >> /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "ONBOOT=yes" >> /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "TYPE=Ethernet" >> /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "USERCTL=yes" >> /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "PEERDNS=yes" >> /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "IPV6INIT=no" >> /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "IPADDR=${leni_ip}" >> /etc/sysconfig/network-scripts/ifcfg-eth1
  echo "NETMASK=$netmask" >> /etc/sysconfig/network-scripts/ifcfg-eth1

  echo "default via ${gateway} dev eth0 table 3" > /etc/sysconfig/network-scripts/route-eth0
  echo "default via ${gateway} dev eth1 table 2" > /etc/sysconfig/network-scripts/route-eth1
  echo "from ${eth0_ip}/32 table 3 priority 600" > /etc/sysconfig/network-scripts/rule-eth0
  echo "from ${leni_ip}/32 table 2 priority 600" > /etc/sysconfig/network-scripts/rule-eth1

  echo "#" > /etc/iproute2/rt_tables
  echo "# reserved values" >> /etc/iproute2/rt_tables
  echo "#" >> /etc/iproute2/rt_tables
  echo "2       table" >> /etc/iproute2/rt_tables
  echo "3       table2" >> /etc/iproute2/rt_tables
  echo "255     local" >> /etc/iproute2/rt_tables
  echo "254     main" >> /etc/iproute2/rt_tables
  echo "253     default" >> /etc/iproute2/rt_tables
  echo "0       unspec" >> /etc/iproute2/rt_tables
  echo "#" >> /etc/iproute2/rt_tables
  echo "# local" >> /etc/iproute2/rt_tables
  echo "#" >> /etc/iproute2/rt_tables
  echo "#1      inr.ruhep" >> /etc/iproute2/rt_tables
  echo "" >> /etc/iproute2/rt_tables

  echo "resource r0 {" > /etc/drbd.d/r0.res
  echo "  on ${lhost}.${VHOSTNAME_2} {" >> /etc/drbd.d/r0.res
  echo "    device    /dev/drbd1;" >> /etc/drbd.d/r0.res
  echo "    disk      /dev/xvdm;" >> /etc/drbd.d/r0.res
  echo "    address   ${leni_ip}:7789;" >> /etc/drbd.d/r0.res
  echo "    meta-disk internal;" >> /etc/drbd.d/r0.res
  echo "  }" >> /etc/drbd.d/r0.res
  echo "  on ${rhost}.${VHOSTNAME_2} {" >> /etc/drbd.d/r0.res
  echo "    device    /dev/drbd1;" >> /etc/drbd.d/r0.res
  echo "    disk      /dev/xvdm;" >> /etc/drbd.d/r0.res
  echo "    address   ${reni_ip}:7789;" >> /etc/drbd.d/r0.res
  echo "    meta-disk internal;" >> /etc/drbd.d/r0.res
  echo "  }" >> /etc/drbd.d/r0.res
  echo "}" >> /etc/drbd.d/r0.res
  service network restart

  # Create and mount to EBS volume
  set +e
  drbdadm up r0
  rtn_status=$?
  set -e
  if [ $rtn_status -ne 0 ]; then
    echo "Create new DRBD metadata."
    echo "yes" |drbdadm create-md r0
    drbdadm up r0
    if [ "${az}" == "${primary_site}" ]; then
      drbdadm primary --force r0
      mkfs -t ext4 /dev/drbd1
      mkdir -p /u01; mount /dev/drbd1 /u01
      echo > /u01/DO-NOT-DELETE.lck
    else
      drbdadm secondary r0
      mkdir -p /u01
    fi
  else
    echo "The DRBD metadata has been created before."
    if [ "${az}" == "${primary_site}" ]; then
      drbdadm primary --force r0
      mkdir -p /u01; mount /dev/drbd1 /u01
    else 
      drbdadm secondary r0
      mkdir -p /u01
    fi
  fi

else
  # Create mount to EBS volume
  sleep 60 # Wait since /dev/xvdn is not ready right away
  filesystem_status=`file -s /dev/xvdn`
  if [ "$filesystem_status" == "/dev/xvdn: data" ]; then
    mkfs -t ext4 /dev/xvdn
    sleep 60
    mkdir -p /u01; mount /dev/xvdn /u01
    echo > /u01/DO-NOT-DELETE.lck
  else
    mkdir -p /u01; mount /dev/xvdn /u01;
  fi
fi

# CFN init/signal
cfn-init --region ${AWS_REGION} --stack ${STACK_NAME} --resource ${LOGICAL_ID}
cfn_init_status=$?
if [ $cfn_init_status -eq 0 ];
then
  echo CFN_INIT_SUCCESS
  cfn-signal --region ${AWS_REGION} --stack ${STACK_NAME} --resource ${LOGICAL_ID} -e $cfn_init_status
else
  echo CFN_INIT_FAILED
  # Collect key data points
  TOKEN=$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
  curl -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/user-data >> /var/log/userdata.log
  TOKEN=$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
  instanceId=$(curl -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/instance-id)
  date_year=`date +%Y`
  date_month=`date +%m`
  date_full=`date +%m-%d-%Y_%H-%M-%S`
  s3_name=${ACCOUNT_ID}-ec2-logs
  # Upload log data to S3 for instance that fails to start properly
  aws --region ${AWS_REGION} s3 cp /var/log/userdata.log s3://${s3_name}/${APP_ENV}/${date_year}/${date_month}/${date_full}-${STACK_NAME}-${INSTANCE_ID}/userdata.log
  aws --region ${AWS_REGION} s3 cp /var/log/boot.log s3://${s3_name}/${APP_ENV}/${date_year}/${date_month}/${date_full}-${STACK_NAME}-${INSTANCE_ID}/boot.log
  aws --region ${AWS_REGION} s3 cp /var/log/chef/chef.log s3://${s3_name}/${APP_ENV}/${date_year}/${date_month}/${date_full}-${STACK_NAME}-${INSTANCE_ID}/chef.log
  aws --region ${AWS_REGION} s3 cp /var/log/cloud-init-output.log s3://${s3_name}/${APP_ENV}/${date_year}/${date_month}/${date_full}-${STACK_NAME}-${INSTANCE_ID}/cloud-init-output.log
  aws --region ${AWS_REGION} s3 cp /var/log/cfn-init.log s3://${s3_name}/${APP_ENV}/${date_year}/${date_month}/${date_full}-${STACK_NAME}-${INSTANCE_ID}/cfn-init.log
fi
echo Done
