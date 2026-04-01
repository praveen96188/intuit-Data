do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="(created_date between to_date('$START_DATE','yyyy-mm-dd hh24:mi:ss') and to_date('$END_DATE','yyyy-mm-dd hh24:mi:ss') )"
MAPPED_WHERE="created_date between '$START_DATE' and '$END_DATE'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$MAPPED_WHERE\" mappedwhere=\"$WHERE\"/" compare_B-A_temp.xml > compare_B-A_utc.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_B_A_${RUN_DATE}.html\"/>|" compare_B-A_utc.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/results_B_A_${RUN_DATE}.sql\"/>|" compare_B-A_utc.xml
./commandLine -dataCompare compare_B-A_utc.xml
echo $START_DATE
echo $END_DATE
}

END_DATE="$(date -u -d '15 min ago' "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -u -d '4 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison








do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="created_date between '$START_DATE' and '$END_DATE'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$WHERE\"/" compare_reverse_QRI_template.xml > QRI_reverse_comparision.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_MON-AUD-24hrs_${RUN_DATE}.html\"/>|" QRI_reverse_comparision.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/results_MON-AUD-24hrs_${RUN_DATE}.sql\"/>|" QRI_reverse_comparision.xml
./commandLine -dataCompare QRI_reverse_comparision.xml
echo $START_DATE
echo $END_DATE
}

END_DATE="$(date -u -d '15 min ago' "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -u -d '4 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison



echo "Start DB Comparison"
date

do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="created_date between '$START_DATE 00:00:00' and '$END_DATE 23:59:59'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$WHERE\"/" comparision_QRI_template.xml > QRI_comparision.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_QRI_${START_DATE}-${END_DATE}.html\"/>|" QRI_comparision.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/results_QRI_${START_DATE}-${END_DATE}.sql\"/>|" QRI_comparision.xml
./commandLine -dataCompare QRI_comparision.xml
echo $START_DATE
echo $END_DATE
}


START_DATE=2012-01-01
END_DATE=2012-01-31
do_comparison

START_DATE=2012-02-01
END_DATE=2012-02-29
do_comparison

START_DATE=2012-03-01
END_DATE=2012-03-31
do_comparison

START_DATE=2012-04-01
END_DATE=2012-04-30
do_comparison

START_DATE=2012-05-01
END_DATE=2012-05-31
do_comparison

START_DATE=2012-06-01
END_DATE=2012-06-30
do_comparison

START_DATE=2012-07-01
END_DATE=2012-07-31
do_comparison

START_DATE=2012-08-01
END_DATE=2012-08-31
do_comparison

START_DATE=2012-09-01
END_DATE=2012-09-30
do_comparison

START_DATE=2012-10-01
END_DATE=2012-10-31
do_comparison

START_DATE=2012-11-01
END_DATE=2012-11-30
do_comparison

START_DATE=2012-12-01
END_DATE=2012-12-31
do_comparison

START_DATE=2013-01-01
END_DATE=2013-01-31
do_comparison

START_DATE=2013-02-01
END_DATE=2013-02-28
do_comparison

START_DATE=2013-03-01
END_DATE=2013-03-31
do_comparison

START_DATE=2013-04-01
END_DATE=2013-04-30
do_comparison

START_DATE=2013-05-01
END_DATE=2013-05-31
do_comparison

START_DATE=2013-06-01
END_DATE=2013-06-30
do_comparison

START_DATE=2013-07-01
END_DATE=2013-07-31
do_comparison

START_DATE=2013-08-01
END_DATE=2013-08-31
do_comparison

START_DATE=2013-09-01
END_DATE=2013-09-30
do_comparison

START_DATE=2013-10-01
END_DATE=2013-10-31
do_comparison

START_DATE=2013-11-01
END_DATE=2013-11-30
do_comparison

START_DATE=2013-12-01
END_DATE=2013-12-31
do_comparison

START_DATE=2014-01-01
END_DATE=2014-01-31
do_comparison

START_DATE=2014-02-01
END_DATE=2014-02-28
do_comparison

START_DATE=2014-03-01
END_DATE=2014-03-31
do_comparison

START_DATE=2014-04-01
END_DATE=2014-04-30
do_comparison

