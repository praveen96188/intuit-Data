aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name 'WriteIOPS-Critical - psp-par-aud' \
--alarm-description 'Alarm/page when # of db WriteIOPS exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'WriteIOPS' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-par-aud"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 35000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name 'CPUUtilization-Critical - psp-par-aud' \
--alarm-description 'Alarm/page when # of db CPU utilization exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CPUUtilization' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-par-aud"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 60 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name 'FreeLocalStorage-Critical - psp-par-aud' \
--alarm-description 'Alarm/page when # of db FreeLocalStorage exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'FreeLocalStorage' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-par-aud"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 107374182400 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name 'DatabaseConnections-Critical - psp-par-aud' \
--alarm-description 'Alarm/page when # of db Connections exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'DatabaseConnections' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-par-aud"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 1400 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name 'ReplicationSlotDiskUsage-Critical - psp-par-aud' \
--alarm-description 'Alarm/page when # of db replication slots exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'ReplicationSlotDiskUsage' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-par-aud"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 1073741824000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name 'ReadIOPS-Critical - psp-par-aud' \
--alarm-description 'Alarm/page when # of db ReadIOPS exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'ReadIOPS' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-par-aud"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 150000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name 'FreeableMemory-Critical - psp-par-aud' \
--alarm-description 'Alarm/page when # of db Freeable Memory exceed this threshold' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:db-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'FreeableMemory' \
--namespace 'AWS/RDS' \
--statistic 'Average' \
--dimensions '[{"Name":"DBClusterIdentifier","Value":"psp-par-aud"}]' \
--period 300 \
--evaluation-periods 1 \
--datapoints-to-alarm 1 \
--threshold 107374182400 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'

