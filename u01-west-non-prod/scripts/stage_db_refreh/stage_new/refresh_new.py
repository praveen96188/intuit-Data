import boto3
import subprocess
import psycopg2
import os
from psycopg2 import OperationalError
import time
import shlex

# Read main passwords from the file
def read_passwords_from_file(password_file_path):
    user_passwords = {}
    try:
        with open(password_file_path, 'r') as f:
            for line in f.readlines():
                line = line.strip()
                if line and ':' in line:
                    user, password = line.split(':', 1)
                    user_passwords[user.strip()] = password.strip()
    except Exception as e:
        print(f"Error reading password file: {e}")
    return user_passwords

# Read appuser passwords from separate file
def read_appuser_passwords(password_file_path):
    user_passwords = {}
    try:
        with open(password_file_path, 'r') as f:
            for line in f.readlines():
                line = line.strip()
                if line and ':' in line:
                    user, password = line.split(':', 1)
                    user_passwords[user.strip()] = password.strip()
    except Exception as e:
        print(f"Error reading appuser password file: {e}")
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
    db_cluster_info['ClusterParameterGroup'] = db_cluster_info.get('DBClusterParameterGroup', '')
    writer_instance_id = next(
        (m['DBInstanceIdentifier'] for m in db_cluster_info['DBClusterMembers'] if m['IsClusterWriter']), None)
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
            print(f"DBInstanceClass not found for writer instance {writer_instance_identifier}.")
            return None
        print(f"Using DBInstanceClass: {db_instance_class}")
        return db_instance_class
    except Exception as e:
        print(f"Error fetching instance details for {writer_instance_identifier}: {e}")
        return None

# Wait for DB instance to become available
def wait_for_db_instance_available(rds_client, db_instance_identifier):
    print(f"Waiting for DB instance {db_instance_identifier} to become available...")
    while True:
        instance_status = rds_client.describe_db_instances(DBInstanceIdentifier=db_instance_identifier)
        status = instance_status['DBInstances'][0]['DBInstanceStatus']
        if status == 'available':
            print(f"DB instance {db_instance_identifier} is available!")
            break
        print(f"Current status: {status}. Waiting...")
        time.sleep(30)

# Create staging DB cluster from snapshot
def create_staging_db_from_snapshot(rds_client, snapshot_identifier, stage_cluster_identifier, existing_stage_cluster_info):
    new_db_cluster_identifier = f"{stage_cluster_identifier.lower().replace('_', '-')}-new".strip('-')
    print(f"Creating new staging DB cluster from snapshot: {snapshot_identifier}")
    try:
        instance_class = get_instance_class_from_existing_cluster(rds_client, existing_stage_cluster_info)
        if not instance_class:
            raise Exception("Instance class not found.")
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
            DBClusterParameterGroupName=existing_stage_cluster_info['ClusterParameterGroup']
        )
        print(f"New DB cluster created: {new_db_cluster_identifier}")
        tags = existing_stage_cluster_info.get('TagList', [])
        filtered_tags = [tag for tag in tags if not tag['Key'].startswith('aws:')]
        writer_instance_identifier = f"{new_db_cluster_identifier}-1"
        print(f"Creating writer instance: {writer_instance_identifier}")
        rds_client.create_db_instance(
            DBInstanceIdentifier=writer_instance_identifier,
            DBClusterIdentifier=new_db_cluster_identifier,
            DBInstanceClass=instance_class,
            Engine=existing_stage_cluster_info['Engine'],
            PubliclyAccessible=False,
            AutoMinorVersionUpgrade=False,
            DBParameterGroupName=existing_stage_cluster_info['InstanceParameterGroup'],
            Tags=filtered_tags
        )
        wait_for_db_instance_available(rds_client, writer_instance_identifier)
        print(f"Waiting for cluster {new_db_cluster_identifier} to become available...")
        while True:
            response = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)
            if response['DBClusters'][0]['Status'] == 'available':
                print(f"Cluster {new_db_cluster_identifier} is available!")
                break
            time.sleep(300)
        return new_db_cluster_identifier
    except Exception as e:
        print(f"Error restoring cluster: {e}")
        raise

