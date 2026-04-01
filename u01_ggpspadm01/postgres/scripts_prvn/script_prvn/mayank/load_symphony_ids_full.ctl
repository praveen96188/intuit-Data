load data
infile '/u01/scripts/mayank/PSP_SymphonyUsage_20220301063346-decrypt-all.csv'
into table mchoubey.psp_symphony_usage_all
fields terminated by ','
(Groupid,SiteGeneratorPortalproduct,EventCode,Quantity,Timestamp)
