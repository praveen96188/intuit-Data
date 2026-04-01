import boto3
import subprocess
import psycopg2
from psycopg2 import OperationalError
from datetime import datetime, timezone
import time

# Read passwords from the file
def read_passwords_from_file(password_file_path):
    user_passwords = {}
    try:
        with open(password_file_path, 'r') as f:
            for line in f.readlines():
                line = line.strip()  # Remove leading/trailing whitespaces
                if line:  # Ignore empty lines
                    user, password = line.split(':')  # Expect format "username:password"
                    user_passwords[user] = password
    except Exception as e:
        print(f"Error reading password file: {e}")
    return user_passwords


# Function to fetch the latest snapshot
def fetch_latest_snapshot(rds_client, db_cluster_identifier):
    snapshots = rds_client.describe_db_cluster_snapshots(
        DBClusterIdentifier=db_cluster_identifier,
        MaxRecords=20
    )
    
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
    print(f"Fetched {db_cluster_identifier} DB cluster info:")
    print(f"  - Status: {db_cluster_info['Status']}")
    print(f"  - Engine: {db_cluster_info['Engine']}")
    print(f"  - Endpoint: {db_cluster_info['Endpoint']}")
    return db_cluster_info


# Function to get instance class from the existing cluster
def get_instance_class_from_existing_cluster(rds_client, existing_stage_cluster_info):
    writer_instance = None
    for instance in existing_stage_cluster_info['DBClusterMembers']:
        if instance.get('IsClusterWriter', False):
            writer_instance = instance
            break
    
    if writer_instance is None:
        print("No writer instance found in the existing cluster!")
        return None
    
    writer_instance_identifier = writer_instance['DBInstanceIdentifier']
    
    try:
        instance_details = rds_client.describe_db_instances(DBInstanceIdentifier=writer_instance_identifier)
        db_instance_class = instance_details['DBInstances'][0].get('DBInstanceClass', None)
        
        if not db_instance_class:
            print(f"DBInstanceClass not found for the writer instance {writer_instance_identifier}.")
            return None
        
        print(f"Using DBInstanceClass: {db_instance_class} for the new writer instance")
        return db_instance_class
    
    except Exception as e:
        print(f"Error fetching instance details for {writer_instance_identifier}: {e}")
        return None


# Function to wait for DB instance to become available
def wait_for_db_instance_available(rds_client, db_instance_identifier):
    print(f"Waiting for DB instance {db_instance_identifier} to become available...")
    while True:
        instance_status = rds_client.describe_db_instances(DBInstanceIdentifier=db_instance_identifier)
        status = instance_status['DBInstances'][0]['DBInstanceStatus']
        if status == 'available':
            print(f"DB instance {db_instance_identifier} is available!")
            break
        print(f"Current status of {db_instance_identifier}: {status}. Waiting...")
        time.sleep(30)


# Function to create the new DB cluster from snapshot
def create_staging_db_from_snapshot(rds_client, snapshot_identifier, stage_cluster_identifier, existing_stage_cluster_info):
    new_db_cluster_identifier = f"{stage_cluster_identifier.lower().replace('_', '-')}-new"
    new_db_cluster_identifier = new_db_cluster_identifier.strip('-')
    
    print(f"Creating new staging DB cluster from snapshot: {snapshot_identifier}")
    
    try:
        instance_class = get_instance_class_from_existing_cluster(rds_client, existing_stage_cluster_info)
        if not instance_class:
            raise Exception("Instance class not found for the existing staging DB cluster.")
        
        # Create the new DB cluster from snapshot
        response = rds_client.restore_db_cluster_from_snapshot(
            DBClusterIdentifier=new_db_cluster_identifier,
            SnapshotIdentifier=snapshot_identifier,
            Engine=existing_stage_cluster_info['Engine'],
            EngineVersion=existing_stage_cluster_info['EngineVersion'],
            Port=existing_stage_cluster_info['Port'],
            DBSubnetGroupName=existing_stage_cluster_info['DBSubnetGroup'],
            KmsKeyId='arn:aws:kms:us-west-2:152430470825:key/8d5ce945-95b1-40d7-b070-219793d62934',
            VpcSecurityGroupIds=[sg['VpcSecurityGroupId'] for sg in existing_stage_cluster_info['VpcSecurityGroups']],
            CopyTagsToSnapshot=existing_stage_cluster_info['CopyTagsToSnapshot'],
            DeletionProtection=existing_stage_cluster_info['DeletionProtection'],
            EnableIAMDatabaseAuthentication=existing_stage_cluster_info['IAMDatabaseAuthenticationEnabled'],
            EngineMode=existing_stage_cluster_info['EngineMode'],
        )

        print(f"Successfully created new staging DB cluster: {new_db_cluster_identifier}")
        
        # Filter out any tags that start with "aws:"
        tags = existing_stage_cluster_info.get('TagList', [])
        filtered_tags = [tag for tag in tags if not tag['Key'].startswith('aws:')]
        
        # Create the writer instance
        writer_instance_identifier = f"{new_db_cluster_identifier}-1"
        print(f"Creating writer instance: {writer_instance_identifier}")

        writer_instance_response = rds_client.create_db_instance(
            DBInstanceIdentifier=writer_instance_identifier,
            DBClusterIdentifier=new_db_cluster_identifier,
            DBInstanceClass=instance_class,
            Engine=existing_stage_cluster_info['Engine'],
            PubliclyAccessible=False,
            AutoMinorVersionUpgrade=False,
            Tags=filtered_tags  # Use filtered tags here
        )

        # Wait for the new writer instance to become available
        wait_for_db_instance_available(rds_client, writer_instance_identifier)

        # Wait for the DB cluster to become available (still useful for completeness)
        print(f"Waiting for new DB cluster {new_db_cluster_identifier} to become available...")
        while True:
            response = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)
            if response['DBClusters'][0]['Status'] == 'available':
                print(f"New DB cluster {new_db_cluster_identifier} is available!")
                break
            time.sleep(300)

        return new_db_cluster_identifier

    except Exception as e:
        print(f"Error restoring staging DB cluster: {e}")
        raise


