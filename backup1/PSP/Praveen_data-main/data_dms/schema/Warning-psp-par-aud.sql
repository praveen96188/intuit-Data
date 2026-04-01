aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name  'FreeableMemory-Warning - psp-par-aud' \
--alarm-description 'Alarm/page when # of db FreeableMemory exceed this threshold' \
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
--threshold 108111600640 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name  'WriteIOPS-Warning - psp-par-aud' \
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
--threshold 30000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name  'DatabaseConnections-Warning - psp-par-aud' \
--alarm-description 'Alarm/page when # of db CPU utilization exceed this threshold' \
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
--threshold 1000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name  'CPUUtilization-Warning - psp-par-aud' \
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
--threshold 50 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name  'FreeLocalStorage-Warning - psp-par-aud' \
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
--threshold 214748364800 \
--comparison-operator 'LessThanOrEqualToThreshold' \
--treat-missing-data 'missing'

aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name  'ReadIOPS-Warning - psp-par-aud' \
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
--threshold 100000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'


aws cloudwatch put-metric-alarm \
--region us-west-2 --alarm-name  'ReplicationSlotDiskUsage-Warning - psp-par-aud' \
--alarm-description 'Alarm/page when # of db CPU utilization exceed this threshold' \
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
--threshold 536870912000 \
--comparison-operator 'GreaterThanThreshold' \
--treat-missing-data 'missing'



