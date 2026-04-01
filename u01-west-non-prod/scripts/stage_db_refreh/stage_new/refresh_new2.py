import boto3
import subprocess
import psycopg2
import os
from psycopg2 import OperationalError
import time

# Read passwords from file
def read_passwords_from_file(filepath):
    user_passwords = {}
    with open(filepath, 'r') as file:
        for line in file:
            line = line.strip()
            if ':' in line:
                username, password = line.split(':', 1)
                user_passwords[username.strip()] = password.strip()
    return user_passwords

# Fetch latest snapshot
def fetch_latest_snapshot(rds_client, db_cluster_identifier):
    snapshots = rds_client.describe_db_cluster_snapshots(
        DBClusterIdentifier=db_cluster_identifier,
        MaxRecords=20
    )['DBClusterSnapshots']
    if not snapshots:
        raise Exception(f"No snapshots found for cluster {db_cluster_identifier}")
    snapshots.sort(key=lambda x: x['SnapshotCreateTime'], reverse=True)
    return snapshots[0]

# Fetch cluster info
def fetch_db_cluster_info(rds_client, db_cluster_identifier):
    info = rds_client.describe_db_clusters(DBClusterIdentifier=db_cluster_identifier)['DBClusters'][0]
    writer_instance_id = next((m['DBInstanceIdentifier'] for m in info['DBClusterMembers'] if m['IsClusterWriter']), None)
    if writer_instance_id:
        instance = rds_client.describe_db_instances(DBInstanceIdentifier=writer_instance_id)['DBInstances'][0]
        info['InstanceParameterGroup'] = instance['DBParameterGroups'][0]['DBParameterGroupName']
    else:
        info['InstanceParameterGroup'] = None
    return info

# Get instance class
def get_instance_class_from_existing_cluster(rds_client, cluster_info):
    for member in cluster_info['DBClusterMembers']:
        if member.get('IsClusterWriter', False):
            instance = rds_client.describe_db_instances(DBInstanceIdentifier=member['DBInstanceIdentifier'])['DBInstances'][0]
            return instance['DBInstanceClass']
    return None

# Wait for instance to be available
def wait_for_db_instance_available(rds_client, db_instance_identifier):
    while True:
        status = rds_client.describe_db_instances(DBInstanceIdentifier=db_instance_identifier)['DBInstances'][0]['DBInstanceStatus']
        if status == 'available':
            break
        time.sleep(30)

# Create cluster from snapshot
def create_staging_db_from_snapshot(rds_client, snapshot_id, stage_cluster_id, cluster_info):
    new_cluster_id = f"{stage_cluster_id.lower().replace('_', '-')}-new".strip('-')
    instance_class = get_instance_class_from_existing_cluster(rds_client, cluster_info)
    if not instance_class:
        raise Exception("Instance class not found")

    rds_client.restore_db_cluster_from_snapshot(
        DBClusterIdentifier=new_cluster_id,
        SnapshotIdentifier=snapshot_id,
        Engine=cluster_info['Engine'],
        EngineVersion=cluster_info['EngineVersion'],
        Port=cluster_info['Port'],
        DBSubnetGroupName=cluster_info['DBSubnetGroup'],
        KmsKeyId=cluster_info['KmsKeyId'],
        VpcSecurityGroupIds=[sg['VpcSecurityGroupId'] for sg in cluster_info['VpcSecurityGroups']],
        CopyTagsToSnapshot=cluster_info['CopyTagsToSnapshot'],
        DeletionProtection=cluster_info['DeletionProtection'],
        EnableIAMDatabaseAuthentication=cluster_info['IAMDatabaseAuthenticationEnabled'],
        EngineMode=cluster_info['EngineMode'],
        DBClusterParameterGroupName=cluster_info['DBClusterParameterGroup']
    )

    writer_instance_id = f"{new_cluster_id}-1"
    rds_client.create_db_instance(
        DBInstanceIdentifier=writer_instance_id,
        DBClusterIdentifier=new_cluster_id,
        DBInstanceClass=instance_class,
        Engine=cluster_info['Engine'],
        PubliclyAccessible=False,
        AutoMinorVersionUpgrade=False,
        DBParameterGroupName=cluster_info['InstanceParameterGroup'],
        Tags=[tag for tag in cluster_info.get('TagList', []) if not tag['Key'].startswith('aws:')]
    )

    wait_for_db_instance_available(rds_client, writer_instance_id)

    # Wait for cluster to be available
    while True:
        status = rds_client.describe_db_clusters(DBClusterIdentifier=new_cluster_id)['DBClusters'][0]['Status']
        if status == 'available':
            break
        time.sleep(60)
    return new_cluster_id

