aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/switchover/CNAME/ppsp-pds-uw02-west.json
aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/switchover/CNAME/ppsp-pds-uw02-ro-west.json
aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/switchover/CNAME/ppsp-pds-db-west.json
