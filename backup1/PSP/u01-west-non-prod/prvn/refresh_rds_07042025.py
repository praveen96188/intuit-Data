import boto3
import subprocess
import psycopg2
from psycopg2 import OperationalError
from datetime import datetime, timezone
import time
import shlex

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

        # Continue with the rest of the process (check active connections, rename DB, etc.)
        return new_db_cluster_identifier

    except Exception as e:
        print(f"Error restoring staging DB cluster: {e}")
        raise

# Function to check active connections in the DB and kill them if needed
def check_active_connections(db_host, db_port, db_name, postgres, password_file_path):
    user_passwords = read_passwords_from_file(password_file_path)  # Read passwords from the file
    password = user_passwords.get(postgres, None)  # Get password for the user from the file

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
            connect_timeout=120  # Increased timeout (in seconds)
        )

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
                # Kill the connection using pg_terminate_backend
                cursor.execute(f"SELECT pg_terminate_backend({pid});")
                print(f"Connection with pid {pid} has been terminated.")
            
            # Commit the changes (terminating connections)
            connection.commit()
            
            # You can also raise an exception here to stop the process if you need
            # raise Exception("Active connections were terminated, proceeding with the operation.")
        else:
            print("No active connections found.")

        cursor.close()
        connection.close()
    except OperationalError as e:
        print(f"Error connecting to the database: {e}")
        print(f"Password for postgres: {password}")
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
            
            connection.commit()

        cursor.close()
        connection.close()

    except Exception as e:
        print(f"❌ Error while checking/dropping replication slots: {e}")
        if 'connection' in locals():
            connection.close()

# Function to rename the DB
def rename_db(db_host, db_port, old_db_name, new_db_name, postgres, password_file_path):
    user_passwords = read_passwords_from_file(password_file_path)  # Read passwords from the file
    password = user_passwords.get(postgres, None)  # Get password for the user from the file

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
            connect_timeout=120  # Increased timeout (in seconds)
        )
        cursor = connection.cursor()

        cursor.execute(f"""
            SELECT pid, usename, application_name, client_addr
            FROM pg_stat_activity
            WHERE usename NOT IN ('postgres', 'rdsadmin') AND datname = '{old_db_name}'
        """)
        active_connections = cursor.fetchall()

        if active_connections:
            print(f"Active connections found to database {old_db_name}. Aborting renaming.")
            for conn in active_connections:
                print(conn)
            return

        cursor.execute(f"ALTER DATABASE {old_db_name} RENAME TO {new_db_name};")
        connection.commit()
        print(f"Successfully renamed database from {old_db_name} to {new_db_name}.")
        cursor.close()
        connection.close()

    except OperationalError as e:
        print(f"Error connecting to the database: {e}")
        if 'connection' in locals():
            connection.close()


# Function to check and create users if needed, and reset passwords for existing users
def check_and_create_users(db_host, db_port, db_name, postgres, password_file_path, required_users):
    user_passwords = read_passwords_from_file(password_file_path)  # Read passwords from the file
    password = user_passwords.get(postgres, None)  # Get password for the user from the file

    if not password:
        print(f"Password for user '{postgres}' not found in the password file.")
        return

    try:
        # Connect to the database
        connection = psycopg2.connect(
            host=db_host,
            port=db_port,
            dbname=db_name,
            user=postgres,
            password=password,
            connect_timeout=120  # Increased timeout (in seconds)
        )
        cursor = connection.cursor()

        # Check for required users
        for user in required_users:
            cursor.execute(f"SELECT 1 FROM pg_roles WHERE rolname = '{user}';")
            if cursor.fetchone():
                print(f"User '{user}' already exists.")
                # Reset password using psql \password command
               # print(f"Resetting password for user '{user}' using \password command.")
                reset_password_with_psql(user, password)  # Pass db_host here
                print(f"Password for user '{user}' has been reset using \\password.")
            else:
                print(f"Creating user '{user}' with login")
                cursor.execute(f"CREATE USER {user} WITH LOGIN;")
                print(f"User '{user}' created.")
        
        connection.commit()
        cursor.close()
        connection.close()

    except OperationalError as e:
        print(f"Error connecting to the database: {e}")
        if 'connection' in locals():
            connection.close()

def read_passwords_from_file(filepath):
    user_passwords = {}
    with open(filepath, 'r') as file:
        for line in file:
            line = line.strip()
            if ':' in line:
                username, password = line.split(':', 1)
                user_passwords[username.strip()] = password.strip()
    return user_passwords