START_DATE=2014-05-01
END_DATE=2014-05-31
do_comparison

START_DATE=2014-06-01
END_DATE=2014-06-30
do_comparison

START_DATE=2014-07-01
END_DATE=2014-07-31
do_comparison

START_DATE=2014-08-01
END_DATE=2014-08-31
do_comparison

START_DATE=2014-09-01
END_DATE=2014-09-30
do_comparison

START_DATE=2014-10-01
END_DATE=2014-10-31
do_comparison

START_DATE=2014-11-01
END_DATE=2014-11-30
do_comparison

START_DATE=2014-12-01
END_DATE=2014-12-31
do_comparison

START_DATE=2015-01-01
END_DATE=2015-01-31
do_comparison

START_DATE=2015-02-01
END_DATE=2015-02-28
do_comparison

START_DATE=2015-03-01
END_DATE=2015-03-31
do_comparison

START_DATE=2015-04-01
END_DATE=2015-04-30
do_comparison

START_DATE=2015-05-01
END_DATE=2015-05-31
do_comparison

START_DATE=2015-06-01
END_DATE=2015-06-30
do_comparison

START_DATE=2015-07-01
END_DATE=2015-07-31
do_comparison

START_DATE=2015-08-01
END_DATE=2015-08-31
do_comparison

START_DATE=2015-09-01
END_DATE=2015-09-30
do_comparison

START_DATE=2015-10-01
END_DATE=2015-10-31
do_comparison

START_DATE=2015-11-01
END_DATE=2015-11-30
do_comparison

START_DATE=2015-12-01
END_DATE=2015-12-31
do_comparison

START_DATE=2016-01-01
END_DATE=2016-01-31
do_comparison

START_DATE=2016-02-01
END_DATE=2016-02-28
do_comparison

START_DATE=2016-03-01
END_DATE=2016-03-31
do_comparison

START_DATE=2016-04-01
END_DATE=2016-04-30
do_comparison

START_DATE=2016-05-01
END_DATE=2016-05-31
do_comparison

START_DATE=2016-06-01
END_DATE=2016-06-30
do_comparison

START_DATE=2016-07-01
END_DATE=2016-07-31
do_comparison

START_DATE=2016-08-01
END_DATE=2016-08-31
do_comparison

START_DATE=2016-09-01
END_DATE=2016-09-30
do_comparison

START_DATE=2016-10-01
END_DATE=2016-10-31
do_comparison

START_DATE=2016-11-01
END_DATE=2016-11-30
do_comparison

START_DATE=2016-12-01
END_DATE=2016-12-31
do_comparison

START_DATE=2017-01-01
END_DATE=2017-01-31
do_comparison

START_DATE=2017-02-01
END_DATE=2017-02-28
do_comparison

START_DATE=2017-03-01
END_DATE=2017-03-31
do_comparison

START_DATE=2017-04-01
END_DATE=2017-04-30
do_comparison

START_DATE=2017-05-01
END_DATE=2017-05-31
do_comparison

START_DATE=2017-06-01
END_DATE=2017-06-30
do_comparison

START_DATE=2017-07-01
END_DATE=2017-07-31
do_comparison

START_DATE=2017-08-01
END_DATE=2017-08-31
do_comparison

START_DATE=2017-09-01
END_DATE=2017-09-30
do_comparison

START_DATE=2017-10-01
END_DATE=2017-10-31
do_comparison

START_DATE=2017-11-01
END_DATE=2017-11-30
do_comparison

START_DATE=2017-12-01
END_DATE=2017-12-31
do_comparison

START_DATE=2018-01-01
END_DATE=2018-01-31
do_comparison

START_DATE=2018-02-01
END_DATE=2018-02-28
do_comparison

START_DATE=2018-03-01
END_DATE=2018-03-31
do_comparison

START_DATE=2018-04-01
END_DATE=2018-04-30
do_comparison

START_DATE=2018-05-01
END_DATE=2018-05-31
do_comparison

START_DATE=2018-06-01
END_DATE=2018-06-30
do_comparison

START_DATE=2018-07-01
END_DATE=2018-07-31
do_comparison

START_DATE=2018-08-01
END_DATE=2018-08-31
do_comparison

