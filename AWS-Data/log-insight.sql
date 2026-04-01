fields @timestamp, @message
| sort @timestamp desc
| filter @message like /FATAL|ERROR|WARNING|Fail|error|fail/
| filter @message not like /column "total_time" does not exist|postgresi|ops_user|ipsmonuser|psp_smssync_failure|error_|"user "pspqsfinadmin", database "pspapg02", no encryption"|duration|unaffected changes were applied|no pg_hba.conf entry/
#| filter @message not like /duplicate key value violates unique constraint|column "total_time" does not exist/
#| filter @message not like /_fail|error_count/
| limit 2000


