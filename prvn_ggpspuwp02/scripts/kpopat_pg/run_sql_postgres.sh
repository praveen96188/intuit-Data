. /l/orcl

stty -echo
echo "Please enter the database master user intuadmin password:"
read postgres_password
stty echo

echo $postgres_password > .p
function run_sql
{
  export PGPASSWORD=`cat .p`
  rm -f .p
  psql -h ppsp-prod-pituwt90.cluster-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d ${2} -U postgres -p 5432 <<EOF
    \echo 
    \timing
    \conninfo
    \set dbname $2
    \set schema $3
    \i $1
    \q 
EOF
}
export -f run_sql
nohup bash -c "run_sql $1 $2 $3" &

