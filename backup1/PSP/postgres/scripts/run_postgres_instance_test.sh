#export PGPASSWORD=`cat .pp|grep ${1::-1}|awk '{print $2}'`
export PGPASSWORD=`cat .pp|grep ${1::-1}|awk '{print $2}'`
psql -U postgres -h ${1}.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com  -p 5432 ${2} --echo-all -P pager=off -f $3 -v dbname="'$4'"
