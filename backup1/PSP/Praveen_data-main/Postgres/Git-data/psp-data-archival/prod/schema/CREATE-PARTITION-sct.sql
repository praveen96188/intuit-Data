-- ------------ Write CREATE-PARTITION-stage scripts -----------

CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_9999
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2023-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-02-01 00:00:00') TO ('2011-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-02-01 00:00:00') TO ('2012-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-02-01 00:00:00') TO ('2013-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-02-01 00:00:00') TO ('2014-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-02-01 00:00:00') TO ('2015-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-02-01 00:00:00') TO ('2016-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-02-01 00:00:00') TO ('2017-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-02-01 00:00:00') TO ('2018-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-02-01 00:00:00') TO ('2019-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-02-01 00:00:00') TO ('2020-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-02-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-02-01 00:00:00') TO ('2022-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-03-01 00:00:00') TO ('2011-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-03-01 00:00:00') TO ('2012-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-03-01 00:00:00') TO ('2013-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-03-01 00:00:00') TO ('2014-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-03-01 00:00:00') TO ('2015-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-03-01 00:00:00') TO ('2019-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-03-01 00:00:00') TO ('2020-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-03-01 00:00:00') TO ('2022-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-04-01 00:00:00') TO ('2011-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-04-01 00:00:00') TO ('2012-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-04-01 00:00:00') TO ('2013-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-04-01 00:00:00') TO ('2014-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-04-01 00:00:00') TO ('2015-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-04-01 00:00:00') TO ('2016-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-04-01 00:00:00') TO ('2017-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-04-01 00:00:00') TO ('2018-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-04-01 00:00:00') TO ('2019-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-04-01 00:00:00') TO ('2020-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-04-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-04-01 00:00:00') TO ('2022-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-05-01 00:00:00') TO ('2011-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-05-01 00:00:00') TO ('2012-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-05-01 00:00:00') TO ('2013-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-05-01 00:00:00') TO ('2014-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-05-01 00:00:00') TO ('2015-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-05-01 00:00:00') TO ('2019-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-05-01 00:00:00') TO ('2020-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-05-01 00:00:00') TO ('2022-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-06-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-06-01 00:00:00') TO ('2012-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-06-01 00:00:00') TO ('2013-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-06-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-06-01 00:00:00') TO ('2015-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-06-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-06-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-06-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-06-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-06-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-06-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-06-01 00:00:00') TO ('2022-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM (MINVALUE) TO ('2009-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2011-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2012-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2013-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2014-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2015-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2019-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2020-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2022-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-08-01 00:00:00') TO ('2011-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-08-01 00:00:00') TO ('2012-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-08-01 00:00:00') TO ('2013-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-08-01 00:00:00') TO ('2014-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-08-01 00:00:00') TO ('2015-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-08-01 00:00:00') TO ('2016-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-08-01 00:00:00') TO ('2017-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-08-01 00:00:00') TO ('2018-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-08-01 00:00:00') TO ('2019-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-08-01 00:00:00') TO ('2020-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-08-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-08-01 00:00:00') TO ('2022-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-09-01 00:00:00') TO ('2011-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-09-01 00:00:00') TO ('2012-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-09-01 00:00:00') TO ('2013-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-09-01 00:00:00') TO ('2014-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-09-01 00:00:00') TO ('2015-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-09-01 00:00:00') TO ('2019-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-09-01 00:00:00') TO ('2020-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-09-01 00:00:00') TO ('2022-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-10-01 00:00:00') TO ('2011-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-10-01 00:00:00') TO ('2012-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-10-01 00:00:00') TO ('2013-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-10-01 00:00:00') TO ('2014-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-10-01 00:00:00') TO ('2015-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-10-01 00:00:00') TO ('2016-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-10-01 00:00:00') TO ('2017-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-10-01 00:00:00') TO ('2018-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-10-01 00:00:00') TO ('2019-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-10-01 00:00:00') TO ('2020-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-10-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-10-01 00:00:00') TO ('2022-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-11-01 00:00:00') TO ('2011-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-11-01 00:00:00') TO ('2012-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-11-01 00:00:00') TO ('2013-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-11-01 00:00:00') TO ('2014-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2015-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2016-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2017-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2018-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-11-01 00:00:00') TO ('2019-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-11-01 00:00:00') TO ('2020-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2021-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-11-01 00:00:00') TO ('2022-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122011
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-12-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122012
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2012-12-01 00:00:00') TO ('2013-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122013
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2013-12-01 00:00:00') TO ('2014-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122014
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2014-12-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122015
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2015-12-01 00:00:00') TO ('2016-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122016
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2016-12-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122017
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2017-12-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122018
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2018-12-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122019
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2019-12-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122020
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2020-12-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-12-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122022
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-12-01 00:00:00') TO ('2023-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_9999
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2023-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-02-01 00:00:00') TO ('2011-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-02-01 00:00:00') TO ('2012-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-02-01 00:00:00') TO ('2013-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-02-01 00:00:00') TO ('2014-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-02-01 00:00:00') TO ('2015-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-02-01 00:00:00') TO ('2016-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-02-01 00:00:00') TO ('2017-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-02-01 00:00:00') TO ('2018-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-02-01 00:00:00') TO ('2019-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-02-01 00:00:00') TO ('2020-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-02-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-02-01 00:00:00') TO ('2022-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-03-01 00:00:00') TO ('2011-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-03-01 00:00:00') TO ('2012-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-03-01 00:00:00') TO ('2013-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-03-01 00:00:00') TO ('2014-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-03-01 00:00:00') TO ('2015-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-03-01 00:00:00') TO ('2019-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-03-01 00:00:00') TO ('2020-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-03-01 00:00:00') TO ('2022-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-04-01 00:00:00') TO ('2011-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-04-01 00:00:00') TO ('2012-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-04-01 00:00:00') TO ('2013-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-04-01 00:00:00') TO ('2014-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-04-01 00:00:00') TO ('2015-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-04-01 00:00:00') TO ('2016-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-04-01 00:00:00') TO ('2017-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-04-01 00:00:00') TO ('2018-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-04-01 00:00:00') TO ('2019-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-04-01 00:00:00') TO ('2020-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-04-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-04-01 00:00:00') TO ('2022-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-05-01 00:00:00') TO ('2011-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-05-01 00:00:00') TO ('2012-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-05-01 00:00:00') TO ('2013-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-05-01 00:00:00') TO ('2014-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-05-01 00:00:00') TO ('2015-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-05-01 00:00:00') TO ('2019-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-05-01 00:00:00') TO ('2020-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-05-01 00:00:00') TO ('2022-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-06-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-06-01 00:00:00') TO ('2012-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-06-01 00:00:00') TO ('2013-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-06-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-06-01 00:00:00') TO ('2015-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-06-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-06-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-06-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-06-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-06-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-06-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-06-01 00:00:00') TO ('2022-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM (MINVALUE) TO ('2009-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2011-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2012-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2013-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2014-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2015-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2019-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2020-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2022-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-08-01 00:00:00') TO ('2011-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-08-01 00:00:00') TO ('2012-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-08-01 00:00:00') TO ('2013-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-08-01 00:00:00') TO ('2014-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-08-01 00:00:00') TO ('2015-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-08-01 00:00:00') TO ('2016-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-08-01 00:00:00') TO ('2017-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-08-01 00:00:00') TO ('2018-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-08-01 00:00:00') TO ('2019-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-08-01 00:00:00') TO ('2020-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-08-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-08-01 00:00:00') TO ('2022-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-09-01 00:00:00') TO ('2011-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-09-01 00:00:00') TO ('2012-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-09-01 00:00:00') TO ('2013-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-09-01 00:00:00') TO ('2014-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-09-01 00:00:00') TO ('2015-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-09-01 00:00:00') TO ('2019-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-09-01 00:00:00') TO ('2020-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-09-01 00:00:00') TO ('2022-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-10-01 00:00:00') TO ('2011-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-10-01 00:00:00') TO ('2012-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-10-01 00:00:00') TO ('2013-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-10-01 00:00:00') TO ('2014-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-10-01 00:00:00') TO ('2015-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-10-01 00:00:00') TO ('2016-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-10-01 00:00:00') TO ('2017-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-10-01 00:00:00') TO ('2018-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-10-01 00:00:00') TO ('2019-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-10-01 00:00:00') TO ('2020-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-10-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-10-01 00:00:00') TO ('2022-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-11-01 00:00:00') TO ('2011-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-11-01 00:00:00') TO ('2012-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-11-01 00:00:00') TO ('2013-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-11-01 00:00:00') TO ('2014-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2015-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2016-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2017-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2018-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-11-01 00:00:00') TO ('2019-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-11-01 00:00:00') TO ('2020-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2021-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-11-01 00:00:00') TO ('2022-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122011
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-12-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122012
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2012-12-01 00:00:00') TO ('2013-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122013
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2013-12-01 00:00:00') TO ('2014-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122014
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2014-12-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122015
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2015-12-01 00:00:00') TO ('2016-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122016
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2016-12-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122017
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2017-12-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122018
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2018-12-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122019
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2019-12-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122020
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2020-12-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-12-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122022
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-12-01 00:00:00') TO ('2023-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_9999
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2023-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12011
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12012
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12013
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12014
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12015
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12016
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12017
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12018
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12019
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12020
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12022
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22011
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2011-03-01 00:00:00') TO ('2011-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22012
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2012-03-01 00:00:00') TO ('2012-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22013
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2013-03-01 00:00:00') TO ('2013-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22014
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2014-03-01 00:00:00') TO ('2014-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22015
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2015-03-01 00:00:00') TO ('2015-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22016
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22017
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22018
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22019
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2019-03-01 00:00:00') TO ('2019-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22020
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2020-03-01 00:00:00') TO ('2020-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22022
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2022-03-01 00:00:00') TO ('2022-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32011
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2011-05-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32012
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2012-05-01 00:00:00') TO ('2012-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32013
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2013-05-01 00:00:00') TO ('2013-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32014
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2014-05-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32015
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2015-05-01 00:00:00') TO ('2015-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32016
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32017
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32018
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32019
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2019-05-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32020
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2020-05-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32022
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2022-05-01 00:00:00') TO ('2022-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM (MINVALUE) TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42011
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2011-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42012
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2012-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42013
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2013-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42014
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2014-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42015
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2015-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42016
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42017
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42018
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42019
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2019-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42020
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2020-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42022
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2022-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52011
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2011-09-01 00:00:00') TO ('2011-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52012
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2012-09-01 00:00:00') TO ('2012-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52013
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2013-09-01 00:00:00') TO ('2013-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52014
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2014-09-01 00:00:00') TO ('2014-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52015
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2015-09-01 00:00:00') TO ('2015-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52016
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52017
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52018
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52019
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2019-09-01 00:00:00') TO ('2019-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52020
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2020-09-01 00:00:00') TO ('2020-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52022
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2022-09-01 00:00:00') TO ('2022-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62011
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2011-11-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62012
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2012-11-01 00:00:00') TO ('2013-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62013
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2013-11-01 00:00:00') TO ('2014-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62014
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2014-11-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62015
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2016-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62016
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62017
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62018
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62019
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2019-11-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62020
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2020-11-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62022
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2022-11-01 00:00:00') TO ('2023-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_9999
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2023-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12009
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM (MINVALUE) TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12010
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12011
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12012
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12013
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12014
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12015
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12016
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12017
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12018
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12019
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12020
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12021
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12022
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22009
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22010
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22011
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22012
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2013-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22013
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2014-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22014
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22015
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2016-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22016
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22017
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22018
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22019
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22020
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22021
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22022
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2023-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_9999
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2023-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12011
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12012
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12013
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12014
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12015
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12016
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12017
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12018
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12019
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12020
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12022
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22011
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2011-03-01 00:00:00') TO ('2011-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22012
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2012-03-01 00:00:00') TO ('2012-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22013
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2013-03-01 00:00:00') TO ('2013-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22014
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2014-03-01 00:00:00') TO ('2014-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22015
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2015-03-01 00:00:00') TO ('2015-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22016
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22017
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22018
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22019
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2019-03-01 00:00:00') TO ('2019-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22020
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2020-03-01 00:00:00') TO ('2020-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22022
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2022-03-01 00:00:00') TO ('2022-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32011
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2011-05-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32012
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2012-05-01 00:00:00') TO ('2012-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32013
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2013-05-01 00:00:00') TO ('2013-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32014
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2014-05-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32015
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2015-05-01 00:00:00') TO ('2015-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32016
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32017
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32018
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32019
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2019-05-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32020
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2020-05-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32022
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2022-05-01 00:00:00') TO ('2022-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM (MINVALUE) TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42011
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2011-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42012
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2012-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42013
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2013-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42014
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2014-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42015
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2015-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42016
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42017
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42018
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42019
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2019-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42020
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2020-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42022
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2022-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52011
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2011-09-01 00:00:00') TO ('2011-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52012
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2012-09-01 00:00:00') TO ('2012-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52013
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2013-09-01 00:00:00') TO ('2013-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52014
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2014-09-01 00:00:00') TO ('2014-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52015
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2015-09-01 00:00:00') TO ('2015-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52016
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52017
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52018
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52019
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2019-09-01 00:00:00') TO ('2019-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52020
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2020-09-01 00:00:00') TO ('2020-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52022
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2022-09-01 00:00:00') TO ('2022-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62011
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2011-11-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62012
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2012-11-01 00:00:00') TO ('2013-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62013
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2013-11-01 00:00:00') TO ('2014-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62014
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2014-11-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62015
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2016-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62016
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62017
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62018
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62019
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2019-11-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62020
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2020-11-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62022
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2022-11-01 00:00:00') TO ('2023-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_9999
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2023-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12010
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12011
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12012
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12013
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12014
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12015
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12016
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12017
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12018
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12019
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12020
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12021
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12022
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22009
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM (MINVALUE) TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22010
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22011
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22012
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2013-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22013
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2014-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22014
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22015
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2016-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22016
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22017
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22018
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22019
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22020
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22021
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22022
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2023-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_9999
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2023-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12010
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12011
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12012
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12013
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12014
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12015
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12016
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12017
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12018
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12019
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12020
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12021
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12022
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2022-01-01 00:00:00') TO ('2022-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22009
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM (MINVALUE) TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22010
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22011
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22012
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2013-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22013
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2014-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22014
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22015
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2016-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22016
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22017
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22018
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22019
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22020
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22021
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22022
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2022-07-01 00:00:00') TO ('2023-01-01 00:00:00');



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_9999
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2019-01-01 00:00:00') TO (MAXVALUE)
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2012-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-01-01 00:00:00') TO ('2013-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-01-01 00:00:00') TO ('2014-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-01-01 00:00:00') TO ('2016-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m012018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-02-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-02-01 00:00:00') TO ('2011-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-02-01 00:00:00') TO ('2012-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-02-01 00:00:00') TO ('2013-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-02-01 00:00:00') TO ('2014-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-02-01 00:00:00') TO ('2015-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-02-01 00:00:00') TO ('2016-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-02-01 00:00:00') TO ('2017-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m022018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-02-01 00:00:00') TO ('2018-03-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-03-01 00:00:00') TO ('2011-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-03-01 00:00:00') TO ('2012-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-03-01 00:00:00') TO ('2013-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-03-01 00:00:00') TO ('2014-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-03-01 00:00:00') TO ('2015-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m032018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-04-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-04-01 00:00:00') TO ('2011-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-04-01 00:00:00') TO ('2012-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-04-01 00:00:00') TO ('2013-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-04-01 00:00:00') TO ('2014-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-04-01 00:00:00') TO ('2015-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-04-01 00:00:00') TO ('2016-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-04-01 00:00:00') TO ('2017-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m042018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-04-01 00:00:00') TO ('2018-05-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-05-01 00:00:00') TO ('2011-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-05-01 00:00:00') TO ('2012-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-05-01 00:00:00') TO ('2013-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-05-01 00:00:00') TO ('2014-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-05-01 00:00:00') TO ('2015-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m052018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-06-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-06-01 00:00:00') TO ('2011-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-06-01 00:00:00') TO ('2012-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-06-01 00:00:00') TO ('2013-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-06-01 00:00:00') TO ('2014-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-06-01 00:00:00') TO ('2015-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-06-01 00:00:00') TO ('2016-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-06-01 00:00:00') TO ('2017-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m062018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-06-01 00:00:00') TO ('2018-07-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072009
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM (MINVALUE) TO ('2009-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2011-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-07-01 00:00:00') TO ('2012-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-07-01 00:00:00') TO ('2013-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2014-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-07-01 00:00:00') TO ('2015-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m072018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-08-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082009
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-08-01 00:00:00') TO ('2011-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-08-01 00:00:00') TO ('2012-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-08-01 00:00:00') TO ('2013-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-08-01 00:00:00') TO ('2014-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-08-01 00:00:00') TO ('2015-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-08-01 00:00:00') TO ('2016-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-08-01 00:00:00') TO ('2017-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m082018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-08-01 00:00:00') TO ('2018-09-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092009
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-09-01 00:00:00') TO ('2011-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-09-01 00:00:00') TO ('2012-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-09-01 00:00:00') TO ('2013-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-09-01 00:00:00') TO ('2014-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-09-01 00:00:00') TO ('2015-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m092018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-10-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102009
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-10-01 00:00:00') TO ('2011-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-10-01 00:00:00') TO ('2012-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-10-01 00:00:00') TO ('2013-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-10-01 00:00:00') TO ('2014-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-10-01 00:00:00') TO ('2015-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-10-01 00:00:00') TO ('2016-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-10-01 00:00:00') TO ('2017-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m102018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-10-01 00:00:00') TO ('2018-11-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112009
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-11-01 00:00:00') TO ('2011-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-11-01 00:00:00') TO ('2012-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-11-01 00:00:00') TO ('2013-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-11-01 00:00:00') TO ('2014-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2015-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2016-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2017-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m112018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2018-12-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122009
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122010
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122011
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2011-12-01 00:00:00') TO ('2012-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122012
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2012-12-01 00:00:00') TO ('2013-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122013
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2013-12-01 00:00:00') TO ('2014-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122014
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2014-12-01 00:00:00') TO ('2015-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122015
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2015-12-01 00:00:00') TO ('2016-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122016
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2016-12-01 00:00:00') TO ('2017-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122017
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2017-12-01 00:00:00') TO ('2018-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.psp_source_system_transmission_srcsystrns_m122018
        PARTITION OF pspadm.psp_source_system_transmission
        FOR VALUES FROM ('2018-12-01 00:00:00') TO ('2019-01-01 00:00:00')
    PARTITION BY LIST (from_source_system);



CREATE TABLE pspadm.t_pceep_old_default
        PARTITION OF pspadm.t_pceep_old
        DEFAULT;



CREATE TABLE pspadm.t_pceep_old_sys_p18304
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 0);



CREATE TABLE pspadm.t_pceep_old_sys_p18305
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 1);



CREATE TABLE pspadm.t_pceep_old_sys_p18306
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 2);



