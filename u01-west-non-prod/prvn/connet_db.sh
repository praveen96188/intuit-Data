export PGPASSWORD=`cat .pp|grep ${1}${2}|awk '{print $2}'`
psql -U ${1} -h ${2}.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p ${3}  --echo-all -P pager=off  
