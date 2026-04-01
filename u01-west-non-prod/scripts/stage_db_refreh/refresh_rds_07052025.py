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
                    user, password = line.split(':', 1)  # Split on first colon only
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
                    user, password = line.split(':', 1)  # Split on first colon only
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

    # Get cluster-level parameter group
    db_cluster_info['ClusterParameterGroup'] = db_cluster_info.get('DBClusterParameterGroup', '')

    # Get the writer instance to fetch its DBParameterGroup
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
            DBClusterParameterGroupName=existing_stage_cluster_info['ClusterParameterGroup']
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
            DBParameterGroupName=existing_stage_cluster_info['InstanceParameterGroup'],
            Tags=filtered_tags
        )

        # Wait for the new writer instance to become available
        wait_for_db_instance_available(rds_client, writer_instance_identifier)

        # Wait for the DB cluster to become available
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

# Function to check active connections in the DB and kill them if needed
def check_active_connections(db_host, db_port, db_name, postgres, password_file_path):
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
        connection.autocommit = True  # Important for killing connections

        cursor = connection.cursor()
        cursor.execute(""" 
            SELECT pid, usename, application_name, client_addr
            FROM pg_stat_activity
            WHERE usename NOT IN ('postgres', 'rdsadmin')
        """)
        active_connections = cursor.fetchall()

        if active_connections:
            print(f"Active connections found: {len(active_connections)}")
            for conn in active_connections:
                print(f"Killing connection: {conn}")
                pid = conn[0]
                try:
                    cursor.execute(f"SELECT pg_terminate_backend({pid});")
                    print(f"Connection with pid {pid} has been terminated.")
                except Exception as e:
                    print(f"Error killing connection {pid}: {e}")
            
        else:
            print("No active connections found.")

        cursor.close()
        connection.close()
    except OperationalError as e:
        print(f"Error connecting to the database: {e}")
        if 'connection' in locals():
            connection.close()

def drop_replication_slots_if_any(db_host, db_port, db_name, postgres, password_file_path):
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
        connection.autocommit = True
        cursor = connection.cursor()

        cursor.execute("SELECT slot_name FROM pg_replication_slots;")
        slots = cursor.fetchall()

        if not slots:
            print("✅ No replication slots found.")
        else:
            print(f"⚠️ Found {len(slots)} replication slots. Dropping them...")
            for slot in slots:
                slot_name = slot[0]
                try:
                    drop_query = f"SELECT pg_drop_replication_slot('{slot_name}');"
                    cursor.execute(drop_query)
                    print(f"Dropped replication slot: {slot_name}")
                except Exception as e:
                    print(f"Error dropping slot {slot_name}: {e}")
            
        cursor.close()
        connection.close()

    except Exception as e:
        print(f"❌ Error while checking/dropping replication slots: {e}")
        if 'connection' in locals():
            connection.close()

# Function to rename the DB
def rename_db(db_host, db_port, old_db_name, new_db_name, postgres, password_file_path):
    user_passwords = read_passwords_from_file(password_file_path)
    password = user_passwords.get(postgres, None)

    if not password:
        print(f"Password for user '{postgres}' not found in the password file.")
        return False

    try:
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            dbname='postgres',
            user=postgres,
            password=password,
            connect_timeout=120
        )
        connection.autocommit = True
        cursor = connection.cursor()

        # Check if old database exists
        cursor.execute(f"SELECT 1 FROM pg_database WHERE datname = '{old_db_name}'")
        if not cursor.fetchone():
            print(f"Database {old_db_name} does not exist")
            return False

        # Check if new database already exists
        cursor.execute(f"SELECT 1 FROM pg_database WHERE datname = '{new_db_name}'")
        if cursor.fetchone():
            print(f"Database {new_db_name} already exists")
            return False

        # Check for active connections
        cursor.execute(f"""
            SELECT pid, usename, application_name, client_addr
            FROM pg_stat_activity
            WHERE usename NOT IN ('postgres', 'rdsadmin') AND datname = '{old_db_name}'
        """)
        active_connections = cursor.fetchall()

        if active_connections:
            print(f"Active connections found to database {old_db_name}. Killing them...")
            for conn in active_connections:
                pid = conn[0]
                try:
                    cursor.execute(f"SELECT pg_terminate_backend({pid});")
                    print(f"Killed connection with pid {pid}")
                except Exception as e:
                    print(f"Error killing connection {pid}: {e}")

        # Rename the database
        cursor.execute(f"ALTER DATABASE {old_db_name} RENAME TO {new_db_name};")
        print(f"Successfully renamed database from {old_db_name} to {new_db_name}.")
        return True

    except Exception as e:
        print(f"Error renaming database: {e}")
        return False
    finally:
        if 'connection' in locals():
            connection.close()

