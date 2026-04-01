aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/switchover/CNAME/ppsp-pds-uw02-east.json
aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/switchover/CNAME/ppsp-pds-uw02-ro-east.json
aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/switchover/CNAME/ppsp-pds-db-east.json
