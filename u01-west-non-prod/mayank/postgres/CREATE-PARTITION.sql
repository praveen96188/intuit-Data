-- ------------ Write CREATE-PARTITION-stage scripts -----------

CREATE TABLE pspadm.psp_batch_job_audit_log_part_8_2013
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM (MINVALUE) TO ('2013-08-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p12019
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-08-01 00:00:00') TO ('2018-09-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p12056
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-07-01 00:00:00') TO ('2018-08-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p12236
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-11-01 00:00:00') TO ('2018-12-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p12336
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-10-01 00:00:00') TO ('2018-11-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p13595
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-09-01 00:00:00') TO ('2018-10-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p15181
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2015-11-01 00:00:00') TO ('2016-02-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p15201
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-02-01 00:00:00') TO ('2016-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p15221
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-03-01 00:00:00') TO ('2016-04-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p15241
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-04-01 00:00:00') TO ('2016-05-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p15261
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-05-01 00:00:00') TO ('2016-06-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p15281
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-06-01 00:00:00') TO ('2016-07-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1781
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-07-01 00:00:00') TO ('2016-08-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1801
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-08-01 00:00:00') TO ('2016-09-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1821
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-09-01 00:00:00') TO ('2016-10-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1841
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-07-01 00:00:00') TO ('2017-08-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1861
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-10-01 00:00:00') TO ('2016-11-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1881
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-11-01 00:00:00') TO ('2016-12-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1901
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2016-12-01 00:00:00') TO ('2017-01-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1921
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-01-01 00:00:00') TO ('2017-02-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1941
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-02-01 00:00:00') TO ('2017-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1961
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-04-01 00:00:00') TO ('2017-05-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p1981
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-03-01 00:00:00') TO ('2017-04-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2001
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-05-01 00:00:00') TO ('2017-06-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2021
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-08-01 00:00:00') TO ('2017-09-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2041
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-06-01 00:00:00') TO ('2017-07-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2061
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-12-01 00:00:00') TO ('2018-01-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2081
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-06-01 00:00:00') TO ('2018-07-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2101
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-07-01 00:00:00') TO ('2019-08-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2121
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-07-01 00:00:00') TO ('2020-08-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2141
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-09-01 00:00:00') TO ('2017-10-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2161
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-11-01 00:00:00') TO ('2017-12-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2181
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2017-10-01 00:00:00') TO ('2017-11-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2201
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2022-03-01 00:00:00') TO ('2027-10-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p22155
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-12-01 00:00:00') TO ('2019-01-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p22815
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-01-01 00:00:00') TO ('2019-02-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p22855
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-09-01 00:00:00') TO ('2019-10-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p24155
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-02-01 00:00:00') TO ('2019-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p2446
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2014-07-01 00:00:00') TO ('2015-01-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p25175
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-03-01 00:00:00') TO ('2019-04-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p25559
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-04-01 00:00:00') TO ('2019-05-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p3478
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-06-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p3492
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-08-01 00:00:00') TO ('2019-09-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p3493
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-08-01 00:00:00') TO ('2020-09-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p3503
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2028-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p3646
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-01-01 00:00:00') TO ('2018-02-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p4035
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-01-01 00:00:00') TO ('2020-02-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p4115
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-10-01 00:00:00') TO ('2019-11-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p4688
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-12-01 00:00:00') TO ('2020-01-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p4765
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-11-01 00:00:00') TO ('2019-12-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5126
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2015-01-01 00:00:00') TO ('2015-11-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5154
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-05-01 00:00:00') TO ('2019-06-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5155
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2019-06-01 00:00:00') TO ('2019-07-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5207
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-04-01 00:00:00') TO ('2018-05-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5359
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-11-01 00:00:00') TO ('2020-12-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5924
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-03-01 00:00:00') TO ('2020-04-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5980
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-04-01 00:00:00') TO ('2020-05-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p5990
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-02-01 00:00:00') TO ('2020-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p6019
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2013-08-01 00:00:00') TO ('2014-07-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p6028
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-05-01 00:00:00') TO ('2020-06-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p6051
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-02-01 00:00:00') TO ('2018-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p7086
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-03-01 00:00:00') TO ('2018-04-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p7115
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-06-01 00:00:00') TO ('2020-07-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p7144
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-12-01 00:00:00') TO ('2021-01-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p7486
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-09-01 00:00:00') TO ('2020-10-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p7497
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2020-10-01 00:00:00') TO ('2020-11-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p7702
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-08-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p7712
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-12-01 00:00:00') TO ('2022-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p8390
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-01-01 00:00:00') TO ('2021-02-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p8445
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-02-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p8716
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-04-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p8793
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-04-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p9145
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-06-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p9491
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2018-05-01 00:00:00') TO ('2018-06-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p9736
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-10-01 00:00:00') TO ('2021-12-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p9886
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-08-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_batch_job_audit_log_sys_p9958
        PARTITION OF pspadm.psp_batch_job_audit_log
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_2008
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_9999
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m012021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2021-02-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-02-01 00:00:00') TO ('2009-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m022021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-02-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-03-01 00:00:00') TO ('2009-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m032021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-04-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-04-01 00:00:00') TO ('2009-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m042021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-04-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-05-01 00:00:00') TO ('2009-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m052021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-06-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-06-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m062021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-06-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2009-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m072021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-08-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m082021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-08-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m092021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-10-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m102021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-10-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m112021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2021-12-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122009
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122010
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_entry_detail_record_entry_detail_rcd_m122021
        PARTITION OF pspadm.psp_entry_detail_record
        FOR VALUES FROM ('2021-12-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_2008
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_9999
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m012021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2021-02-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-02-01 00:00:00') TO ('2009-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-02-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m022021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-02-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-03-01 00:00:00') TO ('2009-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m032021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-04-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-04-01 00:00:00') TO ('2009-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-04-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m042021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-04-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-05-01 00:00:00') TO ('2009-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m052021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-06-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-06-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-06-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m062021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-06-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2009-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m072021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-08-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-08-01 00:00:00') TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-08-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m082021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-08-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m092021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-10-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-10-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-10-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m102021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-10-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2009-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2010-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m112021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2021-12-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122009
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2009-12-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122010
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2010-12-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_trans_state_financial_txn_state_m122021
        PARTITION OF pspadm.psp_financial_trans_state
        FOR VALUES FROM ('2021-12-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_9999
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg12010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-03-01 00:00:00') TO ('2009-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg22021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-05-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg32021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg42021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg52021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62008
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62009
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62010
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_financial_transaction_financial_txn_mg62021
        PARTITION OF pspadm.psp_financial_transaction
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_2008
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_9999
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12009
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12010
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12011
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba12021
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22009
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22010
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22011
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_ledger_balance_ldgrbal_ba22021
        PARTITION OF pspadm.psp_ledger_balance
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_9999
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg12021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2021-03-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-03-01 00:00:00') TO ('2009-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg22021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-03-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-05-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg32021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg42021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg52021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62008
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62009
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62010
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_money_movement_transaction_money_movement_txn_mg62021
        PARTITION OF pspadm.psp_money_movement_transaction
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_2008
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_9999
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12009
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12010
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12011
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba12021
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22009
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22010
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22011
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_paycheck_ba22021
        PARTITION OF pspadm.psp_paycheck
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_2008
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_9999
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12009
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12010
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12011
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2011-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba12021
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2012-01-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22009
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22010
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22011
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2011-07-01 00:00:00') TO ('2012-01-01 00:00:00');



CREATE TABLE pspadm.psp_paycheck_split_paychksplit_ba22021
        PARTITION OF pspadm.psp_paycheck_split
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_9999
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2022-01-01 00:00:00') TO (MAXVALUE);



CREATE TABLE pspadm.repl_issue_repl_issue_mg12009
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2009-01-01 00:00:00') TO ('2009-03-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg12010
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2010-01-01 00:00:00') TO ('2010-03-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg22009
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2009-03-01 00:00:00') TO ('2009-05-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg22010
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2010-03-01 00:00:00') TO ('2010-05-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg22021
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2011-01-01 00:00:00') TO ('2021-05-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg32009
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2009-05-01 00:00:00') TO ('2009-07-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg32010
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2010-05-01 00:00:00') TO ('2010-07-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg32021
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2021-05-01 00:00:00') TO ('2021-07-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg42009
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2009-07-01 00:00:00') TO ('2009-09-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg42010
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2010-07-01 00:00:00') TO ('2010-09-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg42021
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2021-07-01 00:00:00') TO ('2021-09-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg52009
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2009-09-01 00:00:00') TO ('2009-11-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg52010
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2010-09-01 00:00:00') TO ('2010-11-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg52021
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2021-09-01 00:00:00') TO ('2021-11-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg62008
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM (MINVALUE) TO ('2009-01-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg62009
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2009-11-01 00:00:00') TO ('2010-01-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg62010
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2010-11-01 00:00:00') TO ('2011-01-01 00:00:00');



CREATE TABLE pspadm.repl_issue_repl_issue_mg62021
        PARTITION OF pspadm.repl_issue
        FOR VALUES FROM ('2021-11-01 00:00:00') TO ('2022-01-01 00:00:00');



CREATE TABLE pspadm.tpr_metadata_sys_p16635
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 0);



CREATE TABLE pspadm.tpr_metadata_sys_p16636
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 1);



CREATE TABLE pspadm.tpr_metadata_sys_p16637
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 2);



CREATE TABLE pspadm.tpr_metadata_sys_p16638
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 3);