# Function to update cron jobs (only if pg_cron extension exists)
def update_cron_jobs(db_host, db_port, new_db_name, postgres, password_file_path):
    user_passwords = read_passwords_from_file(password_file_path)
    password = user_passwords.get(postgres, None)

    if not password:
        print(f"Password for user '{postgres}' not found in the password file.")
        return

    try:
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            dbname='postgres',
            user=postgres,
            password=password,
            connect_timeout=120
        )
        connection.autocommit = True
        cursor = connection.cursor()

        # Check if pg_cron extension exists
        cursor.execute("SELECT 1 FROM pg_extension WHERE extname = 'pg_cron'")
        if not cursor.fetchone():
            print("⚠️ pg_cron extension not found. Skipping cron job updates.")
            return

        # Update cron jobs
        try:
            cursor.execute(f"UPDATE cron.job SET database = '{new_db_name}' WHERE jobid = 1;")
            cursor.execute(f"UPDATE cron.job SET database = '{new_db_name}' WHERE jobid = 34;")
            print("✅ Successfully updated cron jobs.")
        except Exception as e:
            print(f"⚠️ Error updating cron jobs: {e}")
        
        cursor.close()
        connection.close()

    except Exception as e:
        print(f"Error checking/updating cron jobs: {e}")
        if 'connection' in locals():
            connection.close()

# Function to check and create users if needed
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
        connection.autocommit = True
        cursor = connection.cursor()

        # First create required roles if they don't exist
        role_creation_sql = """
        DO $$
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'pspadm_readwrite_role') THEN
                CREATE ROLE pspadm_readwrite_role;
            END IF;
            
            IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'pspadm_readonly_role') THEN
                CREATE ROLE pspadm_readonly_role;
            END IF;
        END
        $$;
        """
        try:
            cursor.execute(role_creation_sql)
            print("✅ Created required roles if they didn't exist")
        except Exception as e:
            print(f"⚠️ Error creating roles: {e}")

        for user in required_users:
            try:
                cursor.execute(f"SELECT 1 FROM pg_roles WHERE rolname = '{user}';")
                if cursor.fetchone():
                    print(f"User '{user}' already exists.")
                else:
                    print(f"Creating user '{user}' with login")
                    cursor.execute(f"CREATE USER {user} WITH LOGIN;")
                    print(f"User '{user}' created.")
            except Exception as e:
                print(f"Error handling user {user}: {e}")
                continue
        
        cursor.close()
        connection.close()

    except OperationalError as e:
        print(f"Error connecting to the database: {e}")
        if 'connection' in locals():
            connection.close()

# Function to reset passwords using psql \password command
def reset_password_with_psql(user, new_password, db_host, db_name):
    escaped_password = shlex.quote(new_password)
    expect_script = f"""
    log_user 1
    log_file /tmp/psql_password_change.log
    set timeout 10
  
    spawn env PGPASSWORD={escaped_password} psql -X -h {db_host} -d {db_name} -U postgres

    expect {{
        "*=>" {{
            puts stdout "Connected. Checking if user '{user}' exists..."
            flush stdout
            send "SELECT 1 FROM pg_roles WHERE rolname = '{user}';\\r"
            expect {{
                "1 row" {{
                    puts stdout "User '{user}' exists. Attempting password change..."
                    flush stdout
                    send "\\\\password {user}\\r"
                    expect "Enter new password:"
                    send "{new_password}\\r"
                    expect "Enter it again:"
                    send "{new_password}\\r"
                    expect  "*=>"
                    puts stdout "Password change completed for user '{user}'."
                    flush stdout
                    send "\\\\q\\r"
                    expect eof
                }}
                "0 rows" {{
                    puts stdout "ERROR: User '{user}' does not exist."
                    flush stdout
                    send "\\\\q\\r"
                    expect eof
                    exit 1
                }}
                timeout {{
                    puts stdout "ERROR: Timeout while checking user existence."
                    flush stdout
                    exit 1
                }}
            }}
        }}
        "could not connect"* {{
            puts stdout "ERROR: Could not connect to the database."
            flush stdout
            exit 1
        }}
        timeout {{
            puts stdout "ERROR: Timeout waiting for psql prompt."
            flush stdout
            exit 1
        }}
        eof {{
            puts stdout "ERROR: Spawn failed or connection closed unexpectedly."
            flush stdout
            exit 1
        }}
    }}
    """    
    result = subprocess.run(['expect'], input=expect_script, text=True, capture_output=True)

    if result.returncode == 0:
        print(f"✅ Password changed successfully for user: {user}")
    else:
        print(f"❌ Failed to change password for user: {user}")
        print(result.stderr)

