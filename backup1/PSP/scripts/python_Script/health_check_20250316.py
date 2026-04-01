import psycopg2
from psycopg2 import OperationalError
import sys
import argparse
from datetime import timedelta, datetime
import os

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
        return connection
    except OperationalError as e:
        print("Error: Unable to connect to the database: {}".format(e))
        sys.exit(1)

# Function to read SQL query from a file
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

# Function to check active connections
def check_active_connections(connection, file):
    file.write("\n--- Active Connections ---\n")
    sql_query = read_sql_from_file("check_active_connections.sql")  # SQL file for active connections
    active_connections = execute_query(connection, sql_query)
    for row in active_connections:
        file.write("Connection state: {}, Count: {}\n".format(row[0], row[1]))

# Function to check long-running queries
def check_long_running_queries(connection, file):
    file.write("\n--- Long-Running Queries ---\n")
    sql_query = read_sql_from_file("check_long_running_queries.sql")  # SQL file for long-running queries
    long_queries = execute_query(connection, sql_query)
    if long_queries:
        for query in long_queries:
            file.write("PID: {}, Duration: {}, Query: {}\n".format(query[0], query[1], query[2]))
    else:
        file.write("No long-running queries found.\n")

# Function to check database size
def check_database_size(connection, file):
    file.write("\n--- Database Size ---\n")
    sql_query = read_sql_from_file("check_database_size.sql")  # SQL file for database size
    db_size = execute_query(connection, sql_query)
    file.write("Database Size: {}\n".format(db_size[0][0]))
    
# Function to check table sizes (Total, Data, Index)
def check_table_sizes(connection, file):
    file.write("\n--- Top 10 Largest Tables (By Total Size) ---\n")
    sql_query = read_sql_from_file("check_table_sizes.sql")  # SQL file for table sizes
    table_sizes = execute_query(connection, sql_query)
    for table in table_sizes:
        file.write("Table: {}, Total Size: {}, Data Size: {}, Index Size: {}\n".format(
            table[0], table[1], table[2], table[3]
        ))

# Function to check for unused indexes
def check_unused_indexes(connection, file):
    file.write("\n--- Unused Indexes ---\n")
    sql_query = """
    SELECT 
        i.indexrelid::regclass AS index_name,
        t.relname AS table_name
    FROM pg_index i
    JOIN pg_class t ON t.oid = i.indrelid
    LEFT JOIN pg_stat_user_indexes s ON s.indexrelid = i.indexrelid
    WHERE s.idx_scan = 0  -- Index has never been scanned
    AND t.relkind = 'r';  -- Only for tables, not views
    """
    
    unused_indexes = execute_query(connection, sql_query)
    
    if unused_indexes:
        # Print header
        file.write("{:<40} {:<40}\n".format("Table", "Index"))
        file.write("-" * 80 + "\n")  # Separator
        
        # Print data in tabular format
        for index in unused_indexes:
            table_name = index[1] if index[1] is not None else 'N/A'  # Table name
            index_name = index[0] if index[0] is not None else 'N/A'  # Index name
            
            file.write("{:<40} {:<40}\n".format(table_name, index_name))
    else:
        file.write("No unused indexes found.\n")

# Function to check table bloat
def check_table_bloat(connection, file):
    file.write("\n--- Top 5 Largest Tables ---\n")
    sql_query = read_sql_from_file("check_table_bloat.sql")  # SQL file for table bloat
    bloat_tables = execute_query(connection, sql_query)
    for table in bloat_tables:
        file.write("Table: {}, Size: {}\n".format(table[0], table[1]))

# Function to check replication status and lag
def check_replication_status(connection, file):
    file.write("\n--- Replication Status and Lag ---\n")
    sql_query = read_sql_from_file("check_replication_status.sql")  # SQL file for replication status
    replication_status = execute_query(connection, sql_query)
    
    if replication_status:
        for status in replication_status:
            file.write("Application Name: {}, State: {}, Sync State: {}, Replay Lag: {}, "
                        "Slot Name: {}, Active: {}, Current WAL LSN: {}\n".format(
                            status[0], status[1], status[2], status[3], status[4], 
                            status[5], status[6]))
    else:
        file.write("No replication or replication slots found.\n")

# Function to check tables with dead tuples > 500k
def check_dead_tuples(connection, file):
    file.write("\n--- Tables with Dead Tuples > 500k ---\n")
    sql_query = read_sql_from_file("check_dead_tuples.sql")  # SQL file for dead tuples check
    dead_tuples = execute_query(connection, sql_query)
    if dead_tuples:
        for table in dead_tuples:
            file.write("Table: {}, Dead Tuples: {}\n".format(table[0], table[1]))
    else:
        file.write("No tables with dead tuples greater than 500k.\n")

# Function to check vacuum progress
def check_vacuum_progress(connection, file):
    file.write("\n--- Vacuum Progress ---\n")
    sql_query = read_sql_from_file("check_vacuum_progress.sql")  # SQL file for vacuum progress
    vacuum_progress = execute_query(connection, sql_query)
    
    if vacuum_progress:
        for progress in vacuum_progress:
            file.write("Table OID: {}, Phase: {}, Heap Blocks Total: {}, Heap Blocks Scanned: {}, "
                        "Heap Blocks Vacuumed: {}\n".format(progress[0], progress[1], progress[2], progress[3], progress[4]))
    else:
        file.write("No vacuum operations in progress.\n")

