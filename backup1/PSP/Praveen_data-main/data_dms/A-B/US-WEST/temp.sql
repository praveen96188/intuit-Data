 



1.we have setup fullload + cdc (Stop after caches changes apply).
2.task stopped after full+ cdc
3.applied indexes
4.applied constraints witg disable 
5.metadat imported(no invalid objects)
6.resumed task

Create directory DATA_DUMP as '/u01/prvn/';

nohup expdp userid=intuadmin/xxxxx@pspuep05 directory=DATA_PUMP_DIR dumpfile=exp_pspadm_metadata.dmp SCHEMAS=PSPADM content=METADATA_ONLY logfile=DATA_PUMP_DIR:exp_pspadm_metadata.log 2>&1 &

 intuadmin/"KVadPpU3q#(7eC"@'psphpp02.cjls0bohfgpq.us-west-2.rds.amazonaws.com:1521/psphpp02'



 Instance 1: 10.95.111.176
 instance 2;10.95.110.33
 instance 3;10.95.110.13
 instance 4: 10.95.110.149
 instance 5:   10.95.110.12


 c_psp_auth_operation

c_psp_batch_job_status0


SELECT 'TRUNCATE TABLE pspadm.' ||  table_name || 'cascade;' 
  FROM information_schema.tables
 WHERE table_schema='pspadm';


 alter table pspadm.psp_entity_update
    add column company_fk varchar(255);