# Function to execute SQL from file with better error handling
def execute_sql_from_file(db_host, db_port, db_name, postgres, password_file_path, sql_file_path, placeholders=None):
    user_passwords = read_passwords_from_file(password_file_path)
    password = user_passwords.get(postgres, None)

    if not password:
        print(f"Password for user '{postgres}' not found in the password file.")
        return

    if not os.path.exists(sql_file_path):
        print(f"SQL file not found at {sql_file_path}")
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
        connection.autocommit = True  # Use autocommit to prevent transaction issues
        cursor = connection.cursor()

        with open(sql_file_path, 'r') as sql_file:
            sql_content = sql_file.read()

            if placeholders:
                for key, value in placeholders.items():
                    sql_content = sql_content.replace(key, value)

            queries = [q.strip() for q in sql_content.split(';') if q.strip()]
            for query in queries:
                try:
                    cursor.execute(query)
                    print(f"Executed SQL: {query[:100]}...")  # Print first 100 chars to avoid huge logs
                except Exception as e:
                    print(f"Error executing query: {query[:100]}... Error: {e}")
                    # Continue to next query even if one fails
                    continue

        cursor.close()
        connection.close()

    except OperationalError as e:
        print(f"Error connecting to the database: {e}")
        if 'connection' in locals():
            connection.close()

