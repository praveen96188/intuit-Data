--cloud formation JSON
--Instance size db.r6g.16xlarge 

{
    "Type": "AWS::CloudWatch::Alarm",
    "Properties": {
        "AlarmName": "PGCatalogSize-Critical-psp-prod-ibob",
        "AlarmDescription": "Alarm/page when size of catalog exceed this threshold",
        "ActionsEnabled": true,
        "OKActions": [
            "arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-warning-prod-a-intuit-com"
        ],
        "AlarmActions": [
            "arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-critical-prod-a-intuit-com"
        ],
        "InsufficientDataActions": [],
        "MetricName": "prodapgib_pgcatalog_size",
        "Namespace": "PostgresCatalogSize",
        "Statistic": "Average",
        "Dimensions": [],
        "Period": 300,
        "EvaluationPeriods": 1,
        "DatapointsToAlarm": 1,
        "Threshold": 400,
        "ComparisonOperator": "GreaterThanOrEqualToThreshold"
    }
}

--cloud formation yAML
Type: AWS::CloudWatch::Alarm
Properties:
    AlarmName: PGCatalogSize-Critical-psp-prod-ibob
    AlarmDescription: Alarm/page when size of catalog exceed this threshold
    ActionsEnabled: true
    OKActions:
        - arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-warning-prod-a-intuit-com
    AlarmActions:
        - arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-critical-prod-a-intuit-com
    InsufficientDataActions: []
    MetricName: prodapgib_pgcatalog_size
    Namespace: PostgresCatalogSize
    Statistic: Average
    Dimensions: []
    Period: 300
    EvaluationPeriods: 1
    DatapointsToAlarm: 1
    Threshold: 400
    ComparisonOperator: GreaterThanOrEqualToThreshold
--AWS CLI
aws cloudwatch put-metric-alarm \
--alarm-name 'PGCatalogSize-Critical-psp-prod-ibob' \
--alarm-description 'Alarm/page when size of catalog exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-critical-prod-a-intuit-com' \
--metric-name 'prodapgib_pgcatalog_size' \
--namespace 'PostgresCatalogSize' \
--statistic 'Average' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 400 \
--comparison-operator 'GreaterThanOrEqualToThreshold'

aws cloudwatch put-metric-alarm \
--alarm-name 'PGCatalogSize-Warning-psp-prod-ibob' \
--alarm-description 'Alarm/email when size of catalog exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:apg-db-sbg-psp-warning-prod-a-intuit-com' \
--metric-name 'prodapgib_pgcatalog_size' \
--namespace 'PostgresCatalogSize' \
--statistic 'Average' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 250 \
--comparison-operator 'GreaterThanOrEqualToThreshold'


aws cloudwatch put-metric-alarm \
--alarm-name 'DatabaseMaxTransactionId-Critical-psp-prod-ibob-prodapgib' \
--alarm-description 'Alarm/page when DatabaseMaxTransactionId exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'prodapgib' \
--namespace 'DatabaseMaxTransactionId' \
--statistic 'Average' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 205000000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'DatabaseMaxTransactionId-Warning-psp-prod-ibob-prodapgib' \
--alarm-description 'Alarm/page when DatabaseMaxTransactionId exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'prodapgib' \
--namespace 'DatabaseMaxTransactionId' \
--statistic 'Average' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 203000000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'ReadIOPS-Warning - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db ReadIOPS exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'ReadIOPS' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 100000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'WriteIOPS-Warning - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db WriteIOPS exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:psp-prod-slack' 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' 'arn:aws:sns:us-west-2:893547637742:psp-prod-slack' \
--metric-name 'WriteIOPS' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 30000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'FreeableMemory-Warning - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db FreeableMemory exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'FreeableMemory' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 118111600640 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'DatabaseConnections-Warning - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db CPU utilization exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'DatabaseConnections' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 1000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'CPUUtilization-Warning - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db CPU utilization exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CPUUtilization' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 50 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'FreeLocalStorage-Warning - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db FreeLocalStorage exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'FreeLocalStorage' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 214748364800 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'ReplicationSlotDiskUsage-Warning - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db CPU utilization exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'ReplicationSlotDiskUsage' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 536870912000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--alarm-name 'WriteIOPS-Critical - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db WriteIOPS exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'WriteIOPS' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 35000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'MaximumUsedTransactionIDs-Critical - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db MaximumUsedTransactionIDs exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'MaximumUsedTransactionIDs' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 210000000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'CPUUtilization-Critical - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db CPU utilization exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' 'arn:aws:sns:us-west-2:893547637742:psp-prod-slack' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:psp-prod-slack' 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'CPUUtilization' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 60 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'FreeLocalStorage-Critical - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db FreeLocalStorage exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'FreeLocalStorage' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 107374182400 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'DatabaseConnections-Critical - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db Connections exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'DatabaseConnections' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 1400 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'ReadIOPS-Critical - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db ReadIOPS exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'ReadIOPS' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 150000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--alarm-name 'FreeableMemory-Critical - psp-prod-ibob' \
--alarm-description 'Alarm/page when # of db Freeable Memory exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'FreeableMemory' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-prod-ibob"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 107374182400 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'





