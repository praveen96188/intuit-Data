export PGPASSWORD="changeme"
psql -h ppsp-sys-mon.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -U postgres -p 5432 -d psyspg01 -f $1