CREATE TABLE pspadm.t_pceep_old_sys_p18307
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 3);



CREATE TABLE pspadm.t_pceep_old_sys_p18308
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 4);



CREATE TABLE pspadm.t_pceep_old_sys_p18309
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 5);



CREATE TABLE pspadm.t_pceep_old_sys_p18310
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 6);



CREATE TABLE pspadm.t_pceep_old_sys_p18311
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 7);



CREATE TABLE pspadm.t_pceep_old_sys_p18312
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 8);



CREATE TABLE pspadm.t_pceep_old_sys_p18313
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 9);



CREATE TABLE pspadm.t_pceep_old_sys_p18314
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 10);



CREATE TABLE pspadm.t_pceep_old_sys_p18315
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 11);



CREATE TABLE pspadm.t_pceep_old_sys_p18316
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 12);



CREATE TABLE pspadm.t_pceep_old_sys_p18317
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 13);



CREATE TABLE pspadm.t_pceep_old_sys_p18318
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 14);



CREATE TABLE pspadm.t_pceep_old_sys_p18319
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 15);



CREATE TABLE pspadm.t_pceep_old_sys_p18320
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 16);



CREATE TABLE pspadm.t_pceep_old_sys_p18321
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 17);



CREATE TABLE pspadm.t_pceep_old_sys_p18322
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 18);



CREATE TABLE pspadm.t_pceep_old_sys_p18323
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 19);



CREATE TABLE pspadm.t_pceep_old_sys_p18324
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 20);



CREATE TABLE pspadm.t_pceep_old_sys_p18325
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 21);



CREATE TABLE pspadm.t_pceep_old_sys_p18326
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 22);



CREATE TABLE pspadm.t_pceep_old_sys_p18327
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 23);



CREATE TABLE pspadm.t_pceep_old_sys_p18328
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 24);



CREATE TABLE pspadm.t_pceep_old_sys_p18329
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 25);



CREATE TABLE pspadm.t_pceep_old_sys_p18330
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 26);



CREATE TABLE pspadm.t_pceep_old_sys_p18331
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 27);



CREATE TABLE pspadm.t_pceep_old_sys_p18332
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 28);



CREATE TABLE pspadm.t_pceep_old_sys_p18333
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 29);



CREATE TABLE pspadm.t_pceep_old_sys_p18334
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 30);



CREATE TABLE pspadm.t_pceep_old_sys_p18335
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 31);



CREATE TABLE pspadm.t_pceep_old_sys_p18336
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 32);



CREATE TABLE pspadm.t_pceep_old_sys_p18337
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 33);



CREATE TABLE pspadm.t_pceep_old_sys_p18338
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 34);



CREATE TABLE pspadm.t_pceep_old_sys_p18339
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 35);



CREATE TABLE pspadm.t_pceep_old_sys_p18340
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 36);



CREATE TABLE pspadm.t_pceep_old_sys_p18341
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 37);



CREATE TABLE pspadm.t_pceep_old_sys_p18342
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 38);



CREATE TABLE pspadm.t_pceep_old_sys_p18343
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 39);



CREATE TABLE pspadm.t_pceep_old_sys_p18344
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 40);



CREATE TABLE pspadm.t_pceep_old_sys_p18345
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 41);



CREATE TABLE pspadm.t_pceep_old_sys_p18346
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 42);



CREATE TABLE pspadm.t_pceep_old_sys_p18347
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 43);



CREATE TABLE pspadm.t_pceep_old_sys_p18348
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 44);



CREATE TABLE pspadm.t_pceep_old_sys_p18349
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 45);



CREATE TABLE pspadm.t_pceep_old_sys_p18350
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 46);



CREATE TABLE pspadm.t_pceep_old_sys_p18351
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 47);



CREATE TABLE pspadm.t_pceep_old_sys_p18352
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 48);



CREATE TABLE pspadm.t_pceep_old_sys_p18353
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 49);



CREATE TABLE pspadm.t_pceep_old_sys_p18354
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 50);



CREATE TABLE pspadm.t_pceep_old_sys_p18355
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 51);



CREATE TABLE pspadm.t_pceep_old_sys_p18356
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 52);



CREATE TABLE pspadm.t_pceep_old_sys_p18357
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 53);



CREATE TABLE pspadm.t_pceep_old_sys_p18358
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 54);



CREATE TABLE pspadm.t_pceep_old_sys_p18359
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 55);



CREATE TABLE pspadm.t_pceep_old_sys_p18360
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 56);



CREATE TABLE pspadm.t_pceep_old_sys_p18361
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 57);



CREATE TABLE pspadm.t_pceep_old_sys_p18362
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 58);



CREATE TABLE pspadm.t_pceep_old_sys_p18363
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 59);



CREATE TABLE pspadm.t_pceep_old_sys_p18364
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 60);



CREATE TABLE pspadm.t_pceep_old_sys_p18365
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 61);



CREATE TABLE pspadm.t_pceep_old_sys_p18366
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 62);



CREATE TABLE pspadm.t_pceep_old_sys_p18367
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 63);



CREATE TABLE pspadm.t_pceep_old_sys_p18368
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 64);



CREATE TABLE pspadm.t_pceep_old_sys_p18369
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 65);



CREATE TABLE pspadm.t_pceep_old_sys_p18370
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 66);



CREATE TABLE pspadm.t_pceep_old_sys_p18371
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 67);



CREATE TABLE pspadm.t_pceep_old_sys_p18372
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 68);



CREATE TABLE pspadm.t_pceep_old_sys_p18373
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 69);



CREATE TABLE pspadm.t_pceep_old_sys_p18374
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 70);



CREATE TABLE pspadm.t_pceep_old_sys_p18375
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 71);



CREATE TABLE pspadm.t_pceep_old_sys_p18376
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 72);



CREATE TABLE pspadm.t_pceep_old_sys_p18377
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 73);



CREATE TABLE pspadm.t_pceep_old_sys_p18378
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 74);



CREATE TABLE pspadm.t_pceep_old_sys_p18379
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 75);



CREATE TABLE pspadm.t_pceep_old_sys_p18380
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 76);



CREATE TABLE pspadm.t_pceep_old_sys_p18381
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 77);



CREATE TABLE pspadm.t_pceep_old_sys_p18382
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 78);



CREATE TABLE pspadm.t_pceep_old_sys_p18383
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 79);



CREATE TABLE pspadm.t_pceep_old_sys_p18384
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 80);



CREATE TABLE pspadm.t_pceep_old_sys_p18385
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 81);



CREATE TABLE pspadm.t_pceep_old_sys_p18386
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 82);



CREATE TABLE pspadm.t_pceep_old_sys_p18387
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 83);



CREATE TABLE pspadm.t_pceep_old_sys_p18388
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 84);



CREATE TABLE pspadm.t_pceep_old_sys_p18389
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 85);



CREATE TABLE pspadm.t_pceep_old_sys_p18390
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 86);



CREATE TABLE pspadm.t_pceep_old_sys_p18391
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 87);



CREATE TABLE pspadm.t_pceep_old_sys_p18392
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 88);



CREATE TABLE pspadm.t_pceep_old_sys_p18393
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 89);



CREATE TABLE pspadm.t_pceep_old_sys_p18394
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 90);



CREATE TABLE pspadm.t_pceep_old_sys_p18395
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 91);



CREATE TABLE pspadm.t_pceep_old_sys_p18396
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 92);



CREATE TABLE pspadm.t_pceep_old_sys_p18397
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 93);



CREATE TABLE pspadm.t_pceep_old_sys_p18398
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 94);



CREATE TABLE pspadm.t_pceep_old_sys_p18399
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 95);



CREATE TABLE pspadm.t_pceep_old_sys_p18400
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 96);



CREATE TABLE pspadm.t_pceep_old_sys_p18401
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 97);



CREATE TABLE pspadm.t_pceep_old_sys_p18402
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 98);



CREATE TABLE pspadm.t_pceep_old_sys_p18403
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 99);



CREATE TABLE pspadm.t_pceep_old_sys_p18404
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 100);



CREATE TABLE pspadm.t_pceep_old_sys_p18405
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 101);



CREATE TABLE pspadm.t_pceep_old_sys_p18406
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 102);



CREATE TABLE pspadm.t_pceep_old_sys_p18407
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 103);



CREATE TABLE pspadm.t_pceep_old_sys_p18408
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 104);



CREATE TABLE pspadm.t_pceep_old_sys_p18409
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 105);



CREATE TABLE pspadm.t_pceep_old_sys_p18410
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 106);



CREATE TABLE pspadm.t_pceep_old_sys_p18411
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 107);



CREATE TABLE pspadm.t_pceep_old_sys_p18412
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 108);



CREATE TABLE pspadm.t_pceep_old_sys_p18413
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 109);



CREATE TABLE pspadm.t_pceep_old_sys_p18414
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 110);



CREATE TABLE pspadm.t_pceep_old_sys_p18415
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 111);



CREATE TABLE pspadm.t_pceep_old_sys_p18416
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 112);



CREATE TABLE pspadm.t_pceep_old_sys_p18417
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 113);



CREATE TABLE pspadm.t_pceep_old_sys_p18418
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 114);



CREATE TABLE pspadm.t_pceep_old_sys_p18419
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 115);



CREATE TABLE pspadm.t_pceep_old_sys_p18420
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 116);



CREATE TABLE pspadm.t_pceep_old_sys_p18421
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 117);



