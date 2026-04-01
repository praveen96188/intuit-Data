import time
import boto3, json
import logging
from botocore.exceptions import ClientError
import os, sys, pprint
import psycopg2

# os.environ['AWS_PROFILE'] = "sbg-psp-ppd"
# os.environ['AWS_DEFAULT_REGION'] = "us-west-2"

#os.environ['AWS_PROFILE'] = "sbg-psp-prod"
#os.environ['AWS_DEFAULT_REGION'] = "us-west-2"

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def str2bool(value):
    return value.lower() in ("True", True, "False", False)


if len(sys.argv) != 4:
    print('Usage: python3 launch_refresh.py <target_instance_name> <application_name> <state_machine_arn> <region>')
    print('  Example:')
    print(
        '  python3 apg-refresh.py dbinstance1 psp us-west-2')
    print('')
    sys.exit('[Error] Missing or invalid parameters')
else:
    target_instance_name = sys.argv[1]
    application_name = sys.argv[2]
    region = sys.argv[3]

    dbjsondir = "./"
    dbjsonfile = dbjsondir + "db-" + application_name + "-" + target_instance_name + ".json"
    print(dbjsonfile)
    try:
        with open(dbjsonfile) as json_file:
            sm_input = json.load(json_file)
            print('[OK] Restore initiated')
    #            pprint.pprint(sm_input['restore']['check']['torun'])
    except ClientError as e:
        logging.error(e)
        sys.exit('[Error] start_execution API failed')

    awsregion = region
    dbservice = sm_input['restore']['dbservice']
    restoretype = sm_input['restore']['restoretype']
    application = sm_input['restore']['application']
    environment = sm_input['restore']['environment']
    port = sm_input['restore']['port']
    dbname = sm_input['restore']['database']
    pwd = sm_input['restore']['pwd']
    subgrp = sm_input['restore']['subgrp']
    iamdbauth = str2bool(sm_input['restore']['iamdbauth'])
    cwalogs = sm_input['restore']['cwalogs']
    copytagstosnap = str2bool(sm_input['restore']['copytagstosnap'])
    deletionprotection = str2bool(sm_input['restore']['deletionprotection'])
    secgrpids = sm_input['restore']['secgrpids']

    cwalogslist = cwalogs.split(",")

    rdsclient = boto3.client('rds', region_name=awsregion)
    dmsclient = boto3.client('dms', region_name=awsregion)


def CheckRestore(targetcluster):
    while (True):
        response = rdsclient.describe_db_clusters(
            DBClusterIdentifier=targetcluster
        )
        logger.info("Status of the cluster verified")
        clusterstatus = response['DBClusters'][0]['Status']
        if clusterstatus == 'available':
            result = 'Aurora PostgreSQL cluster created successfully !'
            pprint.pprint(result)
            pprint.pprint('')
            pprint.pprint(response)
            break
        elif clusterstatus == 'creating':
            result = 'cluster creation is in progress'
            pprint.pprint(result)
            time.sleep(180)
        else:
            result = 'Failed to create Aurora PostgreSQL Cluster'
            break


def CheckInstance(DBInstanceIdentifier):
    while (True):
        response = rdsclient.describe_db_instances(
            DBInstanceIdentifier=DBInstanceIdentifier
        )
        logger.info("Status of the instance verified")
        dbstatus = response['DBInstances'][0]['DBInstanceStatus']
        logger.info(dbstatus)
        if dbstatus == 'available':
            result = 'Instance created successfully !'
            pprint.pprint(result)
            pprint.pprint('')
            pprint.pprint(response)
            break
        elif dbstatus == 'creating':
            result = 'instance creation is in progress'
            pprint.pprint(result)
            time.sleep(120)


