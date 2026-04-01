import psycopg2
engine = psycopg2.connect(
    database="psyspg01",
    user="postgres",
    password="changeme",
    host="ppsp-sys-mon.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com",
    port='5432'
)
