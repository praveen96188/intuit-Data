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
        print(f"Error: Unable to connect to the database: {e}")
        sys.exit(1)

# Function to read SQL query from a file
def read_sql_from_file(file_path):
    try:
        with open(file_path, 'r') as file:
            sql = file.read()
        return sql
    except Exception as e:
        print(f"Error reading SQL from file: {e}")
        sys.exit(1)

# Function to execute the SQL query
def execute_query(connection, sql_query):
    cursor = connection.cursor()
    try:
        cursor.execute(sql_query)
        result = cursor.fetchall()
        return result
    except Exception as e:
        print(f"Error executing query: {e}")
        sys.exit(1)
    finally:
        cursor.close()

# Function to write table in HTML format
def write_html_table(file, headers, rows):
    file.write("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>\n")
    file.write("<tr style='background-color: #f2f2f2;'>\n")
    for header in headers:
        file.write(f"<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>{header}</th>\n")
    file.write("</tr>\n")

    for row in rows:
        file.write("<tr>\n")
        for column in row:
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px; text-align: left;'>{column}</td>\n")
        file.write("</tr>\n")
    file.write("</table>\n")

# Function to check total database connections grouped by username, machine name, and application_name
def check_total_connections(connection, file):
    file.write("<h2>Total Database Connections Grouped by Username, Machine Name, and Application Name</h2>\n")
    sql_query = read_sql_from_file("check_total_connections.sql")  # SQL for total connections
    total_connections = execute_query(connection, sql_query)
    
    if total_connections:
        headers = ["Username", "Machine Name", "Application Name", "Total Connections Count"]
        write_html_table(file, headers, total_connections)
    else:
        file.write("<p>No database connections found.</p>\n")

# Function to check active sessions grouped by username, machine name, and application_name
def check_active_connections(connection, file):
    file.write("<h2>Total Active Sessions Grouped by Username, Machine Name, and Application Name</h2>\n")
    sql_query = read_sql_from_file("check_active_connections.sql")  # SQL for active sessions
    active_sessions = execute_query(connection, sql_query)
    
    if active_sessions:
        headers = ["Username", "Machine Name", "Application Name", "Active Sessions Count"]
        write_html_table(file, headers, active_sessions)
    else:
        file.write("<p>No active sessions found.</p>\n")

# Function to check long-running queries
def check_long_running_queries(connection, file):
    file.write("<h2>Long-Running Queries</h2>\n")
    sql_query = read_sql_from_file("check_long_running_queries.sql")  # SQL file for long-running queries
    long_queries = execute_query(connection, sql_query)
    if long_queries:
        headers = ["PID", "Duration", "Query"]
        write_html_table(file, headers, long_queries)
    else:
        file.write("<p>No long-running queries found.</p>\n")

# Function to check database size
def check_database_size(connection, file):
    file.write("<h2>Database Size</h2>\n")
    sql_query = read_sql_from_file("check_database_size.sql")  # SQL file for database size
    db_size = execute_query(connection, sql_query)
    file.write(f"<p>Database Size: {db_size[0][0]}</p>\n")
    
# Function to check table sizes (Total, Data, Index)
def check_table_sizes(connection, file):
    file.write("<h2>Top 10 Largest Tables (By Total Size)</h2>\n")
    sql_query = read_sql_from_file("check_table_sizes.sql")  # SQL file for table sizes
    table_sizes = execute_query(connection, sql_query)
    if table_sizes:
        headers = ["Table Name", "Total Size", "Data Size", "Index Size"]
        write_html_table(file, headers, table_sizes)
    else:
        file.write("<p>No data available for table sizes.</p>\n")

# Function to check for unused and invalid indexes
def check_unused_indexes(connection, file):
    file.write("<h2>Unused and Invalid Indexes</h2>\n")
    sql_query = read_sql_from_file("check_unused_indexes.sql")  # SQL for unused and invalid indexes
    unused_and_invalid_indexes = execute_query(connection, sql_query)
    
    if unused_and_invalid_indexes:
        headers = ["Table", "Index", "Status", "Validity"]
        write_html_table(file, headers, unused_and_invalid_indexes)
    else:
        file.write("<p>No unused or invalid indexes found.</p>\n")

# Function to check table bloat (Top 10 Largest Tables By Bloat)
def check_table_bloat(connection, file):
    file.write("<h2>Top 10 Largest Tables (By Bloat)</h2>\n")
    sql_query = read_sql_from_file("check_table_bloat.sql")  # SQL file for table bloat
    bloat_tables = execute_query(connection, sql_query)
    
    if bloat_tables:
        headers = ["Table Name", "Total Size (GB)", "Bloat Size (GB)", "Bloat Percentage"]
        write_html_table(file, headers, bloat_tables)
    else:
        file.write("<p>No bloat tables found.</p>\n")

# Function to check replication status and invalid slots
def check_replication_status(connection, file):
    file.write("<h2>Replication Status and Slots</h2>\n")
    sql_query = read_sql_from_file("check_replication_status.sql")  # SQL for replication slots
    replication_slots = execute_query(connection, sql_query)
    
    if replication_slots:
        headers = ["Slot Name", "Plugin", "Slot Type", "Active", "Restart LSN", "Confirmed Flush LSN", "Total Size"]
        write_html_table(file, headers, replication_slots)
    else:
        file.write("<p>No replication slots found.</p>\n")

