#psql -h ppsp-prod-pituwt90.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d pituwt90 -U postgres -p 6543 -f $1 
psql -h psp-prod-ibob.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d prodapgib -U postgres -p 5432 -f $1
