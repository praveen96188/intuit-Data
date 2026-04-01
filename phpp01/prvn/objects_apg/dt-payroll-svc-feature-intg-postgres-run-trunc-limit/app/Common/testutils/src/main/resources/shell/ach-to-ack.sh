#!/bin/bash

#For all ach files in the cwd (those files starting with d),
#copy the 1 and 9 records into a file named Intuit_AchACK_$achid.txt
#The 'achid' variable contains the timestamp from the ach file name.
#
#Example:
#file=d.1205500008882CCD.txt
#achid=${file//.txt/}  <-- This yields: d.1205500008882CCD
#achid=${achid#*.}     <-- this yields: 1205500008882CCD

filecount=`ls $HOME/[d]* -1 | wc -l`

if [ $filecount -gt 0 ]
then
  sleep 10
  for file in $HOME/[d]*
  do
    achid=${file%.*}
    achid=${achid#*.}
    grep -E ^[19][^9].+ $file > $HOME/Intuit_AchACK_$achid.txt
    mv $file $HOME/archive/
    cp $HOME/Intuit_AchACK_$achid.txt $HOME/archive/Intuit_AchACK_$achid.txt
  done
fi