CREATE TABLE pspadm.t_pceep_old_sys_p18422
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 118);



CREATE TABLE pspadm.t_pceep_old_sys_p18423
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 119);



CREATE TABLE pspadm.t_pceep_old_sys_p18424
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 120);



CREATE TABLE pspadm.t_pceep_old_sys_p18425
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 121);



CREATE TABLE pspadm.t_pceep_old_sys_p18426
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 122);



CREATE TABLE pspadm.t_pceep_old_sys_p18427
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 123);



CREATE TABLE pspadm.t_pceep_old_sys_p18428
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 124);



CREATE TABLE pspadm.t_pceep_old_sys_p18429
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 125);



CREATE TABLE pspadm.t_pceep_old_sys_p18430
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 126);



CREATE TABLE pspadm.t_pceep_old_sys_p18431
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 127);



CREATE TABLE pspadm.t_pceep_old_sys_p18432
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 128);



CREATE TABLE pspadm.t_pceep_old_sys_p18433
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 129);



CREATE TABLE pspadm.t_pceep_old_sys_p18434
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 130);



CREATE TABLE pspadm.t_pceep_old_sys_p18435
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 131);



CREATE TABLE pspadm.t_pceep_old_sys_p18436
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 132);



CREATE TABLE pspadm.t_pceep_old_sys_p18437
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 133);



CREATE TABLE pspadm.t_pceep_old_sys_p18438
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 134);



CREATE TABLE pspadm.t_pceep_old_sys_p18439
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 135);



CREATE TABLE pspadm.t_pceep_old_sys_p18440
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 136);



CREATE TABLE pspadm.t_pceep_old_sys_p18441
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 137);



CREATE TABLE pspadm.t_pceep_old_sys_p18442
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 138);



CREATE TABLE pspadm.t_pceep_old_sys_p18443
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 139);



CREATE TABLE pspadm.t_pceep_old_sys_p18444
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 140);



CREATE TABLE pspadm.t_pceep_old_sys_p18445
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 141);



CREATE TABLE pspadm.t_pceep_old_sys_p18446
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 142);



CREATE TABLE pspadm.t_pceep_old_sys_p18447
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 143);



CREATE TABLE pspadm.t_pceep_old_sys_p18448
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 144);



CREATE TABLE pspadm.t_pceep_old_sys_p18449
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 145);



CREATE TABLE pspadm.t_pceep_old_sys_p18450
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 146);



CREATE TABLE pspadm.t_pceep_old_sys_p18451
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 147);



CREATE TABLE pspadm.t_pceep_old_sys_p18452
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 148);



CREATE TABLE pspadm.t_pceep_old_sys_p18453
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 149);



CREATE TABLE pspadm.t_pceep_old_sys_p18454
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 150);



CREATE TABLE pspadm.t_pceep_old_sys_p18455
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 151);



CREATE TABLE pspadm.t_pceep_old_sys_p18456
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 152);



CREATE TABLE pspadm.t_pceep_old_sys_p18457
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 153);



CREATE TABLE pspadm.t_pceep_old_sys_p18458
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 154);



CREATE TABLE pspadm.t_pceep_old_sys_p18459
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 155);



CREATE TABLE pspadm.t_pceep_old_sys_p18460
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 156);



CREATE TABLE pspadm.t_pceep_old_sys_p18461
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 157);



CREATE TABLE pspadm.t_pceep_old_sys_p18462
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 158);



CREATE TABLE pspadm.t_pceep_old_sys_p18463
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 159);



CREATE TABLE pspadm.t_pceep_old_sys_p18464
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 160);



CREATE TABLE pspadm.t_pceep_old_sys_p18465
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 161);



CREATE TABLE pspadm.t_pceep_old_sys_p18466
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 162);



CREATE TABLE pspadm.t_pceep_old_sys_p18467
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 163);



CREATE TABLE pspadm.t_pceep_old_sys_p18468
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 164);



CREATE TABLE pspadm.t_pceep_old_sys_p18469
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 165);



CREATE TABLE pspadm.t_pceep_old_sys_p18470
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 166);



CREATE TABLE pspadm.t_pceep_old_sys_p18471
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 167);



CREATE TABLE pspadm.t_pceep_old_sys_p18472
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 168);



CREATE TABLE pspadm.t_pceep_old_sys_p18473
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 169);



CREATE TABLE pspadm.t_pceep_old_sys_p18474
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 170);



CREATE TABLE pspadm.t_pceep_old_sys_p18475
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 171);



CREATE TABLE pspadm.t_pceep_old_sys_p18476
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 172);



CREATE TABLE pspadm.t_pceep_old_sys_p18477
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 173);



CREATE TABLE pspadm.t_pceep_old_sys_p18478
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 174);



CREATE TABLE pspadm.t_pceep_old_sys_p18479
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 175);



CREATE TABLE pspadm.t_pceep_old_sys_p18480
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 176);



CREATE TABLE pspadm.t_pceep_old_sys_p18481
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 177);



CREATE TABLE pspadm.t_pceep_old_sys_p18482
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 178);



CREATE TABLE pspadm.t_pceep_old_sys_p18483
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 179);



CREATE TABLE pspadm.t_pceep_old_sys_p18484
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 180);



CREATE TABLE pspadm.t_pceep_old_sys_p18485
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 181);



CREATE TABLE pspadm.t_pceep_old_sys_p18486
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 182);



CREATE TABLE pspadm.t_pceep_old_sys_p18487
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 183);



CREATE TABLE pspadm.t_pceep_old_sys_p18488
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 184);



CREATE TABLE pspadm.t_pceep_old_sys_p18489
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 185);



CREATE TABLE pspadm.t_pceep_old_sys_p18490
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 186);



CREATE TABLE pspadm.t_pceep_old_sys_p18491
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 187);



CREATE TABLE pspadm.t_pceep_old_sys_p18492
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 188);



CREATE TABLE pspadm.t_pceep_old_sys_p18493
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 189);



CREATE TABLE pspadm.t_pceep_old_sys_p18494
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 190);



CREATE TABLE pspadm.t_pceep_old_sys_p18495
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 191);



CREATE TABLE pspadm.t_pceep_old_sys_p18496
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 192);



CREATE TABLE pspadm.t_pceep_old_sys_p18497
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 193);



CREATE TABLE pspadm.t_pceep_old_sys_p18498
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 194);



CREATE TABLE pspadm.t_pceep_old_sys_p18499
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 195);



CREATE TABLE pspadm.t_pceep_old_sys_p18500
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 196);



CREATE TABLE pspadm.t_pceep_old_sys_p18501
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 197);



CREATE TABLE pspadm.t_pceep_old_sys_p18502
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 198);



CREATE TABLE pspadm.t_pceep_old_sys_p18503
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 199);



CREATE TABLE pspadm.t_pceep_old_sys_p18504
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 200);



CREATE TABLE pspadm.t_pceep_old_sys_p18505
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 201);



CREATE TABLE pspadm.t_pceep_old_sys_p18506
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 202);



CREATE TABLE pspadm.t_pceep_old_sys_p18507
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 203);



CREATE TABLE pspadm.t_pceep_old_sys_p18508
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 204);



CREATE TABLE pspadm.t_pceep_old_sys_p18509
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 205);



CREATE TABLE pspadm.t_pceep_old_sys_p18510
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 206);



CREATE TABLE pspadm.t_pceep_old_sys_p18511
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 207);



CREATE TABLE pspadm.t_pceep_old_sys_p18512
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 208);



CREATE TABLE pspadm.t_pceep_old_sys_p18513
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 209);



CREATE TABLE pspadm.t_pceep_old_sys_p18514
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 210);



CREATE TABLE pspadm.t_pceep_old_sys_p18515
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 211);



CREATE TABLE pspadm.t_pceep_old_sys_p18516
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 212);



CREATE TABLE pspadm.t_pceep_old_sys_p18517
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 213);



CREATE TABLE pspadm.t_pceep_old_sys_p18518
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 214);



CREATE TABLE pspadm.t_pceep_old_sys_p18519
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 215);



CREATE TABLE pspadm.t_pceep_old_sys_p18520
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 216);



CREATE TABLE pspadm.t_pceep_old_sys_p18521
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 217);



CREATE TABLE pspadm.t_pceep_old_sys_p18522
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 218);



CREATE TABLE pspadm.t_pceep_old_sys_p18523
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 219);



CREATE TABLE pspadm.t_pceep_old_sys_p18524
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 220);



CREATE TABLE pspadm.t_pceep_old_sys_p18525
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 221);



CREATE TABLE pspadm.t_pceep_old_sys_p18526
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 222);



CREATE TABLE pspadm.t_pceep_old_sys_p18527
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 223);



CREATE TABLE pspadm.t_pceep_old_sys_p18528
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 224);



CREATE TABLE pspadm.t_pceep_old_sys_p18529
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 225);



CREATE TABLE pspadm.t_pceep_old_sys_p18530
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 226);



CREATE TABLE pspadm.t_pceep_old_sys_p18531
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 227);



CREATE TABLE pspadm.t_pceep_old_sys_p18532
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 228);



CREATE TABLE pspadm.t_pceep_old_sys_p18533
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 229);



CREATE TABLE pspadm.t_pceep_old_sys_p18534
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 230);



CREATE TABLE pspadm.t_pceep_old_sys_p18535
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 231);



CREATE TABLE pspadm.t_pceep_old_sys_p18536
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 232);



CREATE TABLE pspadm.t_pceep_old_sys_p18537
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 233);



CREATE TABLE pspadm.t_pceep_old_sys_p18538
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 234);



CREATE TABLE pspadm.t_pceep_old_sys_p18539
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 235);



CREATE TABLE pspadm.t_pceep_old_sys_p18540
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 236);



CREATE TABLE pspadm.t_pceep_old_sys_p18541
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 237);



CREATE TABLE pspadm.t_pceep_old_sys_p18542
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 238);



CREATE TABLE pspadm.t_pceep_old_sys_p18543
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 239);



CREATE TABLE pspadm.t_pceep_old_sys_p18544
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 240);



CREATE TABLE pspadm.t_pceep_old_sys_p18545
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 241);



CREATE TABLE pspadm.t_pceep_old_sys_p18546
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 242);



CREATE TABLE pspadm.t_pceep_old_sys_p18547
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 243);



CREATE TABLE pspadm.t_pceep_old_sys_p18548
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 244);



CREATE TABLE pspadm.t_pceep_old_sys_p18549
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 245);



CREATE TABLE pspadm.t_pceep_old_sys_p18550
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 246);



CREATE TABLE pspadm.t_pceep_old_sys_p18551
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 247);



CREATE TABLE pspadm.t_pceep_old_sys_p18552
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 248);



CREATE TABLE pspadm.t_pceep_old_sys_p18553
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 249);



CREATE TABLE pspadm.t_pceep_old_sys_p18554
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 250);



CREATE TABLE pspadm.t_pceep_old_sys_p18555
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 251);



CREATE TABLE pspadm.t_pceep_old_sys_p18556
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 252);



CREATE TABLE pspadm.t_pceep_old_sys_p18557
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 253);



CREATE TABLE pspadm.t_pceep_old_sys_p18558
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 254);



CREATE TABLE pspadm.t_pceep_old_sys_p18559
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 255);



CREATE TABLE pspadm.t_pceep_old_sys_p18560
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 256);



CREATE TABLE pspadm.t_pceep_old_sys_p18561
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 257);



CREATE TABLE pspadm.t_pceep_old_sys_p18562
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 258);



CREATE TABLE pspadm.t_pceep_old_sys_p18563
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 259);



CREATE TABLE pspadm.t_pceep_old_sys_p18564
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 260);



CREATE TABLE pspadm.t_pceep_old_sys_p18565
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 261);



CREATE TABLE pspadm.t_pceep_old_sys_p18566
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 262);



CREATE TABLE pspadm.t_pceep_old_sys_p18567
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 263);



CREATE TABLE pspadm.t_pceep_old_sys_p18568
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 264);



CREATE TABLE pspadm.t_pceep_old_sys_p18569
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 265);



CREATE TABLE pspadm.t_pceep_old_sys_p18570
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 266);



CREATE TABLE pspadm.t_pceep_old_sys_p18571
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 267);



CREATE TABLE pspadm.t_pceep_old_sys_p18572
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 268);



CREATE TABLE pspadm.t_pceep_old_sys_p18573
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 269);



CREATE TABLE pspadm.t_pceep_old_sys_p18574
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 270);



CREATE TABLE pspadm.t_pceep_old_sys_p18575
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 271);



CREATE TABLE pspadm.t_pceep_old_sys_p18576
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 272);



CREATE TABLE pspadm.t_pceep_old_sys_p18577
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 273);



CREATE TABLE pspadm.t_pceep_old_sys_p18578
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 274);



CREATE TABLE pspadm.t_pceep_old_sys_p18579
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 275);



CREATE TABLE pspadm.t_pceep_old_sys_p18580
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 276);



CREATE TABLE pspadm.t_pceep_old_sys_p18581
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 277);



CREATE TABLE pspadm.t_pceep_old_sys_p18582
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 278);



CREATE TABLE pspadm.t_pceep_old_sys_p18583
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 279);



CREATE TABLE pspadm.t_pceep_old_sys_p18584
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 280);



CREATE TABLE pspadm.t_pceep_old_sys_p18585
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 281);



CREATE TABLE pspadm.t_pceep_old_sys_p18586
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 282);



CREATE TABLE pspadm.t_pceep_old_sys_p18587
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 283);



CREATE TABLE pspadm.t_pceep_old_sys_p18588
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 284);



CREATE TABLE pspadm.t_pceep_old_sys_p18589
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 285);



CREATE TABLE pspadm.t_pceep_old_sys_p18590
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 286);



CREATE TABLE pspadm.t_pceep_old_sys_p18591
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 287);



CREATE TABLE pspadm.t_pceep_old_sys_p18592
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 288);



CREATE TABLE pspadm.t_pceep_old_sys_p18593
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 289);



CREATE TABLE pspadm.t_pceep_old_sys_p18594
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 290);



CREATE TABLE pspadm.t_pceep_old_sys_p18595
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 291);



CREATE TABLE pspadm.t_pceep_old_sys_p18596
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 292);