START_DATE=2018-09-01
END_DATE=2018-09-30
do_comparison

START_DATE=2018-10-01
END_DATE=2018-10-31
do_comparison

START_DATE=2018-11-01
END_DATE=2018-11-30
do_comparison

START_DATE=2018-12-01
END_DATE=2018-12-31
do_comparison

START_DATE=2019-01-01
END_DATE=2019-01-31
do_comparison

START_DATE=2019-02-01
END_DATE=2019-02-28
do_comparison

START_DATE=2019-03-01
END_DATE=2019-03-31
do_comparison

START_DATE=2019-04-01
END_DATE=2019-04-30
do_comparison

START_DATE=2019-05-01
END_DATE=2019-05-31
do_comparison

START_DATE=2019-06-01
END_DATE=2019-06-30
do_comparison

START_DATE=2019-07-01
END_DATE=2019-07-31
do_comparison

START_DATE=2019-08-01
END_DATE=2019-08-31
do_comparison

START_DATE=2019-09-01
END_DATE=2019-09-30
do_comparison

START_DATE=2019-10-01
END_DATE=2019-10-31
do_comparison

START_DATE=2019-11-01
END_DATE=2019-11-30
do_comparison

START_DATE=2019-12-01
END_DATE=2019-12-31
do_comparison

START_DATE=2020-01-01
END_DATE=2020-01-31
do_comparison

START_DATE=2020-02-01
END_DATE=2020-02-28
do_comparison

START_DATE=2020-03-01
END_DATE=2020-03-31
do_comparison

START_DATE=2020-04-01
END_DATE=2020-04-30
do_comparison

START_DATE=2020-05-01
END_DATE=2020-05-31
do_comparison

START_DATE=2020-06-01
END_DATE=2020-06-30
do_comparison

START_DATE=2020-07-01
END_DATE=2020-07-31
do_comparison

START_DATE=2020-08-01
END_DATE=2020-08-31
do_comparison

START_DATE=2020-09-01
END_DATE=2020-09-30
do_comparison

START_DATE=2020-10-01
END_DATE=2020-10-31
do_comparison

START_DATE=2020-11-01
END_DATE=2020-11-30
do_comparison

START_DATE=2020-12-01
END_DATE=2020-12-31
do_comparison

START_DATE=2021-01-01
END_DATE=2021-01-31
do_comparison

START_DATE=2021-02-01
END_DATE=2021-02-28
do_comparison

START_DATE=2021-03-01
END_DATE=2021-03-31
do_comparison

START_DATE=2021-04-01
END_DATE=2021-04-30
do_comparison

START_DATE=2021-05-01
END_DATE=2021-05-31
do_comparison

START_DATE=2021-06-01
END_DATE=2021-06-30
do_comparison

START_DATE=2021-07-01
END_DATE=2021-07-31
do_comparison

START_DATE=2021-08-01
END_DATE=2021-08-31
do_comparison

START_DATE=2021-09-01
END_DATE=2021-09-30
do_comparison

START_DATE=2021-10-01
END_DATE=2021-10-31
do_comparison

START_DATE=2021-11-01
END_DATE=2021-11-30
do_comparison

START_DATE=2021-12-01
END_DATE=2021-12-31
do_comparison

START_DATE=2022-01-01
END_DATE=2022-01-31
do_comparison

START_DATE=2022-02-01
END_DATE=2022-02-28
do_comparison

START_DATE=2022-03-01
END_DATE=2022-03-31
do_comparison

START_DATE=2022-04-01
END_DATE=2022-04-30
do_comparison

START_DATE=2022-05-01
END_DATE=2022-05-31
do_comparison

START_DATE=2022-06-01
END_DATE=2022-06-30
do_comparison

START_DATE=2022-07-01
END_DATE=2022-07-31
do_comparison

START_DATE=2022-08-01
END_DATE=2022-08-31
do_comparison

START_DATE=2022-09-01
END_DATE=2022-09-30
do_comparison

START_DATE=2022-10-01
END_DATE=2022-10-31
do_comparison

START_DATE=2022-11-01
END_DATE=2022-11-30
do_comparison

START_DATE=2022-12-01
END_DATE=2022-12-31
do_comparison

