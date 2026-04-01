--db error
fields @timestamp, @message
| sort @timestamp desc
| filter @message like /FATAL|ERROR|WARNING|Fail|error|fail/
| filter @message not like /column "total_time" does not exist|postgresi|ops_user|ipsmonuser|psp_smssync_failure|error_/
#| filter @message not like /duplicate key value violates unique constraint|column "total_time" does not exist/
#| filter @message not like /_fail|error_count/
| limit 2000
1. open cloud watch
2.select create new dashboard
3.give dash board name
4.cancel all and select action 
5. select view/edit source
6. paste below and select update
--Dashboard postgrea
{
    "widgets": [
        {
            "height": 6,
            "width": 12,
            "y": 0,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "title": "DatabaseActiveConnection",
                "period": 60,
                "yAxis": {
                    "right": {
                        "label": ""
                    },
                    "left": {
                        "label": "count",
                        "showUnits": false
                    }
                },
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 6,
            "x": 12,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "ReadIOPS", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 300,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 6,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "WriteIOPS", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 60,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 12,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "ReadLatency", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02-ReadLatency" } ],
                    [ ".", "WriteLatency", ".", ".", { "region": "us-west-2", "label": "psp-prod-uw02-WriteLatency" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 300,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 18,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "FreeLocalStorage", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 60,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 18,
            "x": 12,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "MaximumUsedTransactionIDs", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 60,
                "stat": "Average",
                "title": "MaximumUsedTransactionIDs"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 0,
            "x": 12,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 300,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 12,
            "x": 12,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "FreeableMemory", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 60,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 24,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "ReplicationSlotDiskUsage", "DBInstanceIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 60,
                "stat": "Average",
                "title": "ReplicationSLotDiskUsage"
            }
        },
        {
            "height": 6,
            "width": 12,
            "y": 24,
            "x": 12,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "AuroraReplicaLag", "DBClusterIdentifier", "psp-prod-uw02", { "region": "us-west-2", "label": "psp-prod-uw02" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 60,
                "stat": "Average",
                "title": "AuroraReplica-Lag"
            }
        },
        {
            "height": 6,
            "width": 24,
            "y": 30,
            "x": 0,
            "type": "log",
            "properties": {
                "query": "SOURCE '/aws/rds/cluster/psp-prod-uw02/postgresql' | fields @timestamp, @message\n| sort @timestamp desc\n| filter @message like /FATAL|ERROR|WARNING|Fail|error|fail/\n| filter @message not like /column \"total_time\" does not exist|postgresi|ops_user|ipsmonuser|database 1|psp_smssync_failure|unaffected|skipping|could not receive data from client|error_/\n#| filter @message not like /duplicate key value violates unique constraint|column \"total_time\" does not exist/\n#| filter @message not like /_fail|error_count/\n| limit 2000",
                "region": "us-west-2",
                "stacked": false,
                "title": "Monolith-logs-check",
                "view": "table"
            }
        }
    ]
}



---oracle

{
    "widgets": [
        {
            "height": 6,
            "width": 6,
            "y": 0,
            "x": 0,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "DatabaseActiveConnection", "psphpp02", { "region": "us-west-2" } ]
                ],
                "region": "us-west-2",
                "title": "DatabaseActiveConnection",
                "period": 300,
                "yAxis": {
                    "left": {
                        "label": "count",
                        "showUnits": false
                    }
                }
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 0,
            "x": 18,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "AWS/RDS", "ReadIOPS", "DBInstanceIdentifier", "psphpp02" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 6,
            "x": 0,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "AWS/RDS", "WriteIOPS", "DBInstanceIdentifier", "psphpp02" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 6,
            "x": 6,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "WriteLatency", "DBInstanceIdentifier", "psphpp02" ],
                    [ ".", "ReadLatency", ".", "." ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 300,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 6,
            "x": 18,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/RDS", "FreeStorageSpace", "DBInstanceIdentifier", "psphpp02" ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "period": 300,
                "stat": "Average"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 6,
            "x": 12,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "AWS/RDS", "FreeableMemory", "DBInstanceIdentifier", "psphpp02" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 0,
            "x": 12,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", "psphpp02" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 0,
            "x": 6,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", "psphpp02" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 12,
            "x": 0,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "DatabaseLoadProfile", "psphpp02-'Redo size'-'per second'" ],
                    [ ".", "psphpp02-'Redo size'-'per transaction'" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 12,
            "x": 6,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "DatabaseLoadProfile", "psphpp02-'Block changes'-'per second'" ],
                    [ ".", "psphpp02-'Logical reads'-'per second'" ],
                    [ ".", "psphpp02-'Physical reads'-'per second'" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 12,
            "x": 12,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "DatabaseLoadProfile", "psphpp02-'Block changes'-'per transaction'" ],
                    [ ".", "psphpp02-'Logical reads'-'per transaction'" ],
                    [ ".", "psphpp02-'Physical reads'-'per transaction'" ]
                ],
                "region": "us-west-2"
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 12,
            "x": 18,
            "type": "metric",
            "properties": {
                "view": "timeSeries",
                "stacked": false,
                "metrics": [
                    [ "DatabaseLoadProfile", "psphpp02-'Executes'-'per second'" ],
                    [ ".", "psphpp02-'Executes'-'per transaction'" ],
                    [ ".", "psphpp02-'Hard parses'-'per second'" ],
                    [ ".", "psphpp02-'Hard parses'-'per transaction'" ]
                ],
                "region": "us-west-2"
            }
        }
    ]
}