def reset_password_with_psql(user, password):
    # Read the new password from the file

    # Create the expect script as a string
    rds_client = boto3.client('rds', region_name='us-west-2')  # Initialize the RDS client
    new_db_cluster_identifier = 'ppsp-ppd-arc-new'
    db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']  # Replace with actual host
    # Escape the password safely
    raw_password = "cJ86WjLGoZheFG5NRo97gg.68lOu.--7"  # your actual hardcoded password
    escaped_password = shlex.quote(raw_password)
    expect_script = f"""
    log_user 1
    log_file /tmp/psql_password_change.log
    set timeout 10
  
    spawn env PGPASSWORD={escaped_password} psql -X -h {db_host} -d psppdarc1 -U intuadmin

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
                    send "{password}\\r"
                    expect "Enter it again:"
                    send "{password}\\r"
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
    # Run the expect script
    result = subprocess.run(['expect'], input=expect_script, text=True, capture_output=True)

    if result.returncode == 0:
        print(f"Password changed successfully for user: {user} {password}")
    else:
        print(f"Failed to change password for user: {user}")
        print(result.stderr)

    # Now verify the new password by attempting to connect using the new password
    #try:
   #     result = subprocess.run(
   #             ['psql', '-h', db_host, '-d', 'psppdarc1', '-U', user, '-W'],
   #         input=password, text=True, capture_output=True
   #     )
#
 #       if result.returncode == 0:
  #          print(f"Successfully connected to the database with the new password for user: {user}")
   #     else:
  #          print(f"Failed to connect to the database with the new password for user: {user}")
  #          print(result.stderr)

 #   except Exception as e:
  #      print(f"Error occurred while verifying new password: {e}")
# ---------- Main Flow ----------
#password_file_path = '/u01/scripts/stage_db_refreh/db_password.txt'
#db_host =rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']  # Replace with actual host

#user_passwords = read_passwords_from_file(password_file_path)

#for user, password in user_passwords.items():
 #   change_password_with_expect(user, password, db_host)

# Function to execute SQL from file
def execute_sql_from_file(db_host, db_port, db_name, postgres, password_file_path, sql_file_path, placeholders=None):
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

        with open(sql_file_path, 'r') as sql_file:
            sql_content = sql_file.read()

            # Apply placeholder substitutions
            if placeholders:
                for key, value in placeholders.items():
                    sql_content = sql_content.replace(key, value)

            # Split queries by semicolon if needed
            queries = [q.strip() for q in sql_content.split(';') if q.strip()]
            for query in queries:
                try:
                    cursor.execute(query)
                    print(f"Executed SQL: {query}")
                except Exception as e:
                    print(f"Error executing query: {query}. Error: {e}")

        connection.commit()
        cursor.close()
        connection.close()

    except OperationalError as e:
        print(f"Error connecting to the database: {e}")
        if 'connection' in locals():
            connection.close()

# Main function to orchestrate the process
def main():
    rds_client = boto3.client('rds', region_name='us-west-2')  # Initialize the RDS client

    prod_cluster_identifier = 'ppsp-ppd-arc'  # Production cluster identifier
    stage_cluster_identifier = 'ppsp-ppd-arc'  # Staging cluster identifie
    new_db_cluster_identifier = 'ppsp-ppd-arc-new'
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

        # Check for active connections to the new staging DB
        check_active_connections(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            db_name='psppdarc',  # The database we want to rename
            postgres='intuadmin',  # The username to connect
            password_file_path=password_file_path
        )


        # Drop replication slots if any
        drop_replication_slots_if_any(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            db_name='psppdarc',  # original DB name before rename
            postgres='intuadmin',
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

        # Check and create the required users and reset their passwords
        required_users = ['testusr','psparcapp']
        check_and_create_users(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            db_name='psppdarc1',  # The newly renamed database
            postgres='intuadmin',  # The username to connect
            password_file_path=password_file_path,
            required_users=required_users
        )

        # Password reset
        user_passwords = read_passwords_from_file(password_file_path)

        # Get the DB host (endpoint) of the new DB cluster
        db_host = rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint']

        # Loop through each user and reset their password
        for user, password in user_passwords.items():
             reset_password_with_psql(user, password)
   # Execute SQL commands from the file
        execute_sql_from_file(
            db_host=rds_client.describe_db_clusters(DBClusterIdentifier=new_db_cluster_identifier)['DBClusters'][0]['Endpoint'],
            db_port=5432,
            db_name='psppdarc1',  # The newly renamed database
            postgres='intuadmin',  # The username to connect
            password_file_path=password_file_path,
            sql_file_path=sql_file_path,
            placeholders={'$new_db_name': 'psppdarc1'}
        )

    # Add or update AWS tag for backup policy
    #    import boto3
        def get_account_id():
            sts = boto3.client("sts")
            return sts.get_caller_identity()["Account"]

        account_id = get_account_id()
        rds_client.add_tags_to_resource(
            ResourceName=f"arn:aws:rds:us-west-2:{account_id}:cluster:{new_db_cluster_identifier}",
            Tags=[{'Key': 'org_awsbackup_local_0700UTC', 'Value': '7continuous'}]
        )
        print("✅ Tag 'org_awsbackup_local_0700UTC=7continuous' applied successfully.")

        # ------------------------------
        # ✅ VACUUM and ANALYZE scripts
        # ------------------------------

        try:
            print("🧹 Running VACUUM script for all tables...")
            subprocess.run(
                ["./vacuum_pitparmo-new.sh"],
                cwd="/u01/refresh_scripts/postgres/vacuum/",
                check=True
            )
            print("✅ VACUUM completed successfully.")

            print("📊 Running ANALYZE script for all tables...")
            subprocess.run(
                ["./analyze_pitparmo-new.sh"],
                cwd="/u01/refresh_scripts/postgres/analyze/",
                check=True
            )
            print("✅ ANALYZE completed successfully.")

        except subprocess.CalledProcessError as e:
            print(f"❌ Error while running VACUUM or ANALYZE: {e}")

    except Exception as e:
        print(f"Error during process: {e}")

# Entry point of the script
if __name__ == "__main__":
    main()  # Execute the main function