# Function to check tables not vacuumed in the last 15 days
def check_autovacuum_status(connection, file):
    file.write("\n--- Tables Not Vacuumed in the Last 15 Days ---\n")
    sql_query = read_sql_from_file("check_autovacuum_status.sql")  # SQL file for autovacuum status
    tables_not_vacuumed = execute_query(connection, sql_query)

    if tables_not_vacuumed:
        # Write the header with the additional columns: Table Size, Live Tuples, Dead Tuples
        file.write("{:<30} {:<15} {:<15} {:<15} {:<20} {:<20} {:<20} {:<20}\n".format(
            "Table", "Table Size", "Live Tuples", "Dead Tuples", "Last Vacuum", 
            "Days Since Last Vacuum", "Days Since Last Autovacuum", "Table Size (GB)"
        ))

        # Iterate through the results and print them in the correct format
        for table in tables_not_vacuumed:
            # Handle None values and convert timedelta objects to string
            table_name = table[0] if table[0] is not None else 'N/A'

            # Convert datetime.timedelta objects to total number of days (if applicable)
            last_vacuum = table[4] if table[4] is not None else 'N/A'
            last_autovacuum = table[5] if table[5] is not None else 'N/A'

            # For "Days Since Last Vacuum" and "Days Since Last Autovacuum", convert timedelta to total days
            days_since_last_vacuum = table[6].days if isinstance(table[6], timedelta) else table[6] if table[6] is not None else 'N/A'
            days_since_last_autovacuum = table[7].days if isinstance(table[7], timedelta) else table[7] if table[7] is not None else 'N/A'
            
            # Add new columns for table size, live tuples, and dead tuples
            table_size = table[1] if table[1] is not None else 'N/A'
            live_tuples = table[2] if table[2] is not None else 'N/A'
            dead_tuples = table[3] if table[3] is not None else 'N/A'

            # Write data with correct alignment, adding the new columns as well
            file.write("{:<30} {:<15} {:<15} {:<15} {:<20} {:<20} {:<20} {:<20}\n".format(
                table_name, table_size, live_tuples, dead_tuples, last_vacuum,
                days_since_last_vacuum, days_since_last_autovacuum, table_size
            ))
    else:
        file.write("All tables have been vacuumed recently.\n")

# Function to perform the health check
def health_check(host, dbname, user, password):
    connection = connect_to_db(host, dbname, user, password)
    
    # Generate the output filename in the format: Healthcheck_dbname_currentdate.html
    current_date = datetime.now().strftime('%Y%m%d')
    output_file = f"Healthcheck_{dbname}_{current_date}.html"  # Combine them to form the output filename
    
    with open(output_file, 'w') as file:
        # Write the basic HTML structure
        file.write("<html>\n")
        file.write("<head>\n")
        file.write(f"<title>Healthcheck for {dbname}</title>\n")
        file.write("<style>\n")
        file.write("body { font-family: Arial, sans-serif; margin: 20px; }\n")
        file.write("h1 { text-align: center; }\n")  # Center the heading
        file.write("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
        file.write("th, td { border: 1px solid #dddddd; padding: 8px; text-align: left; }\n")
        file.write("th { background-color: #f2f2f2; }\n")
        file.write("</style>\n")
        file.write("</head>\n")
        file.write("<body>\n")
        
        # Add the centered heading
        file.write(f"<h1>Healthcheck for {dbname}</h1>\n")
        
        # Perform various health checks
        file.write("<h2>Active Connections</h2>\n")
        check_active_connections(connection, file)
        
        file.write("<h2>Long-Running Queries</h2>\n")
        check_long_running_queries(connection, file)
        
        file.write("<h2>Database Size</h2>\n")
        check_database_size(connection, file)
        
        file.write("<h2>Top 10 Largest Tables</h2>\n")
        check_table_sizes(connection, file)
        
        file.write("<h2>Unused Indexes</h2>\n")
        check_unused_indexes(connection, file)
        
        file.write("<h2>Table Bloat</h2>\n")
        check_table_bloat(connection, file)
        
        file.write("<h2>Replication Status and Lag</h2>\n")
        check_replication_status(connection, file)
        
        file.write("<h2>Dead Tuples</h2>\n")
        check_dead_tuples(connection, file)
        
        file.write("<h2>Vacuum Progress</h2>\n")
        check_vacuum_progress(connection, file)
        
        file.write("<h2>Autovacuum Status</h2>\n")
        check_autovacuum_status(connection, file)
        
        # End the HTML document
        file.write("</body>\n")
        file.write("</html>\n")
    
    # Close the connection
    connection.close()
    print(f"\nHealth check completed. Results saved in {output_file}.")

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

# Main script entry point
if __name__ == "__main__":
    args = parse_arguments()
    password = read_password_from_file(args.password_file)
    health_check(args.host, args.dbname, args.user, password)
