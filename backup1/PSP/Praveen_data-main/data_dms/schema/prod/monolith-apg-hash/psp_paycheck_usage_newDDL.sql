--to check invalid index
SELECT * FROM pg_class, pg_index WHERE pg_index.indisvalid = false AND pg_index.indexrelid = pg_class.oid;


--Drop constraint on psp_paycheck_usage
Alter table pspadm.psp_paycheck_usage drop constraint psp_paycheck_usage_fk1;
Alter table pspadm.psp_paycheck_usage drop constraint psp_paycheckusage_fk1;
Alter table pspadm.psp_paycheck_usage drop constraint psp_paycheckusage_fk2;


--rename table to _old
\timing
set search_path=pspadm;
alter table pspadm.psp_paycheck_usage RENAME to psp_paycheck_usa_old;
alter table pspadm.psp_paycheck_usage_2012 RENAME to psp_paycheck_usa_3012_old;
alter table pspadm.psp_paycheck_usage_2013 RENAME to psp_paycheck_usa_3013_old;
alter table pspadm.psp_paycheck_usage_2014 RENAME to psp_paycheck_usa_3014_old;
alter table pspadm.psp_paycheck_usage_2015 RENAME to psp_paycheck_usa_3015_old;
alter table pspadm.psp_paycheck_usage_2016 RENAME to psp_paycheck_usa_3016_old;
alter table pspadm.psp_paycheck_usage_2017 RENAME to psp_paycheck_usa_3017_old;
alter table pspadm.psp_paycheck_usage_2018 RENAME to psp_paycheck_usa_3018_old;
alter table pspadm.psp_paycheck_usage_2019 RENAME to psp_paycheck_usa_3019_old;
alter table pspadm.psp_paycheck_usage_2020 RENAME to psp_paycheck_usa_3020_old;
alter table pspadm.psp_paycheck_usage_2021 RENAME to psp_paycheck_usa_3021_old;
alter table pspadm.psp_paycheck_usage_2022 RENAME to psp_paycheck_usa_3022_old;
alter table pspadm.psp_paycheck_usage_2023 RENAME to psp_paycheck_usa_3023_old;
alter table pspadm.psp_paycheck_usage_2024 RENAME to psp_paycheck_usa_3024_old;


--rename pk to _old_pk

alter table pspadm.psp_paycheck_usa_2012_old RENAME constraint psp_paycheck_usage_2012_pk to psp_paycheck_usa_2012_old_pk;
alter table pspadm.psp_paycheck_usa_2013_old RENAME constraint psp_paycheck_usage_2013_pk to psp_paycheck_usa_2013_old_pk;
alter table pspadm.psp_paycheck_usa_2014_old RENAME constraint psp_paycheck_usage_2014_pk to psp_paycheck_usa_2014_old_pk;
alter table pspadm.psp_paycheck_usa_2015_old RENAME constraint psp_paycheck_usage_2015_pk to psp_paycheck_usa_2015_old_pk;
alter table pspadm.psp_paycheck_usa_2016_old RENAME constraint psp_paycheck_usage_2016_pk to psp_paycheck_usa_2016_old_pk;
alter table pspadm.psp_paycheck_usa_2017_old RENAME constraint psp_paycheck_usage_2017_pk to psp_paycheck_usa_2017_old_pk;
alter table pspadm.psp_paycheck_usa_2018_old RENAME constraint psp_paycheck_usage_2018_pk to psp_paycheck_usa_2018_old_pk;
alter table pspadm.psp_paycheck_usa_2019_old RENAME constraint psp_paycheck_usage_2019_pk to psp_paycheck_usa_2019_old_pk;
alter table pspadm.psp_paycheck_usa_2020_old RENAME constraint psp_paycheck_usage_2020_pk to psp_paycheck_usa_2020_old_pk;
alter table pspadm.psp_paycheck_usa_2021_old RENAME constraint psp_paycheck_usage_2021_pk to psp_paycheck_usa_2021_old_pk;
alter table pspadm.psp_paycheck_usa_2022_old RENAME constraint psp_paycheck_usage_2022_pk to psp_paycheck_usa_2022_old_pk;
alter table pspadm.psp_paycheck_usa_2023_old RENAME constraint psp_paycheck_usage_2023_pk to psp_paycheck_usa_2023_old_pk;
alter table pspadm.psp_paycheck_usa_2024_old RENAME constraint psp_paycheck_usage_2024_pk to psp_paycheck_usa_2024_old_pk;



