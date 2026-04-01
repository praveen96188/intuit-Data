--west
psql -h ppsp-pds-db.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -U postgres -p 5432 -d pdsibobdb
.4xSM0vHYTlP^nftK0tvv1Y.j5kZBg

psql -h ppsp-pds-uw02.cluster-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com -p 5432 -U postgres -d ppdspg02


--east
psql -h ppsp-pds-dbdr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com -U postgres -p 5432 -d pdsibobdb

psql -h ppsp-pds-uw02dr.cluster-cxph5rnzesrt.us-east-2.rds.amazonaws.com -U postgres -d ppdspg02 -p 5432



insert into psparc.gg_heartbeat values('test_1',current_timestamp);


INTUADMIN/"changeme"@'ppsphp01.sbg-psp-ppd.a.intuit.com:1521/ppsphp01'

INTUADMIN/"changeme"@'ppsphp05.ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:1521/ppsphp05'

select * from pspadm.gg_heartbeat;


GGSCI (ggppspo2p1.sbg-psp-ppd.a.intuit.com as ggt@PPSPHP01) 21> sh ls -ltr /u01/ogg/ppspo2p1/oracle/21.9.0.0.0/dirdat/pd1pg02/
[oracle@ggppspo2p1 21.9.0.0.0]$ ls -ltr /u01/ogg/ppspo2p1/oracle/21.9.0.0.0/dirdat/pd1pg02/

--switch east to west
--monolith

aws rds --region us-east-2 \
   switchover-global-cluster --global-cluster-identifier ppsp-pds-uw02-global \
  --target-db-cluster-identifier arn:aws:rds:us-west-2:152430470825:cluster:ppsp-pds-uw02

--audit

aws rds --region us-east-2 \
   switchover-global-cluster --global-cluster-identifier ppsp-pds-db-global \
  --target-db-cluster-identifier arn:aws:rds:us-west-2:152430470825:cluster:ppsp-pds-db




----switch west to east
--monolith

aws rds --region us-west-2 \
   switchover-global-cluster --global-cluster-identifier ppsp-pds-uw02-global \
  --target-db-cluster-identifier arn:aws:rds:us-east-2:152430470825:cluster:ppsp-pds-uw02dr

--audit

aws rds --region us-west-2 \
   switchover-global-cluster --global-cluster-identifier ppsp-pds-db-global \
  --target-db-cluster-identifier arn:aws:rds:us-east-2:152430470825:cluster:ppsp-pds-dbdr

select * from pg_replication_slots;

--tarminate connection
SELECT pg_terminate_backend();

SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'ppdspg02' and pg_stat_activity.usename='pspapp'
  AND pid <> pg_backend_pid();

SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'pdsibobdb' and pg_stat_activity.usename='pspadm_readwrite_role'
  AND pid <> pg_backend_pid();



---check active connections
SELECT datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,application_name,count(*)
from pg_stat_activity
where pid != pg_backend_pid() and usename not in ('postgres','rdsadmin','ggs')
group by datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost'),application_name
order by usename;

SELECT datname as db,usename as
    username,client_addr as machine, count(*) FROM pg_stat_activity
where usename not in ('rdsadmin')
group by db,usename,machine;


select pid, 
       usename, 
       pg_blocking_pids(pid) as blocked_by, 
       query as blocked_query
from pg_stat_activity
where cardinality(pg_blocking_pids(pid)) > 0;





--check aurora global replication lag

select * from aurora_global_db_status();
select * from aurora_global_db_instance_status();





. /l/oggpg
oggpg
ggsci
info all
. /l/ogg
ogg
ggsci

--Cleanup GG processes for C2=>B, C2=>D2
--Host - ggppspo2p2.sbg-psp-ppd.a.intuit.com
obey ./diroby/dblogin_c2.oby  
stop EPD1PG02
stop EPDSPG02
stop PPDSHP01
 
delete EPD1PG02
UNREGISTER EXTRACT EPD1PG02 with database ppdspg02
delete EPDSPG02
UNREGISTER EXTRACT EPDSPG02 with database ppdspg02
delete PPDSHP01

--delete replicats

Host : ggppspo2p1.sbg-psp-ppd.a.intuit.com
dblogin USERIDALIAS ggtarget_hash_pds
stop RDPG5
stop RDPG6
stop RPDSPG5*
obey ./diroby/delete_replicats.oby
 
Host : ggppspo2p2.sbg-psp-ppd.a.intuit.com
dblogin USERIDALIAS ggtarget_ppsphp05_pds
stop RDPG2
stop RDPG3
stop RPDSPG2*
obey ./diroby/delete_replicats.oby



--rename trail folders and create new with same name for reverse replication
Host : ggppspo2p2.sbg-psp-ppd.a.intuit.com
. /l/oggpg
oggpg
mv pd1pg02 pd1pg02_18Dec
mv ppdspg02 ppdspg02_18Dec
 
mkdir pd1pg02
mkdir ppdspg02
 
Host : ggppspo2p1.sbg-psp-ppd.a.intuit.com
. /l/ogg
ogg
mv pd1pg02 pd1pg02_18Dec
mkdir pd1pg02


--Switchover to Postgres (us-east) via aws console

--Create & Start Extract on C2 in us-east

Hostname - ggpspe2eue.sbg-psp-ppd.a.intuit.com

. /l/oggpg

Register Extract

obey ./diroby/dblogin_c2.oby
register extract EPD1PG02 with database ppdspg02
Add Extract

obey ./diroby/create_epd1pg02.oby
start EPD1PG02

--Test with insert on heartbeat table to ensure its extracting data from C2

insert into pspadm.gg_heartbeat values('C2_to_B_18Dec',current_timestamp);
  
stats EPD1PG02

--Create Pumps from C2 (us-east)

Hostname - ggpspe2eue.sbg-psp-ppd.a.intuit.com

obey ./diroby/create_ppdspg01.oby
info ppdspg01
start ppdspg01
stats ppdspg01
 
obey ./diroby/create_ppdspg02.oby
info ppdspg02
start ppdspg02
stats ppdspg02

--create and start replication from C2(us-east) to B, D2

Host : ggppspo2p2.sbg-psp-ppd.a.intuit.com

. /l/ogg
dblogin useridalias ggtarget_ppsphp05_pds
obey ./diroby/create_replicat_ppsphp05_pds.oby
obey ./diroby/create_coordinated_replicat_ppsphp05_pds.oby
obey ./diroby/register_replicat.oby
Host : ggppspo2p1.sbg-psp-ppd.a.intuit.com 

. /l/ogg
dblogin useridalias ggtarget_hash_pds
obey ./diroby/create_replicats.oby
obey ./diroby/create_coordinated_replicats.oby
obey ./diroby/register_replicat.oby


--Validate replication

For C2->B

start RPPG6
stats RPPG6
For C2→D2

start RDPG3
stats RDPG3



GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.gg_heartbeat  TO pspadm_readwrite_role;
GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_qbdt_request_info  TO pspadm_readwrite_role;
GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_company_policy  TO pspadm_readwrite_role;
GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_company_qbdt_pitem  TO pspadm_readwrite_role;
GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_employee_deduction  TO pspadm_readwrite_role;
GRANT SELECT, INSERT, DELETE, UPDATE ON pspadm.psp_hcm401k_policy  TO pspadm_readwrite_role;


insert into pspadm.gg_heartbeat values('automon', current_timestamp);




