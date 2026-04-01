#install Java
sudo yum install java-1.8.0
 
java -version


# download Db solo installer
curl https://www.dbsolo.com/download/dbsolo_nojre.sh -o dbsolo_nojre.sh

# add execute permission to downloaded db solo installer
chmod 755 ./dbsolo_nojre.sh

# install open JDK depndency required for DB solo
sudo amazon-linux-extras install java-openjdk11

# install db solo
./dbsolo_nojre.sh
give installation path:  /u01/ogg/scripts/dbsolo5

# make below dirs used in XML files
mkdir -p /home/praveen/syncscrip
mkdir -p /home/praveen/datacomp_html_file
mkdir -p /home/praveen/dbsolo_tmp

##run below it will create .dbsolo5 dir in /home/
cd /u01/ogg/scripts/dbsolo5
./commandLine



--CLOB column
<response_document included="true" pk="false" function="" mappedfunction="case when dbms_lob.getlength(#COLUMN#) =0 then null  else #COLUMN# end" mapped="RESPONSE_DOCUMENT" mappedpk="false"/> - Mapped function will contain the function for transformation at target and Function field will contain the same for source
<request_document included="true" pk="false" function="" mappedfunction="case when dbms_lob.getlength(#COLUMN#) =0 then null  else #COLUMN# end" mapped="REQUEST_DOCUMENT" mappedpk="false"/>



--postgres to postgres
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


--oracle to postgres

do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="(created_date between to_date('$START_DATE','yyyy-mm-dd hh24:mi:ss') and to_date('$END_DATE','yyyy-mm-dd hh24:mi:ss') )"
MAPPED_WHERE="created_date between '$START_DATE' and '$END_DATE'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$MAPPED_WHERE\"/" comparison_template.xml > comparison_A-B_daily.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_A-B_daily_${RUN_DATE}.html\"/>|" comparison_A-B_daily.xml
./commandLine -dataCompare  comparison_A-B_daily.xml
echo $START_DATE
echo $END_DATE
}

END_DATE="$(date -d '1 hour ago' "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -d '75 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison




