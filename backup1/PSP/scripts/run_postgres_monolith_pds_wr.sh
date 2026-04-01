export PGPASSWORD=`cat .cp1`
psql_cmd="psql --username=postgres -h ${1}.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f $3 $4"
eval "$psql_cmd"