---DMS Dashboard code 
{
    "widgets": [
        {
            "height": 7,
            "width": 11,
            "y": 0,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CDCLatencySource", "ReplicationInstanceIdentifier", "dms-replication-instance-4", "ReplicationTaskIdentifier", "2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y", { "label": "Task-1a", "region": "us-west-2" } ],
                    [ "...", "M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I", { "label": "Task-1b", "region": "us-west-2" } ],
                    [ "...", "EU72NS7WHMU7QR3JJB3TEZOCVDV5KSVTDIPH2GI", { "label": "Task-1c", "region": "us-west-2" } ],
                    [ "...", "IBIMFFVUNNPOPO4GJ7DA6IKD265XV7HDEOXCSXI", { "label": "Task-2", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-3", ".", "3TBXL6Y7JEDSFRZCLQAY7XHZLYCOUT6FCZGB7IY", { "label": "Task-3", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-2", ".", "PMX26H4RS76ZT4U5ZGA5OWPIDSXUIRKQ7H7QKQY", { "label": "Task-4", "region": "us-west-2" } ],
                    [ "...", "W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA", { "label": "Task-5", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "YRB2OMJSRTA25FW5WJ5NL7KEP5NODD7GM3WIW2Q", { "label": "Task-6", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-4", ".", "YQD6HCU3LEMCB4K66BO5IWHZPWB5P2ZGLTIIJPY", { "label": "Task-7a", "region": "us-west-2" } ],
                    [ "...", "TK5HMZZYS5USWRZ4ZXUNXA656GEQSEVLMOIBBJQ", { "label": "Task-7b", "region": "us-west-2" } ],
                    [ "...", "QSHKZUDA3JRCIHLQ2VUPYX2HIQCBDP43QRCOVHA", { "label": "Task-7c", "region": "us-west-2" } ],
                    [ "...", "UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI", { "label": "Task-7d", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "6KIJFS3AGMWUVEEDMHO3CVDGN2VYB6DAY3XN3CQ", { "label": "Task-8", "region": "us-west-2" } ]
                ],
                "view": "singleValue",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Task - CDCLatencySource",
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        },
        {
            "height": 7,
            "width": 13,
            "y": 0,
            "x": 11,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CDCLatencyTarget", "ReplicationInstanceIdentifier", "dms-replication-instance-4", "ReplicationTaskIdentifier", "2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y", { "label": "Task-1a", "region": "us-west-2" } ],
                    [ "...", "M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I", { "label": "Task-1b", "region": "us-west-2" } ],
                    [ "...", "EU72NS7WHMU7QR3JJB3TEZOCVDV5KSVTDIPH2GI", { "label": "Task-1c", "region": "us-west-2" } ],
                    [ "...", "IBIMFFVUNNPOPO4GJ7DA6IKD265XV7HDEOXCSXI", { "label": "Task-2", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-3", ".", "3TBXL6Y7JEDSFRZCLQAY7XHZLYCOUT6FCZGB7IY", { "label": "Task-3", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-2", ".", "PMX26H4RS76ZT4U5ZGA5OWPIDSXUIRKQ7H7QKQY", { "label": "Task-4", "region": "us-west-2" } ],
                    [ "...", "W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA", { "label": "Task-5", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "YRB2OMJSRTA25FW5WJ5NL7KEP5NODD7GM3WIW2Q", { "label": "Task-6", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-4", ".", "YQD6HCU3LEMCB4K66BO5IWHZPWB5P2ZGLTIIJPY", { "label": "Task-7a", "region": "us-west-2" } ],
                    [ "...", "TK5HMZZYS5USWRZ4ZXUNXA656GEQSEVLMOIBBJQ", { "label": "Task-7b", "region": "us-west-2" } ],
                    [ "...", "QSHKZUDA3JRCIHLQ2VUPYX2HIQCBDP43QRCOVHA", { "label": "Task-7c", "region": "us-west-2" } ],
                    [ "...", "UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI", { "label": "Task-7d", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "6KIJFS3AGMWUVEEDMHO3CVDGN2VYB6DAY3XN3CQ", { "label": "Task-8", "region": "us-west-2" } ]
                ],
                "view": "singleValue",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Task - CDCLatencyTarget",
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 21,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CPUUtilization", "ReplicationInstanceExternalResourceId", "63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y", { "label": "Instance-1", "region": "us-west-2" } ],
                    [ "...", "NAJTH2BBXBANRBWAISBSRU2VKMQC6JCDORUCUEI", { "label": "Instance-2", "region": "us-west-2" } ],
                    [ "...", "3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY", { "label": "Instance-3", "region": "us-west-2" } ],
                    [ "...", "KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y", { "label": "Instance-4", "region": "us-west-2" } ]
                ],
                "view": "singleValue",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Instance - CPU utilization",
                "legend": {
                    "position": "bottom"
                },
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 21,
            "x": 6,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CPUUtilization", "ReplicationInstanceExternalResourceId", "63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y", { "label": "Instance-1", "region": "us-west-2" } ],
                    [ "...", "NAJTH2BBXBANRBWAISBSRU2VKMQC6JCDORUCUEI", { "label": "Instance-2", "region": "us-west-2" } ],
                    [ "...", "3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY", { "label": "Instance-3", "region": "us-west-2" } ],
                    [ "...", "KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y", { "label": "Instance-4", "region": "us-west-2" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Instance - CPU utilization",
                "legend": {
                    "position": "bottom"
                },
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 21,
            "x": 12,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "FreeStorageSpace", "ReplicationInstanceExternalResourceId", "63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y", { "label": "Instance-1", "region": "us-west-2" } ],
                    [ "...", "NAJTH2BBXBANRBWAISBSRU2VKMQC6JCDORUCUEI", { "label": "Instance-2", "region": "us-west-2" } ],
                    [ "...", "3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY", { "label": "Instance-3", "region": "us-west-2" } ],
                    [ "...", "KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y", { "label": "Instance-4", "region": "us-west-2" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Instance - Free storage space",
                "stat": "Average",
                "period": 300
            }
        },
        {
            "height": 6,
            "width": 6,
            "y": 21,
            "x": 18,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "FreeMemory", "ReplicationInstanceExternalResourceId", "63N23JHUF4X3WPS22ZCAEJTH525YQIH6AYKBB3Y", { "region": "us-west-2", "label": "Instance-1" } ],
                    [ "...", "NAJTH2BBXBANRBWAISBSRU2VKMQC6JCDORUCUEI", { "region": "us-west-2", "label": "Instance-2" } ],
                    [ "...", "3QZSP3Z7DYHJ3IEK6QB3KRELWSIFZ65SZGXPXCY", { "region": "us-west-2", "label": "Instance-3" } ],
                    [ "...", "KM42T5HH4LMUKVAHGUIGS3F6XZRVHXOOVCAWO4Y", { "region": "us-west-2", "label": "Instance-4" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Instance - Free memory",
                "stat": "Average",
                "period": 300
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 27,
            "x": 0,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y'\n | fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-1a",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 27,
            "x": 6,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I'\n | fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-1b",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 27,
            "x": 12,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-EU72NS7WHMU7QR3JJB3TEZOCVDV5KSVTDIPH2GI'\n | fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-1c",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 27,
            "x": 18,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-IBIMFFVUNNPOPO4GJ7DA6IKD265XV7HDEOXCSXI'\n | fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-2",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 34,
            "x": 6,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-2' | filter @logStream = 'dms-task-PMX26H4RS76ZT4U5ZGA5OWPIDSXUIRKQ7H7QKQY'\n| fields @timestamp as timestamp, @message as message\n| filter @message like /]E:|]W:Error|recoverable error|archived Redo log|ORA-01406|Error_State|Failed|ORA-|constraint|error|buffer|Task is running|Error/ \n| filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n| filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-4",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 34,
            "x": 18,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-2' | filter @logStream = 'dms-task-W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA'\n| fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-6",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 34,
            "x": 12,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-2' | filter @logStream = 'dms-task-W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA'\n| fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-5",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 34,
            "x": 0,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-3' | filter @logStream = 'dms-task-3TBXL6Y7JEDSFRZCLQAY7XHZLYCOUT6FCZGB7IY'\n | fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-3",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 41,
            "x": 0,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-YQD6HCU3LEMCB4K66BO5IWHZPWB5P2ZGLTIIJPY'\n| fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-7a",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 41,
            "x": 6,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-TK5HMZZYS5USWRZ4ZXUNXA656GEQSEVLMOIBBJQ'\n| fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-7b",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 41,
            "x": 12,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-QSHKZUDA3JRCIHLQ2VUPYX2HIQCBDP43QRCOVHA'\n| fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-7c",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 11,
            "y": 7,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CDCLatencySource", "ReplicationInstanceIdentifier", "dms-replication-instance-4", "ReplicationTaskIdentifier", "2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y", { "label": "Task-1a", "region": "us-west-2" } ],
                    [ "...", "M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I", { "label": "Task-1b", "region": "us-west-2" } ],
                    [ "...", "EU72NS7WHMU7QR3JJB3TEZOCVDV5KSVTDIPH2GI", { "label": "Task-1c", "region": "us-west-2" } ],
                    [ "...", "IBIMFFVUNNPOPO4GJ7DA6IKD265XV7HDEOXCSXI", { "label": "Task-2", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-3", ".", "3TBXL6Y7JEDSFRZCLQAY7XHZLYCOUT6FCZGB7IY", { "label": "Task-3", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-2", ".", "PMX26H4RS76ZT4U5ZGA5OWPIDSXUIRKQ7H7QKQY", { "label": "Task-4", "region": "us-west-2" } ],
                    [ "...", "W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA", { "label": "Task-5", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "YRB2OMJSRTA25FW5WJ5NL7KEP5NODD7GM3WIW2Q", { "label": "Task-6", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-4", ".", "YQD6HCU3LEMCB4K66BO5IWHZPWB5P2ZGLTIIJPY", { "label": "Task-7a", "region": "us-west-2" } ],
                    [ "...", "TK5HMZZYS5USWRZ4ZXUNXA656GEQSEVLMOIBBJQ", { "label": "Task-7b", "region": "us-west-2" } ],
                    [ "...", "QSHKZUDA3JRCIHLQ2VUPYX2HIQCBDP43QRCOVHA", { "label": "Task-7c", "region": "us-west-2" } ],
                    [ "...", "UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI", { "label": "Task-7d", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "6KIJFS3AGMWUVEEDMHO3CVDGN2VYB6DAY3XN3CQ", { "label": "Task-8", "region": "us-west-2" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Task - CDCLatencySource",
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        },
        {
            "height": 7,
            "width": 13,
            "y": 7,
            "x": 11,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CDCLatencyTarget", "ReplicationInstanceIdentifier", "dms-replication-instance-4", "ReplicationTaskIdentifier", "2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y", { "label": "Task-1a", "region": "us-west-2" } ],
                    [ "...", "M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I", { "label": "Task-1b", "region": "us-west-2" } ],
                    [ "...", "EU72NS7WHMU7QR3JJB3TEZOCVDV5KSVTDIPH2GI", { "label": "Task-1c", "region": "us-west-2" } ],
                    [ "...", "IBIMFFVUNNPOPO4GJ7DA6IKD265XV7HDEOXCSXI", { "label": "Task-2", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-3", ".", "3TBXL6Y7JEDSFRZCLQAY7XHZLYCOUT6FCZGB7IY", { "label": "Task-3", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-2", ".", "PMX26H4RS76ZT4U5ZGA5OWPIDSXUIRKQ7H7QKQY", { "label": "Task-4", "region": "us-west-2" } ],
                    [ "...", "W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA", { "label": "Task-5", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "YRB2OMJSRTA25FW5WJ5NL7KEP5NODD7GM3WIW2Q", { "label": "Task-6", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-4", ".", "YQD6HCU3LEMCB4K66BO5IWHZPWB5P2ZGLTIIJPY", { "label": "Task-7a", "region": "us-west-2" } ],
                    [ "...", "TK5HMZZYS5USWRZ4ZXUNXA656GEQSEVLMOIBBJQ", { "label": "Task-7b", "region": "us-west-2" } ],
                    [ "...", "QSHKZUDA3JRCIHLQ2VUPYX2HIQCBDP43QRCOVHA", { "label": "Task-7c", "region": "us-west-2" } ],
                    [ "...", "UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI", { "label": "Task-7d", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "6KIJFS3AGMWUVEEDMHO3CVDGN2VYB6DAY3XN3CQ", { "label": "Task-8", "region": "us-west-2" } ]
                ],
                "view": "timeSeries",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Task - CDCLatencyTarget",
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 41,
            "x": 18,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-4' | filter @logStream = 'dms-task-UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI'\n| fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-7d",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 6,
            "y": 48,
            "x": 0,
            "type": "log",
            "properties": {
                "query": "SOURCE 'dms-tasks-dms-replication-instance-1' | filter @logStream = 'dms-task-6KIJFS3AGMWUVEEDMHO3CVDGN2VYB6DAY3XN3CQ'\n| fields @timestamp as timestamp, @message as message\n | filter @message like /]E:|]W:|exceeded|lmit|paused|resumed|Error|recoverable error|sequence|archived Redo log|Reload for table|ORA-01406|Error_State|ORA-|Task is running/ \n | filter @message not like /Page validate|Will try to reattach|data handler|Start processing online Redo log sequence|Start processing archived Redo log sequence|Next table to load|During startup error state is set to '1' and expiration time is already set./\n | filter @message not like /Start loading table|Start loading segment|Unload finished for table|Unload finished for segment|Query status='Success'|Calculated batch used for UNLOAD size|Total load time|End load handler time|Reading from source is paused|Reading from source is resumed/\n | sort @timestamp desc\n | limit 20",
                "region": "us-west-2",
                "stacked": false,
                "title": "DMS Log Task-8",
                "view": "table"
            }
        },
        {
            "height": 7,
            "width": 11,
            "y": 14,
            "x": 0,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CDCThroughputRowsSource", "ReplicationInstanceIdentifier", "dms-replication-instance-4", "ReplicationTaskIdentifier", "2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y", { "label": "Task-1a", "region": "us-west-2" } ],
                    [ "...", "M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I", { "label": "Task-1b", "region": "us-west-2" } ],
                    [ "...", "EU72NS7WHMU7QR3JJB3TEZOCVDV5KSVTDIPH2GI", { "label": "Task-1c", "region": "us-west-2" } ],
                    [ "...", "IBIMFFVUNNPOPO4GJ7DA6IKD265XV7HDEOXCSXI", { "label": "Task-2", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-3", ".", "3TBXL6Y7JEDSFRZCLQAY7XHZLYCOUT6FCZGB7IY", { "label": "Task-3", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-2", ".", "PMX26H4RS76ZT4U5ZGA5OWPIDSXUIRKQ7H7QKQY", { "label": "Task-4", "region": "us-west-2" } ],
                    [ "...", "W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA", { "label": "Task-5", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "YRB2OMJSRTA25FW5WJ5NL7KEP5NODD7GM3WIW2Q", { "label": "Task-6", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-4", ".", "YQD6HCU3LEMCB4K66BO5IWHZPWB5P2ZGLTIIJPY", { "label": "Task-7a", "region": "us-west-2" } ],
                    [ "...", "TK5HMZZYS5USWRZ4ZXUNXA656GEQSEVLMOIBBJQ", { "label": "Task-7b", "region": "us-west-2" } ],
                    [ "...", "QSHKZUDA3JRCIHLQ2VUPYX2HIQCBDP43QRCOVHA", { "label": "Task-7c", "region": "us-west-2" } ],
                    [ "...", "UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI", { "label": "Task-7d", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "6KIJFS3AGMWUVEEDMHO3CVDGN2VYB6DAY3XN3CQ", { "label": "Task-8", "region": "us-west-2" } ]
                ],
                "view": "singleValue",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Task - CDCRowsSource",
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        },
        {
            "height": 7,
            "width": 13,
            "y": 14,
            "x": 11,
            "type": "metric",
            "properties": {
                "metrics": [
                    [ "AWS/DMS", "CDCThroughputRowsTarget", "ReplicationInstanceIdentifier", "dms-replication-instance-4", "ReplicationTaskIdentifier", "2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y", { "label": "Task-1a", "region": "us-west-2" } ],
                    [ "...", "M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I", { "label": "Task-1b", "region": "us-west-2" } ],
                    [ "...", "EU72NS7WHMU7QR3JJB3TEZOCVDV5KSVTDIPH2GI", { "label": "Task-1c", "region": "us-west-2" } ],
                    [ "...", "IBIMFFVUNNPOPO4GJ7DA6IKD265XV7HDEOXCSXI", { "label": "Task-2", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-3", ".", "3TBXL6Y7JEDSFRZCLQAY7XHZLYCOUT6FCZGB7IY", { "label": "Task-3", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-2", ".", "PMX26H4RS76ZT4U5ZGA5OWPIDSXUIRKQ7H7QKQY", { "label": "Task-4", "region": "us-west-2" } ],
                    [ "...", "W4LAG4YMR4KE527IZOP74BKQQQVVFIFEQQRW5EA", { "label": "Task-5", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-1", ".", "YRB2OMJSRTA25FW5WJ5NL7KEP5NODD7GM3WIW2Q", { "label": "Task-6", "region": "us-west-2" } ],
                    [ "...", "dms-replication-instance-4", ".", "YQD6HCU3LEMCB4K66BO5IWHZPWB5P2ZGLTIIJPY", { "label": "Task-7a", "region": "us-west-2" } ],
                    [ "...", "TK5HMZZYS5USWRZ4ZXUNXA656GEQSEVLMOIBBJQ", { "label": "Task-7b", "region": "us-west-2" } ],
                    [ "...", "QSHKZUDA3JRCIHLQ2VUPYX2HIQCBDP43QRCOVHA", { "label": "Task-7c", "region": "us-west-2" } ],
                    [ "...", "UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI", { "label": "Task-7d", "region": "us-west-2" } ],
                    [ ".", "CDCLatencyTarget", ".", "dms-replication-instance-1", ".", "6KIJFS3AGMWUVEEDMHO3CVDGN2VYB6DAY3XN3CQ", { "label": "Task-8", "region": "us-west-2" } ]
                ],
                "view": "singleValue",
                "stacked": false,
                "region": "us-west-2",
                "title": "DMS Task - CDCRowsTarget",
                "stat": "Average",
                "period": 300,
                "sparkline": true
            }
        }
    ]
}