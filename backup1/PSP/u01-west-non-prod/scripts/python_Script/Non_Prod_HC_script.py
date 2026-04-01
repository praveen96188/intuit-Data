import psycopg2
from psycopg2 import OperationalError
import sys
import argparse
from datetime import timedelta, datetime
import os

def connect_to_db(host, dbname, user, password):
    try:
        connection = psycopg2.connect(
            host=host,
            database=dbname,
            user=user,
            password=password,
            port=5432
        )
        return connection
    except OperationalError as e:
        print(f"Error: Unable to connect to the database: {e}")
        sys.exit(1)

def read_sql_from_file(file_path):
    try:
        with open(file_path, 'r') as file:
            return file.read()
    except Exception as e:
        print(f"Error reading SQL from file {file_path}: {e}")
        sys.exit(1)

def execute_query(connection, sql_query):
    cursor = connection.cursor()
    try:
        cursor.execute(sql_query)
        return cursor.fetchall()
    except Exception as e:
        print(f"Error executing query: {e}")
        sys.exit(1)
    finally:
        cursor.close()

def write_table(file_html, file_txt, headers, rows):
    file_html.write("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>\n")
    file_html.write("<tr style='background-color: #f2f2f2;'>\n")
    for header in headers:
        file_html.write(f"<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>{header}</th>\n")
    file_html.write("</tr>\n")

    txt_header = " | ".join(headers)
    file_txt.write(f"{txt_header}\n")
    file_txt.write(f"{'-' * len(txt_header)}\n")

    for row in rows:
        file_html.write("<tr>\n")
        for column in row:
            file_html.write(f"<td style='border: 1px solid #ddd; padding: 8px; text-align: left;'>{column}</td>\n")
        file_html.write("</tr>\n")
        file_txt.write(" | ".join(str(col) for col in row) + "\n")

    file_html.write("</table>\n")
    file_txt.write("\n")

