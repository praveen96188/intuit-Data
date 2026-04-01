set search_path=ibobadm;

CREATE TABLE ibobadm.psp_source_system_transmission_9999_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_9999
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_9999_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_9999
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_9999_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_9999
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_9999_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_9999
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_9999_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_9999
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_9999_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_9999
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_9999_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_9999
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m012022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m022022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112022
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122009_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122009
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122009_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122009
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122009_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122009
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122009_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122009
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122009_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122009
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122009_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122009
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122009_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122009
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122010_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122010
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122010_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122010
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122010_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122010
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122010_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122010
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122010_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122010
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122010_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122010
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122010_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122010
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122011_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122011
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122011_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122011
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122011_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122011
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122011_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122011
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122011_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122011
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122011_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122011
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122011_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122011
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122012_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122012
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122012_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122012
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122012_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122012
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122012_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122012
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122012_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122012
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122012_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122012
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122012_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122012
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122013_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122013
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122013_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122013
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122013_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122013
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122013_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122013
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122013_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122013
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122013_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122013
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122013_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122013
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122014_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122014
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122014_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122014
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122014_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122014
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122014_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122014
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122014_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122014
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122014_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122014
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122014_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122014
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122015_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122015
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122015_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122015
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122015_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122015
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122015_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122015
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122015_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122015
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122015_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122015
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122015_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122015
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122016_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122016
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122016_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122016
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122016_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122016
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122016_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122016
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122016_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122016
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122016_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122016
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122016_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122016
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122017_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122017
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122017_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122017
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122017_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122017
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122017_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122017
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122017_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122017
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122017_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122017
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122017_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122017
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122018_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122018
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122018_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122018
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122018_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122018
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122018_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122018
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122018_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122018
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122018_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122018
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122018_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122018
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122019_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122019
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122019_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122019
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122019_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122019
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122019_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122019
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122019_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122019
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122019_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122019
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122019_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122019
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122020_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122020
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122020_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122020
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122020_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122020
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122020_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122020
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122020_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122020
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122020_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122020
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122020_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122020
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122021_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122021
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122021_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122021
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122021_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122021
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122021_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122021
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122021_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122021
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122021_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122021
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122021_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122021
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122022_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122022
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122022_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122022
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122022_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122022
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122022_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122022
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122022_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122022
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122022_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122022
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122022_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122022
        FOR VALUES IN ('QBDT');


CREATE TABLE ibobadm.psp_source_system_transmission_m012023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m012023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m012023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m012023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m012023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m012023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m012023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m012023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m012023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m012023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m012023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m012023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m012023
        FOR VALUES IN ('QBDT');


CREATE TABLE ibobadm.psp_source_system_transmission_m022023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m022023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m022023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m022023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m022023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m022023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m022023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m022023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m022023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m022023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m022023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m022023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m022023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m032023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m032023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m032023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m032023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m032023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m032023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m032023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m032023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m032023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m032023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m032023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m032023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m032023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m042023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m042023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m042023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m042023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m042023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m042023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m042023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m042023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m042023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m042023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m042023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m042023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m042023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m052023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m052023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m052023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m052023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m052023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m052023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m052023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m052023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m052023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m052023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m052023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m052023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m052023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m062023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m062023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m062023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m062023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m062023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m062023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m062023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m062023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m062023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m062023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m062023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m062023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m062023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m072023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m072023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m072023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m072023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m072023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m072023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m072023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m072023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m072023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m072023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m072023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m072023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m072023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m082023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m082023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m082023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m082023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m082023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m082023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m082023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m082023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m082023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m082023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m082023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m082023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m082023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m092023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m092023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m092023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m092023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m092023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m092023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m092023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m092023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m092023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m092023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m092023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m092023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m092023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m102023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m102023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m102023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m102023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m102023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m102023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m102023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m102023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m102023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m102023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m102023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m102023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m102023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m112023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m112023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m112023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m112023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m112023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m112023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m112023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m112023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m112023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m112023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m112023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m112023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m112023
        FOR VALUES IN ('QBDT');

CREATE TABLE ibobadm.psp_source_system_transmission_m122023_from_as400
        PARTITION OF ibobadm.psp_source_system_transmission_m122023
        FOR VALUES IN ('AS400');

CREATE TABLE ibobadm.psp_source_system_transmission_m122023_from_cris
        PARTITION OF ibobadm.psp_source_system_transmission_m122023
        FOR VALUES IN ('CRIS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122023_from_dflt
        PARTITION OF ibobadm.psp_source_system_transmission_m122023
        DEFAULT;

CREATE TABLE ibobadm.psp_source_system_transmission_m122023_from_ews
        PARTITION OF ibobadm.psp_source_system_transmission_m122023
        FOR VALUES IN ('EWS');

CREATE TABLE ibobadm.psp_source_system_transmission_m122023_from_null
        PARTITION OF ibobadm.psp_source_system_transmission_m122023
        FOR VALUES IN (NULL);

CREATE TABLE ibobadm.psp_source_system_transmission_m122023_from_psp
        PARTITION OF ibobadm.psp_source_system_transmission_m122023
        FOR VALUES IN ('PSP');

CREATE TABLE ibobadm.psp_source_system_transmission_m122023_from_qbdt
        PARTITION OF ibobadm.psp_source_system_transmission_m122023
        FOR VALUES IN ('QBDT');



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