def CheckSnapshot(snapshot, dbservice):
    if dbservice == 'aurora':
        while (True):
            response = rdsclient.describe_db_cluster_snapshots(
                DBClusterSnapshotIdentifier=snapshot
            )
            logger.info("Status of the snapshot verified")
            snapshotstatus = response['DBClusterSnapshots'][0]['Status']
            if snapshotstatus == 'available':
                result = 'Snapshot created successfully !'
                pprint.pprint(result)
                break
            elif snapshotstatus == 'creating':
                result = 'snapshot creation is in progress'
                pprint.pprint(result)
                time.sleep(120)
            else:
                result = 'Failed to create snapshot'
                break
    else:
        while (True):
            response = rdsclient.describe_db_snapshots(
                DBSnapshotIdentifier=snapshot
            )
            logger.info("Status of the snapshot verified")
            snapshotstatus = response['DBSnapshots'][0]['Status']
            if snapshotstatus == 'available':
                result = 'Snapshot created successfully !'
                pprint.pprint(result)
                break
            elif snapshotstatus == 'creating':
                result = 'snapshot creation is in progress'
                pprint.pprint(result)
                time.sleep(120)
            else:
                result = 'Failed to create snapshot'
                break


if dbservice == 'aurora':
    sourcecluster = sm_input['restore']['sourcecluster']
    targetcluster = sm_input['restore']['targetcluster']
    cluparamgrp = sm_input['restore']['cluparamgrp']
    engine = sm_input['restore']['engine']

    temptargetcluster = targetcluster

    if restoretype == 'latestpoint' or restoretype == 'fastcloning':

        if restoretype == 'latestpoint':
            type = 'full-copy'
        else:
            type = 'copy-on-write'

        cludesc = rdsclient.describe_db_clusters(
            DBClusterIdentifier=sourcecluster
        )

        latestrestorablepoint = str(cludesc['DBClusters'][0]['LatestRestorableTime'])
        pprint.pprint(latestrestorablepoint)
        if engine == "aurora-postgresql":
            logger.info(type)
            response = rdsclient.restore_db_cluster_to_point_in_time(
                DBClusterIdentifier=temptargetcluster,
                RestoreType=type,
                SourceDBClusterIdentifier=sourcecluster,
                UseLatestRestorableTime=True,
                Port=port,
                DBSubnetGroupName=subgrp,
                VpcSecurityGroupIds=[
                    secgrpids,
                ],
                Tags=[
                    {
                        'Key': 'refresh-application',
                        'Value': application
                    },
                    {
                        'Key': 'refresh-environment',
                        'Value': environment
                    },
                    {
                        'Key': 'refresh-cluster',
                        'Value': 'to_modify_after_rename'
                    },
                    {
                        'Key': 'refresh',
                        'Value': 'true'
                    }
                ],
                EnableIAMDatabaseAuthentication=iamdbauth,
                EnableCloudwatchLogsExports=cwalogslist,
                DBClusterParameterGroupName=cluparamgrp,
                DeletionProtection=deletionprotection,
                CopyTagsToSnapshot=copytagstosnap
            )

        result = "Cluster restore to the latest restorable time initiated"
        pprint.pprint(result)
        CheckRestore(temptargetcluster)
        response = rdsclient.create_db_instance(
            DBInstanceIdentifier=sm_input['restore']['dbinstance'],
            DBInstanceClass=sm_input['restore']['dbclass'],
            Engine=sm_input['restore']['engine'],
            DBSubnetGroupName=sm_input['restore']['subgrp'],
            DBParameterGroupName=sm_input['restore']['instparamgrp'],
            AutoMinorVersionUpgrade=str2bool(sm_input['restore']['autominor']),
            PubliclyAccessible=False,
            Tags=[
                {
                    'Key': 'refresh-application',
                    'Value': application
                },
                {
                    'Key': 'refresh-environment',
                    'Value': environment
                },
                {
                    'Key': 'refresh-instance',
                    'Value': 'to_modify_after_rename'
                },
                {
                    'Key': 'refresh',
                    'Value': 'true'
                }
            ],
            DBClusterIdentifier=temptargetcluster,
            CopyTagsToSnapshot=copytagstosnap
        )

        result = "Instance creation initiated"
        CheckInstance(sm_input['restore']['dbinstance'])
    elif restoretype == 'restorepoint':
        restoretime = sm_input['restore']['restoretime']
        latestrestorablepoint = "null"
        if engine == "aurora-postgresql":
            response = rdsclient.restore_db_cluster_to_point_in_time(
                DBClusterIdentifier=temptargetcluster,
                RestoreType='full-copy',
                RestoreToTime=restoretime,
                SourceDBClusterIdentifier=sourcecluster,
                Port=port,
                DBSubnetGroupName=subgrp,
                VpcSecurityGroupIds=[
                    secgrpids,
                ],
                Tags=[
                    {
                        'Key': 'refresh-application',
                        'Value': application
                    },
                    {
                        'Key': 'refresh-environment',
                        'Value': environment
                    },
                    {
                        'Key': 'refresh-cluster',
                        'Value': 'to_modify_after_rename'
                    },
                    {
                        'Key': 'refresh',
                        'Value': 'true'
                    }
                ],
                EnableIAMDatabaseAuthentication=iamdbauth,
                EnableCloudwatchLogsExports=cwalogslist,
                DBClusterParameterGroupName=cluparamgrp,
                DeletionProtection=deletionprotection,
                CopyTagsToSnapshot=copytagstosnap
            )

        result = "Cluster restore to the point specified initiated"
        pprint.pprint(result)
        CheckRestore(rdsclient, result)
    elif restoretype == 'fromsnapshot' or restoretype == 'incrementalrefresh':
        if restoretype == 'incrementalrefresh':
            snapshot = sm_input['restore']['snapshot']
            pprint.pprint(snapshot)
            engineversion = sm_input['restore']['engineversion']
            latestrestorablepoint = "null"
            database = sm_input['restore']['database']
            srcconnattr = sm_input['restore']['srcconnattr']
            repinstarn = sm_input['restore']['repinstarn']
            '''
            #ToDo
            '''
        if engine == "aurora-postgresql":
            response = rdsclient.restore_db_cluster_from_snapshot(
                DBClusterIdentifier=temptargetcluster,
                SnapshotIdentifier=snapshot,
                Engine=engine,
                EngineVersion=engineversion,
                Port=port,
                DBSubnetGroupName=subgrp,
                VpcSecurityGroupIds=[
                    secgrpids,
                ],
                Tags=[
                    {
                        'Key': 'refresh-application',
                        'Value': application
                    },
                    {
                        'Key': 'refresh-environment',
                        'Value': environment
                    },
                    {
                        'Key': 'refresh-cluster',
                        'Value': 'to_modify_after_rename'
                    },
                    {
                        'Key': 'refresh',
                        'Value': 'true'
                    }
                ],
                EnableIAMDatabaseAuthentication=iamdbauth,
                EnableCloudwatchLogsExports=cwalogslist,
                DBClusterParameterGroupName=cluparamgrp,
                DeletionProtection=deletionprotection,
                CopyTagsToSnapshot=copytagstosnap
            )

        result = "Cluster restore from snapshot initiated"
        pprint.pprint(result)
        CheckRestore(temptargetcluster)
        response = rdsclient.create_db_instance(
            DBInstanceIdentifier=sm_input['restore']['dbinstance'],
            DBInstanceClass=sm_input['restore']['dbclass'],
            Engine=sm_input['restore']['engine'],
            DBSubnetGroupName=sm_input['restore']['subgrp'],
            DBParameterGroupName=sm_input['restore']['instparamgrp'],
            AutoMinorVersionUpgrade=str2bool(sm_input['restore']['autominor']),
            PubliclyAccessible=False,
            Tags=[
                {
                    'Key': 'refresh-application',
                    'Value': application
                },
                {
                    'Key': 'refresh-environment',
                    'Value': environment
                },
                {
                    'Key': 'refresh-instance',
                    'Value': 'to_modify_after_rename'
                },
                {
                    'Key': 'refresh',
                    'Value': 'true'
                }
            ],
            DBClusterIdentifier=temptargetcluster,
            CopyTagsToSnapshot=copytagstosnap
        )

        result = "Instance creation initiated"
        pprint.pprint(response)
        CheckInstance(sm_input['restore']['dbinstance'])
    else:
        # result = "Unknown restore type"
        raise ValueError("Restore type unknown or not supported by this function")
else:
raise ValueError("Database service specified unknown or not supported by this function")