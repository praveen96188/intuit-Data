export PGPASSWORD="PIgRgK7d#(2XZ"
psql -h ppsp-stg-pitparmo-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -U postgres -p 5432 -d pitparmo -f $1 -a -b -e
