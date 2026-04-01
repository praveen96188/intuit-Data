import boto3
import subprocess
import psycopg2
import os
from psycopg2 import OperationalError
import time

def read_passwords_from_file(filepath):
    user_passwords = {}
    try:
        with open(filepath, 'r') as file:
            for line in file:
                line = line.strip()
                if ':' in line:
                    username, password = line.split(':', 1)
                    user_passwords[username.strip()] = password.strip()
    except Exception as e:
        print(f"Error reading password file {filepath}: {e}")
    return user_passwords

def fetch_latest_snapshot(rds_client, db_cluster_identifier):
    snapshots = rds_client.describe_db_cluster_snapshots(
        DBClusterIdentifier=db_cluster_identifier,
        MaxRecords=20
    )['DBClusterSnapshots']
    if not snapshots:
        raise Exception(f"No snapshots found for cluster {db_cluster_identifier}")
    snapshots.sort(key=lambda x: x['SnapshotCreateTime'], reverse=True)
    latest_snapshot = snapshots[0]
    print(f"Latest snapshot: {latest_snapshot['DBClusterSnapshotIdentifier']}")
    return latest_snapshot

def fetch_db_cluster_info(rds_client, db_cluster_identifier):
    response = rds_client.describe_db_clusters(DBClusterIdentifier=db_cluster_identifier)
    db_cluster_info = response['DBClusters'][0]
    writer_instance_id = next((m['DBInstanceIdentifier'] for m in db_cluster_info['DBClusterMembers'] if m['IsClusterWriter']), None)
    if writer_instance_id:
        instance_details = rds_client.describe_db_instances(DBInstanceIdentifier=writer_instance_id)
        db_instance = instance_details['DBInstances'][0]
        db_cluster_info['InstanceParameterGroup'] = db_instance['DBParameterGroups'][0]['DBParameterGroupName']
    else:
        db_cluster_info['InstanceParameterGroup'] = None
    db_cluster_info['ClusterParameterGroup'] = db_cluster_info.get('DBClusterParameterGroup', '')
    return db_cluster_info

def get_instance_class_from_existing_cluster(rds_client, existing_stage_cluster_info):
    for instance in existing_stage_cluster_info['DBClusterMembers']:
        if instance.get('IsClusterWriter', False):
            instance_details = rds_client.describe_db_instances(DBInstanceIdentifier=instance['DBInstanceIdentifier'])
            return instance_details['DBInstances'][0]['DBInstanceClass']
    return None

def wait_for_db_instance_available(rds_client, db_instance_identifier):
    print(f"Waiting for DB instance {db_instance_identifier} to become available...")
    while True:
        status = rds_client.describe_db_instances(DBInstanceIdentifier=db_instance_identifier)['DBInstances'][0]['DBInstanceStatus']
        if status == 'available':
            print(f"DB instance {db_instance_identifier} is available!")
            break
        time.sleep(30)

def create_staging_db_from_snapshot(rds_client, snapshot_identifier, stage_cluster_identifier, existing_stage_cluster_info):
    new_db_cluster_identifier = f"{stage_cluster_identifier.lower().replace('_', '-')}-new".strip('-')
    instance_class = get_instance_class_from_existing_cluster(rds_client, existing_stage_cluster_info)
    if not instance_class:
        raise Exception("Could not determine instance class.")
    rds_client.restore_db_cluster_from_snapshot(
        DBClusterIdentifier=new_db_cluster_identifier,
        SnapshotIdentifier=snapshot_identifier,
        Engine=existing_stage_cluster_info['Engine'],
        EngineVersion=existing_stage_cluster_info['EngineVersion'],
        Port=existing_stage_cluster_info['Port'],
        DBSubnetGroupName=existing_stage_cluster_info['DBSubnetGroup'],
        KmsKeyId=existing_stage_cluster_info['KmsKeyId'],
        VpcSecurityGroupIds=[sg['VpcSecurityGroupId'] for sg in existing_stage_cluster_info['VpcSecurityGroups']],
        CopyTagsToSnapshot=existing_stage_cluster_info['CopyTagsToSnapshot'],
        DeletionProtection=existing_stage_cluster_info['DeletionProtection'],
        EnableIAMDatabaseAuthentication=existing_stage_cluster_info['IAMDatabaseAuthenticationEnabled'],
        EngineMode=existing_stage_cluster_info['EngineMode'],
        DBClusterParameterGroupName=existing_stage_cluster_info['ClusterParameterGroup']
    )
    writer_instance_identifier = f"{new_db_cluster_identifier}-1"
    tags = [tag for tag in existing_stage_cluster_info.get('TagList', []) if not tag['Key'].startswith('aws:')]
    rds_client.create_db_instance(
        DBInstanceIdentifier=writer_instance_identifier,
        DBClusterIdentifier=new_db_cluster_identifier,
        DBInstanceClass=instance_class,
        Engine=existing_stage_cluster_info['Engine'],
        PubliclyAccessible=False,
        AutoMinorVersionUpgrade=False,
        DBParameterGroupName=existing_stage_cluster_info['InstanceParameterGroup'],
        Tags=tags
    )
    wait_for_db_instance_available(rds_client, writer_instance_identifier)
    while True:
        status = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Status']
        if status == 'available':
            break
        time.sleep(60)
    return new_db_cluster_identifier

