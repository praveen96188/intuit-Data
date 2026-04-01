import boto3
import subprocess
import psycopg2
import os
from psycopg2 import OperationalError
import time
import shlex
import pexpect

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
    new_db_cluster_identifier = f"{stage_cluster_identifier.lower().replace('_', '-')}-new".strip('-')
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
            KmsKeyId='arn:aws:kms:us-west-2:152430470825:key/8d5ce945-95b1-40d7-b070-219793d62934',
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

# Kill active connections
def kill_active_connections(db_host, db_name, db_user, db_password):
    try:
        conn = psycopg2.connect(host=db_host, database=db_name, user=db_user, password=db_password, connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute("""
            SELECT pg_terminate_backend(pid) FROM pg_stat_activity
            WHERE usename NOT IN ('rdsadmin', 'postgres') AND pid <> pg_backend_pid();
        """)
        terminated = cur.fetchall()
        print(f"Terminated {len(terminated)} active session(s).")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error terminating sessions: {e}")

# Reset password using psql with \password
def run_psql_password(db_host, db_name, db_user, target_user, db_password, new_password):
    try:
        cmd = f'psql -h {db_host} -U {db_user} -d {db_name}'
        child = pexpect.spawn(cmd, encoding='utf-8', timeout=10)
        child.expect_exact('Password for user')
        child.sendline(db_password)
        child.expect_exact(f'{db_name}=#')
        child.sendline(f'\\password {target_user}')
        child.expect_exact(f'Enter new password for user "{target_user}":')
        child.sendline(new_password)
        child.expect_exact('Enter it again:')
        child.sendline(new_password)
        child.expect_exact(f'{db_name}=#')
        print(f"Password reset for {target_user} successful.")
        child.sendline('\\q')
        child.close()
    except Exception as e:
        print(f"Error resetting {target_user} password with pexpect: {e}")

# Drop replication slots
def drop_replication_slots(db_host, db_user, db_password):
    try:
        conn = psycopg2.connect(host=db_host, user=db_user, password=db_password, dbname='postgres', connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute("SELECT slot_name FROM pg_replication_slots;")
        for slot in cur.fetchall():
            cur.execute(f"SELECT pg_drop_replication_slot('{slot[0]}');")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error dropping replication slots: {e}")

# Rename the database
def rename_database(db_host, db_user, db_password, old_name, new_name):
    try:
        conn = psycopg2.connect(host=db_host, user=db_user, password=db_password, dbname='postgres', connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute(f"ALTER DATABASE {old_name} RENAME TO {new_name};")
        cur.close()
        conn.close()
        print(f"Database renamed to {new_name}")
    except Exception as e:
        print(f"Error renaming database: {e}")

# Execute SQL file
def execute_sql_file(db_host, db_name, db_user, db_password, sql_file_path):
    subprocess.run(shlex.split(f'psql -h {db_host} -U {db_user} -d {db_name} -f {sql_file_path}'), env={**os.environ, 'PGPASSWORD': db_password}, check=True)

# Manage pspadm_owner
def manage_pspadm_owner(db_host, db_name, db_user, db_password, new_password):
    conn = psycopg2.connect(host=db_host, dbname=db_name, user=db_user, password=db_password)
    conn.autocommit = True
    with conn.cursor() as cur:
        cur.execute("SELECT 1 FROM pg_roles WHERE rolname = 'pspadm_owner';")
        if cur.fetchone():
            print("User exists. Resetting password using \\password...")
            run_psql_password(db_host, db_name, db_user, 'pspadm_owner', db_password, new_password)
        else:
            cur.execute("CREATE USER pspadm_owner WITH LOGIN;")
            run_psql_password(db_host, db_name, db_user, 'pspadm_owner', db_password, new_password)
    conn.close()

# Main function
def main():
    rds_client = boto3.client('rds', region_name='us-west-2')

    prod_cluster_identifier = 'ppsp-ppd-arc'
    stage_cluster_identifier = 'ppsp-ppd-arc'
    password_file_path = '/u01/scripts/stage_db_refreh/stage_new/db_password.txt'
    new_password_file_path = '/u01/scripts/stage_db_refreh/stage_new/intuadmin_new_password.txt'
    sql_file = '/u01/scripts/stage_db_refreh/stage_new/postgres_postgres.sql'

    db_name = 'psppdarc'
    new_db_name = 'psppdarc1'
    db_user = 'intuadmin'

    passwords = read_passwords_from_file(password_file_path)
    new_passwords = read_passwords_from_file(new_password_file_path)

    current_password = passwords.get(db_user)
    intuadmin_password = new_passwords.get('intuadmin')
    pspadm_password = new_passwords.get('pspadm_owner')

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

        db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']
        if current_password:
            kill_active_connections(db_host, db_name, db_user, current_password)
            if intuadmin_password:
                run_psql_password(db_host, db_name, db_user, 'intuadmin', current_password, intuadmin_password)
                drop_replication_slots(db_host, db_user, current_password)
                rename_database(db_host, db_user, current_password, old_name=db_name, new_name=new_db_name)
            else:
                print("intuadmin password not found in new password file.")
        else:
            print("intuadmin current password not found in password file.")

        execute_sql_file(db_host, new_db_name, db_user, current_password, sql_file)
        manage_pspadm_owner(db_host, new_db_name, db_user, current_password, pspadm_password)

    except Exception as e:
        print(f"Error during staging refresh: {e}")

if __name__ == '__main__':
    main()
