set search_path=pspadm;
SELECT CURRENT_TIMESTAMP;

CREATE TABLE pspadm.psp_entitlement_message_range(
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
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM (MINVALUE) TO ('2013-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2013
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2014-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2014
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2015-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2015
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2016-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2016
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2017-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2017
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2018-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2018
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2019-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2019
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2020-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2020
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2021-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2021
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2022-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2022
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2023
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2024-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2024
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2025-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2025
        PARTITION OF pspadm.psp_entitlement_message_range
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE pspadm.psp_entitlement_message_2026
        PARTITION OF pspadm.psp_entitlement_message_range
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