--create table new table 

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
   PARTITION BY RANGE (paycheck_date)
        WITH (
        OIDS=FALSE
        );

--Partition tables 
--24-OCT-12

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




--copy data from _old to new
\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2012_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2013_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2014_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2015_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2016_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2017_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2018_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2019_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2020_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2021_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2022_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2023_old;
Insert into pspadm.psp_paycheck_usage select * from pspadm.psp_paycheck_usa_2024_old;
SELECT CURRENT_TIMESTAMP;




--rename indexes to old

set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;
alter index  psp_paycheckusage_fk1 RENAME to psp_paycheckusage_fk1_old;
alter index  psp_paycheckusage_fk2  RENAME to psp_paycheckusage_fk2_old;
alter index  psp_paycheck_usage_fk1  RENAME to psp_paycheck_usage_fk1_old;
alter index  psp_paycheck_usage_nu1  RENAME to psp_paycheck_usage_nu1_old;
alter index  psp_paycheck_usage_u1  RENAME  to psp_paycheck_usage_u1_old;
alter index  idx_pchk_usg_mod_date   RENAME to idx_pchk_usg_mod_date_old;

alter index psp_paycheck_usage_fk1_2012 RENAME to psp_paycheck_usage_fk1_2012_old;
alter index psp_paycheck_usage_fk1_2013 RENAME to psp_paycheck_usage_fk1_2013_old;
alter index psp_paycheck_usage_fk1_2014 RENAME to psp_paycheck_usage_fk1_2014_old;
alter index psp_paycheck_usage_fk1_2015 RENAME to psp_paycheck_usage_fk1_2015_old;
alter index psp_paycheck_usage_fk1_2016 RENAME to psp_paycheck_usage_fk1_2016_old;
alter index psp_paycheck_usage_fk1_2017 RENAME to psp_paycheck_usage_fk1_2017_old;
alter index psp_paycheck_usage_fk1_2018 RENAME to psp_paycheck_usage_fk1_2018_old;
alter index psp_paycheck_usage_fk1_2019 RENAME to psp_paycheck_usage_fk1_2019_old;
alter index psp_paycheck_usage_fk1_2020 RENAME to psp_paycheck_usage_fk1_2020_old;
alter index psp_paycheck_usage_fk1_2021 RENAME to psp_paycheck_usage_fk1_2021_old;
alter index psp_paycheck_usage_fk1_2022 RENAME to psp_paycheck_usage_fk1_2022_old;
alter index psp_paycheck_usage_fk1_2023 RENAME to psp_paycheck_usage_fk1_2023_old;
alter index psp_paycheck_usage_fk1_2024 RENAME to psp_paycheck_usage_fk1_2024_old;

