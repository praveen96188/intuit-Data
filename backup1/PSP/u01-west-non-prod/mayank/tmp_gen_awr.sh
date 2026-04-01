logdir="/u01/mayank/logs"

rm -f ${logdir}/awrrpt_*.html
rm -f /tmp/awrrpt_*.html

./awr_report_m.sh pspsys01 2203020800 2203020830
#./awr_report_m.sh pspsys01 2107181345 2107181400
#./awr_report_m.sh pspsys01 2107251345 2107251400
#./awr_report_m.sh pspsys01 2107171000 2107171015
#./awr_report_m.sh pspsys01 2107241000 2107241015
#./awr_report_m.sh pspsys01 2107171100 2107171130
#./awr_report_m.sh pspsys01 2107241100 2107241130
#./awr_report_m.sh pspsys01 2107160845 2107160900
#./awr_report_m.sh pspsys01 2107230845 2107230900
#./awr_report_m.sh pspsys01 2107161015 2107161030
#./awr_report_m.sh pspsys01 2107231015 2107231030