START_DATE=2023-01-01
END_DATE=2023-01-31
do_comparison

START_DATE=2023-02-01
END_DATE=2023-02-28
do_comparison

START_DATE=2023-03-01
END_DATE=2023-03-31
do_comparison

START_DATE=2023-04-01
END_DATE=2023-04-30
do_comparison

START_DATE=2023-05-01
END_DATE=2023-05-31
do_comparison

START_DATE=2023-06-01
END_DATE=2023-06-30
do_comparison

START_DATE=2023-07-01
END_DATE=2023-07-31
do_comparison

START_DATE=2023-08-01
END_DATE=2023-08-31
do_comparison

START_DATE=2023-09-01
END_DATE=2023-09-30
do_comparison

START_DATE=2023-10-01
END_DATE=2023-10-31
do_comparison

START_DATE=2023-11-01
END_DATE=2023-11-30
do_comparison

START_DATE=2023-12-01
END_DATE=2023-12-31
do_comparison

START_DATE=2024-01-01
END_DATE=2024-01-31
do_comparison

START_DATE=2024-02-01
END_DATE=2024-02-28
do_comparison

START_DATE=2024-03-01
END_DATE=2024-03-31
do_comparison

START_DATE=2024-04-01
END_DATE=2024-04-30
do_comparison

date
echo "Start DB Comparison completed"







---Historical Data comparision

echo "Start DB Comparison"


do_comparison () {
WHERE="(created_date between to_date('$START_DATE 00:00:00','yyyy-mm-dd hh24:mi:ss') and to_date('$END_DATE 23:59:59','yyyy-mm-dd hh24:mi:ss') )"
MAPPED_WHERE="created_date between '$START_DATE 00:00:00' and '$END_DATE 23:59:59'"
sed "s/$TABLE_NAME included=\"false\" where=\"\" mappedwhere=\"\"/$TABLE_NAME included=\"true\" where=\"$WHERE\" mappedwhere=\"$MAPPED_WHERE\"/" comparison_template.xml > comparison.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_${TABLE_NAME}_${START_DATE}_${END_DATE}.html\"/>|" comparison.xml
./commandLine -dataCompare comparison.xml
echo $TABLE_NAME
echo $START_DATE
echo $END_DATE
}


START_DATE=2014-03-01
END_DATE=2014-03-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-03-11
END_DATE=2014-03-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-03-21
END_DATE=2014-03-31
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-04-01
END_DATE=2014-04-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-04-11
END_DATE=2014-04-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-04-21
END_DATE=2014-04-30
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-05-01
END_DATE=2014-05-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-05-11
END_DATE=2014-05-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-05-21
END_DATE=2014-05-31
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-06-01
END_DATE=2014-06-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-06-11
END_DATE=2014-06-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-06-21
END_DATE=2014-06-30
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-07-01
END_DATE=2014-07-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-07-11
END_DATE=2014-07-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-07-21
END_DATE=2014-07-31
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-09-01
END_DATE=2014-09-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-09-11
END_DATE=2014-09-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-09-21
END_DATE=2014-09-30
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-10-01
END_DATE=2014-10-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-10-11
END_DATE=2014-10-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-10-21
END_DATE=2014-10-31
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-12-01
END_DATE=2014-12-10
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-12-11
END_DATE=2014-12-20
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

START_DATE=2014-12-21
END_DATE=2014-12-31
TABLE_NAME=PSP_SOURCE_SYSTEM_TRANSMISSION
do_comparison

echo "Start DB Comparison completed"




