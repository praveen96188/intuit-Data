set search_path=ibobadm;

CREATE TABLE ibobadm.psp_source_system_transmission_9999
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2025-01-01 00:00:00') TO (MAXVALUE)
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m012022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-02-01 00:00:00') TO ('2011-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-02-01 00:00:00') TO ('2012-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-02-01 00:00:00') TO ('2013-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-02-01 00:00:00') TO ('2014-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-02-01 00:00:00') TO ('2015-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-02-01 00:00:00') TO ('2016-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-02-01 00:00:00') TO ('2017-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-02-01 00:00:00') TO ('2018-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-02-01 00:00:00') TO ('2019-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-02-01 00:00:00') TO ('2020-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-02-01 00:00:00') TO ('2021-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-02-01 00:00:00') TO ('2022-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-03-01 00:00:00') TO ('2011-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-03-01 00:00:00') TO ('2012-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-03-01 00:00:00') TO ('2013-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-03-01 00:00:00') TO ('2014-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-03-01 00:00:00') TO ('2015-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-03-01 00:00:00') TO ('2019-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-03-01 00:00:00') TO ('2020-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-03-01 00:00:00') TO ('2022-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-04-01 00:00:00') TO ('2011-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-04-01 00:00:00') TO ('2012-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-04-01 00:00:00') TO ('2013-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-04-01 00:00:00') TO ('2014-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-04-01 00:00:00') TO ('2015-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-04-01 00:00:00') TO ('2016-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-04-01 00:00:00') TO ('2017-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-04-01 00:00:00') TO ('2018-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-04-01 00:00:00') TO ('2019-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-04-01 00:00:00') TO ('2020-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-04-01 00:00:00') TO ('2021-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-04-01 00:00:00') TO ('2022-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-05-01 00:00:00') TO ('2011-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-05-01 00:00:00') TO ('2012-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-05-01 00:00:00') TO ('2013-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-05-01 00:00:00') TO ('2014-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-05-01 00:00:00') TO ('2015-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-05-01 00:00:00') TO ('2019-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-05-01 00:00:00') TO ('2020-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-05-01 00:00:00') TO ('2022-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-06-01 00:00:00') TO ('2011-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-06-01 00:00:00') TO ('2012-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-06-01 00:00:00') TO ('2013-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-06-01 00:00:00') TO ('2014-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-06-01 00:00:00') TO ('2015-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-06-01 00:00:00') TO ('2016-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-06-01 00:00:00') TO ('2017-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-06-01 00:00:00') TO ('2018-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-06-01 00:00:00') TO ('2019-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-06-01 00:00:00') TO ('2020-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-06-01 00:00:00') TO ('2021-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-06-01 00:00:00') TO ('2022-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM (MINVALUE) TO ('2009-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2011-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2012-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2013-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2014-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2015-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2019-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2020-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2022-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-08-01 00:00:00') TO ('2011-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-08-01 00:00:00') TO ('2012-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-08-01 00:00:00') TO ('2013-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-08-01 00:00:00') TO ('2014-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-08-01 00:00:00') TO ('2015-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-08-01 00:00:00') TO ('2016-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-08-01 00:00:00') TO ('2017-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-08-01 00:00:00') TO ('2018-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-08-01 00:00:00') TO ('2019-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-08-01 00:00:00') TO ('2020-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-08-01 00:00:00') TO ('2021-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-08-01 00:00:00') TO ('2022-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-09-01 00:00:00') TO ('2011-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-09-01 00:00:00') TO ('2012-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-09-01 00:00:00') TO ('2013-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-09-01 00:00:00') TO ('2014-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-09-01 00:00:00') TO ('2015-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-09-01 00:00:00') TO ('2019-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-09-01 00:00:00') TO ('2020-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-09-01 00:00:00') TO ('2022-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-10-01 00:00:00') TO ('2011-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-10-01 00:00:00') TO ('2012-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-10-01 00:00:00') TO ('2013-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-10-01 00:00:00') TO ('2014-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-10-01 00:00:00') TO ('2015-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-10-01 00:00:00') TO ('2016-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-10-01 00:00:00') TO ('2017-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-10-01 00:00:00') TO ('2018-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-10-01 00:00:00') TO ('2019-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-10-01 00:00:00') TO ('2020-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-10-01 00:00:00') TO ('2021-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-10-01 00:00:00') TO ('2022-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-11-01 00:00:00') TO ('2011-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-11-01 00:00:00') TO ('2012-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-11-01 00:00:00') TO ('2013-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-11-01 00:00:00') TO ('2014-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2015-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2016-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2017-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2018-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-11-01 00:00:00') TO ('2019-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-11-01 00:00:00') TO ('2020-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2021-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-11-01 00:00:00') TO ('2022-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122009
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122010
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122011
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-12-01 00:00:00') TO ('2012-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122012
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-12-01 00:00:00') TO ('2013-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122013
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-12-01 00:00:00') TO ('2014-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122014
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-12-01 00:00:00') TO ('2015-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122015
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-12-01 00:00:00') TO ('2016-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122016
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-12-01 00:00:00') TO ('2017-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122017
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-12-01 00:00:00') TO ('2018-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122018
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-12-01 00:00:00') TO ('2019-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122019
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-12-01 00:00:00') TO ('2020-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122020
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2020-12-01 00:00:00') TO ('2021-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122021
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2021-12-01 00:00:00') TO ('2022-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122022
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2022-12-01 00:00:00') TO ('2023-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);


CREATE TABLE ibobadm.psp_source_system_transmission_m012023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-01-01 00:00:00') TO ('2023-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m022023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-02-01 00:00:00') TO ('2023-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m032023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-03-01 00:00:00') TO ('2023-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m042023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-04-01 00:00:00') TO ('2023-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m052023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-05-01 00:00:00') TO ('2023-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m062023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-06-01 00:00:00') TO ('2023-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m072023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-07-01 00:00:00') TO ('2023-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m082023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-08-01 00:00:00') TO ('2023-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m092023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-09-01 00:00:00') TO ('2023-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m102023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-10-01 00:00:00') TO ('2023-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m112023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-11-01 00:00:00') TO ('2023-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);

CREATE TABLE ibobadm.psp_source_system_transmission_m122023
        PARTITION OF ibobadm.psp_source_system_transmission
        FOR VALUES FROM ('2023-12-01 00:00:00') TO ('2024-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



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
