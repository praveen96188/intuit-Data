typeset -i ret
while ( true )
do
  ./$1 $2 $3 > /dev/null
  ret=`grep "^Job" $3 |wc -l`
  echo "At `date`: return=$ret"
  if [ $ret -eq 1 ]; then
    echo "$1 $2 $3"|mailx -s "Job $2 is completed at `date`" simon_liu@intuit.com
    exit
  fi
  sleep 300
done