<?xml version = '1.0' encoding = 'UTF-8'?>
<connections>
   <connection driver="PostgreSQL - JDBC" name="postgres_audit" user="dms_apg_src" domain="" password="e#xc91kggPGH" host="psp-par-aud.cluster-ro-cjls0bohfgpq.us-west-2.rds.amazonaws.com" file="" url="" database="parapgib" port="5432" sid="MSC" group="1" servicename="" encrypt="false" trust_server_cert="false" hostname_in_server_cert="" tnsname="" lastconnect="1710844567949" connection_count="1" id="1672631697040" autocommit="true" isolation="2" persist_username="2" identifiers="NONE" connectivity="false" reconnect="false" connectivity_interval="3" connect_as="0" connection_type="1">
           <properties/>
      <settings/>
   </connection>
   <connection driver="PostgreSQL - JDBC" name="postgres_mon" user="dms_apg_src" domain="" password="e#xc91kggPGH" host="ppsp-stg-pitparmo.cluster-ro-cjls0bohfgpq.us-west-2.rds.amazonaws.com" file="" url="" database="pitparmo" port="5432" sid="" group="1" servicename="" encrypt="false" trust_server_cert="false" hostname_in_server_cert="" tnsname="" lastconnect="1686833929398" connection_count="1" id="1710844497693" autocommit="true" isolation="8" persist_username="2" identifiers="NONE" connectivity="false" reconnect="false" connectivity_interval="3" connect_as="0" connection_type="0">
      <properties/>
      <settings/>
   </connection>
</connections>



        <datacomp_emptystrings_as_nulls value="false"/>
        <datacomp_ignorestringcase value="false"/>
        <datacomp_trimstring value="true"/>
        <datacomp_ignorenumbersign value="false"/>
        <datacomp_decimal_scale value="-1"/>
        <datacomp_comparenumberdecimals value="true"/>
        <datacomp_comparedateyear value="true"/>
        <datacomp_comparedatemonth value="true"/>
        <datacomp_comparedateday value="true"/>
        <datacomp_comparedatehour value="true"/>
        <datacomp_comparedateminute value="true"/>
        <datacomp_comparedatesecond value="true"/>
        <datacomp_comparedatemillisecond value="true"/>
        <datacomp_ignorematching value="true"/>
        <datacomp_maxrows value="100000000"/>
        <datacomp_filebased value="true"/>
        <datacomp_temp_dir value="/u01/ogg/scripts/dbsolo5/tempdir"/>
        <datacomp_rows_per_tempfile value="16384"/>
        <datacomp_rows_per_tempfile_blob value="1024"/>
        <datacomp_missing_table_error value="true"/>
        <datacomp_missing_column_error value="true"/>
        <datacomp_extra_table_include value="false"/>
        <datacomp_extra_column_include value="true"/>
        <datacomp_sync_syncsource value="false"/>
        <datacomp_sync_upperkeywords value="true"/>
        <datacomp_sync_sepsemicolon value="true"/>
        <datacomp_sync_ident_brackets value="false"/>
        <datacomp_sync_ident_quotes value="false"/>
        <datacomp_sync_ident_custom value=""/>
        <datacomp_sync_commit value="0"/>
        <datacomp_sync_comments value="true"/>
        <datacomp_sync_schema value="true"/>
        <datacomp_sync_indent value="true"/>
        <datacomp_sync_filename value=""/>
        <datacomp_sync_file_encoding value="UTF-8"/>
        <datacomp_sync_directoryname value="/u01/ogg/scripts/dbsolo5/syncfiles"/>
        <datacomp_loader_directory value="/u01/ogg/scripts/dbsolo5/syncfiles"/>
        <datacomp_sync_loader value="false"/>
        <datacomp_sync_disabletriggers value="false"/>
        <datacomp_sync_disablefk value="false"/>
        <datacomp_sync_delete value="true"/>
        <datacomp_sync_identity_on value="true"/>
        <datacomp_sync_update value="true"/>
        <datacomp_sync_insert value="true"/>
        <datacomp_sync_directory value="false"/>
        <datacomp_html_file value=""/>
        <datacomp_html_title value="DB Solo - Data Compare Results"/>
        <datacomp_html_heading value="DB Solo - Data Compare Results"/>
        <datacomp_html_header_background value="209,209,209"/>
        <datacomp_html_header_fontsize value="11"/>
        <datacomp_html_header_alignment value="1"/>
        <datacomp_html_even_background value="238,238,238"/>
        <datacomp_html_even_fontsize value="11"/>
        <datacomp_html_even_alignment value="1"/>
        <datacomp_html_odd_background value="221,221,221"/>
        <datacomp_html_odd_fontsize value="11"/>
        <datacomp_html_odd_alignment value="1"/>
        <datacomp_html_diff_background value="239,242,202"/>
        <datacomp_html_diff_fontsize value="11"/>
        <datacomp_html_diff_alignment value="1"/>
        <datacomp_html_timing value="false"/>
        <datacomp_html_details value="true"/>
        <datacomp_html_details_include_matching value="false"/>
        <datacomp_html_details_include_matching_columns value="true"/>
        <datacomp_html_details_timestamp value="false"/>
        <datacomp_html_include_matching value="true"/>
    </settings>
