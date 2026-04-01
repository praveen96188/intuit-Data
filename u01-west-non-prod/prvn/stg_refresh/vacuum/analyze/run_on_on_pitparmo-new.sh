export PGPASSWORD="qrJi3wms3NlDZF31kjqL5VuZCUnZ_dE,"
psql -h ppsp-stg-pitparmo-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -U postgres -p 5432 -d pitparmo -f $1
