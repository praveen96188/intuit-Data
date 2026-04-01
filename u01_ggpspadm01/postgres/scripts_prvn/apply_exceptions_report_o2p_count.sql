select count(*)
from awsdms_control_table.awsdms_apply_exceptions
where "ERROR_TIME" between current_timestamp-interval '1 hour' and current_timestamp;

select count(*)
from awsdms_control_table.awsdms_apply_exceptions;

