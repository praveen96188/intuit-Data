load data
infile '/u01/scripts/mayank/PSP_SymphonyUsage_20220301063346-delta-error.csv'
into table mchoubey.psp_symphony_usage_delta_error
fields terminated by ','
(Groupid,SiteGeneratorPortalproduct,EventCode,Quantity,Timestamp)