def check_total_connections(conn, file_html, file_txt):
    file_html.write("<h2>Total Connections</h2>\n")
    file_txt.write("## Total Connections\n\n")
    query = read_sql_from_file("check_total_connections.sql")
    data = execute_query(conn, query)
    headers = ["Username", "Machine Name", "Application Name", "Total Connections Count"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No data found.</p>\n")
        file_txt.write("No data found.\n\n")

def check_active_connections(conn, file_html, file_txt):
    file_html.write("<h2>Active Sessions</h2>\n")
    file_txt.write("## Active Sessions\n\n")
    query = read_sql_from_file("check_active_connections.sql")
    data = execute_query(conn, query)
    headers = ["Username", "Machine Name", "Application Name", "Active Sessions Count"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No active sessions found.</p>\n")
        file_txt.write("No active sessions found.\n\n")

def check_long_running_queries(conn, file_html, file_txt):
    file_html.write("<h2>Long-Running Queries</h2>\n")
    file_txt.write("## Long-Running Queries\n\n")
    query = read_sql_from_file("check_long_running_queries.sql")
    data = execute_query(conn, query)
    headers = ["PID", "Duration", "Query"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No long-running queries found.</p>\n")
        file_txt.write("No long-running queries found.\n\n")

def check_database_size(conn, file_html, file_txt):
    file_html.write("<h2>Database Size</h2>\n")
    file_txt.write("## Database Size\n\n")
    query = read_sql_from_file("check_database_size.sql")
    data = execute_query(conn, query)
    file_html.write(f"<p>Database Size: {data[0][0]}</p>\n")
    file_txt.write(f"Database Size: {data[0][0]}\n\n")

def check_table_sizes(conn, file_html, file_txt):
    file_html.write("<h2>Top 10 Largest Tables (Total Size)</h2>\n")
    file_txt.write("## Top 10 Largest Tables (Total Size)\n\n")
    query = read_sql_from_file("check_table_sizes.sql")
    data = execute_query(conn, query)
    headers = ["Table Name", "Total Size", "Data Size", "Index Size"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No table size data available.</p>\n")
        file_txt.write("No table size data available.\n\n")

def check_unused_indexes(conn, file_html, file_txt):
    file_html.write("<h2>Unused and Invalid Indexes</h2>\n")
    file_txt.write("## Unused and Invalid Indexes\n\n")
    query = read_sql_from_file("check_unused_indexes.sql")
    data = execute_query(conn, query)
    headers = ["Table", "Index", "Status", "Validity"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No unused or invalid indexes found.</p>\n")
        file_txt.write("No unused or invalid indexes found.\n\n")

def check_table_bloat(conn, file_html, file_txt):
    file_html.write("<h2>Top 10 Tables by Bloat</h2>\n")
    file_txt.write("## Top 10 Tables by Bloat\n\n")
    query = read_sql_from_file("check_table_bloat.sql")
    data = execute_query(conn, query)
    headers = ["Table Name", "Total Size (GB)", "Bloat Size (GB)", "Bloat Percentage"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No bloated tables found.</p>\n")
        file_txt.write("No bloated tables found.\n\n")

def check_replication_status(conn, file_html, file_txt):
    file_html.write("<h2>Replication Status</h2>\n")
    file_txt.write("## Replication Status\n\n")
    query = read_sql_from_file("check_replication_status.sql")
    data = execute_query(conn, query)
    headers = ["Slot Name", "Plugin", "Slot Type", "Active", "Restart LSN", "Confirmed Flush LSN", "Total Size"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No replication data found.</p>\n")
        file_txt.write("No replication data found.\n\n")

def check_dead_tuples(conn, file_html, file_txt):
    file_html.write("<h2>Tables with Dead Tuples > 500k</h2>\n")
    file_txt.write("## Tables with Dead Tuples > 500k\n\n")
    query = read_sql_from_file("check_dead_tuples.sql")
    data = execute_query(conn, query)
    headers = ["Table", "Dead Tuples"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No dead tuples above threshold found.</p>\n")
        file_txt.write("No dead tuples above threshold found.\n\n")

def check_vacuum_progress(conn, file_html, file_txt):
    file_html.write("<h2>Vacuum Progress</h2>\n")
    file_txt.write("## Vacuum Progress\n\n")
    query = read_sql_from_file("check_vacuum_progress.sql")
    data = execute_query(conn, query)
    headers = ["Table OID", "Phase", "Heap Blocks Total", "Heap Blocks Scanned", "Heap Blocks Vacuumed"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>No vacuum operations in progress.</p>\n")
        file_txt.write("No vacuum operations in progress.\n\n")

def check_autovacuum_status(conn, file_html, file_txt):
    file_html.write("<h2>Tables Not Vacuumed in Last 15 Days</h2>\n")
    file_txt.write("## Tables Not Vacuumed in Last 15 Days\n\n")
    query = read_sql_from_file("check_autovacuum_status.sql")
    data = execute_query(conn, query)
    headers = ["Table", "Table Size", "Live Tuples", "Dead Tuples", "Last Vacuum",
               "Days Since Last Vacuum", "Days Since Last Autovacuum", "Table Size (GB)"]
    if data:
        write_table(file_html, file_txt, headers, data)
    else:
        file_html.write("<p>All tables have been vacuumed recently.</p>\n")
        file_txt.write("All tables have been vacuumed recently.\n\n")

def health_check(host, dbname, user, password):
    conn = connect_to_db(host, dbname, user, password)
    current_date = datetime.now().strftime('%Y%m%d')
    html_report = f"Healthcheck_{dbname}_{current_date}.html"
    txt_report = f"Healthcheck_{dbname}_{current_date}.txt"

    with open(html_report, 'w') as file_html, open(txt_report, 'w') as file_txt:
        file_html.write("<html><head><title>Healthcheck</title><style>body { font-family: Arial; }</style></head><body>\n")
        file_html.write(f"<h1>Healthcheck for {dbname}</h1>\n")
        file_html.write(f"<p>Generated at {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>\n")

        file_txt.write(f"Healthcheck for {dbname}\n")
        file_txt.write(f"Generated at {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        file_txt.write("=" * 80 + "\n\n")

        check_total_connections(conn, file_html, file_txt)
        check_active_connections(conn, file_html, file_txt)
        check_long_running_queries(conn, file_html, file_txt)
        check_database_size(conn, file_html, file_txt)
        check_table_sizes(conn, file_html, file_txt)
        check_unused_indexes(conn, file_html, file_txt)
        check_table_bloat(conn, file_html, file_txt)
        check_replication_status(conn, file_html, file_txt)
        check_dead_tuples(conn, file_html, file_txt)
        check_vacuum_progress(conn, file_html, file_txt)
        check_autovacuum_status(conn, file_html, file_txt)

        file_html.write("</body></html>\n")

    conn.close()
    print(f"Health check completed. Reports saved as {html_report} and {txt_report}")

def parse_arguments():
    parser = argparse.ArgumentParser(description="PostgreSQL Health Check for AWS RDS")
    parser.add_argument('--host', required=True)
    parser.add_argument('--dbname', required=True)
    parser.add_argument('--user', required=True)
    parser.add_argument('--password-file', required=True)
    return parser.parse_args()

def read_password_from_file(password_file, target_host):
    try:
        with open(password_file, 'r') as f:
            for line in f:
                # Remove extra whitespace and skip empty lines
                line = line.strip()
                if not line:
                    continue

                # Split line into [host, password]
                parts = line.split(None, 1) # Splits at first whitespace
                if len(parts) == 2:
                    file_host, file_pass = parts
                    if file_host == target_host:
                        return file_pass

            print("Error: Host '{0}' not found in password file.".format(target_host))
            sys.exit(1)
    except Exception as e:
        print("Error reading password file: {0}".format(e))
        sys.exit(1)


if __name__ == "__main__":
    args = parse_arguments()
    # Pass both the file path AND the host you are looking for
    password = read_password_from_file(args.password_file, args.host)
    health_check(args.host, args.dbname, args.user, password)


