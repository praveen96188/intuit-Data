\timing
set search_path=psparc;
SELECT CURRENT_TIMESTAMP;

CREATE TABLE psparc.psp_entitlement_message(
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

CREATE TABLE psparc.psp_entitlement_message_2012
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM (MINVALUE) TO ('2013-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2013
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2014-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2014
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2015-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2015
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2016-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2016
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2017-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2017
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2018-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2018
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2019-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2019
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2020-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2020
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2021-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2021
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2022-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2022
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2023
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2024-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2024
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2025-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2025
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE psparc.psp_entitlement_message_2026
        PARTITION OF psparc.psp_entitlement_message
        FOR VALUES FROM ('2026-01-01 00:00:00') TO (MAXVALUE);
--PK

ALTER TABLE psparc.psp_entitlement_message_2012 ADD CONSTRAINT psp_entitlement_message_2012_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2013 ADD CONSTRAINT psp_entitlement_message_2013_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2014 ADD CONSTRAINT psp_entitlement_message_2014_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2015 ADD CONSTRAINT psp_entitlement_message_2015_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2016 ADD CONSTRAINT psp_entitlement_message_2016_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2017 ADD CONSTRAINT psp_entitlement_message_2017_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2018 ADD CONSTRAINT psp_entitlement_message_2018_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2019 ADD CONSTRAINT psp_entitlement_message_2019_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2020 ADD CONSTRAINT psp_entitlement_message_2020_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2021 ADD CONSTRAINT psp_entitlement_message_2021_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2022 ADD CONSTRAINT psp_entitlement_message_2022_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2023 ADD CONSTRAINT psp_entitlement_message_2023_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2024 ADD CONSTRAINT psp_entitlement_message_2024_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2025 ADD CONSTRAINT psp_entitlement_message_2025_pk PRIMARY KEY (entitlement_message_seq, realm_id);
ALTER TABLE psparc.psp_entitlement_message_2026 ADD CONSTRAINT psp_entitlement_message_2026_pk PRIMARY KEY (entitlement_message_seq, realm_id);

--psp_paycheck_usage

CREATE TABLE psparc.psp_paycheck_usage(
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

CREATE TABLE psparc.psp_paycheck_usage_2012
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM (MINVALUE) TO ('2013-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2013
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2014-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2014
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2015-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2015
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2016-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2016
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2017-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2017
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2018-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2018
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2019-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2019
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2020-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2020
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2021-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2021
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2022-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2022
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2023
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2024-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2024
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2025-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2025
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE psparc.psp_paycheck_usage_2026
        PARTITION OF psparc.psp_paycheck_usage
        FOR VALUES FROM ('2026-01-01 00:00:00') TO (MAXVALUE);
--PK

ALTER TABLE psparc.psp_paycheck_usage_2012 ADD CONSTRAINT psp_paycheck_usage_2012_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2013 ADD CONSTRAINT psp_paycheck_usage_2013_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2014 ADD CONSTRAINT psp_paycheck_usage_2014_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2015 ADD CONSTRAINT psp_paycheck_usage_2015_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2016 ADD CONSTRAINT psp_paycheck_usage_2016_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2017 ADD CONSTRAINT psp_paycheck_usage_2017_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2018 ADD CONSTRAINT psp_paycheck_usage_2018_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2019 ADD CONSTRAINT psp_paycheck_usage_2019_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2020 ADD CONSTRAINT psp_paycheck_usage_2020_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2021 ADD CONSTRAINT psp_paycheck_usage_2021_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2022 ADD CONSTRAINT psp_paycheck_usage_2022_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2023 ADD CONSTRAINT psp_paycheck_usage_2023_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2024 ADD CONSTRAINT psp_paycheck_usage_2024_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2025 ADD CONSTRAINT psp_paycheck_usage_2025_pk PRIMARY KEY (paycheck_usage_seq, realm_id);
ALTER TABLE psparc.psp_paycheck_usage_2026 ADD CONSTRAINT psp_paycheck_usage_2026_pk PRIMARY KEY (paycheck_usage_seq, realm_id);

--psp_entity_update

CREATE TABLE psparc.psp_entity_update(
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

CREATE TABLE psparc.psp_entity_update_m112022
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM (MINVALUE) TO ('2022-12-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m122022
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2022-12-01 00:00:00') TO ('2023-01-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m012023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2023-02-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m022023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-02-01 00:00:00') TO ('2023-03-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m032023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-03-01 00:00:00') TO ('2023-04-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m042023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-04-01 00:00:00') TO ('2023-05-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m052023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-05-01 00:00:00') TO ('2023-06-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m062023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-06-01 00:00:00') TO ('2023-07-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m072023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-07-01 00:00:00') TO ('2023-08-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m082023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-08-01 00:00:00') TO ('2023-09-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m092023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-09-01 00:00:00') TO ('2023-10-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m102023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-10-01 00:00:00') TO ('2023-11-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m112023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-11-01 00:00:00') TO ('2023-12-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m122023
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2023-12-01 00:00:00') TO ('2024-01-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m122024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-12-01 00:00:00') TO ('2025-01-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m112024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-11-01 00:00:00') TO ('2024-12-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m102024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-10-01 00:00:00') TO ('2024-11-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m092024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-09-01 00:00:00') TO ('2024-10-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m082024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-08-01 00:00:00') TO ('2024-09-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m072024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-07-01 00:00:00') TO ('2024-08-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m062024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-06-01 00:00:00') TO ('2024-07-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m052024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-05-01 00:00:00') TO ('2024-06-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m042024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-04-01 00:00:00') TO ('2024-05-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m032024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-03-01 00:00:00') TO ('2024-04-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m022024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-02-01 00:00:00') TO ('2024-03-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m012024
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2024-02-01 00:00:00');

--2025
CREATE TABLE psparc.psp_entity_update_m122025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-12-01 00:00:00') TO ('2026-01-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m112025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-11-01 00:00:00') TO ('2025-12-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m102025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-10-01 00:00:00') TO ('2025-11-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m092025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-09-01 00:00:00') TO ('2025-10-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m082025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-08-01 00:00:00') TO ('2025-09-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m072025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-07-01 00:00:00') TO ('2025-08-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m062025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-06-01 00:00:00') TO ('2025-07-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m052025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-05-01 00:00:00') TO ('2025-06-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m042025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-04-01 00:00:00') TO ('2025-05-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m032025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-03-01 00:00:00') TO ('2025-04-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m022025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-02-01 00:00:00') TO ('2025-03-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m012025
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2025-01-01 00:00:00') TO ('2025-02-01 00:00:00');

--2026

CREATE TABLE psparc.psp_entity_update_m122026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-12-01 00:00:00') TO (MAXVALUE);

CREATE TABLE psparc.psp_entity_update_m112026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m102026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m092026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m082026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m072026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-07-01 00:00:00') TO ('2026-08-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m062026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-06-01 00:00:00') TO ('2026-07-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m052026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m042026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m032026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-03-01 00:00:00') TO ('2026-04-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m022026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-02-01 00:00:00') TO ('2026-03-01 00:00:00');

CREATE TABLE psparc.psp_entity_update_m012026
        PARTITION OF psparc.psp_entity_update
        FOR VALUES FROM ('2026-01-01 00:00:00') TO ('2026-02-01 00:00:00');

--2022 & 2023
ALTER TABLE psparc.psp_entity_update_m112022 ADD CONSTRAINT psp_entity_update_m112022_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m122022 ADD CONSTRAINT psp_entity_update_m122022_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m012023 ADD CONSTRAINT psp_entity_update_m012023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m022023 ADD CONSTRAINT psp_entity_update_m022023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m032023 ADD CONSTRAINT psp_entity_update_m032023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m042023 ADD CONSTRAINT psp_entity_update_m042023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m052023 ADD CONSTRAINT psp_entity_update_m052023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m062023 ADD CONSTRAINT psp_entity_update_m062023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m072023 ADD CONSTRAINT psp_entity_update_m072023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m082023 ADD CONSTRAINT psp_entity_update_m082023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m092023 ADD CONSTRAINT psp_entity_update_m092023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m102023 ADD CONSTRAINT psp_entity_update_m102023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m112023 ADD CONSTRAINT psp_entity_update_m112023_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m122023 ADD CONSTRAINT psp_entity_update_m122023_pk PRIMARY KEY (entity_update_seq, realm_id);

--2024
ALTER TABLE psparc.psp_entity_update_m012024 ADD CONSTRAINT psp_entity_update_m012024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m022024 ADD CONSTRAINT psp_entity_update_m022024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m032024 ADD CONSTRAINT psp_entity_update_m032024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m042024 ADD CONSTRAINT psp_entity_update_m042024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m052024 ADD CONSTRAINT psp_entity_update_m052024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m062024 ADD CONSTRAINT psp_entity_update_m062024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m072024 ADD CONSTRAINT psp_entity_update_m072024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m082024 ADD CONSTRAINT psp_entity_update_m082024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m092024 ADD CONSTRAINT psp_entity_update_m092024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m102024 ADD CONSTRAINT psp_entity_update_m102024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m112024 ADD CONSTRAINT psp_entity_update_m112024_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m122024 ADD CONSTRAINT psp_entity_update_m122024_pk PRIMARY KEY (entity_update_seq, realm_id);

--2025
ALTER TABLE psparc.psp_entity_update_m012025 ADD CONSTRAINT psp_entity_update_m012025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m022025 ADD CONSTRAINT psp_entity_update_m022025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m032025 ADD CONSTRAINT psp_entity_update_m032025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m042025 ADD CONSTRAINT psp_entity_update_m042025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m052025 ADD CONSTRAINT psp_entity_update_m052025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m062025 ADD CONSTRAINT psp_entity_update_m062025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m072025 ADD CONSTRAINT psp_entity_update_m072025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m082025 ADD CONSTRAINT psp_entity_update_m082025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m092025 ADD CONSTRAINT psp_entity_update_m092025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m102025 ADD CONSTRAINT psp_entity_update_m102025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m112025 ADD CONSTRAINT psp_entity_update_m112025_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m122025 ADD CONSTRAINT psp_entity_update_m122025_pk PRIMARY KEY (entity_update_seq, realm_id);

--2026
ALTER TABLE psparc.psp_entity_update_m012026 ADD CONSTRAINT psp_entity_update_m012026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m022026 ADD CONSTRAINT psp_entity_update_m022026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m032026 ADD CONSTRAINT psp_entity_update_m032026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m042026 ADD CONSTRAINT psp_entity_update_m042026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m052026 ADD CONSTRAINT psp_entity_update_m052026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m062026 ADD CONSTRAINT psp_entity_update_m062026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m072026 ADD CONSTRAINT psp_entity_update_m072026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m082026 ADD CONSTRAINT psp_entity_update_m082026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m092026 ADD CONSTRAINT psp_entity_update_m092026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m102026 ADD CONSTRAINT psp_entity_update_m102026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m112026 ADD CONSTRAINT psp_entity_update_m112026_pk PRIMARY KEY (entity_update_seq, realm_id);
ALTER TABLE psparc.psp_entity_update_m122026 ADD CONSTRAINT psp_entity_update_m122026_pk PRIMARY KEY (entity_update_seq, realm_id);

SELECT CURRENT_TIMESTAMP;

