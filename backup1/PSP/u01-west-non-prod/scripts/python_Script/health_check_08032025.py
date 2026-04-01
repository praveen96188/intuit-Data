import psycopg2
from psycopg2 import OperationalError
import sys
import argparse

# Function to connect to the database
def connect_to_db(host, dbname, user, password):
    try:
        connection = psycopg2.connect(
            host=host,
            database=dbname,
            user=user,
            password=password,
            port=5432  # default PostgreSQL port
        )
        print("Successfully connected to the database.")
        return connection
    except OperationalError as e:
        print("Error: Unable to connect to the database: {}".format(e))
        sys.exit(1)

# Function to check active connections
def check_active_connections(connection):
    cursor = connection.cursor()
    cursor.execute("SELECT state, COUNT(*) FROM pg_stat_activity GROUP BY state;")
    connection_info = cursor.fetchall()
    
    # Print each connection state with its count
    for state, count in connection_info:
        print("Connection state: {}, Count: {}".format(state, count))
    cursor.close()

# Function to check long-running queries
def check_long_running_queries(connection):
    cursor = connection.cursor()
    cursor.execute("""
        SELECT pid, now() - pg_stat_activity.query_start AS duration, query
        FROM pg_stat_activity
        WHERE state = 'active' AND now() - pg_stat_activity.query_start > interval '5 minutes';
    """)
    long_queries = cursor.fetchall()
    if long_queries:
        print("Long-running queries:")
        for query in long_queries:
            print("PID: {}, Duration: {}, Query: {}".format(query[0], query[1], query[2]))
    else:
        print("No long-running queries found.")
    cursor.close()

# Function to check database size
def check_database_size(connection):
    cursor = connection.cursor()
    cursor.execute("SELECT pg_size_pretty(pg_database_size(current_database())) AS db_size;")
    db_size = cursor.fetchone()[0]
    print("Database Size: {}".format(db_size))
    cursor.close()

# Function to check table bloat
def check_table_bloat(connection):
    cursor = connection.cursor()
    cursor.execute("""
        SELECT relname, pg_size_pretty(pg_total_relation_size(relid)) AS total_size
        FROM pg_stat_user_tables
        ORDER BY pg_total_relation_size(relid) DESC
        LIMIT 5;
    """)
    bloat_tables = cursor.fetchall()
    if bloat_tables:
        print("Top 5 largest tables:")
        for table in bloat_tables:
            print("Table: {}, Size: {}".format(table[0], table[1]))
    cursor.close()

# Function to check for replication (if applicable)
def check_replication_status(connection):
    cursor = connection.cursor()
    cursor.execute("""
        SELECT application_name, state, sync_state, replay_lag
        FROM pg_stat_replication;
    """)
    replication_status = cursor.fetchall()
    if replication_status:
        print("Replication Status:")
        for status in replication_status:
            print("Application Name: {}, State: {}, Sync State: {}, Replay Lag: {}".format(
                status[0], status[1], status[2], status[3]
            ))
    else:
        print("No replication found.")
    cursor.close()

# Function to read SQL queries from a file
def read_sql_from_file(file_path):
    try:
        with open(file_path, 'r') as file:
            sql = file.read()
        return sql
    except Exception as e:
        print("Error reading SQL from file: {}".format(e))
        sys.exit(1)

# Function to execute the SQL query
def execute_query(connection, sql_query):
    cursor = connection.cursor()
    try:
        cursor.execute(sql_query)
        result = cursor.fetchall()
        return result
    except Exception as e:
        print("Error executing query: {}".format(e))
        sys.exit(1)
    finally:
        cursor.close()

# Function to perform the health check
def health_check(host, dbname, user, password):
    connection = connect_to_db(host, dbname, user, password)
    
    # Perform various health checks
    check_active_connections(connection)
    check_long_running_queries(connection)
    check_database_size(connection)
    check_table_bloat(connection)
    check_replication_status(connection)

    # Execute queries from SQL files
    active_connections_sql = read_sql_from_file("check_active_connections.sql")
    active_connections = execute_query(connection, active_connections_sql)
    print("Active Connections Query Results: {}".format(active_connections))

    # Close the connection
    connection.close()
    print("Health check completed.")

# Function to parse command-line arguments
def parse_arguments():
    parser = argparse.ArgumentParser(description="PostgreSQL Health Check for AWS RDS")
    parser.add_argument('--host', required=True, help='The PostgreSQL endpoint (RDS endpoint)')
    parser.add_argument('--dbname', required=True, help='The name of the database')
    parser.add_argument('--user', required=True, help='The PostgreSQL username')
    parser.add_argument('--password-file', required=True, help='Path to the file containing the PostgreSQL password')
    return parser.parse_args()

# Function to read password from file
def read_password_from_file(password_file):
    try:
        with open(password_file, 'r') as f:
            password = f.read().strip()  # Remove any leading/trailing whitespaces or newlines
            return password
    except Exception as e:
        print("Error reading password from file: {}".format(e))
        sys.exit(1)

if __name__ == "__main__":
    args = parse_arguments()
    password = read_password_from_file(args.password_file)
    health_check(args.host, args.dbname, args.user, password)

