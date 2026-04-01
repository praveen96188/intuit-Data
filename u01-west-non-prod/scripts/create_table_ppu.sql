--psp_paycheck_usage

CREATE TABLE pspadm.psp_paycheck_usage_cr_date(
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
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM (MINVALUE) TO ('2013-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2013
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2014-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2014
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2015-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2015
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2016-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2016
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2017-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2017
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2018-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2018
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2019-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2019
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2020-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2020
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2021-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2021
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2022-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2022
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2023
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2024-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2024
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2025-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2025
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE pspadm.psp_paycheck_usage_2026
        PARTITION OF pspadm.psp_paycheck_usage_cr_date
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

