#psql -h ppsp-prod-pituwt90.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d pituwt90 -U postgres -p 6543 -f $1 
psql -h ppsp-stg-pitparmo.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d pitparmo -U postgres -p 6543 -f $1