def kill_active_connections(db_host, db_name, db_user, db_password):
    try:
        conn = psycopg2.connect(host=db_host, database=db_name, user=db_user, password=db_password, connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute("""
            SELECT pg_terminate_backend(pid)
            FROM pg_stat_activity
            WHERE usename NOT IN ('rdsadmin', 'postgres')
              AND pid <> pg_backend_pid();
        """)
        print(f"Terminated {len(cur.fetchall())} sessions.")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error terminating sessions: {e}")

def reset_password_with_psql(user, old_password, new_password, db_host):
    import tempfile
    import subprocess
    import os

    expect_script = f"""
log_user 1
set timeout 20

spawn env PGPASSWORD="{old_password}" psql -X -h {db_host} -d postgres -U {user}

expect {{
    -re "postgres=\\\\#|postgres=\\\\>" {{
        send "\\\\password {user}\\r"
        expect "Enter new password:"
        send "{new_password}\\r"
        expect "Enter it again:"
        send "{new_password}\\r"
        expect -re "postgres=\\\\#|postgres=\\\\>"
        send "\\\\q\\r"
        expect eof
    }}
    timeout {{
        puts "ERROR: Timeout waiting for psql prompt."
        exit 1
    }}
    eof {{
        puts "ERROR: Connection failed or closed unexpectedly."
        exit 1
    }}
}}
"""

    with tempfile.NamedTemporaryFile(mode="w", suffix=".expect", delete=False) as f:
        f.write(expect_script)
        script_path = f.name

    try:
        subprocess.run(['expect', script_path], check=True)
    except subprocess.CalledProcessError as e:
        print(f"Password reset failed for user '{user}': {e}")
    finally:
        os.remove(script_path)

def check_and_create_users(db_host, db_port, db_name, postgres_user, login_pw_file, reset_pw_file, required_users):
    login_passwords = read_passwords_from_file(login_pw_file)
    reset_passwords = read_passwords_from_file(reset_pw_file)
    login_password = login_passwords.get(postgres_user)
    if not login_password:
        print(f"Login password for '{postgres_user}' not found.")
        return
    try:
        conn = psycopg2.connect(
            host=db_host, port=db_port, dbname=db_name,
            user=postgres_user, password=login_password, connect_timeout=120
        )
        cur = conn.cursor()
        for user in required_users:
            cur.execute(f"SELECT 1 FROM pg_roles WHERE rolname = '{user}';")
            if cur.fetchone():
                print(f"User '{user}' exists. Resetting password.")
            else:
                print(f"Creating user '{user}'...")
                cur.execute(f"CREATE USER {user} WITH LOGIN;")
            new_password = reset_passwords.get(user)
            if new_password:
                reset_password_with_psql(user, login_password, new_password, db_host)
            else:
                print(f"No new password found for user '{user}'")
        conn.commit()
        cur.close()
        conn.close()
    except OperationalError as e:
        print(f"Connection error: {e}")

def main():
    rds_client = boto3.client('rds', region_name='us-west-2')
    prod_cluster_identifier = 'ppsp-ppd-arc'
    stage_cluster_identifier = 'ppsp-ppd-arc'
    login_pw_file = '/u01/scripts/stage_db_refreh/stage_new/db_password.txt'
    reset_pw_file = '/u01/scripts/stage_db_refreh/stage_new/intuadmin_new_password.txt'

    print("\n=== STEP 1: Create new staging cluster ===")
    snapshot = fetch_latest_snapshot(rds_client, prod_cluster_identifier)
    cluster_info = fetch_db_cluster_info(rds_client, stage_cluster_identifier)
    cluster_info['KmsKeyId'] = 'arn:aws:kms:us-west-2:152430470825:key/8d5ce945-95b1-40d7-b070-219793d62934'
    new_cluster_id = create_staging_db_from_snapshot(rds_client, snapshot['DBClusterSnapshotIdentifier'], stage_cluster_identifier, cluster_info)
    db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_cluster_id)['DBClusters'][0]['Endpoint']
    db_name = 'psppdarc'
    db_user = 'intuadmin'
    login_passwords = read_passwords_from_file(login_pw_file)
    db_password = login_passwords.get(db_user)

    if db_password:
        kill_active_connections(db_host, db_name, db_user, db_password)
    else:
        print("Login password for intuadmin not found.")

    required_users = ['intuadmin', 'pspadm_owner']
    check_and_create_users(
        db_host=db_host,
        db_port=5432,
        db_name=db_name,
        postgres_user=db_user,
        login_pw_file=login_pw_file,
        reset_pw_file=reset_pw_file,
        required_users=required_users
    )

if __name__ == "__main__":
    main()
