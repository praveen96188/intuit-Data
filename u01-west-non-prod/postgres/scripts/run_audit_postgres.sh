export PGPASSWORD=`cat .pp|grep ${1}|awk '{print $2}'`
psql -U postgres -h ${1}.cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 -v dbname="'$4'"
#psql_cmd="psql --username=postgres -h ${1}.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 $4"
#eval "$psql_cmd"