CREATE TABLE pspadm.t_pceep_old_sys_p18597
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 293);



CREATE TABLE pspadm.t_pceep_old_sys_p18598
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 294);



CREATE TABLE pspadm.t_pceep_old_sys_p18599
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 295);



CREATE TABLE pspadm.t_pceep_old_sys_p18600
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 296);



CREATE TABLE pspadm.t_pceep_old_sys_p18601
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 297);



CREATE TABLE pspadm.t_pceep_old_sys_p18602
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 298);



CREATE TABLE pspadm.t_pceep_old_sys_p18603
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 299);



CREATE TABLE pspadm.t_pceep_old_sys_p18604
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 300);



CREATE TABLE pspadm.t_pceep_old_sys_p18605
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 301);



CREATE TABLE pspadm.t_pceep_old_sys_p18606
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 302);



CREATE TABLE pspadm.t_pceep_old_sys_p18607
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 303);



CREATE TABLE pspadm.t_pceep_old_sys_p18608
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 304);



CREATE TABLE pspadm.t_pceep_old_sys_p18609
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 305);



CREATE TABLE pspadm.t_pceep_old_sys_p18610
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 306);



CREATE TABLE pspadm.t_pceep_old_sys_p18611
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 307);



CREATE TABLE pspadm.t_pceep_old_sys_p18612
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 308);



CREATE TABLE pspadm.t_pceep_old_sys_p18613
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 309);



CREATE TABLE pspadm.t_pceep_old_sys_p18614
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 310);



CREATE TABLE pspadm.t_pceep_old_sys_p18615
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 311);



CREATE TABLE pspadm.t_pceep_old_sys_p18616
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 312);



CREATE TABLE pspadm.t_pceep_old_sys_p18617
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 313);



CREATE TABLE pspadm.t_pceep_old_sys_p18618
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 314);



CREATE TABLE pspadm.t_pceep_old_sys_p18619
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 315);



CREATE TABLE pspadm.t_pceep_old_sys_p18620
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 316);



CREATE TABLE pspadm.t_pceep_old_sys_p18621
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 317);



CREATE TABLE pspadm.t_pceep_old_sys_p18622
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 318);



CREATE TABLE pspadm.t_pceep_old_sys_p18623
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 319);



CREATE TABLE pspadm.t_pceep_old_sys_p18624
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 320);



CREATE TABLE pspadm.t_pceep_old_sys_p18625
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 321);



CREATE TABLE pspadm.t_pceep_old_sys_p18626
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 322);



CREATE TABLE pspadm.t_pceep_old_sys_p18627
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 323);



CREATE TABLE pspadm.t_pceep_old_sys_p18628
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 324);



CREATE TABLE pspadm.t_pceep_old_sys_p18629
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 325);



CREATE TABLE pspadm.t_pceep_old_sys_p18630
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 326);



CREATE TABLE pspadm.t_pceep_old_sys_p18631
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 327);



CREATE TABLE pspadm.t_pceep_old_sys_p18632
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 328);



CREATE TABLE pspadm.t_pceep_old_sys_p18633
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 329);



CREATE TABLE pspadm.t_pceep_old_sys_p18634
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 330);



CREATE TABLE pspadm.t_pceep_old_sys_p18635
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 331);



CREATE TABLE pspadm.t_pceep_old_sys_p18636
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 332);



CREATE TABLE pspadm.t_pceep_old_sys_p18637
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 333);



CREATE TABLE pspadm.t_pceep_old_sys_p18638
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 334);



CREATE TABLE pspadm.t_pceep_old_sys_p18639
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 335);



CREATE TABLE pspadm.t_pceep_old_sys_p18640
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 336);



CREATE TABLE pspadm.t_pceep_old_sys_p18641
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 337);



CREATE TABLE pspadm.t_pceep_old_sys_p18642
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 338);



CREATE TABLE pspadm.t_pceep_old_sys_p18643
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 339);



CREATE TABLE pspadm.t_pceep_old_sys_p18644
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 340);



CREATE TABLE pspadm.t_pceep_old_sys_p18645
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 341);



CREATE TABLE pspadm.t_pceep_old_sys_p18646
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 342);



CREATE TABLE pspadm.t_pceep_old_sys_p18647
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 343);



CREATE TABLE pspadm.t_pceep_old_sys_p18648
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 344);



CREATE TABLE pspadm.t_pceep_old_sys_p18649
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 345);



CREATE TABLE pspadm.t_pceep_old_sys_p18650
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 346);



CREATE TABLE pspadm.t_pceep_old_sys_p18651
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 347);



CREATE TABLE pspadm.t_pceep_old_sys_p18652
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 348);



CREATE TABLE pspadm.t_pceep_old_sys_p18653
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 349);



CREATE TABLE pspadm.t_pceep_old_sys_p18654
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 350);



CREATE TABLE pspadm.t_pceep_old_sys_p18655
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 351);



CREATE TABLE pspadm.t_pceep_old_sys_p18656
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 352);



CREATE TABLE pspadm.t_pceep_old_sys_p18657
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 353);



CREATE TABLE pspadm.t_pceep_old_sys_p18658
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 354);



CREATE TABLE pspadm.t_pceep_old_sys_p18659
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 355);



CREATE TABLE pspadm.t_pceep_old_sys_p18660
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 356);



CREATE TABLE pspadm.t_pceep_old_sys_p18661
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 357);



CREATE TABLE pspadm.t_pceep_old_sys_p18662
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 358);



CREATE TABLE pspadm.t_pceep_old_sys_p18663
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 359);



CREATE TABLE pspadm.t_pceep_old_sys_p18664
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 360);



CREATE TABLE pspadm.t_pceep_old_sys_p18665
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 361);



CREATE TABLE pspadm.t_pceep_old_sys_p18666
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 362);



CREATE TABLE pspadm.t_pceep_old_sys_p18667
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 363);



CREATE TABLE pspadm.t_pceep_old_sys_p18668
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 364);



CREATE TABLE pspadm.t_pceep_old_sys_p18669
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 365);



CREATE TABLE pspadm.t_pceep_old_sys_p18670
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 366);



CREATE TABLE pspadm.t_pceep_old_sys_p18671
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 367);



CREATE TABLE pspadm.t_pceep_old_sys_p18672
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 368);



CREATE TABLE pspadm.t_pceep_old_sys_p18673
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 369);



CREATE TABLE pspadm.t_pceep_old_sys_p18674
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 370);



CREATE TABLE pspadm.t_pceep_old_sys_p18675
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 371);



CREATE TABLE pspadm.t_pceep_old_sys_p18676
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 372);



CREATE TABLE pspadm.t_pceep_old_sys_p18677
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 373);



CREATE TABLE pspadm.t_pceep_old_sys_p18678
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 374);



CREATE TABLE pspadm.t_pceep_old_sys_p18679
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 375);



CREATE TABLE pspadm.t_pceep_old_sys_p18680
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 376);



CREATE TABLE pspadm.t_pceep_old_sys_p18681
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 377);



CREATE TABLE pspadm.t_pceep_old_sys_p18682
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 378);



CREATE TABLE pspadm.t_pceep_old_sys_p18683
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 379);



CREATE TABLE pspadm.t_pceep_old_sys_p18684
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 380);



CREATE TABLE pspadm.t_pceep_old_sys_p18685
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 381);



CREATE TABLE pspadm.t_pceep_old_sys_p18686
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 382);



CREATE TABLE pspadm.t_pceep_old_sys_p18687
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 383);



CREATE TABLE pspadm.t_pceep_old_sys_p18688
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 384);



CREATE TABLE pspadm.t_pceep_old_sys_p18689
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 385);



CREATE TABLE pspadm.t_pceep_old_sys_p18690
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 386);



CREATE TABLE pspadm.t_pceep_old_sys_p18691
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 387);



CREATE TABLE pspadm.t_pceep_old_sys_p18692
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 388);



CREATE TABLE pspadm.t_pceep_old_sys_p18693
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 389);



CREATE TABLE pspadm.t_pceep_old_sys_p18694
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 390);



CREATE TABLE pspadm.t_pceep_old_sys_p18695
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 391);



CREATE TABLE pspadm.t_pceep_old_sys_p18696
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 392);



CREATE TABLE pspadm.t_pceep_old_sys_p18697
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 393);



CREATE TABLE pspadm.t_pceep_old_sys_p18698
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 394);



CREATE TABLE pspadm.t_pceep_old_sys_p18699
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 395);



CREATE TABLE pspadm.t_pceep_old_sys_p18700
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 396);



CREATE TABLE pspadm.t_pceep_old_sys_p18701
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 397);



CREATE TABLE pspadm.t_pceep_old_sys_p18702
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 398);



CREATE TABLE pspadm.t_pceep_old_sys_p18703
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 399);



CREATE TABLE pspadm.t_pceep_old_sys_p18704
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 400);



CREATE TABLE pspadm.t_pceep_old_sys_p18705
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 401);



CREATE TABLE pspadm.t_pceep_old_sys_p18706
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 402);



CREATE TABLE pspadm.t_pceep_old_sys_p18707
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 403);



CREATE TABLE pspadm.t_pceep_old_sys_p18708
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 404);



CREATE TABLE pspadm.t_pceep_old_sys_p18709
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 405);



CREATE TABLE pspadm.t_pceep_old_sys_p18710
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 406);



CREATE TABLE pspadm.t_pceep_old_sys_p18711
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 407);



CREATE TABLE pspadm.t_pceep_old_sys_p18712
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 408);



CREATE TABLE pspadm.t_pceep_old_sys_p18713
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 409);



CREATE TABLE pspadm.t_pceep_old_sys_p18714
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 410);



CREATE TABLE pspadm.t_pceep_old_sys_p18715
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 411);



CREATE TABLE pspadm.t_pceep_old_sys_p18716
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 412);



CREATE TABLE pspadm.t_pceep_old_sys_p18717
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 413);



CREATE TABLE pspadm.t_pceep_old_sys_p18718
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 414);



CREATE TABLE pspadm.t_pceep_old_sys_p18719
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 415);



CREATE TABLE pspadm.t_pceep_old_sys_p18720
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 416);



CREATE TABLE pspadm.t_pceep_old_sys_p18721
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 417);



CREATE TABLE pspadm.t_pceep_old_sys_p18722
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 418);



CREATE TABLE pspadm.t_pceep_old_sys_p18723
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 419);



CREATE TABLE pspadm.t_pceep_old_sys_p18724
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 420);



CREATE TABLE pspadm.t_pceep_old_sys_p18725
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 421);



CREATE TABLE pspadm.t_pceep_old_sys_p18726
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 422);



CREATE TABLE pspadm.t_pceep_old_sys_p18727
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 423);



CREATE TABLE pspadm.t_pceep_old_sys_p18728
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 424);



CREATE TABLE pspadm.t_pceep_old_sys_p18729
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 425);



CREATE TABLE pspadm.t_pceep_old_sys_p18730
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 426);



CREATE TABLE pspadm.t_pceep_old_sys_p18731
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 427);



CREATE TABLE pspadm.t_pceep_old_sys_p18732
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 428);



CREATE TABLE pspadm.t_pceep_old_sys_p18733
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 429);



CREATE TABLE pspadm.t_pceep_old_sys_p18734
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 430);



CREATE TABLE pspadm.t_pceep_old_sys_p18735
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 431);



CREATE TABLE pspadm.t_pceep_old_sys_p18736
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 432);



CREATE TABLE pspadm.t_pceep_old_sys_p18737
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 433);



CREATE TABLE pspadm.t_pceep_old_sys_p18738
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 434);



CREATE TABLE pspadm.t_pceep_old_sys_p18739
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 435);



CREATE TABLE pspadm.t_pceep_old_sys_p18740
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 436);



CREATE TABLE pspadm.t_pceep_old_sys_p18741
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 437);



CREATE TABLE pspadm.t_pceep_old_sys_p18742
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 438);



CREATE TABLE pspadm.t_pceep_old_sys_p18743
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 439);



CREATE TABLE pspadm.t_pceep_old_sys_p18744
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 440);



CREATE TABLE pspadm.t_pceep_old_sys_p18745
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 441);



CREATE TABLE pspadm.t_pceep_old_sys_p18746
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 442);



CREATE TABLE pspadm.t_pceep_old_sys_p18747
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 443);



CREATE TABLE pspadm.t_pceep_old_sys_p18748
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 444);



CREATE TABLE pspadm.t_pceep_old_sys_p18749
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 445);



CREATE TABLE pspadm.t_pceep_old_sys_p18750
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 446);



CREATE TABLE pspadm.t_pceep_old_sys_p18751
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 447);



CREATE TABLE pspadm.t_pceep_old_sys_p18752
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 448);



CREATE TABLE pspadm.t_pceep_old_sys_p18753
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 449);



CREATE TABLE pspadm.t_pceep_old_sys_p18754
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 450);



CREATE TABLE pspadm.t_pceep_old_sys_p18755
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 451);



CREATE TABLE pspadm.t_pceep_old_sys_p18756
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 452);



CREATE TABLE pspadm.t_pceep_old_sys_p18757
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 453);



CREATE TABLE pspadm.t_pceep_old_sys_p18758
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 454);



CREATE TABLE pspadm.t_pceep_old_sys_p18759
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 455);



CREATE TABLE pspadm.t_pceep_old_sys_p18760
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 456);



CREATE TABLE pspadm.t_pceep_old_sys_p18761
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 457);



CREATE TABLE pspadm.t_pceep_old_sys_p18762
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 458);



CREATE TABLE pspadm.t_pceep_old_sys_p18763
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 459);



CREATE TABLE pspadm.t_pceep_old_sys_p18764
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 460);



CREATE TABLE pspadm.t_pceep_old_sys_p18765
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 461);



CREATE TABLE pspadm.t_pceep_old_sys_p18766
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 462);



CREATE TABLE pspadm.t_pceep_old_sys_p18767
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 463);



CREATE TABLE pspadm.t_pceep_old_sys_p18768
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 464);



CREATE TABLE pspadm.t_pceep_old_sys_p18769
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 465);



CREATE TABLE pspadm.t_pceep_old_sys_p18770
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 466);



CREATE TABLE pspadm.t_pceep_old_sys_p18771
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 467);



