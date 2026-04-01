sqlplus intuadmin/"changeme"@'pspe2euw-new.sbg-psp-ppd.a.intuit.com:1521/pspe2euw' << EOF
set echo on
set feed on
set time on
set timi on

select INSTANCE_NAME, HOST_NAME from v\$instance;

@$1 $2
exit

EOF
