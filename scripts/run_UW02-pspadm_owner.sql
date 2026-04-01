export PGPASSWORD="p3zu#JA7M5aa"
psql -h psp-prod-uw02.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -U pspadm_owner -p 5432 -d pspapg02 -f $1 -a -b -e