alter index psp_paycheck_usage_nu1_2012 RENAME to psp_paycheck_usage_nu1_2012_old;
alter index psp_paycheck_usage_nu1_2013 RENAME to psp_paycheck_usage_nu1_2013_old;
alter index psp_paycheck_usage_nu1_2014 RENAME to psp_paycheck_usage_nu1_2014_old;
alter index psp_paycheck_usage_nu1_2015 RENAME to psp_paycheck_usage_nu1_2015_old;
alter index psp_paycheck_usage_nu1_2016 RENAME to psp_paycheck_usage_nu1_2016_old;
alter index psp_paycheck_usage_nu1_2017 RENAME to psp_paycheck_usage_nu1_2017_old;
alter index psp_paycheck_usage_nu1_2018 RENAME to psp_paycheck_usage_nu1_2018_old;
alter index psp_paycheck_usage_nu1_2019 RENAME to psp_paycheck_usage_nu1_2019_old;
alter index psp_paycheck_usage_nu1_2020 RENAME to psp_paycheck_usage_nu1_2020_old;
alter index psp_paycheck_usage_nu1_2021 RENAME to psp_paycheck_usage_nu1_2021_old;
alter index psp_paycheck_usage_nu1_2022 RENAME to psp_paycheck_usage_nu1_2022_old;
alter index psp_paycheck_usage_nu1_2023 RENAME to psp_paycheck_usage_nu1_2023_old;
alter index psp_paycheck_usage_nu1_2024 RENAME to psp_paycheck_usage_nu1_2024_old;

alter index psp_paycheck_usage_u1_2012 RENAME to psp_paycheck_usage_u1_2012_old;
alter index psp_paycheck_usage_u1_2013 RENAME to psp_paycheck_usage_u1_2013_old;
alter index psp_paycheck_usage_u1_2014 RENAME to psp_paycheck_usage_u1_2014_old;
alter index psp_paycheck_usage_u1_2015 RENAME to psp_paycheck_usage_u1_2015_old;
alter index psp_paycheck_usage_u1_2016 RENAME to psp_paycheck_usage_u1_2016_old;
alter index psp_paycheck_usage_u1_2017 RENAME to psp_paycheck_usage_u1_2017_old;
alter index psp_paycheck_usage_u1_2018 RENAME to psp_paycheck_usage_u1_2018_old;
alter index psp_paycheck_usage_u1_2019 RENAME to psp_paycheck_usage_u1_2019_old;
alter index psp_paycheck_usage_u1_2020 RENAME to psp_paycheck_usage_u1_2020_old;
alter index psp_paycheck_usage_u1_2021 RENAME to psp_paycheck_usage_u1_2021_old;
alter index psp_paycheck_usage_u1_2022 RENAME to psp_paycheck_usage_u1_2022_old;
alter index psp_paycheck_usage_u1_2023 RENAME to psp_paycheck_usage_u1_2023_old;
alter index psp_paycheck_usage_u1_2024 RENAME to psp_paycheck_usage_u1_2024_old;

alter index psp_paycheckusage_fk1_2012 RENAME to psp_paycheckusage_fk1_2012_old;
alter index psp_paycheckusage_fk1_2013 RENAME to psp_paycheckusage_fk1_2013_old;
alter index psp_paycheckusage_fk1_2014 RENAME to psp_paycheckusage_fk1_2014_old;
alter index psp_paycheckusage_fk1_2015 RENAME to psp_paycheckusage_fk1_2015_old;
alter index psp_paycheckusage_fk1_2016 RENAME to psp_paycheckusage_fk1_2016_old;
alter index psp_paycheckusage_fk1_2017 RENAME to psp_paycheckusage_fk1_2017_old;
alter index psp_paycheckusage_fk1_2018 RENAME to psp_paycheckusage_fk1_2018_old;
alter index psp_paycheckusage_fk1_2019 RENAME to psp_paycheckusage_fk1_2019_old;
alter index psp_paycheckusage_fk1_2020 RENAME to psp_paycheckusage_fk1_2020_old;
alter index psp_paycheckusage_fk1_2021 RENAME to psp_paycheckusage_fk1_2021_old;
alter index psp_paycheckusage_fk1_2022 RENAME to psp_paycheckusage_fk1_2022_old;
alter index psp_paycheckusage_fk1_2023 RENAME to psp_paycheckusage_fk1_2023_old;
alter index psp_paycheckusage_fk1_2024 RENAME to psp_paycheckusage_fk1_2024_old;

