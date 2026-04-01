#!/bin/sh
#echo "Trying to Connect to DB"
#c=1
#echo "exit" | sqlplus -L PSP_LOCAL/PSP_LOCAL@XE | grep Connected > /dev/null
#while [[ ("$?" -ne "0")  && ("$c" -le "20") ]]
#do
#let c=c+1
#echo "Retrying to connect to DB"
#sleep 10
#echo "exit" | sqlplus -L PSP_LOCAL/PSP_LOCAL@XE | grep Connected > /dev/null
#done

echo "DB connection will take 5 min"
sleep 5m

echo "Successfully Connected to DB"