import boto3
import subprocess
import psycopg2
import pexpect
import os
from psycopg2 import OperationalError
import time
import shlex
import argparse

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
###         KmsKeyId='arn:aws:kms:us-west-2:152430470825:key/8d5ce945-95b1-40d7-b070-219793d62934',
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
def kill_active_connections(db_host, db_name, master_user, db_password, port):
    try:
        conn = psycopg2.connect(host=db_host, database=db_name, user=master_user, password=db_password, port=PORT,connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute(f"""
            SELECT pg_terminate_backend(pid)
            FROM pg_stat_activity
            WHERE datname = '{db_name}' AND pid <> pg_backend_pid()
              AND usename NOT IN ('rdsadmin', 'postgres');
        """)
        terminated = cur.fetchall()
        print(f"Terminated {len(terminated)} active session(s).")
        cur.close()
        conn.close()
        time.sleep(5)  # Ensure time to terminate
    except Exception as e:
        print(f"Error terminating sessions: {e}")

def run_psql_password(db_host, db_name, master_user, target_user, old_password, new_password, port):
    try:
        child = pexpect.spawn(
            '/usr/bin/psql',
            ['-h', db_host, '-U', master_user, '-d', db_name, '-p', PORT],
            timeout=15
        )

        child.expect('Password for user .*:')
        child.sendline(old_password)

        child.expect_exact([f'{db_name}=#', f'{db_name}=>'])
        child.sendline(f'\\password {target_user}')

        child.expect(f'Enter new password for user "{target_user}":')
        child.sendline(new_password)

        child.expect("Enter it again:")
        child.sendline(new_password)

        child.expect_exact([f'{db_name}=#', f'{db_name}=>'])
        child.sendline('\\q')
        child.close()

        print(f"Password for user {target_user} reset successfully.")

    except pexpect.exceptions.TIMEOUT as e:
        print(f"Timeout error resetting password for {target_user}: {e}")
    except Exception as e:
        print(f"Error resetting password for {target_user}: {e}")

def drop_replication_slots(db_host, master_user, db_password, port):
    try:
        conn = psycopg2.connect(host=db_host, database='postgres', user=master_user, password=db_password, port=PORT, connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute("SELECT slot_name FROM pg_replication_slots;")
        slots = cur.fetchall()
        for slot in slots:
            print(f"Dropping replication slot: {slot[0]}")
            cur.execute(f"SELECT pg_drop_replication_slot('{slot[0]}');")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error dropping replication slots: {e}")

def rename_database(db_host, master_user, db_password, old_name, new_name, port):
    try:
        conn = psycopg2.connect(host=db_host, database='postgres', user=master_user, password=db_password, port=PORT, connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute(f"ALTER DATABASE {old_name} RENAME TO {new_name};")
        cur.close()
        conn.close()
        print(f"Renamed database from {old_name} to {new_name}.")
    except Exception as e:
        print(f"Error renaming database: {e}")

def execute_sql_file(db_host, db_name, master_user, db_password, sql_file):
    try:
        cmd = [
            'psql',
            '-h', db_host,
            '-U', master_user,
            '-d', db_name,
            '-f', sql_file,
            '-p', port
        ]
        env = os.environ.copy()
        env['PGPASSWORD'] = db_password
        subprocess.run(cmd, check=True, env=env)
        print(f"Executed SQL file: {sql_file}")
    except subprocess.CalledProcessError as e:
        print(f"Error executing SQL file: {e}")

def manage_pspadm_owner(db_host, db_name, master_user, db_password, new_password, port):
    try:
        conn = psycopg2.connect(host=db_host, database=db_name, user=master_user, password=db_password, port=PORT, connect_timeout=10)
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute("SELECT 1 FROM pg_roles WHERE rolname={schema_owner};")
        if cur.fetchone():
            print("User {schema_owner} exists. Resetting password...")
        else:
            print("Creating user {schema_owner}...")
            cur.execute("CREATE USER {schema_owner} WITH LOGIN;")
        
        # Grant CONNECT privilege
        cur.execute(f"GRANT CONNECT ON DATABASE {db_name} TO {schema_owner};")
        print(f"Granted CONNECT on database {db_name} to {schema_owner}.")

        cur.close()
        conn.close()

        run_psql_password(db_host, db_name, master_user, schema_owner, db_password, new_password, PORT)
    except Exception as e:
        print(f"Error managing pspadm_owner user: {e}")

def database_exists(db_host, master_user, db_password, db_name):
    try:
        conn = psycopg2.connect(host=db_host, user=master_user, password=db_password, dbname='postgres', port=PORT, connect_timeout=10)
        cur = conn.cursor()
        cur.execute("SELECT 1 FROM pg_database WHERE datname = %s;", (db_name,))
        exists = cur.fetchone() is not None
        cur.close()
        conn.close()
        return exists
    except Exception as e:
        print(f"Error checking database existence: {e}")
        return False

def manage_app_users(db_host, db_name, admin_user, admin_password, user_password_file, PORT):
    user_passwords = read_passwords_from_file(user_password_file)
    for user, password in user_passwords.items():
        try:
            conn = psycopg2.connect(host=db_host, database=db_name, user=admin_user, password=admin_password, port=PORT, connect_timeout=10)
            conn.autocommit = True
            cur = conn.cursor()
            cur.execute("SELECT 1 FROM pg_roles WHERE rolname=%s;", (user,))
            if not cur.fetchone():
                print(f"Creating user {user}...")
                cur.execute(f"CREATE USER {user} WITH LOGIN;")

                # Grant CONNECT privilege
                cur.execute(f"GRANT CONNECT ON DATABASE {db_name} TO {user};")
                print(f"Granted CONNECT on database {db_name} to {user}.")

            else:
                print(f"User {user} already exists.")
            cur.close()
            conn.close()

            # Reset password using \password
            run_psql_password(db_host, db_name, admin_user, user, admin_password, password, PORT)

        except Exception as e:
            print(f"Error managing user {user}: {e}")

# Main function
def main(prod_cluster_identifier, stage_cluster_identifier, db_name, new_db_name):
    rds_client = boto3.client('rds', region_name='us-west-2')

    password_file_path = '/u01/scripts/staging_refresh/.arop'
    new_password_file_path = '/u01/scripts/staging_refresh/.arnp'
    appusers_file_path = '/u01/scripts/staging_refresh/.arap'
    sql_file = '/u01/scripts/staging_refresh/ar_postgres_postgres.sql'
    pitparmo_sql_file = '/u01/scripts/staging_refresh/ar_pspaarc_postgres.sql'
##   vacuum_script_path = '/u01/scripts/staging_refresh/vacuum/vacuum_pitparmo-new.sh'
##   analyze_script_path = '/u01/scripts/staging_refresh/vacuum/analyze/ppsp-ppd-new.sh'
    pspadm_sql_file = '/u01/scripts/staging_refresh/ar_psparcowner_pspaudcdb.sql'
    def_db_name = 'postgres'
    master_user = 'intuadmin'
    schema_owner = 'psparc_owner'
    PORT = 5432

    # Debugging: Print the configurations
    print(f"Database configuration for '{new_db_name}':")
    print(f"new_password_file '{new_password_file_path}':")
    print(f"Password File Path: {password_file_path}")
    print(f"appusers_file  '{appusers_file_path}':")
    print(f"def_db_nameDB '{def_db_name}':")
    print(f"master_user_user '{master_user}':")
    print(f"schema_owner_user '{schema_owner}':")
    print(f"SQL File: {sql_file}")
    print(f"Port: {PORT}")

    # Read current and new passwords
    passwords = read_passwords_from_file(password_file_path)
    new_passwords = read_passwords_from_file(new_password_file_path)

    current_password = passwords.get(master_user)
    intuadmin_password = new_passwords.get(master_user)
    pspadm_password = new_passwords.get(schema_owner, intuadmin_password)  # fallback if not provided

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
    
        if current_password and intuadmin_password:
            try:
                run_psql_password(db_host, db_name, master_user, master_user, current_password, intuadmin_password, PORT)
                drop_replication_slots(db_host, master_user, intuadmin_password, PORT)
                kill_active_connections(db_host, db_name, master_user, intuadmin_password, PORT)
                rename_database(db_host, master_user, intuadmin_password, PORT,old_name=db_name, new_name=new_db_name)
                rename_success = True
            except Exception as e:
                print(f"Error during password reset/rename: {e}")

        if rename_success and database_exists(db_host, master_user, intuadmin_password, new_db_name, PORT):
            execute_sql_file(db_host, def_db_name, master_user, intuadmin_password, sql_file , PORT)
            manage_pspadm_owner(db_host, def_db_name, master_user, intuadmin_password, pspadm_password, PORT)
        else:
            print("Skipping SQL execution and user management due to earlier errors.")

        # STEP 2: Execute pspadm_owner SQL file
            print(f"Checking if user {schema_owner} can connect to database '{new_db_name}'...")
        if database_exists(db_host, schema_owner, pspadm_password, new_db_name, PORT):
            print(f"Connection successful. Executing SQL file: {pspadm_sql_file}")
            try:
                execute_sql_file(db_host, new_db_name, schema_owner, pspadm_password, pspadm_sql_file)
            except Exception as e:
                print(f"Error executing SQL file as {schema_owner}: {e}")
        else:
            print("Connection failed for user {schema_owner}. Skipping SQL execution.")

        # STEP 3: Manage app users
        print("\n=== STEP 3: Managing app users ===")
        manage_app_users(db_host, new_db_name, master_user, intuadmin_password, appusers_file_path, PORT)

        # STEP 4: Execute pitparmo_postgres.sql
        print("\n=== STEP 4: Executing pitparmo_postgres.sql ===")
        try:
            execute_sql_file(db_host, new_db_name, master_user, intuadmin_password, pitparmo_sql_file, PORT)
        except Exception as e:
            print(f"Error executing pitparmo_postgres.sql: {e}")
        
        # 5. Add AWS backup tag
        print("\n=== STEP 5: Adding AWS backup tag ===")
        def get_account_id():
            sts = boto3.client("sts")
            return sts.get_caller_identity()["Account"]

        account_id = get_account_id()
        rds_client.add_tags_to_resource(
            ResourceName=f"arn:aws:rds:us-west-2:{account_id}:cluster:{new_db_cluster_identifier}",
            Tags=[{'Key': 'org_awsbackup_local_0700UTC', 'Value': '7continuous'}]
        )
        print("✅ Tag 'org_awsbackup_local_0700UTC=7continuous' applied successfully.")

        # 6. Run VACUUM script if exists
        print("\n=== STEP 6: Running VACUUM script ===")
        if os.path.exists(vacuum_script_path):
          print("🧹 Running VACUUM script for all tables...")
          log_filename = f"vacuum_{time.strftime('%Y%m%d')}.log"
          log_path = os.path.join(os.path.dirname(vacuum_script_path), log_filename)

          with open(log_path, 'w') as log_file:
             subprocess.run(
            ['bash', vacuum_script_path],
            cwd=os.path.dirname(vacuum_script_path),
            stdout=log_file,
            stderr=subprocess.STDOUT,
            check=True
           )
        print(f"✅ VACUUM completed successfully. Log saved to {log_path}")

        # 7. Run ANALYZE script if exists
        print("\n=== STEP 7: Running ANALYZE script ===")
       # if os.path.exists(analyze_script_path):
        #    print("📊 Running ANALYZE script for all tables...")
         #   subprocess.run(
          #      ['bash', analyze_script_path],
         #       cwd=os.path.dirname(analyze_script_path),
          #      check=True
          #  )
          #  print("✅ ANALYZE completed successfully.")
        #else:
         #   print(f"⚠️ Analyze script not found at {analyze_script_path}")

        print("\n=== DATABASE REFRESH COMPLETED SUCCESSFULLY ===")

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