alter index psp_paycheckusage_fk2_2012 RENAME to psp_paycheckusage_fk2_2012_old;
alter index psp_paycheckusage_fk2_2013 RENAME to psp_paycheckusage_fk2_2013_old;
alter index psp_paycheckusage_fk2_2014 RENAME to psp_paycheckusage_fk2_2014_old;
alter index psp_paycheckusage_fk2_2015 RENAME to psp_paycheckusage_fk2_2015_old;
alter index psp_paycheckusage_fk2_2016 RENAME to psp_paycheckusage_fk2_2016_old;
alter index psp_paycheckusage_fk2_2017 RENAME to psp_paycheckusage_fk2_2017_old;
alter index psp_paycheckusage_fk2_2018 RENAME to psp_paycheckusage_fk2_2018_old;
alter index psp_paycheckusage_fk2_2019 RENAME to psp_paycheckusage_fk2_2019_old;
alter index psp_paycheckusage_fk2_2020 RENAME to psp_paycheckusage_fk2_2020_old;
alter index psp_paycheckusage_fk2_2021 RENAME to psp_paycheckusage_fk2_2021_old;
alter index psp_paycheckusage_fk2_2022 RENAME to psp_paycheckusage_fk2_2022_old;
alter index psp_paycheckusage_fk2_2023 RENAME to psp_paycheckusage_fk2_2023_old;
alter index psp_paycheckusage_fk2_2024 RENAME to psp_paycheckusage_fk2_2024_old;

alter index  idx_pchk_usg_mod_date_2012 RENAME to idx_pchk_usg_mod_date_2012_old;
alter index  idx_pchk_usg_mod_date_2013 RENAME to idx_pchk_usg_mod_date_2013_old;
alter index  idx_pchk_usg_mod_date_2014 RENAME to idx_pchk_usg_mod_date_2014_old;
alter index  idx_pchk_usg_mod_date_2015 RENAME to idx_pchk_usg_mod_date_2015_old;
alter index  idx_pchk_usg_mod_date_2016 RENAME to idx_pchk_usg_mod_date_2016_old;
alter index  idx_pchk_usg_mod_date_2017 RENAME to idx_pchk_usg_mod_date_2017_old;
alter index  idx_pchk_usg_mod_date_2018 RENAME to idx_pchk_usg_mod_date_2018_old;
alter index  idx_pchk_usg_mod_date_2019 RENAME to idx_pchk_usg_mod_date_2019_old;
alter index  idx_pchk_usg_mod_date_2020 RENAME to idx_pchk_usg_mod_date_2020_old;
alter index  idx_pchk_usg_mod_date_2021 RENAME to idx_pchk_usg_mod_date_2021_old;
alter index  idx_pchk_usg_mod_date_2022 RENAME to idx_pchk_usg_mod_date_2022_old;
alter index  idx_pchk_usg_mod_date_2023 RENAME to idx_pchk_usg_mod_date_2023_old;
alter index  idx_pchk_usg_mod_date_2024 RENAME to idx_pchk_usg_mod_date_2024_old;
SELECT CURRENT_TIMESTAMP;

--create index psp_paycheck_usage
\timing
set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

select 'creating index on psp_paycheck_usage' as status;
create index concurrently psp_paycheck_usage_fk1_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (company_fk );
create index concurrently psp_paycheck_usage_fk1_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (company_fk );

create index concurrently psp_paycheck_usage_nu1_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (source_paycheck_id );
create index concurrently psp_paycheck_usage_nu1_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (source_paycheck_id );

create index concurrently psp_paycheck_usage_u1_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (employee_usage_fk ,source_paycheck_id );
create index concurrently psp_paycheck_usage_u1_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (employee_usage_fk ,source_paycheck_id );

create index concurrently psp_paycheckusage_fk1_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (bill_fk );
create index concurrently psp_paycheckusage_fk1_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (bill_fk );

create index concurrently psp_paycheckusage_fk2_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (employee_usage_fk );
create index concurrently psp_paycheckusage_fk2_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (employee_usage_fk );