CREATE TABLE pspadm.t_pceep_old_sys_p18772
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 468);



CREATE TABLE pspadm.t_pceep_old_sys_p18773
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 469);



CREATE TABLE pspadm.t_pceep_old_sys_p18774
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 470);



CREATE TABLE pspadm.t_pceep_old_sys_p18775
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 471);



CREATE TABLE pspadm.t_pceep_old_sys_p18776
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 472);



CREATE TABLE pspadm.t_pceep_old_sys_p18777
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 473);



CREATE TABLE pspadm.t_pceep_old_sys_p18778
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 474);



CREATE TABLE pspadm.t_pceep_old_sys_p18779
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 475);



CREATE TABLE pspadm.t_pceep_old_sys_p18780
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 476);



CREATE TABLE pspadm.t_pceep_old_sys_p18781
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 477);



CREATE TABLE pspadm.t_pceep_old_sys_p18782
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 478);



CREATE TABLE pspadm.t_pceep_old_sys_p18783
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 479);



CREATE TABLE pspadm.t_pceep_old_sys_p18784
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 480);



CREATE TABLE pspadm.t_pceep_old_sys_p18785
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 481);



CREATE TABLE pspadm.t_pceep_old_sys_p18786
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 482);



CREATE TABLE pspadm.t_pceep_old_sys_p18787
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 483);



CREATE TABLE pspadm.t_pceep_old_sys_p18788
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 484);



CREATE TABLE pspadm.t_pceep_old_sys_p18789
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 485);



CREATE TABLE pspadm.t_pceep_old_sys_p18790
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 486);



CREATE TABLE pspadm.t_pceep_old_sys_p18791
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 487);



CREATE TABLE pspadm.t_pceep_old_sys_p18792
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 488);



CREATE TABLE pspadm.t_pceep_old_sys_p18793
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 489);



CREATE TABLE pspadm.t_pceep_old_sys_p18794
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 490);



CREATE TABLE pspadm.t_pceep_old_sys_p18795
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 491);



CREATE TABLE pspadm.t_pceep_old_sys_p18796
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 492);



CREATE TABLE pspadm.t_pceep_old_sys_p18797
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 493);



CREATE TABLE pspadm.t_pceep_old_sys_p18798
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 494);



CREATE TABLE pspadm.t_pceep_old_sys_p18799
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 495);



CREATE TABLE pspadm.t_pceep_old_sys_p18800
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 496);



CREATE TABLE pspadm.t_pceep_old_sys_p18801
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 497);



CREATE TABLE pspadm.t_pceep_old_sys_p18802
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 498);



CREATE TABLE pspadm.t_pceep_old_sys_p18803
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 499);



CREATE TABLE pspadm.t_pceep_old_sys_p18804
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 500);



CREATE TABLE pspadm.t_pceep_old_sys_p18805
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 501);



CREATE TABLE pspadm.t_pceep_old_sys_p18806
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 502);



CREATE TABLE pspadm.t_pceep_old_sys_p18807
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 503);



CREATE TABLE pspadm.t_pceep_old_sys_p18808
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 504);



CREATE TABLE pspadm.t_pceep_old_sys_p18809
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 505);



CREATE TABLE pspadm.t_pceep_old_sys_p18810
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 506);



CREATE TABLE pspadm.t_pceep_old_sys_p18811
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 507);



CREATE TABLE pspadm.t_pceep_old_sys_p18812
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 508);



CREATE TABLE pspadm.t_pceep_old_sys_p18813
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 509);



CREATE TABLE pspadm.t_pceep_old_sys_p18814
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 510);



CREATE TABLE pspadm.t_pceep_old_sys_p18815
        PARTITION OF pspadm.t_pceep_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 511);



CREATE TABLE pspadm.t_pdatl_old_default
        PARTITION OF pspadm.t_pdatl_old
        DEFAULT;



CREATE TABLE pspadm.t_pdatl_old_sys_p19328
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 0);



CREATE TABLE pspadm.t_pdatl_old_sys_p19329
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 1);



CREATE TABLE pspadm.t_pdatl_old_sys_p19330
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 2);



CREATE TABLE pspadm.t_pdatl_old_sys_p19331
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 3);



CREATE TABLE pspadm.t_pdatl_old_sys_p19332
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 4);



CREATE TABLE pspadm.t_pdatl_old_sys_p19333
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 5);



CREATE TABLE pspadm.t_pdatl_old_sys_p19334
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 6);



CREATE TABLE pspadm.t_pdatl_old_sys_p19335
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 7);



CREATE TABLE pspadm.t_pdatl_old_sys_p19336
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 8);



CREATE TABLE pspadm.t_pdatl_old_sys_p19337
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 9);



CREATE TABLE pspadm.t_pdatl_old_sys_p19338
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 10);



CREATE TABLE pspadm.t_pdatl_old_sys_p19339
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 11);



CREATE TABLE pspadm.t_pdatl_old_sys_p19340
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 12);



CREATE TABLE pspadm.t_pdatl_old_sys_p19341
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 13);



CREATE TABLE pspadm.t_pdatl_old_sys_p19342
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 14);



CREATE TABLE pspadm.t_pdatl_old_sys_p19343
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 15);



CREATE TABLE pspadm.t_pdatl_old_sys_p19344
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 16);



CREATE TABLE pspadm.t_pdatl_old_sys_p19345
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 17);



CREATE TABLE pspadm.t_pdatl_old_sys_p19346
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 18);



CREATE TABLE pspadm.t_pdatl_old_sys_p19347
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 19);



CREATE TABLE pspadm.t_pdatl_old_sys_p19348
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 20);



CREATE TABLE pspadm.t_pdatl_old_sys_p19349
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 21);



CREATE TABLE pspadm.t_pdatl_old_sys_p19350
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 22);



CREATE TABLE pspadm.t_pdatl_old_sys_p19351
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 23);



CREATE TABLE pspadm.t_pdatl_old_sys_p19352
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 24);



CREATE TABLE pspadm.t_pdatl_old_sys_p19353
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 25);



CREATE TABLE pspadm.t_pdatl_old_sys_p19354
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 26);



CREATE TABLE pspadm.t_pdatl_old_sys_p19355
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 27);



CREATE TABLE pspadm.t_pdatl_old_sys_p19356
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 28);



CREATE TABLE pspadm.t_pdatl_old_sys_p19357
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 29);



CREATE TABLE pspadm.t_pdatl_old_sys_p19358
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 30);



CREATE TABLE pspadm.t_pdatl_old_sys_p19359
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 31);



CREATE TABLE pspadm.t_pdatl_old_sys_p19360
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 32);



CREATE TABLE pspadm.t_pdatl_old_sys_p19361
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 33);



CREATE TABLE pspadm.t_pdatl_old_sys_p19362
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 34);



CREATE TABLE pspadm.t_pdatl_old_sys_p19363
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 35);



CREATE TABLE pspadm.t_pdatl_old_sys_p19364
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 36);



CREATE TABLE pspadm.t_pdatl_old_sys_p19365
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 37);



CREATE TABLE pspadm.t_pdatl_old_sys_p19366
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 38);



CREATE TABLE pspadm.t_pdatl_old_sys_p19367
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 39);



CREATE TABLE pspadm.t_pdatl_old_sys_p19368
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 40);



CREATE TABLE pspadm.t_pdatl_old_sys_p19369
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 41);



CREATE TABLE pspadm.t_pdatl_old_sys_p19370
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 42);



CREATE TABLE pspadm.t_pdatl_old_sys_p19371
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 43);



CREATE TABLE pspadm.t_pdatl_old_sys_p19372
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 44);



CREATE TABLE pspadm.t_pdatl_old_sys_p19373
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 45);



CREATE TABLE pspadm.t_pdatl_old_sys_p19374
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 46);



CREATE TABLE pspadm.t_pdatl_old_sys_p19375
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 47);



CREATE TABLE pspadm.t_pdatl_old_sys_p19376
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 48);



CREATE TABLE pspadm.t_pdatl_old_sys_p19377
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 49);



CREATE TABLE pspadm.t_pdatl_old_sys_p19378
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 50);



CREATE TABLE pspadm.t_pdatl_old_sys_p19379
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 51);



CREATE TABLE pspadm.t_pdatl_old_sys_p19380
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 52);



CREATE TABLE pspadm.t_pdatl_old_sys_p19381
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 53);



CREATE TABLE pspadm.t_pdatl_old_sys_p19382
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 54);



CREATE TABLE pspadm.t_pdatl_old_sys_p19383
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 55);



CREATE TABLE pspadm.t_pdatl_old_sys_p19384
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 56);



CREATE TABLE pspadm.t_pdatl_old_sys_p19385
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 57);



CREATE TABLE pspadm.t_pdatl_old_sys_p19386
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 58);



CREATE TABLE pspadm.t_pdatl_old_sys_p19387
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 59);



CREATE TABLE pspadm.t_pdatl_old_sys_p19388
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 60);



CREATE TABLE pspadm.t_pdatl_old_sys_p19389
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 61);



CREATE TABLE pspadm.t_pdatl_old_sys_p19390
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 62);



CREATE TABLE pspadm.t_pdatl_old_sys_p19391
        PARTITION OF pspadm.t_pdatl_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 63);



CREATE TABLE pspadm.t_pppi_old_default
        PARTITION OF pspadm.t_pppi_old
        DEFAULT;



CREATE TABLE pspadm.t_pppi_old_sys_p18816
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 0);



CREATE TABLE pspadm.t_pppi_old_sys_p18817
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 1);



CREATE TABLE pspadm.t_pppi_old_sys_p18818
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 2);



CREATE TABLE pspadm.t_pppi_old_sys_p18819
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 3);



CREATE TABLE pspadm.t_pppi_old_sys_p18820
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 4);



CREATE TABLE pspadm.t_pppi_old_sys_p18821
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 5);



CREATE TABLE pspadm.t_pppi_old_sys_p18822
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 6);



CREATE TABLE pspadm.t_pppi_old_sys_p18823
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 7);



CREATE TABLE pspadm.t_pppi_old_sys_p18824
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 8);



CREATE TABLE pspadm.t_pppi_old_sys_p18825
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 9);



CREATE TABLE pspadm.t_pppi_old_sys_p18826
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 10);



CREATE TABLE pspadm.t_pppi_old_sys_p18827
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 11);



CREATE TABLE pspadm.t_pppi_old_sys_p18828
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 12);



CREATE TABLE pspadm.t_pppi_old_sys_p18829
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 13);



CREATE TABLE pspadm.t_pppi_old_sys_p18830
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 14);



CREATE TABLE pspadm.t_pppi_old_sys_p18831
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 15);



CREATE TABLE pspadm.t_pppi_old_sys_p18832
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 16);



CREATE TABLE pspadm.t_pppi_old_sys_p18833
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 17);



CREATE TABLE pspadm.t_pppi_old_sys_p18834
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 18);



CREATE TABLE pspadm.t_pppi_old_sys_p18835
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 19);



CREATE TABLE pspadm.t_pppi_old_sys_p18836
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 20);



CREATE TABLE pspadm.t_pppi_old_sys_p18837
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 21);



CREATE TABLE pspadm.t_pppi_old_sys_p18838
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 22);



CREATE TABLE pspadm.t_pppi_old_sys_p18839
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 23);



CREATE TABLE pspadm.t_pppi_old_sys_p18840
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 24);



CREATE TABLE pspadm.t_pppi_old_sys_p18841
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 25);



CREATE TABLE pspadm.t_pppi_old_sys_p18842
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 26);



CREATE TABLE pspadm.t_pppi_old_sys_p18843
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 27);



CREATE TABLE pspadm.t_pppi_old_sys_p18844
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 28);



CREATE TABLE pspadm.t_pppi_old_sys_p18845
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 29);



CREATE TABLE pspadm.t_pppi_old_sys_p18846
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 30);



CREATE TABLE pspadm.t_pppi_old_sys_p18847
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 31);



CREATE TABLE pspadm.t_pppi_old_sys_p18848
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 32);



CREATE TABLE pspadm.t_pppi_old_sys_p18849
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 33);



CREATE TABLE pspadm.t_pppi_old_sys_p18850
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 34);



CREATE TABLE pspadm.t_pppi_old_sys_p18851
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 35);



CREATE TABLE pspadm.t_pppi_old_sys_p18852
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 36);



CREATE TABLE pspadm.t_pppi_old_sys_p18853
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 37);



CREATE TABLE pspadm.t_pppi_old_sys_p18854
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 38);



CREATE TABLE pspadm.t_pppi_old_sys_p18855
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 39);



CREATE TABLE pspadm.t_pppi_old_sys_p18856
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 40);



CREATE TABLE pspadm.t_pppi_old_sys_p18857
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 41);



CREATE TABLE pspadm.t_pppi_old_sys_p18858
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 42);



CREATE TABLE pspadm.t_pppi_old_sys_p18859
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 43);



CREATE TABLE pspadm.t_pppi_old_sys_p18860
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 44);



CREATE TABLE pspadm.t_pppi_old_sys_p18861
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 45);



CREATE TABLE pspadm.t_pppi_old_sys_p18862
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 46);



CREATE TABLE pspadm.t_pppi_old_sys_p18863
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 47);



CREATE TABLE pspadm.t_pppi_old_sys_p18864
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 48);



CREATE TABLE pspadm.t_pppi_old_sys_p18865
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 49);



CREATE TABLE pspadm.t_pppi_old_sys_p18866
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 50);



CREATE TABLE pspadm.t_pppi_old_sys_p18867
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 51);



CREATE TABLE pspadm.t_pppi_old_sys_p18868
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 52);



CREATE TABLE pspadm.t_pppi_old_sys_p18869
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 53);



CREATE TABLE pspadm.t_pppi_old_sys_p18870
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 54);



CREATE TABLE pspadm.t_pppi_old_sys_p18871
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 55);



CREATE TABLE pspadm.t_pppi_old_sys_p18872
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 56);



CREATE TABLE pspadm.t_pppi_old_sys_p18873
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 57);



CREATE TABLE pspadm.t_pppi_old_sys_p18874
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 58);



CREATE TABLE pspadm.t_pppi_old_sys_p18875
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 59);