ALTER TABLE pspadm.psp_batch_job_setup add CONSTRAINT c_psp_batch_job_setup0 CHECK (job_type IN ('ACHDeEnrollmentBatchJob', 'ACHEnrollmentBatchJob', 'ACHEnrollmentResponseBatchJob', 'ACHTraceIdProcessor', 'AMLReportMonitor', 'AMLReportProcessor', 'AMOMessageProcessor', 'AMOMessageProcessorMonitor', 'ATFCompanyInfoExtract', 'ATFCompanyLiabilityExtract', 'ATFCompanyPaymentExtract', 'ATFCompanyPayrollItemExtract', 'ATFCompanyTaxExtract', 'ATFCompanyTaxRateExtract', 'ATFDataExtract', 'ATFDepositFrequencyExtract', 'ATFEmployeeInfoExtract', 'ATFEmployeeTotalsExtract', 'ATFWageLimitsExtract', 'AccountServiceSyncExceptionProcessor', 'AchDebitOffload', 'AchDebitOffloadMonitor', 'AchOffloadCompleteMonitor', 'AchReturnsMonitor', 'AchTaxPaymentOffload', 'AchTaxPaymentOffloadMonitor', 'AchTransactionsMonitor', 'AchZeroPayments', 'AchZeroPaymentsMonitor', 'AnnualBillingMonitor', 'AnnualBillingProcessor', 'AssistedUsageDataSyncProcessor', 'AssistedUsageReportingToBRMProcessor', 'BRMUsageErrorFileProcessor', 'BulkWorkforceInviteProcessor', 'CheckPrint', 'CheckPrintMonitor', 'CompanyMigrationProcessor', 'ComplianceToolKit', 'CostCoPlSqlJobsProcessor', 'DataReencryptionProcessor', 'EDRAssociationFixPlSqlJobsProcessor', 'EFTPSOnHoldPaymentPlSqlJobsProcessor', 'EMSBSToBRMDataSyncProcessor', 'EMSBSToBRMDataSyncProcessorMonitor', 'EVSCompanyProcessor', 'EdiPayment', 'EdiPaymentMonitor', 'EdiResponse', 'EdiResponseMonitor', 'EdiSend', 'EdiSendMonitor', 'EftpsEnrollments', 'EftpsEnrollmentsAgeOut', 'EftpsEnrollmentsAgeOutMonitor', 'EftpsEnrollmentsMonitor', 'EftpsPayment', 'EftpsPaymentMonitor', 'EftpsResponse', 'EftpsResponseMonitor', 'EftpsSend', 'EftpsSendMonitor', 'EmailGateway', 'EmailGatewayMonitor', 'EmployeePayrollItemTotalsCalcProcess', 'EmployeeTotalsCalculationMonitor', 'EmployeeTotalsCalculationProcess', 'EmployeeW2TotalsCalculationProcessor', 'EnrollmentDeleteSelectionMonitor', 'EnrollmentDeleteSelectionProcessor', 'EntitlementProcessor', 'EntitlementProcessorMonitor', 'EntityEvent', 'EntityEventRetry', 'EntityInitialLoadProcessor', 'EoqSUIAdjustments', 'EoqSUIAdjustmentsMonitor', 'FailedPayrollPlSqlJobsProcessor', 'FraudPayrolls', 'FraudPayrollsMonitor', 'FsetFilingMonitor', 'FsetFilingProcessor', 'FsetResponseMonitor', 'FsetResponseProcessor', 'GemsAccountsReceivable', 'GemsAccountsReceivableMonitor', 'GemsGeneralLedger', 'GemsGeneralLedgerMonitor', 'GemsGeneralLedgerUpload', 'GemsGeneralLedgerUploadMonitor', 'IOPDataSync', 'IOPDataSyncMonitor', 'IRSDepositFrequencyFileProcessor', 'IRSDepositFrequencyFileProcessorMonitor', 'IamEmailAddressMonitor', 'IamEmailAddressProcessor', 'IndustryReportMonitor', 'IndustryReportProcessor', 'LedgerBalance', 'LedgerBalanceMonitor', 'LedgerOperations', 'MTLCompanyToOnHoldProcessor', 'MissedPayrollsMonitor', 'MissedTransactionsMonitor', 'MonthlyFee', 'MonthlyFeeMonitor', 'MtlTransactionReportEnrichProcessor', 'NCDFixALLPlSqlJobsProcessor', 'NCDFixPlSqlJobsProcessor', 'NightlyBatchJobs', 'NightlyBatchJobsMonitor', 'OFACReportMonitor', 'OFACReportProcessor', 'OfferingUpdateUsageBillingPlSqlJobsProcessor', 'OffloadedTransactionsEvents', 'OffloadedTransactionsEventsMonitor', 'PSPToEMSBSDataSyncProcessor', 'PSPToSMSMigrationProcessor', 'PayrollFraudBatchPurgePlSqlJobsProcessor', 'PrimaryAchOffloadMonitor', 'PrimaryDailyBatchJobs', 'PrimaryDailyBatchJobsMonitor', 'PrintedCheckBatch', 'PrintedCheckBatchMonitor', 'QbdtUnprocessedRequestsRetry', 'RAFWriter', 'RTBAutomation', 'RealTimeEntityEventRetryProcessor', 'ReconPlus', 'ReconPlusMonitor', 'RetryEntitlementActivationPlSqlJobsProcessor', 'RiskProfileMigrationProcessor', 'SUICreditsBatchJob', 'SalesTaxExceptionMonitor', 'SalesTaxExceptionProcessor', 'ScheduledAchOffloadMonitor', 'ScheduledDailyBatchJobs', 'ScheduledDailyBatchJobsMonitor', 'ScheduledEmails', 'SendCustomEmailsProcessor', 'SendMonthlyDataToTFSMonitor', 'SendMonthlyDataToTFSProcessor', 'SendW2AnnualDataToTFSProcessor', 'SendW2PreviewDataToTFSProcessor', 'SoxDBUserReport', 'SoxReport', 'StateReport', 'StateReportMonitor', 'TPSUReportMonitor', 'TPSUReportProcessor', 'TaxCreditsEchoSign', 'TaxCreditsEchoSignMonitor', 'ThirdParty401kOffload', 'ThirdParty401kOffloadMonitor', 'ThirdParty401kValidation', 'ThirdParty401kValidationMonitor', 'ValidateEmployeeWagePlansPlSqlJobsProcessor', 'W2CountsExtract', 'WorkersCompMonitor', 'WorkersCompProcessor');






SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity
where usename not in ('rdsadmin','postgres')
group by db,usename,machine;



select pg_terminate_backend(pid)
from pg_stat_activity
where pid in (select pid
              FROM pg_stat_activity
              where usename in ('ibob_sys_pspapp','ibobadm_owner'));


ALTER USER  WITH PASSWORD 'ftK0tvv1Y.j5kZBg';

do_comparison () {
cd /u01/ogg/scripts/dbsolo5/
WHERE="(created_date between to_date('$START_DATE','yyyy-mm-dd hh24:mi:ss') and to_date('$END_DATE','yyyy-mm-dd hh24:mi:ss') )"
MAPPED_WHERE="created_date between '$START_DATE' and '$END_DATE'"
sed "s/included=\"false\" where=\"\" mappedwhere=\"\"/included=\"true\" where=\"$WHERE\" mappedwhere=\"$MAPPED_WHERE\"/" comparison_template.xml > comparison_A-B_4hrs.xml
sed -i "s|<datacomp_html_file value=\"\"/>|<datacomp_html_file value=\"/u01/ogg/scripts/dbsolo5/results/results_A-B_${RUN_DATE}.html\"/>|" comparison_A-B_4hrs.xml
./commandLine -dataCompare comparison_A-B_4hrs.xml
echo $START_DATE
echo $END_DATE
}

END_DATE="$(date "+%Y-%m-%d %H:%M:%S")"
START_DATE="$(date -d '4 hour ago' "+%Y-%m-%d %H:%M:%S")"
RUN_DATE="$(date +%Y%m%d%H%M%S)"
do_comparison








export PGPASSWORD=`cat .pp`
psql_cmd="psql --username=postgres -h ${1}.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 ${2} --echo-all -P pager=off -f ${3} -o ${4}"
eval "$psql_cmd"



. /l/orcl
cd /u01/prvn/postgres/ 
cluster_postgres=$1
db_name=$2
vacuum_script=$3
RUN_DATE="$(date +%Y%m%d%H%M%S)"


sed '1~10 i ' 

./run_postgres.sh ${cluster_postgres} $db_name $vacuum_script  $vacuum_out_${RUN_DATE}.sql

sed -e '10~10 SELECT pg_sleep(60);'  vacuum_out_${RUN_DATE}.sql

./run_postgres.sh ${cluster_postgres} $db_name $vacuum_out_${RUN_DATE}.sql  $vacuum_out_${RUN_DATE}.log



Using an Amazon RDS Oracle Standby (read replica) as a source with Binary Reader for CDC in AWS DMS
Working with an AWS-managed Oracle database as a source for AWS DMS


. /l/orcl
cd /u01/prvn/postgres/ 
cluster_postgres=$1
db_name=$2
RUN_DATE=vacuum_out_"$(date +%Y%m%d)"



./run_postgres.sh ${cluster_postgres} $db_name vacuum_script.sql  ${RUN_DATE}.sql



sed  'N;s/.*/&\nLINE/' 


escalating this issue to our internal team for further investigation .

Aurora Postgres Hash Database Setup
Cluster setup Via Ips
Schema setup
DMS Replication Setup (Full-Load + CDC)

pre-requirement setup on source and target
Endpoint setup source for and target
Get Range Boundaries from Source
Table wise DMS Tasks
Table mapping
DMS Task settings
Post-FullLoad steps
Index creation on Target