# Main function to orchestrate the process
def main():
    rds_client = boto3.client('rds', region_name='us-west-2')

    prod_cluster_identifier = 'ppsp-ppd-arc'
    stage_cluster_identifier = 'ppsp-ppd-arc'
    password_file_path = '/u01/scripts/stage_db_refreh/db_password.txt'
    appuser_password_file_path = '/u01/scripts/stage_db_refreh/appuser_passwords.txt'
    sql_file_path = '/u01/scripts/stage_db_refreh/sql_file.sql'
    lock_users_sql_path = '/u01/scripts/stage_db_refreh/postgres_postgres.sql'
    grants_sql_path = '/u01/scripts/stage_db_refreh/pspadmowner_pitparmo.sql'
    appuser_grants_sql_path = '/u01/scripts/stage_db_refreh/postgres_pitparmo.sql'
    vacuum_script_path = '/u01/scripts/stage_db_refreh/vacuum_pitparmo-new.sh'
    analyze_script_path = '/u01/scripts/stage_db_refreh/analyze_pitparmo-new.sh'

    try:
        # 1. Fetch latest snapshot and create new cluster
        print("\n=== STEP 1: Fetching latest snapshot and creating new cluster ===")
        latest_snapshot = fetch_latest_snapshot(rds_client, prod_cluster_identifier)
        existing_stage_cluster_info = fetch_db_cluster_info(rds_client, stage_cluster_identifier)
        new_db_cluster_identifier = create_staging_db_from_snapshot(
            rds_client,
            latest_snapshot['DBClusterSnapshotIdentifier'],
            stage_cluster_identifier,
            existing_stage_cluster_info
        )

        # Get new cluster endpoint
        db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']

        # 2. Check for active connections
        print("\n=== STEP 2: Checking for active connections ===")
        check_active_connections(
            db_host=db_host,
            db_port=5432,
            db_name='psppdarc',
            postgres='intuadmin',
            password_file_path=password_file_path
        )

        # 3. Drop replication slots if any
        print("\n=== STEP 3: Checking for replication slots ===")
        drop_replication_slots_if_any(
            db_host=db_host,
            db_port=5432,
            db_name='psppdarc',
            postgres='intuadmin',
            password_file_path=password_file_path
        )

        # 4. Rename the database
        print("\n=== STEP 4: Renaming database ===")
        rename_db(
            db_host=db_host,
            db_port=5432,
            old_db_name='psppdarc',
            new_db_name='psppdarc1',
            postgres='intuadmin',
            password_file_path=password_file_path
        )

        # 5. Update cron jobs
        print("\n=== STEP 5: Updating cron jobs ===")
        update_cron_jobs(
            db_host=db_host,
            db_port=5432,
            new_db_name='postgres',
            postgres='intuadmin',
            password_file_path=password_file_path
        )

        # 6. Lock original prod users
        print("\n=== STEP 6: Locking original users ===")
        execute_sql_from_file(
            db_host=db_host,
            db_port=5432,
            db_name='postgres',
            postgres='intuadmin',
            password_file_path=password_file_path,
            sql_file_path=lock_users_sql_path
        )

        # 7. Handle users and passwords
        print("\n=== STEP 7: Handling users and passwords ===")
        user_passwords = read_passwords_from_file(password_file_path)
        appuser_passwords = read_appuser_passwords(appuser_password_file_path)
        
        # Combine all passwords
        all_passwords = {**user_passwords, **appuser_passwords}
        
        # First handle pspadm_owner
        check_and_create_users(
            db_host=db_host,
            db_port=5432,
            db_name='psppdarc1',
            postgres='intuadmin',
            password_file_path=password_file_path,
            required_users=['pspadm_owner']
        )
        
        # Reset pspadm_owner password
        if 'pspadm_owner' in all_passwords:
            reset_password_with_psql(
                user='pspadm_owner',
                new_password=all_passwords['pspadm_owner'],
                db_host=db_host,
                db_name='psppdarc1'
            )
        else:
            print("⚠️ pspadm_owner password not found in password files")

        # 8. Run grants as pspadm_owner
        print("\n=== STEP 8: Running grants as pspadm_owner ===")
        execute_sql_from_file(
            db_host=db_host,
            db_port=5432,
            db_name='psppdarc1',
            postgres='pspadm_owner',
            password_file_path=password_file_path,
            sql_file_path=grants_sql_path
        )

        # 9. Create all users and reset passwords
        print("\n=== STEP 9: Creating all users and resetting passwords ===")
        # Get all unique users from both password files
        all_users = list(all_passwords.keys())
        
        # Create all users
        check_and_create_users(
            db_host=db_host,
            db_port=5432,
            db_name='psppdarc1',
            postgres='intuadmin',
            password_file_path=password_file_path,
            required_users=all_users
        )
        
        # Reset passwords for all users
        for user, password in all_passwords.items():
            print(f"\nResetting password for user: {user}")
            reset_password_with_psql(
                user=user,
                new_password=password,
                db_host=db_host,
                db_name='psppdarc1'
            )

        # 10. Run app user grants as postgres
        print("\n=== STEP 10: Running app user grants ===")
        execute_sql_from_file(
            db_host=db_host,
            db_port=5432,
            db_name='psppdarc1',
            postgres='intuadmin',
            password_file_path=password_file_path,
            sql_file_path=appuser_grants_sql_path
        )

        # 11. Add AWS backup tag
        print("\n=== STEP 11: Adding AWS backup tag ===")
        def get_account_id():
            sts = boto3.client("sts")
            return sts.get_caller_identity()["Account"]

        account_id = get_account_id()
        rds_client.add_tags_to_resource(
            ResourceName=f"arn:aws:rds:us-west-2:{account_id}:cluster:{new_db_cluster_identifier}",
            Tags=[{'Key': 'org_awsbackup_local_0700UTC', 'Value': '7continuous'}]
        )
        print("✅ Tag 'org_awsbackup_local_0700UTC=7continuous' applied successfully.")

        # 12. Run VACUUM script if exists
        print("\n=== STEP 12: Running VACUUM script ===")
        if os.path.exists(vacuum_script_path):
            print("🧹 Running VACUUM script for all tables...")
            subprocess.run(
                [vacuum_script_path],
                cwd=os.path.dirname(vacuum_script_path),
                check=True
            )
            print("✅ VACUUM completed successfully.")
        else:
            print(f"⚠️ Vacuum script not found at {vacuum_script_path}")

        # 13. Run ANALYZE script if exists
        print("\n=== STEP 13: Running ANALYZE script ===")
        if os.path.exists(analyze_script_path):
            print("📊 Running ANALYZE script for all tables...")
            subprocess.run(
                [analyze_script_path],
                cwd=os.path.dirname(analyze_script_path),
                check=True
            )
            print("✅ ANALYZE completed successfully.")
        else:
            print(f"⚠️ Analyze script not found at {analyze_script_path}")

        print("\n=== DATABASE REFRESH COMPLETED SUCCESSFULLY ===")

    except Exception as e:
        print(f"\n❌ Error during process: {e}")
        raise

if __name__ == "__main__":
    main()
