--Partitions
CREATE TABLE ibobadm.psp_source_system_transmission_m012024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2024-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-02-01 00:00:00') TO ('2024-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-03-01 00:00:00') TO ('2024-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-04-01 00:00:00') TO ('2024-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-05-01 00:00:00') TO ('2024-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-06-01 00:00:00') TO ('2024-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-07-01 00:00:00') TO ('2024-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-08-01 00:00:00') TO ('2024-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-09-01 00:00:00') TO ('2024-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-10-01 00:00:00') TO ('2024-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-11-01 00:00:00') TO ('2024-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122024
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2024-12-01 00:00:00') TO ('2025-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

--Subpartitions
CREATE TABLE ibobadm.psp_source_system_transmission_m012024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112024
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122024_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122024
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122024_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122024
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122024_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122024
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122024_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122024
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122024_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122024
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122024_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122024
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122024_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122024
        FOR VALUES IN ('QBDT');

