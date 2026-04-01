
--monolith writer
--save file ppsp-pds-uw02-west.json 
{
    "Comment": "Update record to reflect new IP address for a system ",
    "Changes": [
        {
            "Action": "UPSERT",
            "ResourceRecordSet": {
                "Name": "ppsp-pds-uw02.sbg-psp-ppd.a.intuit.com",
                "Type": "CNAME",
                "TTL": 300,
                "ResourceRecords": [
                    {
                        "Value": "ppsp-pds-uw02.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com"
                    }
                ]
            }
        }
    ]
}
--run command 
aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/CNAME/ppsp-pds-uw02-west.json 



--monolith Reader
--save file ppsp-pds-uw02-ro-west.json
{
    "Comment": "Update record to reflect new IP address for a system ",
    "Changes": [
        {
            "Action": "UPSERT",
            "ResourceRecordSet": {
                "Name": "ppsp-pds-uw02-ro.sbg-psp-ppd.a.intuit.com",
                "Type": "CNAME",
                "TTL": 300,
                "ResourceRecords": [
                    {
                        "Value": "ppsp-pds-uw02.cluster-ro-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com"
                    }
                ]
            }
        }
    ]
}

--run command 
aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/CNAME/ppsp-pds-uw02-ro-west.json 



--audit writer
--save file ppsp-pds-db-west.json

{
    "Comment": "Update record to reflect new IP address for a system ",
    "Changes": [
        {
            "Action": "UPSERT",
            "ResourceRecordSet": {
                "Name": "   ppsp-pds-db.sbg-psp-ppd.a.intuit.com",
                "Type": "CNAME",
                "TTL": 300,
                "ResourceRecords": [
                    {
                        "Value": "ppsp-pds-db.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com"
                    }
                ]
            }
        }
    ]
}

--run command
aws route53 change-resource-record-sets --hosted-zone-id ZPMOD8K08QNEL --change-batch file:///u01/CNAME/ppsp-pds-db-west.json 