</compare>




        <datacomp_emptystrings_as_nulls value="false"/>
        <datacomp_ignorestringcase value="false"/>
        <datacomp_trimstring value="true"/>
        <datacomp_ignorenumbersign value="false"/>
        <datacomp_decimal_scale value="-1"/>
        <datacomp_comparenumberdecimals value="true"/>
        <datacomp_comparedateyear value="true"/>
        <datacomp_comparedatemonth value="true"/>
        <datacomp_comparedateday value="true"/>
        <datacomp_comparedatehour value="true"/>
        <datacomp_comparedateminute value="true"/>
        <datacomp_comparedatesecond value="true"/>
        <datacomp_comparedatemillisecond value="true"/>
        <datacomp_ignorematching value="true"/>
        <datacomp_maxrows value="100000000"/>
        <datacomp_filebased value="true"/>
        <datacomp_temp_dir value="/u01/ogg/scripts/dbsolo5/tempdir"/>
        <datacomp_rows_per_tempfile value="16384"/>
        <datacomp_rows_per_tempfile_blob value="1024"/>
        <datacomp_missing_table_error value="true"/>
        <datacomp_missing_column_error value="true"/>
        <datacomp_extra_table_include value="false"/>
        <datacomp_extra_column_include value="true"/>
        <datacomp_sync_syncsource value="false"/>
        <datacomp_sync_upperkeywords value="true"/>
        <datacomp_sync_sepsemicolon value="true"/>
        <datacomp_sync_ident_brackets value="false"/>
        <datacomp_sync_ident_quotes value="false"/>
        <datacomp_sync_ident_custom value=""/>
        <datacomp_sync_commit value="0"/>
        <datacomp_sync_comments value="true"/>
        <datacomp_sync_schema value="true"/>
        <datacomp_sync_indent value="true"/>
        <datacomp_sync_filename value="/u01/ogg/scripts/dbsolo5/results/results_B_A_20230120050829.sql"/>
        <datacomp_sync_file_encoding value="UTF-8"/>
        <datacomp_sync_directoryname value="/u01/ogg/scripts/dbsolo5/syncfiles"/>
        <datacomp_loader_directory value="/u01/ogg/scripts/dbsolo5/syncfiles"/>
        <datacomp_sync_loader value="false"/>
        <datacomp_sync_disabletriggers value="false"/>
        <datacomp_sync_disablefk value="false"/>
        <datacomp_sync_delete value="true"/>
        <datacomp_sync_identity_on value="true"/>
        <datacomp_sync_update value="true"/>
        <datacomp_sync_insert value="true"/>
        <datacomp_sync_directory value="false"/>
        <datacomp_html_file value="/u01/ogg/scripts/dbsolo5/results/results_B_A_20230120050829.html"/>
        <datacomp_html_title value="DB Solo - Data Compare Results"/>
        <datacomp_html_heading value="DB Solo - Data Compare Results"/>
        <datacomp_html_header_background value="209,209,209"/>
        <datacomp_html_header_fontsize value="11"/>
        <datacomp_html_header_alignment value="1"/>
        <datacomp_html_even_background value="238,238,238"/>
        <datacomp_html_even_fontsize value="11"/>
        <datacomp_html_even_alignment value="1"/>
        <datacomp_html_odd_background value="221,221,221"/>
        <datacomp_html_odd_fontsize value="11"/>
        <datacomp_html_odd_alignment value="1"/>
        <datacomp_html_diff_background value="239,242,202"/>
        <datacomp_html_diff_fontsize value="11"/>
        <datacomp_html_diff_alignment value="1"/>
        <datacomp_html_timing value="false"/>
        <datacomp_html_details value="true"/>
        <datacomp_html_details_include_matching value="false"/>
        <datacomp_html_details_include_matching_columns value="true"/>
        <datacomp_html_details_timestamp value="false"/>
        <datacomp_html_include_matching value="true"/>
    </settings>
