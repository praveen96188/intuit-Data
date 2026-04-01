-- ------------ Write CREATE-SUBPARTITION-stage scripts -----------

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_2008_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_2008
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_9999_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m012010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m022010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m032010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m042010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m052010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m062010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m072010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m082010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m092010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m102010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m112010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_srcsystrns_m122010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('QBDT');

