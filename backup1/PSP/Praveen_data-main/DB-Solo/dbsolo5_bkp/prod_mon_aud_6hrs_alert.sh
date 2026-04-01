echo "Start DB Comparison"
date
do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="created_date between '$START_DATE' and '$END_DATE'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$WHERE\"/" prod_QRI_Reverse_temp.xml > prod_QRI_reverse_comparision.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/prod/CDC/results_PROD-MON-AUD-6hrs_${RUN_DATE}.html\"/>|" prod_QRI_reverse_comparision.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/prod/CDC/results_PROD-MON-AUD-6hrs_${RUN_DATE}.sql\"/>|" prod_QRI_reverse_comparision.xml
./commandLine -dataCompare prod_QRI_reverse_comparision.xml
echo $START_DATE
echo $END_DATE
aws s3 cp /u01/ogg/scripts/dbsolo5/results/prod/CDC/results_PROD-MON-AUD-6hrs_${RUN_DATE}.html  s3://dbsolo-reports-prod/mon-aud/cdc/
cd /u01/ogg/scripts/dbsolo5/results/prod/CDC/
grep -i '>*</a></td>' results_PROD-MON-AUD-6hrs_${RUN_DATE}.html >  mismatch_record_info.txt
if [ -s "mismatch_record_info.txt" ]
then
    cat mismatch_record_info.txt
    echo "success:db-solo prod mon-aud validation completed for 6hours& out-of-sync report"
    /usr/bin/aws sns publish --region us-west-2 --topic-arn "arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com" --subject "success:db-solo prod mon-aud validation completed for 6hours & out-of-sync report" --message "`cat results_PROD-MON-AUD-6hrs_${RUN_DATE}.sql`"
else
    echo "success:db-solo prod mon-aud validation completed for 6hours & no out-of-sync"
   /usr/bin/aws sns publish --region us-west-2 --topic-arn "arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com" --subject "success: db-solo prod mon-aud validation completed for 6hours & no out-of-sync" --message "no out-of-sync records from  mon-aud last 6hours"
fi

}

END_DATE="$(date -u -d '15 min ago' "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -u -d '6 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison
date
echo "DB Comparison Completed"