CREATE TABLE pspadm.tpr_metadata_sys_p16639
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 4);



CREATE TABLE pspadm.tpr_metadata_sys_p16640
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 5);



CREATE TABLE pspadm.tpr_metadata_sys_p16641
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 6);



CREATE TABLE pspadm.tpr_metadata_sys_p16642
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 7);



CREATE TABLE pspadm.tpr_metadata_sys_p16643
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 8);



CREATE TABLE pspadm.tpr_metadata_sys_p16644
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 9);



CREATE TABLE pspadm.tpr_metadata_sys_p16645
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 10);



CREATE TABLE pspadm.tpr_metadata_sys_p16646
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 11);



CREATE TABLE pspadm.tpr_metadata_sys_p16647
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 12);



CREATE TABLE pspadm.tpr_metadata_sys_p16648
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 13);



CREATE TABLE pspadm.tpr_metadata_sys_p16649
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 14);



CREATE TABLE pspadm.tpr_metadata_sys_p16650
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 15);



CREATE TABLE pspadm.tpr_metadata_sys_p16651
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 16);



CREATE TABLE pspadm.tpr_metadata_sys_p16652
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 17);



CREATE TABLE pspadm.tpr_metadata_sys_p16653
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 18);



CREATE TABLE pspadm.tpr_metadata_sys_p16654
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 19);