# Function to reset passwords for all users
def reset_all_user_passwords(password_file_path, rds_client, new_db_cluster_identifier):
    # Read the passwords from the file
    user_passwords = read_passwords_from_file(password_file_path)
    
    # Get the DB host endpoint
    db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']
    
    # Loop through each user and reset their password
    for user, password in user_passwords.items():
        reset_password_with_psql(user, password, db_host)


# Function to reset a user's password using expect
def reset_password_with_psql(user, password, db_host):
    # Create the expect script as a string
    expect_script = f"""
    spawn psql -h {db_host} -d psppdarcl -U intuadmin
    expect "#"
    send "\\\\password {user}\\r"
    expect "Enter new password:"
    send "{password}\\r"
    expect "Enter it again:"
    send "{password}\\r"
    expect "#"
    send "\\q\\r"
    expect eof
    """

    # Run the expect script
    result = subprocess.run(['expect'], input=expect_script, text=True, capture_output=True)

    if result.returncode == 0:
        print(f"Password changed successfully for user: {user}")
    else:
        print(f"Failed to change password for user: {user}")
        print(result.stderr)


# Main function to orchestrate the process
def main():
    rds_client = boto3.client('rds', region_name='us-west-2')  # Initialize the RDS client

    prod_cluster_identifier = 'ppsp-ppd-arc'  # Production cluster identifier
    stage_cluster_identifier = 'ppsp-ppd-arc'  # Staging cluster identifier
    password_file_path = '/u01/scripts/stage_db_refreh/db_password.txt'  # Path to password file
    sql_file_path = '/u01/scripts/stage_db_refreh/sql_file.sql'  # Path to the SQL file

    try:
        # Fetch the latest snapshot from the production DB cluster
        latest_snapshot = fetch_latest_snapshot(rds_client, prod_cluster_identifier)
        existing_stage_cluster_info = fetch_db_cluster_info(rds_client, stage_cluster_identifier)

        # Create a new staging DB from the latest snapshot
        new_db_cluster_identifier = create_staging_db_from_snapshot(
            rds_client,
            latest_snapshot['DBClusterSnapshotIdentifier'],
            stage_cluster_identifier,
            existing_stage_cluster_info
        )

        # Reset passwords for all users in the password file
        reset_all_user_passwords(password_file_path, rds_client, new_db_cluster_identifier)

        # Check for active connections to the new staging DB
        check_active_connections(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            db_name='psppdarc',  # The database we want to rename
            postgres='intuadmin',  # The username to connect
            password_file_path=password_file_path
        )

        # Rename the production database to 'stage'
        rename_db(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            old_db_name='psppdarc',
            new_db_name='psppdarc1',
            postgres='intuadmin',
            password_file_path=password_file_path
        )

        # Check and create users if needed, reset passwords for existing users
        required_users = ['testusr', 'psparcapp']
        check_and_create_users(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            db_name='psppdarc1',  # The newly renamed database
            postgres='intuadmin',  # The username to connect
            password_file_path=password_file_path,
            required_users=required_users
        )

        # Execute SQL queries from file
        execute_sql_from_file(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            db_name='psppdarc1',  # The newly renamed database
            postgres='intuadmin',  # The username to connect
            password_file_path=password_file_path,
            sql_file_path=sql_file_path
        )

    except Exception as e:
        print(f"Error occurred during DB refresh process: {e}")
        raise

if __name__ == '__main__':
    main()
