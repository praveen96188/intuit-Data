-- ------------ Write CREATE-PARTITION-stage scripts -----------

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-01-01 00:00:00') TO (MAXVALUE)
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-02-01 00:00:00') TO ('2009-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-03-01 00:00:00') TO ('2009-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-04-01 00:00:00') TO ('2009-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-05-01 00:00:00') TO ('2009-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-06-01 00:00:00') TO ('2009-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2009-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

