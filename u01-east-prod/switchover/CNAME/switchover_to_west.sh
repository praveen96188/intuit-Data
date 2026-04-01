aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_datalake_west.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_rjf_west.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_rpt_west.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_vmp_west.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_writer_west.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_audit_reader_west.json
aws route53 change-resource-record-sets --hosted-zone-id Z35GYKEF3QQE9L --change-batch file:///u01/switchover/CNAME/prod_audit_writer_west.json
