set search_path=ibobadm;

create table ibobadm.psp_source_system_transmission_2009(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072009;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082009;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092009;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102009;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112009;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122009;

alter table  ibobadm.psp_source_system_transmission_2009 attach partition psp_source_system_transmission_m072009 FOR VALUES FROM (MINVALUE) TO ('2009-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2009 attach partition psp_source_system_transmission_m082009 FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2009 attach partition psp_source_system_transmission_m092009 FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2009 attach partition psp_source_system_transmission_m102009 FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2009 attach partition psp_source_system_transmission_m112009 FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2009 attach partition psp_source_system_transmission_m122009 FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00');


create table ibobadm.psp_source_system_transmission_2010(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112010;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122010;

alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m012010 FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m022010 FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m032010 FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m042010 FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m052010 FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m062010 FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m072010 FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m082010 FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m092010 FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m102010 FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m112010 FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2010 attach partition psp_source_system_transmission_m122010 FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00');


create table ibobadm.psp_source_system_transmission_2011(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112011;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122011;

alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m012011 FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m022011 FOR VALUES FROM ('2011-02-01 00:00:00') TO ('2011-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m032011 FOR VALUES FROM ('2011-03-01 00:00:00') TO ('2011-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m042011 FOR VALUES FROM ('2011-04-01 00:00:00') TO ('2011-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m052011 FOR VALUES FROM ('2011-05-01 00:00:00') TO ('2011-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m062011 FOR VALUES FROM ('2011-06-01 00:00:00') TO ('2011-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m072011 FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2011-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m082011 FOR VALUES FROM ('2011-08-01 00:00:00') TO ('2011-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m092011 FOR VALUES FROM ('2011-09-01 00:00:00') TO ('2011-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m102011 FOR VALUES FROM ('2011-10-01 00:00:00') TO ('2011-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m112011 FOR VALUES FROM ('2011-11-01 00:00:00') TO ('2011-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2011 attach partition psp_source_system_transmission_m122011 FOR VALUES FROM ('2011-12-01 00:00:00') TO ('2012-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2012(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112012;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122012;

alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m012012 FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m022012 FOR VALUES FROM ('2012-02-01 00:00:00') TO ('2012-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m032012 FOR VALUES FROM ('2012-03-01 00:00:00') TO ('2012-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m042012 FOR VALUES FROM ('2012-04-01 00:00:00') TO ('2012-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m052012 FOR VALUES FROM ('2012-05-01 00:00:00') TO ('2012-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m062012 FOR VALUES FROM ('2012-06-01 00:00:00') TO ('2012-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m072012 FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2012-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m082012 FOR VALUES FROM ('2012-08-01 00:00:00') TO ('2012-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m092012 FOR VALUES FROM ('2012-09-01 00:00:00') TO ('2012-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m102012 FOR VALUES FROM ('2012-10-01 00:00:00') TO ('2012-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m112012 FOR VALUES FROM ('2012-11-01 00:00:00') TO ('2012-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2012 attach partition psp_source_system_transmission_m122012 FOR VALUES FROM ('2012-12-01 00:00:00') TO ('2013-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2013(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112013;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122013;

alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m012013 FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m022013 FOR VALUES FROM ('2013-02-01 00:00:00') TO ('2013-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m032013 FOR VALUES FROM ('2013-03-01 00:00:00') TO ('2013-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m042013 FOR VALUES FROM ('2013-04-01 00:00:00') TO ('2013-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m052013 FOR VALUES FROM ('2013-05-01 00:00:00') TO ('2013-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m062013 FOR VALUES FROM ('2013-06-01 00:00:00') TO ('2013-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m072013 FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2013-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m082013 FOR VALUES FROM ('2013-08-01 00:00:00') TO ('2013-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m092013 FOR VALUES FROM ('2013-09-01 00:00:00') TO ('2013-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m102013 FOR VALUES FROM ('2013-10-01 00:00:00') TO ('2013-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m112013 FOR VALUES FROM ('2013-11-01 00:00:00') TO ('2013-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2013 attach partition psp_source_system_transmission_m122013 FOR VALUES FROM ('2013-12-01 00:00:00') TO ('2014-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2014(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112014;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122014;

alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m012014 FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m022014 FOR VALUES FROM ('2014-02-01 00:00:00') TO ('2014-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m032014 FOR VALUES FROM ('2014-03-01 00:00:00') TO ('2014-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m042014 FOR VALUES FROM ('2014-04-01 00:00:00') TO ('2014-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m052014 FOR VALUES FROM ('2014-05-01 00:00:00') TO ('2014-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m062014 FOR VALUES FROM ('2014-06-01 00:00:00') TO ('2014-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m072014 FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2014-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m082014 FOR VALUES FROM ('2014-08-01 00:00:00') TO ('2014-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m092014 FOR VALUES FROM ('2014-09-01 00:00:00') TO ('2014-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m102014 FOR VALUES FROM ('2014-10-01 00:00:00') TO ('2014-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m112014 FOR VALUES FROM ('2014-11-01 00:00:00') TO ('2014-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2014 attach partition psp_source_system_transmission_m122014 FOR VALUES FROM ('2014-12-01 00:00:00') TO ('2015-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2015(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112015;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122015;

alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m012015 FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m022015 FOR VALUES FROM ('2015-02-01 00:00:00') TO ('2015-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m032015 FOR VALUES FROM ('2015-03-01 00:00:00') TO ('2015-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m042015 FOR VALUES FROM ('2015-04-01 00:00:00') TO ('2015-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m052015 FOR VALUES FROM ('2015-05-01 00:00:00') TO ('2015-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m062015 FOR VALUES FROM ('2015-06-01 00:00:00') TO ('2015-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m072015 FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2015-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m082015 FOR VALUES FROM ('2015-08-01 00:00:00') TO ('2015-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m092015 FOR VALUES FROM ('2015-09-01 00:00:00') TO ('2015-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m102015 FOR VALUES FROM ('2015-10-01 00:00:00') TO ('2015-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m112015 FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2015-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2015 attach partition psp_source_system_transmission_m122015 FOR VALUES FROM ('2015-12-01 00:00:00') TO ('2016-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2016(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112016;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122016;

alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m012016 FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m022016 FOR VALUES FROM ('2016-02-01 00:00:00') TO ('2016-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m032016 FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m042016 FOR VALUES FROM ('2016-04-01 00:00:00') TO ('2016-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m052016 FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m062016 FOR VALUES FROM ('2016-06-01 00:00:00') TO ('2016-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m072016 FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m082016 FOR VALUES FROM ('2016-08-01 00:00:00') TO ('2016-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m092016 FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m102016 FOR VALUES FROM ('2016-10-01 00:00:00') TO ('2016-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m112016 FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2016-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2016 attach partition psp_source_system_transmission_m122016 FOR VALUES FROM ('2016-12-01 00:00:00') TO ('2017-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2017(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112017;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122017;

alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m012017 FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m022017 FOR VALUES FROM ('2017-02-01 00:00:00') TO ('2017-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m032017 FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m042017 FOR VALUES FROM ('2017-04-01 00:00:00') TO ('2017-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m052017 FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m062017 FOR VALUES FROM ('2017-06-01 00:00:00') TO ('2017-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m072017 FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m082017 FOR VALUES FROM ('2017-08-01 00:00:00') TO ('2017-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m092017 FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m102017 FOR VALUES FROM ('2017-10-01 00:00:00') TO ('2017-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m112017 FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2017-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2017 attach partition psp_source_system_transmission_m122017 FOR VALUES FROM ('2017-12-01 00:00:00') TO ('2018-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2018(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112018;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122018;

alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m012018 FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m022018 FOR VALUES FROM ('2018-02-01 00:00:00') TO ('2018-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m032018 FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m042018 FOR VALUES FROM ('2018-04-01 00:00:00') TO ('2018-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m052018 FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m062018 FOR VALUES FROM ('2018-06-01 00:00:00') TO ('2018-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m072018 FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m082018 FOR VALUES FROM ('2018-08-01 00:00:00') TO ('2018-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m092018 FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m102018 FOR VALUES FROM ('2018-10-01 00:00:00') TO ('2018-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m112018 FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2018-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2018 attach partition psp_source_system_transmission_m122018 FOR VALUES FROM ('2018-12-01 00:00:00') TO ('2019-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2019(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112019;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122019;

alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m012019 FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m022019 FOR VALUES FROM ('2019-02-01 00:00:00') TO ('2019-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m032019 FOR VALUES FROM ('2019-03-01 00:00:00') TO ('2019-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m042019 FOR VALUES FROM ('2019-04-01 00:00:00') TO ('2019-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m052019 FOR VALUES FROM ('2019-05-01 00:00:00') TO ('2019-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m062019 FOR VALUES FROM ('2019-06-01 00:00:00') TO ('2019-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m072019 FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2019-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m082019 FOR VALUES FROM ('2019-08-01 00:00:00') TO ('2019-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m092019 FOR VALUES FROM ('2019-09-01 00:00:00') TO ('2019-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m102019 FOR VALUES FROM ('2019-10-01 00:00:00') TO ('2019-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m112019 FOR VALUES FROM ('2019-11-01 00:00:00') TO ('2019-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2019 attach partition psp_source_system_transmission_m122019 FOR VALUES FROM ('2019-12-01 00:00:00') TO ('2020-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2020(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112020;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122020;

alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m012020 FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m022020 FOR VALUES FROM ('2020-02-01 00:00:00') TO ('2020-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m032020 FOR VALUES FROM ('2020-03-01 00:00:00') TO ('2020-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m042020 FOR VALUES FROM ('2020-04-01 00:00:00') TO ('2020-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m052020 FOR VALUES FROM ('2020-05-01 00:00:00') TO ('2020-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m062020 FOR VALUES FROM ('2020-06-01 00:00:00') TO ('2020-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m072020 FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2020-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m082020 FOR VALUES FROM ('2020-08-01 00:00:00') TO ('2020-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m092020 FOR VALUES FROM ('2020-09-01 00:00:00') TO ('2020-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m102020 FOR VALUES FROM ('2020-10-01 00:00:00') TO ('2020-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m112020 FOR VALUES FROM ('2020-11-01 00:00:00') TO ('2020-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2020 attach partition psp_source_system_transmission_m122020 FOR VALUES FROM ('2020-12-01 00:00:00') TO ('2021-01-01 00:00:00');

create table ibobadm.psp_source_system_transmission_2021(
    source_system_transmission_seq CHARACTER VARYING(255) NOT NULL,
    version smallint NOT NULL,
    creator_id CHARACTER VARYING(30),
    created_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    modifier_id CHARACTER VARYING(30),
    modified_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    realm_id smallint NOT NULL DEFAULT - 1,
    from_source_system CHARACTER VARYING(255),
    request_token NUMERIC(19,0),
    response_token NUMERIC(19,0),
    request_document_old TEXT,
    response_document_old TEXT,
    type CHARACTER VARYING(255),
    initialize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    finalize_date_time TIMESTAMP(6) WITHOUT TIME ZONE,
    description CHARACTER VARYING(256),
    to_source_system CHARACTER VARYING(255),
    transmission_identifier CHARACTER VARYING(40),
    i_p_address CHARACTER VARYING(20),
    company_id CHARACTER VARYING(255),
    host CHARACTER VARYING(200),
    application_version CHARACTER VARYING(100),
    application_id CHARACTER VARYING(100),
    tax_table_id CHARACTER VARYING(100),
    response_document TEXT,
    request_document TEXT
)
    PARTITION BY RANGE (created_date)
        WITH (
        OIDS=FALSE
        );

alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m012021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m022021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m032021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m042021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m052021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m062021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m072021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m082021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m092021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m102021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m112021;
alter table ibobadm.psp_source_system_transmission detach partition psp_source_system_transmission_m122021;

alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m012021 FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-02-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m022021 FOR VALUES FROM ('2021-02-01 00:00:00') TO ('2021-03-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m032021 FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-04-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m042021 FOR VALUES FROM ('2021-04-01 00:00:00') TO ('2021-05-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m052021 FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-06-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m062021 FOR VALUES FROM ('2021-06-01 00:00:00') TO ('2021-07-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m072021 FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-08-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m082021 FOR VALUES FROM ('2021-08-01 00:00:00') TO ('2021-09-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m092021 FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-10-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m102021 FOR VALUES FROM ('2021-10-01 00:00:00') TO ('2021-11-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m112021 FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2021-12-01 00:00:00');
alter table  ibobadm.psp_source_system_transmission_2021 attach partition psp_source_system_transmission_m122021 FOR VALUES FROM ('2021-12-01 00:00:00') TO ('2022-01-01 00:00:00');