CREATE TABLE pspadm.t_pppi_old_sys_p18876
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 60);



CREATE TABLE pspadm.t_pppi_old_sys_p18877
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 61);



CREATE TABLE pspadm.t_pppi_old_sys_p18878
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 62);



CREATE TABLE pspadm.t_pppi_old_sys_p18879
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 63);



CREATE TABLE pspadm.t_pppi_old_sys_p18880
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 64);



CREATE TABLE pspadm.t_pppi_old_sys_p18881
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 65);



CREATE TABLE pspadm.t_pppi_old_sys_p18882
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 66);



CREATE TABLE pspadm.t_pppi_old_sys_p18883
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 67);



CREATE TABLE pspadm.t_pppi_old_sys_p18884
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 68);



CREATE TABLE pspadm.t_pppi_old_sys_p18885
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 69);



CREATE TABLE pspadm.t_pppi_old_sys_p18886
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 70);



CREATE TABLE pspadm.t_pppi_old_sys_p18887
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 71);



CREATE TABLE pspadm.t_pppi_old_sys_p18888
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 72);



CREATE TABLE pspadm.t_pppi_old_sys_p18889
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 73);



CREATE TABLE pspadm.t_pppi_old_sys_p18890
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 74);



CREATE TABLE pspadm.t_pppi_old_sys_p18891
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 75);



CREATE TABLE pspadm.t_pppi_old_sys_p18892
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 76);



CREATE TABLE pspadm.t_pppi_old_sys_p18893
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 77);



CREATE TABLE pspadm.t_pppi_old_sys_p18894
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 78);



CREATE TABLE pspadm.t_pppi_old_sys_p18895
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 79);



CREATE TABLE pspadm.t_pppi_old_sys_p18896
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 80);



CREATE TABLE pspadm.t_pppi_old_sys_p18897
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 81);



CREATE TABLE pspadm.t_pppi_old_sys_p18898
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 82);



CREATE TABLE pspadm.t_pppi_old_sys_p18899
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 83);



CREATE TABLE pspadm.t_pppi_old_sys_p18900
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 84);



CREATE TABLE pspadm.t_pppi_old_sys_p18901
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 85);



CREATE TABLE pspadm.t_pppi_old_sys_p18902
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 86);



CREATE TABLE pspadm.t_pppi_old_sys_p18903
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 87);



CREATE TABLE pspadm.t_pppi_old_sys_p18904
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 88);



CREATE TABLE pspadm.t_pppi_old_sys_p18905
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 89);



CREATE TABLE pspadm.t_pppi_old_sys_p18906
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 90);



CREATE TABLE pspadm.t_pppi_old_sys_p18907
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 91);



CREATE TABLE pspadm.t_pppi_old_sys_p18908
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 92);



CREATE TABLE pspadm.t_pppi_old_sys_p18909
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 93);



CREATE TABLE pspadm.t_pppi_old_sys_p18910
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 94);



CREATE TABLE pspadm.t_pppi_old_sys_p18911
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 95);



CREATE TABLE pspadm.t_pppi_old_sys_p18912
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 96);



CREATE TABLE pspadm.t_pppi_old_sys_p18913
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 97);



CREATE TABLE pspadm.t_pppi_old_sys_p18914
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 98);



CREATE TABLE pspadm.t_pppi_old_sys_p18915
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 99);



CREATE TABLE pspadm.t_pppi_old_sys_p18916
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 100);



CREATE TABLE pspadm.t_pppi_old_sys_p18917
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 101);



CREATE TABLE pspadm.t_pppi_old_sys_p18918
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 102);



CREATE TABLE pspadm.t_pppi_old_sys_p18919
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 103);



CREATE TABLE pspadm.t_pppi_old_sys_p18920
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 104);



CREATE TABLE pspadm.t_pppi_old_sys_p18921
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 105);



CREATE TABLE pspadm.t_pppi_old_sys_p18922
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 106);



CREATE TABLE pspadm.t_pppi_old_sys_p18923
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 107);



CREATE TABLE pspadm.t_pppi_old_sys_p18924
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 108);



CREATE TABLE pspadm.t_pppi_old_sys_p18925
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 109);



CREATE TABLE pspadm.t_pppi_old_sys_p18926
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 110);



CREATE TABLE pspadm.t_pppi_old_sys_p18927
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 111);



CREATE TABLE pspadm.t_pppi_old_sys_p18928
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 112);



CREATE TABLE pspadm.t_pppi_old_sys_p18929
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 113);



CREATE TABLE pspadm.t_pppi_old_sys_p18930
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 114);



CREATE TABLE pspadm.t_pppi_old_sys_p18931
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 115);



CREATE TABLE pspadm.t_pppi_old_sys_p18932
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 116);



CREATE TABLE pspadm.t_pppi_old_sys_p18933
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 117);



CREATE TABLE pspadm.t_pppi_old_sys_p18934
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 118);



CREATE TABLE pspadm.t_pppi_old_sys_p18935
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 119);



CREATE TABLE pspadm.t_pppi_old_sys_p18936
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 120);



CREATE TABLE pspadm.t_pppi_old_sys_p18937
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 121);



CREATE TABLE pspadm.t_pppi_old_sys_p18938
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 122);



CREATE TABLE pspadm.t_pppi_old_sys_p18939
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 123);



CREATE TABLE pspadm.t_pppi_old_sys_p18940
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 124);



CREATE TABLE pspadm.t_pppi_old_sys_p18941
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 125);



CREATE TABLE pspadm.t_pppi_old_sys_p18942
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 126);



CREATE TABLE pspadm.t_pppi_old_sys_p18943
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 127);



CREATE TABLE pspadm.t_pppi_old_sys_p18944
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 128);



CREATE TABLE pspadm.t_pppi_old_sys_p18945
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 129);



CREATE TABLE pspadm.t_pppi_old_sys_p18946
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 130);



CREATE TABLE pspadm.t_pppi_old_sys_p18947
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 131);



CREATE TABLE pspadm.t_pppi_old_sys_p18948
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 132);



CREATE TABLE pspadm.t_pppi_old_sys_p18949
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 133);



CREATE TABLE pspadm.t_pppi_old_sys_p18950
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 134);



CREATE TABLE pspadm.t_pppi_old_sys_p18951
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 135);



CREATE TABLE pspadm.t_pppi_old_sys_p18952
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 136);



CREATE TABLE pspadm.t_pppi_old_sys_p18953
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 137);



CREATE TABLE pspadm.t_pppi_old_sys_p18954
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 138);



CREATE TABLE pspadm.t_pppi_old_sys_p18955
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 139);



CREATE TABLE pspadm.t_pppi_old_sys_p18956
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 140);



CREATE TABLE pspadm.t_pppi_old_sys_p18957
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 141);



CREATE TABLE pspadm.t_pppi_old_sys_p18958
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 142);



CREATE TABLE pspadm.t_pppi_old_sys_p18959
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 143);



CREATE TABLE pspadm.t_pppi_old_sys_p18960
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 144);



CREATE TABLE pspadm.t_pppi_old_sys_p18961
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 145);



CREATE TABLE pspadm.t_pppi_old_sys_p18962
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 146);



CREATE TABLE pspadm.t_pppi_old_sys_p18963
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 147);



CREATE TABLE pspadm.t_pppi_old_sys_p18964
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 148);



CREATE TABLE pspadm.t_pppi_old_sys_p18965
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 149);



CREATE TABLE pspadm.t_pppi_old_sys_p18966
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 150);



CREATE TABLE pspadm.t_pppi_old_sys_p18967
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 151);



CREATE TABLE pspadm.t_pppi_old_sys_p18968
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 152);



CREATE TABLE pspadm.t_pppi_old_sys_p18969
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 153);



CREATE TABLE pspadm.t_pppi_old_sys_p18970
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 154);



CREATE TABLE pspadm.t_pppi_old_sys_p18971
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 155);



CREATE TABLE pspadm.t_pppi_old_sys_p18972
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 156);



CREATE TABLE pspadm.t_pppi_old_sys_p18973
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 157);



CREATE TABLE pspadm.t_pppi_old_sys_p18974
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 158);



CREATE TABLE pspadm.t_pppi_old_sys_p18975
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 159);



CREATE TABLE pspadm.t_pppi_old_sys_p18976
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 160);



CREATE TABLE pspadm.t_pppi_old_sys_p18977
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 161);



CREATE TABLE pspadm.t_pppi_old_sys_p18978
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 162);



CREATE TABLE pspadm.t_pppi_old_sys_p18979
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 163);



CREATE TABLE pspadm.t_pppi_old_sys_p18980
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 164);



CREATE TABLE pspadm.t_pppi_old_sys_p18981
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 165);



CREATE TABLE pspadm.t_pppi_old_sys_p18982
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 166);



CREATE TABLE pspadm.t_pppi_old_sys_p18983
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 167);



CREATE TABLE pspadm.t_pppi_old_sys_p18984
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 168);



CREATE TABLE pspadm.t_pppi_old_sys_p18985
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 169);



CREATE TABLE pspadm.t_pppi_old_sys_p18986
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 170);



CREATE TABLE pspadm.t_pppi_old_sys_p18987
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 171);



CREATE TABLE pspadm.t_pppi_old_sys_p18988
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 172);



CREATE TABLE pspadm.t_pppi_old_sys_p18989
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 173);



CREATE TABLE pspadm.t_pppi_old_sys_p18990
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 174);



CREATE TABLE pspadm.t_pppi_old_sys_p18991
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 175);



CREATE TABLE pspadm.t_pppi_old_sys_p18992
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 176);



CREATE TABLE pspadm.t_pppi_old_sys_p18993
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 177);



CREATE TABLE pspadm.t_pppi_old_sys_p18994
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 178);



CREATE TABLE pspadm.t_pppi_old_sys_p18995
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 179);



CREATE TABLE pspadm.t_pppi_old_sys_p18996
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 180);



CREATE TABLE pspadm.t_pppi_old_sys_p18997
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 181);



CREATE TABLE pspadm.t_pppi_old_sys_p18998
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 182);



CREATE TABLE pspadm.t_pppi_old_sys_p18999
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 183);



CREATE TABLE pspadm.t_pppi_old_sys_p19000
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 184);



CREATE TABLE pspadm.t_pppi_old_sys_p19001
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 185);



CREATE TABLE pspadm.t_pppi_old_sys_p19002
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 186);



CREATE TABLE pspadm.t_pppi_old_sys_p19003
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 187);



CREATE TABLE pspadm.t_pppi_old_sys_p19004
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 188);



CREATE TABLE pspadm.t_pppi_old_sys_p19005
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 189);



CREATE TABLE pspadm.t_pppi_old_sys_p19006
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 190);



CREATE TABLE pspadm.t_pppi_old_sys_p19007
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 191);



CREATE TABLE pspadm.t_pppi_old_sys_p19008
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 192);



CREATE TABLE pspadm.t_pppi_old_sys_p19009
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 193);



CREATE TABLE pspadm.t_pppi_old_sys_p19010
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 194);



CREATE TABLE pspadm.t_pppi_old_sys_p19011
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 195);



CREATE TABLE pspadm.t_pppi_old_sys_p19012
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 196);



CREATE TABLE pspadm.t_pppi_old_sys_p19013
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 197);



CREATE TABLE pspadm.t_pppi_old_sys_p19014
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 198);



CREATE TABLE pspadm.t_pppi_old_sys_p19015
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 199);



CREATE TABLE pspadm.t_pppi_old_sys_p19016
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 200);



CREATE TABLE pspadm.t_pppi_old_sys_p19017
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 201);



CREATE TABLE pspadm.t_pppi_old_sys_p19018
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 202);



CREATE TABLE pspadm.t_pppi_old_sys_p19019
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 203);



CREATE TABLE pspadm.t_pppi_old_sys_p19020
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 204);



CREATE TABLE pspadm.t_pppi_old_sys_p19021
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 205);



CREATE TABLE pspadm.t_pppi_old_sys_p19022
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 206);



CREATE TABLE pspadm.t_pppi_old_sys_p19023
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 207);



CREATE TABLE pspadm.t_pppi_old_sys_p19024
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 208);



CREATE TABLE pspadm.t_pppi_old_sys_p19025
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 209);



CREATE TABLE pspadm.t_pppi_old_sys_p19026
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 210);



CREATE TABLE pspadm.t_pppi_old_sys_p19027
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 211);



CREATE TABLE pspadm.t_pppi_old_sys_p19028
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 212);



CREATE TABLE pspadm.t_pppi_old_sys_p19029
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 213);



CREATE TABLE pspadm.t_pppi_old_sys_p19030
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 214);



CREATE TABLE pspadm.t_pppi_old_sys_p19031
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 215);



CREATE TABLE pspadm.t_pppi_old_sys_p19032
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 216);



CREATE TABLE pspadm.t_pppi_old_sys_p19033
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 217);



CREATE TABLE pspadm.t_pppi_old_sys_p19034
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 218);



CREATE TABLE pspadm.t_pppi_old_sys_p19035
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 219);



CREATE TABLE pspadm.t_pppi_old_sys_p19036
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 220);



CREATE TABLE pspadm.t_pppi_old_sys_p19037
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 221);



CREATE TABLE pspadm.t_pppi_old_sys_p19038
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 222);



CREATE TABLE pspadm.t_pppi_old_sys_p19039
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 223);



CREATE TABLE pspadm.t_pppi_old_sys_p19040
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 224);



CREATE TABLE pspadm.t_pppi_old_sys_p19041
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 225);



CREATE TABLE pspadm.t_pppi_old_sys_p19042
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 226);



CREATE TABLE pspadm.t_pppi_old_sys_p19043
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 227);



CREATE TABLE pspadm.t_pppi_old_sys_p19044
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 228);



CREATE TABLE pspadm.t_pppi_old_sys_p19045
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 229);



CREATE TABLE pspadm.t_pppi_old_sys_p19046
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 230);



CREATE TABLE pspadm.t_pppi_old_sys_p19047
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 231);



CREATE TABLE pspadm.t_pppi_old_sys_p19048
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 232);



CREATE TABLE pspadm.t_pppi_old_sys_p19049
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 233);



CREATE TABLE pspadm.t_pppi_old_sys_p19050
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 234);



