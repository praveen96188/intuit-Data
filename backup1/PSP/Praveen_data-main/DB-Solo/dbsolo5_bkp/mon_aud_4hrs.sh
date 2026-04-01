do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="created_date between '$START_DATE' and '$END_DATE'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$WHERE\"/" compare_reverse_QRI_template.xml > QRI_reverse_comparision.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_MON-AUD-4hrs_${RUN_DATE}.html\"/>|" QRI_reverse_comparision.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/results_MON-AUD-4hrs_${RUN_DATE}.sql\"/>|" QRI_reverse_comparision.xml
./commandLine -dataCompare QRI_reverse_comparision.xml
echo $START_DATE
echo $END_DATE
aws s3 cp /u01/ogg/scripts/dbsolo5/results/results_MON-AUD-4hrs_${RUN_DATE}.html s3://dbsolo-reports-prod/staging-aud-mon/
}

END_DATE="$(date -u -d '15 min ago' "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -u -d '4 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison
