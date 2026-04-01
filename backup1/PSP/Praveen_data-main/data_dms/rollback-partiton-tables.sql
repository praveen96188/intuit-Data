--DMS rollback partition steps



1.Drop 2 tables new  partition in C3.
2.create 2 tables with range partition.
3.create new task full-load+ CDC from B-C3 (full load done)
4.create full load + CDC (C3-D3)
5.create indexes on C3.



--C2-D2prime

1.stop GG replication B-C2
2.ensure  both tables are range in C2 .
3.drop exiting task6a.
4. create new task6a with range partition table

./dms_cdc.sh ppdspg02 ppsphp07 task6a arn:aws:dms:us-west-2:152430470825:rep:55TRK6FXIG7T7AT4XOPWAL3LWME55PE4OMGHO3I
--tables C3



\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

CREATE TABLE pspadm.psp_entitlement_message(
    entitlement_message_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    entitlement_offering_code CHARACTER VARYING(20),
    order_number CHARACTER VARYING(20),
    message_bkp TEXT,
    license_number CHARACTER VARYING(20),
    status CHARACTER VARYING(255),
    token NUMERIC(19,0),
    event_reason CHARACTER VARYING(50),
    failure_count integer,
    last_failure_message CHARACTER VARYING(1000),
    message_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    expiration_timestamp TIMESTAMP(6) WITHOUT TIME ZONE,
    message TEXT,
    message_enc TEXT
) 
   PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

--Partition tables 

CREATE TABLE pspadm.psp_entitlement_message_2012
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM (MINVALUE) TO ('2013-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2013
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2014-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2014
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2015-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2015
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2016-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2016
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2017-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2017
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2018-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2018
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2019-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2019
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2020-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2020
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2021-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2021
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2022-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2022
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2023
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2024-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2024
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2025-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2025
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2026
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2026-01-01 00:00:00') TO (MAXVALUE);


--PK

ALTER TABLE pspadm.psp_entitlement_message_2012 ADD CONSTRAINT psp_entitlement_message_2012_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2013 ADD CONSTRAINT psp_entitlement_message_2013_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2014 ADD CONSTRAINT psp_entitlement_message_2014_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2015 ADD CONSTRAINT psp_entitlement_message_2015_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2016 ADD CONSTRAINT psp_entitlement_message_2016_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2017 ADD CONSTRAINT psp_entitlement_message_2017_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2018 ADD CONSTRAINT psp_entitlement_message_2018_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2019 ADD CONSTRAINT psp_entitlement_message_2019_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2020 ADD CONSTRAINT psp_entitlement_message_2020_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2021 ADD CONSTRAINT psp_entitlement_message_2021_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2022 ADD CONSTRAINT psp_entitlement_message_2022_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2023 ADD CONSTRAINT psp_entitlement_message_2023_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2024 ADD CONSTRAINT psp_entitlement_message_2024_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2025 ADD CONSTRAINT psp_entitlement_message_2025_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2026 ADD CONSTRAINT psp_entitlement_message_2026_pk PRIMARY KEY (entitlement_message_seq, realm_id);


--psp_paycheck_usage

CREATE TABLE pspadm.psp_paycheck_usage(
    paycheck_usage_seq CHARACTER VARYING(255) NOT NULL,
    version integer,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    paycheck_date TIMESTAMP(6) WITHOUT TIME ZONE,
    check_number CHARACTER VARYING(11),
    source_paycheck_id CHARACTER VARYING(50),
    transaction_id CHARACTER VARYING(50),
    paycheck_status_code CHARACTER VARYING(255),
    bill_fk CHARACTER VARYING(255),
    employee_usage_fk CHARACTER VARYING(255),
    reason_for_free_charge CHARACTER VARYING(255),
    company_fk CHARACTER VARYING(255) 
)
   PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

--Partition tables 

CREATE TABLE pspadm.psp_paycheck_usage_2012
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM (MINVALUE) TO ('2013-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2013
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2014-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2014
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2015-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2015
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2016-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2016
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2017-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2017
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2018-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2018
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2019-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2019
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2020-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2020
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2021-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2021
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2022-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2022
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2023
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2024-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2024
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2025-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2025
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2026
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2026-01-01 00:00:00') TO (MAXVALUE);

--PK

ALTER TABLE pspadm.psp_paycheck_usage_2012 ADD CONSTRAINT psp_paycheck_usage_2012_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2013 ADD CONSTRAINT psp_paycheck_usage_2013_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2014 ADD CONSTRAINT psp_paycheck_usage_2014_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2015 ADD CONSTRAINT psp_paycheck_usage_2015_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2016 ADD CONSTRAINT psp_paycheck_usage_2016_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2017 ADD CONSTRAINT psp_paycheck_usage_2017_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2018 ADD CONSTRAINT psp_paycheck_usage_2018_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2019 ADD CONSTRAINT psp_paycheck_usage_2019_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2020 ADD CONSTRAINT psp_paycheck_usage_2020_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2021 ADD CONSTRAINT psp_paycheck_usage_2021_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2022 ADD CONSTRAINT psp_paycheck_usage_2022_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2023 ADD CONSTRAINT psp_paycheck_usage_2023_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2024 ADD CONSTRAINT psp_paycheck_usage_2024_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2025 ADD CONSTRAINT psp_paycheck_usage_2025_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2026 ADD CONSTRAINT psp_paycheck_usage_2026_pk PRIMARY KEY (paycheck_usage_seq, realm_id);



alter table pspadm.psp_entitlement_message RENAME to psp_entitlement_message_new;
alter table pspadm.psp_entitlement_message_old RENAME to psp_entitlement_message;
alter table pspadm.psp_entitlement_message_2012_old RENAME to psp_entitlement_message_2012;
alter table pspadm.psp_entitlement_message_2013_old RENAME to psp_entitlement_message_2013;
alter table pspadm.psp_entitlement_message_2014_old RENAME to psp_entitlement_message_2014;
alter table pspadm.psp_entitlement_message_2015_old RENAME to psp_entitlement_message_2015;
alter table pspadm.psp_entitlement_message_2016_old RENAME to psp_entitlement_message_2016;
alter table pspadm.psp_entitlement_message_2017_old RENAME to psp_entitlement_message_2017;
alter table pspadm.psp_entitlement_message_2018_old RENAME to psp_entitlement_message_2018;
alter table pspadm.psp_entitlement_message_2019_old RENAME to psp_entitlement_message_2019;
alter table pspadm.psp_entitlement_message_2020_old RENAME to psp_entitlement_message_2020;
alter table pspadm.psp_entitlement_message_2021_old RENAME to psp_entitlement_message_2021;
alter table pspadm.psp_entitlement_message_2022_old RENAME to psp_entitlement_message_2022;
alter table pspadm.psp_entitlement_message_2023_old RENAME to psp_entitlement_message_2023;
alter table pspadm.psp_entitlement_message_2024_old RENAME to psp_entitlement_message_2024;



alter table pspadm.psp_paycheck_usage RENAME to psp_paycheck_usage_new;
alter table pspadm.psp_paycheck_usage_2012 RENAME to psp_paycheck_usage_2012_new;
alter table pspadm.psp_paycheck_usage_2013 RENAME to psp_paycheck_usage_2013_new;
alter table pspadm.psp_paycheck_usage_2014 RENAME to psp_paycheck_usage_2014_new;
alter table pspadm.psp_paycheck_usage_2015 RENAME to psp_paycheck_usage_2015_new;
alter table pspadm.psp_paycheck_usage_2016 RENAME to psp_paycheck_usage_2016_new;
alter table pspadm.psp_paycheck_usage_2017 RENAME to psp_paycheck_usage_2017_new;
alter table pspadm.psp_paycheck_usage_2018 RENAME to psp_paycheck_usage_2018_new;
alter table pspadm.psp_paycheck_usage_2019 RENAME to psp_paycheck_usage_2019_new;
alter table pspadm.psp_paycheck_usage_2020 RENAME to psp_paycheck_usage_2020_new;
alter table pspadm.psp_paycheck_usage_2021 RENAME to psp_paycheck_usage_2021_new;
alter table pspadm.psp_paycheck_usage_2022 RENAME to psp_paycheck_usage_2022_new;
alter table pspadm.psp_paycheck_usage_2023 RENAME to psp_paycheck_usage_2023_new;
alter table pspadm.psp_paycheck_usage_2024 RENAME to psp_paycheck_usage_2024_new;


alter table pspadm.psp_paycheck_usage_old RENAME to psp_paycheck_usage;
alter table pspadm.psp_paycheck_usage_2012_old RENAME to psp_paycheck_usage_2012;
alter table pspadm.psp_paycheck_usage_2013_old RENAME to psp_paycheck_usage_2013;
alter table pspadm.psp_paycheck_usage_2014_old RENAME to psp_paycheck_usage_2014;
alter table pspadm.psp_paycheck_usage_2015_old RENAME to psp_paycheck_usage_2015;
alter table pspadm.psp_paycheck_usage_2016_old RENAME to psp_paycheck_usage_2016;
alter table pspadm.psp_paycheck_usage_2017_old RENAME to psp_paycheck_usage_2017;
alter table pspadm.psp_paycheck_usage_2018_old RENAME to psp_paycheck_usage_2018;
alter table pspadm.psp_paycheck_usage_2019_old RENAME to psp_paycheck_usage_2019;
alter table pspadm.psp_paycheck_usage_2020_old RENAME to psp_paycheck_usage_2020;
alter table pspadm.psp_paycheck_usage_2021_old RENAME to psp_paycheck_usage_2021;
alter table pspadm.psp_paycheck_usage_2022_old RENAME to psp_paycheck_usage_2022;
alter table pspadm.psp_paycheck_usage_2023_old RENAME to psp_paycheck_usage_2023;
alter table pspadm.psp_paycheck_usage_2024_old RENAME to psp_paycheck_usage_2024;




---psp_entitlement_message

CREATE TABLE pspadm.psp_entitlement_message_2025
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2026
        PARTITION OF pspadm.psp_entitlement_message
        FOR VALUES FROM ('2026-01-01 00:00:00') TO (MAXVALUE);

ALTER TABLE pspadm.psp_entitlement_message_2025 ADD CONSTRAINT psp_entitlement_message_2025_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE pspadm.psp_entitlement_message_2026 ADD CONSTRAINT psp_entitlement_message_2026_pk PRIMARY KEY (entitlement_message_seq, realm_id);


--psp_paycheck_usage

CREATE TABLE pspadm.psp_paycheck_usage_2025
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2026
        PARTITION OF pspadm.psp_paycheck_usage
        FOR VALUES FROM ('2026-01-01 00:00:00') TO (MAXVALUE);

ALTER TABLE pspadm.psp_paycheck_usage_2025 ADD CONSTRAINT psp_paycheck_usage_2025_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE pspadm.psp_paycheck_usage_2026 ADD CONSTRAINT psp_paycheck_usage_2026_pk PRIMARY KEY (paycheck_usage_seq, realm_id);


--psp_entity_update

CREATE TABLE pspadm.psp_entity_update_m012024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2024-02-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m022024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-02-01 00:00:00') TO ('2024-03-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m032024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-03-01 00:00:00') TO ('2024-04-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m042024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-04-01 00:00:00') TO ('2024-05-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m052024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-05-01 00:00:00') TO ('2024-06-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m062024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-06-01 00:00:00') TO ('2024-07-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m072024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-07-01 00:00:00') TO ('2024-08-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m082024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-08-01 00:00:00') TO ('2024-09-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m092024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-09-01 00:00:00') TO ('2024-10-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m102024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-10-01 00:00:00') TO ('2024-11-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m112024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-11-01 00:00:00') TO ('2024-12-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m122024
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2024-12-01 00:00:00') TO ('2025-01-01 00:00:00');




--2025
CREATE TABLE pspadm.psp_entity_update_m012025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2025-02-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m022025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-02-01 00:00:00') TO ('2025-03-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m032025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-03-01 00:00:00') TO ('2025-04-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m042025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-04-01 00:00:00') TO ('2025-05-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m052025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-05-01 00:00:00') TO ('2025-06-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m062025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-06-01 00:00:00') TO ('2025-07-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m072025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-07-01 00:00:00') TO ('2025-08-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m082025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-08-01 00:00:00') TO ('2025-09-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m092025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-09-01 00:00:00') TO ('2025-10-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m102025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-10-01 00:00:00') TO ('2025-11-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m112025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-11-01 00:00:00') TO ('2025-12-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m122025
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2025-12-01 00:00:00') TO ('2026-01-01 00:00:00');

--2026

CREATE TABLE pspadm.psp_entity_update_m012026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m022026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m032026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m042026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m052026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m062026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m072026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m082026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m092026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m102026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m112026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');
CREATE TABLE pspadm.psp_entity_update_m122026
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2026-12-01 00:00:00') TO (MAXVALUE);


--2024
ALTER TABLE pspadm.psp_entity_update_m012024 ADD CONSTRAINT psp_entity_update_m012024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022024 ADD CONSTRAINT psp_entity_update_m022024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032024 ADD CONSTRAINT psp_entity_update_m032024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042024 ADD CONSTRAINT psp_entity_update_m042024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m052024 ADD CONSTRAINT psp_entity_update_m052024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m062024 ADD CONSTRAINT psp_entity_update_m062024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m072024 ADD CONSTRAINT psp_entity_update_m072024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m082024 ADD CONSTRAINT psp_entity_update_m082024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m092024 ADD CONSTRAINT psp_entity_update_m092024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m102024 ADD CONSTRAINT psp_entity_update_m102024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m112024 ADD CONSTRAINT psp_entity_update_m112024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122024 ADD CONSTRAINT psp_entity_update_m122024_pk PRIMARY KEY (entity_update_seq, realm_id);

--2025
ALTER TABLE pspadm.psp_entity_update_m012025 ADD CONSTRAINT psp_entity_update_m012025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022025 ADD CONSTRAINT psp_entity_update_m022025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032025 ADD CONSTRAINT psp_entity_update_m032025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042025 ADD CONSTRAINT psp_entity_update_m042025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m052025 ADD CONSTRAINT psp_entity_update_m052025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m062025 ADD CONSTRAINT psp_entity_update_m062025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m072025 ADD CONSTRAINT psp_entity_update_m072025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m082025 ADD CONSTRAINT psp_entity_update_m082025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m092025 ADD CONSTRAINT psp_entity_update_m092025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m102025 ADD CONSTRAINT psp_entity_update_m102025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m112025 ADD CONSTRAINT psp_entity_update_m112025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122025 ADD CONSTRAINT psp_entity_update_m122025_pk PRIMARY KEY (entity_update_seq, realm_id);

--2026
ALTER TABLE pspadm.psp_entity_update_m012026 ADD CONSTRAINT psp_entity_update_m012026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022026 ADD CONSTRAINT psp_entity_update_m022026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032026 ADD CONSTRAINT psp_entity_update_m032026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042026 ADD CONSTRAINT psp_entity_update_m042026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m052026 ADD CONSTRAINT psp_entity_update_m052026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m062026 ADD CONSTRAINT psp_entity_update_m062026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m072026 ADD CONSTRAINT psp_entity_update_m072026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m082026 ADD CONSTRAINT psp_entity_update_m082026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m092026 ADD CONSTRAINT psp_entity_update_m092026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m102026 ADD CONSTRAINT psp_entity_update_m102026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m112026 ADD CONSTRAINT psp_entity_update_m112026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122026 ADD CONSTRAINT psp_entity_update_m122026_pk PRIMARY KEY (entity_update_seq, realm_id);




Alter index psp_entitlement_message_2025_license_number_entitlement_off_idx  RENAME TO psp_entitlement_msg_lcno_eofcd_2025;
Alter index psp_entitlement_message_2025_message_timestamp_event_reason_idx  RENAME TO psp_entitlement_msg_msgts_er_2025;
Alter index psp_entitlement_message_2025_order_number_license_number_idx     RENAME TO psp_entitlement_msg_orno_lcno_2025;
Alter index psp_entitlement_message_2025_modified_date_idx                   RENAME TO idx_ent_msg_mod_date_2025; 
Alter index psp_entitlement_message_2025_token_idx                           RENAME TO psp_entitlement_message_u1_2025;


Alter index psp_entitlement_message_2026_license_number_entitlement_off_idx  RENAME TO psp_entitlement_msg_lcno_eofcd_2026;
Alter index psp_entitlement_message_2026_message_timestamp_event_reason_idx  RENAME TO psp_entitlement_msg_msgts_er_2026;
Alter index psp_entitlement_message_2026_order_number_license_number_idx     RENAME TO psp_entitlement_msg_orno_lcno_2026;
Alter index psp_entitlement_message_2026_modified_date_idx                   RENAME TO idx_ent_msg_mod_date_2026; 
Alter index psp_entitlement_message_2026_token_idx                           RENAME TO psp_entitlement_message_u1_2026;
 



Alter index psp_paycheck_usage_2025_bill_fk_idx    RENAME TO             psp_paycheckusage_fk1_2025;
Alter index psp_paycheck_usage_2025_company_fk_idx  RENAME TO         psp_paycheck_usage_fk1_2025;
Alter index psp_paycheck_usage_2025_employee_usage_fk_idx RENAME TO psp_paycheckusage_fk2_2025;
Alter index psp_paycheck_usage_2025_employee_usage_fk_source_paycheck_i_idx RENAME TO psp_paycheck_usage_u1_2025;
Alter index psp_paycheck_usage_2025_modified_date_idx   RENAME TO idx_pchk_usg_mod_date_2025;
Alter index psp_paycheck_usage_2025_source_paycheck_id_idx  RENAME TO  psp_paycheck_usage_nu1_2025;

Alter index psp_paycheck_usage_2026_bill_fk_idx    RENAME TO             psp_paycheckusage_fk1_2026;
Alter index psp_paycheck_usage_2026_company_fk_idx  RENAME TO         psp_paycheck_usage_fk1_2026;
Alter index psp_paycheck_usage_2026_employee_usage_fk_idx RENAME TO psp_paycheckusage_fk2_2026;
Alter index psp_paycheck_usage_2026_employee_usage_fk_source_paycheck_i_idx RENAME TO psp_paycheck_usage_u1_2026;
Alter index psp_paycheck_usage_2026_modified_date_idx   RENAME TO idx_pchk_usg_mod_date_2026;
Alter index psp_paycheck_usage_2026_source_paycheck_id_idx  RENAME TO  psp_paycheck_usage_nu1_2026;

--Indexes
psp_paycheck_usage
idx_pchk_usg_mod_date
psp_paycheck_usage_fk1
psp_paycheck_usage_nu1
psp_paycheck_usage_u1
psp_paycheckusage_fk1
psp_paycheckusage_fk2

--psp_entitlement_message

idx_ent_msg_mod_date
psp_entitlement_message_u1
psp_entitlement_msg_lcno_eofc
psp_entitlement_msg_msgts_er
psp_entitlement_msg_orno_lcno

--psp_entity_update
idx_ent_upd_mod_date
idx_entityupdate_crdate


psp_entity_update_m012024_created_date_idx

alter index psp_entity_update_m012024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m012024;
alter index psp_entity_update_m012025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m012025;
alter index psp_entity_update_m012026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m012026;
alter index psp_entity_update_m022024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m022024;
alter index psp_entity_update_m022025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m022025;
alter index psp_entity_update_m022026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m022026;
alter index psp_entity_update_m032024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m032024;
alter index psp_entity_update_m032025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m032025;
alter index psp_entity_update_m032026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m032026;
alter index psp_entity_update_m042024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m042024;
alter index psp_entity_update_m042025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m042025;
alter index psp_entity_update_m042026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m042026;
alter index psp_entity_update_m052024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m052024;
alter index psp_entity_update_m052025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m052025;
alter index psp_entity_update_m052026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m052026;
alter index psp_entity_update_m062024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m062024;
alter index psp_entity_update_m062025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m062025;
alter index psp_entity_update_m062026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m062026;
alter index psp_entity_update_m072024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m072024;
alter index psp_entity_update_m072025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m072025;
alter index psp_entity_update_m072026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m072026;
alter index psp_entity_update_m082024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m082024;
alter index psp_entity_update_m082025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m082025;
alter index psp_entity_update_m082026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m082026;
alter index psp_entity_update_m092024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m092024;
alter index psp_entity_update_m092025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m092025;
alter index psp_entity_update_m092026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m092026;
alter index psp_entity_update_m102024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m102024;
alter index psp_entity_update_m102025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m102025;
alter index psp_entity_update_m102026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m102026;
alter index psp_entity_update_m112024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m112024;
alter index psp_entity_update_m112025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m112025;
alter index psp_entity_update_m112026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m112026;
alter index psp_entity_update_m122024_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122024;
alter index psp_entity_update_m122025_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122025;
alter index psp_entity_update_m122026_created_date_idx  RENAME TO   idx_entityupdate_crdate_m122026;


