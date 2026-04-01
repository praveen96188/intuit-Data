aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencySource-pspapg02-prodapgib-pspadm-ibobadm-hcm-Warning' \
--region us-west-2 \
--alarm-description 'CDC source latency greater than 5 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencySource' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-1"},{"Name":"ReplicationTaskIdentifier","Value":"472FH6YRB5FMNMNCOC5FM6AXB4"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 300 \
--comparison-operator 'GreaterThanOrEqualToThreshold'


aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencySource-pspapg02-prodapgib-pspadm-ibobadm-qri-Warning' \
--region us-west-2 \
--alarm-description 'CDC source latency greater than 5 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencySource' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-1"},{"Name":"ReplicationTaskIdentifier","Value":"JR7MQYPQLVDBTMPPQNIIGHRUJY"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 300 \
--comparison-operator 'GreaterThanOrEqualToThreshold'



aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencyTarget-pspapg02-prodapgib-pspadm-ibobadm-hcm-Critical' \
--region us-west-2 \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'CDCLatencyTarget' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-1"},{"Name":"ReplicationTaskIdentifier","Value":"472FH6YRB5FMNMNCOC5FM6AXB4"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'


aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencyTarget-pspapg02-prodapgib-pspadm-ibobadm-qri-Critical' \
--region us-west-2 \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-critical-prod-a-intuit-com' \
--metric-name 'CDCLatencyTarget' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-1"},{"Name":"ReplicationTaskIdentifier","Value":"JR7MQYPQLVDBTMPPQNIIGHRUJY"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'