CREATE TABLE pspadm.tpr_metadata_sys_p16655
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 20);



CREATE TABLE pspadm.tpr_metadata_sys_p16656
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 21);



CREATE TABLE pspadm.tpr_metadata_sys_p16657
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 22);



CREATE TABLE pspadm.tpr_metadata_sys_p16658
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 23);



CREATE TABLE pspadm.tpr_metadata_sys_p16659
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 24);



CREATE TABLE pspadm.tpr_metadata_sys_p16660
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 25);



CREATE TABLE pspadm.tpr_metadata_sys_p16661
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 26);



CREATE TABLE pspadm.tpr_metadata_sys_p16662
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 27);



CREATE TABLE pspadm.tpr_metadata_sys_p16663
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 28);



CREATE TABLE pspadm.tpr_metadata_sys_p16664
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 29);



CREATE TABLE pspadm.tpr_metadata_sys_p16665
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 30);



CREATE TABLE pspadm.tpr_metadata_sys_p16666
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 31);



CREATE TABLE pspadm.tpr_metadata_sys_p16667
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 32);



CREATE TABLE pspadm.tpr_metadata_sys_p16668
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 33);



CREATE TABLE pspadm.tpr_metadata_sys_p16669
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 34);



CREATE TABLE pspadm.tpr_metadata_sys_p16670
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 35);