# Kill active connections
def kill_active_connections(db_host, db_name, db_user, db_password):
    try:
        print(f"Checking and terminating active connections on {db_name}@{db_host}...")
        conn = psycopg2.connect(
            host=db_host,
            database=db_name,
            user=db_user,
            password=db_password,
            connect_timeout=10
        )
        conn.autocommit = True
        cur = conn.cursor()
        terminate_query = """
            SELECT pg_terminate_backend(pid)
            FROM pg_stat_activity
            WHERE usename NOT IN ('rdsadmin', 'postgres')
              AND pid <> pg_backend_pid();
        """
        cur.execute(terminate_query)
        terminated = cur.fetchall()
        print(f"Terminated {len(terminated)} active session(s).")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error terminating sessions: {e}")

# Check and create users, reset passwords using \password
def check_and_create_users(db_host, db_port, db_name, postgres, password_file_path, required_users):
    user_passwords = read_passwords_from_file(password_file_path)
    password = user_passwords.get(postgres, None)
    if not password:
        print(f"Password for user '{postgres}' not found in the password file.")
        return
    try:
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            dbname=db_name,
            user=postgres,
            password=password,
            connect_timeout=120
        )
        cursor = connection.cursor()
        for user in required_users:
            cursor.execute(f"SELECT 1 FROM pg_roles WHERE rolname = '{user}';")
            if cursor.fetchone():
                print(f"User '{user}' already exists.")
                print(f"Resetting password for user '{user}' using \\password command.")
                reset_password_with_psql(user, user_passwords[user], db_host)
            else:
                print(f"Creating user '{user}' with login")
                cursor.execute(f"CREATE USER {user} WITH LOGIN;")
        connection.commit()
        cursor.close()
        connection.close()
    except OperationalError as e:
        print(f"Error connecting to the database: {e}")

# Reset password using psql \password via expect
def reset_password_with_psql(user, password, db_host):
    expect_script = f"""
log_user 1
set timeout 10
spawn env PGPASSWORD="{password}" psql -X -h {db_host} -d psppdarc -U intuadmin
expect {{
   -re "intuadmin=[#>]" {{
     send "\\\\password {user}\\r"
     expect "Enter new password:"
     send "{password}\\r"
     expect "Enter it again:"
     send "{password}\\r"
     expect -re "intuadmin=[#>]"
     send "\\\\q\\r"
     expect eof
   }}
   "could not connect"* {{
     puts "ERROR: Could not connect to the database."
     exit 1
   }}
   timeout {{
     puts "ERROR: Timeout waiting for psql prompt."
     exit 1
   }}
   eof {{
     puts "ERROR: Spawn failed or connection closed unexpectedly."
     exit 1
   }}
}}
"""
    subprocess.run(['expect', '-c', expect_script])

# Main function
def main():
    rds_client = boto3.client('rds', region_name='us-west-2')

    prod_cluster_identifier = 'ppsp-ppd-arc'
    stage_cluster_identifier = 'ppsp-ppd-arc'
    password_file_path = '/u01/scripts/stage_db_refreh/stage_new/db_password.txt'
    new_password_file_path = '/u01/scripts/stage_db_refreh/stage_new/intuadmin_new_password.txt'
    sql_file = '/u01/scripts/stage_db_refreh/stage_new/postgres_postgres.sql'

    print("\n=== STEP 1: Creating staging cluster from snapshot ===")
    latest_snapshot = fetch_latest_snapshot(rds_client, prod_cluster_identifier)
    existing_stage_cluster_info = fetch_db_cluster_info(rds_client, stage_cluster_identifier)
    new_db_cluster_identifier = create_staging_db_from_snapshot(
        rds_client,
        latest_snapshot['DBClusterSnapshotIdentifier'],
        stage_cluster_identifier,
        existing_stage_cluster_info
    )

    db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']
    db_name = 'psppdarc'
    db_user = 'intuadmin'
    passwords = read_passwords_from_file(new_password_file_path)
    db_password = passwords.get(db_user)

    if db_password:
        kill_active_connections(db_host, db_name, db_user, db_password)
    else:
        print("Postgres password not found in password file.")

    required_users = ['intuadmin', 'pspadm_owner']
    check_and_create_users(
        db_host=db_host,
        db_port=5432,
        db_name=db_name,
        postgres=db_user,
        password_file_path=new_password_file_path,
        required_users=required_users
    )

    user_passwords = read_passwords_from_file(new_password_file_path)
    for user, password in user_passwords.items():
        reset_password_with_psql(user, password, db_host)

# Entry point of the script
if __name__ == "__main__":
    main()