create index concurrently  idx_pchk_usg_mod_date_2012 on pspadm.psp_paycheck_usage_2012  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2013 on pspadm.psp_paycheck_usage_2013  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2014 on pspadm.psp_paycheck_usage_2014  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2015 on pspadm.psp_paycheck_usage_2015  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2016 on pspadm.psp_paycheck_usage_2016  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2017 on pspadm.psp_paycheck_usage_2017  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2018 on pspadm.psp_paycheck_usage_2018  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2019 on pspadm.psp_paycheck_usage_2019  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2020 on pspadm.psp_paycheck_usage_2020  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2021 on pspadm.psp_paycheck_usage_2021  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2022 on pspadm.psp_paycheck_usage_2022  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2023 on pspadm.psp_paycheck_usage_2023  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2024 on pspadm.psp_paycheck_usage_2024  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2025 on pspadm.psp_paycheck_usage_2025  USING BTREE (modified_date);
create index concurrently  idx_pchk_usg_mod_date_2026 on pspadm.psp_paycheck_usage_2026  USING BTREE (modified_date);


select  'psp_paycheck_usage completed'  as status;


--psp_paycheck_usage
create index  psp_paycheckusage_fk1 ON ONLY  pspadm.psp_paycheck_usage USING BTREE (bill_fk );
create index  psp_paycheckusage_fk2 ON ONLY  pspadm.psp_paycheck_usage USING BTREE (employee_usage_fk );
create index  psp_paycheck_usage_fk1 ON ONLY  pspadm.psp_paycheck_usage USING BTREE (company_fk );
create index  psp_paycheck_usage_nu1 ON ONLY  pspadm.psp_paycheck_usage USING BTREE (source_paycheck_id );
create index  psp_paycheck_usage_u1 ON ONLY  pspadm.psp_paycheck_usage USING BTREE (employee_usage_fk ,source_paycheck_id );
create index  idx_pchk_usg_mod_date ON ONLY  pspadm.psp_paycheck_usage USING BTREE (modified_date);

alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2012 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2013 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2014 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2015 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2016 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2017 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2018 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2019 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2020 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2021 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2022 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2023 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2024 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2025 ;
alter index psp_paycheck_usage_fk1 attach partition  pspadm.psp_paycheck_usage_fk1_2026 ;

alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2012 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2013 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2014 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2015 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2016 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2017 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2018 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2019 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2020 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2021 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2022 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2023 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2024 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2025 ;
alter index psp_paycheck_usage_nu1 attach partition  pspadm.psp_paycheck_usage_nu1_2026 ;

alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2012 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2013 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2014 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2015 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2016 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2017 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2018 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2019 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2020 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2021 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2022 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2023 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2024 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2025 ;
alter index psp_paycheck_usage_u1 attach partition  pspadm.psp_paycheck_usage_u1_2026 ;

alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2012 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2013 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2014 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2015 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2016 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2017 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2018 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2019 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2020 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2021 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2022 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2023 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2024 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2025 ;
alter index psp_paycheckusage_fk1 attach partition  pspadm.psp_paycheckusage_fk1_2026 ;

alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2012 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2013 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2014 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2015 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2016 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2017 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2018 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2019 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2020 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2021 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2022 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2023 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2024 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2025 ;
alter index psp_paycheckusage_fk2 attach partition  pspadm.psp_paycheckusage_fk2_2026 ;

alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2012 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2013 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2014 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2015 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2016 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2017 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2018 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2019 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2020 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2021 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2022 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2023 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2024 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2025 ;
alter index idx_pchk_usg_mod_date attach partition  pspadm.idx_pchk_usg_mod_date_2026 ;

SELECT CURRENT_TIMESTAMP;

--Check old and new table count
select count(*) from pspadm.psp_paycheck_usage;
select count(*) from pspadm.psp_paycheck_usa_old;
