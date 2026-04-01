aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencyTarget-psphdg02-psp-prod-mon-pspadm-task-1-Critical' \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencyTarget' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-1"},{"Name":"ReplicationTaskIdentifier","Value":"LTGQPKZZEJUP5COOPUMYSDSP4VRXZRAK2BOIHOQ"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'


aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencySource-psphdg02-psp-prod-mon-pspadm-task-1-Critical' \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencySource' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-1"},{"Name":"ReplicationTaskIdentifier","Value":"LTGQPKZZEJUP5COOPUMYSDSP4VRXZRAK2BOIHOQ"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'

aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencySource-psphdg02-psp-prod-mon-pspadm-task-7-Critical' \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencySource' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-2"},{"Name":"ReplicationTaskIdentifier","Value":"PFMENVTZXKOJAWYMMJGECZGGURBYBPFNSQ62O6I"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'


aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencyTarget-psphdg02-psp-prod-mon-pspadm-task-7-Critical' \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencyTarget' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-2"},{"Name":"ReplicationTaskIdentifier","Value":"PFMENVTZXKOJAWYMMJGECZGGURBYBPFNSQ62O6I"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'


aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencySource-psppp01-psphpp06-pspadm-task1a-Critical' \
--alarm-description 'CDC source latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencySource' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-4"},{"Name":"ReplicationTaskIdentifier","Value":"2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'

aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencySource-psppp01-psphpp06-pspadm-task1b-Critical' \
--alarm-description 'CDC source latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencySource' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-4"},{"Name":"ReplicationTaskIdentifier","Value":"M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'



aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencyTarget-psppp01-psphpp06-pspadm-task1a-Critical' \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencyTarget' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-4"},{"Name":"ReplicationTaskIdentifier","Value":"2YT32VX7CHRMJNQCHGA5Z5TLHMKKESSTQS4ZK5Y"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'

aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencyTarget-psppp01-psphpp06-pspadm-task1b-Critical' \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencyTarget' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-4"},{"Name":"ReplicationTaskIdentifier","Value":"M6G4DNBTCU6OW75XKKAQUBUD4DJHPEGDXVRTD2I"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'



aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencySource-psppp01-psphpp06-pspadm-task7d-Critical' \
--alarm-description 'CDC source latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencySource' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-4"},{"Name":"ReplicationTaskIdentifier","Value":"UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'



aws cloudwatch put-metric-alarm \
--alarm-name 'CDCLatencyTarget-psppp01-psphpp06-pspadm-task7d-Critical' \
--alarm-description 'CDC target latency greater than 15 min' \
--actions-enabled \
--ok-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--alarm-actions 'arn:aws:sns:us-west-2:893547637742:dms-sbg-psp-oracle-exit-warning-prod-a-intuit-com' \
--metric-name 'CDCLatencyTarget' \
--namespace 'AWS/DMS' \
--statistic 'Average' \
--dimensions '[{"Name":"ReplicationInstanceIdentifier","Value":"dms-replication-instance-4"},{"Name":"ReplicationTaskIdentifier","Value":"UVQRNBUJSSMK4UZPPRUMQ7FYXBJOOHXXOOE7NWI"}]' \
--period 60 \
--evaluation-periods 2 \
--datapoints-to-alarm 2 \
--threshold 900 \
--comparison-operator 'GreaterThanOrEqualToThreshold'