</compare>


revoke SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_qbdt_request_info  from pspadm_readwrite_role;
revoke SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_company_policy  from pspadm_readwrite_role;
revoke SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_company_qbdt_pitem  from pspadm_readwrite_role;
revoke SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_employee_deduction  from pspadm_readwrite_role;
revoke SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_policy  from pspadm_readwrite_role;



do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
MAPPED_WHERE="created_date between '$START_DATE' and '$END_DATE'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$MAPPED_WHERE\" mappedwhere=\"$MAPPED_WHERE\"/" ccomparision_qri_reverse.xml > Mon_Aud_QRI_compare.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_B_A_${RUN_DATE}.html\"/>|" Mon_Aud_QRI_compare.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/results_B_A_${RUN_DATE}.sql\"/>|" Mon_Aud_QRI_compare.xml
./commandLine -dataCompare Mon_Aud_QRI_compare.xml
echo $START_DATE
echo $END_DATE
}

END_DATE="$(date -u -d '15 min ago' "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -u -d '4 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison







echo "Start DB Comparison"
date

do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="created_date between '$START_DATE ' and '$END_DATE '"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$WHERE\"/" comparision_qri_reverse.xml > compare_QRI_24Reverse.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_QRI_${RUN_DATE}.html\"/>|" compare_QRI_24Reverse.xml
sed -i "s|<datacomp_sync_filename value=\"\"/>|<datacomp_sync_filename value=\"/u01/ogg/scripts/dbsolo5/results/results_QRI_${RUN_DATE}.sql\"/>|" compare_QRI_24Reverse.xml
./commandLine -dataCompare compare_QRI_24Reverse.xml
echo $START_DATE
echo $END_DATE
}

END_DATE="$(date "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -d '24 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison




revoke SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_qbdt_request_info  from pspadm_readwrite_role;

GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_qbdt_request_info  TO pspadm_readwrite_role;

REVOKE ALL ON pspadm.psp_qbdt_request_info FROM pspadm_readwrite_role;




GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.temp_qri  TO pspadm_readwrite_role;

select count(*)* from pspadm.psp_qbdt_request_info;
DELETE FROM pspadm.psp_qbdt_request_info WHERE qbdt_request_info_seq = 'test';
update pspadm.psp_qbdt_request_info set   qbdt_request_info_seq = 'test' where qbdt_request_info_seq= 'test' ;

INSERT INTO ibobadm_pds.psp_qbdt_request_info (qbdt_request_info_seq, 
                                          version, 
                                          creator_id, 
                                          created_date, 
                                          modifier_id, 
                                          modified_date, 
                                          employee_add_count, 
                                          employee_update_count, 
                                          employee_update_start, 
                                          employee_add_start, 
                                          paycheck_add_count, 
                                          paycheck_update_count, 
                                          payroll_processing_start, 
                                          payroll_item_add_count, 
                                          payroll_item_update_count, 
                                          payroll_item_update_start, 
                                          payroll_item_delete_count, 
                                          payroll_processing_end, 
                                          payroll_item_add_end, 
                                          payroll_item_update_end, 
                                          payroll_transaction_add_end, 
                                          payroll_trans_update_end, 
                                          employee_delete_count, 
                                          payroll_trans_delete_count, 
                                          delete_processing_start, 
                                          delete_processing_end, 
                                          payroll_item_add_start, 
                                          payroll_transaction_add_count, 
                                          payroll_trans_update_count, 
                                          payroll_trans_update_start, 
                                          payroll_transaction_add_start, 
                                          employee_add_end, 
                                          employee_update_end, 
                                          paycheck_delete_count, 
                                          source_system_transmission_fk, 
                                          company_fk, 
                                          realm_id)
                                          VALUES 
                                         ('test', 
                                          1, 
                                          NULL, 
                                          TO_TIMESTAMP('2024-4-4 02:34:31:987','YYYY-MM-DD HH24:MI:SS:MS'), 
                                          NULL, 
                                          TO_TIMESTAMP('2024-4-4 02:34:31:987','YYYY-MM-DD HH24:MI:SS:MS'), 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          NULL, 
                                          'test5', 
                                          'test2', 
                                          1);
