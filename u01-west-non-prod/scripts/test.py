import psycopg2

# Establish connection to the database
conn = psycopg2.connect(
    host="ppsp-sys-mon.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com",
    database="drcsdb01",
    user="postgres",
    password="changeme"
)

# Open a cursor to perform database operations
cur = conn.cursor()

# Open and read the script file
with open('script.sql', 'r') as f:
    sql = f.read()

# Execute the SQL script
cur.execute(sql)

# Fetch the results
results = cur.fetchall()

# Print the results
for row in results:
    print(row)

# Commit the changes to the database
conn.commit()

# Close the database connection
cur.close()
conn.close()

print("SQL script executed successfully!")

