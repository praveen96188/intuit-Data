aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_datalake_east.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_rjf_east.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_rpt_east.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_vmp_east.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_writer_east.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_audit_reader_east.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_audit_writer_east.json
