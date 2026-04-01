sqlplus intuadmin/'changeme'@pspppib1.cjls0bohfgpq.us-west-2.rds.amazonaws.com:1521/pspppib1 << EOF
set echo on
set feed on
set time on
set timi on

select INSTANCE_NAME, HOST_NAME from v\$instance;

@$1 $2
exit

EOF

