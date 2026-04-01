import time
import boto3, json
import logging
from botocore.exceptions import ClientError
import os, sys, pprint
from datetime import datetime, timezone

# os.environ['AWS_PROFILE'] = "sbg-psp-ppd"
# os.environ['AWS_DEFAULT_REGION'] = "us-west-2"

logger = logging.getLogger()
logger.setLevel(logging.INFO)

if len(sys.argv) != 2:
    print('Usage: python3 create_apg_switchover_role.py <profile>')
    print("      where <profile> is AWS account profile")
    print('')
    sys.exit('[Error] Missing or invalid parameters')
else:
    os.environ['AWS_PROFILE'] = sys.argv[1]
    print("profile: ", os.environ['AWS_PROFILE'])

iamclient = boto3.client('iam')
stsclient = boto3.client('sts')

try:
    response_sts = stsclient.get_caller_identity()
    print(response_sts)
    account = response_sts['Account']
    print("AWS Account", account)
    print(' ')

except Exception as e:
    print("Error getting STS caller identity: ", str(e))

# Define the trust policy
assume_role_policy_document = {
    "Version": "2012-10-17",
    "Statement": [
        {
          "Effect": "Allow",
          "Principal": {
            "Service": "lambda.amazonaws.com"
          },
          "Action": "sts:AssumeRole"
        }
    ]
}

try:
    # Create a role with the trust policy
    response = iamclient.create_role(
        RoleName='apg-switchover-role',
        AssumeRolePolicyDocument=json.dumps(assume_role_policy_document)
    )
    print(response)
    print(' ')

except Exception as e:
    print('Error creating IAM role:', str(e))

# Load the policy from JSON file
with open('apg-switchover-policy.json', 'r') as json_file:
    policy_document = json.load(json_file)
# Create the policy
try:
    response = iamclient.create_policy(
        PolicyName='apg-switchover',
        PolicyDocument=json.dumps(policy_document),
        Description='policy to grant permissions for aurora postgres global db switchover lambda'
    )
    print(response)
    print(' ')
except Exception as e:
    print("Error creating policy: ", str(e))

# Attach policy created above to the IAM role
try:
    response = iamclient.attach_role_policy(
        RoleName='apg-switchover-role',
        PolicyArn='arn:aws:iam::' + account + ':policy/apg-switchover'
    )
    print(response)
    print(' ')
except Exception as e:
    print("Error attaching policy to IAM role: ", str(e))
