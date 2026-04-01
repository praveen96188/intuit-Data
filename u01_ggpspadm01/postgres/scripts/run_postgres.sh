export PGPASSWORD=`cat .pp|grep ${1}|awk '{print $2}'`
if [[ ${1} =~ "arc" ]]; then
    psql -U intuadmin -h ${1}.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 -v dbname="'$4'"
else
    psql -U postgres -h ${1}.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 -v dbname="'$4'"
fi
#psql_cmd="psql --username=postgres -h ${1}.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 $4"
#eval "$psql_cmd"

