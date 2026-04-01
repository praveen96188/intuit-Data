export PGPASSWORD="ppp3zu#JA7M5aa"
psql -h  ppsp-stg-pitparmo-new-cluster.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -U pspadm_owner -p 5432 -d pitparmo -f $1 -a -b -e

