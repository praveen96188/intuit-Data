import boto3
import subprocess
import psycopg2
import os
from psycopg2 import OperationalError
import time
import shlex
import argparse

# Function to fetch the latest snapshot
def fetch_latest_snapshot(rds_client, db_cluster_identifier):
    snapshots = rds_client.describe_db_cluster_snapshots(DBClusterIdentifier=db_cluster_identifier, MaxRecords=20)
    snapshots = snapshots['DBClusterSnapshots']
    if not snapshots:
        raise Exception(f"No snapshots found for cluster {db_cluster_identifier}")
    snapshots.sort(key=lambda x: x['SnapshotCreateTime'], reverse=True)
    latest_snapshot = snapshots[0]
    print(f"Latest snapshot: {latest_snapshot['DBClusterSnapshotIdentifier']}")
    return latest_snapshot

# Function to fetch DB cluster info
def fetch_db_cluster_info(rds_client, db_cluster_identifier):
    response = rds_client.describe_db_clusters(DBClusterIdentifier=db_cluster_identifier)
    db_cluster_info = response['DBClusters'][0]
    db_cluster_info['ClusterParameterGroup'] = db_cluster_info.get('DBClusterParameterGroup', '')
    writer_instance_id = next((m['DBInstanceIdentifier'] for m in db_cluster_info['DBClusterMembers'] if m['IsClusterWriter']), None)
    if writer_instance_id:
        instance_details = rds_client.describe_db_instances(DBInstanceIdentifier=writer_instance_id)
        db_instance = instance_details['DBInstances'][0]
        db_cluster_info['InstanceParameterGroup'] = db_instance['DBParameterGroups'][0]['DBParameterGroupName']
    else:
        db_cluster_info['InstanceParameterGroup'] = None
    print(f"Fetched cluster parameter group: {db_cluster_info['ClusterParameterGroup']}")
    print(f"Fetched instance parameter group: {db_cluster_info['InstanceParameterGroup']}")
    return db_cluster_info

# Get DB instance class
def get_instance_class_from_existing_cluster(rds_client, existing_stage_cluster_info):
    writer_instance = next((inst for inst in existing_stage_cluster_info['DBClusterMembers'] if inst.get('IsClusterWriter', False)), None)
    if writer_instance is None:
        print("No writer instance found in the existing cluster!")
        return None
    writer_instance_identifier = writer_instance['DBInstanceIdentifier']
    try:
        instance_details = rds_client.describe_db_instances(DBInstanceIdentifier=writer_instance_identifier)
        db_instance_class = instance_details['DBInstances'][0].get('DBInstanceClass', None)
        print(f"Using DBInstanceClass: {db_instance_class}")
        return db_instance_class
    except Exception as e:
        print(f"Error fetching instance details: {e}")
        return None

# Wait for DB instance to become available
def wait_for_db_instance_available(rds_client, db_instance_identifier):
    print(f"Waiting for DB instance {db_instance_identifier} to become available...")
    while True:
        status = rds_client.describe_db_instances(DBInstanceIdentifier=db_instance_identifier)['DBInstances'][0]['DBInstanceStatus']
        if status == 'available':
            print(f"DB instance {db_instance_identifier} is available!")
            break
        print(f"Current status: {status}. Waiting...")
        time.sleep(30)

# Create staging DB cluster from snapshot
def create_staging_db_from_snapshot(rds_client, snapshot_identifier, stage_cluster_identifier, existing_stage_cluster_info):
    new_db_cluster_identifier = f"{stage_cluster_identifier.lower().replace('_', '-')}-new1".strip('-')
    print(f"Creating new staging DB cluster from snapshot: {snapshot_identifier}")
    try:
        instance_class = get_instance_class_from_existing_cluster(rds_client, existing_stage_cluster_info)
        if not instance_class:
            raise Exception("Instance class not found.")
        rds_client.restore_db_cluster_from_snapshot(
            DBClusterIdentifier=new_db_cluster_identifier,
            SnapshotIdentifier=snapshot_identifier,
            Engine=existing_stage_cluster_info['Engine'],
            EngineVersion=existing_stage_cluster_info['EngineVersion'],
            Port=existing_stage_cluster_info['Port'],
            DBSubnetGroupName=existing_stage_cluster_info['DBSubnetGroup'],
 #           KmsKeyId='xxxxxxxx',
            VpcSecurityGroupIds=[sg['VpcSecurityGroupId'] for sg in existing_stage_cluster_info['VpcSecurityGroups']],
            CopyTagsToSnapshot=existing_stage_cluster_info['CopyTagsToSnapshot'],
            DeletionProtection=existing_stage_cluster_info['DeletionProtection'],
            EnableIAMDatabaseAuthentication=existing_stage_cluster_info['IAMDatabaseAuthenticationEnabled'],
            EngineMode=existing_stage_cluster_info['EngineMode'],
            DBClusterParameterGroupName=existing_stage_cluster_info['ClusterParameterGroup']
        )
        writer_instance_identifier = f"{new_db_cluster_identifier}-1"
        rds_client.create_db_instance(
            DBInstanceIdentifier=writer_instance_identifier,
            DBClusterIdentifier=new_db_cluster_identifier,
            DBInstanceClass=instance_class,
            Engine=existing_stage_cluster_info['Engine'],
            PubliclyAccessible=False,
            AutoMinorVersionUpgrade=False,
            DBParameterGroupName=existing_stage_cluster_info['InstanceParameterGroup'],
            Tags=[tag for tag in existing_stage_cluster_info.get('TagList', []) if not tag['Key'].startswith('aws:')]
        )
        wait_for_db_instance_available(rds_client, writer_instance_identifier)
        while True:
            status = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Status']
            if status == 'available':
                break
            time.sleep(300)
        return new_db_cluster_identifier
    except Exception as e:
        print(f"Error restoring cluster: {e}")
        raise

# Main function
def main(prod_cluster_identifier, stage_cluster_identifier, db_name, new_db_name):
    rds_client = boto3.client('rds', region_name='us-west-2')

    try:
        print("\n=== STEP 1: Creating staging cluster from snapshot ===")
        latest_snapshot = fetch_latest_snapshot(rds_client, prod_cluster_identifier)
        existing_stage_cluster_info = fetch_db_cluster_info(rds_client, stage_cluster_identifier)
        new_db_cluster_identifier = create_staging_db_from_snapshot(
            rds_client,
            latest_snapshot['DBClusterSnapshotIdentifier'],
            stage_cluster_identifier,
            existing_stage_cluster_info
        )
#        new_db_cluster_identifier = f"{stage_cluster_identifier.lower().replace('_', '-')}-new".strip('-')
        print(new_db_cluster_identifier)
        # Get endpoint after creation
        db_host = rds_client.describe_db_clusters(
            DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']
        rename_success = False
    
    except Exception as main_e:
        print(f"Main execution error: {main_e}")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Refresh RDS staging cluster from snapshot.')
    parser.add_argument('--prod-cluster', required=True, help='Production cluster identifier')
    parser.add_argument('--stage-cluster', required=True, help='Staging cluster identifier')
    parser.add_argument('--db-name', required=True, help='Old DB name')
    parser.add_argument('--new-db-name', required=True, help='New DB name')
    args = parser.parse_args()

    main(args.prod_cluster, args.stage_cluster, args.db_name, args.new_db_name)

