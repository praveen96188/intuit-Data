export PGPASSWORD="WpicaRyz^og-GKz1"
psql -h psp-prod-mon.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -U postgres -p 5432 -d psppp01 -a -b -e -f $1
