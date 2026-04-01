echo "Start DB Comparison"
do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="created_date between '$START_DATE 00:00:00' and '$END_DATE 00:00:00'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$WHERE\"/" comparision_QRI_template.xml > QRI_comparision.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_QRI_${START_DATE}-${START_DATE}.html\"/>|" QRI_comparision.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/results_QRI_${START_DATE}-${END_DATE}.sql\"/>|" QRI_comparision.xml
./commandLine -dataCompare QRI_comparision.xml
echo $START_DATE
echo $END_DATE
}

START_DATE=2012-01-01
END_DATE=2013-01-01
do_comparison
