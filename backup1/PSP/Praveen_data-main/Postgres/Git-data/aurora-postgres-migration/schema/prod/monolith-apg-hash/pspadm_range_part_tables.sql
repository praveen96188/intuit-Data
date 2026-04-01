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


--psp_entity_update

CREATE TABLE pspadm.psp_entity_update(
    entity_update_seq CHARACTER VARYING(255) NOT NULL,
    version integer NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id SMALLINT NOT NULL DEFAULT - 1,
    entity_name CHARACTER VARYING(4000),
    retry_count integer,
    changed_attributes TEXT,
    status CHARACTER VARYING(255),
    transaction_id CHARACTER VARYING(4000),
    event_type CHARACTER VARYING(255),
    entity_id CHARACTER VARYING(4000),
    company_fk CHARACTER VARYING(255)
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );



CREATE TABLE pspadm.psp_entity_update_m042023
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2023-04-01 00:00:00') TO (MAXVALUE);

CREATE TABLE pspadm.psp_entity_update_m032023
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2023-03-01 00:00:00') TO ('2023-04-01 00:00:00');

CREATE TABLE pspadm.psp_entity_update_m022023
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2023-02-01 00:00:00') TO ('2023-03-01 00:00:00');

CREATE TABLE pspadm.psp_entity_update_m012023
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2023-02-01 00:00:00');

CREATE TABLE pspadm.psp_entity_update_m122022
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2022-12-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE pspadm.psp_entity_update_m112022
        PARTITION OF pspadm.psp_entity_update
        FOR VALUES FROM ('2022-11-01 00:00:00') TO ('2022-12-01 00:00:00');



ALTER TABLE pspadm.psp_entity_update_m112022 ADD CONSTRAINT psp_entity_update_m112022_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m122022 ADD CONSTRAINT psp_entity_update_m122022_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m012023 ADD CONSTRAINT psp_entity_update_m012023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m022023 ADD CONSTRAINT psp_entity_update_m022023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m032023 ADD CONSTRAINT psp_entity_update_m032023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE pspadm.psp_entity_update_m042023 ADD CONSTRAINT psp_entity_update_m042023_pk PRIMARY KEY (entity_update_seq, realm_id);

SELECT CURRENT_TIMESTAMP;
