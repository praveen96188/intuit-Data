export PGPASSWORD="pspval#123"
psql -h ppsp-stg-pspval.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com  -U pspval -p 5432 -d pspval -f $1 -a -b -e