CREATE TABLE pspadm.tpr_metadata_sys_p16671
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 36);



CREATE TABLE pspadm.tpr_metadata_sys_p16672
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 37);



CREATE TABLE pspadm.tpr_metadata_sys_p16673
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 38);



CREATE TABLE pspadm.tpr_metadata_sys_p16674
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 39);



CREATE TABLE pspadm.tpr_metadata_sys_p16675
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 40);



CREATE TABLE pspadm.tpr_metadata_sys_p16676
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 41);



CREATE TABLE pspadm.tpr_metadata_sys_p16677
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 42);



CREATE TABLE pspadm.tpr_metadata_sys_p16678
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 43);



CREATE TABLE pspadm.tpr_metadata_sys_p16679
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 44);



CREATE TABLE pspadm.tpr_metadata_sys_p16680
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 45);



CREATE TABLE pspadm.tpr_metadata_sys_p16681
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 46);



CREATE TABLE pspadm.tpr_metadata_sys_p16682
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 47);



CREATE TABLE pspadm.tpr_metadata_sys_p16683
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 48);



CREATE TABLE pspadm.tpr_metadata_sys_p16684
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 49);



CREATE TABLE pspadm.tpr_metadata_sys_p16685
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 50);



CREATE TABLE pspadm.tpr_metadata_sys_p16686
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 51);



CREATE TABLE pspadm.tpr_metadata_sys_p16687
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 52);



CREATE TABLE pspadm.tpr_metadata_sys_p16688
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 53);



CREATE TABLE pspadm.tpr_metadata_sys_p16689
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 54);



CREATE TABLE pspadm.tpr_metadata_sys_p16690
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 55);



CREATE TABLE pspadm.tpr_metadata_sys_p16691
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 56);



CREATE TABLE pspadm.tpr_metadata_sys_p16692
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 57);



CREATE TABLE pspadm.tpr_metadata_sys_p16693
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 58);



CREATE TABLE pspadm.tpr_metadata_sys_p16694
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 59);



CREATE TABLE pspadm.tpr_metadata_sys_p16695
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 60);



CREATE TABLE pspadm.tpr_metadata_sys_p16696
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 61);



CREATE TABLE pspadm.tpr_metadata_sys_p16697
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 62);



CREATE TABLE pspadm.tpr_metadata_sys_p16698
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 63);



CREATE TABLE pspadm.tpr_metadata_sys_p16699
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 64);



CREATE TABLE pspadm.tpr_metadata_sys_p16700
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 65);



CREATE TABLE pspadm.tpr_metadata_sys_p16701
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 66);



CREATE TABLE pspadm.tpr_metadata_sys_p16702
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 67);



CREATE TABLE pspadm.tpr_metadata_sys_p16703
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 68);



CREATE TABLE pspadm.tpr_metadata_sys_p16704
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 69);



CREATE TABLE pspadm.tpr_metadata_sys_p16705
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 70);



CREATE TABLE pspadm.tpr_metadata_sys_p16706
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 71);



CREATE TABLE pspadm.tpr_metadata_sys_p16707
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 72);



CREATE TABLE pspadm.tpr_metadata_sys_p16708
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 73);



CREATE TABLE pspadm.tpr_metadata_sys_p16709
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 74);



CREATE TABLE pspadm.tpr_metadata_sys_p16710
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 75);



CREATE TABLE pspadm.tpr_metadata_sys_p16711
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 76);



CREATE TABLE pspadm.tpr_metadata_sys_p16712
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 77);



CREATE TABLE pspadm.tpr_metadata_sys_p16713
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 78);



CREATE TABLE pspadm.tpr_metadata_sys_p16714
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 79);



CREATE TABLE pspadm.tpr_metadata_sys_p16715
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 80);



