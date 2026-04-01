export PGPASSWORD="changeme"
psql -h psp-prod-uw02.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d pspapg02 -U mchoubey -p 5432 --echo-all -P pager=off -f $1