# Terminate active sessions
def kill_active_connections(db_host, db_user, db_password):
    try:
        conn = psycopg2.connect(
            host=db_host, dbname='postgres', user=db_user, password=db_password, connect_timeout=10
        )
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute("""
            SELECT pg_terminate_backend(pid)
            FROM pg_stat_activity
            WHERE datname = 'postgres'
              AND usename NOT IN ('rdsadmin', 'postgres')
              AND pid <> pg_backend_pid();
        """)
        terminated = cur.fetchall()
        print(f"Terminated {len(terminated)} sessions.")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error terminating sessions: {e}")

# Reset password using \password via expect
def reset_password_with_psql(user, password, db_host):
    expect_script = f"""
log_user 1
set timeout 10

spawn env PGPASSWORD="{password}" psql -X -h {db_host} -d postgres -U intuadmin
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
    timeout {{
        puts "ERROR: Timeout waiting for prompt"
        exit 1
    }}
    eof {{
        puts "ERROR: Connection failed or closed"
        exit 1
    }}
}}
"""
    subprocess.run(['expect', '-c', expect_script])

# Create user if not exists and reset password
def check_and_create_users(db_host, db_port, db_user, db_password, new_passwords, required_users):
    try:
        conn = psycopg2.connect(
            host=db_host, port=db_port, dbname='postgres', user=db_user, password=db_password, connect_timeout=10
        )
        cur = conn.cursor()
        for user in required_users:
            cur.execute("SELECT 1 FROM pg_roles WHERE rolname = %s;", (user,))
            if not cur.fetchone():
                print(f"User '{user}' does not exist. Creating...")
                cur.execute(f"CREATE USER {user} WITH LOGIN;")
                conn.commit()
            else:
                print(f"User '{user}' already exists.")
            print(f"Resetting password for {user}...")
            reset_password_with_psql(user, new_passwords[user], db_host)
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error checking/creating users: {e}")

# Main function
def main():
    rds_client = boto3.client('rds', region_name='us-west-2')
    prod_cluster_id = 'ppsp-ppd-arc'
    stage_cluster_id = 'ppsp-ppd-arc'
    old_pwd_file = '/u01/scripts/stage_db_refreh/stage_new/db_password.txt'
    new_pwd_file = '/u01/scripts/stage_db_refreh/stage_new/intuadmin_new_password.txt'

    db_passwords = read_passwords_from_file(old_pwd_file)
    new_passwords = read_passwords_from_file(new_pwd_file)

    latest_snapshot = fetch_latest_snapshot(rds_client, prod_cluster_id)
    cluster_info = fetch_db_cluster_info(rds_client, stage_cluster_id)
    new_cluster_id = create_staging_db_from_snapshot(rds_client, latest_snapshot['DBClusterSnapshotIdentifier'], stage_cluster_id, cluster_info)

    db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_cluster_id)['DBClusters'][0]['Endpoint']

    intuadmin_pwd = db_passwords.get('intuadmin')
    if intuadmin_pwd:
        kill_active_connections(db_host, 'intuadmin', intuadmin_pwd)
        required_users = ['intuadmin', 'pspadm_owner']
        check_and_create_users(db_host, 5432, 'intuadmin', intuadmin_pwd, new_passwords, required_users)
    else:
        print("Error: intuadmin password not found in db_password.txt")

if __name__ == "__main__":
    main()
