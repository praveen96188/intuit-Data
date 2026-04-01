import time
import boto3, json
import logging
from botocore.exceptions import ClientError
import os, sys, pprint
from datetime import datetime, timezone
import zipfile

# os.environ['AWS_PROFILE'] = "sbg-psp-ppd"
# os.environ['AWS_DEFAULT_REGION'] = "us-west-2"

logger = logging.getLogger()
logger.setLevel(logging.INFO)

if len(sys.argv) != 5:
    print('Usage: python3 create_apg_switchover_lambda_function.py <profile> <region> <vpc_name> <function_name>')
    print("      where <profile> is AWS account profile")
    print("      <region> is AWS region")
    print("      <vpc name> is AWS VPC name")
    print("      <function name> is name of Lambda function you want to create")
    print('')
    sys.exit('[Error] Missing or invalid parameters')

profile = sys.argv[1]
os.environ['AWS_PROFILE'] = sys.argv[1]
region = sys.argv[2]
vpc_name = sys.argv[3]
function_name = sys.argv[4]

if region == 'us-west-2':
    ec2client = boto3.client('ec2',region_name='us-west-2')
    lambdaclient = boto3.client('lambda', region_name='us-west-2')
elif region == 'us-east-2':
    ec2client = boto3.client('ec2',region_name='us-east-2')
    lambdaclient = boto3.client('lambda', region_name='us-east-2')
else:
    print("Wrong region provide. Exiting...")
    exit()

stsclient = boto3.client('sts')

# get aws account id
try:
    response_sts = stsclient.get_caller_identity()
    print(response_sts)
    account = response_sts['Account']
    print("AWS Account", account)
    print(' ')

except Exception as e:
    print("Error getting STS caller identity: ", str(e))
    exit()

# get vpc id
try:
    response = ec2client.describe_vpcs(
        Filters=[{'Name': 'tag:Name', 'Values': [vpc_name]}]
    )

    for vpc in response['Vpcs']:
        print('VPC ID:', vpc['VpcId'])
        vpc_id = vpc['VpcId']

except Exception as e:
    print('Error retrieving VPCs:', str(e))
    exit()

# get private subnet ids
try:
    pvt_subnet_ids = []
    subnet_names = ['PrivateSubnetAz1', 'PrivateSubnetAZ1', 'PrivateSubnetAz2', 'PrivateSubnetAZ2', 'PrivateSubnetAz3',
                    'PrivateSubnetAZ3']
    response = ec2client.describe_subnets(
        Filters=[{'Name': 'vpc-id', 'Values': [vpc_id]},
                 {'Name': 'tag:Name', 'Values': subnet_names}]
    )
    for subnet in response['Subnets']:
        if not subnet['MapPublicIpOnLaunch']:
            # print('Private Subnet ID:', subnet['SubnetId'])
            pvt_subnet_ids.append(subnet['SubnetId'])
            print(pvt_subnet_ids)
            print(' ')


except Exception as e:
    print('Error retrieving Subnets:', str(e))
    exit()

# Create security group apg_switchover_lambda_${vpcid}
try:
    response = ec2client.describe_security_groups(
        Filters=[{'Name': 'group-name', 'Values': ['apg_switchover_lambda_' + vpc_id]}]
    )

    # Extract the GroupId from the first security group in the response (if any)
    print('printing response from describe_security_groups')
    print(response)
    if response['SecurityGroups']:
        print('Security Group ID:', response['SecurityGroups'][0]['GroupId'])
        security_group_id = response['SecurityGroups'][0]['GroupId']
    else:
        print('No security groups with the name apg_switchover_lambda_' + vpc_id + ' found. Creating it...')
        response = ec2client.create_security_group(
            GroupName='apg_switchover_lambda_' + vpc_id,
            Description='apg_switchover_lambda_' + vpc_id,
            VpcId=vpc_id
        )
        security_group_id = response['GroupId']
        print('Security Group Created %s in vpc %s.' % (security_group_id, vpc_id))

except Exception as e:
    print(f"Error creating Security Group: {str(e)}")
    # exit()

# Create tag for newly created SG
try:
    response = ec2client.create_tags(
        Resources=[security_group_id],
        Tags=[
            {
                'Key': 'Name',
                'Value': 'apg_switchover_lambda_' + vpc_id
            }
        ]
    )
    print(f'Tags added to security group {security_group_id}.')

except Exception as e:
    print(f'Error adding tags to Security Group: {str(e)}')
    # exit()

# Create zip file to use to create lambda function
def zipdir(path, ziph):
    # Zip an entire directory
    for root, dirs, files in os.walk(path):
        for file in files:
            ziph.write(os.path.join(root, file),
                       os.path.relpath(os.path.join(root, file),
                                       os.path.join(path, '..')))

try:
    with zipfile.ZipFile('apg-global-db-switchover.zip', 'w', zipfile.ZIP_DEFLATED) as zipf:
        # Add individual files to the zipfile
        zipf.write('apg-global-db-switchover.py')
        zipf.write('postgres_utils.py')
        # Add folders (with all their content) to the zipfile
        zipdir('pg8000/', zipf)
        zipdir('scramp/', zipf)
        zipdir('asn1crypto/', zipf)

    print('Zip file created successfully')

except Exception as e:
    print(f'Error creating zip file: {str(e)}')
    # exit()

# Create Lambda function
try:
    response = lambdaclient.create_function(
        FunctionName=function_name,
        Runtime='python3.9',
        Role='arn:aws:iam::' + account + ':role/apg-switchover-role',
        Handler=function_name + '.lambda_handler',
        Code={
            'ZipFile': open('./apg-global-db-switchover.zip', 'rb').read(),
        },
        Description=function_name,
        Timeout=900,
        Publish=True,
        VpcConfig={
            'SubnetIds': [
                pvt_subnet_ids[0],pvt_subnet_ids[1],pvt_subnet_ids[2]
            ],
            'SecurityGroupIds': [
                security_group_id,
            ]
        }
    )
    print(response)

except Exception as e:
    print("Error creating Lambda function: ", str(e))
    exit()