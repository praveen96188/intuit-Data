export PGPASSWORD="changeme"
psql -h ppsp-stg-pitparmo.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d pitparmo -U mchoubey -p 5432 --echo-all -P pager=off -f $1
