-- ------------ Write CREATE-SUBPARTITION-stage scripts -----------

CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_9999
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_9999
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m012018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m022018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m032018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m042018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m052018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m062018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072009
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072009
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m072018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082009
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082009
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m082018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092009
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092009
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m092018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102009
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102009
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m102018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112009
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112009
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m112018
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122009
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122009
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122010
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122010
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122011
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122011
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122011
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122011
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122011
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122011
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122011
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122012
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122012
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122012
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122012
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122012
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122012
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122012
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122013
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122013
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122013
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122013
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122013
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122013
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122013
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122014
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122014
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122014
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122014
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122014
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122014
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122014
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122015
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122015
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122015
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122015
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122015
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122015
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122015
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122016
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122016
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122016
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122016
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122016
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122016
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122016
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122017
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122017
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122017
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122017
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122017
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122017
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122017
        FOR VALUES IN ('QBDT');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018_from_as400
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122018
        FOR VALUES IN ('AS400');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018_from_cris
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122018
        FOR VALUES IN ('CRIS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018_from_dflt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122018
        DEFAULT;



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018_from_ews
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122018
        FOR VALUES IN ('EWS');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018_from_null
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122018
        FOR VALUES IN (NULL);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018_from_psp
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122018
        FOR VALUES IN ('PSP');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018_from_qbdt
        PARTITION OF pspadm.psp_source_system_transmission_srcsystrns_m122018
        FOR VALUES IN ('QBDT');