CREATE TABLE pspadm.tpr_metadata_sys_p16716
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 81);



CREATE TABLE pspadm.tpr_metadata_sys_p16717
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 82);



CREATE TABLE pspadm.tpr_metadata_sys_p16718
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 83);



CREATE TABLE pspadm.tpr_metadata_sys_p16719
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 84);



CREATE TABLE pspadm.tpr_metadata_sys_p16720
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 85);



CREATE TABLE pspadm.tpr_metadata_sys_p16721
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 86);



CREATE TABLE pspadm.tpr_metadata_sys_p16722
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 87);



CREATE TABLE pspadm.tpr_metadata_sys_p16723
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 88);



CREATE TABLE pspadm.tpr_metadata_sys_p16724
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 89);



CREATE TABLE pspadm.tpr_metadata_sys_p16725
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 90);



CREATE TABLE pspadm.tpr_metadata_sys_p16726
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 91);



CREATE TABLE pspadm.tpr_metadata_sys_p16727
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 92);



CREATE TABLE pspadm.tpr_metadata_sys_p16728
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 93);



CREATE TABLE pspadm.tpr_metadata_sys_p16729
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 94);



CREATE TABLE pspadm.tpr_metadata_sys_p16730
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 95);



CREATE TABLE pspadm.tpr_metadata_sys_p16731
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 96);



CREATE TABLE pspadm.tpr_metadata_sys_p16732
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 97);



CREATE TABLE pspadm.tpr_metadata_sys_p16733
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 98);



CREATE TABLE pspadm.tpr_metadata_sys_p16734
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 99);



CREATE TABLE pspadm.tpr_metadata_sys_p16735
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 100);



CREATE TABLE pspadm.tpr_metadata_sys_p16736
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 101);



CREATE TABLE pspadm.tpr_metadata_sys_p16737
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 102);



CREATE TABLE pspadm.tpr_metadata_sys_p16738
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 103);



CREATE TABLE pspadm.tpr_metadata_sys_p16739
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 104);



CREATE TABLE pspadm.tpr_metadata_sys_p16740
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 105);



CREATE TABLE pspadm.tpr_metadata_sys_p16741
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 106);



CREATE TABLE pspadm.tpr_metadata_sys_p16742
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 107);



CREATE TABLE pspadm.tpr_metadata_sys_p16743
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 108);



CREATE TABLE pspadm.tpr_metadata_sys_p16744
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 109);



CREATE TABLE pspadm.tpr_metadata_sys_p16745
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 110);



CREATE TABLE pspadm.tpr_metadata_sys_p16746
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 111);



CREATE TABLE pspadm.tpr_metadata_sys_p16747
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 112);



CREATE TABLE pspadm.tpr_metadata_sys_p16748
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 113);



CREATE TABLE pspadm.tpr_metadata_sys_p16749
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 114);



CREATE TABLE pspadm.tpr_metadata_sys_p16750
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 115);



CREATE TABLE pspadm.tpr_metadata_sys_p16751
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 116);



CREATE TABLE pspadm.tpr_metadata_sys_p16752
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 117);



CREATE TABLE pspadm.tpr_metadata_sys_p16753
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 118);



CREATE TABLE pspadm.tpr_metadata_sys_p16754
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 119);



CREATE TABLE pspadm.tpr_metadata_sys_p16755
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 120);



CREATE TABLE pspadm.tpr_metadata_sys_p16756
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 121);



CREATE TABLE pspadm.tpr_metadata_sys_p16757
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 122);



CREATE TABLE pspadm.tpr_metadata_sys_p16758
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 123);



CREATE TABLE pspadm.tpr_metadata_sys_p16759
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 124);



CREATE TABLE pspadm.tpr_metadata_sys_p16760
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 125);



CREATE TABLE pspadm.tpr_metadata_sys_p16761
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 126);



CREATE TABLE pspadm.tpr_metadata_sys_p16762
        PARTITION OF pspadm.tpr_metadata
        FOR VALUES WITH (MODULUS 128,REMAINDER 127);