CREATE TABLE pspadm.t_pppi_old_sys_p19051
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 235);



CREATE TABLE pspadm.t_pppi_old_sys_p19052
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 236);



CREATE TABLE pspadm.t_pppi_old_sys_p19053
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 237);



CREATE TABLE pspadm.t_pppi_old_sys_p19054
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 238);



CREATE TABLE pspadm.t_pppi_old_sys_p19055
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 239);



CREATE TABLE pspadm.t_pppi_old_sys_p19056
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 240);



CREATE TABLE pspadm.t_pppi_old_sys_p19057
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 241);



CREATE TABLE pspadm.t_pppi_old_sys_p19058
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 242);



CREATE TABLE pspadm.t_pppi_old_sys_p19059
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 243);



CREATE TABLE pspadm.t_pppi_old_sys_p19060
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 244);



CREATE TABLE pspadm.t_pppi_old_sys_p19061
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 245);



CREATE TABLE pspadm.t_pppi_old_sys_p19062
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 246);



CREATE TABLE pspadm.t_pppi_old_sys_p19063
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 247);



CREATE TABLE pspadm.t_pppi_old_sys_p19064
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 248);



CREATE TABLE pspadm.t_pppi_old_sys_p19065
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 249);



CREATE TABLE pspadm.t_pppi_old_sys_p19066
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 250);



CREATE TABLE pspadm.t_pppi_old_sys_p19067
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 251);



CREATE TABLE pspadm.t_pppi_old_sys_p19068
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 252);



CREATE TABLE pspadm.t_pppi_old_sys_p19069
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 253);



CREATE TABLE pspadm.t_pppi_old_sys_p19070
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 254);



CREATE TABLE pspadm.t_pppi_old_sys_p19071
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 255);



CREATE TABLE pspadm.t_pppi_old_sys_p19072
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 256);



CREATE TABLE pspadm.t_pppi_old_sys_p19073
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 257);



CREATE TABLE pspadm.t_pppi_old_sys_p19074
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 258);



CREATE TABLE pspadm.t_pppi_old_sys_p19075
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 259);



CREATE TABLE pspadm.t_pppi_old_sys_p19076
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 260);



CREATE TABLE pspadm.t_pppi_old_sys_p19077
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 261);



CREATE TABLE pspadm.t_pppi_old_sys_p19078
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 262);



CREATE TABLE pspadm.t_pppi_old_sys_p19079
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 263);



CREATE TABLE pspadm.t_pppi_old_sys_p19080
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 264);



CREATE TABLE pspadm.t_pppi_old_sys_p19081
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 265);



CREATE TABLE pspadm.t_pppi_old_sys_p19082
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 266);



CREATE TABLE pspadm.t_pppi_old_sys_p19083
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 267);



CREATE TABLE pspadm.t_pppi_old_sys_p19084
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 268);



CREATE TABLE pspadm.t_pppi_old_sys_p19085
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 269);



CREATE TABLE pspadm.t_pppi_old_sys_p19086
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 270);



CREATE TABLE pspadm.t_pppi_old_sys_p19087
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 271);



CREATE TABLE pspadm.t_pppi_old_sys_p19088
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 272);



CREATE TABLE pspadm.t_pppi_old_sys_p19089
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 273);



CREATE TABLE pspadm.t_pppi_old_sys_p19090
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 274);



CREATE TABLE pspadm.t_pppi_old_sys_p19091
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 275);



CREATE TABLE pspadm.t_pppi_old_sys_p19092
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 276);



CREATE TABLE pspadm.t_pppi_old_sys_p19093
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 277);



CREATE TABLE pspadm.t_pppi_old_sys_p19094
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 278);



CREATE TABLE pspadm.t_pppi_old_sys_p19095
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 279);



CREATE TABLE pspadm.t_pppi_old_sys_p19096
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 280);



CREATE TABLE pspadm.t_pppi_old_sys_p19097
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 281);



CREATE TABLE pspadm.t_pppi_old_sys_p19098
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 282);



CREATE TABLE pspadm.t_pppi_old_sys_p19099
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 283);



CREATE TABLE pspadm.t_pppi_old_sys_p19100
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 284);



CREATE TABLE pspadm.t_pppi_old_sys_p19101
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 285);



CREATE TABLE pspadm.t_pppi_old_sys_p19102
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 286);



CREATE TABLE pspadm.t_pppi_old_sys_p19103
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 287);



CREATE TABLE pspadm.t_pppi_old_sys_p19104
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 288);



CREATE TABLE pspadm.t_pppi_old_sys_p19105
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 289);



CREATE TABLE pspadm.t_pppi_old_sys_p19106
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 290);



CREATE TABLE pspadm.t_pppi_old_sys_p19107
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 291);



CREATE TABLE pspadm.t_pppi_old_sys_p19108
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 292);



CREATE TABLE pspadm.t_pppi_old_sys_p19109
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 293);



CREATE TABLE pspadm.t_pppi_old_sys_p19110
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 294);



CREATE TABLE pspadm.t_pppi_old_sys_p19111
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 295);



CREATE TABLE pspadm.t_pppi_old_sys_p19112
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 296);



CREATE TABLE pspadm.t_pppi_old_sys_p19113
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 297);



CREATE TABLE pspadm.t_pppi_old_sys_p19114
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 298);



CREATE TABLE pspadm.t_pppi_old_sys_p19115
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 299);



CREATE TABLE pspadm.t_pppi_old_sys_p19116
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 300);



CREATE TABLE pspadm.t_pppi_old_sys_p19117
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 301);



CREATE TABLE pspadm.t_pppi_old_sys_p19118
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 302);



CREATE TABLE pspadm.t_pppi_old_sys_p19119
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 303);



CREATE TABLE pspadm.t_pppi_old_sys_p19120
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 304);



CREATE TABLE pspadm.t_pppi_old_sys_p19121
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 305);



CREATE TABLE pspadm.t_pppi_old_sys_p19122
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 306);



CREATE TABLE pspadm.t_pppi_old_sys_p19123
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 307);



CREATE TABLE pspadm.t_pppi_old_sys_p19124
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 308);



CREATE TABLE pspadm.t_pppi_old_sys_p19125
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 309);



CREATE TABLE pspadm.t_pppi_old_sys_p19126
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 310);



CREATE TABLE pspadm.t_pppi_old_sys_p19127
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 311);



CREATE TABLE pspadm.t_pppi_old_sys_p19128
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 312);



CREATE TABLE pspadm.t_pppi_old_sys_p19129
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 313);



CREATE TABLE pspadm.t_pppi_old_sys_p19130
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 314);



CREATE TABLE pspadm.t_pppi_old_sys_p19131
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 315);



CREATE TABLE pspadm.t_pppi_old_sys_p19132
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 316);



CREATE TABLE pspadm.t_pppi_old_sys_p19133
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 317);



CREATE TABLE pspadm.t_pppi_old_sys_p19134
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 318);



CREATE TABLE pspadm.t_pppi_old_sys_p19135
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 319);



CREATE TABLE pspadm.t_pppi_old_sys_p19136
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 320);



CREATE TABLE pspadm.t_pppi_old_sys_p19137
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 321);



CREATE TABLE pspadm.t_pppi_old_sys_p19138
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 322);



CREATE TABLE pspadm.t_pppi_old_sys_p19139
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 323);



CREATE TABLE pspadm.t_pppi_old_sys_p19140
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 324);



CREATE TABLE pspadm.t_pppi_old_sys_p19141
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 325);



CREATE TABLE pspadm.t_pppi_old_sys_p19142
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 326);



CREATE TABLE pspadm.t_pppi_old_sys_p19143
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 327);



CREATE TABLE pspadm.t_pppi_old_sys_p19144
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 328);



CREATE TABLE pspadm.t_pppi_old_sys_p19145
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 329);



CREATE TABLE pspadm.t_pppi_old_sys_p19146
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 330);



CREATE TABLE pspadm.t_pppi_old_sys_p19147
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 331);



CREATE TABLE pspadm.t_pppi_old_sys_p19148
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 332);



CREATE TABLE pspadm.t_pppi_old_sys_p19149
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 333);



CREATE TABLE pspadm.t_pppi_old_sys_p19150
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 334);



CREATE TABLE pspadm.t_pppi_old_sys_p19151
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 335);



CREATE TABLE pspadm.t_pppi_old_sys_p19152
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 336);



CREATE TABLE pspadm.t_pppi_old_sys_p19153
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 337);



CREATE TABLE pspadm.t_pppi_old_sys_p19154
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 338);



CREATE TABLE pspadm.t_pppi_old_sys_p19155
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 339);



CREATE TABLE pspadm.t_pppi_old_sys_p19156
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 340);



CREATE TABLE pspadm.t_pppi_old_sys_p19157
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 341);



CREATE TABLE pspadm.t_pppi_old_sys_p19158
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 342);



CREATE TABLE pspadm.t_pppi_old_sys_p19159
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 343);



CREATE TABLE pspadm.t_pppi_old_sys_p19160
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 344);



CREATE TABLE pspadm.t_pppi_old_sys_p19161
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 345);



CREATE TABLE pspadm.t_pppi_old_sys_p19162
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 346);



CREATE TABLE pspadm.t_pppi_old_sys_p19163
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 347);



CREATE TABLE pspadm.t_pppi_old_sys_p19164
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 348);



CREATE TABLE pspadm.t_pppi_old_sys_p19165
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 349);



CREATE TABLE pspadm.t_pppi_old_sys_p19166
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 350);



CREATE TABLE pspadm.t_pppi_old_sys_p19167
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 351);



CREATE TABLE pspadm.t_pppi_old_sys_p19168
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 352);



CREATE TABLE pspadm.t_pppi_old_sys_p19169
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 353);



CREATE TABLE pspadm.t_pppi_old_sys_p19170
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 354);



CREATE TABLE pspadm.t_pppi_old_sys_p19171
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 355);



CREATE TABLE pspadm.t_pppi_old_sys_p19172
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 356);



CREATE TABLE pspadm.t_pppi_old_sys_p19173
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 357);



CREATE TABLE pspadm.t_pppi_old_sys_p19174
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 358);



CREATE TABLE pspadm.t_pppi_old_sys_p19175
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 359);



CREATE TABLE pspadm.t_pppi_old_sys_p19176
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 360);



CREATE TABLE pspadm.t_pppi_old_sys_p19177
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 361);



CREATE TABLE pspadm.t_pppi_old_sys_p19178
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 362);



CREATE TABLE pspadm.t_pppi_old_sys_p19179
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 363);



CREATE TABLE pspadm.t_pppi_old_sys_p19180
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 364);



CREATE TABLE pspadm.t_pppi_old_sys_p19181
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 365);



CREATE TABLE pspadm.t_pppi_old_sys_p19182
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 366);



CREATE TABLE pspadm.t_pppi_old_sys_p19183
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 367);



CREATE TABLE pspadm.t_pppi_old_sys_p19184
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 368);



CREATE TABLE pspadm.t_pppi_old_sys_p19185
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 369);



CREATE TABLE pspadm.t_pppi_old_sys_p19186
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 370);



CREATE TABLE pspadm.t_pppi_old_sys_p19187
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 371);



CREATE TABLE pspadm.t_pppi_old_sys_p19188
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 372);



CREATE TABLE pspadm.t_pppi_old_sys_p19189
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 373);



CREATE TABLE pspadm.t_pppi_old_sys_p19190
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 374);



CREATE TABLE pspadm.t_pppi_old_sys_p19191
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 375);



CREATE TABLE pspadm.t_pppi_old_sys_p19192
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 376);



CREATE TABLE pspadm.t_pppi_old_sys_p19193
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 377);



CREATE TABLE pspadm.t_pppi_old_sys_p19194
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 378);



CREATE TABLE pspadm.t_pppi_old_sys_p19195
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 379);



CREATE TABLE pspadm.t_pppi_old_sys_p19196
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 380);



CREATE TABLE pspadm.t_pppi_old_sys_p19197
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 381);



CREATE TABLE pspadm.t_pppi_old_sys_p19198
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 382);



CREATE TABLE pspadm.t_pppi_old_sys_p19199
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 383);



CREATE TABLE pspadm.t_pppi_old_sys_p19200
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 384);



CREATE TABLE pspadm.t_pppi_old_sys_p19201
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 385);



CREATE TABLE pspadm.t_pppi_old_sys_p19202
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 386);



CREATE TABLE pspadm.t_pppi_old_sys_p19203
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 387);



CREATE TABLE pspadm.t_pppi_old_sys_p19204
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 388);



CREATE TABLE pspadm.t_pppi_old_sys_p19205
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 389);



CREATE TABLE pspadm.t_pppi_old_sys_p19206
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 390);



CREATE TABLE pspadm.t_pppi_old_sys_p19207
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 391);



CREATE TABLE pspadm.t_pppi_old_sys_p19208
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 392);



CREATE TABLE pspadm.t_pppi_old_sys_p19209
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 393);



CREATE TABLE pspadm.t_pppi_old_sys_p19210
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 394);



CREATE TABLE pspadm.t_pppi_old_sys_p19211
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 395);



CREATE TABLE pspadm.t_pppi_old_sys_p19212
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 396);



CREATE TABLE pspadm.t_pppi_old_sys_p19213
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 397);



CREATE TABLE pspadm.t_pppi_old_sys_p19214
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 398);



CREATE TABLE pspadm.t_pppi_old_sys_p19215
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 399);



CREATE TABLE pspadm.t_pppi_old_sys_p19216
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 400);



CREATE TABLE pspadm.t_pppi_old_sys_p19217
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 401);



CREATE TABLE pspadm.t_pppi_old_sys_p19218
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 402);



CREATE TABLE pspadm.t_pppi_old_sys_p19219
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 403);



CREATE TABLE pspadm.t_pppi_old_sys_p19220
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 404);



CREATE TABLE pspadm.t_pppi_old_sys_p19221
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 405);



CREATE TABLE pspadm.t_pppi_old_sys_p19222
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 406);



CREATE TABLE pspadm.t_pppi_old_sys_p19223
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 407);



CREATE TABLE pspadm.t_pppi_old_sys_p19224
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 408);



