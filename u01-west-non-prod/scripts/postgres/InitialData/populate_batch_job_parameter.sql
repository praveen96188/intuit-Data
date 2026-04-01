DROP TABLE IF EXISTS TEMP_PSP_BATCH_JOB_PARAMETER CASCADE;

--------------------------------------------------------
-- Create temp table                                  --
--------------------------------------------------------
CREATE TABLE TEMP_PSP_BATCH_JOB_PARAMETER (LIKE PSP_BATCH_JOB_PARAMETER INCLUDING ALL) ;

--------------------------------------------------------
-- Insert into temp table                             --
--------------------------------------------------------

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('SalesTaxExceptionMonitor', 1, -1, NULL, 'time_constraint', 'relative', '381692a8-3408-484f-b4ff-c4435b0531ef')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('SalesTaxExceptionMonitor', 1, -1, NULL, 'time_constraint_seconds', '18000', '5bd9f54f-b3d5-45a9-81db-613d26393bb7')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('SalesTaxExceptionMonitor', 1, -1, NULL, 'min_successful_runs', '1', '493b22e0-c34d-4c03-86d8-4521c2b9e447')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('SalesTaxExceptionMonitor', 1, -1, 'SalesTaxExceptionMonitorStep', 'time_constraint', 'relative', '481692a8-3408-484f-b4ff-c4435b0531ef')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('SalesTaxExceptionMonitor', 1, -1, 'SalesTaxExceptionMonitorStep', 'time_constraint_seconds', '18000', '6bd9f54f-b3d5-45a9-81db-613d26393bb7')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('SalesTaxExceptionMonitor', 1, -1, 'SalesTaxExceptionMonitorStep', 'min_successful_runs', '1', '593b22e0-c34d-4c03-86d8-4521c2b9e447')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AnnualBillingMonitor', 1, -1, NULL, 'time_constraint', 'absolute', 'fe8ee8c1-639c-4c4c-a41d-449bd7b9cd92')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AnnualBillingMonitor', 1, -1, NULL, 'time_constraint_begin_time', '08:00', 'fe8ee8c2-639c-4c4c-a41d-449bd7b9cd92')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AchOffloadCompleteMonitor', 1, -1, NULL, 'time_constraint', 'absolute', 'c49c7bc1-50a9-466b-ada0-43d08a2b93e5')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AchOffloadCompleteMonitor', 1, -1, NULL, 'time_constraint_begin_time', '17:00', 'd5103b56-2caa-4386-a532-ee3eb848fc1f')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AchReturnsMonitor', 1, -1, NULL, 'time_constraint', 'absolute', 'd57bf462-5fe2-4772-b995-abf89af2af27')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AchReturnsMonitor', 1, -1, NULL, 'time_constraint_begin_time', '05:00', '60680987-2c15-4851-b660-cb64db41b6b5')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AchTransactionsMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '5ac70e8a-5e2b-4bae-bd52-2d5dd3d1f7c3')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AchTransactionsMonitor', 1, -1, NULL, 'time_constraint_begin_time', '05:00', 'f79d187a-6f4a-45a3-8038-8e42ac8eaecf')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EmailGatewayMonitor', 1, -1, NULL, 'time_constraint', 'relative', 'd84667e7-c339-464e-91b9-bebe4d186c4e')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EmailGatewayMonitor', 1, -1, NULL, 'time_constraint_seconds', '600', '3d38cff3-ccbb-4942-83e8-ce024c5923d6')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EmailGatewayMonitor', 1, -1, NULL, 'min_successful_runs', '1', 'fe8ee8c1-538c-4c4c-a41d-448bd7b9cd91')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OffloadedTransactionsEventsMonitor', 1, -1, NULL, 'time_constraint', 'relative', '0dac73ed-0a02-402d-b6ee-82f3dc536596')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OffloadedTransactionsEventsMonitor', 1, -1, NULL, 'time_constraint_seconds', '2700', 'c98e706f-0e91-42e2-86b2-6c30b9634a4a')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OffloadedTransactionsEventsMonitor', 1, -1, NULL, 'min_successful_runs', '2', 'ceee5e50-b8dc-4b4d-8a9c-c6e65f18330b')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('FraudPayrollsMonitor', 1, -1, NULL, 'time_constraint', 'relative', 'f5caf896-2cd6-45b3-bd78-650ba3ce3be9')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('FraudPayrollsMonitor', 1, -1, NULL, 'time_constraint_seconds', '900', '4daf3387-16ad-4d01-8995-95447c247913')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('FraudPayrollsMonitor', 1, -1, NULL, 'min_successful_runs', '14', '1d376ef2-f5fb-456e-9467-53b9985cc93a')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsAccountsReceivableMonitor', 1, -1, NULL, 'time_constraint', 'relative', '01af11cc-ddde-4b76-ad58-4b08fd66a793')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsAccountsReceivableMonitor', 1, -1, NULL, 'time_constraint_seconds', '3600', '8e921d34-31cb-428c-8c9d-cef7ea174455')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsAccountsReceivableMonitor', 1, -1, NULL, 'min_successful_runs', '1', '5d96a41b-13f8-4e2d-b3e0-2dec400f6f5f')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsGeneralLedgerMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '7ed8b207-58bf-4bb5-8a90-8a30cdad4b14')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsGeneralLedgerMonitor', 1, -1, NULL, 'time_constraint_begin_time', '03:00', 'c604642c-5dd2-4d90-a7b9-82e824e7b5d6')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsGeneralLedgerUploadMonitor', 1, -1, NULL, 'time_constraint', 'relative', '79fb50f2-9502-444c-a50c-35f598bb73a0')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsGeneralLedgerUploadMonitor', 1, -1, NULL, 'time_constraint_seconds', '900', '25af72fd-0902-4c3c-a21e-a6f7f34436cd')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('GemsGeneralLedgerUploadMonitor', 1, -1, NULL, 'min_successful_runs', '1', '2f2a42c8-dbc3-47e7-a62c-4d6796831502')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('LedgerBalanceMonitor', 1, -1, NULL, 'time_constraint', 'relative', '95d2c205-e20f-4347-9188-5f3eb875bf4b')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('LedgerBalanceMonitor', 1, -1, NULL, 'time_constraint_seconds', '5400', '9959969a-77b0-4175-bc46-07ebb3ae8d02')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('LedgerBalanceMonitor', 1, -1, NULL, 'min_successful_runs', '1', 'ab4a9a1b-ddde-4123-aa81-9c6e62ac2aa1')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedPayrollsMonitor', 1, -1, 'MissedPayrollsMonitorProcessorStep', 'time_constraint', 'absolute', 'dd5bcc8a-5575-420a-a123-69aeaf9c0267')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedPayrollsMonitor', 1, -1, 'MissedPayrollsMonitorProcessorStep', 'time_constraint_begin_time', '17:00', 'c70c9dbe-614f-49d9-82e7-4d3857207c42')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedPayrollsMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '3c74b768-238d-43f1-98f5-2f62467d2517')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedPayrollsMonitor', 1, -1, NULL, 'time_constraint_begin_time', '17:00', '99be71c5-4a2a-4c72-b4b1-f9a2ecc4df37')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedTransactionsMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '0f0f431c-4c76-4e36-aec1-c478cb902789')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedTransactionsMonitor', 1, -1, NULL, 'time_constraint_begin_time', '17:00', 'bdef4b57-d944-47d6-8b81-f4308745646e')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedTransactionsMonitor', 1, -1, 'MissedTransactionsMonitorProcessorStep', 'time_constraint', 'absolute', '41253446-e540-4bc4-b4f5-842389e38708')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('MissedTransactionsMonitor', 1, -1, 'MissedTransactionsMonitorProcessorStep', 'time_constraint_begin_time', '17:00', '9f1caf3a-1444-425a-9ab4-eb5b8a9f04dd')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NightlyBatchJobsMonitor', 1, -1, NULL, 'time_constraint', 'absolute', 'be5acd50-9f7f-43ff-8952-1f69ffaeb505')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NightlyBatchJobsMonitor', 1, -1, NULL, 'time_constraint_begin_time', '5:00', 'bbf28294-691d-4eed-bbc3-6659b3ddae97')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryDailyBatchJobsMonitor', 1, -1, NULL, 'time_constraint', 'absolute', 'e2beb79b-6bfb-44fd-8426-937d2ef90a85')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryDailyBatchJobsMonitor', 1, -1, NULL, 'time_constraint_begin_time', '17:15', '45c8e8c8-4a97-4a87-9e6d-c33881663302')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledDailyBatchJobsMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '6856d949-e231-4238-99b6-7bc3b6793586')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledDailyBatchJobsMonitor', 1, -1, NULL, 'time_constraint_begin_time', '19:05', '68d1fa1a-5c8d-4b93-8794-859e156aeda7')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ThirdParty401kOffloadMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '3f005d5a-926b-4051-ae7e-962b0d3b7b8a')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ThirdParty401kOffloadMonitor', 1, -1, NULL, 'time_constraint_begin_time', '07:05', '7ed617e7-3036-4475-afb4-bb1e383fec18')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ThirdParty401kValidationMonitor', 1, -1, NULL, 'time_constraint', 'absolute', 'f86a8656-2e70-4489-9a5a-5e9f6f981448')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ThirdParty401kValidationMonitor', 1, -1, NULL, 'time_constraint_begin_time', '09:00', '859f325e-6fc9-4cec-992b-af0b7e7d38d2')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('CheckPrintMonitor', 1, -1, NULL, 'time_constraint', 'relative', 'e2efa634-9e52-4f7a-8e4b-2ec7d963e738')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('CheckPrintMonitor', 1, -1, NULL, 'time_constraint_seconds', '900', 'f367b915-8f41-4b3b-b81d-101c0d9078ed')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('CheckPrintMonitor', 1, -1, NULL, 'min_successful_runs', '1', 'e5d9f0aa-5053-476d-9484-785d768373ea')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('TaxCreditsEchoSignMonitor', 1, -1, NULL, 'time_constraint', 'relative', '97154745-fb50-4421-9563-724c3e7973e0')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('TaxCreditsEchoSignMonitor', 1, -1, NULL, 'time_constraint_seconds', '900', '5cf0f996-0bc3-4dfc-bbb0-21eb7bb2a365')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('TaxCreditsEchoSignMonitor', 1, -1, NULL, 'min_successful_runs', '2', '93cd3ced-ce90-4a3d-82d1-13978115d7e0')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsEnrollmentsMonitor', 1, -1, NULL, 'time_constraint', 'relative', '6f74bdf1-41aa-44e1-9f5d-f29a86552110')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsEnrollmentsMonitor', 1, -1, NULL, 'time_constraint_seconds', '3600', '29aa4e36-f59f-4736-844e-64d9dafd3b44')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsEnrollmentsMonitor', 1, -1, NULL, 'min_successful_runs', '1', 'b868a939-8e85-48fb-942c-407e4aa6405b')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsSendMonitor', 1, -1, NULL, 'time_constraint', 'relative', '3296508a-c1a1-4144-a555-2e208c5f49c1')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsSendMonitor', 1, -1, NULL, 'time_constraint_seconds', '1800', 'd4fe6e2e-22ae-4a34-bf07-141636c06460')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsSendMonitor', 1, -1, NULL, 'min_successful_runs', '1', 'df544dcf-7acd-4b1d-9b4d-456f9a976aed')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsResponseMonitor', 1, -1, NULL, 'time_constraint', 'relative', 'cf95c455-272a-4d8c-9926-1485638ec111')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsResponseMonitor', 1, -1, NULL, 'time_constraint_seconds', '1800', 'b09fd79b-5641-43d7-a700-a5df5954e1d7')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsResponseMonitor', 1, -1, NULL, 'min_successful_runs', '1', 'ad7e0384-5501-4aaf-be0a-f09413568c34')
;


INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsEnrollmentsAgeOutMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '0029de41-111c-40d2-b5c4-ec1656bc850a')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsEnrollmentsAgeOutMonitor', 1, -1, NULL, 'time_constraint_begin_time', '07:00', '442b13b3-d9be-4874-b55c-eb2ba5266959')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('IOPDataSyncMonitor', 1, -1, NULL, 'time_constraint', 'relative', '281692a8-3408-484f-b4ff-c4435b0531ef')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('IOPDataSyncMonitor', 1, -1, NULL, 'time_constraint_seconds', '600', '4bd9f54f-b3d5-45a9-81db-613d26393bb7')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('IOPDataSyncMonitor', 1, -1, NULL, 'min_successful_runs', '1', '393b22e0-c34d-4c03-86d8-4521c2b9e447')
;

-- EftpsPaymentMonitor is a relative check and goes back 2 hours.  This setting will not pick up a false positive
-- but could pick up a false negative if the processing only takes 1 hour.
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsPaymentMonitor', 1, -1, NULL, 'time_constraint', 'relative', 'aeabdf7d-35ed-4fa3-85b7-96674b0d0640')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsPaymentMonitor', 1, -1, NULL, 'time_constraint_seconds', '7800', '416e2e63-6a85-4e03-a2c7-738acd605946')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EftpsPaymentMonitor', 1, -1, NULL, 'min_successful_runs', '1', '50b9301c-9994-47f3-8623-d15bd41c8d3a')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, NULL, 'time_constraint', 'relative', '4a9881bb-7a5d-4cd0-ba1e-cd39d67e68cc')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, NULL, 'time_constraint_seconds', '300', '3fbf691a-14b8-46ab-95b2-516b59c3375a')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, NULL, 'min_successful_runs', '1', '1e6c90d6-1b1e-4d21-bdf2-36dd990dc0dc')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, NULL, 'time_constraint', 'relative', '0d0d3fb4-a95e-4de9-9109-5a41dc7da0bb')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, NULL, 'time_constraint_seconds', '300', 'e2683213-e7cd-4453-aaf0-caafd4c548e6')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, NULL, 'min_successful_runs', '1', '56156e96-f4ad-43a3-979a-d6eead0ab0f9')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationStarted', 'time_constraint', 'relative', '27a3ec82-5ee9-41c9-a42b-815bab1986c4')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationStarted', 'time_constraint_seconds', '300', '03c5b23a-546a-4fe2-9bf8-d3d6dd5b8143')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationStarted', 'min_successful_runs', '1', '1f5c28fe-3a8b-47f5-89ba-cd7e535554c6')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationCompleted', 'time_constraint', 'relative', 'd4babd44-535d-43be-8e60-03d8f0700a8d')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationCompleted', 'time_constraint_seconds', '300', 'e7258e2d-e3c4-4dd4-a08c-f3d3cd2afdec')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationCompleted', 'min_successful_runs', '1', '3defcf31-d7bd-4890-9d65-d473b41711f7')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileSendStarted', 'time_constraint', 'relative', '7f81484b-b319-4daa-b759-1242075c6f0e')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileSendStarted', 'time_constraint_seconds', '300', '1c42876c-a844-450a-a79d-ce9bad23d5c1')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileSendStarted', 'min_successful_runs', '1', '55cef177-d3e9-40f1-9626-d619f5a3615f')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileSendComplete', 'time_constraint', 'relative', '915bd967-50ab-4d66-a171-e914a5114b41')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileSendComplete', 'time_constraint_seconds', '300', '587db2f4-2b8c-444b-b4dc-19ad5f258767')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyAchFileSendComplete', 'min_successful_runs', '1', 'f229f8a8-fa14-4aab-9229-5c90744105f5')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyDicrFileReceived', 'time_constraint', 'relative', 'a627a1bc-a564-4e9f-863f-54600fb7ff5b')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyDicrFileReceived', 'time_constraint_seconds', '300', '47826e61-53ad-40c6-ba89-40c874b376dc')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'VerifyDicrFileReceived', 'min_successful_runs', '1', '38c4fd7b-4cbb-44bf-bd71-09343f3c6b63')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'PerformFinalOffloadChecks', 'time_constraint', 'relative', 'e9fe2609-2520-4ab8-a768-48c5d4e761e0')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'PerformFinalOffloadChecks', 'time_constraint_seconds', '300', '2285a5a0-3977-4533-8911-7fd0da796895')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PrimaryAchOffloadMonitor', 1, -1, 'PerformFinalOffloadChecks', 'min_successful_runs', '1', '02b5a93a-aaf8-4031-9fe8-0dba5b2aa82f')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationStarted', 'time_constraint', 'relative', '8ed53580-2524-492e-96a7-3880ebb96a59')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationStarted', 'time_constraint_seconds', '300', '333c0810-bf66-48b8-828b-0a97bf06ea28')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationStarted', 'min_successful_runs', '1', 'e3718b8e-ac51-438e-ad3b-8a9cec1202dc')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationCompleted', 'time_constraint', 'relative', 'd903f161-d86a-4662-9e8b-05cf4590fb8d')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationCompleted', 'time_constraint_seconds', '300', '03f30244-a5ec-4a21-abf9-8b1febed2c11')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileCreationCompleted', 'min_successful_runs', '1', '61a4b532-f93c-4192-8cde-58d8ab08119c')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileSendStarted', 'time_constraint', 'relative', 'f9e2f764-5121-4422-ba49-5bb84a79e223')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileSendStarted', 'time_constraint_seconds', '300', '0976c5d2-92a3-4acd-b367-5d9cefa577a0')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileSendStarted', 'min_successful_runs', '1', 'ac9ab68e-88c0-4ac7-bd1e-c2911ca7103b')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileSendComplete', 'time_constraint', 'relative', '1289e3bf-7bf5-4026-bab0-887671865c2b')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileSendComplete', 'time_constraint_seconds', '300', 'fd51d8d1-9221-4432-8901-c222418b4baf')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyAchFileSendComplete', 'min_successful_runs', '1', 'dbc701ef-ada7-45fb-bfbc-13b7ff9288bf')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyDicrFileReceived', 'time_constraint', 'relative', 'a086ce58-5aed-4a6b-b0a1-23fde67957fc')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyDicrFileReceived', 'time_constraint_seconds', '300', 'bdfebd50-a101-46fe-a968-aa683318e777')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'VerifyDicrFileReceived', 'min_successful_runs', '1', 'aabe0ecc-b540-4aff-9b14-90e700d45bf0')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'PerformFinalOffloadChecks', 'time_constraint', 'relative', 'ebf288e2-1db2-47fc-9bb6-f0d916923b74')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'PerformFinalOffloadChecks', 'time_constraint_seconds', '300', '7097641d-ef2f-4eaf-b50a-bab7631f7520')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ScheduledAchOffloadMonitor', 1, -1, 'PerformFinalOffloadChecks', 'min_successful_runs', '1', 'c276b374-a6b5-41aa-8d5e-449e467e3865')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('StateReportMonitor', 1, -1, NULL, 'time_constraint', 'absolute', '9bf5ef4c-e7ac-4bff-a2b0-cbfdb7de7986')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('StateReportMonitor', 1, -1, NULL, 'time_constraint_begin_time', '01:00', '6e9c4f36-ee80-4c56-b6cf-144d6b491ebc')
;

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('WorkersCompMonitor', 1, -1, NULL, 'time_constraint', 'relative', '46b9f66a-852d-4486-96cc-c790bcff5caa')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('WorkersCompMonitor', 1, -1, NULL, 'time_constraint_seconds', '2700', '464521a9-1678-4997-82c1-d1aee47cc48f')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OFACReportMonitor', 1, -1, NULL, 'time_constraint', 'relative', '8a40a480-74e0-441e-a9a5-9d63cd25b7b5')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OFACReportMonitor', 1, -1, NULL, 'time_constraint_seconds', '2700', 'eede7bff-d6e1-4b6e-bf52-369da7a88ddf')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AMLReportMonitor', 1, -1, NULL, 'time_constraint', 'relative', 'de1e958e-7cc3-44b7-b59b-3b8129ee7709')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AMLReportMonitor', 1, -1, NULL, 'time_constraint_seconds', '2700', 'da8775c7-3b54-4fbe-af23-07eece3a2309')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('IndustryReportMonitor', 1, -1, NULL, 'time_constraint', 'relative', '8eb2e7f0-c1c5-4653-a1fb-1e7531a29852')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('IndustryReportMonitor', 1, -1, NULL, 'time_constraint_seconds', '5400', '6f2c069e-25d7-4a16-a7ea-f7a2d1854f49')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('TPSUReportMonitor', 1, -1, NULL, 'time_constraint', 'relative', '8eb2e7f0-c1c5-4653-a1fb-1e7531a29853')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('TPSUReportMonitor', 1, -1, NULL, 'time_constraint_seconds', '5400', '6f2c069e-25d7-4a16-a7ea-f7a2d1854f50')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('IamEmailAddressMonitor', 1, -1, NULL, 'time_constraint', 'relative', '060e5953-8900-49ec-a626-a7e3c919a62a')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('IamEmailAddressMonitor', 1, -1, NULL, 'time_constraint_seconds', '600', 'f7b8c024-9205-4ac7-899d-f9c26f437e3e')
;
--Write Cron Jobs
--INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
--VALUES ('DailyPayrollStatsPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'daily_payroll_stats.sql', '491b7782-d857-448c-b56e-d7943921d0c0')
--/
--INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
--VALUES ('DailyPayrollStatsPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'Daily Payroll Stats', '4c581fff-b35a-4bca-8a3e-73c1b06d89e0')
--/
-- INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
-- VALUES ('DailyPayrollStatsPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'SBSEG-PDKusumitaDirect@intuit.com,Praveenkumar_Hoolimath@intuit.com,vikas_pandey@intuit.com', '28c9e986-1a31-486f-a04e-d0570ae3e092')
-- /
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('FailedPayrollPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'jira_15771.sql', '43d96947-c870-4802-90c9-31fb1465d828')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('FailedPayrollPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'Retry Symphony errors at month end', '4e37365c-8ac6-49b0-a44f-b840590f3732')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('FailedPayrollPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', 'be465e3a-a70b-4ae4-93c0-fa4958499eae')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PayrollFraudBatchPurgePlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'dbupgrade_PSRV001106b.sql,delete_payroll_fraud_batch.sql', 'b93086d4-e358-47a7-a8b8-025fa4c0fab7')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PayrollFraudBatchPurgePlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'PSPADM.PSP_BATCH_JOB_AUDIT_LOG purge', 'df99ffd2-8cbe-4552-b034-35b03b01d876')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('PayrollFraudBatchPurgePlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', '00da122c-0de8-4d35-b77e-9f1e9e3cea42')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NCDFixPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'jira_17895.sql', 'eb58bb2c-fd2e-4f10-a9c5-c722bf021003')
;
--INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
--VALUES ('PSPEventLogPurgePlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'dbupgrade_PSRV001106e.sql', 'a8a93cbf-4b23-4cc0-a8ee-bd7a19198bc9')
--/
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NCDFixPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'NCD fix for Item 1101349 with EOC 037893 or 145897 or 145279', '8d30a10e-4f60-4ae8-92ff-7673036855a2')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NCDFixPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', 'c2e1d4f3-9ddf-42c5-9f18-dc5d6fe61931')
;
-- INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
-- VALUES ('PSPEventLogPurgePlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'dbupgrade_PSRV001106e.sql', 'a8a93cbf-4b23-4cc0-a8ee-bd7a19198bc9')
-- /

INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EFTPSOnHoldPaymentPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'update_psp_money_movement_transaction.sql', '307e13e7-bad1-4c97-8827-d31e625a42d6')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EFTPSOnHoldPaymentPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'update_psp_money_movement_transaction', 'aa54ad05-5545-4385-9b26-6196c65cca9b')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EFTPSOnHoldPaymentPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', '4181dca6-6b5b-4829-93ed-172598efe289')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ValidateEmployeeWagePlansPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'jira_15963.sql', '8d87595a-85b3-4869-8e18-d21b5c0d42ed')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ValidateEmployeeWagePlansPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'Validate Employee Wage Plans', 'c5d25a9c-a7a8-47cc-8cd7-cfe3921af61b')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('ValidateEmployeeWagePlansPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', '4f71741a-b646-4f91-923e-4f4acac0fcac')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OfferingUpdateUsageBillingPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', '71763.sql', 'c2266746-acb5-42c5-a2bd-5b7f02be6510')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OfferingUpdateUsageBillingPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'OFFERING UPDATE USAGE BILLING', '6166fcd5-0297-42c5-b670-b2460598c8ed')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('OfferingUpdateUsageBillingPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', 'b18d7ca1-5999-4487-a656-f2aac4625113')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EDRAssociationFixPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'update_pspentry.sql', 'cf375937-afea-4412-b9fc-96b7dae2f894')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EDRAssociationFixPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'EDR Association Fix', 'c7b631f3-1383-4dd9-b38c-2ba612e4dce0')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('EDRAssociationFixPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', 'affbd6b5-1d9c-40ee-a49d-486fa30865f8')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('RetryEntitlementActivationPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'jira_14162.sql', '6e7a4e72-e1cc-4cfc-b358-e3a0500f211e')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('RetryEntitlementActivationPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'Retry Entitlement Activation', 'df867ddd-0d3a-4df3-a892-d71a42b0210e')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('RetryEntitlementActivationPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', 'bf0e19ca-731b-4f2c-a1b1-96ec8d88fa14')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NCDFixALLPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'jira_9096.sql', '585c13e7-51ae-46d9-ac7e-91dd321746c1')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NCDFixALLPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'Updates for Retail Activation Customers', '317b69e3-14d7-4022-b695-dccda7565b52')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('NCDFixALLPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', '101dd803-1696-4389-b72d-8071a0057904')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('CostCoPlSqlJobsProcessor', 1, -1, NULL, 'sql_files_list', 'psp-5052_costco672_84.sql', '5a80b0ce-859f-442e-bcec-f9c0d2759995')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('CostCoPlSqlJobsProcessor', 1, -1, NULL, 'email_subject', 'costco672 psp-5052', '72e1ab83-f9e0-4c41-a7bc-ea2f88fd826d')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('CostCoPlSqlJobsProcessor', 1, -1, NULL, 'email_recipient', 'Praveenkumar_Hoolimath@intuit.com,karthikeyan_muthurangam@intuit.com,ramasubramani_n@intuit.com,koustav_sur@intuit.com,anu_johnson@intuit.com', '7e9bdb5b-ed64-4b71-81b7-ce53e3a7d33c')
;
INSERT INTO TEMP_PSP_BATCH_JOB_PARAMETER ( BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID )
VALUES ('AccountServiceSyncExceptionProcessor', 1, -1, NULL, 'email_recipient', 'vikas_pandey@intuit.com', 'a2df38d5-e4a3-428b-b709-03dd741e7afb')
;
--------------------------------------------------------
-- Synchronize temp table and real table by           --
-- inserting, deleting, and updating as necessary     --
--------------------------------------------------------

INSERT INTO PSP_BATCH_JOB_PARAMETER
(BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID)
  SELECT
    BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID
  FROM
    TEMP_PSP_BATCH_JOB_PARAMETER tt
  WHERE
    tt.ID NOT IN (SELECT ID FROM PSP_BATCH_JOB_PARAMETER)
;

DELETE FROM PSP_BATCH_JOB_PARAMETER
WHERE
  ID NOT IN (SELECT ID FROM TEMP_PSP_BATCH_JOB_PARAMETER)
;

UPDATE PSP_BATCH_JOB_PARAMETER RT
SET (BATCH_JOB_SETUP_FK, VERSION, REALM_ID, JOB_STEP, PARAM_NAME, PARAM_VALUE, ID) =
(SELECT
TT.BATCH_JOB_SETUP_FK, TT.VERSION, TT.REALM_ID, TT.JOB_STEP, TT.PARAM_NAME, TT.PARAM_VALUE, TT.ID
FROM
TEMP_PSP_BATCH_JOB_PARAMETER TT
WHERE
TT.ID = RT.ID
)
;

--------------------------------------------------------
-- Drop temp table                                  --
--------------------------------------------------------
DROP TABLE TEMP_PSP_BATCH_JOB_PARAMETER
;

COMMIT
