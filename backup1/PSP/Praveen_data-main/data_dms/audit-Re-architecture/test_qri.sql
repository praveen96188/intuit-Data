CREATE OR REPLACE PROCEDURE prvn.PRC_FIX_ENTITY_UPDATE(
START_DATE timestamp, END_DATE timestamp, partition_window interval
)
    LANGUAGE plpgsql AS
$$
DECLARE
    
    PARTITION_END_DATE TIMESTAMP := START_DATE + partition_window ;
    BATCH_COUNT        INTEGER   := 0;
    PROCESSED_RECORDS  INTEGER   := 0;
    RECORD_COUNT       INTEGER   := 246117372;
    MAX_RECORDS        INTEGER   := 100000000;
    COMMIT_SIZE        INTEGER   := 5000;
    start_time         timestamp;
    end_time           timestamp;
    elapsed_interval   INTEGER;
    totalrowsAffected INTEGER :=0;

BEGIN
    WHILE (START_DATE < END_DATE AND RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)
        LOOP
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.
            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS )
                LOOP
                    select CURRENT_TIMESTAMP into start_time;
                    UPDATE prvn.qbdt_request_info_backup_data B
                    SET company_fk = A.company_id
                    FROM (
                             SELECT source_system_transmission_seq, company_id
                             FROM ibobadm.psp_source_system_transmission ai, prvn.qbdt_request_info_backup_data bi
                             WHERE ai.CREATED_DATE between START_DATE AND PARTITION_END_DATE
                               AND ai.source_system_transmission_seq = bi.source_system_transmission_fk
                               AND ai.from_source_system = 'QBDT'
                               AND ai.type IN ('PayrollSubmission', 'UsageSend', 'BalanceFile')
                               AND bi.company_fk IS NULL
                             LIMIT COMMIT_SIZE
                         ) AS A
                    WHERE B.source_system_transmission_fk = A.source_system_transmission_seq
                      AND B.company_fk IS NULL;

                    GET DIAGNOSTICS BATCH_COUNT = ROW_COUNT;
                    COMMIT;
                   

                    select CURRENT_TIMESTAMP into end_time;
                    elapsed_interval := EXTRACT(EPOCH FROM (end_time - start_time));
                    RAISE NOTICE 'Processed % records successfully in % for date % to %', BATCH_COUNT, elapsed_interval, START_DATE, PARTITION_END_DATE;
                     totalrowsAffected := (totalrowsAffected + BATCH_COUNT);
                     IF BATCH_COUNT < COMMIT_SIZE THEN
                         EXIT;
                        END IF;
                    PROCESSED_RECORDS = PROCESSED_RECORDS + BATCH_COUNT;

                END LOOP;

            START_DATE := START_DATE + partition_window;
            PARTITION_END_DATE := START_DATE + partition_window;
        END LOOP;
        RAISE NOTICE 'Total Updated rows %', totalrowsAffected;
END;
$$;



--12:05PM

select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update_year('2015-01-01 00:00:00', '2015-12-31 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;

 call prc_fix_entity_upda_qri_1('2012-01-01 00:00:00', '2025-01-01 00:00:00', '2 hour');


2013-07-29 14:00:00 to 2013-07-29 16:00:00

 select count(*) from ibobadm.psp_qbdt_request_info where created_date between '2013-07-29 14:00:00' and '2013-07-29 16:00:00';


 CREATE unique INDEX concurrently psp_qbdt_request_info_dms ON pspadm.PSP_QBDT_REQUEST_INFO USING BTREE (qbdt_request_info_seq, realm_id);





CREATE OR REPLACE PROCEDURE prvn.PRC_FIX_ENTITY_UPDATE_3(
START_DATE timestamp, END_DATE timestamp, partition_window interval
)
    LANGUAGE plpgsql AS
$$
DECLARE
    
    PARTITION_END_DATE TIMESTAMP := START_DATE + partition_window ;
    BATCH_COUNT        INTEGER   := 0;
    PROCESSED_RECORDS  INTEGER   := 0;
    RECORD_COUNT       INTEGER   := 246117372;
    MAX_RECORDS        INTEGER   := 100000000;
    COMMIT_SIZE        INTEGER   := 5000;
    start_time         timestamp;
    end_time           timestamp;
    elapsed_interval   INTEGER;
    totalrowsAffected INTEGER :=0;

BEGIN
    WHILE (START_DATE < END_DATE AND RECORD_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS)
        LOOP
            BATCH_COUNT := 1; --TO ENTER THE LOOP AT LEAST ONCE FOR A DAY.
            WHILE (BATCH_COUNT > 0 AND PROCESSED_RECORDS < MAX_RECORDS )
                LOOP
                    select CURRENT_TIMESTAMP into start_time;
                    UPDATE prvn.qbdt_request_info_backup_data B
                    SET company_fk = A.company_id
                    FROM (
                             SELECT source_system_transmission_seq, company_id
                             FROM ibobadm.psp_source_system_transmission ai, prvn.qbdt_request_info_backup_data bi
                             WHERE ai.CREATED_DATE between START_DATE AND PARTITION_END_DATE
                               AND ai.source_system_transmission_seq = bi.source_system_transmission_fk
                               AND ai.from_source_system = 'QBDT'
                               AND ai.type IN ('PayrollSubmission', 'UsageSend', 'BalanceFile')
                               AND bi.company_fk IS NULL
                             LIMIT COMMIT_SIZE
                         ) AS A
                    WHERE B.source_system_transmission_fk = A.source_system_transmission_seq
                      AND B.company_fk IS NULL;

                    GET DIAGNOSTICS BATCH_COUNT = ROW_COUNT;
                    COMMIT;
                   

                    select CURRENT_TIMESTAMP into end_time;
                    elapsed_interval := EXTRACT(EPOCH FROM (end_time - start_time));
                    RAISE NOTICE 'Processed % records successfully in % for date % to %', BATCH_COUNT, elapsed_interval, START_DATE, PARTITION_END_DATE;
                     totalrowsAffected := (totalrowsAffected + BATCH_COUNT);
                     IF BATCH_COUNT < COMMIT_SIZE THEN
                         EXIT;
                        END IF;
                    PROCESSED_RECORDS = PROCESSED_RECORDS + BATCH_COUNT;

                END LOOP;

            START_DATE := START_DATE + partition_window;
            PARTITION_END_DATE := START_DATE + partition_window;
        END LOOP;
        RAISE NOTICE 'Total Updated rows %', totalrowsAffected;
END;
$$;



SELECT source_system_transmission_seq, company_id
                             FROM ibobadm.psp_source_system_transmission ai, prvn.qbdt_request_info_backup_data bi
                             WHERE ai.CREATED_DATE between '2015-01-01 00:00:00' and  '2016-01-01 00:00:00'
                               AND ai.source_system_transmission_seq = bi.source_system_transmission_fk
                               AND ai.from_source_system = 'QBDT'
                               AND ai.type IN ('PayrollSubmission', 'UsageSend', 'BalanceFile')
                               AND bi.company_fk IS NULL;



no changes freeable and freelocal storage, replicatioslot Disk change

select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update('2024-01-01 00:00:00', '2024-04-01 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;





select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update_1('2012-01-01 00:00:00', '2013-01-01 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;



select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update_2('2013-01-01 00:00:00', '2014-01-01 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;



select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update_3('2014-01-01 00:00:00', '2015-01-01 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;




--12:05PM

select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update_1('2016-01-01 00:00:00', '2017-01-01 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;



select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update_2('2017-01-01 00:00:00', '2018-01-01 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;



select CURRENT_TIMESTAMP;
call prvn.prc_fix_entity_update_3('2018-01-01 00:00:00', '2019-01-01 00:00:00', '1 hour');
select CURRENT_TIMESTAMP;

select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2012-01-01 00:00:00' and  '2013-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2013-01-01 00:00:00' and  '2014-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2014-01-01 00:00:00' and  '2015-01-01 00:00:00' and company_fk is null;

select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2015-01-01 00:00:00.000' and  '2016-01-01 00:00:00.000' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2016-01-01 00:00:00' and  '2017-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2017-01-01 00:00:00' and  '2018-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2018-01-01 00:00:00' and  '2019-01-01 00:00:00' and company_fk is null;


select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2019-01-01 00:00:00' and  '2020-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2020-01-01 00:00:00' and  '2021-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2021-01-01 00:00:00' and  '2022-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2022-01-01 00:00:00' and  '2023-01-01 00:00:00' and company_fk is null;
select count(*)  from prvn.qbdt_request_info_backup_data where created_date between '2023-01-01 00:00:00' and  '2024-01-01 00:00:00' and company_fk is null;


select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2024-01-01 00:00:00' and  '2024-03-29 00:00:00' and company_fk is  null;




select type,company_id ,from_source_system,CREATED_DATE from ibobadm.psp_source_system_transmission where source_system_transmission_seq in ('5db41971-6fa6-47dc-aa17-80fdfb6a85ba',
 'e68a1870-bc91-4db9-90cc-a4e9601c1984');

select CREATED_DATE  from prvn.qbdt_request_info_backup_data where created_date between '2019-01-01 00:00:00' and  '2020-01-01 00:00:00' and company_fk is null;





select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2012-01-01 00:00:00' and  '2013-01-01 00:00:00' and company_fk is  null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2013-01-01 00:00:00' and  '2014-01-01 00:00:00' and company_fk is not  null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2014-01-01 00:00:00' and  '2015-01-01 00:00:00' and company_fk is null;

select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2015-01-01 00:00:00.000' and  '2016-01-01 00:00:00.000' and company_fk is null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2016-01-01 00:00:00' and  '2017-01-01 00:00:00' and company_fk is null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2017-01-01 00:00:00' and  '2018-01-01 00:00:00' and company_fk is null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2018-01-01 00:00:00' and  '2019-01-01 00:00:00' and company_fk is null;


select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2019-01-01 00:00:00' and  '2020-01-01 00:00:00' and company_fk is null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2020-01-01 00:00:00' and  '2021-01-01 00:00:00' and company_fk is null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2021-01-01 00:00:00' and  '2022-01-01 00:00:00' and company_fk is null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2022-01-01 00:00:00' and  '2023-01-01 00:00:00' and company_fk is null;
select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2023-01-01 00:00:00' and  '2024-01-01 00:00:00' and company_fk is null;


select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2024-01-01 00:00:00' and  '2025-01-01 00:00:00' and company_fk is not null;


select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2012-01-01 00:00:00' and  '2013-01-01 00:00:00' and  company_fk in (select company_fk from );



select count(*)  from ibobadm.psp_qbdt_request_info where created_date between '2024-03-01 00:00:00' and  '2025-01-01 00:00:00' and company_fk is not null;


--Disable the insert 
update pspadm.PSP_SYSTEM_PARAMETER set SYSTEM_PARAMETER_VALUE=false where SYSTEM_PARAMETER_CD='RECORD_REQUEST_INFO';



select 
SELECT c.oid::regclass as table_name,
       greatest(age(c.relfrozenxid),age(t.relfrozenxid)) as age
FROM pg_class c
         LEFT JOIN pg_class t ON c.reltoastrelid = t.oid
WHERE c.relkind IN ('r', 'm') and c.relname='psp_qbdt_request_info';


---Checks;



parapgib=> SELECT datname, age(datfrozenxid) FROM pg_database;
  datname  |    age    
-----------+-----------
 rdsadmin  | 134643747
 parapgib  | 177406839
 template0 | 197977199
 template1 | 197977199
 postgres  | 134639036
(5 rows)



ssh -o ServerAliveInterval=45 -i ~/.ssh/id_rsa -L 127.0.0.1:10540:ppsp-pds-db.cluster-ro-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla@ec2-52-38-34-141.us-west-2.compute.amazonaws.com
ssh -o ServerAliveInterval=45 -i ~/.ssh/id_rsa -L 127.0.0.1:10542:ppsp-pds-uw02.cluster-ro-ccqjgvvo0rwy.us-west-2.rds.amazonaws.com:5432 pnarlagalla@ec2-52-38-34-141.us-west-2.compute.amazonaws.com


insert into pspadm.psp_qbdt_request_info (qbdt_request_info_seq,company_fk,created_date) values ('test1','test345', current_timestamp);







ALTER TABLE ibobadm.psp_qbdt_request_info
ADD column company_fk  CHARACTER VARYING(255);


psp-prod-uw02-vmp-->22,23 
psp-prod-uw02-rpt-->24
psp-prod-uw02-rjf-->23
psp-prod-uw02-dpc-->24
psp-prod-uw02-datalake-->24



export PGPASSWORD="HV32pts0PUD=4lWpicaRyz^og-GKz1"
psql -h psp-prod-ibob.cluster-ro-cjls0bohfgpq.us-west-2.rds.amazonaws.com -d prodapgib -U postgres -p 5432




SELECT datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost') as client_hostname,application_name,count(*)
from pg_stat_activity
where pid != pg_backend_pid() and usename not in ('postgres','rdsadmin','ggs','postgresi') and application_name like 'psp-jss-monitor-deployment-6bdf4595b4-xrvqx'
group by datname,usename,
coalesce(client_hostname, client_addr::text, 'localhost'),application_name
order by usename;