CREATE TABLE pspadm.t_pppi_old_sys_p19225
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 409);



CREATE TABLE pspadm.t_pppi_old_sys_p19226
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 410);



CREATE TABLE pspadm.t_pppi_old_sys_p19227
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 411);



CREATE TABLE pspadm.t_pppi_old_sys_p19228
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 412);



CREATE TABLE pspadm.t_pppi_old_sys_p19229
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 413);



CREATE TABLE pspadm.t_pppi_old_sys_p19230
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 414);



CREATE TABLE pspadm.t_pppi_old_sys_p19231
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 415);



CREATE TABLE pspadm.t_pppi_old_sys_p19232
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 416);



CREATE TABLE pspadm.t_pppi_old_sys_p19233
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 417);



CREATE TABLE pspadm.t_pppi_old_sys_p19234
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 418);



CREATE TABLE pspadm.t_pppi_old_sys_p19235
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 419);



CREATE TABLE pspadm.t_pppi_old_sys_p19236
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 420);



CREATE TABLE pspadm.t_pppi_old_sys_p19237
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 421);



CREATE TABLE pspadm.t_pppi_old_sys_p19238
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 422);



CREATE TABLE pspadm.t_pppi_old_sys_p19239
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 423);



CREATE TABLE pspadm.t_pppi_old_sys_p19240
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 424);



CREATE TABLE pspadm.t_pppi_old_sys_p19241
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 425);



CREATE TABLE pspadm.t_pppi_old_sys_p19242
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 426);



CREATE TABLE pspadm.t_pppi_old_sys_p19243
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 427);



CREATE TABLE pspadm.t_pppi_old_sys_p19244
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 428);



CREATE TABLE pspadm.t_pppi_old_sys_p19245
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 429);



CREATE TABLE pspadm.t_pppi_old_sys_p19246
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 430);



CREATE TABLE pspadm.t_pppi_old_sys_p19247
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 431);



CREATE TABLE pspadm.t_pppi_old_sys_p19248
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 432);



CREATE TABLE pspadm.t_pppi_old_sys_p19249
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 433);



CREATE TABLE pspadm.t_pppi_old_sys_p19250
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 434);



CREATE TABLE pspadm.t_pppi_old_sys_p19251
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 435);



CREATE TABLE pspadm.t_pppi_old_sys_p19252
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 436);



CREATE TABLE pspadm.t_pppi_old_sys_p19253
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 437);



CREATE TABLE pspadm.t_pppi_old_sys_p19254
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 438);



CREATE TABLE pspadm.t_pppi_old_sys_p19255
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 439);



CREATE TABLE pspadm.t_pppi_old_sys_p19256
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 440);



CREATE TABLE pspadm.t_pppi_old_sys_p19257
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 441);



CREATE TABLE pspadm.t_pppi_old_sys_p19258
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 442);



CREATE TABLE pspadm.t_pppi_old_sys_p19259
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 443);



CREATE TABLE pspadm.t_pppi_old_sys_p19260
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 444);



CREATE TABLE pspadm.t_pppi_old_sys_p19261
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 445);



CREATE TABLE pspadm.t_pppi_old_sys_p19262
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 446);



CREATE TABLE pspadm.t_pppi_old_sys_p19263
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 447);



CREATE TABLE pspadm.t_pppi_old_sys_p19264
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 448);



CREATE TABLE pspadm.t_pppi_old_sys_p19265
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 449);



CREATE TABLE pspadm.t_pppi_old_sys_p19266
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 450);



CREATE TABLE pspadm.t_pppi_old_sys_p19267
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 451);



CREATE TABLE pspadm.t_pppi_old_sys_p19268
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 452);



CREATE TABLE pspadm.t_pppi_old_sys_p19269
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 453);



CREATE TABLE pspadm.t_pppi_old_sys_p19270
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 454);



CREATE TABLE pspadm.t_pppi_old_sys_p19271
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 455);



CREATE TABLE pspadm.t_pppi_old_sys_p19272
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 456);



CREATE TABLE pspadm.t_pppi_old_sys_p19273
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 457);



CREATE TABLE pspadm.t_pppi_old_sys_p19274
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 458);



CREATE TABLE pspadm.t_pppi_old_sys_p19275
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 459);



CREATE TABLE pspadm.t_pppi_old_sys_p19276
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 460);



CREATE TABLE pspadm.t_pppi_old_sys_p19277
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 461);



CREATE TABLE pspadm.t_pppi_old_sys_p19278
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 462);



CREATE TABLE pspadm.t_pppi_old_sys_p19279
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 463);



CREATE TABLE pspadm.t_pppi_old_sys_p19280
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 464);



CREATE TABLE pspadm.t_pppi_old_sys_p19281
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 465);



CREATE TABLE pspadm.t_pppi_old_sys_p19282
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 466);



CREATE TABLE pspadm.t_pppi_old_sys_p19283
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 467);



CREATE TABLE pspadm.t_pppi_old_sys_p19284
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 468);



CREATE TABLE pspadm.t_pppi_old_sys_p19285
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 469);



CREATE TABLE pspadm.t_pppi_old_sys_p19286
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 470);



CREATE TABLE pspadm.t_pppi_old_sys_p19287
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 471);



CREATE TABLE pspadm.t_pppi_old_sys_p19288
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 472);



CREATE TABLE pspadm.t_pppi_old_sys_p19289
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 473);



CREATE TABLE pspadm.t_pppi_old_sys_p19290
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 474);



CREATE TABLE pspadm.t_pppi_old_sys_p19291
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 475);



CREATE TABLE pspadm.t_pppi_old_sys_p19292
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 476);



CREATE TABLE pspadm.t_pppi_old_sys_p19293
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 477);



CREATE TABLE pspadm.t_pppi_old_sys_p19294
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 478);



CREATE TABLE pspadm.t_pppi_old_sys_p19295
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 479);



CREATE TABLE pspadm.t_pppi_old_sys_p19296
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 480);



CREATE TABLE pspadm.t_pppi_old_sys_p19297
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 481);



CREATE TABLE pspadm.t_pppi_old_sys_p19298
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 482);



CREATE TABLE pspadm.t_pppi_old_sys_p19299
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 483);



CREATE TABLE pspadm.t_pppi_old_sys_p19300
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 484);



CREATE TABLE pspadm.t_pppi_old_sys_p19301
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 485);



CREATE TABLE pspadm.t_pppi_old_sys_p19302
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 486);



CREATE TABLE pspadm.t_pppi_old_sys_p19303
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 487);



CREATE TABLE pspadm.t_pppi_old_sys_p19304
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 488);



CREATE TABLE pspadm.t_pppi_old_sys_p19305
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 489);



CREATE TABLE pspadm.t_pppi_old_sys_p19306
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 490);



CREATE TABLE pspadm.t_pppi_old_sys_p19307
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 491);



CREATE TABLE pspadm.t_pppi_old_sys_p19308
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 492);



CREATE TABLE pspadm.t_pppi_old_sys_p19309
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 493);



CREATE TABLE pspadm.t_pppi_old_sys_p19310
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 494);



CREATE TABLE pspadm.t_pppi_old_sys_p19311
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 495);



CREATE TABLE pspadm.t_pppi_old_sys_p19312
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 496);



CREATE TABLE pspadm.t_pppi_old_sys_p19313
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 497);



CREATE TABLE pspadm.t_pppi_old_sys_p19314
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 498);



CREATE TABLE pspadm.t_pppi_old_sys_p19315
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 499);



CREATE TABLE pspadm.t_pppi_old_sys_p19316
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 500);



CREATE TABLE pspadm.t_pppi_old_sys_p19317
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 501);



CREATE TABLE pspadm.t_pppi_old_sys_p19318
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 502);



CREATE TABLE pspadm.t_pppi_old_sys_p19319
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 503);



CREATE TABLE pspadm.t_pppi_old_sys_p19320
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 504);



CREATE TABLE pspadm.t_pppi_old_sys_p19321
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 505);



CREATE TABLE pspadm.t_pppi_old_sys_p19322
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 506);



CREATE TABLE pspadm.t_pppi_old_sys_p19323
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 507);



CREATE TABLE pspadm.t_pppi_old_sys_p19324
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 508);



CREATE TABLE pspadm.t_pppi_old_sys_p19325
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 509);



CREATE TABLE pspadm.t_pppi_old_sys_p19326
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 510);



CREATE TABLE pspadm.t_pppi_old_sys_p19327
        PARTITION OF pspadm.t_pppi_old
        FOR VALUES WITH (MODULUS 512,REMAINDER 511);



CREATE TABLE pspadm.t_ppu_default
        PARTITION OF pspadm.t_ppu
        DEFAULT;



CREATE TABLE pspadm.t_ppu_sys_p19896
        PARTITION OF pspadm.t_ppu
        FOR VALUES WITH (MODULUS 1,REMAINDER 0);



CREATE TABLE pspadm.t_ppu_old_default
        PARTITION OF pspadm.t_ppu_old
        DEFAULT;



CREATE TABLE pspadm.t_ppu_old_sys_p19392
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 0);



CREATE TABLE pspadm.t_ppu_old_sys_p19393
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 1);



CREATE TABLE pspadm.t_ppu_old_sys_p19394
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 2);



CREATE TABLE pspadm.t_ppu_old_sys_p19395
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 3);



CREATE TABLE pspadm.t_ppu_old_sys_p19396
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 4);



CREATE TABLE pspadm.t_ppu_old_sys_p19397
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 5);



CREATE TABLE pspadm.t_ppu_old_sys_p19398
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 6);



CREATE TABLE pspadm.t_ppu_old_sys_p19399
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 7);



CREATE TABLE pspadm.t_ppu_old_sys_p19400
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 8);



CREATE TABLE pspadm.t_ppu_old_sys_p19401
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 9);



CREATE TABLE pspadm.t_ppu_old_sys_p19402
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 10);



CREATE TABLE pspadm.t_ppu_old_sys_p19403
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 11);



CREATE TABLE pspadm.t_ppu_old_sys_p19404
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 12);



CREATE TABLE pspadm.t_ppu_old_sys_p19405
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 13);



CREATE TABLE pspadm.t_ppu_old_sys_p19406
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 14);



CREATE TABLE pspadm.t_ppu_old_sys_p19407
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 15);



CREATE TABLE pspadm.t_ppu_old_sys_p19408
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 16);



CREATE TABLE pspadm.t_ppu_old_sys_p19409
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 17);



CREATE TABLE pspadm.t_ppu_old_sys_p19410
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 18);



CREATE TABLE pspadm.t_ppu_old_sys_p19411
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 19);



CREATE TABLE pspadm.t_ppu_old_sys_p19412
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 20);



CREATE TABLE pspadm.t_ppu_old_sys_p19413
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 21);



CREATE TABLE pspadm.t_ppu_old_sys_p19414
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 22);



CREATE TABLE pspadm.t_ppu_old_sys_p19415
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 23);



CREATE TABLE pspadm.t_ppu_old_sys_p19416
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 24);



CREATE TABLE pspadm.t_ppu_old_sys_p19417
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 25);



CREATE TABLE pspadm.t_ppu_old_sys_p19418
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 26);



CREATE TABLE pspadm.t_ppu_old_sys_p19419
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 27);



CREATE TABLE pspadm.t_ppu_old_sys_p19420
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 28);



CREATE TABLE pspadm.t_ppu_old_sys_p19421
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 29);



CREATE TABLE pspadm.t_ppu_old_sys_p19422
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 30);



CREATE TABLE pspadm.t_ppu_old_sys_p19423
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 31);



CREATE TABLE pspadm.t_ppu_old_sys_p19424
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 32);



CREATE TABLE pspadm.t_ppu_old_sys_p19425
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 33);



CREATE TABLE pspadm.t_ppu_old_sys_p19426
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 34);



CREATE TABLE pspadm.t_ppu_old_sys_p19427
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 35);



CREATE TABLE pspadm.t_ppu_old_sys_p19428
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 36);



CREATE TABLE pspadm.t_ppu_old_sys_p19429
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 37);



CREATE TABLE pspadm.t_ppu_old_sys_p19430
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 38);



CREATE TABLE pspadm.t_ppu_old_sys_p19431
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 39);



CREATE TABLE pspadm.t_ppu_old_sys_p19432
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 40);



CREATE TABLE pspadm.t_ppu_old_sys_p19433
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 41);



CREATE TABLE pspadm.t_ppu_old_sys_p19434
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 42);



CREATE TABLE pspadm.t_ppu_old_sys_p19435
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 43);



CREATE TABLE pspadm.t_ppu_old_sys_p19436
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 44);



CREATE TABLE pspadm.t_ppu_old_sys_p19437
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 45);



CREATE TABLE pspadm.t_ppu_old_sys_p19438
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 46);



CREATE TABLE pspadm.t_ppu_old_sys_p19439
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 47);



CREATE TABLE pspadm.t_ppu_old_sys_p19440
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 48);



CREATE TABLE pspadm.t_ppu_old_sys_p19441
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 49);



CREATE TABLE pspadm.t_ppu_old_sys_p19442
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 50);



CREATE TABLE pspadm.t_ppu_old_sys_p19443
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 51);



CREATE TABLE pspadm.t_ppu_old_sys_p19444
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 52);



CREATE TABLE pspadm.t_ppu_old_sys_p19445
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 53);



CREATE TABLE pspadm.t_ppu_old_sys_p19446
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 54);



CREATE TABLE pspadm.t_ppu_old_sys_p19447
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 55);



CREATE TABLE pspadm.t_ppu_old_sys_p19448
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 56);



CREATE TABLE pspadm.t_ppu_old_sys_p19449
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 57);



CREATE TABLE pspadm.t_ppu_old_sys_p19450
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 58);



CREATE TABLE pspadm.t_ppu_old_sys_p19451
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 59);



CREATE TABLE pspadm.t_ppu_old_sys_p19452
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 60);



CREATE TABLE pspadm.t_ppu_old_sys_p19453
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 61);



CREATE TABLE pspadm.t_ppu_old_sys_p19454
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 62);



CREATE TABLE pspadm.t_ppu_old_sys_p19455
        PARTITION OF pspadm.t_ppu_old
        FOR VALUES WITH (MODULUS 64,REMAINDER 63);



