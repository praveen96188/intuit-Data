select count(*) from awsdms_control_table.awsdms_validation_failures_v1;


select count(*)
from awsdms_control_table.awsdms_validation_failures_v1
where "FAILURE_TIME" between current_timestamp-interval '1 hour' and current_timestamp;