# Function to check dead tuples
def check_dead_tuples(connection, file):
    file.write("<h2>Tables with Dead Tuples > 500k</h2>\n")
    sql_query = read_sql_from_file("check_dead_tuples.sql")  # SQL for dead tuples
    dead_tuples = execute_query(connection, sql_query)
    if dead_tuples:
        headers = ["Table", "Dead Tuples"]
        write_html_table(file, headers, dead_tuples)
    else:
        file.write("<p>No dead tuples greater than 500k found.</p>\n")

# Function to check vacuum progress
def check_vacuum_progress(connection, file):
    file.write("<h2>Vacuum Progress</h2>\n")
    sql_query = read_sql_from_file("check_vacuum_progress.sql")  # SQL for vacuum progress
    vacuum_progress = execute_query(connection, sql_query)
    if vacuum_progress:
        headers = ["Table OID", "Phase", "Heap Blocks Total", "Heap Blocks Scanned", "Heap Blocks Vacuumed"]
        write_html_table(file, headers, vacuum_progress)
    else:
        file.write("<p>No vacuum operations in progress.</p>\n")

# Function to check autovacuum status
def check_autovacuum_status(connection, file):
    file.write("<h2>Tables Not Vacuumed in the Last 15 Days</h2>\n")
    sql_query = read_sql_from_file("check_autovacuum_status.sql")  # SQL file for autovacuum status
    tables_not_vacuumed = execute_query(connection, sql_query)

    if tables_not_vacuumed:
        # Start the HTML table and add headers
        file.write("<table style='width: 100%; border-collapse: collapse;'>\n")
        file.write("<tr style='background-color: #f2f2f2;'>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Table</th>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Table Size</th>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Live Tuples</th>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Dead Tuples</th>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Last Vacuum</th>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Days Since Last Vacuum</th>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Days Since Last Autovacuum</th>\n")
        file.write("<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Table Size (GB)</th>\n")
        file.write("</tr>\n")

        # Iterate through the results and write them in HTML table rows
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
            live_tuples = table[2] if table[2] is not None else 0
            dead_tuples = table[3] if table[3] is not None else 0

            # Ensure there's no decimal point for live and dead tuples if they're integers
            live_tuples = int(live_tuples) if isinstance(live_tuples, float) and live_tuples.is_integer() else live_tuples
            dead_tuples = int(dead_tuples) if isinstance(dead_tuples, float) and dead_tuples.is_integer() else dead_tuples

            # Write data in HTML table rows
            file.write("<tr>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{table_name}</td>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{table_size}</td>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{live_tuples}</td>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{dead_tuples}</td>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{last_vacuum}</td>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{days_since_last_vacuum}</td>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{days_since_last_autovacuum}</td>\n")
            file.write(f"<td style='border: 1px solid #ddd; padding: 8px;'>{table_size}</td>\n")
            file.write("</tr>\n")

        file.write("</table>\n")  # Close the table
    else:
        file.write("<p>All tables have been vacuumed recently.</p>\n")

# Function to perform the health check
def health_check(host, dbname, user, password):
    connection = connect_to_db(host, dbname, user, password)
    
    # Generate the output filename in the format: Healthcheck_dbname_currentdate.html
    current_date = datetime.now().strftime('%Y%m%d')
    output_file = f"Healthcheck_{dbname}_{current_date}.html"
    
    with open(output_file, 'w') as file:
        # Write the basic HTML structure
        file.write("<html>\n")
        file.write("<head>\n")
        file.write(f"<title>Healthcheck for {dbname}</title>\n")
        file.write("<style>\n")
        file.write("body { font-family: Arial, sans-serif; margin: 20px; }\n")
        file.write("h1 { text-align: center; }\n")
        file.write("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
        file.write("th, td { border: 1px solid #dddddd; padding: 8px; text-align: left; }\n")
        file.write("th { background-color: #f2f2f2; }\n")
        file.write("</style>\n")
        file.write("</head>\n")
        file.write("<body>\n")
        
        # Add the centered heading
        file.write(f"<h1>Healthcheck for {dbname}</h1>\n")
        file.write(f"<p>Report generated at {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>\n")
        
        # Perform various health checks
        check_total_connections(connection, file)  # Add total connections check
        check_active_connections(connection, file)  # Ensure this function is defined
        check_long_running_queries(connection, file)
        check_database_size(connection, file)
        check_table_sizes(connection, file)
        check_unused_indexes(connection, file)
        check_table_bloat(connection, file)
        check_replication_status(connection, file)
        check_dead_tuples(connection, file)
        check_vacuum_progress(connection, file)
        check_autovacuum_status(connection, file)
        
        # End the HTML document
        file.write("</body>\n")
        file.write("</html>\n")
    
    # Close the connection
    connection.close()
    print(f"Health check completed. Results saved in {output_file}.")

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
        print(f"Error reading password from file: {e}")
        sys.exit(1)

# Main script entry point
if __name__ == "__main__":
    args = parse_arguments()
    password = read_password_from_file(args.password_file)
    health_check(args.host, args.dbname, args.user, password)
